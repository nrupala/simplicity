Write-Host "=== Starting Ollama via Official App (Auto GPU) ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] Starting Ollama app (GUI handles GPU auto-detection)"
Start-Process -FilePath "C:\Users\nrupa\AppData\Local\Programs\Ollama\Ollama.exe" -ArgumentList "run --verbose codellama:34b-code" -PassThru

Write-Host ""
Write-Host "[2] Waiting for model load..."
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "[3] Checking process"
$procs = Get-Process -Name ollama -ErrorAction SilentlyContinue
$procs | ForEach-Object {
    Write-Host "  PID: $($_.Id) - Memory: $([math]::Round($_.WorkingSet64/1GB, 2)) GB"
}

Write-Host ""
Write-Host "[4] Checking API"
try {
    $tags = Invoke-RestMethod -Uri 'http://localhost:11434/api/tags' -TimeoutSec 10
    Write-Host "  API: OK"
} catch {
    Write-Host "  API: Not ready"
}

Write-Host ""
Write-Host "[5] GPU Status"
nvidia-smi --query-gpu=index,name,memory.used,memory.total --format=csv,noheader

Write-Host ""
Write-Host "[6] Test simple prompt"
$body = @{
    model = "codellama:34b-code"
    prompt = "x"
    stream = $false
} | ConvertTo-Json

$start = Get-Date
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60
    $duration = ((Get-Date) - $start).TotalSeconds
    Write-Host "  Success: $($duration)s"
} catch {
    Write-Host "  Failed: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[7] GPU after"
nvidia-smi --query-gpu=memory.used,utilization.gpu --format=csv,noheader
