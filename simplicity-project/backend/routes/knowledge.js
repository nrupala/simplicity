const express = require('express');
const router = express.Router();
const { addKnowledge, queryKnowledge, getKnowledgeGraph } = require('../src/engines/knowledge-graph');

// Add knowledge to user's graph
router.post('/add', async (req, res) => {
    try {
        const { userId, entity, relationship, targetEntity } = req.body;
        await addKnowledge(userId, entity, relationship, targetEntity);
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Query knowledge graph
router.get('/query/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { query } = req.query;
        const results = await queryKnowledge(userId, query);
        res.json({ results });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get full knowledge graph
router.get('/graph/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const graph = await getKnowledgeGraph(userId);
        res.json({ graph });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;