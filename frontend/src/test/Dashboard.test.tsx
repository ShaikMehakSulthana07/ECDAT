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
import { ScanProjectView } from '../components/ScanProjectView';
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

  it('1. Phase 1: Renders LoginScreen with split screen layout matching reference', () => {
    const handleLogin = vi.fn();
    render(<LoginScreen onLogin={handleLogin} />);
    expect(screen.getAllByText('ECDAT').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/Enterprise Cryptographic Discovery & Analysis/i)).toBeInTheDocument();
    expect(screen.getByText(/Discover, Analyze, Secure, Quantum Ready/i)).toBeInTheDocument();
    expect(screen.getByText(/Welcome to ECDAT/i)).toBeInTheDocument();
    expect(screen.getByText(/Sign in to access your security analysis workspace/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Email Address$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Password$/i)).toBeInTheDocument();
    expect(screen.getByText(/For demonstration purposes only/i)).toBeInTheDocument();

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

  it('3. Phase 3: Renders DashboardView with KPI cards, charts, and recent findings', () => {
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

    expect(screen.getByRole('heading', { name: 'Cryptographic Security Overview' })).toBeInTheDocument();
    expect(screen.getByText('Visibility into cryptographic assets, quantum exposure, and migration readiness.')).toBeInTheDocument();
    expect(screen.getByText('Total Crypto Assets')).toBeInTheDocument();
    expect(screen.getAllByText('Quantum Vulnerable').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('High / Critical Risk')).toBeInTheDocument();
    expect(screen.getByText('Migration Required')).toBeInTheDocument();
    expect(screen.getByText('PQC Recommendations')).toBeInTheDocument();
    expect(screen.getByText('Risk Distribution')).toBeInTheDocument();
    expect(screen.getByText('Quantum Exposure')).toBeInTheDocument();
    expect(screen.getByText('Quantum Exposure Assessment')).toBeInTheDocument();
    expect(screen.getByText('Recent Cryptographic Findings')).toBeInTheDocument();

    // Recent Findings table
    expect(screen.getAllByText('ECDSA').length).toBeGreaterThanOrEqual(1);
    const inspectBtn = screen.getByRole('button', { name: /^View$/i });
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

    expect(screen.getByText(/Crypto Inventory/i)).toBeInTheDocument();
    expect(screen.getByText('ECDSA')).toBeInTheDocument();
    expect(screen.getAllByText(/Digital Signature/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('HIGH').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Vulnerable').length).toBeGreaterThanOrEqual(1);

    const inspectBtn = screen.getByRole('button', { name: /^View$/i });
    fireEvent.click(inspectBtn);
    expect(handleSelect).toHaveBeenCalledWith(0);
  });

  it('5. Phase 7: Renders AssetDetailDrawer with hero card, tabs, and reasoning chain', () => {
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

    expect(screen.getByText('Asset Detail')).toBeInTheDocument();
    expect(screen.getByText('← Back to Inventory')).toBeInTheDocument();
    expect(screen.getByText(/Reasoning Chain & Cryptographic Traceability/i)).toBeInTheDocument();
    expect(screen.getByText(/1. Source AST/i)).toBeInTheDocument();
    expect(screen.getByText('Overview')).toBeInTheDocument();
    expect(screen.getByText('Mosca Assessment')).toBeInTheDocument();
    expect(screen.getByText('PQC Recommendation')).toBeInTheDocument();
    expect(screen.getByText('Evidence')).toBeInTheDocument();

    const closeBtn = screen.getByRole('button', { name: /Close Drawer/i });
    fireEvent.click(closeBtn);
    expect(handleClose).toHaveBeenCalled();
  });

  it('6. Phase 8: Renders QuantumRiskView with Mosca calculations and charts', () => {
    const handleSelect = vi.fn();
    render(
      <QuantumRiskView
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
        context={mockResponse.context}
        onSelectFinding={handleSelect}
      />
    );

    expect(screen.getByText('Quantum Risk Analysis')).toBeInTheDocument();
    expect(screen.getByText(/Assess quantum vulnerability and migration urgency/i)).toBeInTheDocument();
    expect(screen.getAllByText('Quantum Vulnerable').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/Mosca's Theorem: Quantum Risk Condition/i)).toBeInTheDocument();
  });

  it('7. Phase 9: Renders PQCMigrationView with NIST standards alignment and charts', () => {
    const handleSelect = vi.fn();
    render(
      <PQCMigrationView
        findings={[mockFinding]}
        riskAssessments={[mockRisk]}
        pqcRecommendations={[mockPQC]}
        onSelectFinding={handleSelect}
      />
    );

    expect(screen.getByText('PQC Migration Strategy')).toBeInTheDocument();
    expect(screen.getByText(/Recommended post-quantum cryptography algorithms/i)).toBeInTheDocument();
    expect(screen.getByText('Target Standard Distribution')).toBeInTheDocument();
    expect(screen.getByText('Migration Strategy Breakdown')).toBeInTheDocument();
    expect(screen.getByText('Top Recommendations')).toBeInTheDocument();
  });

  it('7b. PQC Migration semantic separation: Target Standard counts and percentages sum to exactly 100% and exclude hybrid', () => {
    const mockRec1: typeof mockPQC = {
      ...mockPQC,
      currentAlgorithm: 'ECDSA',
      recommendedAlgorithm: 'ML-DSA',
      migrationStrategy: 'HYBRID',
      migrationPriority: 'HIGH',
    };
    const mockRec2: typeof mockPQC = {
      ...mockPQC,
      currentAlgorithm: 'ECDH',
      recommendedAlgorithm: 'ML-KEM',
      migrationStrategy: 'HYBRID',
      migrationPriority: 'HIGH',
    };

    render(
      <PQCMigrationView
        findings={[mockFinding, mockFinding]}
        riskAssessments={[mockRisk, mockRisk]}
        pqcRecommendations={[mockRec1, mockRec2]}
        onSelectFinding={() => {}}
      />
    );

    // Target Standard bars must have ML-DSA (50%), ML-KEM (50%), Needs Analysis (0%)
    expect(screen.getByText('ML-DSA (FIPS 204)')).toBeInTheDocument();
    expect(screen.getByText('ML-KEM (FIPS 203)')).toBeInTheDocument();
    expect(screen.getAllByText('50%').length).toBe(2);

    // Migration Strategy must display Hybrid Transition separately with count 2
    expect(screen.getByText('Migration Strategy Breakdown')).toBeInTheDocument();
    expect(screen.getByText('Hybrid Transition')).toBeInTheDocument();
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
    expect(screen.getByText(/Direct Usage/i)).toBeInTheDocument();
  });

  it('10. Phase 12: Renders CBOMViewer with CycloneDX-inspired format & JSON/CSV export', () => {
    render(<CBOMViewer cbom={mockCBOM} />);
    expect(screen.getByText('Cryptographic Bill of Materials')).toBeInTheDocument();
    expect(screen.getByText(/CycloneDX-inspired structure/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Download CSV/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Download JSON/i })).toBeInTheDocument();

    const rawJsonBtn = screen.getByRole('button', { name: /Raw CBOM JSON/i });
    fireEvent.click(rawJsonBtn);
    expect(screen.getByText(/CycloneDX-Inspired CBOM Document JSON/i)).toBeInTheDocument();
  });

  it('11. Phase 13 & 14: Renders ReportsView and SettingsView with Theme Toggle', () => {
    render(<ReportsView analysisData={mockResponse} />);
    expect(screen.getByRole('heading', { name: 'Reports' })).toBeInTheDocument();
    expect(screen.getByText(/Generate and export/i)).toBeInTheDocument();
    expect(screen.getByText('Full Analysis Report')).toBeInTheDocument();
    expect(screen.getByText('Recent Reports')).toBeInTheDocument();

    const handleHealth = vi.fn();
    const handleToggleTheme = vi.fn();
    render(
      <SettingsView
        user={{ name: 'Security Analyst', email: 'analyst@ecdat.local', org: 'SecOps' }}
        backendStatus="UP"
        onHealthCheck={handleHealth}
        theme="dark"
        onToggleTheme={handleToggleTheme}
      />
    );
    expect(screen.getByRole('heading', { name: 'Settings' })).toBeInTheDocument();
    expect(screen.getByText('Account Information')).toBeInTheDocument();
    expect(screen.getByText('Security Analyst')).toBeInTheDocument();
    expect(screen.getByText(/Local Development Session/i)).toBeInTheDocument();
    
    // Switch to Security tab and toggle theme
    const secTab = screen.getByRole('button', { name: /Security & Appearance/i });
    fireEvent.click(secTab);
    expect(screen.getByText(/Platform Appearance & Theme/i)).toBeInTheDocument();

    const themeToggleBtn = screen.getByRole('button', { name: /Toggle to Light Theme/i });
    fireEvent.click(themeToggleBtn);
    expect(handleToggleTheme).toHaveBeenCalled();
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
    expect(screen.getAllByText('Dashboard').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Scan Project').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('Crypto Inventory').length).toBeGreaterThanOrEqual(1);

    // Navigate to Scan page
    const scanNavBtn = screen.getByRole('button', { name: /Scan Project Archive/i });
    fireEvent.click(scanNavBtn);
    expect(screen.getByText(/Discover cryptographic artefacts across source code/i)).toBeInTheDocument();

    // Test Theme Toggle in TopBar
    const topBarThemeBtn = screen.getByRole('button', { name: /Switch to light theme/i });
    expect(topBarThemeBtn).toBeInTheDocument();
    fireEvent.click(topBarThemeBtn);
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
    expect(localStorage.getItem('ecdat_theme')).toBe('light');

    globalThis.fetch = originalFetch;
  });

  it('13. Phase 4: Renders redesigned ScanProjectView with 4 source cards, 6 scope cards, and scan configuration', () => {
    const handleScanPath = vi.fn();
    const handleScanFile = vi.fn();

    render(
      <ScanProjectView
        onScanPath={handleScanPath}
        onScanFile={handleScanFile}
        isLoading={false}
      />
    );

    // 1. Header
    expect(screen.getByRole('heading', { name: 'Scan Project' })).toBeInTheDocument();
    expect(screen.getByText(/Discover cryptographic artefacts across source code, binaries, configuration, libraries and container images/i)).toBeInTheDocument();

    // 2. Analysis Source Section & 4 Cards
    expect(screen.getByRole('heading', { name: 'Analysis Source' })).toBeInTheDocument();
    expect(screen.getByText('Upload Project')).toBeInTheDocument();
    expect(screen.getByText('ZIP or TAR project archive')).toBeInTheDocument();
    expect(screen.getByText('Repository URL')).toBeInTheDocument();
    expect(screen.getByText('Analyze a Git repository')).toBeInTheDocument();
    expect(screen.getByText('Files & Binaries')).toBeInTheDocument();
    expect(screen.getByText('JAR, CLASS, configuration and certificate files')).toBeInTheDocument();
    expect(screen.getByText('Container Image')).toBeInTheDocument();
    expect(screen.getByText('Analyze a container image or image archive')).toBeInTheDocument();

    // Badges for roadmap features
    expect(screen.getByText('Backend integration required')).toBeInTheDocument();
    expect(screen.getAllByText('Coming soon').length).toBe(2);

    // 3. Analysis Scope Section & 6 Custom Cards
    expect(screen.getByRole('heading', { name: 'Analysis Scope' })).toBeInTheDocument();
    expect(screen.getByText('Cryptographic APIs')).toBeInTheDocument();
    expect(screen.getByText('Discover cryptographic primitives and usage')).toBeInTheDocument();
    expect(screen.getByText('Dependencies')).toBeInTheDocument();
    expect(screen.getByText('Identify cryptographic libraries and versions')).toBeInTheDocument();
    expect(screen.getByText('Certificates')).toBeInTheDocument();
    expect(screen.getByText('Inspect certificates and public-key properties')).toBeInTheDocument();
    expect(screen.getByText('Quantum Risk')).toBeInTheDocument();
    expect(screen.getByText('Assess quantum vulnerability and Mosca exposure')).toBeInTheDocument();
    expect(screen.getByText('PQC Migration')).toBeInTheDocument();
    expect(screen.getByText('Map vulnerable primitives to PQC alternatives')).toBeInTheDocument();
    expect(screen.getByText('CBOM')).toBeInTheDocument();
    expect(screen.getByText('Generate cryptographic bill of materials')).toBeInTheDocument();

    // Toggle a scope card
    const cbomCard = screen.getByRole('checkbox', { name: /CBOM/i });
    expect(cbomCard).toHaveAttribute('aria-checked', 'true');
    fireEvent.click(cbomCard);
    expect(cbomCard).toHaveAttribute('aria-checked', 'false');

    // 4. Scan Configuration Section
    expect(screen.getByRole('heading', { name: 'Scan Configuration' })).toBeInTheDocument();
    expect(screen.getByLabelText(/Application \/ Service Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Business Criticality$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Data Sensitivity$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Data Lifetime \(Y\)/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Migration Time \(X\)/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Threat Horizon \(Z\)/i)).toBeInTheDocument();

    // 5. Start Analysis CTA
    const startBtn = screen.getByRole('button', { name: /Start Analysis/i });
    expect(startBtn).toBeInTheDocument();
    expect(startBtn).toBeDisabled(); // Disabled because no archive is selected yet
  });

  it('14. Phase 4: Handles file selection, displays accurate metadata state, and triggers onScanFile', () => {
    const handleScanPath = vi.fn();
    const handleScanFile = vi.fn();

    const { container } = render(
      <ScanProjectView
        onScanPath={handleScanPath}
        onScanFile={handleScanFile}
        isLoading={false}
      />
    );

    // Create a mock ZIP file
    const file = new File(['fake zip content bytes'], 'payment-service.zip', { type: 'application/zip' });
    const fileInput = container.querySelector('input[type="file"]') as HTMLInputElement;
    expect(fileInput).not.toBeNull();

    // Select file
    fireEvent.change(fileInput, { target: { files: [file] } });

    // Verify Selected File State
    expect(screen.getAllByText('payment-service.zip').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/Ready for analysis/i)).toBeInTheDocument();
    expect(screen.getByText('Remove')).toBeInTheDocument();

    // Submit form
    const startBtn = screen.getByRole('button', { name: /Start Analysis/i });
    expect(startBtn).not.toBeDisabled();
    fireEvent.click(startBtn);

    expect(handleScanFile).toHaveBeenCalledWith(
      file,
      expect.objectContaining({
        businessCriticality: 'CRITICAL',
        dataSensitivity: 'HIGHLY_SENSITIVE',
      })
    );
  });

  it('15. Phase 4: Handles filesystem directory preset switching and triggers onScanPath', () => {
    const handleScanPath = vi.fn();
    const handleScanFile = vi.fn();

    render(
      <ScanProjectView
        onScanPath={handleScanPath}
        onScanFile={handleScanFile}
        isLoading={false}
      />
    );

    // Switch to Directory Path subtab
    const dirTab = screen.getByRole('button', { name: /^Directory Path$/i });
    fireEvent.click(dirTab);

    expect(screen.getByLabelText(/Filesystem Directory Path/i)).toBeInTheDocument();
    const dirInput = screen.getByLabelText(/Filesystem Directory Path/i) as HTMLInputElement;
    expect(dirInput.value).toBe('../test-target');

    // Click quick preset
    const presetBtn = screen.getByRole('button', { name: '../test-target/src/main/java/demo' });
    fireEvent.click(presetBtn);
    expect(dirInput.value).toBe('../test-target/src/main/java/demo');

    // Submit form
    const startBtn = screen.getByRole('button', { name: /Start Analysis/i });
    expect(startBtn).not.toBeDisabled();
    fireEvent.click(startBtn);

    expect(handleScanPath).toHaveBeenCalledWith(
      '../test-target/src/main/java/demo',
      expect.objectContaining({
        businessCriticality: 'CRITICAL',
      })
    );
  });

  it('16. Phase 4: Displays multi-step analysis progress state when isLoading is true', () => {
    render(
      <ScanProjectView
        onScanPath={vi.fn()}
        onScanFile={vi.fn()}
        isLoading={true}
        activeTarget="crypto-gateway.zip"
      />
    );

    expect(screen.getByText('ANALYSIS IN PROGRESS')).toBeInTheDocument();
    expect(screen.getByText(/crypto-gateway\.zip/i)).toBeInTheDocument();
    expect(screen.getByText('Source Discovery')).toBeInTheDocument();
    expect(screen.getByText('Cryptographic API Analysis')).toBeInTheDocument();
    expect(screen.getByText('Dependency Analysis')).toBeInTheDocument();
    expect(screen.getByText('Certificate Analysis')).toBeInTheDocument();
    expect(screen.getByText('Quantum Risk Assessment')).toBeInTheDocument();
    expect(screen.getByText('PQC Recommendations')).toBeInTheDocument();
    expect(screen.getByText('CBOM Generation')).toBeInTheDocument();
    expect(screen.getByText('Executing Pipeline')).toBeInTheDocument();
  });

  it('17. Phase 4B: Renders ReportsView with dynamic metrics and no fake history', () => {
    render(<ReportsView analysisData={mockResponse} />);

    // Header & Target Badge
    expect(screen.getByRole('heading', { name: 'Reports' })).toBeInTheDocument();
    expect(screen.getByText(/Generate and export cryptographic security assessments from the current analysis/i)).toBeInTheDocument();

    // Current Analysis Card
    expect(screen.getByText('CURRENT ANALYSIS')).toBeInTheDocument();
    expect(screen.getByText('Total Crypto Assets')).toBeInTheDocument();
    expect(screen.getByText('Quantum Vulnerable')).toBeInTheDocument();
    expect(screen.getByText('High / Critical Risk')).toBeInTheDocument();
    expect(screen.getByText('Migration Required')).toBeInTheDocument();

    // 3 Generator Cards
    expect(screen.getByText('Full Analysis Report')).toBeInTheDocument();
    expect(screen.getByText('CBOM Report')).toBeInTheDocument();
    expect(screen.getByText('Findings Report')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Generate Full Report →/i })).toBeEnabled();
    expect(screen.getByRole('button', { name: /Export CBOM →/i })).toBeEnabled();
    expect(screen.getByRole('button', { name: /Export Findings →/i })).toBeEnabled();

    // Recent Reports initial empty session state (NO fake report rows!)
    expect(screen.getByText('No reports generated yet.')).toBeInTheDocument();
    expect(screen.getByText('Generate a report above to make it available here.')).toBeInTheDocument();
    expect(screen.queryByText('ECDAT-Report-2025-04-27')).not.toBeInTheDocument();
    expect(screen.queryByText('CBOM-2025-04-27')).not.toBeInTheDocument();
  });

  it('18. Phase 4B: Renders ReportsView empty state when no analysis data is loaded', () => {
    const handleNavScan = vi.fn();
    render(<ReportsView analysisData={null} onNavigateScan={handleNavScan} />);

    // Empty Analysis Banner
    expect(screen.getByText('Run a project analysis to generate reports.')).toBeInTheDocument();
    expect(screen.getByText(/Reports require active cryptographic findings/i)).toBeInTheDocument();

    // Buttons must be disabled
    expect(screen.getByRole('button', { name: /Generate Full Report →/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /Export CBOM →/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /Export Findings →/i })).toBeDisabled();

    // Nav to Scan button
    const scanBtn = screen.getByRole('button', { name: /Go to Scan Project →/i });
    fireEvent.click(scanBtn);
    expect(handleNavScan).toHaveBeenCalled();
  });

  it('19. Phase 4B: Exports CBOM JSON and Findings CSV and records in session history', () => {
    render(<ReportsView analysisData={mockResponse} />);

    // Mock URL.createObjectURL and URL.revokeObjectURL
    const createObjectURLMock = vi.fn(() => 'blob:mock-url');
    const revokeObjectURLMock = vi.fn();
    globalThis.URL.createObjectURL = createObjectURLMock;
    globalThis.URL.revokeObjectURL = revokeObjectURLMock;

    // Export CBOM
    const cbomBtn = screen.getByRole('button', { name: /Export CBOM →/i });
    fireEvent.click(cbomBtn);

    // Verify Success Alert and History Record
    expect(screen.getByText(/CBOM Report exported successfully/i)).toBeInTheDocument();
    expect(screen.getByText('Recent Reports')).toBeInTheDocument();
    expect(screen.getByText(/1 Report Available/i)).toBeInTheDocument();

    // Export Findings CSV
    const csvBtn = screen.getByRole('button', { name: /Export Findings →/i });
    fireEvent.click(csvBtn);

    // Verify Success Alert and Updated History Record
    expect(screen.getByText(/Findings Report exported successfully/i)).toBeInTheDocument();
    expect(screen.getByText(/2 Reports Available/i)).toBeInTheDocument();
  });

  it('20. Phase 4B: Generates Full Analysis Report (PDF/HTML) and toggles Executive Preview modal', () => {
    render(<ReportsView analysisData={mockResponse} />);

    // Open Executive Preview Modal
    const previewBtn = screen.getByRole('button', { name: /Preview Executive Report/i });
    fireEvent.click(previewBtn);

    expect(screen.getByText('Executive Security Assessment Preview')).toBeInTheDocument();
    expect(screen.getByText(/12-Section Cryptographic Posture/i)).toBeInTheDocument();

    // Close preview modal
    const closeBtn = screen.getByRole('button', { name: 'Close' });
    fireEvent.click(closeBtn);
    expect(screen.queryByText('Executive Security Assessment Preview')).not.toBeInTheDocument();

    // Generate Full Report
    const fullReportBtn = screen.getByRole('button', { name: /Generate Full Report →/i });
    fireEvent.click(fullReportBtn);

    expect(screen.getByText(/Full Analysis Report generated successfully/i)).toBeInTheDocument();
  });
});
