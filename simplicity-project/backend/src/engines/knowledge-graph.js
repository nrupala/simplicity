const sqlite3 = require('sqlite3').verbose();
const path = require('path');

// Initialize database
const dbPath = path.join(__dirname, '../../data/knowledge.db');
const db = new sqlite3.Database(dbPath);

// Create tables
db.serialize(() => {
    db.run(`
    CREATE TABLE IF NOT EXISTS knowledge_graph (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id TEXT NOT NULL,
      entity TEXT NOT NULL,
      relationship TEXT NOT NULL,
      target_entity TEXT NOT NULL,
      confidence REAL DEFAULT 1.0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

    db.run(`
    CREATE TABLE IF NOT EXISTS user_profiles (
      user_id TEXT PRIMARY KEY,
      profile_data TEXT,
      evolution_hash TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);
});

// Add knowledge to graph
async function addKnowledge(userId, entity, relationship, targetEntity, confidence = 1.0) {
    return new Promise((resolve, reject) => {
        const stmt = db.prepare(`
      INSERT INTO knowledge_graph (user_id, entity, relationship, target_entity, confidence)
      VALUES (?, ?, ?, ?, ?)
    `);

        stmt.run([userId, entity, relationship, targetEntity, confidence], function (err) {
            if (err) reject(err);
            else resolve(this.lastID);
        });
        stmt.finalize();
    });
}

// Query knowledge graph
async function queryKnowledge(userId, query) {
    return new Promise((resolve, reject) => {
        let sql = 'SELECT * FROM knowledge_graph WHERE user_id = ?';
        let params = [userId];

        if (query && query.trim()) {
            sql += ' AND (entity LIKE ? OR target_entity LIKE ? OR relationship LIKE ?)';
            const searchTerm = `%${query.trim()}%`;
            params.push(searchTerm, searchTerm, searchTerm);
        }

        sql += ' ORDER BY confidence DESC LIMIT 50';

        db.all(sql, params, (err, rows) => {
            if (err) reject(err);
            else resolve(rows || []);
        });
    });
}

// Get knowledge graph for visualization
async function getKnowledgeGraph(userId) {
    return new Promise((resolve, reject) => {
        const sql = `
      SELECT entity, relationship, target_entity, confidence
      FROM knowledge_graph
      WHERE user_id = ?
      ORDER BY confidence DESC
      LIMIT 100
    `;

        db.all(sql, [userId], (err, rows) => {
            if (err) reject(err);
            else {
                // Convert to node-link format for visualization
                const nodes = new Map();
                const links = [];

                rows.forEach(row => {
                    // Add source node
                    if (!nodes.has(row.entity)) {
                        nodes.set(row.entity, {
                            id: row.entity,
                            label: row.entity,
                            type: 'entity'
                        });
                    }

                    // Add target node
                    if (!nodes.has(row.target_entity)) {
                        nodes.set(row.target_entity, {
                            id: row.target_entity,
                            label: row.target_entity,
                            type: 'entity'
                        });
                    }

                    // Add link
                    links.push({
                        source: row.entity,
                        target: row.target_entity,
                        label: row.relationship,
                        value: row.confidence
                    });
                });

                resolve({
                    nodes: Array.from(nodes.values()),
                    links: links
                });
            }
        });
    });
}

// Clear all knowledge for a user
async function clearKnowledge(userId) {
    return new Promise((resolve, reject) => {
        db.run('DELETE FROM knowledge_graph WHERE user_id = ?', [userId], function (err) {
            if (err) reject(err);
            else resolve(this.changes);
        });
    });
}

module.exports = { addKnowledge, queryKnowledge, getKnowledgeGraph, clearKnowledge };