const modelRegistry = require('../models/registry');
const axios = require('axios');

async function generateResponse(query, correlation, provider = null, model = null, documentContext = '') {
    try {
        const webKnowledge = await retrieveWebKnowledge(query);
        const context = buildContext(correlation.userKnowledge, webKnowledge, documentContext);

        // Generate 5 diverse candidate prompts
        const candidatePrompts = [
            `You are an expert analyst. Based on this context, provide a comprehensive answer to: ${query}\n\nContext: ${context}`,
            `You are a technical expert. Explain this topic clearly and accurately using the provided information:\nQuestion: ${query}\n\nReference material: ${context}`,
            `You are a teacher. Break down this topic step by step for someone trying to understand it:\nQuestion: ${query}\n\nBackground: ${context}`,
            `You are a researcher. Provide a detailed, evidence-based answer citing the available information:\nQuestion: ${query}\n\nEvidence: ${context}`,
            `You are a consultant. Give a practical, actionable answer with specific details:\nQuestion: ${query}\n\nContext: ${context}`
        ];

        // Generate all 5 candidates
        const candidates = [];
        for (let i = 0; i < candidatePrompts.length; i++) {
            try {
                const result = await modelRegistry.generate(provider, candidatePrompts[i], {
                    temperature: 0.5 + (i * 0.15),
                    maxTokens: 500,
                    model: model
                });
                candidates.push({
                    text: result.text,
                    prompt: candidatePrompts[i],
                    temperature: 0.5 + (i * 0.15),
                    index: i
                });
            } catch (error) {
                console.error(`Candidate ${i + 1} generation error:`, error.message);
            }
        }

        if (candidates.length === 0) {
            return await basicGeneration(query, provider, model);
        }

        // Discriminate: score each candidate
        const scored = await discriminateCandidates(candidates, query, correlation);
        scored.model = candidates[0]?.model || model;
        return scored;
    } catch (error) {
        console.error('GAN-RAG generation error:', error);
        return generateFallbackResponse(query, correlation);
    }
}

async function discriminateCandidates(candidates, query, correlation) {
    if (candidates.length === 0) {
        return await basicGeneration(query);
    }

    // If we have multiple candidates, use the best one as discriminator
    if (candidates.length >= 2) {
        try {
            // Build a scoring prompt for the discriminator
            const candidateTexts = candidates.map((c, i) =>
                `Candidate ${i + 1} (temp=${c.temperature}):\n${c.text.substring(0, 300)}...`
            ).join('\n\n---\n\n');

            const scoringPrompt = `You are an expert evaluator. Score these candidate responses to the question "${query}" on:
1. Accuracy - Does it correctly answer the question?
2. Completeness - Does it cover all aspects?
3. Clarity - Is it well-structured and easy to understand?
4. Specificity - Does it provide concrete details, not vague statements?

Score each 1-10 and pick the best.

${candidateTexts}

Respond with ONLY: "Best: N" where N is the candidate number.`;

            const scoringResult = await modelRegistry.generateDefault(scoringPrompt, {
                temperature: 0.1,
                maxTokens: 50
            });

            const match = scoringResult.text.match(/Best:\s*(\d+)/i);
            if (match) {
                const bestIndex = parseInt(match[1]) - 1;
                if (bestIndex >= 0 && bestIndex < candidates.length) {
                    return {
                        text: candidates[bestIndex].text,
                        discriminatorScore: 10,
                        method: 'ai-discriminator',
                        temperature: candidates[bestIndex].temperature
                    };
                }
            }
        } catch (err) {
            console.log('AI discrimination failed, falling back to heuristic:', err.message);
        }
    }

    // Fallback: heuristic scoring
    const scored = candidates.map(candidate => {
        let score = 0;

        // Length score (prefer substantive but not rambling answers)
        const len = candidate.text.length;
        if (len > 100 && len < 3000) score += Math.min(10, len / 200);
        else if (len <= 100) score += 2;
        else score += 5;

        // Specificity score (look for numbers, examples, technical terms)
        const hasNumbers = (candidate.text.match(/\d+/g) || []).length;
        score += Math.min(5, hasNumbers / 3);

        // Structure score (look for paragraphs, lists)
        const hasStructure = candidate.text.includes('\n') || candidate.text.includes('- ') || candidate.text.includes('.');
        if (hasStructure) score += 3;

        // Avoid repetition penalty
        const words = candidate.text.toLowerCase().split(/\s+/);
        const uniqueWords = new Set(words);
        const uniqueness = uniqueWords.size / words.length;
        score += uniqueness * 5;

        // Correlation confidence boost
        score *= correlation.confidence;

        return {
            ...candidate,
            heuristicScore: score
        };
    });

    scored.sort((a, b) => (b.heuristicScore || 0) - (a.heuristicScore || 0));

    return {
        text: scored[0].text,
        discriminatorScore: scored[0].heuristicScore || 0,
        method: scored.length > 1 ? 'heuristic' : 'single',
        temperature: scored[0].temperature,
        candidatesGenerated: candidates.length
    };
}

async function retrieveWebKnowledge(query) {
    const results = [];

    // DuckDuckGo instant answer
    try {
        const ddgRes = await axios.get('https://api.duckduckgo.com/', {
            params: { q: query, format: 'json', no_html: 1 },
            timeout: 5000
        });
        if (ddgRes.data.Abstract) {
            results.push({ source: 'DuckDuckGo', content: ddgRes.data.Abstract, relevance: 0.95 });
        }
        if (ddgRes.data.RelatedTopics) {
            ddgRes.data.RelatedTopics.slice(0, 3).forEach(topic => {
                if (topic.Text && topic.Text.length > 20) {
                    results.push({ source: 'DuckDuckGo', content: topic.Text, relevance: 0.8 });
                }
            });
        }
    } catch {}

    // Wikipedia summary
    try {
        const wikiRes = await axios.get('https://en.wikipedia.org/api/rest_v1/page/summary/' + encodeURIComponent(query), { timeout: 5000 });
        if (wikiRes.data.extract) {
            results.push({ source: 'Wikipedia', content: wikiRes.data.extract, relevance: 0.9 });
        }
    } catch {}

    return results;
}

function buildContext(userKnowledge, webKnowledge, documentContext) {
    const parts = [];

    if (documentContext) {
        parts.push('Your documents:');
        parts.push(documentContext);
    }

    if (userKnowledge && userKnowledge.length > 0) {
        parts.push('Your knowledge graph:');
        parts.push(userKnowledge.map(k => `- ${k.entity} ${k.relationship} ${k.target_entity}`).join('\n'));
    }

    if (webKnowledge && webKnowledge.length > 0) {
        parts.push('Real-world information:');
        webKnowledge.forEach(w => {
            parts.push(`[${w.source}]: ${w.content}`);
        });
    }

    return parts.join('\n\n') || 'No additional context available.';
}

async function basicGeneration(query, provider = null, model = null) {
    try {
        const result = await modelRegistry.generate(provider || null, query, {
            temperature: 0.7,
            maxTokens: 300,
            model: model
        });
        return { text: result.text, model: result.model, method: 'direct' };
    } catch (error) {
        console.error('Basic generation error:', error.message);
        return { text: `I don't have enough information to answer "${query}" accurately.`, model: 'fallback', method: 'fallback' };
    }
}

function generateFallbackResponse(query, correlation) {
    return {
        text: `Thank you for your question: "${query}". SIMPLICITY is running with ${correlation.userKnowledge?.length || 0} knowledge items and your documents.`,
        model: 'fallback',
        method: 'fallback'
    };
}

module.exports = { generateResponse, retrieveWebKnowledge, buildContext };
