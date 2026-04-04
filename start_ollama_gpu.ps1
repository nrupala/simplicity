Write-Host "=== Configuring Ollama for GPU Acceleration ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "[1] Setting GPU environment variables"
$envVars = @{
    "CUDA_VISIBLE_DEVICES" = "0"
}

foreach ($key in $envVars.Keys) {
    [System.Environment]::SetEnvironmentVariable($key, $envVars[$key], "Process")
    Write-Host "  $key = $($envVars[$key])"
}

Write-Host ""
Write-Host "[2] Stopping any existing Ollama processes"
Get-Process -Name ollama -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Host "  Done"

Write-Host ""
Write-Host "[3] Starting Ollama with GPU acceleration"
$startInfo = New-Object System.Diagnostics.ProcessStartInfo
$startInfo.FileName = "C:\Users\nrupa\AppData\Local\Programs\Ollama\ollama.exe"
$startInfo.Arguments = "serve"
$startInfo.UseShellExecute = $false
$startInfo.RedirectStandardOutput = $false
$startInfo.RedirectStandardError = $false
$startInfo.CreateNoWindow = $true
$startInfo.EnvironmentVariables["CUDA_VISIBLE_DEVICES"] = "0"

$process = [System.Diagnostics.Process]::Start($startInfo)
Write-Host "  Ollama started (PID: $($process.Id))"

Write-Host ""
Write-Host "[4] Waiting for Ollama to initialize..."
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "[5] Verifying Ollama is running"
try {
    $tags = Invoke-RestMethod -Uri 'http://localhost:11434/api/tags' -TimeoutSec 10
    Write-Host "  Status: Running"
    Write-Host "  Models: $($tags.models.name -join ', ')"
} catch {
    Write-Host "  Status: Not responding - $_"
}

Write-Host ""
Write-Host "[6] GPU Status"
nvidia-smi --query-gpu=index,name,memory.used,memory.total,utilization.gpu --format=csv,noheader

Write-Host ""
Write-Host "[7] Testing quick inference"
$body = @{
    model = "codellama:34b-code"
    prompt = "Hi"
    stream = $false
} | ConvertTo-Json

$start = Get-Date
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 90
    $duration = (Get-Date) - $start
    Write-Host "  Duration: $($duration.TotalSeconds)s"
    Write-Host "  Response: $($resp.response.Substring(0, [Math]::Min(50, $resp.response.Length)))"
} catch {
    Write-Host "  Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "[8] GPU Status After Inference"
nvidia-smi --query-gpu=memory.used,memory.total,utilization.gpu --format=csv,noheader
