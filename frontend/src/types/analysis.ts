/**
 * TypeScript types matching ECDAT backend DTOs and models (Phase 1-7).
 */

export type Purpose =
  | 'ENCRYPTION'
  | 'DECRYPTION'
  | 'KEY_GENERATION'
  | 'KEY_AGREEMENT'
  | 'DIGITAL_SIGNATURE'
  | 'HASHING'
  | 'PROTOCOL'
  | 'UNKNOWN';

export type Confidence = 'HIGH' | 'MEDIUM' | 'LOW';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type QuantumRisk = 'NONE' | 'LOW' | 'HIGH';

export type PQCRecommendationStatus =
  | 'RECOMMENDED'
  | 'CONDITIONAL'
  | 'NEEDS_ANALYSIS'
  | 'NOT_REQUIRED';

export type MigrationPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type MigrationStrategy = 'DIRECT_PQC' | 'HYBRID' | 'NEEDS_ANALYSIS' | 'NO_ACTION' | 'UNKNOWN';

export type AssetCategory =
  | 'ENCRYPTION'
  | 'DIGITAL_SIGNATURE'
  | 'KEY_ESTABLISHMENT'
  | 'HASHING'
  | 'TLS_PROTOCOL'
  | 'CERTIFICATE'
  | 'KEY_GENERATION'
  | 'UNKNOWN';

export type CryptoUsageCategory =
  | 'DIRECT_USAGE'
  | 'INDIRECT_CONFIGURATION'
  | 'DEPENDENCY_PRESENCE'
  | 'UNKNOWN';

export type LifecycleStatus = 'ACTIVE' | 'DEPRECATED' | 'UNKNOWN';

export type BusinessCriticality =
  | 'CRITICAL'
  | 'HIGH'
  | 'MEDIUM'
  | 'LOW'
  | 'UNKNOWN';

export type DataSensitivity = 'HIGHLY_SENSITIVE' | 'CONFIDENTIAL' | 'INTERNAL' | 'PUBLIC' | 'UNKNOWN';

export type ValueSource = 'USER_PROVIDED' | 'ORGANIZATION_DEFAULT' | 'SYSTEM_DEFAULT' | 'UNKNOWN';

export type Provenance =
  | 'OBSERVED'
  | 'INFERRED'
  | 'DEPENDENCY_METADATA'
  | 'USER_PROVIDED'
  | 'DEFAULT_ASSUMPTION'
  | 'UNKNOWN';

export interface CryptoFinding {
  algorithm: string;
  variant?: string | null;
  mode?: string | null;
  padding?: string | null;
  purpose: Purpose;
  keySize?: number | null;
  file: string;
  line: number;
  evidence: string;
  confidence: Confidence;
  sourceType: string;
  // Phase 7 Enterprise Classification
  assetCategory?: AssetCategory;
  usageCategory?: CryptoUsageCategory;
  lifecycleStatus?: LifecycleStatus;
  businessCriticality?: BusinessCriticality;
  dataSensitivity?: DataSensitivity;
  protocol?: string | null;
  library?: string | null;
  // Provenance fields for key attributes
  algorithmProvenance?: Provenance;
  keySizeProvenance?: Provenance;
  purposeProvenance?: Provenance;
  libraryProvenance?: Provenance;
  businessCriticalityProvenance?: Provenance;
  dataSensitivityProvenance?: Provenance;
}

export interface CryptoAsset {
  assetId: string;
  algorithm: string;
  variant?: string | null;
  mode?: string | null;
  padding?: string | null;
  purpose: Purpose;
  keySize?: number | null;
  protocol?: string | null;
  library?: string | null;
  sourceFile: string;
  sourceLine: number;
  evidence: string;
  confidence: Confidence;
  sourceType: string;
  assetCategory: AssetCategory;
  usageCategory: CryptoUsageCategory;
  lifecycleStatus: LifecycleStatus;
  businessCriticality: BusinessCriticality;
  dataSensitivity: DataSensitivity;
  riskAssessment?: RiskAssessment;
  pqcRecommendation?: PQCRecommendation;
  originalFinding?: CryptoFinding;
}

export interface CryptoInventory {
  assets: CryptoAsset[];
  totalAssets: number;
  categoryBreakdown: Record<string, number>;
  lifecycleBreakdown: Record<string, number>;
  usageBreakdown: Record<string, number>;
  generatedAt: string;
}

export interface RiskFactor {
  name: string;
  score: number;
  explanation: string;
}

export interface RiskAssessment {
  riskScore: number;
  riskLevel: RiskLevel;
  reasons: string[];
  factors: RiskFactor[];
  quantumRisk: QuantumRisk;
  confidence: Confidence;
  originalFinding: CryptoFinding;
  quantumRiskResult?: QuantumRiskResult;
  scoreBreakdown?: RiskScoreBreakdown;
}

export interface RiskScoreBreakdown {
  totalScore: number;
  riskLevel: RiskLevel;
  components: ScoreComponent[];
  methodologyNote: string;
}

export interface ScoreComponent {
  name: string;
  score: number;
  explanation: string;
  category: string;
}

export interface PQCRecommendation {
  recommendationStatus: PQCRecommendationStatus;
  currentAlgorithm: string;
  currentPurpose: Purpose;
  recommendedAlgorithm?: string | null;
  alternativeAlgorithms: string[];
  rationale: string;
  migrationPriority: MigrationPriority;
  migrationStrategy?: MigrationStrategy;
  quantumRisk: QuantumRisk;
  confidence: Confidence;
  considerations: string[];
  // Provenance fields for recommendation attributes
  recommendationStatusProvenance?: Provenance;
  recommendedAlgorithmProvenance?: Provenance;
  migrationPriorityProvenance?: Provenance;
}

export interface CBOMProperty {
  name: string;
  value: string;
}

export interface CBOMRiskInfo {
  riskLevel?: string;
  riskScore?: number;
  quantumRisk?: string;
  riskFactors?: string[];
  // Quantum migration risk details (Mosca-style assessment)
  quantumVulnerable?: boolean;
  migrationRequired?: boolean;
  dataLifetimeYears?: number;
  migrationTimeYears?: number;
  threatHorizonYears?: number;
  moscaConditionMet?: boolean;
  totalExposureYears?: number;
  migrationUrgency?: string;
  quantumRiskExplanation?: string;
  moscaCalculationDetails?: string;
  // Value source attribution
  dataLifetimeYearsSource?: ValueSource;
  migrationTimeYearsSource?: ValueSource;
  threatHorizonYearsSource?: ValueSource;
  businessCriticalitySource?: ValueSource;
  dataSensitivitySource?: ValueSource;
}

export interface CBOMPQCInfo {
  recommendationStatus?: string;
  recommendedAlgorithm?: string | null;
  alternativeAlgorithms?: string[];
  rationale?: string;
  migrationPriority?: string;
  considerations?: string[];
}

export interface CBOMCryptoProperties {
  algorithm?: string;
  algorithmVariant?: string;
  mode?: string | null;
  padding?: string | null;
  purpose?: string;
  keySize?: number | null;
  protocol?: string;
  library?: string;
  sourceFile?: string;
  sourceLine?: number;
  evidence?: string;
  confidence?: string;
  sourceType?: string;
  risk?: CBOMRiskInfo;
  pqcRecommendation?: CBOMPQCInfo;
  assetCategory?: string;
  usageCategory?: string;
  lifecycleStatus?: string;
  businessCriticality?: string;
  dataSensitivity?: string;
  // Provenance fields for key attributes
  algorithmProvenance?: Provenance;
  keySizeProvenance?: Provenance;
  purposeProvenance?: Provenance;
  businessCriticalityProvenance?: Provenance;
  dataSensitivityProvenance?: Provenance;
}

export interface CBOMComponent {
  type: string;
  name: string;
  description: string;
  cryptoProperties?: CBOMCryptoProperties;
  properties?: CBOMProperty[];
}

export interface CBOMTool {
  vendor: string;
  name: string;
  version: string;
}

export interface CBOMMetadata {
  tool: CBOMTool;
}

export interface CBOMDocument {
  bomFormat: string;
  specVersion: string;
  serialNumber: string;
  version: number;
  metadata: CBOMMetadata;
  components: CBOMComponent[];
}

export interface AnalysisSummary {
  totalFindings: number;
  lowRiskCount: number;
  mediumRiskCount: number;
  highRiskCount: number;
  criticalRiskCount: number;
  quantumHighRiskCount: number;
  pqcRecommendedCount: number;
  pqcConditionalCount: number;
  pqcNeedsAnalysisCount: number;
  pqcNotRequiredCount: number;
  activeAssetCount?: number;
  deprecatedAssetCount?: number;
  unknownLifecycleCount?: number;
  directUsageCount?: number;
}

export interface AnalysisResponse {
  status: string;
  sourcePath: string;
  inputType?: string;
  inputName?: string;
  inputSource?: string;
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
  cryptoAssets?: CryptoAsset[];
  inventory?: CryptoInventory;
  cbom: CBOMDocument;
  summary: AnalysisSummary;
  context?: ProjectAnalysisContext;
  certificateFindings?: CertificateArtifactFinding[];
}

export interface CertificateArtifactFinding {
  fileName: string;
  filePath: string;
  fileType: string;
  certificateType: string;
  publicKeyAlgorithm?: string;
  keySize?: number;
  signatureAlgorithm?: string;
  validityDates?: string;
  confidence: Confidence;
}

export interface AnalyzeRequest {
  path: string;
  context?: ProjectAnalysisContext;
}

export interface ProjectAnalysisContext {
  applicationName?: string;
  businessCriticality?: BusinessCriticality;
  dataSensitivity?: DataSensitivity;
  dataLifetimeYears?: number;
  migrationTimeYears?: number;
  threatHorizonYears?: number;
  // Value source attribution
  businessCriticalitySource?: ValueSource;
  dataSensitivitySource?: ValueSource;
  dataLifetimeYearsSource?: ValueSource;
  migrationTimeYearsSource?: ValueSource;
  threatHorizonYearsSource?: ValueSource;
}

// Alias for frontend usage
export type ProjectContext = ProjectAnalysisContext;

export interface QuantumRiskInput {
  algorithm: string;
  keySize?: number | null;
  cryptographicPurpose: string;
  dataLifetimeYears: number;
  migrationTimeYears: number;
  threatHorizonYears: number;
  businessCriticality: BusinessCriticality;
  dataSensitivity: DataSensitivity;
}

export type MigrationUrgency = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'NONE' | 'UNKNOWN';

export interface QuantumRiskResult {
  algorithm: string;
  quantumVulnerabilityStatus?: 'VULNERABLE' | 'NOT_QUANTUM_VULNERABLE' | 'UNKNOWN';
  quantumVulnerable: boolean;
  migrationRequired: boolean;
  migrationTimeYears: number;
  dataLifetimeYears: number;
  threatHorizonYears: number;
  moscaConditionMet: boolean;
  totalExposureYears: number;
  yearsUntilThreat: number;
  businessCriticality: BusinessCriticality;
  dataSensitivity: DataSensitivity;
  migrationUrgency: MigrationUrgency;
  explanation: string;
  calculationDetails: string;
  // Value source attribution
  migrationTimeYearsSource?: ValueSource;
  dataLifetimeYearsSource?: ValueSource;
  threatHorizonYearsSource?: ValueSource;
  businessCriticalitySource?: ValueSource;
  dataSensitivitySource?: ValueSource;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

// Phase 4 Multi-Input Scan Architecture Types
export type AnalysisInputType =
  | 'ZIP_ARCHIVE'
  | 'SOURCE_FILE'
  | 'DIRECTORY'
  | 'REPOSITORY_URL'
  | 'CONFIGURATION_FILE'
  | 'BINARY_FILE'
  | 'CONTAINER_IMAGE';

export interface InputCapability {
  type: string;
  supported: boolean;
  displayName: string;
  description: string;
  plannedPhase?: string;
}

export interface CapabilitiesResponse {
  inputs: InputCapability[];
}

export type ScanInputType =
  | 'ZIP_ARCHIVE'
  | 'SOURCE_FILE'
  | 'DIRECTORY'
  | 'GIT_REPOSITORY'
  | 'REPOSITORY_URL'
  | 'FILES'
  | 'JAR'
  | 'CLASS'
  | 'CONFIGURATION'
  | 'CONFIGURATION_FILE'
  | 'BINARY_FILE'
  | 'CONTAINER_IMAGE';

export type AnalysisScopeType =
  | 'CRYPTO_APIS'
  | 'DEPENDENCIES'
  | 'CERTIFICATES'
  | 'QUANTUM_RISK'
  | 'PQC_MIGRATION'
  | 'CBOM';

export interface ScanRequest {
  inputType: ScanInputType;
  sourceIdentifier: string;
  directoryPath?: string;
  repositoryUrl?: string;
  projectName?: string;
  context?: ProjectAnalysisContext;
  scopes?: AnalysisScopeType[];
}

export interface AnalysisInput {
  inputType: AnalysisInputType;
  originalName: string;
  sourceIdentifier?: string;
  directoryPath?: string;
  repositoryUrl?: string;
  projectName?: string;
  context?: ProjectAnalysisContext;
  scopes?: AnalysisScopeType[];
}
