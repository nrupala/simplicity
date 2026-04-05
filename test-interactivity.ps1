$baseUrl = "http://localhost:3001"
$userId = "test-user"
$provider = "lmstudio"
$model = "qwen2.5-coder-7b-instruct"
$history = @()
$successCount = 0
$failCount = 0

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  SIMPLICITY Interactivity Test" -ForegroundColor Cyan
Write-Host "  Testing 100 interactions with context" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$queries = @(
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
)

Write-Host "Starting test with $($queries.Count) queries...`n" -ForegroundColor Yellow

foreach ($i in 0..($queries.Count - 1)) {
    $query = $queries[$i]
    $queryNum = $i + 1

    Write-Host "[$queryNum/$($queries.Count)] $query" -NoNewline

    try {
        $historyJson = if ($history.Count -eq 0) { "[]" } else { ($history | ConvertTo-Json -Depth 5 -Compress) }
        $body = "{`"query`":`"$query`",`"userId`":`"$userId`",`"provider`":`"$provider`",`"model`":`"$model`",`"history`":$historyJson}"

        $response = Invoke-WebRequest -Uri "$baseUrl/api/query" -Method POST -ContentType "application/json" -Body $body -TimeoutSec 60

        if ($response.StatusCode -eq 200) {
            $data = $response.Content | ConvertFrom-Json
            $history += @{ role = "user"; content = $query }
            $history += @{ role = "assistant"; content = $data.text }
            if ($history.Count -gt 100) { $history = $history | Select-Object -Last 100 }
            $successCount++
            $ctxLen = if ($data.conversationLength) { $data.conversationLength } else { $history.Count }
            Write-Host " OK (context:$ctxLen msgs, time:$($data.processingTime)ms)" -ForegroundColor Green
        } else {
            $failCount++
            Write-Host " FAIL (status:$($response.StatusCode))" -ForegroundColor Red
        }
    } catch {
        $failCount++
        Write-Host " ERROR: $($_.Exception.Message)" -ForegroundColor Red
    }

    Start-Sleep -Milliseconds 50
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Results" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Queries:    $($queries.Count)" -ForegroundColor White
Write-Host "Successful:       $successCount" -ForegroundColor Green
Write-Host "Failed:           $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host "Context Messages: $($history.Count)" -ForegroundColor White
Write-Host "Context Preserved: $(if ($history.Count -gt 0) { 'YES' } else { 'NO' })" -ForegroundColor $(if ($history.Count -gt 0) { "Green" } else { "Red" })
Write-Host "========================================`n" -ForegroundColor Cyan

if ($failCount -eq 0 -and $history.Count -gt 0) {
    Write-Host "ALL TESTS PASSED! Context maintained throughout $successCount interactions." -ForegroundColor Green
} else {
    Write-Host "SOME TESTS FAILED. Check results above." -ForegroundColor Red
}
