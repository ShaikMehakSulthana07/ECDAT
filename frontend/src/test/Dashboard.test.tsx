import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import App from '../App';
import { LoginScreen } from '../components/LoginScreen';
import { DashboardView } from '../components/DashboardView';
import { CryptoInventoryView } from '../components/CryptoInventoryView';
import { AssetDetailDrawer } from '../components/AssetDetailDrawer';
import { QuantumRiskView } from '../components/QuantumRiskView';
import { PQCMigrationView } from '../components/PQCMigrationView';
import { CertificatesView } from '../components/CertificatesView';
import { DependenciesView } from '../components/DependenciesView';
import { CBOMViewer } from '../components/CBOMViewer';
import { ReportsView } from '../components/ReportsView';
import { SettingsView } from '../components/SettingsView';
import type { AnalysisResponse, CryptoFinding, RiskAssessment, PQCRecommendation, CBOMDocument, CertificateArtifactFinding } from '../types/analysis';

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
  businessCriticality: 'CRITICAL',
  dataSensitivity: 'HIGHLY_SENSITIVE',
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
  quantumRiskResult: {
    algorithm: 'ECDSA',
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
    explanation: 'ECDSA is vulnerable to Shor algorithm. Migration required immediately.',
    calculationDetails: 'Mosca Calculation: 3 + 10 = 13 > 10 (TRUE)',
  },
};

const mockPQC: PQCRecommendation = {
  recommendationStatus: 'RECOMMENDED',
  currentAlgorithm: 'ECDSA',
  currentPurpose: 'DIGITAL_SIGNATURE',
  recommendedAlgorithm: 'ML-DSA',
  alternativeAlgorithms: ['SLH-DSA'],
  rationale: 'NIST FIPS 204 standardized digital signature replacement.',
  migrationPriority: 'HIGH',
  migrationStrategy: 'DIRECT_PQC',
  quantumRisk: 'HIGH',
  confidence: 'HIGH',
  considerations: ['Certificate ecosystem compatibility', 'Signature size changes'],
};

const mockCert: CertificateArtifactFinding = {
  fileName: 'sample-cert.crt',
  filePath: '/path/sample-cert.crt',
  fileType: 'X.509 Certificate',
  certificateType: 'X.509',
  publicKeyAlgorithm: 'RSA',
  keySize: 2048,
  signatureAlgorithm: 'SHA256withRSA',
  validityDates: '2026-01-01 to 2027-01-01',
  confidence: 'HIGH',
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
        businessCriticality: 'CRITICAL',
        dataSensitivity: 'HIGHLY_SENSITIVE',
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
  certificateFindings: [mockCert],
  cbom: mockCBOM,
  context: {
    applicationName: 'Test Core',
    businessCriticality: 'CRITICAL',
    dataSensitivity: 'HIGHLY_SENSITIVE',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10,
  },
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

describe('ECDAT Enterprise Frontend Redesign Tests', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('1. Phase 1: Renders LoginScreen with local workspace session elements', () => {
    const handleLogin = vi.fn();
    render(<LoginScreen onLogin={handleLogin} />);
    expect(screen.getByText('ECDAT')).toBeInTheDocument();
    expect(screen.getByText(/Enterprise Cryptographic Discovery & Analysis/i)).toBeInTheDocument();
    expect(screen.getByText(/Sign in to access your local analysis workspace/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Work Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password/i)).toBeInTheDocument();
    expect(screen.getByText(/Local analysis session/i)).toBeInTheDocument();
    expect(screen.getByText(/Continue with SSO \(Not Configured\)/i)).toBeInTheDocument();

    const signInBtn = screen.getByRole('button', { name: /Sign In/i });
    fireEvent.click(signInBtn);
    expect(handleLogin).toHaveBeenCalledWith(
      expect.objectContaining({
        email: expect.stringContaining('@'),
        name: expect.any(String),
      })
    );
  });

  it('2. Phase 3: Renders DashboardView empty state when no scan data is loaded', () => {
    const handleNav = vi.fn();
    const handleSelect = vi.fn();
    const handleQuickScan = vi.fn();

    render(
      <DashboardView
        analysisData={null}
        onNavigate={handleNav}
        onSelectFinding={handleSelect}
        onQuickScan={handleQuickScan}
        isLoading={false}
      />
    );

    expect(screen.getByText(/No Analysis Available/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Scan Project Archive/i })).toBeInTheDocument();
    
    const quickBtn = screen.getByRole('button', { name: /Scan Default Target/i });
    fireEvent.click(quickBtn);
    expect(handleQuickScan).toHaveBeenCalled();
  });

  it('3. Phase 3: Renders DashboardView with actual metrics and Mosca Theorem panel', () => {
    const handleNav = vi.fn();
    const handleSelect = vi.fn();
    const handleQuickScan = vi.fn();

    render(
      <DashboardView
        analysisData={mockResponse}
        onNavigate={handleNav}
        onSelectFinding={handleSelect}
        onQuickScan={handleQuickScan}
        isLoading={false}
      />
    );

    expect(screen.getByText(/Cryptographic Security Overview/i)).toBeInTheDocument();
    expect(screen.getByText('Total Crypto Assets')).toBeInTheDocument();
    expect(screen.getAllByText('Quantum Vulnerable').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('High / Critical Risk')).toBeInTheDocument();
    expect(screen.getAllByText('Migration Required').length).toBeGreaterThanOrEqual(1);

    // Mosca equation values
    expect(screen.getByText(/Mosca's Theorem/i)).toBeInTheDocument();
    expect(screen.getByText('3 yrs')).toBeInTheDocument();
    expect(screen.getByText('13 yrs')).toBeInTheDocument();
    expect(screen.getAllByText(/MIGRATION REQUIRED/i).length).toBeGreaterThanOrEqual(1);

    // Discovered Assets Quick View table
    expect(screen.getByText('ECDSA')).toBeInTheDocument();
    const inspectBtn = screen.getByRole('button', { name: /Inspect/i });
    fireEvent.click(inspectBtn);
    expect(handleSelect).toHaveBeenCalledWith(0);
  });

  it('4. Phase 6: Renders CryptoInventoryView with search, filter, and pagination', () => {
    const handleSelect = vi.fn();
    render(
      <CryptoInventoryView
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
        pqcRecommendations={[mockPQC]}
        onSelectFinding={handleSelect}
        selectedIndex={null}
      />
    );

    expect(screen.getByText(/Cryptographic Asset Inventory/i)).toBeInTheDocument();
    expect(screen.getByText('ECDSA')).toBeInTheDocument();
    expect(screen.getAllByText('DIGITAL SIGNATURE').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('ACTIVE')).toBeInTheDocument();
    expect(screen.getByText(/HIGH \(65\)/i)).toBeInTheDocument();

    const inspectBtn = screen.getByRole('button', { name: /Inspect/i });
    fireEvent.click(inspectBtn);
    expect(handleSelect).toHaveBeenCalledWith(0);
  });

  it('5. Phase 7: Renders AssetDetailDrawer with full reasoning chain and Mosca details', () => {
    const handleClose = vi.fn();
    render(
      <AssetDetailDrawer
        finding={mockFinding}
        riskAssessment={mockRisk}
        pqcRecommendation={mockPQC}
        findingIndex={0}
        totalFindings={1}
        onClose={handleClose}
      />
    );

    expect(screen.getByText(/Asset Finding 1 of 1/i)).toBeInTheDocument();
    expect(screen.getByText(/Reasoning Chain & Cryptographic Traceability/i)).toBeInTheDocument();
    expect(screen.getByText(/1. Source AST/i)).toBeInTheDocument();
    expect(screen.getByText(/4. Quantum Assessment/i)).toBeInTheDocument();
    expect(screen.getByText(/5. PQC Target/i)).toBeInTheDocument();
    expect(screen.getAllByText(/ML-DSA/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/SLH-DSA/i)).toBeInTheDocument();
    expect(screen.getByText(/Signature.getInstance\("SHA256withECDSA"\)/i)).toBeInTheDocument();

    const closeBtn = screen.getByRole('button', { name: /Close Drawer/i });
    fireEvent.click(closeBtn);
    expect(handleClose).toHaveBeenCalled();
  });

  it('6. Phase 8: Renders QuantumRiskView with Mosca calculations', () => {
    const handleSelect = vi.fn();
    render(
      <QuantumRiskView
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
        context={mockResponse.context}
        onSelectFinding={handleSelect}
      />
    );

    expect(screen.getByText(/Quantum Risk & Exposure Assessment/i)).toBeInTheDocument();
    expect(screen.getByText('Quantum Vulnerable Assets')).toBeInTheDocument();
    expect(screen.getByText('Critical Quantum Assets')).toBeInTheDocument();
    expect(screen.getByText(/Mosca's Theorem: Quantum Risk Condition/i)).toBeInTheDocument();
  });

  it('7. Phase 9: Renders PQCMigrationView with NIST standards alignment', () => {
    const handleSelect = vi.fn();
    render(
      <PQCMigrationView
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
        pqcRecommendations={[mockPQC]}
        onSelectFinding={handleSelect}
      />
    );

    expect(screen.getByText(/Post-Quantum Cryptography \(PQC\) Migration/i)).toBeInTheDocument();
    expect(screen.getByText(/FIPS 203 · ML-KEM/i)).toBeInTheDocument();
    expect(screen.getByText(/FIPS 204 · ML-DSA/i)).toBeInTheDocument();
    expect(screen.getByText(/FIPS 205 · SLH-DSA/i)).toBeInTheDocument();
    expect(screen.getByText('ML-DSA')).toBeInTheDocument();
    expect(screen.getByText('DIRECT_PQC')).toBeInTheDocument();
  });

  it('8. Phase 10: Renders CertificatesView with discovered artifacts', () => {
    render(<CertificatesView certificates={[mockCert]} />);
    expect(screen.getByText(/Certificate & Cryptographic Artifacts/i)).toBeInTheDocument();
    expect(screen.getByText('sample-cert.crt')).toBeInTheDocument();
    expect(screen.getByText('2048 bits')).toBeInTheDocument();
    expect(screen.getByText('SHA256withRSA')).toBeInTheDocument();
  });

  it('9. Phase 11: Renders DependenciesView with library posture', () => {
    render(
      <DependenciesView
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
      />
    );
    expect(screen.getByText(/Cryptographic Dependencies/i)).toBeInTheDocument();
    expect(screen.getByText('Java Cryptography Architecture (JCA)')).toBeInTheDocument();
    expect(screen.getByText('DIRECT_USAGE')).toBeInTheDocument();
  });

  it('10. Phase 12: Renders CBOMViewer with CycloneDX-inspired format & JSON export', () => {
    render(<CBOMViewer cbom={mockCBOM} />);
    expect(screen.getAllByText(/ECDAT Cryptography Bill of Materials/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('CycloneDX')).toBeInTheDocument();
    expect(screen.getByText('1.6')).toBeInTheDocument();
    expect(screen.getByText('urn:uuid:test-12345')).toBeInTheDocument();

    const rawJsonBtn = screen.getByRole('button', { name: /Raw CBOM JSON/i });
    fireEvent.click(rawJsonBtn);
    expect(screen.getByText(/CycloneDX-Inspired CBOM Document JSON/i)).toBeInTheDocument();
  });

  it('11. Phase 13 & 14: Renders ReportsView and SettingsView', () => {
    render(<ReportsView analysisData={mockResponse} />);
    expect(screen.getByText(/Security & Governance Reports/i)).toBeInTheDocument();
    expect(screen.getByText(/Executive Security Summary/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Export Inventory CSV/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Download CBOM JSON/i })).toBeInTheDocument();

    const handleHealth = vi.fn();
    render(
      <SettingsView
        user={{ name: 'Security Analyst', email: 'sec@corp.com', org: 'SecOps' }}
        backendStatus="UP"
        onHealthCheck={handleHealth}
      />
    );
    expect(screen.getByText(/Platform & Analysis Settings/i)).toBeInTheDocument();
    expect(screen.getByText(/Application Session Profile/i)).toBeInTheDocument();
    expect(screen.getByText('Security Analyst')).toBeInTheDocument();
    expect(screen.getByText(/Local Analysis Session/i)).toBeInTheDocument();
    expect(screen.getByText(/ONLINE \(HEALTHY\)/i)).toBeInTheDocument();
  });

  it('12. Full App: Handles authenticated session and sidebar navigation', async () => {
    localStorage.setItem('ecdat_auth', 'true');
    const originalFetch = globalThis.fetch;
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'UP' }),
    });

    await act(async () => {
      render(<App />);
    });

    expect(screen.getAllByText('ECDAT').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/Analysis Engine:/i)).toBeInTheDocument();
    expect(screen.getByText('Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Scan Project')).toBeInTheDocument();
    expect(screen.getByText('Crypto Inventory')).toBeInTheDocument();

    // Navigate to Scan page
    const scanNavBtn = screen.getByText('Scan Project');
    fireEvent.click(scanNavBtn);
    expect(screen.getByText(/Upload a project archive to discover/i)).toBeInTheDocument();

    globalThis.fetch = originalFetch;
  });
});
