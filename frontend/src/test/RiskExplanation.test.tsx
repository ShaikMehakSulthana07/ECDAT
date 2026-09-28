import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { RiskExplanation } from '../components/RiskExplanation';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

describe('RiskExplanation "Why is this risky?" Evidence Chain Component', () => {
  // Finding 1: RSA-2048 Digital Signature (Quantum Vulnerable, High Risk, Mosca Exposed)
  const rsaFinding: CryptoFinding = {
    algorithm: 'RSA',
    keySize: 2048,
    purpose: 'DIGITAL_SIGNATURE',
    file: 'PaymentService.java',
    line: 142,
    evidence: 'KeyPairGenerator.getInstance("RSA")',
    confidence: 'HIGH',
    sourceType: 'JAVA_AST',
  };

  const rsaRisk: RiskAssessment = {
    riskScore: 85,
    riskLevel: 'HIGH',
    confidence: 'HIGH',
    quantumRisk: 'HIGH',
    originalFinding: rsaFinding,
    reasons: [
      'RSA is vulnerable to Shor algorithm on quantum hardware.',
      'Key size of 2048 bits provides 112 bits of classical security, deprecated for long-term protection.',
    ],
    factors: [
      {
        name: 'PUBLIC_KEY_QUANTUM_VULNERABILITY',
        score: 50,
        explanation: 'Vulnerable to Shor algorithm quantum attacks.',
      },
      {
        name: 'RSA_KEY_SIZE_2048',
        score: 25,
        explanation: 'RSA 2048 provides acceptable classical security today but requires modernization.',
      },
    ],
    quantumRiskResult: {
      algorithm: 'RSA',
      quantumVulnerabilityStatus: 'VULNERABLE',
      quantumVulnerable: true,
      migrationRequired: true,
      migrationTimeYears: 3,
      dataLifetimeYears: 10,
      threatHorizonYears: 10,
      moscaConditionMet: true,
      totalExposureYears: 13,
      yearsUntilThreat: 10,
      businessCriticality: 'CRITICAL',
      dataSensitivity: 'HIGHLY_SENSITIVE',
      migrationUrgency: 'HIGH',
      explanation:
        'RSA relies on integer factorization and is vulnerable to a sufficiently capable quantum computer using Shor\'s algorithm.',
      calculationDetails:
        'Mosca Calculation: 3 (migration time) + 10 (data lifetime) = 13 (total exposure)\nThreat Horizon: 10 years\nCondition: 13 > 10 = true\nAlgorithm RSA quantum-vulnerable: true',
    },
  };

  const rsaPqc: PQCRecommendation = {
    recommendationStatus: 'RECOMMENDED',
    currentAlgorithm: 'RSA',
    currentPurpose: 'DIGITAL_SIGNATURE',
    recommendedAlgorithm: 'ML-DSA',
    alternativeAlgorithms: ['SLH-DSA'],
    rationale:
      'RSA digital signatures are vulnerable to future cryptographically relevant quantum attacks. ML-DSA is a NIST-standardized post-quantum digital-signature scheme suitable for migration planning.',
    migrationPriority: 'HIGH',
    migrationStrategy: 'HYBRID',
    quantumRisk: 'HIGH',
    confidence: 'HIGH',
    considerations: [
      'Certificate ecosystem compatibility requires verification',
      'Signature size differences compared to RSA',
    ],
  };

  // Finding 2: AES-256 Encryption (Quantum Safe / Resistant, Low Risk)
  const aesFinding: CryptoFinding = {
    algorithm: 'AES',
    keySize: 256,
    purpose: 'ENCRYPTION',
    mode: 'GCM',
    padding: 'NoPadding',
    file: 'StorageEncryptionService.java',
    line: 88,
    evidence: 'Cipher.getInstance("AES/GCM/NoPadding")',
    confidence: 'HIGH',
    sourceType: 'JAVA_AST',
  };

  const aesRisk: RiskAssessment = {
    riskScore: 10,
    riskLevel: 'LOW',
    confidence: 'HIGH',
    quantumRisk: 'LOW',
    originalFinding: aesFinding,
    reasons: [
      'AES-256 provides strong classical and post-quantum resistance against Grover algorithm.',
    ],
    factors: [
      {
        name: 'AES_GCM_AEAD',
        score: 0,
        explanation: 'Authenticated encryption with associated data provides authenticated confidentiality.',
      },
    ],
    quantumRiskResult: {
      algorithm: 'AES',
      quantumVulnerabilityStatus: 'NOT_QUANTUM_VULNERABLE',
      quantumVulnerable: false,
      migrationRequired: false,
      migrationTimeYears: 2,
      dataLifetimeYears: 5,
      threatHorizonYears: 10,
      moscaConditionMet: false,
      totalExposureYears: 7,
      yearsUntilThreat: 10,
      businessCriticality: 'MEDIUM',
      dataSensitivity: 'CONFIDENTIAL',
      migrationUrgency: 'NONE',
      explanation:
        'Result: This algorithm is not considered quantum-vulnerable.\nReason: AES is a symmetric algorithm, which has Grover resistance at 256-bit key sizes.',
      calculationDetails:
        'Mosca Calculation: 2 + 5 = 7 <= 10. Condition: false',
    },
  };

  const aesPqc: PQCRecommendation = {
    recommendationStatus: 'NOT_REQUIRED',
    currentAlgorithm: 'AES',
    currentPurpose: 'ENCRYPTION',
    recommendedAlgorithm: null,
    alternativeAlgorithms: [],
    rationale: 'AES-256 with GCM authenticated encryption is already post-quantum resistant.',
    migrationPriority: 'LOW',
    migrationStrategy: 'NO_ACTION',
    quantumRisk: 'LOW',
    confidence: 'HIGH',
    considerations: ['Maintain proper IV/nonce uniqueness in GCM mode.'],
  };

  // Finding 3: Custom or Unrecognized Primitive (UNKNOWN handling requirement)
  const unknownFinding: CryptoFinding = {
    algorithm: 'UNKNOWN_CIPHER',
    purpose: 'UNKNOWN',
    file: 'LegacyModule.java',
    line: 23,
    evidence: 'CryptoUtil.process("UNKNOWN_CIPHER")',
    confidence: 'LOW',
    sourceType: 'JAVA_AST',
  };

  const unknownRisk: RiskAssessment = {
    riskScore: 50,
    riskLevel: 'MEDIUM',
    confidence: 'LOW',
    quantumRisk: 'NONE',
    originalFinding: unknownFinding,
    reasons: ['Unrecognized cryptographic algorithm requires cryptographic audit.'],
    factors: [],
    quantumRiskResult: {
      algorithm: 'UNKNOWN_CIPHER',
      quantumVulnerabilityStatus: 'UNKNOWN',
      quantumVulnerable: false,
      migrationRequired: false,
      migrationTimeYears: 3,
      dataLifetimeYears: 5,
      threatHorizonYears: 10,
      moscaConditionMet: false,
      totalExposureYears: 8,
      yearsUntilThreat: 10,
      businessCriticality: 'UNKNOWN',
      dataSensitivity: 'UNKNOWN',
      migrationUrgency: 'UNKNOWN',
      explanation:
        'Algorithm: UNKNOWN_CIPHER\nQuantum Vulnerability Status: UNKNOWN\nResult: Quantum vulnerability cannot be determined.\nReason: ECDAT does not have a classification rule for this algorithm.',
      calculationDetails: '',
    },
  };

  const unknownPqc: PQCRecommendation = {
    recommendationStatus: 'NEEDS_ANALYSIS',
    currentAlgorithm: 'UNKNOWN_CIPHER',
    currentPurpose: 'UNKNOWN',
    recommendedAlgorithm: null,
    alternativeAlgorithms: [],
    rationale: 'Algorithm could not be identified with certainty. Manual review is required.',
    migrationPriority: 'MEDIUM',
    migrationStrategy: 'NEEDS_ANALYSIS',
    quantumRisk: 'NONE',
    confidence: 'LOW',
    considerations: ['Conduct static and dynamic binary auditing.'],
  };

  it('renders all 8 evidence chain steps in order for RSA-2048 with executive hero summary', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    // 10-Second Executive Summary Hero
    expect(screen.getByText(/10-SECOND VERDICT & REASONING PATH/i)).toBeInTheDocument();
    expect(screen.getByText('QUANTUM VULNERABLE')).toBeInTheDocument();
    expect(screen.getByText(/HIGH RISK/i)).toBeInTheDocument();
    expect(screen.getAllByText('ML-DSA').length).toBeGreaterThanOrEqual(1);

    // Verify 8 explicit evidence chain steps exist
    expect(screen.getByTestId('step-cryptographic-artifact')).toBeInTheDocument();
    expect(screen.getByTestId('step-source-evidence')).toBeInTheDocument();
    expect(screen.getByTestId('step-classical-security')).toBeInTheDocument();
    expect(screen.getByTestId('step-quantum-assessment')).toBeInTheDocument();
    expect(screen.getByTestId('step-mosca-timeline')).toBeInTheDocument();
    expect(screen.getByTestId('step-risk-classification')).toBeInTheDocument();
    expect(screen.getByTestId('step-migration-priority')).toBeInTheDocument();
    expect(screen.getByTestId('step-pqc-recommendation')).toBeInTheDocument();
  });

  it('step 1: renders observed Cryptographic Artifact details without hallucination', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-cryptographic-artifact');
    expect(step).toHaveTextContent('RSA-2048');
    expect(step).toHaveTextContent('DIGITAL SIGNATURE');
    expect(step).toHaveTextContent('2048 bits');
    expect(step).toHaveTextContent('OBSERVED EVIDENCE');
  });

  it('step 2: renders observed Source Evidence with exact file, line, and code snippet', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-source-evidence');
    expect(step).toHaveTextContent('PaymentService.java:142');
    expect(step).toHaveTextContent('KeyPairGenerator.getInstance("RSA")');
    expect(step).toHaveTextContent('HIGH');
    expect(step).toHaveTextContent('OBSERVED EVIDENCE');
  });

  it('step 3: renders Classical Security Assessment with risk score and factors', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-classical-security');
    expect(step).toHaveTextContent('85 / 100');
    expect(step).toHaveTextContent('HIGH');
    expect(step).toHaveTextContent('PUBLIC_KEY_QUANTUM_VULNERABILITY');
    expect(step).toHaveTextContent('ANALYTICAL CONCLUSION');
  });

  it('step 4: renders Quantum Assessment with exact backend explanation and Shor algorithm reasoning', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-quantum-assessment');
    expect(step).toHaveTextContent('VULNERABLE');
    expect(step).toHaveTextContent(/relies on integer factorization and is vulnerable to a sufficiently capable quantum computer using Shor's algorithm/i);
    expect(step).toHaveTextContent('ANALYTICAL CONCLUSION');
  });

  it('step 5: renders Mosca-style Timeline with X, Y, Z equation and condition evaluation', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-mosca-timeline');
    expect(step).toHaveTextContent('3 yrs');
    expect(step).toHaveTextContent('10 yrs');
    expect(step).toHaveTextContent('10 yrs');
    expect(step).toHaveTextContent(/EXPOSED: Total Exposure \(X \+ Y\) > Threat Horizon \(Z\)/i);
    expect(step).toHaveTextContent('13 years');

    // Toggle calculation details button
    const toggleBtn = screen.getByRole('button', { name: /View Calculation Details/i });
    fireEvent.click(toggleBtn);
    expect(screen.getByText(/Mosca Calculation: 3 \(migration time\) \+ 10 \(data lifetime\) = 13/i)).toBeInTheDocument();
  });

  it('step 6: renders Risk Classification with risk reasons list', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-risk-classification');
    expect(step).toHaveTextContent('HIGH');
    expect(step).toHaveTextContent('Score: 85/100');
    expect(step).toHaveTextContent('RSA is vulnerable to Shor algorithm on quantum hardware.');
  });

  it('step 7: renders Migration Priority with urgency note', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-migration-priority');
    expect(step).toHaveTextContent('HIGH PRIORITY');
    expect(step).toHaveTextContent(/High-priority modernization candidate/i);
  });

  it('step 8: renders PQC / Hybrid Recommendation with target algorithm, strategy, and considerations', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const step = screen.getByTestId('step-pqc-recommendation');
    expect(step).toHaveTextContent('RECOMMENDED');
    expect(step).toHaveTextContent('HYBRID');
    expect(step).toHaveTextContent('ML-DSA');
    expect(step).toHaveTextContent('SLH-DSA');
    expect(step).toHaveTextContent(/NIST-standardized post-quantum digital-signature scheme/i);

    // Expand considerations
    const considerBtn = screen.getByRole('button', { name: /View Implementation Considerations/i });
    fireEvent.click(considerBtn);
    expect(screen.getByText('Certificate ecosystem compatibility requires verification')).toBeInTheDocument();
  });

  it('handles quantum resistant findings (AES-256) appropriately without false alarms', () => {
    render(
      <RiskExplanation
        finding={aesFinding}
        riskAssessment={aesRisk}
        pqcRecommendation={aesPqc}
      />
    );

    const hero = screen.getByText('QUANTUM SAFE / RESISTANT');
    expect(hero).toBeInTheDocument();

    const quantumStep = screen.getByTestId('step-quantum-assessment');
    expect(quantumStep).toHaveTextContent('NOT QUANTUM VULNERABLE');
    expect(quantumStep).toHaveTextContent(/symmetric algorithm, which has Grover resistance/i);

    const moscaStep = screen.getByTestId('step-mosca-timeline');
    expect(moscaStep).toHaveTextContent(/NOT EXPOSED/i);

    const pqcStep = screen.getByTestId('step-pqc-recommendation');
    expect(pqcStep).toHaveTextContent('NOT_REQUIRED');
    expect(pqcStep).toHaveTextContent('No PQC replacement required for this primitive');
  });

  it('strictly displays UNKNOWN instead of assuming safe when classification is unknown', () => {
    render(
      <RiskExplanation
        finding={unknownFinding}
        riskAssessment={unknownRisk}
        pqcRecommendation={unknownPqc}
      />
    );

    // Hero must show UNKNOWN, not safe
    expect(screen.getByText('QUANTUM UNKNOWN')).toBeInTheDocument();

    const quantumStep = screen.getByTestId('step-quantum-assessment');
    expect(quantumStep).toHaveTextContent('UNKNOWN');
    expect(quantumStep).toHaveTextContent(/Quantum vulnerability cannot be determined/i);
    expect(quantumStep).not.toHaveTextContent('QUANTUM SAFE');

    const pqcStep = screen.getByTestId('step-pqc-recommendation');
    expect(pqcStep).toHaveTextContent('NEEDS ANALYSIS / UNKNOWN');
  });

  it('clearly distinguishes observed evidence tags from analytical conclusion tags', () => {
    render(
      <RiskExplanation
        finding={rsaFinding}
        riskAssessment={rsaRisk}
        pqcRecommendation={rsaPqc}
      />
    );

    const observedTags = screen.getAllByText('OBSERVED EVIDENCE');
    // 1 in header legend + Step 1 + Step 2 = 3
    expect(observedTags.length).toBeGreaterThanOrEqual(2);

    const analyticalTags = screen.getAllByText('ANALYTICAL CONCLUSION');
    // 1 in header legend + Step 3, 4, 5, 6, 7, 8 = 7
    expect(analyticalTags.length).toBeGreaterThanOrEqual(6);
  });
});
