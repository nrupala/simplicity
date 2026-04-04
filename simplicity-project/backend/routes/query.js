const express = require('express');
const router = express.Router();
const { processQuery } = require('../src/engines/correlation');
const { generateResponse } = require('../src/engines/gan-rag');
const { personifyResponse } = require('../src/engines/personification');

// Main query endpoint
router.post('/', async (req, res) => {
    try {
        const { query, userId, context } = req.body;

        if (!query) {
            return res.status(400).json({ error: 'Query is required' });
        }

        // 1. Correlate user knowledge with query
        const correlation = await processQuery(query, userId);

        // 2. Generate response using GAN-RAG
        const rawResponse = await generateResponse(query, correlation);

        // 3. Personify the response
        const personifiedResponse = await personifyResponse(rawResponse, userId);

        // 4. Create unique experience hash
        const experienceHash = require('crypto').createHash('sha256')
            .update(`${userId}-${query}-${Date.now()}`).digest('hex');

        res.json({
            response: personifiedResponse,
            experienceHash,
            timestamp: new Date().toISOString(),
            correlation: correlation.metadata
        });

    } catch (error) {
        console.error('Query processing error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

module.exports = router;