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

export type DataSensitivity = 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN';

export interface CryptoFinding {
  algorithm: string;
  variant?: string | null;
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
}

export interface CryptoAsset {
  assetId: string;
  algorithm: string;
  variant?: string | null;
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
}

export interface PQCRecommendation {
  recommendationStatus: PQCRecommendationStatus;
  currentAlgorithm: string;
  currentPurpose: Purpose;
  recommendedAlgorithm?: string | null;
  alternativeAlgorithms: string[];
  rationale: string;
  migrationPriority: MigrationPriority;
  quantumRisk: QuantumRisk;
  confidence: Confidence;
  considerations: string[];
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
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
  cryptoAssets?: CryptoAsset[];
  inventory?: CryptoInventory;
  cbom: CBOMDocument;
  summary: AnalysisSummary;
}

export interface AnalyzeRequest {
  path: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
