const express = require('express');
const { processQuery } = require('../engines/correlation');
const { generateResponse } = require('../engines/gan-rag');
const { personifyResponse } = require('../engines/personification');
const modelRegistry = require('../models/registry');
const { addKnowledge } = require('../engines/knowledge-graph');
const { searchChunks } = require('../engines/documents');
const { hybridSearch, embedAllChunks, getCacheStats, demoteColdChunks } = require('../engines/inference/embedding-engine');

const router = express.Router();

const conversations = new Map();
const MAX_CONTEXT_MESSAGES = 100;

function getConversation(userId) {
    if (!conversations.has(userId)) conversations.set(userId, []);
    return conversations.get(userId);
}

function trimContext(messages) {
    if (messages.length <= MAX_CONTEXT_MESSAGES) return messages;
    return messages.slice(-MAX_CONTEXT_MESSAGES);
}

// Get available providers
router.get('/providers', async (req, res) => {
    const providers = modelRegistry.getProviderInfo();
    const status = await modelRegistry.checkAllProviders();
    res.json({
        providers: providers.map(p => ({ ...p, available: status[p.id] || false })),
        default: modelRegistry.getDefaultProvider()
    });
});

// Set default provider
router.post('/provider', (req, res) => {
    const { provider } = req.body;
    if (!provider) return res.status(400).json({ error: 'Provider is required' });
    try {
        modelRegistry.setDefaultProvider(provider);
        res.json({ success: true, provider });
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
});

// Get models for a provider
router.get('/models/:provider', async (req, res) => {
    const { provider } = req.params;
    try {
        const adapter = modelRegistry.getAdapter(provider);
        if (!adapter) return res.status(404).json({ error: 'Provider not found' });
        const models = await adapter.listModels();
        res.json({ provider, models });
    } catch (error) {
        res.json({ provider, models: [] });
    }
});

// Update provider config
router.post('/config', async (req, res) => {
    const { provider, config } = req.body;
    if (!provider || !config) return res.status(400).json({ error: 'Provider and config required' });
    try {
        const adapter = modelRegistry.getAdapter(provider);
        if (adapter) Object.assign(adapter.config, config);
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Test provider connection
router.get('/test/:provider', async (req, res) => {
    const { provider } = req.params;
    try {
        const available = await modelRegistry.checkProvider(provider);
        res.json({ provider, available });
    } catch {
        res.json({ provider, available: false });
    }
});

// Get conversation history
router.get('/history/:userId', (req, res) => {
    const { userId } = req.params;
    const history = getConversation(userId);
    res.json({ userId, history, count: history.length });
});

// Clear conversation history
router.delete('/history/:userId', (req, res) => {
    const { userId } = req.params;
    conversations.set(userId, []);
    res.json({ success: true, message: 'Conversation cleared' });
});

// Build prompt from conversation history + knowledge context
function buildPrompt(query, conversation, knowledgeContext) {
    let prompt = '';
    if (knowledgeContext) {
        prompt += `Context from user's documents and knowledge:\n${knowledgeContext}\n\n`;
    }
    if (conversation.length > 0) {
        prompt += conversation.map(m => `${m.role}: ${m.content}`).join('\n\n') + '\n\n';
    }
    prompt += `user: ${query}\nassistant:`;
    return prompt;
}

// Streaming query endpoint (SSE)
router.post('/stream', async (req, res) => {
    try {
        const { query, userId = 'default', provider, model, history } = req.body;

        if (!query) {
            res.setHeader('Content-Type', 'text/event-stream');
            res.write(`data: ${JSON.stringify({ error: 'Query is required' })}\n\n`);
            return res.end();
        }

        res.setHeader('Content-Type', 'text/event-stream');
        res.setHeader('Cache-Control', 'no-cache');
        res.setHeader('Connection', 'keep-alive');
        res.setHeader('X-Accel-Buffering', 'no');

        const adapter = modelRegistry.getAdapter(provider || modelRegistry.getDefaultProvider());
        if (!adapter) {
            res.write(`data: ${JSON.stringify({ error: 'Provider not available' })}\n\n`);
            return res.end();
        }

        if (model) adapter.config.model = model;

        const conversation = getConversation(userId);
        if (history && Array.isArray(history)) {
            conversations.set(userId, trimContext(history));
        }

        conversation.push({ role: 'user', content: query });
        const contextMessages = trimContext([...conversation]);

        const startTime = Date.now();
        const correlation = await processQuery(query, userId);

        // Search documents for relevant context (hybrid: semantic + keyword)
        let documentContext = '';
        let docResults = [];
        try {
            docResults = await hybridSearch(userId, query, 5);
            if (docResults.length > 0) {
                documentContext = docResults.map(d =>
                    `[${d.filename}] (${d.method}, ${(d.relevance * 100).toFixed(0)}%): ${d.content}`
                ).join('\n\n');
            }
        } catch {}

        const knowledgeContext = correlation.userKnowledge.length > 0
            ? correlation.userKnowledge.map(k => `- ${k.entity} ${k.relationship} ${k.target_entity}`).join('\n')
            : '';

        const combinedContext = [documentContext, knowledgeContext].filter(Boolean).join('\n\n');
        const prompt = buildPrompt(query, contextMessages, combinedContext);

        try {
            // Native engine streaming
            if (adapter.name === 'native' && typeof adapter.generateStream === 'function') {
                let fullResponse = '';
                for await (const chunk of adapter.generateStream(prompt, {
                    temperature: adapter.config.temperature,
                    maxTokens: adapter.config.maxTokens,
                    topK: adapter.config.topK,
                    topP: adapter.config.topP,
                    minP: adapter.config.minP,
                    repeatPenalty: adapter.config.repeatPenalty
                })) {
                    fullResponse += chunk;
                    res.write(`data: ${JSON.stringify({ token: chunk, done: false })}\n\n`);
                }

                conversation.push({ role: 'assistant', content: fullResponse });
                trimContext(conversation);
                await extractKnowledge(userId, query, fullResponse);

                res.write(`data: ${JSON.stringify({
                    done: true,
                    model: adapter.config.modelId || 'native',
                    knowledgeUsed: correlation.userKnowledge.length,
                    documentsUsed: docResults?.length || 0,
                    processingTime: Date.now() - startTime,
                    conversationLength: conversation.length,
                    correlation: {
                        knowledgeCount: correlation.userKnowledge.length,
                        confidence: correlation.confidence,
                        queryType: correlation.queryType
                    }
                })}\n\n`);
            } else {
                // External provider streaming (Ollama, LM Studio, etc.)
                const stream = await adapter.client.generate({
                    model: adapter.config.model,
                    prompt: prompt,
                    stream: true,
                    options: {
                        temperature: adapter.config.temperature,
                        num_predict: adapter.config.maxTokens
                    }
                });

                let fullResponse = '';
                for await (const chunk of stream) {
                    const content = chunk.response || '';
                    if (content) {
                        fullResponse += content;
                        res.write(`data: ${JSON.stringify({ token: content, done: false })}\n\n`);
                    }
                }

                conversation.push({ role: 'assistant', content: fullResponse });
                trimContext(conversation);
                await extractKnowledge(userId, query, fullResponse);

                res.write(`data: ${JSON.stringify({
                    done: true,
                    model: adapter.config.model,
                    knowledgeUsed: correlation.userKnowledge.length,
                    documentsUsed: docResults?.length || 0,
                    processingTime: Date.now() - startTime,
                    conversationLength: conversation.length,
                    correlation: {
                        knowledgeCount: correlation.userKnowledge.length,
                        confidence: correlation.confidence,
                        queryType: correlation.queryType
                    }
                })}\n\n`);
            }
        } catch (error) {
            console.error('Stream generation error:', error);
            try {
                const result = await adapter.generate(prompt);
                const text = typeof result === 'string' ? result : (result.text || '');
                conversation.push({ role: 'assistant', content: text });
                trimContext(conversation);
                await extractKnowledge(userId, query, text);

                res.write(`data: ${JSON.stringify({ token: text, done: true })}\n\n`);
                res.write(`data: ${JSON.stringify({
                    done: true,
                    model: adapter.config.model,
                    knowledgeUsed: correlation.userKnowledge.length,
                    processingTime: Date.now() - startTime,
                    conversationLength: conversation.length
                })}\n\n`);
            } catch (fallbackError) {
                res.write(`data: ${JSON.stringify({ error: fallbackError.message, done: true })}\n\n`);
            }
        }

        res.end();
    } catch (error) {
        console.error('Stream error:', error);
        res.end();
    }
});

// Non-streaming query endpoint
router.post('/', async (req, res) => {
    try {
        const { query, userId = 'default', provider, model, history } = req.body;
        if (!query) return res.status(400).json({ error: 'Query is required' });

        const adapter = modelRegistry.getAdapter(provider || modelRegistry.getDefaultProvider());
        if (!adapter) return res.status(503).json({ error: 'Provider not available' });

        if (model) adapter.config.model = model;

        const conversation = getConversation(userId);
        if (history && Array.isArray(history)) {
            conversations.set(userId, trimContext(history));
        }

        conversation.push({ role: 'user', content: query });
        const contextMessages = trimContext([...conversation]);

        const startTime = Date.now();
        const correlation = await processQuery(query, userId);

        // Search documents for relevant context (hybrid: semantic + keyword)
        let documentContext = '';
        try {
            const docResults = await hybridSearch(userId, query, 5);
            if (docResults.length > 0) {
                documentContext = docResults.map(d =>
                    `[${d.filename}] (${d.method}, ${(d.relevance * 100).toFixed(0)}%): ${d.content}`
                ).join('\n\n');
            }
        } catch {}

        const knowledgeContext = correlation.userKnowledge.length > 0
            ? correlation.userKnowledge.map(k => `- ${k.entity} ${k.relationship} ${k.target_entity}`).join('\n')
            : '';

        const combinedContext = [documentContext, knowledgeContext].filter(Boolean).join('\n\n');
        const prompt = buildPrompt(query, contextMessages, combinedContext);

        let responseText = '';
        try {
            const result = await adapter.generate(prompt);
            responseText = typeof result === 'string' ? result : (result.text || '');
        } catch (error) {
            console.error('Generation error:', error);
            responseText = `Error: ${error.message}`;
        }

        if (typeof responseText !== 'string') responseText = JSON.stringify(responseText);

        conversation.push({ role: 'assistant', content: responseText });
        trimContext(conversation);
        await extractKnowledge(userId, query, responseText);

        const personifiedResponse = await personifyResponse(responseText, userId);
        const processingTime = Date.now() - startTime;

        res.json({
            query,
            text: typeof personifiedResponse === 'string' ? personifiedResponse : (personifiedResponse.text || personifiedResponse),
            model: adapter.config.model,
            profile: typeof personifiedResponse === 'object' ? (personifiedResponse.profile || { tone: 'professional' }) : { tone: 'professional' },
            knowledgeUsed: correlation.userKnowledge.length,
            processingTime,
            conversationLength: conversation.length,
            correlation: {
                knowledgeCount: correlation.userKnowledge.length,
                confidence: correlation.confidence,
                queryType: correlation.queryType
            },
            timestamp: new Date().toISOString()
        });
    } catch (error) {
        console.error('Query error:', error);
        res.status(500).json({ error: 'Failed to process query', message: error.message });
    }
});

// Extract knowledge from interactions
async function extractKnowledge(userId, query, response) {
    try {
        const words = query.toLowerCase().split(/\s+/).filter(w => w.length > 3);
        const stopWords = ['what', 'how', 'why', 'when', 'where', 'which', 'does', 'this', 'that', 'with', 'from', 'have', 'been', 'would', 'could', 'should', 'about', 'their', 'there', 'these', 'those', 'other', 'another', 'through', 'between', 'after', 'before', 'during', 'without', 'within', 'along', 'following', 'across'];
        const importantWords = words.filter(w => !stopWords.includes(w));
        if (importantWords.length >= 2) {
            const entity = importantWords.slice(0, 2).join(' ');
            const targetEntity = importantWords.slice(2, 4).join(' ') || 'concept';
            await addKnowledge(userId, entity, 'discussed_with', targetEntity, 0.8);
        }
    } catch {}
}

module.exports = router;
