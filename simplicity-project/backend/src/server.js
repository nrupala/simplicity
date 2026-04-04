const express = require('express');
const cors = require('cors');
const queryRoutes = require('./routes/query');
const sovereigntyRoutes = require('./routes/sovereignty');
const knowledgeRoutes = require('./routes/knowledge');
const portabilityRoutes = require('./routes/portability');

const app = express();
const PORT = process.env.PORT || 3001;

app.use(cors());
app.use(express.json());

// Routes
app.use('/api/query', queryRoutes);
app.use('/api/sovereignty', sovereigntyRoutes);
app.use('/api/knowledge', knowledgeRoutes);
app.use('/api/portability', portabilityRoutes);

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'SIMPLICITY Backend Running', timestamp: new Date().toISOString() });
});

app.listen(PORT, () => {
    console.log(`SIMPLICITY Backend running on port ${PORT}`);
});

module.exports = app;