##usage: run .\verify_git.ps1 to verify the git file is pushed correctly.

Write-Host "--- Checking Remote Connection ---" -ForegroundColor Cyan
git remote -v
Write-Host "`n--- Fetching Latest from Origin ---" -ForegroundColor Cyan
git fetch origin
Write-Host "`n--- Branch Tracking Status ---" -ForegroundColor Cyan
git branch -vv
Write-Host "`n--- Local vs Remote Sync ---" -ForegroundColor Cyan
git status -sb
Write-Host "`n--- Last 5 Commits (Visual) ---" -ForegroundColor Cyan
git log --oneline -n 5 --graph --all
