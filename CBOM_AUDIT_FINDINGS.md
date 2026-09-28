# ECDAT CBOM Audit Findings

## Executive Summary

ECDAT's CBOM implementation is **NOT schema-compliant** with CycloneDX 1.6. The implementation uses a custom structure inspired by CycloneDX but does not follow the official CBOM schema specification.

## Schema/Version Used

**Claimed**: CycloneDX 1.6
**Actual**: Custom CycloneDX-inspired structure (not schema-compliant)

## Validation Result

❌ **INVALID** - The generated JSON does not conform to the CycloneDX 1.6 CBOM schema.

## Compatibility Gaps

### 1. Component Type Violation

**CycloneDX 1.6 Spec**: Component type must use the `CLASSIFICATION_CRYPTOGRAPHIC_ASSET` enum (value 13)

**ECDAT Implementation**: Uses string `"cryptographic-asset"` as the type

**Impact**: Schema violation - type field is not a valid CycloneDX component classification

### 2. cryptoProperties Structure Violation

**CycloneDX 1.6 Spec**: cryptoProperties must have:
- `assetType` (required): One of "algorithm", "certificate", "protocol", "related-crypto-material"
- `algorithmProperties` (optional wrapper for algorithm-specific fields)
- `certificateProperties` (optional wrapper for certificate-specific fields)
- `relatedCryptoMaterialProperties` (optional wrapper for related material)

**ECDAT Implementation**: Flat custom structure with direct fields:
- `algorithm` (custom)
- `algorithmVariant` (custom)
- `mode` (custom - not using enum values)
- `padding` (custom - not using enum values)
- `purpose` (custom)
- `keySize` (custom)
- `protocol` (custom)
- `library` (custom)
- `sourceFile` (custom)
- `sourceLine` (custom)
- `evidence` (custom)
- `confidence` (custom)
- `sourceType` (custom)
- `risk` (custom nested object)
- `pqcRecommendation` (custom nested object)
- `assetCategory` (custom)
- `usageCategory` (custom)
- `lifecycleStatus` (custom)
- `businessCriticality` (custom)
- `dataSensitivity` (custom)

**Impact**: Major schema violation - entire cryptoProperties structure is non-standard

### 3. Missing Standard Fields

ECDAT does not implement these standard CycloneDX 1.6 CBOM fields:
- `primitive` (cryptographic primitive type: drbg, mac, block-cipher, stream-cipher, signature, hash, pke, xof, kdf, key-agree, kem, ae, combiner, other, unknown)
- `parameterSetIdentifier` (e.g., "128" for AES-128)
- `curve` (elliptic curve name)
- `executionEnvironment` (software-plain-ram, software-encrypted-ram, software-tee, hardware, other, unknown)
- `implementationPlatform` (generic, x86_64, armv8-a, etc.)
- `certificationLevel` (FIPS 140-3, CC-EAL, etc.)
- `cryptoFunctions` (array of functions: encrypt, decrypt, sign, verify, etc.)
- `classicalSecurityLevel` (bits)
- `nistQuantumSecurityLevel` (0-6)

### 4. Enum Value Violations

**mode field**:
- CycloneDX requires: cbc, ecb, ccm, gcm, cfb, ofb, ctr, other, unknown
- ECDAT uses: "GCM" (correct) but may use other values not in enum

**padding field**:
- CycloneDX requires: pkcs5, pkcs7, pkcs1v15, oaep, raw, other, unknown
- ECDAT uses: "NoPadding" (NOT in enum - should be "raw")

### 5. Extension Mechanism Not Used

ECDAT-specific fields (risk assessment, PQC recommendations, source location, etc.) should be placed in the standard `properties` array with namespaced keys (e.g., `ecdat:riskLevel`), but are instead embedded directly in the custom cryptoProperties object.

## ECDAT-Specific Fields (Extensions)

The following fields are ECDAT extensions and should be moved to the `properties` array:
- `risk` (entire CBOMRiskInfo object)
- `pqcRecommendation` (entire CBOMPQCInfo object)
- `protocol`
- `library`
- `sourceFile`
- `sourceLine`
- `evidence`
- `confidence`
- `sourceType`
- `assetCategory`
- `usageCategory`
- `lifecycleStatus`
- `businessCriticality`
- `dataSensitivity`

## Preserved Data (Must Keep)

The following data must be preserved in the corrected implementation:
- Algorithm name
- Variant/mode
- Purpose
- Key size
- Source location (file, line)
- Risk information (score, level, quantum risk, Mosca calculations)
- Quantum status (vulnerability status, migration urgency)
- PQC recommendation (algorithm, priority, rationale)
- Migration priority
- Evidence and confidence

## Deterministic Identifiers

**Current Status**: ✅ GOOD
- Component names are deterministic based on algorithm, variant, purpose, file, and line
- Serial number is randomly generated per document (correct per CycloneDX spec)

## Unknown Quantum Status Handling

**Current Status**: ✅ GOOD
- Uses `QuantumVulnerabilityStatus.UNKNOWN` enum value
- Safely handles unknown algorithms with LOW confidence
- Test coverage exists (testQuantumVulnerabilityStatusUnknownSerialization)

## Example Valid CBOM (What It Should Look Like)

```json
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.6",
  "serialNumber": "urn:uuid:...",
  "version": 1,
  "components": [{
    "type": "cryptographic-asset",
    "name": "AES-encryption-src-main-java-com-example-CryptoUtil.java-L42",
    "description": "Cryptographic asset: AES used for encryption found in src/main/java/com/example/CryptoUtil.java at line 42",
    "cryptoProperties": {
      "assetType": "algorithm",
      "algorithmProperties": {
        "primitive": "block-cipher",
        "parameterSetIdentifier": "256",
        "mode": "gcm",
        "padding": "raw",
        "cryptoFunctions": ["encrypt", "decrypt"],
        "classicalSecurityLevel": 256
      }
    },
    "properties": [
      {"name": "ecdat:algorithm", "value": "AES"},
      {"name": "ecdat:purpose", "value": "ENCRYPTION"},
      {"name": "ecdat:sourceFile", "value": "src/main/java/com/example/CryptoUtil.java"},
      {"name": "ecdat:sourceLine", "value": "42"},
      {"name": "ecdat:evidence", "value": "Cipher.getInstance(\"AES/GCM/NoPadding\")"},
      {"name": "ecdat:confidence", "value": "HIGH"},
      {"name": "ecdat:riskLevel", "value": "LOW"},
      {"name": "ecdat:riskScore", "value": "18"},
      {"name": "ecdat:quantumRisk", "value": "LOW"},
      {"name": "ecdat:risk_assessment_version", "value": "prototype"}
    ]
  }],
  "metadata": {
    "tool": {
      "vendor": "ECDAT",
      "name": "Enterprise Cryptographic Discovery & Analysis Tool",
      "version": "0.0.1-SNAPSHOT"
    }
  }
}
```

## Current ECDAT CBOM (What It Actually Looks Like)

```json
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.6",
  "serialNumber": "urn:uuid:184ad435-dc87-479f-9be9-ce253f685fd7",
  "version": 1,
  "components": [{
    "type": "cryptographic-asset",
    "name": "AES-encryption-src-main-java-com-example-CryptoUtil.java-L42",
    "description": "Cryptographic asset: AES used for encryption found in src/main/java/com/example/CryptoUtil.java at line 42",
    "cryptoProperties": {
      "algorithm": "AES",
      "mode": "GCM",
      "padding": "NoPadding",
      "purpose": "ENCRYPTION",
      "keySize": 256,
      "sourceFile": "src/main/java/com/example/CryptoUtil.java",
      "sourceLine": 42,
      "evidence": "Cipher.getInstance(\"AES/GCM/NoPadding\")",
      "confidence": "HIGH",
      "sourceType": "JAVA_AST",
      "risk": {
        "riskLevel": "LOW",
        "riskScore": 18,
        "quantumRisk": "LOW",
        "riskFactors": ["Test risk factor"]
      }
    },
    "properties": [
      {"name": "ecdat:risk_assessment_version", "value": "prototype"},
      {"name": "ecdat:confidence_preserved", "value": "true"},
      {"name": "ecdat:mode", "value": "GCM"},
      {"name": "ecdat:padding", "value": "NoPadding"}
    ]
  }],
  "metadata": {
    "tool": {
      "vendor": "ECDAT",
      "name": "Enterprise Cryptographic Discovery & Analysis Tool",
      "version": "0.0.1-SNAPSHOT"
    }
  },
  "note": "ECDAT CBOM - CycloneDX-inspired structure for cryptographic asset inventory."
}
```

## Recommendations

### Option 1: Full Schema Compliance (Recommended for Production)
- Refactor CBOMCryptoProperties to match CycloneDX 1.6 structure
- Move all ECDAT-specific fields to `properties` array with `ecdat:` prefix
- Map ECDAT fields to standard fields where possible:
  - `algorithm` → `algorithmProperties.primitive` (with enum mapping)
  - `keySize` → `algorithmProperties.parameterSetIdentifier`
  - `mode` → `algorithmProperties.mode` (ensure enum compliance)
  - `padding` → `algorithmProperties.padding` (ensure enum compliance)
- Add missing standard fields where data is available
- Update component type to use proper classification

### Option 2: CycloneDX-Aligned Format (Current Approach with Documentation)
- Keep current custom structure
- Update all documentation to clearly state "CycloneDX-aligned" or "CycloneDX-inspired"
- Remove any claims of schema compliance
- Document exact deviations from the standard
- Note that this is a proprietary format for ECDAT-specific use cases

## Files Requiring Changes

1. `backend/src/main/java/com/ecdat/backend/cbom/CBOMDocument.java` - Remove note field, update component type
2. `backend/src/main/java/com/ecdat/backend/cbom/CBOMComponent.java` - Update type field
3. `backend/src/main/java/com/ecdat/backend/cbom/CBOMCryptoProperties.java` - Major refactor to match spec
4. `backend/src/main/java/com/ecdat/backend/cbom/CBOMGenerator.java` - Update generation logic
5. `frontend/src/components/CBOMViewer.tsx` - Update terminology
6. `README.md` - Update CBOM claims and compliance status
7. Add schema validation tests

## Test Coverage Status

✅ Existing tests cover:
- Serialization/deserialization
- Field preservation
- Deterministic naming
- Unknown quantum status handling
- Risk and PQC integration

❌ Missing tests:
- Schema validation against official CycloneDX 1.6 JSON schema
- Enum value validation (mode, padding, primitive, etc.)
- Component type validation
- cryptoProperties structure validation
