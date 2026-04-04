Write-Host "=== Starting Ollama with Explicit GPU Configuration ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] Setting CUDA environment"
$env:CUDA_VISIBLE_DEVICES = "0"
$env:OLLAMA_DEBUG = "1"
[System.Environment]::SetEnvironmentVariable("CUDA_VISIBLE_DEVICES", "0", "Process")

Write-Host ""
Write-Host "[2] Starting Ollama from CMD"
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "cmd.exe"
$psi.Arguments = "/c set CUDA_VISIBLE_DEVICES=0 && `"C:\Users\nrupa\AppData\Local\Programs\Ollama\ollama.exe`" serve"
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true

$process = [System.Diagnostics.Process]::Start($psi)
Write-Host "  Started PID: $($process.Id)"

Write-Host ""
Write-Host "[3] Waiting for server..."
Start-Sleep -Seconds 8

Write-Host ""
Write-Host "[4] Testing API"
try {
    $tags = Invoke-RestMethod -Uri 'http://localhost:11434/api/tags' -TimeoutSec 10
    Write-Host "  API: OK"
    Write-Host "  Models: $($tags.models.name -join ', ')"
} catch {
    Write-Host "  API Failed: $_"
}

Write-Host ""
Write-Host "[5] GPU Detection"
nvidia-smi --query-gpu=index,name,driver,memory.total,compute_cap --format=csv,noheader

Write-Host ""
Write-Host "[6] Testing quick generate"
$body = @{
    model = "codellama:34b-code"
    prompt = "OK"
    stream = $false
} | ConvertTo-Json

$start = Get-Date
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 90
    $duration = ((Get-Date) - $start).TotalSeconds
    Write-Host "  Duration: $($duration)s"
    Write-Host "  Response: $($resp.response.Substring(0, [Math]::Min(30, $resp.response.Length)))"
} catch {
    Write-Host "  Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[7] GPU after"
nvidia-smi --query-gpu=memory.used,memory.total,utilization.gpu --format=csv,noheader
