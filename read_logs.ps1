Write-Host "=== Reading Ollama Logs ===" -ForegroundColor Cyan
$logFiles = Get-ChildItem "C:\Users\nrupa\AppData\Local\Ollama\*.log"
Write-Host "Found $($logFiles.Count) log files"
Write-Host ""

foreach ($log in $logFiles) {
    Write-Host "=== $($log.Name) ===" -ForegroundColor Yellow
    try {
        Get-Content $log.FullName -Tail 10 -ErrorAction SilentlyContinue
    } catch {
        Write-Host "  (empty or inaccessible)"
    }
    Write-Host ""
}
