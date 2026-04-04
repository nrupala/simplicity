const express = require('express');
const { addKnowledge, queryKnowledge, getKnowledgeGraph, clearKnowledge } = require('../engines/knowledge-graph');

const router = express.Router();

// Add knowledge to graph
router.post('/', async (req, res) => {
    try {
        const { userId = 'default', entity, relationship, targetEntity, confidence = 1.0 } = req.body;

        if (!entity || !relationship || !targetEntity) {
            return res.status(400).json({ error: 'entity, relationship, and targetEntity are required' });
        }

        const id = await addKnowledge(userId, entity, relationship, targetEntity, confidence);

        res.json({
            success: true,
            id: id,
            message: 'Knowledge added successfully'
        });

    } catch (error) {
        console.error('Add knowledge error:', error);
        res.status(500).json({
            error: 'Failed to add knowledge',
            message: error.message
        });
    }
});

// Query knowledge
router.get('/', async (req, res) => {
    try {
        const { userId = 'default', q: query = '' } = req.query;

        const knowledge = await queryKnowledge(userId, query);

        res.json({
            userId: userId,
            query: query,
            knowledge: knowledge,
            count: knowledge.length
        });

    } catch (error) {
        console.error('Query knowledge error:', error);
        res.status(500).json({
            error: 'Failed to query knowledge',
            message: error.message
        });
    }
});

// Get knowledge graph for visualization
router.get('/graph/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        const graph = await getKnowledgeGraph(userId);

        res.json({
            userId: userId,
            graph: graph
        });

    } catch (error) {
        console.error('Get knowledge graph error:', error);
        res.status(500).json({
            error: 'Failed to get knowledge graph',
            message: error.message
        });
    }
});

// Clear all knowledge for user
router.delete('/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        const deletedCount = await clearKnowledge(userId);

        res.json({
            success: true,
            userId: userId,
            deletedCount: deletedCount,
            message: 'Knowledge cleared successfully'
        });

    } catch (error) {
        console.error('Clear knowledge error:', error);
        res.status(500).json({
            error: 'Failed to clear knowledge',
            message: error.message
        });
    }
});

module.exports = router;