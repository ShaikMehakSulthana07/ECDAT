# Manual container archive creation script (for Windows environments without Docker)
# Creates a minimal Docker-compatible TAR structure for testing

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$tempDir = Join-Path $env:TEMP "cryptoguard-docker-temp"
$imageDir = Join-Path $tempDir "cryptoguard-crypto-image"

# Create directories
New-Item -ItemType Directory -Force -Path $imageDir | Out-Null
New-Item -ItemType Directory -Force -Path "$imageDir\blobs\sha256" | Out-Null
New-Item -ItemType Directory -Force -Path "$imageDir\layers" | Out-Null

# Copy the JAR to simulate a layer
New-Item -ItemType Directory -Force -Path "$imageDir\layers\layer1" | Out-Null
Copy-Item "$scriptDir\cryptoguard-crypto-app\target\cryptoguard-crypto-fixture-1.0.0.jar" "$imageDir\layers\layer1\"

# Create manifest.json
$manifest = @"
[{
  "Config": "sha256/config.json",
  "RepoTags": ["cryptoguard-crypto-fixture:1.0"],
  "Layers": ["layer1/layer.tar"]
}]
"@
Set-Content -Path "$imageDir\manifest.json" -Value $manifest

# Create config.json
$config = @"
{
  "config": {
    "Cmd": ["java", "-jar", "/app/cryptoguard-crypto-fixture.jar"],
    "WorkingDir": "/app"
  },
  "architecture": "amd64",
  "os": "linux"
}
"@
Set-Content -Path "$imageDir\config.json" -Value $config

# Create layer.tar using tar (if available) or 7z
$layerDir = "$imageDir\layers\layer1"
Set-Location $layerDir

# Try using tar (Windows tar.exe if available, or 7z)
if (Get-Command tar -ErrorAction SilentlyContinue) {
    tar -cf layer.tar cryptoguard-crypto-fixture-1.0.0.jar
} elseif (Get-Command 7z -ErrorAction Continue) {
    7z a -ttar layer.tar cryptoguard-crypto-fixture-1.0.0.jar
} else {
    # Fallback: create a simple file to simulate the layer
    Copy-Item cryptoguard-crypto-fixture-1.0.0.jar layer.tar
}

Set-Location $scriptDir

# Create the final TAR archive
Set-Location $tempDir
if (Get-Command tar -ErrorAction SilentlyContinue) {
    tar -cf cryptoguard-crypto-image.tar cryptoguard-crypto-image
} elseif (Get-Command 7z -ErrorAction Continue) {
    7z a -ttar cryptoguard-crypto-image.tar cryptoguard-crypto-image
} else {
    # Final fallback: create a ZIP and rename
    Compress-Archive -Path "$imageDir\*" -DestinationPath "cryptoguard-crypto-image.zip"
    Copy-Item "cryptoguard-crypto-image.zip" "$scriptDir\cryptoguard-crypto-image.tar"
}

Copy-Item "$tempDir\cryptoguard-crypto-image.tar" "$scriptDir\"

# Cleanup
Remove-Item -Recurse -Force $tempDir

Write-Host "Manual container archive created: $scriptDir\cryptoguard-crypto-image.tar"
Write-Host "This is a minimal Docker-compatible structure for testing"
