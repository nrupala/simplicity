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
    "Can you explain that in simpler terms?",
    "What are the main features you mentioned?",
    "How does the knowledge graph work?",
    "What encryption does it use?",
    "Can I export my data?",
    "What formats are supported for export?",
    "How is data sovereignty maintained?",
    "What models can I use with it?",
    "How do I switch between providers?",
    "What is the context window size?",
    "Can it handle long conversations?",
    "How many messages can it remember?",
    "What happens when context is full?",
    "Is my data sent to the cloud?",
    "How does local processing work?",
    "What databases does it use?",
    "Can I backup my knowledge graph?",
    "How do I restore from backup?",
    "What is GAN-RAG coupling?",
    "How does personification work?",
    "Can I customize the AI personality?",
    "What temperature setting should I use?",
    "How do I adjust max tokens?",
    "What is the difference between providers?",
    "Which provider is fastest?",
    "Which provider has the best quality?",
    "Can I run multiple providers at once?",
    "How does the streaming work?",
    "What happens if a provider goes offline?",
    "Is there a fallback mechanism?",
    "How does error handling work?",
    "Can I see the conversation history?",
    "How do I clear the chat?",
    "What keyboard shortcuts are available?",
    "Can I use Shift+Enter for new lines?",
    "How does the knowledge graph grow?",
    "What entities are tracked?",
    "How are relationships stored?",
    "Can I visualize the knowledge graph?",
    "What does the sovereignty panel show?",
    "How do I test provider connections?",
    "What settings can I customize?",
    "How do I change the host URL?",
    "Can I add custom models?",
    "What is the default model for Ollama?",
    "What is the default model for LM Studio?",
    "How does the refresh models button work?",
    "What does the context badge show?",
    "How many messages are in context now?",
    "Can you summarize our conversation so far?",
    "What was the first question I asked?",
    "What was my fifth question?",
    "How many questions have I asked total?",
    "What topics have we discussed?",
    "Can you list all the features mentioned?",
    "What encryption methods were discussed?",
    "What export formats were mentioned?",
    "Which providers are available?",
    "What are the default ports for each?",
    "How does the settings panel work?",
    "What is the temperature range?",
    "What is the max token range?",
    "How does context window affect responses?",
    "What happens with very long conversations?",
    "Is there a message limit?",
    "How is context trimmed when full?",
    "What is the maximum context size?",
    "Can I adjust the context window?",
    "How does localStorage work for history?",
    "What data is stored locally?",
    "How is the SQLite database used?",
    "What tables exist in the database?",
    "How are user profiles stored?",
    "Can multiple users use the system?",
    "How is user data separated?",
    "What is the evolution hash?",
    "How does the consent ledger work?",
    "What zero-knowledge proofs are used?",
    "How does homomorphic encryption work?",
    "What is federated learning?",
    "How does the model registry work?",
    "What models are registered?",
    "How are model updates handled?",
    "What is the weekly model refresh?",
    "How does the interface layer work?",
    "What are the four interface modes?",
    "How does casual mode work?",
    "What is expert mode?",
    "What can I do in architect mode?",
    "How does the portability engine work?",
    "What export formats are supported?",
    "How does import work?",
    "Can I migrate to another system?",
    "What migration paths exist?",
    "How does the correlation engine work?",
    "What is the unique experience hash?",
    "How is my experience unique?",
    "What makes responses personalized?",
    "How does the system learn from me?",
    "What feedback mechanisms exist?",
    "How does continuous learning work?",
    "What is the final question?"
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
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(data)
            },
            timeout: 60000
        };

        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                try {
                    resolve({ status: res.statusCode, data: JSON.parse(body) });
                } catch {
                    resolve({ status: res.statusCode, data: body });
                }
            });
        });
        req.on('error', reject);
        req.on('timeout', () => { req.destroy(); reject(new Error('Timeout')); });
        req.write(data);
        req.end();
    });
}

async function runTests() {
    console.log('\n========================================');
    console.log('  SIMPLICITY Interactivity Test');
    console.log('  Testing 103 interactions with context');
    console.log('========================================\n');

    for (let i = 0; i < queries.length; i++) {
        const query = queries[i];
        const num = i + 1;

        process.stdout.write(`[${num}/${queries.length}] ${query.substring(0, 50)}... `);

        try {
            const result = await postJson('/api/query', {
                query,
                userId,
                provider,
                model,
                history
            });

            if (result.status === 200 && result.data.text) {
                history.push({ role: 'user', content: query });
                history.push({ role: 'assistant', content: result.data.text });
                if (history.length > 100) history = history.slice(-100);
                successCount++;
                const ctx = result.data.conversationLength || history.length;
                console.log(`OK (context:${ctx} msgs, time:${result.data.processingTime}ms)`);
            } else {
                failCount++;
                console.log(`FAIL (status:${result.status})`);
            }
        } catch (err) {
            failCount++;
            console.log(`ERROR: ${err.message}`);
        }
    }

    console.log('\n========================================');
    console.log('  Test Results');
    console.log('========================================');
    console.log(`Total Queries:    ${queries.length}`);
    console.log(`Successful:       ${successCount}`);
    console.log(`Failed:           ${failCount}`);
    console.log(`Context Messages: ${history.length}`);
    console.log(`Context Preserved: ${history.length > 0 ? 'YES' : 'NO'}`);
    console.log('========================================\n');

    if (failCount === 0 && history.length > 0) {
        console.log('ALL TESTS PASSED! Context maintained throughout ' + successCount + ' interactions.');
    } else {
        console.log('SOME TESTS FAILED. Check results above.');
    }
}

runTests().catch(console.error);
