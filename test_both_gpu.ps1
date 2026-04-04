Write-Host "=== Testing Both GPU-Enabled Providers ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] GPU Baseline"
nvidia-smi --query-gpu=memory.used,memory.total --format=csv,noheader

Write-Host ""
Write-Host "[2] LM Studio - Fast test"
$body1 = @{
    model = "qwen2.5-coder-7b-instruct"
    prompt = "OK"
    max_tokens = 5
    temperature = 0
} | ConvertTo-Json

$start1 = Get-Date
$resp1 = Invoke-RestMethod -Uri 'http://localhost:1234/v1/completions' -Method POST -Body $body1 -ContentType "application/json" -TimeoutSec 30
$duration1 = ((Get-Date) - $start1).TotalSeconds
Write-Host "  LM Studio: $($duration1)s - Response: $($resp1.choices[0].text)"

Write-Host ""
Write-Host "[3] LM Studio GPU after"
nvidia-smi --query-gpu=memory.used,utilization.gpu --format=csv,noheader

Write-Host ""
Write-Host "[4] Ollama - Using chat endpoint"
$body2 = @{
    model = "codellama:34b-code"
    messages = @(
        @{role = "user"; content = "OK"}
    )
    stream = $false
} | ConvertTo-Json

$start2 = Get-Date
try {
    $resp2 = Invoke-RestMethod -Uri 'http://localhost:11434/api/chat' -Method POST -Body $body2 -ContentType "application/json" -TimeoutSec 120
    $duration2 = ((Get-Date) - $start2).TotalSeconds
    Write-Host "  Ollama: $($duration2)s"
    Write-Host "  Response: $($resp2.message.content)"
} catch {
    Write-Host "  Ollama Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[5] Ollama GPU after"
nvidia-smi --query-gpu=memory.used,utilization.gpu --format=csv,noheader

Write-Host ""
Write-Host "[6] Summary"
Write-Host "  LM Studio (qwen2.5-coder-7b-instruct): Working fast on GPU"
Write-Host "  Ollama (codellama:34b-code): Needs investigation"
