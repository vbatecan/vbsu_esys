#!/usr/bin/env powershell
# Setup and run script for real_seeder on Windows

$ErrorActionPreference = "Stop"

# Check if Python exists
Write-Host "Checking for Python installation..." -ForegroundColor Cyan
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) {
    Write-Host "ERROR: Python is not installed or not in PATH." -ForegroundColor Red
    Write-Host "Please install Python 3.8 or higher from https://www.python.org/downloads/" -ForegroundColor Yellow
    exit 1
}

$pythonVersion = python --version 2>&1
Write-Host "Found Python: $pythonVersion" -ForegroundColor Green

# Get the script directory
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$scriptsDir = Join-Path $projectRoot "scripts"
$requirementsPath = Join-Path $scriptsDir "requirements.txt"

# Check if requirements.txt exists
if (-not (Test-Path $requirementsPath)) {
    Write-Host "ERROR: requirements.txt not found at $requirementsPath" -ForegroundColor Red
    exit 1
}

# Install dependencies
Write-Host "`nInstalling dependencies from requirements.txt..." -ForegroundColor Cyan
try {
    pip install -r $requirementsPath
    if ($LASTEXITCODE -ne 0) {
        throw "pip install failed with exit code $LASTEXITCODE"
    }
} catch {
    Write-Host "ERROR: Failed to install dependencies" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host "`nDependencies installed successfully!" -ForegroundColor Green

# Navigate to scripts directory and run the seeder
Write-Host "`nRunning real_seeder..." -ForegroundColor Cyan
Set-Location $scriptsDir

try {
    python -m real_seeder.cli --db-type derby --database sample --user app --password app
    if ($LASTEXITCODE -ne 0) {
        throw "Seeder failed with exit code $LASTEXITCODE"
    }
} catch {
    Write-Host "ERROR: Seeder execution failed" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host "`nSeeder completed successfully!" -ForegroundColor Green
