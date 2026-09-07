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

## Current Implementation Status

### Completed (Foundation Phase)
- ✅ Backend Spring Boot application with proper package structure (`com.ecdat.backend`)
- ✅ Health endpoint (`GET /health`) returning `{"status":"UP"}`
- ✅ CORS configuration for local development
- ✅ Frontend React + TypeScript + Vite application
- ✅ Frontend page displaying ECDAT title and subtitle
- ✅ Frontend connectivity to backend health endpoint
- ✅ Backend tests (context loading test)
- ✅ Frontend build verification
- ✅ Git configuration (.gitignore)

### Not Yet Implemented
- ❌ Cryptographic discovery engine
- ❌ Risk analysis
- ❌ PQC recommendations
- ❌ CBOM generation
- ❌ Certificate analysis
- ❌ Dashboard functionality
- ❌ Authentication and authorization

## Testing

### Backend Tests
```bash
cd backend
mvn clean test
```

### Frontend Build
```bash
cd frontend
npm run build
```

## Project Structure
```
ECDAT/
├── backend/          # Spring Boot backend
├── frontend/        # React frontend
├── test-target/     # Target for cryptographic testing
├── docs/            # Documentation
└── README.md
```

## Crypto Discovery

The first phase of the Enterprise Cryptographic Discovery & Analysis Tool (ECDAT) relies on a custom static analysis engine built on JavaParser.

*   **Java Source Scanning**: Recursively scans `.java` files within a designated target directory. Malformed files log an error and are skipped without terminating the scan.
*   **JavaParser AST Analysis**: Uses Abstract Syntax Trees to identify cryptographic API usages (e.g., `MethodCallExpr` targeting `getInstance`). This provides higher accuracy than standard regex scanning and enables context extraction, such as mapping `initialize()` calls back to `KeyPairGenerator` instances to extract key sizes.
*   **Supported APIs**: Currently detects Java Cryptography Architecture (JCA) APIs including AES, RSA, ECDSA, ECDH, Hashing (SHA-256, SHA-1, SHA-512, MD5), and TLS.
*   **Evidence Collection**: Generates a `CryptoFinding` containing the detected algorithm, line number, relative file path, explicit code evidence, and confidence rating.
*   **Limitations**: 
    *   Static analysis may not resolve cryptographic algorithms whose values are generated dynamically at runtime. These are marked as `UNKNOWN` with a `LOW` confidence score.
    *   Library/dependency presence is not treated as proof of cryptographic usage. Detection is strictly bound to source code AST evaluation.
