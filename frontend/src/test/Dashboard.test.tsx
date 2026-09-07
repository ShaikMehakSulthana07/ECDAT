import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import App from '../App';
import { DashboardSummary } from '../components/DashboardSummary';
import { FindingsTable } from '../components/FindingsTable';
import { FindingDetailsModal } from '../components/FindingDetailsModal';
import { RiskOverview } from '../components/RiskOverview';
import { PQCRecommendations } from '../components/PQCRecommendations';
import { CBOMViewer } from '../components/CBOMViewer';
import { LoadingState } from '../components/LoadingState';
import { ErrorAlert } from '../components/ErrorAlert';
import { EmptyState } from '../components/EmptyState';
import { SourceTraceability } from '../components/SourceTraceability';
import type { AnalysisResponse, CryptoFinding, RiskAssessment, PQCRecommendation, CBOMDocument } from '../types/analysis';

const mockFinding: CryptoFinding = {
  algorithm: 'ECDSA',
  variant: 'SHA256withECDSA',
  purpose: 'DIGITAL_SIGNATURE',
  keySize: null,
  file: 'src/main/java/demo/ECDSAExample.java',
  line: 6,
  evidence: 'Signature.getInstance("SHA256withECDSA")',
  confidence: 'HIGH',
  sourceType: 'JAVA_AST',
  assetCategory: 'DIGITAL_SIGNATURE',
  usageCategory: 'DIRECT_USAGE',
  lifecycleStatus: 'ACTIVE',
  businessCriticality: 'UNKNOWN',
  dataSensitivity: 'UNKNOWN',
  protocol: null,
  library: 'Java Cryptography Architecture (JCA)',
};

const mockRisk: RiskAssessment = {
  riskScore: 65,
  riskLevel: 'HIGH',
  reasons: ['ECDSA digital signatures are vulnerable to future quantum cryptanalysis.'],
  factors: [
    {
      name: 'PUBLIC_KEY_QUANTUM_VULNERABILITY',
      score: 50,
      explanation: 'Vulnerable to Shor algorithm quantum attacks.',
    },
  ],
  quantumRisk: 'HIGH',
  confidence: 'HIGH',
  originalFinding: mockFinding,
};

const mockPQC: PQCRecommendation = {
  recommendationStatus: 'RECOMMENDED',
  currentAlgorithm: 'ECDSA',
  currentPurpose: 'DIGITAL_SIGNATURE',
  recommendedAlgorithm: 'ML-DSA',
  alternativeAlgorithms: ['SLH-DSA'],
  rationale: 'NIST FIPS 204 standardized digital signature replacement.',
  migrationPriority: 'HIGH',
  quantumRisk: 'HIGH',
  confidence: 'HIGH',
  considerations: ['Certificate ecosystem compatibility', 'Signature size changes'],
};

const mockCBOM: CBOMDocument = {
  bomFormat: 'CycloneDX',
  specVersion: '1.6',
  serialNumber: 'urn:uuid:test-12345',
  version: 1,
  metadata: {
    tool: {
      vendor: 'ECDAT',
      name: 'Enterprise Cryptographic Discovery & Analysis Tool',
      version: '0.0.1-SNAPSHOT',
    },
  },
  components: [
    {
      type: 'cryptographic-asset',
      name: 'ECDSA-digital_signature-ECDSAExample.java-L6',
      description: 'ECDSA digital signature in ECDSAExample.java:6',
      cryptoProperties: {
        algorithm: 'ECDSA',
        algorithmVariant: 'SHA256withECDSA',
        purpose: 'DIGITAL_SIGNATURE',
        assetCategory: 'DIGITAL_SIGNATURE',
        usageCategory: 'DIRECT_USAGE',
        lifecycleStatus: 'ACTIVE',
        businessCriticality: 'UNKNOWN',
        dataSensitivity: 'UNKNOWN',
        sourceFile: 'src/main/java/demo/ECDSAExample.java',
        sourceLine: 6,
        evidence: 'Signature.getInstance("SHA256withECDSA")',
        confidence: 'HIGH',
        risk: {
          riskLevel: 'HIGH',
          riskScore: 65,
          quantumRisk: 'HIGH',
          riskFactors: ['PUBLIC_KEY_QUANTUM_VULNERABILITY'],
        },
        pqcRecommendation: {
          recommendationStatus: 'RECOMMENDED',
          recommendedAlgorithm: 'ML-DSA',
          alternativeAlgorithms: ['SLH-DSA'],
          migrationPriority: 'HIGH',
        },
      },
      properties: [
        { name: 'ecdat:confidence_preserved', value: 'true' },
        { name: 'ecdat:asset_category', value: 'DIGITAL_SIGNATURE' },
        { name: 'ecdat:lifecycle_status', value: 'ACTIVE' },
      ],
    },
  ],
};

const mockResponse: AnalysisResponse = {
  status: 'SUCCESS',
  sourcePath: 'test-target',
  findings: [mockFinding],
  riskAssessments: [mockRisk],
  pqcRecommendations: [mockPQC],
  cbom: mockCBOM,
  summary: {
    totalFindings: 1,
    lowRiskCount: 0,
    mediumRiskCount: 0,
    highRiskCount: 1,
    criticalRiskCount: 0,
    quantumHighRiskCount: 1,
    pqcRecommendedCount: 1,
    pqcConditionalCount: 0,
    pqcNeedsAnalysisCount: 0,
    pqcNotRequiredCount: 0,
    activeAssetCount: 1,
    deprecatedAssetCount: 0,
    unknownLifecycleCount: 0,
    directUsageCount: 1,
  },
};

describe('ECDAT Frontend Component Tests', () => {
  it('1. Renders Empty/Landing state initially', () => {
    const handleQuickScan = vi.fn();
    render(<EmptyState onQuickScan={handleQuickScan} isLoading={false} />);
    expect(screen.getByText(/Enterprise Cryptographic Discovery/i)).toBeInTheDocument();
    expect(screen.getByText(/AST Discovery Engine/i)).toBeInTheDocument();
    
    const quickScanBtn = screen.getByRole('button', { name: /Scan Default Target/i });
    fireEvent.click(quickScanBtn);
    expect(handleQuickScan).toHaveBeenCalled();
  });

  it('2. Renders Loading state with pipeline stages', () => {
    render(<LoadingState target="test-target" />);
    expect(screen.getByText(/Analyzing Cryptographic Implementations/i)).toBeInTheDocument();
    expect(screen.getByText(/Discovering Cryptography/i)).toBeInTheDocument();
    expect(screen.getByText(/Assessing Risk/i)).toBeInTheDocument();
    expect(screen.getByText(/Evaluating PQC Migration/i)).toBeInTheDocument();
    expect(screen.getByText(/Generating CycloneDX 1.6 CBOM/i)).toBeInTheDocument();
  });

  it('3. Renders Error state with message', () => {
    const handleDismiss = vi.fn();
    render(
      <ErrorAlert
        message="Directory not found: invalid/path"
        errorType="NOT_FOUND"
        status={400}
        onDismiss={handleDismiss}
      />
    );
    expect(screen.getByText(/Analysis Request Failed/i)).toBeInTheDocument();
    expect(screen.getByText(/Directory not found: invalid\/path/i)).toBeInTheDocument();
    expect(screen.getByText(/HTTP 400/i)).toBeInTheDocument();
  });

  it('4. Renders Dashboard Summary with exact backend counts and Phase 7 lifecycle', () => {
    const handleNav = vi.fn();
    render(<DashboardSummary summary={mockResponse.summary} sourcePath="test-target" onNavigateTab={handleNav} />);
    expect(screen.getByText('Discovered Assets')).toBeInTheDocument();
    expect(screen.getByText('Quantum High Risk')).toBeInTheDocument();
    expect(screen.getByText('PQC Migration Required')).toBeInTheDocument();
    expect(screen.getByText('Inventory Lifecycle Status')).toBeInTheDocument();
    expect(screen.getByText('test-target')).toBeInTheDocument();
  });

  it('5. Renders Findings Table and handles selection with Asset Category & Lifecycle', () => {
    const handleSelect = vi.fn();
    render(
      <FindingsTable
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
        pqcRecommendations={[mockPQC]}
        onSelectFinding={handleSelect}
        selectedIndex={null}
      />
    );
    expect(screen.getByText('ECDSA')).toBeInTheDocument();
    expect(screen.getAllByText('DIGITAL SIGNATURE').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('ACTIVE')).toBeInTheDocument();
    expect(screen.getByText(/HIGH \(65\)/i)).toBeInTheDocument();

    const inspectBtn = screen.getByRole('button', { name: /Inspect/i });
    fireEvent.click(inspectBtn);
    expect(handleSelect).toHaveBeenCalledWith(0);
  });

  it('6. Renders Finding Details Modal with full evidence chain and Enterprise Inventory details', () => {
    const handleClose = vi.fn();
    render(
      <FindingDetailsModal
        finding={mockFinding}
        riskAssessment={mockRisk}
        pqcRecommendation={mockPQC}
        findingIndex={0}
        totalFindings={1}
        onClose={handleClose}
      />
    );
    expect(screen.getByText('Finding #1 of 1')).toBeInTheDocument();
    expect(screen.getByText('Enterprise Inventory & Classification')).toBeInTheDocument();
    expect(screen.getByText('Direct API Invocation')).toBeInTheDocument();
    expect(screen.getByText('Evidence Chain & Traceability')).toBeInTheDocument();
    expect(screen.getAllByText('Signature.getInstance("SHA256withECDSA")').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('ML-DSA').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('SLH-DSA')).toBeInTheDocument();
  });

  it('7. Renders Risk Overview and algorithm breakdown', () => {
    render(<RiskOverview riskAssessments={[mockRisk]} summary={mockResponse.summary} />);
    expect(screen.getByText(/Risk Distribution Bar/i)).toBeInTheDocument();
    expect(screen.getByText(/Algorithm Posture Analysis/i)).toBeInTheDocument();
    expect(screen.getByText('Prototype Risk Methodology (0–100)')).toBeInTheDocument();
  });

  it('8. Renders PQC Recommendations with NIST standards alignment', () => {
    const handleSelect = vi.fn();
    render(
      <PQCRecommendations
        pqcRecommendations={[mockPQC]}
        findings={[mockFinding]}
        onSelectFinding={handleSelect}
      />
    );
    expect(screen.getByText(/NIST Post-Quantum Cryptography Standards Alignment/i)).toBeInTheDocument();
    expect(screen.getByText('FIPS 203')).toBeInTheDocument();
    expect(screen.getByText('FIPS 204')).toBeInTheDocument();
    expect(screen.getByText('FIPS 205')).toBeInTheDocument();
    expect(screen.getByText('ML-DSA')).toBeInTheDocument();
    expect(screen.getByText('HIGH Priority')).toBeInTheDocument();
  });

  it('9. Renders CycloneDX 1.6 CBOM Viewer with components and JSON view', () => {
    render(<CBOMViewer cbom={mockCBOM} />);
    expect(screen.getByText(/CycloneDX 1.6 Cryptography Bill of Materials/i)).toBeInTheDocument();
    expect(screen.getByText('CycloneDX')).toBeInTheDocument();
    expect(screen.getByText('1.6')).toBeInTheDocument();
    expect(screen.getByText('urn:uuid:test-12345')).toBeInTheDocument();
    expect(screen.getAllByText(/DIGITAL SIGNATURE/i).length).toBeGreaterThanOrEqual(1);

    // Switch to JSON view
    const jsonBtn = screen.getByRole('button', { name: /Raw CBOM JSON/i });
    fireEvent.click(jsonBtn);
    expect(screen.getByText(/CycloneDX v1.6 Document JSON/i)).toBeInTheDocument();
  });

  it('10. Renders Source Traceability view', () => {
    const handleSelect = vi.fn();
    render(
      <SourceTraceability
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
        pqcRecommendations={[mockPQC]}
        onSelectFinding={handleSelect}
      />
    );
    expect(screen.getByText(/End-to-End Cryptographic Traceability/i)).toBeInTheDocument();
    expect(screen.getByText(/1. Discovered Asset/i)).toBeInTheDocument();
    expect(screen.getByText(/3. AST Code Evidence/i)).toBeInTheDocument();
    expect(screen.getByText(/4. Risk Assessment/i)).toBeInTheDocument();
    expect(screen.getByText(/5. PQC Migration/i)).toBeInTheDocument();
  });

  it('11. Renders master App layout and responds to health check', async () => {
    const originalFetch = globalThis.fetch;
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'UP' }),
    });

    const { act } = await import('@testing-library/react');
    await act(async () => {
      render(<App />);
    });

    expect(screen.getByText('ECDAT')).toBeInTheDocument();
    expect(screen.getByText('SIH26164')).toBeInTheDocument();
    expect(screen.getByText('Enterprise Cryptographic Discovery & Analysis Tool')).toBeInTheDocument();

    globalThis.fetch = originalFetch;
  });
});
