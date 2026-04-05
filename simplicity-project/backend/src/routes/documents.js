const express = require('express');
const multer = require('multer');
const path = require('path');
const { addDocument, searchChunks, getDocuments, deleteDocument, getChunkCount } = require('../engines/documents');

const router = express.Router();

const storage = multer.memoryStorage();
const upload = multer({
    storage,
    limits: { fileSize: 50 * 1024 * 1024 },
    fileFilter: (req, file, cb) => {
        const allowed = ['.txt', '.md', '.json', '.csv', '.html', '.htm', '.js', '.ts', '.py', '.java', '.xml', '.yaml', '.yml', '.log', '.css', '.sql', '.sh', '.bat', '.ps1', '.conf', '.cfg', '.ini', '.toml'];
        const ext = path.extname(file.originalname).toLowerCase();
        if (allowed.includes(ext) || file.mimetype.startsWith('text/')) {
            cb(null, true);
        } else {
            cb(new Error(`File type ${ext} not supported. Supported: ${allowed.join(', ')}`));
        }
    }
});

// Upload documents
router.post('/upload', upload.array('files', 20), async (req, res) => {
    try {
        const userId = req.body.userId || 'default-user';
        const results = [];

        for (const file of req.files) {
            const content = file.buffer.toString('utf8');
            const fileType = path.extname(file.originalname).replace('.', '') || 'txt';
            const result = await addDocument(userId, file.originalname, content, fileType);
            results.push(result);
        }

        const totalChunks = await getChunkCount(userId);
        res.json({
            success: true,
            uploaded: results.filter(r => !r.duplicate).length,
            duplicates: results.filter(r => r.duplicate).length,
            results,
            totalChunks
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// List documents
router.get('/list/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const docs = await getDocuments(userId);
        const totalChunks = await getChunkCount(userId);
        res.json({ documents: docs, totalChunks });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Search document chunks
router.get('/search/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { q, limit = 10 } = req.query;
        if (!q) return res.status(400).json({ error: 'Query parameter "q" is required' });
        const results = await searchChunks(userId, q, parseInt(limit));
        res.json({ query: q, results, count: results.length });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Delete document
router.delete('/:userId/:docId', async (req, res) => {
    try {
        const { userId, docId } = req.params;
        const result = await deleteDocument(userId, parseInt(docId));
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;
