#!/bin/bash
# Manual container archive creation script (for environments without Docker)
# Creates a minimal Docker-compatible TAR structure for testing

cd "$(dirname "$0")"

# Create temporary directory for the archive
TEMP_DIR=$(mktemp -d)
IMAGE_DIR="$TEMP_DIR/cryptoguard-crypto-image"

# Create image structure
mkdir -p "$IMAGE_DIR/blobs/sha256"
mkdir -p "$IMAGE_DIR/layers"

# Copy the JAR to simulate a layer
mkdir -p "$IMAGE_DIR/layers/layer1"
cp target/cryptoguard-crypto-fixture-1.0.0.jar "$IMAGE_DIR/layers/layer1/"

# Create manifest.json
cat > "$IMAGE_DIR/manifest.json" << 'EOF'
[{
  "Config": "sha256/config.json",
  "RepoTags": ["cryptoguard-crypto-fixture:1.0"],
  "Layers": ["layer1/layer.tar"]
}]
EOF

# Create config.json
cat > "$IMAGE_DIR/config.json" << 'EOF'
{
  "config": {
    "Cmd": ["java", "-jar", "/app/cryptoguard-crypto-fixture.jar"],
    "WorkingDir": "/app"
  },
  "architecture": "amd64",
  "os": "linux"
}
EOF

# Create layer.tar (simple tar containing the JAR)
cd "$IMAGE_DIR/layers/layer1"
tar -cf layer.tar cryptoguard-crypto-fixture-1.0.0.jar
cd -

# Create the final TAR archive
cd "$TEMP_DIR"
tar -cf cryptoguard-crypto-image.tar cryptoguard-crypto-image

# Copy to fixtures directory
cp cryptoguard-crypto-image.tar ../

# Cleanup
rm -rf "$TEMP_DIR"

echo "Manual container archive created: ../cryptoguard-crypto-image.tar"
echo "This is a minimal Docker-compatible structure for testing"
