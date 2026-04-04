const express = require('express');
const { generateGPGKey, signData, verifySignature, encryptData, decryptData } = require('../engines/sovereignty');

const router = express.Router();

// Generate GPG key pair
router.post('/generate-key', async (req, res) => {
    try {
        const { userId, passphrase } = req.body;

        if (!userId || !passphrase) {
            return res.status(400).json({ error: 'userId and passphrase are required' });
        }

        const keyPair = await generateGPGKey(userId, passphrase);

        res.json({
            success: true,
            publicKey: keyPair.publicKey,
            keyId: `gpg-${userId}`,
            message: 'GPG key pair generated successfully'
        });

    } catch (error) {
        console.error('Key generation error:', error);
        res.status(500).json({
            error: 'Failed to generate GPG key',
            message: error.message
        });
    }
});

// Sign data
router.post('/sign', async (req, res) => {
    try {
        const { data, privateKey, passphrase } = req.body;

        if (!data || !privateKey || !passphrase) {
            return res.status(400).json({ error: 'data, privateKey, and passphrase are required' });
        }

        const signature = await signData(data, privateKey, passphrase);

        res.json({
            success: true,
            signature: signature,
            data: data
        });

    } catch (error) {
        console.error('Signing error:', error);
        res.status(500).json({
            error: 'Failed to sign data',
            message: error.message
        });
    }
});

// Verify signature
router.post('/verify', async (req, res) => {
    try {
        const { data, signature, publicKey } = req.body;

        if (!data || !signature || !publicKey) {
            return res.status(400).json({ error: 'data, signature, and publicKey are required' });
        }

        const isValid = await verifySignature(data, signature, publicKey);

        res.json({
            success: true,
            verified: isValid,
            data: data
        });

    } catch (error) {
        console.error('Verification error:', error);
        res.status(500).json({
            error: 'Failed to verify signature',
            message: error.message
        });
    }
});

// Encrypt data
router.post('/encrypt', async (req, res) => {
    try {
        const { data, publicKey } = req.body;

        if (!data || !publicKey) {
            return res.status(400).json({ error: 'data and publicKey are required' });
        }

        const encrypted = await encryptData(data, publicKey);

        res.json({
            success: true,
            encrypted: encrypted,
            data: data
        });

    } catch (error) {
        console.error('Encryption error:', error);
        res.status(500).json({
            error: 'Failed to encrypt data',
            message: error.message
        });
    }
});

// Decrypt data
router.post('/decrypt', async (req, res) => {
    try {
        const { encryptedData, privateKey, passphrase } = req.body;

        if (!encryptedData || !privateKey || !passphrase) {
            return res.status(400).json({ error: 'encryptedData, privateKey, and passphrase are required' });
        }

        const decrypted = await decryptData(encryptedData, privateKey, passphrase);

        res.json({
            success: true,
            decrypted: decrypted,
            encryptedData: encryptedData
        });

    } catch (error) {
        console.error('Decryption error:', error);
        res.status(500).json({
            error: 'Failed to decrypt data',
            message: error.message
        });
    }
});

module.exports = router;