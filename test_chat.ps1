Write-Host "=== Testing Chat with Current Models ==="
Write-Host ""

Write-Host "[1] Ollama - codellama:34b-code"
$body = @{
    model = "codellama:34b-code"
    prompt = "Hello! Reply with just 'Hi from Ollama codellama'"
    stream = $false
} | ConvertTo-Json

try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:11434/api/generate' -Method POST -Body $body -ContentType "application/json" -TimeoutSec 30
    Write-Host "Response: $($resp.response.Substring(0, [Math]::Min(50, $resp.response.Length)))..."
} catch {
    Write-Host "Error: $_"
}

Write-Host ""
Write-Host "[2] LM Studio - qwen2.5-coder-7b-instruct"
$body2 = @{
    model = "qwen2.5-coder-7b-instruct"
    prompt = "Hello! Reply with just 'Hi from LM Studio qwen'"
    max_tokens = 50
} | ConvertTo-Json

try {
    $resp2 = Invoke-RestMethod -Uri 'http://localhost:1234/v1/completions' -Method POST -Body $body2 -ContentType "application/json" -TimeoutSec 30
    Write-Host "Response: $($resp2.choices[0].text.Substring(0, [Math]::Min(50, $resp2.choices[0].text.Length)))..."
} catch {
    Write-Host "Error: $_"
}
