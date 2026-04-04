Write-Host "=== Testing Simplicity Web App Provider ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1] Checking Ollama (port 11434)"
try {
    $start = Get-Date
    $resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/tags' -TimeoutSec 5
    Write-Host "  Status: OK"
    Write-Host "  Models: $($resp.models.name -join ', ')"
} catch {
    Write-Host "  Status: Failed - $_"
}

Write-Host ""
Write-Host "[2] Checking LM Studio (port 1234)"
try {
    $start = Get-Date
    $resp = Invoke-RestMethod -Uri 'http://localhost:1234/v1/models' -TimeoutSec 5
    Write-Host "  Status: OK"
    Write-Host "  Models: $($resp.data.id -join ', ')"
} catch {
    Write-Host "  Status: Failed - $_"
}

Write-Host ""
Write-Host "[3] Testing LM Studio Chat (v1/chat/completions)"
$body = @{
    messages = @(
        @{role = "user"; content = "Say 'OK' in one word"}
    )
    max_tokens = 10
    temperature = 0
} | ConvertTo-Json

$start = Get-Date
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:1234/v1/chat/completions' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60
    $duration = (Get-Date) - $start
    Write-Host "  Status: OK"
    Write-Host "  Duration: $($duration.TotalSeconds)s"
    Write-Host "  Response: $($resp.choices[0].message.content)"
} catch {
    Write-Host "  Status: Failed - $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[4] Checking Current GPU Memory"
nvidia-smi --query-gpu=index,name,memory.used,memory.total --format=csv,noheader
