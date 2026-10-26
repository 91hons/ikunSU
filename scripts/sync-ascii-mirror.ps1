param(
    [string]$Mirror = 'D:\ikunSU-build'
)

# The Manager cannot build from a path containing non-ASCII characters, so the
# sources are mirrored into an ASCII path before Gradle runs. Every path is
# derived from this script's location so the checkout can live anywhere.
$ErrorActionPreference = 'Stop'

$source = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
New-Item -ItemType Directory -Force -Path $Mirror | Out-Null

$options = @(
    '/MIR', '/NFL', '/NDL', '/NJH', '/NJS', '/NC', '/NS', '/NP',
    '/XD', 'build', '.gradle', '.cxx', '.idea', '.kotlin',
    '/XF', 'local.properties'
)

function Invoke-Mirror([string]$from, [string]$to) {
    & robocopy $from $to @options | Out-Null
    if ($LASTEXITCODE -ge 8) { throw "robocopy $from failed: $LASTEXITCODE" }
}

Invoke-Mirror (Join-Path $source 'manager') (Join-Path $Mirror 'manager')
Invoke-Mirror (Join-Path $source 'uapi') (Join-Path $Mirror 'uapi')

Copy-Item (Join-Path $source 'version.properties') $Mirror -Force
Copy-Item (Join-Path $source 'manager\local.properties') (Join-Path $Mirror 'manager\local.properties') -Force

Write-Output "mirrored $source -> $Mirror"
