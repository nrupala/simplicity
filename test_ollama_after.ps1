Write-Host "=== Testing After Ollama GPU Load ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] GPU Memory - Should be loaded now"
nvidia-smi --query-gpu=index,name,memory.used,memory.total --format=csv,noheader

Write-Host ""
Write-Host "[2] Second test - model should be cached in GPU"
$body = @{
    model = "codellama:34b-code"
    prompt = "Say 'Test' in one word"
    stream = $false
} | ConvertTo-Json

$start = Get-Date
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60
    $duration = (Get-Date) - $start
    Write-Host "  Duration: $($duration.TotalSeconds)s"
    Write-Host "  Response: $($resp.response)"
} catch {
    Write-Host "  Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[3] GPU After"
nvidia-smi --query-gpu=memory.used,utilization.gpu --format=csv,noheader

Write-Host ""
Write-Host "=== LM Studio Comparison ===" -ForegroundColor Cyan
$body2 = @{
    model = "qwen2.5-coder-7b-instruct"
    prompt = "Say 'Test' in one word"
    max_tokens = 10
    temperature = 0
} | ConvertTo-Json

$start2 = Get-Date
try {
    $resp2 = Invoke-RestMethod -Uri 'http://localhost:1234/v1/completions' -Method POST -Body $body2 -ContentType "application/json" -TimeoutSec 30
    $duration2 = (Get-Date) - $start2
    Write-Host "  LM Studio Duration: $($duration2.TotalSeconds)s"
    Write-Host "  Response: $($resp2.choices[0].text)"
} catch {
    Write-Host "  Error: $($_.Exception.Message)"
}
