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
        // Simple keyword search for now
        const sql = `
      SELECT * FROM knowledge_graph
      WHERE user_id = ? AND (entity LIKE ? OR target_entity LIKE ?)
      ORDER BY confidence DESC, created_at DESC
      LIMIT 50
    `;

        db.all(sql, [userId, `%${query}%`, `%${query}%`], (err, rows) => {
            if (err) reject(err);
            else resolve(rows);
        });
    });
}

// Get full knowledge graph for user
async function getKnowledgeGraph(userId) {
    return new Promise((resolve, reject) => {
        const sql = `
      SELECT entity, relationship, target_entity, confidence
      FROM knowledge_graph
      WHERE user_id = ?
      ORDER BY created_at DESC
    `;

        db.all(sql, [userId], (err, rows) => {
            if (err) reject(err);
            else {
                // Convert to graph format
                const nodes = new Set();
                const links = [];

                rows.forEach(row => {
                    nodes.add(row.entity);
                    nodes.add(row.target_entity);
                    links.push({
                        source: row.entity,
                        target: row.target_entity,
                        relationship: row.relationship,
                        confidence: row.confidence
                    });
                });

                resolve({
                    nodes: Array.from(nodes).map(node => ({ id: node, label: node })),
                    links
                });
            }
        });
    });
}

// Update user evolution
async function updateUserEvolution(userId, newData) {
    return new Promise((resolve, reject) => {
        const evolutionHash = require('crypto').createHash('sha256')
            .update(JSON.stringify(newData)).digest('hex');

        const stmt = db.prepare(`
      INSERT OR REPLACE INTO user_profiles (user_id, profile_data, evolution_hash, updated_at)
      VALUES (?, ?, ?, CURRENT_TIMESTAMP)
    `);

        stmt.run([userId, JSON.stringify(newData), evolutionHash], function (err) {
            if (err) reject(err);
            else resolve(this.lastID);
        });
        stmt.finalize();
    });
}

module.exports = {
    addKnowledge,
    queryKnowledge,
    getKnowledgeGraph,
    updateUserEvolution
};