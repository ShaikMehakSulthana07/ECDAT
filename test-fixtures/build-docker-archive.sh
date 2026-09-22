#!/bin/bash
# Build script for creating a Docker image archive
# Run this from the cryptoguard-crypto-app directory

cd "$(dirname "$0")"

# Build the JAR first
mvn clean package

# Build Docker image
docker build -t cryptoguard-crypto-fixture:1.0 .

# Export as TAR archive
docker save cryptoguard-crypto-fixture:1.0 -o ../cryptoguard-crypto-image.tar

echo "Docker image archive created: ../cryptoguard-crypto-image.tar"
echo "To use: docker load -i ../cryptoguard-crypto-image.tar"
