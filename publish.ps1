# 热管理学习 App - 一键推送到 GitHub
# 用法（在 thermal-app 目录执行）：
#   powershell -ExecutionPolicy Bypass -File publish.ps1
$ErrorActionPreference = "Stop"

$user = Read-Host "请输入 GitHub 用户名"
$repo = Read-Host "请输入仓库名（如 thermal-learning-app，仅英文/数字/中划线）"

if (-not $user -or -not $repo) {
    Write-Host "用户名和仓库名都不能为空，已退出。" -ForegroundColor Red
    exit 1
}

$remote = "https://github.com/$user/$repo.git"
Write-Host "目标仓库: $remote" -ForegroundColor Cyan

# 清理旧的 origin（若有），避免重复
git remote remove origin 2>$null
git remote add origin $remote
git branch -M main

Write-Host "正在推送 main 分支..." -ForegroundColor Yellow
Write-Host "（若弹窗请求登录，请用浏览器登录 GitHub；若要求输入密码，请用 Personal Access Token 当作密码）" -ForegroundColor DarkGray

git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ 推送完成！" -ForegroundColor Green
    Write-Host "下一步：打开 https://github.com/$user/$repo" -ForegroundColor Cyan
    Write-Host "→ 点 Actions 标签 → 选 Build Signed APK → Run workflow → 下载 Artifacts 里的 apk" -ForegroundColor Cyan
} else {
    Write-Host "❌ 推送失败，请检查用户名/仓库名或登录凭据。" -ForegroundColor Red
}
