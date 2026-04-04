const { queryKnowledge } = require('../knowledge-graph');

// Process query and correlate with user knowledge
async function processQuery(query, userId) {
    // Get relevant knowledge from user's graph
    const knowledge = await queryKnowledge(userId, query);

    // Analyze query type and complexity
    const queryAnalysis = analyzeQuery(query);

    // Correlate user knowledge with query requirements
    const correlation = {
        userKnowledge: knowledge,
        queryType: queryAnalysis.type,
        complexity: queryAnalysis.complexity,
        relevantEntities: extractEntities(knowledge, query),
        confidence: calculateConfidence(knowledge, queryAnalysis),
        metadata: {
            knowledgeCount: knowledge.length,
            topEntities: knowledge.slice(0, 5).map(k => k.entity)
        }
    };

    return correlation;
}

// Analyze query characteristics
function analyzeQuery(query) {
    const lowerQuery = query.toLowerCase();

    let type = 'general';
    if (lowerQuery.includes('how') || lowerQuery.includes('what is')) type = 'factual';
    else if (lowerQuery.includes('why') || lowerQuery.includes('explain')) type = 'explanatory';
    else if (lowerQuery.includes('compare') || lowerQuery.includes('vs')) type = 'comparative';
    else if (lowerQuery.includes('code') || lowerQuery.includes('implement')) type = 'technical';

    const complexity = Math.min(query.split(' ').length / 10, 1); // Simple complexity metric

    return { type, complexity };
}

// Extract relevant entities from knowledge
function extractEntities(knowledge, query) {
    const queryWords = query.toLowerCase().split(/\s+/);
    return knowledge
        .filter(k => queryWords.some(word =>
            k.entity.toLowerCase().includes(word) ||
            k.target_entity.toLowerCase().includes(word)
        ))
        .map(k => ({ entity: k.entity, relevance: 0.8 })); // Simplified relevance
}

// Calculate confidence based on knowledge and query
function calculateConfidence(knowledge, queryAnalysis) {
    const baseConfidence = knowledge.length > 0 ? 0.7 : 0.3;
    const complexityBonus = queryAnalysis.complexity * 0.2;
    return Math.min(baseConfidence + complexityBonus, 1.0);
}

module.exports = {
    processQuery
};