const express = require('express');
const PortabilityManager = require('../portability/manager');

const router = express.Router();
const portabilityManager = new PortabilityManager();

// Export user data
router.get('/export/:userId', async (req, res) => {
    try {
        const { userId } = req.params;

        const exportData = await portabilityManager.exportUserData(userId);

        res.json({
            success: true,
            data: exportData
        });

    } catch (error) {
        console.error('Export error:', error);
        res.status(500).json({
            error: 'Failed to export data',
            message: error.message
        });
    }
});

// Import user data
router.post('/import/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { importData } = req.body;

        if (!importData) {
            return res.status(400).json({ error: 'importData is required' });
        }

        const result = await portabilityManager.importUserData(userId, importData);

        res.json({
            success: true,
            result: result
        });

    } catch (error) {
        console.error('Import error:', error);
        res.status(500).json({
            error: 'Failed to import data',
            message: error.message
        });
    }
});

// Export to file
router.post('/export-file/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { filePath } = req.body;

        if (!filePath) {
            return res.status(400).json({ error: 'filePath is required' });
        }

        const result = await portabilityManager.exportToFile(userId, filePath);

        res.json({
            success: true,
            result: result
        });

    } catch (error) {
        console.error('Export to file error:', error);
        res.status(500).json({
            error: 'Failed to export to file',
            message: error.message
        });
    }
});

// Import from file
router.post('/import-file/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { filePath } = req.body;

        if (!filePath) {
            return res.status(400).json({ error: 'filePath is required' });
        }

        const result = await portabilityManager.importFromFile(userId, filePath);

        res.json({
            success: true,
            result: result
        });

    } catch (error) {
        console.error('Import from file error:', error);
        res.status(500).json({
            error: 'Failed to import from file',
            message: error.message
        });
    }
});

// Create backup
router.post('/backup/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { backupDir = './backups' } = req.body;

        const result = await portabilityManager.createBackup(userId, backupDir);

        res.json({
            success: true,
            result: result
        });

    } catch (error) {
        console.error('Backup error:', error);
        res.status(500).json({
            error: 'Failed to create backup',
            message: error.message
        });
    }
});

// List backups
router.get('/backups', async (req, res) => {
    try {
        const { backupDir = './backups' } = req.query;

        const backups = await portabilityManager.listBackups(backupDir);

        res.json({
            success: true,
            backups: backups
        });

    } catch (error) {
        console.error('List backups error:', error);
        res.status(500).json({
            error: 'Failed to list backups',
            message: error.message
        });
    }
});

// Validate backup file
router.post('/validate-backup', async (req, res) => {
    try {
        const { filePath } = req.body;

        if (!filePath) {
            return res.status(400).json({ error: 'filePath is required' });
        }

        const validation = await portabilityManager.validateBackup(filePath);

        res.json({
            success: true,
            validation: validation
        });

    } catch (error) {
        console.error('Validate backup error:', error);
        res.status(500).json({
            error: 'Failed to validate backup',
            message: error.message
        });
    }
});

module.exports = router;