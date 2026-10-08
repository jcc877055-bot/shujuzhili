[CmdletBinding()]
param(
    [switch]$PromptForPassword,
    [switch]$Bootstrap
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$javaHome = Join-Path $env:USERPROFILE '.jdks\ms-17.0.20'
$java = Join-Path $javaHome 'bin\java.exe'
$jar = Join-Path $PSScriptRoot 'target\datagov-backend-0.1.0-SNAPSHOT.jar'

if ($PromptForPassword) {
    $env:DB_PASSWORD = $null
}

if (-not (Test-Path $java)) {
    throw "Java 17 was not found at: $java"
}

if (-not $PromptForPassword -and [string]::IsNullOrWhiteSpace($env:DB_PASSWORD) -and -not [string]::IsNullOrWhiteSpace($env:MYSQL_PASSWORD)) {
    $env:DB_PASSWORD = $env:MYSQL_PASSWORD
}

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $securePassword = Read-Host 'Enter the local MySQL root password' -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
}

$env:FLYWAY_ENABLED = 'true'

if ($Bootstrap) {
    $env:BOOTSTRAP_ENABLED = 'true'
    $env:BOOTSTRAP_ORG_CODE = 'PHASE1ROOT'
    $env:BOOTSTRAP_USERNAME = 'sysadmin'
    $secureAdminPassword = Read-Host 'Enter the initial application admin password' -AsSecureString
    $adminPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureAdminPassword)
    try {
        $env:BOOTSTRAP_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPasswordPointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPasswordPointer)
    }
    Write-Host 'Creating the initial administrator for organization PHASE1ROOT'
} else {
    $env:BOOTSTRAP_ENABLED = 'false'
}

Write-Host 'Starting backend at 127.0.0.1:8080 with database soft_dev'
& $java '-jar' $jar
