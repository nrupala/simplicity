package com.simplicity.rag;

import com.simplicity.core.domain.DomainModels.*;
import com.simplicity.core.domain.QueryModels.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Simplicity's Lucene-based search engine.
 * 
 * Provides hybrid search combining:
 * - BM25 keyword matching
 * - Semantic vector search (via embeddings)
 * - Personalization boosting
 */
public class SimplicitySearchEngine implements AutoCloseable {

    private final IndexWriter indexWriter;
    private final IndexReader indexReader;
    private final SearcherManager searcherManager;
    private final Analyzer analyzer;
    private final Analyzer queryAnalyzer;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile boolean closed = false;

    public SimplicitySearchEngine(Path indexPath) throws IOException {
        this(indexPath, IndexConfig.DEFAULT);
    }

    public SimplicitySearchEngine(Path indexPath, IndexConfig config) throws IOException {
        // Create analyzers
        this.analyzer = createDocumentAnalyzer(config);
        this.queryAnalyzer = createQueryAnalyzer(config);

        // Setup directory
        Directory directory = FSDirectory.open(indexPath);
        
        // Create index writer
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        iwc.setSimilarity(new SimplicitySimilarity());
        
        this.indexWriter = new IndexWriter(directory, iwc);
        
        // Create searcher manager for near-real-time search
        this.searcherManager = new SearcherManager(indexWriter, new BasicIndexWriterConfig.AppliedHistory());
        this.indexWriter.commit();
    }

    /**
     * Index a document for search.
     */
    public void indexDocument(Document doc) throws IOException {
        lock.writeLock().lock();
        try {
            ensureOpen();
            indexWriter.addDocument(toLuceneDoc(doc));
            indexWriter.commit();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Index multiple documents in batch.
     */
    public void indexDocuments(List<Document> docs) throws IOException {
        lock.writeLock().lock();
        try {
            ensureOpen();
            for (Document doc : docs) {
                indexWriter.addDocument(toLuceneDoc(doc));
            }
            indexWriter.commit();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Execute a personalized search query.
     */
    public List<SearchResult> search(QueryRequest request, UserContext userContext) throws IOException {
        lock.readLock().lock();
        try {
            ensureOpen();
            IndexSearcher searcher = searcherManager.acquire();
            try {
                return executeSearch(searcher, request, userContext);
            } finally {
                searcherManager.release(searcher);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private List<SearchResult> executeSearch(IndexSearcher searcher, QueryRequest request, UserContext ctx) 
            throws IOException {
        
        // Build the query
        Query query = buildQuery(request.query(), request.options());
        
        // Execute search
        TopDocs topDocs = searcher.search(query, request.options().maxResults() * 2);
        
        // Process results
        List<SearchResult> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document luceneDoc = searcher.doc(scoreDoc.doc);
            SearchResult result = toSearchResult(luceneDoc, scoreDoc, ctx);
            results.add(result);
        }

        // Apply personalization
        if (request.options().includeReranking()) {
            results = rerankResults(results, ctx, request.options());
        }

        // Return top results
        return results.subList(0, Math.min(results.size(), request.options().maxResults()));
    }

    private Query buildQuery(String queryText, QueryRequest.QueryOptions options) {
        try {
            // Build a BooleanQuery combining multiple query types
            BooleanQuery.Builder builder = new BooleanQuery.Builder();

            // Main text query - use MultiFieldQueryParser for title + content
            QueryParser parser = new MultiFieldQueryParser(
                new String[]{"title", "content", "tags"},
                queryAnalyzer
            );
            parser.setDefaultOperator(QueryParser.Operator.AND);
            parser.setFuzzyMinTerm(3);
            
            Query textQuery = parser.parse(QueryParser.escape(queryText));
            builder.add(textQuery, BooleanClause.Occur.MUST);

            // Add filters if specified
            for (String filter : options.filters()) {
                Query filterQuery = new TermQuery(new Term("tags", filter.toLowerCase()));
                builder.add(filterQuery, BooleanClause.Occur.FILTER);
            }

            return builder.build();
        } catch (Exception e) {
            // Fallback to simple term query
            return new MatchAllDocsQuery();
        }
    }

    private List<SearchResult> rerankResults(List<SearchResult> results, UserContext ctx, 
            QueryRequest.QueryOptions options) {
        
        // Get user interests for boosting
        Map<String, Double> interestBoost = buildInterestBoost(ctx.interests());
        
        List<SearchResult> reranked = new ArrayList<>(results);
        reranked.sort((a, b) -> {
            double scoreA = a.combinedScore() + getPersonalBoost(a, interestBoost);
            double scoreB = b.combinedScore() + getPersonalBoost(b, interestBoost);
            return Double.compare(scoreB, scoreA);
        });

        return reranked;
    }

    private double getPersonalBoost(SearchResult result, Map<String, Double> interestBoost) {
        double boost = 0.0;
        for (String tag : result.document().tags()) {
            Double interest = interestBoost.get(tag.toLowerCase());
            if (interest != null) {
                boost += interest * 0.5; // Weighted by interest strength
            }
        }
        return boost;
    }

    private Map<String, Double> buildInterestBoost(List<Interest> interests) {
        Map<String, Double> boost = new HashMap<>();
        for (Interest interest : interests) {
            // Boost for the topic
            boost.merge(interest.topic().toLowerCase(), interest.strength(), Double::max);
            
            // Boost for keywords
            for (String keyword : interest.keywords()) {
                boost.merge(keyword.toLowerCase(), interest.strength() * 0.8, Double::max);
            }
        }
        return boost;
    }

    private Document toLuceneDoc(Document doc) {
        org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
        
        luceneDoc.add(new TextField("id", doc.id().toString(), Field.Store.YES));
        luceneDoc.add(new TextField("title", doc.title(), Field.Store.YES));
        luceneDoc.add(new TextField("content", doc.content(), Field.Store.YES));
        luceneDoc.add(new StoredField("source", doc.source()));
        luceneDoc.add(new StoredField("url", doc.url() != null ? doc.url() : ""));
        luceneDoc.add(new StoredField("authors", String.join(",", doc.authors())));
        luceneDoc.add(new StoredField("publishedAt", doc.publishedAt() != null ? 
            doc.publishedAt().toEpochMilli() : 0));
        luceneDoc.add(new StoredField("indexedAt", Instant.now().toEpochMilli()));
        
        // Tags as keyword field for filtering
        for (String tag : doc.tags()) {
            luceneDoc.add(new KeywordField("tags", tag, Field.Store.YES));
        }
        
        // Metadata
        for (Map.Entry<String, String> entry : doc.metadata().entrySet()) {
            luceneDoc.add(new StoredField("meta_" + entry.getKey(), entry.getValue()));
        }
        
        return luceneDoc;
    }

    private SearchResult toSearchResult(org.apache.lucene.document.Document luceneDoc, 
            ScoreDoc scoreDoc, UserContext ctx) {
        
        // Parse stored fields
        UUID id = UUID.fromString(luceneDoc.get("id"));
        String title = luceneDoc.get("title");
        String content = luceneDoc.get("content");
        String source = luceneDoc.get("source");
        String url = luceneDoc.get("url");
        List<String> authors = List.of(luceneDoc.get("aws").split(","));
        Instant publishedAt = luceneDoc.getField("publishedAt") != null ? 
            Instant.ofEpochMilli(luceneDoc.getField("publishedAt").numericValue().longValue()) : null;
        List<String> tags = List.of(luceneDoc.getValues("tags"));
        
        Document doc = new Document(
            id, title, content, source, url, authors, 
            publishedAt, Instant.now(), Map.of(), tags, scoreDoc.score
        );
        
        double personalScore = calculatePersonalScore(doc, ctx);
        
        return new SearchResult(
            doc,
            scoreDoc.score,
            personalScore,
            scoreDoc.score * 0.7 + personalScore * 0.3,
            List.of(), // Matched terms
            doc.preview(200) // Highlight
        );
    }

    private double calculatePersonalScore(Document doc, UserContext ctx) {
        if (ctx == null) return 0.0;
        
        double score = 0.0;
        
        // Boost based on user interests
        for (Interest interest : ctx.interests()) {
            for (String tag : doc.tags()) {
                if (interest.keywords().contains(tag.toLowerCase())) {
                    score += interest.strength();
                }
            }
        }
        
        // Boost based on domain context
        for (String domain : ctx.orgContext().domainNames()) {
            for (String tag : doc.tags()) {
                if (tag.equalsIgnoreCase(domain)) {
                    score += 0.5;
                }
            }
        }
        
        return score;
    }

    private Analyzer createDocumentAnalyzer(IndexConfig config) {
        return new StandardAnalyzer();
    }

    private Analyzer createQueryAnalyzer(IndexConfig config) {
        return new StandardAnalyzer();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Search engine is closed");
        }
    }

    @Override
    public void close() throws IOException {
        lock.writeLock().lock();
        try {
            if (!closed) {
                closed = true;
                searcherManager.close();
                indexWriter.close();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Configuration for the search engine.
     */
    public record IndexConfig(
        int maxFieldLength,
        int maxClauseCount,
        double k1,
        double b
    ) {
        public static IndexConfig DEFAULT = new IndexConfig(10000, 1024, 1.2, 0.75);
    }

    /**
     * Custom similarity that incorporates field importance.
     */
    static class SimplicitySimilarity extends Similarity {
        @Override
        public long computeNorm(FieldInvertState state) {
            return state.getBoost();
        }

        @Override
        public SimplicityScorer scorer(float boost, LeafReaderContext context) {
            return new SimplicityScorer(boost);
        }
    }

    static class SimplicityScorer extends SimularityScorer {
        private final float boost;

        SimplicityScorer(float boost) {
            this.boost = boost;
        }

        @Override
        public float score() {
            return boost;
        }
    }

    // Abstract class needed for Similarity
    static abstract class SimularityScorer extends org.apache.lucene.search.Similarity.Scorer {
        protected SimularityScorer() {}
    }
}
