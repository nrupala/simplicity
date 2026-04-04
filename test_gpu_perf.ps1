Write-Host "=== Testing GPU vs CPU Performance ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] Current GPU"
nvidia-smi --query-gpu=name,memory.used,memory.total --format=csv,noheader

Write-Host ""
Write-Host "[2] Test LM Studio (known fast)"
$body = @{
    model = "qwen2.5-coder-7b-instruct"
    prompt = "Count 1 2 3"
    max_tokens = 20
} | ConvertTo-Json

$start = Get-Date
$resp = Invoke-RestMethod -Uri 'http://localhost:1234/v1/completions' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 30
$duration = Get-Date - $start
Write-Host "  LM Studio: $($duration.TotalSeconds)s - OK"

Write-Host ""
Write-Host "[3] Test Ollama with tiny prompt"
$body2 = @{
    model = "codellama:34b-code"
    prompt = "OK"
    stream = $false
    options = @{
        num_ctx = 512
        num_gpu = 1
    }
} | ConvertTo-Json

$start2 = Get-Date
try {
    $resp2 = Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method POST -Body $body2 -ContentType "application/json" -TimeoutSec 120
    $duration2 = Get-Date - $start2
    Write-Host "  Ollama: $($duration2.TotalSeconds)s"
    Write-Host "  Response: $($resp2.response)"
} catch {
    Write-Host "  Ollama Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[4] GPU after tests"
nvidia-smi --query-gpu=memory.used,utilization.gpu --format=csv,noheader
