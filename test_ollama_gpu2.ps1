Write-Host "=== Testing Ollama Simple GPU Test ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] Before - GPU Memory"
nvidia-smi --query-gpu=memory.used,memory.total --format=csv,noheader

Write-Host ""
Write-Host "[2] Testing Ollama Basic Generate"
$body = @{
    model = "codellama:34b-code"
    prompt = "Say 'Hi' in one word"
    stream = $false
} | ConvertTo-Json

$start = Get-Date
$resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60
$duration = (Get-Date) - $start
Write-Host "  Duration: $($duration.TotalSeconds)s"
Write-Host "  Response: $($resp.response)"

Write-Host ""
Write-Host "[3] During - GPU Memory"
nvidia-smi --query-gpu=memory.used,memory.total,utilization.gpu --format=csv,noheader

Write-Host ""
Write-Host "[4] After - GPU Memory"
Start-Sleep -Seconds 2
nvidia-smi --query-gpu=memory.used,memory.total,utilization.gpu --format=csv,noheader
