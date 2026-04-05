const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const crypto = require('crypto');
const inferenceEngine = require('./inference');

const dbPath = path.join(__dirname, '../../../data/knowledge.db');
const db = new sqlite3.Database(dbPath);

db.serialize(() => {
    db.run(`
        CREATE TABLE IF NOT EXISTS embeddings (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            chunk_id INTEGER NOT NULL,
            user_id TEXT NOT NULL,
            vector TEXT NOT NULL,
            dimension INTEGER NOT NULL,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (chunk_id) REFERENCES document_chunks(id) ON DELETE CASCADE
        )
    `);
    db.run(`
        CREATE TABLE IF NOT EXISTS cache_tier (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            chunk_id INTEGER NOT NULL UNIQUE,
            user_id TEXT NOT NULL,
            tier TEXT NOT NULL DEFAULT 'cold',
            access_count INTEGER DEFAULT 0,
            last_access DATETIME DEFAULT CURRENT_TIMESTAMP,
            cached_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (chunk_id) REFERENCES document_chunks(id) ON DELETE CASCADE
        )
    `);
    db.run('CREATE INDEX IF NOT EXISTS idx_embeddings_user ON embeddings(user_id)');
    db.run('CREATE INDEX IF NOT EXISTS idx_embeddings_chunk ON embeddings(chunk_id)');
    db.run('CREATE INDEX IF NOT EXISTS idx_cache_tier ON cache_tier(tier)');
    db.run('CREATE INDEX IF NOT EXISTS idx_cache_user ON cache_tier(user_id)');
});

// Cosine similarity between two vectors
function cosineSimilarity(a, b) {
    if (a.length !== b.length) return 0;
    let dotProduct = 0, normA = 0, normB = 0;
    for (let i = 0; i < a.length; i++) {
        dotProduct += a[i] * b[i];
        normA += a[i] * a[i];
        normB += b[i] * b[i];
    }
    const denominator = Math.sqrt(normA) * Math.sqrt(normB);
    return denominator === 0 ? 0 : dotProduct / denominator;
}

// Generate embedding for text using loaded model
async function generateEmbedding(text) {
    try {
        const vector = await inferenceEngine.generateEmbedding(text);
        if (vector) return vector;
    } catch (err) {
        console.log('Embedding generation failed, using fallback:', err.message);
    }
    // Fallback: simple hash-based pseudo-embedding (not semantic, but functional)
    return generateFallbackEmbedding(text);
}

// Fallback embedding using character n-gram hashing
function generateFallbackEmbedding(text, dim = 384) {
    const vector = new Float32Array(dim);
    const ngrams = [];
    for (let i = 0; i < text.length - 2; i++) {
        ngrams.push(text.substring(i, i + 3).toLowerCase());
    }

    for (const ngram of ngrams) {
        const hash = crypto.createHash('md5').update(ngram).digest('hex');
        for (let i = 0; i < 8 && i < dim; i += 2) {
            const val = parseInt(hash.substring(i, i + 2), 16) / 255 - 0.5;
            const idx = parseInt(hash.substring(i + 2, i + 4), 16) % dim;
            vector[idx] += val;
        }
    }

    // Normalize
    let norm = 0;
    for (let i = 0; i < dim; i++) norm += vector[i] * vector[i];
    norm = Math.sqrt(norm);
    if (norm > 0) {
        for (let i = 0; i < dim; i++) vector[i] /= norm;
    }

    return Array.from(vector);
}

// Store embedding for a chunk
async function storeEmbedding(chunkId, userId, vector) {
    return new Promise((resolve, reject) => {
        db.run(
            'INSERT OR REPLACE INTO embeddings (chunk_id, user_id, vector, dimension) VALUES (?, ?, ?, ?)',
            [chunkId, userId, JSON.stringify(vector), vector.length],
            (err) => err ? reject(err) : resolve()
        );
    });
}

// Generate and store embeddings for all unembedded chunks
async function embedAllChunks(userId) {
    const chunks = await new Promise((resolve, reject) => {
        db.all(
            `SELECT dc.id, dc.content FROM document_chunks dc
             LEFT JOIN embeddings e ON dc.id = e.chunk_id
             WHERE dc.user_id = ? AND e.id IS NULL`,
            [userId],
            (err, rows) => err ? reject(err) : resolve(rows || [])
        );
    });

    let embedded = 0;
    for (const chunk of chunks) {
        try {
            const vector = await generateEmbedding(chunk.content);
            await storeEmbedding(chunk.id, userId, vector);
            embedded++;
        } catch (err) {
            console.error(`Failed to embed chunk ${chunk.id}:`, err.message);
        }
    }

    return { total: chunks.length, embedded };
}

// Semantic search using embeddings
async function semanticSearch(userId, query, limit = 5) {
    // Get query embedding
    const queryVector = await generateEmbedding(query);

    // Get all embeddings for user
    const embeddings = await new Promise((resolve, reject) => {
        db.all(
            `SELECT e.chunk_id, e.vector, dc.content, dc.chunk_index, d.filename, d.file_type
             FROM embeddings e
             JOIN document_chunks dc ON e.chunk_id = dc.id
             JOIN documents d ON dc.document_id = d.id
             WHERE e.user_id = ?`,
            [userId],
            (err, rows) => err ? reject(err) : resolve(rows || [])
        );
    });

    // Score each chunk
    const scored = embeddings.map(e => {
        const vector = JSON.parse(e.vector);
        const similarity = cosineSimilarity(queryVector, vector);
        return {
            chunk_id: e.chunk_id,
            content: e.content,
            filename: e.filename,
            file_type: e.file_type,
            chunk_index: e.chunk_index,
            similarity
        };
    });

    // Sort by similarity and return top results
    scored.sort((a, b) => b.similarity - a.similarity);
    const results = scored.slice(0, limit);

    // Update cache tier for accessed chunks
    for (const result of results) {
        await updateCacheTier(result.chunk_id, userId);
    }

    return results;
}

// Hybrid search: embeddings + keyword fallback
async function hybridSearch(userId, query, limit = 5) {
    try {
        const semanticResults = await semanticSearch(userId, query, limit);
        if (semanticResults.length > 0 && semanticResults[0].similarity > 0.3) {
            return semanticResults.map(r => ({
                ...r,
                relevance: r.similarity,
                method: 'semantic'
            }));
        }
    } catch (err) {
        console.log('Semantic search failed, falling back to keyword:', err.message);
    }

    // Keyword fallback
    const { searchChunks } = require('./documents');
    const keywordResults = await searchChunks(userId, query, limit);
    return keywordResults.map(r => ({
        ...r,
        relevance: 0.5,
        method: 'keyword'
    }));
}

// Three-tier cache management
async function updateCacheTier(chunkId, userId) {
    return new Promise((resolve, reject) => {
        db.get('SELECT * FROM cache_tier WHERE chunk_id = ? AND user_id = ?', [chunkId, userId], (err, existing) => {
            if (err) return reject(err);

            const now = new Date().toISOString();
            if (!existing) {
                db.run('INSERT INTO cache_tier (chunk_id, user_id, tier, access_count, last_access) VALUES (?, ?, ?, 1, ?)',
                    [chunkId, userId, 'warm', now], (err) => err ? reject(err) : resolve());
            } else {
                const newCount = existing.access_count + 1;
                let newTier = existing.tier;

                // Promote based on access count
                if (newCount >= 10 && existing.tier !== 'hot') newTier = 'hot';
                else if (newCount >= 3 && existing.tier === 'cold') newTier = 'warm';

                db.run('UPDATE cache_tier SET access_count = ?, tier = ?, last_access = ? WHERE chunk_id = ? AND user_id = ?',
                    [newCount, newTier, now, chunkId, userId], (err) => err ? reject(err) : resolve());
            }
        });
    });
}

// Demote cold chunks to make room
async function demoteColdChunks(userId, maxHot = 50, maxWarm = 200) {
    return new Promise((resolve, reject) => {
        // Count current tiers
        db.all('SELECT tier, COUNT(*) as count FROM cache_tier WHERE user_id = ? GROUP BY tier', [userId], (err, counts) => {
            if (err) return reject(err);

            const tierCounts = {};
            for (const c of counts) tierCounts[c.tier] = c.count;

            const actions = [];

            // If too many hot, demote oldest to warm
            if (tierCounts.hot > maxHot) {
                db.run('UPDATE cache_tier SET tier = ? WHERE user_id = ? AND tier = ? ORDER BY last_access ASC LIMIT ?',
                    ['warm', userId, 'hot', tierCounts.hot - maxHot], (err) => {
                        if (!err) actions.push(`Demoted ${tierCounts.hot - maxHot} hot → warm`);
                    });
            }

            // If too many warm, demote oldest to cold
            if (tierCounts.warm > maxWarm) {
                db.run('UPDATE cache_tier SET tier = ? WHERE user_id = ? AND tier = ? ORDER BY last_access ASC LIMIT ?',
                    ['cold', userId, 'warm', tierCounts.warm - maxWarm], (err) => {
                        if (!err) actions.push(`Demoted ${tierCounts.warm - maxWarm} warm → cold`);
                    });
            }

            resolve(actions);
        });
    });
}

// Get cache statistics
async function getCacheStats(userId) {
    return new Promise((resolve, reject) => {
        db.all('SELECT tier, COUNT(*) as count, SUM(access_count) as total_access FROM cache_tier WHERE user_id = ? GROUP BY tier',
            [userId], (err, rows) => {
                if (err) return reject(err);
                const stats = { hot: 0, warm: 0, cold: 0 };
                for (const row of rows) stats[row.tier] = { count: row.count, totalAccess: row.total_access };
                resolve(stats);
            });
    });
}

module.exports = {
    generateEmbedding,
    embedAllChunks,
    semanticSearch,
    hybridSearch,
    updateCacheTier,
    demoteColdChunks,
    getCacheStats,
    cosineSimilarity
};
