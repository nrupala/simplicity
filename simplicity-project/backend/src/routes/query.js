const express = require('express');
const { processQuery } = require('../engines/correlation');
const { generateResponse } = require('../engines/gan-rag');
const { personifyResponse } = require('../engines/personification');

const router = express.Router();

// Main query endpoint
router.post('/', async (req, res) => {
    try {
        const { query, userId = 'default' } = req.body;

        if (!query) {
            return res.status(400).json({ error: 'Query is required' });
        }

        // Process query through correlation engine
        const correlation = await processQuery(query, userId);

        // Generate response using GAN-RAG
        const rawResponse = await generateResponse(query, correlation);

        // Apply personification
        const personifiedResponse = await personifyResponse(rawResponse, userId);

        res.json({
            query: query,
            response: personifiedResponse.text,
            correlation: {
                knowledgeCount: correlation.userKnowledge.length,
                confidence: correlation.confidence,
                queryType: correlation.queryType
            },
            profile: personifiedResponse.profile,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        console.error('Query processing error:', error);
        res.status(500).json({
            error: 'Failed to process query',
            message: error.message
        });
    }
});

// Get query history (placeholder)
router.get('/history/:userId', async (req, res) => {
    const { userId } = req.params;
    // In a real implementation, retrieve from database
    res.json({
        userId: userId,
        history: [],
        message: 'Query history not implemented yet'
    });
});

module.exports = router;