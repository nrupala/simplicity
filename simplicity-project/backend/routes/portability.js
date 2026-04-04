const express = require('express');
const router = express.Router();
const PortabilityManager = require('../src/portability/manager');

const portabilityManager = new PortabilityManager('./data');

// Export user data
router.get('/export/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const data = await portabilityManager.exportUserData(userId);
        res.json(data);
    } catch (error) {
        console.error('Export error:', error);
        res.status(500).json({ error: 'Failed to export data' });
    }
});

// Import user data
router.post('/import/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const importData = req.body;
        const result = await portabilityManager.importUserData(userId, importData);
        res.json(result);
    } catch (error) {
        console.error('Import error:', error);
        res.status(500).json({ error: 'Failed to import data' });
    }
});

// Create backup
router.post('/backup/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { backupDir = './backups' } = req.body;
        const result = await portabilityManager.createBackup(userId, backupDir);
        res.json(result);
    } catch (error) {
        console.error('Backup error:', error);
        res.status(500).json({ error: 'Failed to create backup' });
    }
});

// List backups
router.get('/backups', async (req, res) => {
    try {
        const { backupDir = './backups' } = req.query;
        const backups = await portabilityManager.listBackups(backupDir);
        res.json({ backups });
    } catch (error) {
        console.error('List backups error:', error);
        res.status(500).json({ error: 'Failed to list backups' });
    }
});

// Validate backup file
router.post('/validate-backup', async (req, res) => {
    try {
        const { filePath } = req.body;
        const validation = await portabilityManager.validateBackup(filePath);
        res.json(validation);
    } catch (error) {
        console.error('Validate backup error:', error);
        res.status(500).json({ error: 'Failed to validate backup' });
    }
});

module.exports = router;