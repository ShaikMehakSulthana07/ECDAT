# Minimal Docker image for CryptoGuard container scanning tests
# This image contains the crypto fixture JAR for static analysis only
# DO NOT RUN THIS IMAGE - it exists only for scanning tests

FROM eclipse-temurin:21-jre

# Copy the crypto fixture JAR
COPY target/cryptoguard-crypto-fixture-1.0.0.jar /app/cryptoguard-crypto-fixture.jar

# Create app directory
WORKDIR /app

# Entry point (should never be executed for scanning tests)
ENTRYPOINT ["java", "-jar", "/app/cryptoguard-crypto-fixture.jar"]

# Labels for identification
LABEL description="CryptoGuard crypto fixture for static analysis"
LABEL purpose="TEST_DATA_ONLY"
LABEL crypto-content="RSA,ECDSA,ECDHES,AES,SHA-256,SHA-1,MD5,TLS"
