const express = require('express');
const router = express.Router();
const { generateGPGKey, signData, verifySignature } = require('../src/engines/sovereignty');

// Generate GPG key pair
router.post('/generate-key', async (req, res) => {
    try {
        const { userId, passphrase } = req.body;
        const keyPair = await generateGPGKey(userId, passphrase);
        res.json({ keyPair });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Sign data
router.post('/sign', async (req, res) => {
    try {
        const { data, privateKey, passphrase } = req.body;
        const signature = await signData(data, privateKey, passphrase);
        res.json({ signature });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Verify signature
router.post('/verify', async (req, res) => {
    try {
        const { data, signature, publicKey } = req.body;
        const isValid = await verifySignature(data, signature, publicKey);
        res.json({ isValid });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;