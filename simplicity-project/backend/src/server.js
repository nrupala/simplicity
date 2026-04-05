const express = require('express');
const cors = require('cors');
const path = require('path');
const queryRoutes = require('./routes/query');
const sovereigntyRoutes = require('./routes/sovereignty');
const knowledgeRoutes = require('./routes/knowledge');
const portabilityRoutes = require('./routes/portability');
const documentsRoutes = require('./routes/documents');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Serve static frontend
app.use(express.static(path.join(__dirname, '../public')));

// API Routes
app.use('/api/query', queryRoutes);
app.use('/api/sovereignty', sovereigntyRoutes);
app.use('/api/knowledge', knowledgeRoutes);
app.use('/api/portability', portabilityRoutes);
app.use('/api/documents', documentsRoutes);

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'SIMPLICITY Backend Running', timestamp: new Date().toISOString() });
});

// SPA fallback - serve index.html for all non-API routes
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
});

app.listen(PORT, () => {
    console.log(`SIMPLICITY running on http://localhost:${PORT}`);
});

module.exports = app;