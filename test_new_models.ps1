Write-Host "=== Testing with New Models ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] Current Models"
$tags = Invoke-RestMethod -Uri 'http://localhost:11434/api/tags' -TimeoutSec 5
$tags.models | ForEach-Object { Write-Host "  - $($_.name)" }

Write-Host ""
Write-Host "[2] Testing gemma3:4b (newest, should be optimized)"
$body = @{
    model = "gemma3:4b"
    messages = @(
        @{role = "user"; content = "Say OK"}
    )
    stream = $false
} | ConvertTo-Json

$start = Get-Date
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/chat' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60
    $duration = ((Get-Date) - $start).TotalSeconds
    Write-Host "  Duration: $($duration)s"
    Write-Host "  Response: $($resp.message.content)"
} catch {
    Write-Host "  Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[3] GPU Status"
nvidia-smi --query-gpu=memory.used,memory.total,utilization.gpu --format=csv,noheader

Write-Host ""
Write-Host "[4] Testing deepseek-r1:8b"
$body2 = @{
    model = "deepseek-r1:8b"
    messages = @(
        @{role = "user"; content = "Say OK"}
    )
    stream = $false
} | ConvertTo-Json

$start2 = Get-Date
try {
    $resp2 = Invoke-RestMethod -Uri 'http://localhost:11434/api/chat' -Method POST -Body $body2 -ContentType "application/json" -TimeoutSec 60
    $duration2 = ((Get-Date) - $start2).TotalSeconds
    Write-Host "  Duration: $($duration2)s"
    Write-Host "  Response: $($resp2.message.content)"
} catch {
    Write-Host "  Error: $($_.Exception.Message)"
}
