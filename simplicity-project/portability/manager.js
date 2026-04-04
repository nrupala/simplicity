const fs = require('fs').promises;
const path = require('path');
const { queryKnowledge, addKnowledge, clearKnowledge } = require('../engines/knowledge-graph');
const { getUserProfile, setUserProfile } = require('../engines/sovereignty');

// Portability functions for SIMPLICITY
class PortabilityManager {
    constructor(dataDir = './data') {
        this.dataDir = dataDir;
    }

    // Export user data
    async exportUserData(userId) {
        try {
            // Get knowledge graph
            const knowledge = await queryKnowledge(userId, ''); // Get all knowledge

            // Get user profile
            const profile = await getUserProfile(userId);

            // Get sovereignty keys (encrypted)
            const keys = await this.getUserKeys(userId);

            const exportData = {
                version: '1.0',
                exportedAt: new Date().toISOString(),
                userId: userId,
                knowledge: knowledge,
                profile: profile,
                keys: keys,
                metadata: {
                    knowledgeCount: knowledge.length,
                    exportType: 'full'
                }
            };

            return exportData;
        } catch (error) {
            console.error('Export failed:', error);
            throw new Error(`Failed to export data: ${error.message}`);
        }
    }

    // Import user data
    async importUserData(userId, importData) {
        try {
            // Validate import data
            if (!importData.version || !importData.knowledge) {
                throw new Error('Invalid import data format');
            }

            // Clear existing data if requested
            if (importData.metadata?.clearExisting !== false) {
                await clearKnowledge(userId);
            }

            // Import knowledge
            for (const item of importData.knowledge) {
                await addKnowledge(userId, item.entity, item.relationship, item.target_entity, item.metadata);
            }

            // Import profile
            if (importData.profile) {
                await setUserProfile(userId, importData.profile);
            }

            // Import keys
            if (importData.keys) {
                await this.setUserKeys(userId, importData.keys);
            }

            return {
                success: true,
                imported: {
                    knowledge: importData.knowledge.length,
                    profile: !!importData.profile,
                    keys: !!importData.keys
                }
            };
        } catch (error) {
            console.error('Import failed:', error);
            throw new Error(`Failed to import data: ${error.message}`);
        }
    }

    // Export to file
    async exportToFile(userId, filePath) {
        const data = await this.exportUserData(userId);
        await fs.writeFile(filePath, JSON.stringify(data, null, 2));
        return { success: true, filePath };
    }

    // Import from file
    async importFromFile(userId, filePath) {
        const data = await fs.readFile(filePath, 'utf8');
        const importData = JSON.parse(data);
        return await this.importUserData(userId, importData);
    }

    // Get user keys (placeholder - in real implementation, use encrypted storage)
    async getUserKeys(userId) {
        // In a real implementation, retrieve from secure storage
        return {
            gpgKeyId: `gpg-${userId}`,
            encrypted: true
        };
    }

    // Set user keys
    async setUserKeys(userId, keys) {
        // In a real implementation, store in secure storage
        console.log(`Setting keys for user ${userId}`);
        return { success: true };
    }

    // Backup to directory
    async createBackup(userId, backupDir) {
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
        const backupPath = path.join(backupDir, `simplicity-backup-${userId}-${timestamp}.json`);
        return await this.exportToFile(userId, backupPath);
    }

    // List backups
    async listBackups(backupDir) {
        try {
            const files = await fs.readdir(backupDir);
            return files
                .filter(file => file.startsWith('simplicity-backup-') && file.endsWith('.json'))
                .map(file => ({
                    file: file,
                    path: path.join(backupDir, file),
                    timestamp: this.extractTimestamp(file)
                }))
                .sort((a, b) => b.timestamp - a.timestamp);
        } catch (error) {
            return [];
        }
    }

    // Extract timestamp from backup filename
    extractTimestamp(filename) {
        const match = filename.match(/simplicity-backup-.*-(\d{4}-\d{2}-\d{2}T\d{2}-\d{2}-\d{2}-\d{3}Z)\.json/);
        return match ? new Date(match[1]).getTime() : 0;
    }

    // Validate backup file
    async validateBackup(filePath) {
        try {
            const data = await fs.readFile(filePath, 'utf8');
            const parsed = JSON.parse(data);

            const required = ['version', 'userId', 'knowledge'];
            const valid = required.every(key => parsed.hasOwnProperty(key));

            return {
                valid: valid,
                version: parsed.version,
                userId: parsed.userId,
                knowledgeCount: parsed.knowledge?.length || 0
            };
        } catch (error) {
            return { valid: false, error: error.message };
        }
    }
}

module.exports = PortabilityManager;