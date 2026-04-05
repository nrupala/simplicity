const express = require('express');
const memoryOrchestrator = require('../engines/inference/memory-orchestrator');

const router = express.Router();

// Get monitoring status
router.get('/status', (req, res) => {
    res.json(memoryOrchestrator.getStatus());
});

// Start monitoring
router.post('/start', (req, res) => {
    const { interval } = req.body;
    if (interval) memoryOrchestrator.setInterval(interval);
    memoryOrchestrator.start();
    res.json({ success: true, message: 'Monitoring started' });
});

// Stop monitoring
router.post('/stop', (req, res) => {
    memoryOrchestrator.stop();
    res.json({ success: true, message: 'Monitoring stopped' });
});

// Update thresholds
router.post('/thresholds', (req, res) => {
    const { optimal, comfortable, throttle, critical } = req.body;
    const thresholds = {};
    if (optimal !== undefined) thresholds.optimal = optimal;
    if (comfortable !== undefined) thresholds.comfortable = comfortable;
    if (throttle !== undefined) thresholds.throttle = throttle;
    if (critical !== undefined) thresholds.critical = critical;
    memoryOrchestrator.setThresholds(thresholds);
    res.json({ success: true, thresholds: memoryOrchestrator.thresholds });
});

// SSE stream for real-time updates
router.get('/stream', (req, res) => {
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');

    const listener = (data) => {
        res.write(`data: ${JSON.stringify(data)}\n\n`);
    };

    const unsubscribe = memoryOrchestrator.on(listener);

    // Send initial status
    res.write(`data: ${JSON.stringify(memoryOrchestrator.getStatus())}\n\n`);

    req.on('close', () => {
        unsubscribe();
        res.end();
    });
});

module.exports = router;
