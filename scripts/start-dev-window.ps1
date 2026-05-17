param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("backend", "frontend")]
    [string]$Kind,

    [Parameter(Mandatory = $true)]
    [string]$Root
)

$Root = $Root.TrimEnd('\', '/')

if ($Kind -eq "backend") {
    $workDir = Join-Path $Root "javaService\demo"
    $innerCmd = "chcp 65001 >nul & title LMS-Backend & call mvnw.cmd spring-boot:run"
    $pidFile = Join-Path $Root ".dev-backend.pid"
    $windowTitle = "LMS-Backend"
} else {
    $workDir = Join-Path $Root "frontEnd"
    $innerCmd = "title LMS-Frontend & pnpm dev"
    $pidFile = Join-Path $Root ".dev-frontend.pid"
    $windowTitle = "LMS-Frontend"
}

if (-not (Test-Path -LiteralPath $workDir)) {
    Write-Error "目录不存在: $workDir"
    exit 1
}

# cmd.exe 正确用法：/k 与后续命令分为两个参数，并指定工作目录
$p = Start-Process -FilePath "cmd.exe" `
    -WorkingDirectory $workDir `
    -ArgumentList @("/k", $innerCmd) `
    -PassThru

$p.Id | Out-File -LiteralPath $pidFile -Encoding ascii -NoNewline
Write-Host "已启动 $windowTitle (PID $($p.Id))"
