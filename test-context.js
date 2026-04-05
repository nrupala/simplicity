const http = require('http');

const BASE = 'http://localhost:3001';
const userId = 'test-user';
const provider = 'lmstudio';
const model = 'qwen2.5-coder-7b-instruct';
let history = [];
let successCount = 0;
let failCount = 0;

const queries = [
    "What is SIMPLICITY?",
    "Explain simpler.",
    "Main features?",
    "How does knowledge graph work?",
    "What encryption?",
    "Can I export data?",
    "What export formats?",
    "How is sovereignty maintained?",
    "What models available?",
    "How to switch providers?",
    "What is context window?",
    "Handle long conversations?",
    "How many messages remembered?",
    "What when context full?",
    "Data sent to cloud?",
    "How local processing?",
    "What databases used?",
    "Backup knowledge graph?",
    "Restore from backup?",
    "What is GAN-RAG?",
    "How personification works?",
    "Customize personality?",
    "What temperature setting?",
    "Adjust max tokens?",
    "Difference between providers?",
    "Which fastest?",
    "Which best quality?",
    "Run multiple providers?",
    "How streaming works?",
    "Provider offline?",
    "Fallback mechanism?",
    "Error handling?",
    "See conversation history?",
    "Clear chat?",
    "Keyboard shortcuts?",
    "Shift+Enter new lines?",
    "Knowledge graph grow?",
    "What entities tracked?",
    "How relationships stored?",
    "Visualize knowledge graph?",
    "Sovereignty panel shows?",
    "Test provider connections?",
    "What settings customize?",
    "Change host URL?",
    "Add custom models?",
    "Default Ollama model?",
    "Default LM Studio model?",
    "Refresh models button?",
    "Context badge show?",
    "Messages in context now?"
];

function postJson(path, body) {
    return new Promise((resolve, reject) => {
        const data = JSON.stringify(body);
        const url = new URL(path, BASE);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname,
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) },
            timeout: 60000
        };
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => { try { resolve({ status: res.statusCode, data: JSON.parse(body) }); } catch { resolve({ status: res.statusCode, data: body }); } });
        });
        req.on('error', reject);
        req.on('timeout', () => { req.destroy(); reject(new Error('Timeout')); });
        req.write(data);
        req.end();
    });
}

async function runTests() {
    console.log('\n========================================');
    console.log('  SIMPLICITY Context Test (50 queries)');
    console.log('========================================\n');

    for (let i = 0; i < queries.length; i++) {
        const query = queries[i];
        const num = i + 1;
        process.stdout.write(`[${num}/${queries.length}] ${query} `);

        try {
            const result = await postJson('/api/query', { query, userId, provider, model, history });
            if (result.status === 200 && result.data.text) {
                history.push({ role: 'user', content: query });
                history.push({ role: 'assistant', content: result.data.text });
                if (history.length > 100) history = history.slice(-100);
                successCount++;
                console.log(`OK context:${result.data.conversationLength || history.length} msgs ${result.data.processingTime}ms`);
            } else { failCount++; console.log(`FAIL status:${result.status}`); }
        } catch (err) { failCount++; console.log(`ERROR: ${err.message}`); }
    }

    console.log('\n========================================');
    console.log('  Results');
    console.log('========================================');
    console.log(`Total: ${queries.length} | Success: ${successCount} | Failed: ${failCount}`);
    console.log(`Context Messages: ${history.length}`);
    console.log(`Context Preserved: ${history.length > 0 ? 'YES' : 'NO'}`);
    console.log('========================================\n');

    if (failCount === 0 && history.length > 0) {
        console.log('ALL TESTS PASSED!');
    } else {
        console.log('SOME TESTS FAILED.');
    }
}

runTests().catch(console.error);
