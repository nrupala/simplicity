const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');

const dbPath = path.join(__dirname, '../../data/knowledge.db');
const db = new sqlite3.Database(dbPath);

db.serialize(() => {
    db.run(`
        CREATE TABLE IF NOT EXISTS documents (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT NOT NULL,
            filename TEXT NOT NULL,
            file_type TEXT NOT NULL,
            file_hash TEXT NOT NULL UNIQUE,
            file_size INTEGER,
            content TEXT,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP
        )
    `);

    db.run(`
        CREATE TABLE IF NOT EXISTS document_chunks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            document_id INTEGER NOT NULL,
            user_id TEXT NOT NULL,
            chunk_index INTEGER NOT NULL,
            content TEXT NOT NULL,
            embedding_hint TEXT,
            FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
        )
    `);

    db.run('CREATE INDEX IF NOT EXISTS idx_chunks_user ON document_chunks(user_id)');
    db.run('CREATE INDEX IF NOT EXISTS idx_chunks_doc ON document_chunks(document_id)');
});

function hashContent(content) {
    return crypto.createHash('sha256').update(content).digest('hex');
}

function chunkText(text, chunkSize = 500, overlap = 100) {
    const chunks = [];
    let start = 0;
    while (start < text.length) {
        const end = Math.min(start + chunkSize, text.length);
        let chunk = text.slice(start, end);

        // Try to break at sentence/word boundary
        if (end < text.length) {
            const lastSentence = chunk.lastIndexOf('. ');
            const lastNewline = chunk.lastIndexOf('\n');
            const lastSpace = chunk.lastIndexOf(' ');
            const breakPoint = Math.max(lastSentence, lastNewline, lastSpace);
            if (breakPoint > chunkSize * 0.5) {
                chunk = chunk.slice(0, breakPoint + 1);
                start = start + breakPoint + 1 - overlap;
            } else {
                start = end - overlap;
            }
        } else {
            start = end;
        }

        if (chunk.trim().length > 10) {
            chunks.push(chunk.trim());
        }
    }
    return chunks;
}

function extractTextFromContent(content, fileType) {
    if (fileType === 'json') {
        try {
            const parsed = JSON.parse(content);
            return JSON.stringify(parsed, null, 2);
        } catch {
            return content;
        }
    }
    if (fileType === 'html' || fileType === 'htm') {
        // Strip HTML tags
        return content.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
    }
    if (fileType === 'csv') {
        return content;
    }
    if (fileType === 'md' || fileType === 'markdown') {
        // Strip markdown formatting but keep text
        return content
            .replace(/#{1,6}\s?/g, '')
            .replace(/\*\*([^*]+)\*\*/g, '$1')
            .replace(/\*([^*]+)\*/g, '$1')
            .replace(/`([^`]+)`/g, '$1')
            .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
            .replace(/!\[([^\]]*)\]\([^)]+\)/g, '$1')
            .replace(/^[-*+]\s/gm, '')
            .replace(/^>\s/gm, '');
    }
    return content;
}

async function addDocument(userId, filename, content, fileType = 'txt') {
    return new Promise((resolve, reject) => {
        const fileHash = hashContent(content);
        const fileSize = Buffer.byteLength(content, 'utf8');

        // Check for duplicate
        db.get('SELECT id FROM documents WHERE file_hash = ?', [fileHash], (err, existing) => {
            if (err) return reject(err);
            if (existing) return resolve({ id: existing.id, duplicate: true, filename });

            db.run(
                'INSERT INTO documents (user_id, filename, file_type, file_hash, file_size, content) VALUES (?, ?, ?, ?, ?, ?)',
                [userId, filename, fileType, fileHash, fileSize, content],
                function (err) {
                    if (err) return reject(err);
                    const docId = this.lastID;

                    // Extract and chunk text
                    const text = extractTextFromContent(content, fileType);
                    const chunks = chunkText(text);

                    let completed = 0;
                    if (chunks.length === 0) {
                        return resolve({ id: docId, duplicate: false, filename, chunks: 0 });
                    }

                    chunks.forEach((chunk, index) => {
                        db.run(
                            'INSERT INTO document_chunks (document_id, user_id, chunk_index, content) VALUES (?, ?, ?, ?)',
                            [docId, userId, index, chunk],
                            (err) => {
                                completed++;
                                if (completed === chunks.length) {
                                    resolve({ id: docId, duplicate: false, filename, chunks: chunks.length });
                                }
                            }
                        );
                    });
                }
            );
        });
    });
}

async function searchChunks(userId, query, limit = 10) {
    return new Promise((resolve, reject) => {
        // Simple keyword-based retrieval (would use embeddings in production)
        const keywords = query.toLowerCase().split(/\s+/)
            .filter(w => w.length > 2)
            .map(w => w.replace(/[^\w]/g, ''))
            .filter(w => w.length > 0);

        if (keywords.length === 0) return resolve([]);

        const conditions = keywords.map(() => 'content LIKE ?').join(' OR ');
        const params = keywords.map(k => `%${k}%`);

        db.all(
            `SELECT dc.content, dc.chunk_index, d.filename, d.file_type,
                    (LENGTH(dc.content) - LENGTH(REPLACE(LOWER(dc.content), LOWER(?), ''))) / LENGTH(?) as relevance
             FROM document_chunks dc
             JOIN documents d ON dc.document_id = d.id
             WHERE dc.user_id = ? AND (${conditions})
             ORDER BY relevance DESC
             LIMIT ?`,
            [keywords[0], keywords[0], userId, ...params.slice(1), limit],
            (err, rows) => {
                if (err) return reject(err);
                resolve(rows || []);
            }
        );
    });
}

async function getDocuments(userId) {
    return new Promise((resolve, reject) => {
        db.all(
            'SELECT id, filename, file_type, file_size, file_hash, created_at FROM documents WHERE user_id = ? ORDER BY created_at DESC',
            [userId],
            (err, rows) => {
                if (err) return reject(err);
                resolve(rows || []);
            }
        );
    });
}

async function deleteDocument(userId, docId) {
    return new Promise((resolve, reject) => {
        db.run(
            'DELETE FROM documents WHERE id = ? AND user_id = ?',
            [docId, userId],
            function (err) {
                if (err) return reject(err);
                resolve({ deleted: this.changes });
            }
        );
    });
}

async function getChunkCount(userId) {
    return new Promise((resolve, reject) => {
        db.get(
            'SELECT COUNT(*) as count FROM document_chunks WHERE user_id = ?',
            [userId],
            (err, row) => {
                if (err) return reject(err);
                resolve(row?.count || 0);
            }
        );
    });
}

module.exports = {
    addDocument,
    searchChunks,
    getDocuments,
    deleteDocument,
    getChunkCount
};
