# ECDAT
Enterprise Cryptographic Discovery & Analysis Tool (ECDAT) — SIH 2026

## Project Description
ECDAT is an enterprise-grade tool for discovering and analyzing cryptographic implementations across software systems. This project is developed for SIH 2026.

## Technology Stack

### Backend
- Java 21
- Spring Boot 4.1.1
- Maven
- Spring Web (MVC)
- Spring Actuator

### Frontend
- React 19.2.8
- TypeScript
- Vite 8.2.2

## Getting Started

### Backend
```bash
cd backend
mvn spring-boot:run
```
The backend will start on `http://localhost:8080`

### Frontend
```bash
cd frontend
npm run dev
```
The frontend will start on `http://localhost:5173`

## Testing

### Backend Tests
```bash
cd backend
mvn clean test
```

### Frontend Tests & Build
```bash
cd frontend
npm test
npm run build
```

## Project Structure
```
ECDAT/
├── backend/          # Spring Boot backend
│   ├── src/main/java/com/ecdat/backend/
│   │   ├── cbom/                    # CBOM generation
│   │   ├── controller/              # REST API controllers
│   │   ├── dto/                     # Data transfer objects
│   │   ├── input/                   # Input processors & security
│   │   ├── inventory/               # Asset classification
│   │   ├── pqc/                     # PQC recommendation engine
│   │   ├── risk/                    # Risk assessment engine
│   │   ├── scanner/                 # Discovery scanners
│   │   └── service/                 # Business logic services
│   └── src/test/                    # Comprehensive test suite
├── frontend/        # React + TypeScript + Vite frontend
│   ├── src/components/              # UI components
│   ├── src/services/                # API client
│   ├── src/types/                   # TypeScript types
│   └── src/test/                    # Frontend tests
├── test-target/     # Sample Java project for testing
├── test-fixtures/   # Test data and fixtures
├── docs/            # Documentation
└── README.md
```

## Supported Input Types

ECDAT supports multiple input sources for cryptographic analysis:

| Input Type | Supported | API Endpoint | File Formats | Notes |
|------------|-----------|---------------|---------------|-------|
| **ZIP Archive** | ✅ Yes | `POST /api/analyze/upload` | .zip | Project archives with source code |
| **Directory Path** | ✅ Yes | `POST /api/analyze` | Local directory | Requires security configuration |
| **Repository URL** | ✅ Yes | `POST /api/analyze/repository` | HTTPS Git URLs | Public repositories only, SSRF protected |
| **Configuration File** | ✅ Yes | `POST /api/analyze/configuration` | .properties, .yml, .yaml, .xml, .conf, .cfg, .ini | 10 MB max file size |
| **Binary File** | ✅ Yes | `POST /api/analyze/binary` | .jar, .class | Static bytecode analysis, no execution |
| **Container Image** | ✅ Yes | `POST /api/analyze/container` | .tar, .tar.gz, .tgz | Static Docker/OCI analysis, no daemon required |

## Current Limitations

### Discovery Scope
- **Software-Centric Discovery**: ECDAT focuses on static analysis of software artifacts (source code, binaries, configurations). It does not perform live infrastructure scanning or runtime cryptographic discovery.
- **Language Support**: Currently optimized for Java source code analysis. Other languages are not supported.
- **Static Analysis Boundaries**: Cannot resolve cryptographic algorithms constructed dynamically at runtime (marked as `UNKNOWN` with `LOW` confidence).
- **Dependency Presence vs Usage**: Library/dependency presence in build files is not treated as proof of cryptographic usage.

### Infrastructure & Cloud
- **No HSM/KMS Integration**: Does not discover cryptographic assets in Hardware Security Modules or Key Management Services.
- **No Cloud Service Discovery**: No integration with AWS KMS, Azure Key Vault, GCP KMS, or other cloud cryptographic services.
- **No Live Runtime Analysis**: Cannot discover cryptographic operations in running applications or network traffic.

### Quantum Risk Assessment
- **Prototype Methodology**: Risk scores use an ECDAT prototype heuristic (0-100), not official NIST/CVSS scoring formulas.
- **Threat Horizon Assumptions**: Quantum threat timelines are configurable but not based on real-time threat intelligence.
- **Mosca's Theorem Implementation**: Quantum risk calculations use a simplified implementation of Mosca's theorem (X+Y > Z) without complex migration modeling.

### Recommendations vs Migration
- **Advisory Only**: PQC recommendations are advisory suggestions for migration planning. ECDAT does not automatically modify source code, replace certificates, or enforce cryptographic policies.
- **No Automated Migration**: No code generation, certificate renewal, or automated migration tooling.
- **Manual Implementation Required**: Organizations must manually implement recommended PQC algorithms based on guidance.

### Security & Access Control
- **No Authentication**: Basic UI login screen exists but has no backend authentication or authorization implementation.
- **No Authorization**: No role-based access control or user permissions.
- **No Audit Logging**: No audit trail for analysis requests or results.
- **In-Memory Only**: All analysis data is stored in-memory per request with no database persistence.

### Operational Constraints
- **Synchronous Processing**: API executes analysis synchronously. Large codebases may experience higher latency.
- **Single-Tenant**: Designed for single-tenant operation without multi-tenancy support.
- **No Scalability**: No horizontal scaling or distributed processing capabilities.

## Crypto Discovery

The first phase of the Enterprise Cryptographic Discovery & Analysis Tool (ECDAT) relies on a custom static analysis engine built on JavaParser.

*   **Java Source Scanning**: Recursively scans `.java` files within a designated target directory. Malformed files log an error and are skipped without terminating the scan.
*   **JavaParser AST Analysis**: Uses Abstract Syntax Trees to identify cryptographic API usages (e.g., `MethodCallExpr` targeting `getInstance`). This provides higher accuracy than standard regex scanning and enables context extraction, such as mapping `initialize()` calls back to `KeyPairGenerator` instances to extract key sizes.
*   **Supported APIs**: Currently detects Java Cryptography Architecture (JCA) APIs including AES, RSA, ECDSA, ECDH, Hashing (SHA-256, SHA-1, SHA-512, MD5), and TLS.
*   **Evidence Collection**: Generates a `CryptoFinding` containing the detected algorithm, line number, relative file path, explicit code evidence, and confidence rating.
*   **Limitations**: 
    *   Static analysis may not resolve cryptographic algorithms whose values are generated dynamically at runtime. These are marked as `UNKNOWN` with a `LOW` confidence score.
    *   Library/dependency presence is not treated as proof of cryptographic usage. Detection is strictly bound to source code AST evaluation.

## Risk Engine

The Risk Engine provides explainable, rule-based risk assessment for cryptographic findings identified by the discovery engine.

### Risk Assessment Model

*   **Risk Score**: 0–100 prototype methodology (not an official NIST scoring formula)
*   **Risk Levels**: LOW (0–24), MEDIUM (25–49), HIGH (50–74), CRITICAL (75–100)
*   **Quantum Risk**: NONE, LOW, HIGH
*   **Risk Factors**: Individual contributions with name, score, and explanation
*   **Confidence Integration**: LOW confidence findings receive adjusted risk assessments
*   **Score Breakdown**: Explainable component-by-component score attribution with methodology note

### ECDAT Risk Score Methodology

The ECDAT Risk Score is a **heuristic assessment** (not NIST/CVSS) that combines multiple weighted factors to produce a 0–100 risk score. The scoring is deterministic and fully explainable.

#### Scoring Formula

```
Total Score = Σ (Algorithm Factors + Key Size Factors + Quantum Factors + Business Context Factors)
```

The score is capped at 100 and floored at 0.

#### Factor Categories and Weights

**Algorithm Strength Factors:**
- Public-key cryptography (RSA, ECDSA, ECDH): +5 points
- Symmetric cryptography (AES): +3 points
- Modern hash (SHA-256, SHA-512): +10 points
- Weak hash (SHA-1): +60 points
- Broken hash (MD5): +85 points
- Cryptographic concern (algorithm-specific base): +5 to +30 points

**Quantum Vulnerability Factors:**
- Quantum-vulnerable algorithms (RSA, ECDSA, ECDH): +20 points
- Quantum-resistant algorithms (AES, SHA-256, SHA-512): +5 points

**Key Size Factors:**
- RSA < 1024 bits: +40 points
- RSA 1024-2047 bits: +10 points
- RSA 2048 bits: 0 points
- RSA > 2048 bits: -5 points
- RSA unknown key size: +15 points
- AES-256: -5 points
- AES-128: +5 points
- AES unknown key size: +10 points

**Business Context Factors:**
- Data sensitivity (HIGHLY_SENSITIVE): +15 points
- Data sensitivity (CONFIDENTIAL): +10 points
- Data sensitivity (INTERNAL): +5 points
- Data sensitivity (PUBLIC): 0 points
- Business criticality (CRITICAL): +15 points
- Business criticality (HIGH): +10 points
- Business criticality (MEDIUM): +5 points
- Business criticality (LOW): 0 points

**Usage Context Factors:**
- Digital signature purpose: +2 points
- Key generation purpose: +2 points
- Key agreement purpose: +2 points

**Algorithm Status Factors:**
- Deprecated algorithm (SHA-1, MD5): +10 to +15 points

#### Example Calculation

**RSA-2048 with HIGH business context:**
```
PUBLIC_KEY_CRYPTOGRAPHY: +5
QUANTUM_VULNERABILITY: +20
CRYPTOGRAPHIC_CONCERN: +25
KEY_SIZE (2048 bits): 0
DATA_SENSITIVITY (HIGHLY_SENSITIVE): +15
BUSINESS_CRITICALITY (CRITICAL): +15
─────────────────────────────────
Total: 80 → Risk Level: HIGH
```

**MD5 with LOW business context:**
```
BROKEN_HASH: +85
DEPRECATED_ALGORITHM: +15
DATA_SENSITIVITY (PUBLIC): 0
BUSINESS_CRITICALITY (LOW): 0
─────────────────────────────────
Total: 100 → Risk Level: CRITICAL (capped)
```

**AES-256 with MEDIUM business context:**
```
SYMMETRIC_CRYPTOGRAPHY: +3
CRYPTOGRAPHIC_CONCERN: +5
KEY_SIZE (AES-256): -5
QUANTUM_RESISTANCE: +5
DATA_SENSITIVITY (INTERNAL): +5
BUSINESS_CRITICALITY (MEDIUM): +5
─────────────────────────────────
Total: 18 → Risk Level: LOW
```

#### Important Notes

*   **Heuristic Methodology**: This is an ECDAT prototype heuristic, not an official NIST or CVSS scoring formula.
*   **Deterministic**: Same input always produces the same score.
*   **Explainable**: Every score includes a full breakdown of contributing factors.
*   **Business Context**: When provided, business criticality and data sensitivity adjust the score by ±15 points.
*   **Score Capping**: Scores are capped at 100 (CRITICAL) and floored at 0 (LOW).

### Risk Scoring Rules

#### Public-Key Cryptography (RSA, ECDSA, ECDH)
*   **Quantum Risk**: HIGH - vulnerable to future cryptographically relevant quantum attacks
*   **Base Risk**: Elevated due to post-quantum migration requirements
*   **Key Size Impact**: Unknown or small key sizes increase risk significantly
*   **Purpose Awareness**: Different considerations for digital signatures vs key agreement

#### Symmetric Cryptography (AES)
*   **Quantum Risk**: LOW - more quantum-resistant than public-key cryptography
*   **Key Size Impact**: AES-256 considered very secure, AES-128 has moderate quantum concerns
*   **Base Risk**: Generally LOW when used with proper key sizes and modes

#### Hash Functions
*   **SHA-256/SHA-512**: LOW risk, modern hash functions
*   **SHA-1**: HIGH risk due to cryptographic weaknesses
*   **MD5**: CRITICAL risk due to collision vulnerabilities

#### TLS Protocols
*   **TLS 1.3**: LOW risk - most current and secure version
*   **TLS 1.2**: LOW to MEDIUM risk - depends on cipher suite configuration
*   **Assessment Limitations**: Current scanner does not analyze cipher suites, certificates, or key exchange methods

### Important Notes

*   **Prototype Methodology**: The risk score is an ECDAT prototype methodology and is not an official NIST scoring formula.
*   **Explainable Assessment**: Every risk assessment includes detailed factors and human-readable reasons.
*   **Research Basis**: Aligned with NIST's crypto-agility guidance and PQC migration project principles for cryptographic inventories and risk management.
*   **Consistency & Testability**: The scoring model is designed to be deterministic, explainable, and thoroughly tested.

### Risk Engine Implementation

The Risk Engine is implemented in `com.ecdat.backend.risk` package with the following components:

*   **RiskEngine.java**: Main assessment engine with algorithm-specific risk rules
*   **RiskAssessment.java**: Comprehensive risk result model
*   **RiskLevel.java**: Enum defining LOW/MEDIUM/HIGH/CRITICAL levels
*   **QuantumRisk.java**: Enum defining NONE/LOW/HIGH quantum risk levels
*   **RiskFactor.java**: Individual risk contribution with explanation

The Risk Engine is designed to be:
*   **Deterministic**: Same input always produces same output
*   **Explainable**: Every assessment includes detailed reasoning
*   **Unit-testable**: Pure Java logic independent of Spring framework
*   **Independent**: No database, ML, or external API dependencies

## PQC Recommendation Engine

The PQC Recommendation Engine provides explainable, purpose-aware post-quantum cryptography recommendations based on cryptographic findings and risk assessments.

### Recommendation Model

*   **Recommendation Status**: RECOMMENDED, CONDITIONAL, NEEDS_ANALYSIS, NOT_REQUIRED
*   **Migration Priority**: LOW, MEDIUM, HIGH, CRITICAL
*   **Algorithm Mapping**: Purpose-aware classical-to-PQC algorithm recommendations
*   **Confidence Preservation**: Low-confidence findings result in NEEDS_ANALYSIS status
*   **Considerations**: Detailed migration guidance for each recommendation

### Purpose-Aware Mappings

#### Digital Signatures (RSA, ECDSA)
*   **Primary Recommendation**: ML-DSA (FIPS 204) - NIST-standardized post-quantum digital signature
*   **Alternative**: SLH-DSA (FIPS 205) - Stateless hash-based digital signature
*   **Rationale**: Classical digital signatures are vulnerable to future cryptographically relevant quantum attacks and require migration planning
*   **Considerations**: Certificate ecosystem compatibility, library/provider support, signature size, protocol support, deployment compatibility

#### Key Agreement (ECDH)
*   **Primary Recommendation**: ML-KEM (FIPS 203) - NIST-standardized key-encapsulation mechanism
*   **Rationale**: ECDH is a classical public-key key-agreement mechanism with quantum-vulnerability concerns
*   **Considerations**: Protocol compatibility, key/ciphertext sizes, implementation support, hybrid migration options, interoperability

#### RSA Key Establishment/Encryption
*   **Recommendation**: NEEDS_ANALYSIS
*   **Rationale**: Further usage analysis required before selecting a PQC replacement. ML-KEM may be applicable depending on specific use case
*   **Considerations**: Purpose-specific compatibility analysis, key-establishment protocol compatibility

#### Symmetric Cryptography (AES)
*   **AES-256**: NOT_REQUIRED - No PQC replacement needed solely due to quantum risk when properly configured
*   **AES-128**: CONDITIONAL - Review long-term security requirements and consider migration toward stronger symmetric security
*   **Rationale**: AES is symmetric cryptography, more quantum-resistant than public-key cryptography

#### Hash Functions (SHA-1, MD5)
*   **Recommendation**: Replace with modern approved cryptographic hash (SHA-256, SHA-3)
*   **Rationale**: Primary problem is classical cryptographic weakness, not quantum migration issue
*   **Note**: These do NOT require PQC algorithm recommendations

#### TLS Protocols
*   **Recommendation**: NEEDS_ANALYSIS
*   **Rationale**: Further TLS cryptographic analysis required to identify cipher suite, key exchange mechanism, certificate algorithm, and signature algorithm
*   **Note**: Current scanner does not identify specific cryptographic mechanisms within TLS

### Uncertainty Handling

*   **Low Confidence**: Automatically results in NEEDS_ANALYSIS status
*   **Unknown Algorithm**: Results in NEEDS_ANALYSIS status with explanation
*   **Unknown Purpose**: Results in NEEDS_ANALYSIS status rather than guessing usage

### Hybrid Migration Considerations

The recommendation engine supports the concept of hybrid/dual-stack migration where appropriate:
*   Hybrid deployment may be considered where compatibility requires retaining classical mechanisms during transition
*   Does not invent protocol-specific hybrid implementations
*   Aligns with real migration practice and crypto-agility principles

### Important Notes

*   **Advisory Nature**: Recommendations are advisory and intended to support migration planning. They do not constitute an automatic cryptographic migration or a universal algorithm-selection policy.
*   **No Automatic Modification**: This phase does NOT automatically modify source code, configuration files, certificates, or dependencies.
*   **NIST Standards**: References NIST's finalized PQC standards: FIPS 203 (ML-KEM), FIPS 204 (ML-DSA), FIPS 205 (SLH-DSA)
*   **Terminology**: Uses correct cryptographic terminology:
    *   ML-KEM: Key-Encapsulation Mechanism / key establishment (not "post-quantum encryption algorithm")
    *   ML-DSA: Digital signature
    *   SLH-DSA: Stateless hash-based digital signature
*   **NIST Guidance**: NIST currently identifies these three standards as ready for implementation and recommends organizations begin migration to quantum-resistant cryptography. ECDAT does not claim NIST endorses its specific recommendation logic.



