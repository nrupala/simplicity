const axios = require('axios');

async function testAPI() {
    try {
        console.log('Testing SIMPLICITY API...');

        // Test health endpoint
        const healthResponse = await axios.get('http://localhost:3001/health');
        console.log('Health check:', healthResponse.data);

        // Test query endpoint
        const queryResponse = await axios.post('http://localhost:3001/api/query', {
            query: 'What is SIMPLICITY?',
            userId: 'test-user'
        });
        console.log('Query response:', queryResponse.data);

        // Test knowledge endpoint
        const knowledgeResponse = await axios.get('http://localhost:3001/api/knowledge?userId=test-user');
        console.log('Knowledge response:', knowledgeResponse.data);

        console.log('All tests passed!');

    } catch (error) {
        console.error('Test failed:', error.response?.data || error.message);
    }
}

testAPI();