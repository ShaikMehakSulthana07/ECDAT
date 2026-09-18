import type { AnalysisResponse } from '../types/analysis';

/**
 * Generates a comprehensive, self-contained executive HTML document for the Full Analysis Report.
 * Contains all 12 real sections populated directly from the loaded AnalysisResponse.
 */
export function generateExecutiveReportHtml(analysisData: AnalysisResponse): string {
  const target = analysisData.sourcePath || analysisData.context?.applicationName || 'Target Project';
  const targetName = target.split(/[\\/]/).pop() || target;
  const dateStr = new Date().toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  const findings = analysisData.findings || [];
  const riskAssessments = analysisData.riskAssessments || [];
  const pqcRecommendations = analysisData.pqcRecommendations || [];
  const certificateFindings = analysisData.certificateFindings || [];
  const cbom = analysisData.cbom;
  const context = analysisData.context || {};
  const summary = analysisData.summary || {
    totalFindings: findings.length,
    lowRiskCount: 0,
    mediumRiskCount: 0,
    highRiskCount: 0,
    criticalRiskCount: 0,
    quantumHighRiskCount: 0,
    pqcRecommendedCount: 0,
    pqcConditionalCount: 0,
    pqcNeedsAnalysisCount: 0,
    pqcNotRequiredCount: 0,
  };

  const totalAssets = analysisData.cryptoAssets?.length || findings.length;
  const quantumVulnerableCount = summary.quantumHighRiskCount;
  const highCriticalRiskCount = (summary.highRiskCount || 0) + (summary.criticalRiskCount || 0);
  const migrationRequiredCount = pqcRecommendations.filter(
    (r) => r.recommendationStatus === 'RECOMMENDED' || r.recommendationStatus === 'CONDITIONAL'
  ).length;

  // 4. Algorithm Distribution Map
  const algoCountMap = new Map<string, { count: number; purpose: string; highRisk: number }>();
  findings.forEach((f, idx) => {
    const key = f.algorithm || 'UNKNOWN';
    const risk = riskAssessments[idx];
    const existing = algoCountMap.get(key) || { count: 0, purpose: f.purpose || 'UNKNOWN', highRisk: 0 };
    existing.count += 1;
    if (risk?.riskLevel === 'HIGH' || risk?.riskLevel === 'CRITICAL') {
      existing.highRisk += 1;
    }
    algoCountMap.set(key, existing);
  });
  const algoDistribution = Array.from(algoCountMap.entries()).sort((a, b) => b[1].count - a[1].count);

  // 5. Risk Distribution
  const riskCounts = {
    CRITICAL: summary.criticalRiskCount || findings.filter((_, i) => riskAssessments[i]?.riskLevel === 'CRITICAL').length,
    HIGH: summary.highRiskCount || findings.filter((_, i) => riskAssessments[i]?.riskLevel === 'HIGH').length,
    MEDIUM: summary.mediumRiskCount || findings.filter((_, i) => riskAssessments[i]?.riskLevel === 'MEDIUM').length,
    LOW: summary.lowRiskCount || findings.filter((_, i) => riskAssessments[i]?.riskLevel === 'LOW').length,
  };

  // 6. Quantum Vulnerability
  const quantumVulnerableAlgos = Array.from(
    new Set(
      findings
        .filter((_, i) => riskAssessments[i]?.quantumRisk === 'HIGH')
        .map((f) => f.algorithm)
    )
  );
  const quantumSafeAlgos = Array.from(
    new Set(
      findings
        .filter((_, i) => riskAssessments[i]?.quantumRisk === 'NONE' || riskAssessments[i]?.quantumRisk === 'LOW')
        .map((f) => f.algorithm)
    )
  );

  // 7. Mosca Assessment
  const lifetimeY = context.dataLifetimeYears ?? 10;
  const migrationX = context.migrationTimeYears ?? 3;
  const horizonZ = context.threatHorizonYears ?? 7;
  const totalExposure = migrationX + lifetimeY;
  const isMoscaConditionMet = totalExposure > horizonZ;
  const urgency = isMoscaConditionMet
    ? totalExposure - horizonZ >= 5
      ? 'CRITICAL'
      : 'HIGH'
    : 'LOW';

  // 9. Dependency Findings
  const dependencyFindings = findings.filter(
    (f) => f.sourceType === 'DEPENDENCY' || f.sourceType === 'MAVEN_DEPENDENCY' || f.library
  );

  // HTML Generation
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>ECDAT Cryptographic Security Assessment - ${targetName}</title>
  <style>
    @page {
      size: A4;
      margin: 14mm 12mm;
    }
    *, *::before, *::after {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      font-size: 11pt;
      line-height: 1.45;
      color: #1e293b;
      background: #ffffff;
      padding: 24px;
    }
    .header-banner {
      border-bottom: 2px solid #7c3aed;
      padding-bottom: 16px;
      margin-bottom: 24px;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
    }
    .org-title {
      font-size: 22pt;
      font-weight: 800;
      color: #151627;
      letter-spacing: -0.02em;
    }
    .org-subtitle {
      font-size: 10pt;
      font-weight: 600;
      color: #6366f1;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-top: 2px;
    }
    .meta-box {
      text-align: right;
      font-size: 9pt;
      color: #64748b;
    }
    .meta-target {
      font-weight: 700;
      color: #0f172a;
      font-size: 10pt;
      font-family: monospace;
    }
    .section-block {
      margin-bottom: 22px;
      page-break-inside: avoid;
    }
    .section-title {
      font-size: 13pt;
      font-weight: 700;
      color: #1e1b4b;
      border-bottom: 1px solid #e2e8f0;
      padding-bottom: 4px;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .section-number {
      display: inline-block;
      width: 20px;
      height: 20px;
      background: #7c3aed;
      color: #fff;
      font-size: 8pt;
      font-weight: 700;
      text-align: center;
      line-height: 20px;
      border-radius: 4px;
      margin-right: 6px;
    }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 10px;
      margin-bottom: 16px;
    }
    .kpi-card {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      padding: 10px 12px;
      text-align: center;
    }
    .kpi-label {
      font-size: 8.5pt;
      font-weight: 600;
      color: #64748b;
      text-transform: uppercase;
      margin-bottom: 4px;
    }
    .kpi-value {
      font-size: 18pt;
      font-weight: 800;
      color: #0f172a;
    }
    .kpi-value.danger { color: #dc2626; }
    .kpi-value.warning { color: #d97706; }
    .kpi-value.primary { color: #7c3aed; }
    .kpi-value.success { color: #16a34a; }
    
    .verdict-box {
      background: #faf5ff;
      border: 1px solid #d8b4fe;
      border-left: 4px solid #7c3aed;
      border-radius: 4px;
      padding: 12px;
      margin-bottom: 16px;
      font-size: 10pt;
    }
    .verdict-title {
      font-weight: 700;
      color: #581c87;
      margin-bottom: 4px;
    }
    
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 9pt;
      margin-top: 6px;
    }
    th, td {
      padding: 6px 8px;
      text-align: left;
      border: 1px solid #e2e8f0;
    }
    th {
      background: #f1f5f9;
      font-weight: 700;
      color: #334155;
    }
    tr:nth-child(even) td {
      background: #f8fafc;
    }
    .tag {
      display: inline-block;
      padding: 2px 6px;
      border-radius: 3px;
      font-size: 7.5pt;
      font-weight: 700;
      text-transform: uppercase;
      font-family: monospace;
    }
    .tag-critical { background: #fee2e2; color: #991b1b; }
    .tag-high { background: #ffedd5; color: #9a3412; }
    .tag-medium { background: #fef9c3; color: #854d0e; }
    .tag-low { background: #dcfce7; color: #166534; }
    .tag-purple { background: #f3e8ff; color: #6b21a8; }
    .tag-blue { background: #e0e7ff; color: #3730a3; }
    
    .formula-box {
      background: #f8fafc;
      border: 1px dashed #cbd5e1;
      padding: 10px;
      border-radius: 6px;
      font-family: monospace;
      font-size: 9pt;
      margin: 8px 0;
    }
    .font-mono { font-family: monospace; font-size: 8.5pt; }
    
    .footer-note {
      border-top: 1px solid #e2e8f0;
      margin-top: 30px;
      padding-top: 12px;
      font-size: 8pt;
      color: #94a3b8;
      display: flex;
      justify-content: space-between;
    }
    @media print {
      body { padding: 0; }
      .no-print { display: none; }
    }
  </style>
</head>
<body>

  <!-- HEADER -->
  <div class="header-banner">
    <div>
      <div class="org-title">ECDAT Security Assessment</div>
      <div class="org-subtitle">Enterprise Cryptographic Discovery &amp; Post-Quantum Analysis</div>
    </div>
    <div class="meta-box">
      <div>Target: <span class="meta-target">${escapeHtml(targetName)}</span></div>
      <div>Generated: <strong>${dateStr}</strong></div>
      <div>Spec: <strong>CycloneDX 1.6 / NIST FIPS 203 &amp; 204</strong></div>
    </div>
  </div>

  <!-- 1. EXECUTIVE SUMMARY -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">1</span> Executive Summary</div>
    <div class="verdict-box">
      <div class="verdict-title">${isMoscaConditionMet ? '⚠️ MIGRATION REQUIRED — QUANTUM THREAT HORIZON VIOLATION' : '✅ COMPLIANT — TIMELINE WITHIN TOLERANCE'}</div>
      <div>
        Discovery identified <strong>${totalAssets}</strong> cryptographic assets across target source code, dependencies, and configuration. 
        <strong>${quantumVulnerableCount}</strong> assets are vulnerable to Shor's algorithm (asymmetric public-key cryptography), requiring migration to NIST FIPS 203 (ML-KEM) and FIPS 204 (ML-DSA) standards. 
        ${isMoscaConditionMet ? `Mosca exposure duration (${totalExposure} years) exceeds the quantum threat horizon (${horizonZ} years), resulting in <strong>${urgency}</strong> migration urgency.` : 'No immediate quantum exposure timeline breach detected.'}
      </div>
    </div>

    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-label">Total Assets</div>
        <div class="kpi-value">${totalAssets}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">Quantum Vulnerable</div>
        <div class="kpi-value danger">${quantumVulnerableCount}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">High / Critical Risk</div>
        <div class="kpi-value warning">${highCriticalRiskCount}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">PQC Migration Req.</div>
        <div class="kpi-value primary">${migrationRequiredCount}</div>
      </div>
    </div>
  </div>

  <!-- 2. PROJECT / SCAN INFORMATION -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">2</span> Project &amp; Ingestion Information</div>
    <table>
      <tbody>
        <tr>
          <td style="width: 25%; font-weight: 700; background: #f8fafc;">Application / System</td>
          <td style="width: 25%;">${escapeHtml(context.applicationName || targetName)}</td>
          <td style="width: 25%; font-weight: 700; background: #f8fafc;">Source Path / Archive</td>
          <td style="width: 25%;" class="font-mono">${escapeHtml(analysisData.sourcePath || targetName)}</td>
        </tr>
        <tr>
          <td style="font-weight: 700; background: #f8fafc;">Business Criticality</td>
          <td><span class="tag tag-purple">${escapeHtml(context.businessCriticality || 'CRITICAL')}</span></td>
          <td style="font-weight: 700; background: #f8fafc;">Data Sensitivity</td>
          <td><span class="tag tag-blue">${escapeHtml(context.dataSensitivity || 'HIGHLY_SENSITIVE')}</span></td>
        </tr>
        <tr>
          <td style="font-weight: 700; background: #f8fafc;">Data Lifetime (Y)</td>
          <td>${lifetimeY} years</td>
          <td style="font-weight: 700; background: #f8fafc;">Migration Window (X)</td>
          <td>${migrationX} years</td>
        </tr>
        <tr>
          <td style="font-weight: 700; background: #f8fafc;">Threat Horizon (Z)</td>
          <td>${horizonZ} years</td>
          <td style="font-weight: 700; background: #f8fafc;">Analysis Engine</td>
          <td>ECDAT Java AST &amp; Bytecode Scanner</td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- 3. CRYPTOGRAPHIC ASSET INVENTORY -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">3</span> Cryptographic Asset Inventory Summary</div>
    <p style="font-size: 9.5pt; color: #475569; margin-bottom: 8px;">
      Classification breakdown of cryptographic discovery across direct source code invocations, third-party libraries, and certificates:
    </p>
    <table>
      <thead>
        <tr>
          <th>Category</th>
          <th>Discovered Count</th>
          <th>Percentage</th>
          <th>Risk Profile</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>Direct API Invocations</strong> (JCE / Cipher / Signature)</td>
          <td>${findings.filter((f) => f.sourceType === 'JAVA_SOURCE' || !f.sourceType).length}</td>
          <td>${totalAssets > 0 ? Math.round((findings.filter((f) => f.sourceType === 'JAVA_SOURCE' || !f.sourceType).length / totalAssets) * 100) : 0}%</td>
          <td>Primary code-level exposure</td>
        </tr>
        <tr>
          <td><strong>Dependency &amp; Library Usages</strong> (BouncyCastle, etc.)</td>
          <td>${dependencyFindings.length}</td>
          <td>${totalAssets > 0 ? Math.round((dependencyFindings.length / totalAssets) * 100) : 0}%</td>
          <td>Transitive cryptographic dependencies</td>
        </tr>
        <tr>
          <td><strong>Certificate &amp; Keystore Artifacts</strong></td>
          <td>${certificateFindings.length}</td>
          <td>${totalAssets > 0 ? Math.round((certificateFindings.length / totalAssets) * 100) : 0}%</td>
          <td>PKI &amp; TLS trust anchors</td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- 4. ALGORITHM DISTRIBUTION -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">4</span> Algorithm Distribution</div>
    <table>
      <thead>
        <tr>
          <th>Algorithm</th>
          <th>Primary Purpose</th>
          <th>Occurrences</th>
          <th>% of Total</th>
          <th>High/Crit Risk Count</th>
        </tr>
      </thead>
      <tbody>
        ${algoDistribution.map(([algo, data]) => `
          <tr>
            <td><strong class="font-mono">${escapeHtml(algo)}</strong></td>
            <td>${escapeHtml(data.purpose)}</td>
            <td>${data.count}</td>
            <td>${totalAssets > 0 ? Math.round((data.count / totalAssets) * 100) : 0}%</td>
            <td>${data.highRisk > 0 ? `<span class="tag tag-high">${data.highRisk}</span>` : '<span class="tag tag-low">0</span>'}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  </div>

  <!-- 5. RISK DISTRIBUTION -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">5</span> Risk Classification Distribution</div>
    <table>
      <thead>
        <tr>
          <th>Severity Level</th>
          <th>Finding Count</th>
          <th>Share</th>
          <th>Impact &amp; Recommended Action</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><span class="tag tag-critical">CRITICAL</span></td>
          <td><strong>${riskCounts.CRITICAL}</strong></td>
          <td>${totalAssets > 0 ? Math.round((riskCounts.CRITICAL / totalAssets) * 100) : 0}%</td>
          <td>Immediate remediation required (broken primitives, deprecated key sizes)</td>
        </tr>
        <tr>
          <td><span class="tag tag-high">HIGH</span></td>
          <td><strong>${riskCounts.HIGH}</strong></td>
          <td>${totalAssets > 0 ? Math.round((riskCounts.HIGH / totalAssets) * 100) : 0}%</td>
          <td>Quantum-vulnerable asymmetric algorithms &amp; legacy configurations</td>
        </tr>
        <tr>
          <td><span class="tag tag-medium">MEDIUM</span></td>
          <td><strong>${riskCounts.MEDIUM}</strong></td>
          <td>${totalAssets > 0 ? Math.round((riskCounts.MEDIUM / totalAssets) * 100) : 0}%</td>
          <td>Symmetric algorithms nearing recommended lifetime thresholds (e.g. AES-128)</td>
        </tr>
        <tr>
          <td><span class="tag tag-low">LOW</span></td>
          <td><strong>${riskCounts.LOW}</strong></td>
          <td>${totalAssets > 0 ? Math.round((riskCounts.LOW / totalAssets) * 100) : 0}%</td>
          <td>Modern quantum-resilient primitives (e.g. AES-256, SHA-384, SHA-512)</td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- 6. QUANTUM VULNERABILITY -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">6</span> Quantum Vulnerability Assessment</div>
    <p style="font-size: 9.5pt; color: #475569; margin-bottom: 8px;">
      Vulnerability analysis based on quantum threat vectors (Shor's Algorithm for discrete log/factoring; Grover's Algorithm for symmetric search):
    </p>
    <table>
      <thead>
        <tr>
          <th>Threat Vector</th>
          <th>Vulnerable Algorithms</th>
          <th>Status</th>
          <th>Quantum Threat Impact</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td><strong>Shor's Algorithm</strong> (Asymmetric / PKI)</td>
          <td>${quantumVulnerableAlgos.length > 0 ? quantumVulnerableAlgos.map((a) => `<span class="tag tag-critical font-mono">${escapeHtml(a)}</span>`).join(' ') : '<em>None Detected</em>'}</td>
          <td><span class="tag tag-critical">${quantumVulnerableCount} Assets At Risk</span></td>
          <td>Complete private key recovery &amp; signature forgery by cryptanalytically relevant quantum computer (CRQC).</td>
        </tr>
        <tr>
          <td><strong>Grover's Algorithm</strong> (Symmetric / Hashing)</td>
          <td>${quantumSafeAlgos.length > 0 ? quantumSafeAlgos.map((a) => `<span class="tag tag-low font-mono">${escapeHtml(a)}</span>`).join(' ') : '<em>None Detected</em>'}</td>
          <td><span class="tag tag-low">Resilient</span></td>
          <td>Effective key length halved ($2^{k/2}$). AES-256 and SHA-384 provide sufficient post-quantum security margin.</td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- 7. MOSCA ASSESSMENT -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">7</span> Mosca's Theorem Assessment (X + Y &gt; Z)</div>
    <div class="formula-box">
      <strong>Theorem Formula:</strong> Migration Window (X = ${migrationX}y) + Data Lifetime (Y = ${lifetimeY}y) = Total Exposure (${totalExposure}y)<br/>
      <strong>Adversary Quantum Horizon:</strong> Z = ${horizonZ}y<br/>
      <strong>Condition Status:</strong> ${totalExposure} &gt; ${horizonZ} ⇒ <strong>${isMoscaConditionMet ? 'MOSCA VIOLATION DETECTED' : 'WITHIN TOLERANCE'}</strong>
    </div>
    <p style="font-size: 9.5pt; color: #475569;">
      ${isMoscaConditionMet
      ? `Adversaries executing "Harvest Now, Decrypt Later" (HNDL) attacks can record encrypted communications today and decrypt them when a CRQC arrives in ~${horizonZ} years. Because protected data must remain confidential for ${lifetimeY} years and migration will take ${migrationX} years, migration must commence immediately.`
      : `Current projected migration window (${migrationX}y) combined with data retention (${lifetimeY}y) remains within the estimated quantum horizon (${horizonZ}y).`
    }
    </p>
  </div>

  <!-- 8. CERTIFICATE FINDINGS -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">8</span> Certificate &amp; PKI Findings</div>
    ${certificateFindings.length === 0 ? '<p style="font-size: 9.5pt; color: #64748b;">No standalone certificate or keystore files detected in ingestion scope.</p>' : `
    <table>
      <thead>
        <tr>
          <th>File Name</th>
          <th>Type</th>
          <th>Public Key Algo</th>
          <th>Key Size</th>
          <th>Signature Algo</th>
        </tr>
      </thead>
      <tbody>
        ${certificateFindings.map((c) => `
          <tr>
            <td><strong class="font-mono">${escapeHtml(c.fileName)}</strong></td>
            <td><span class="tag tag-blue font-mono">${escapeHtml(c.certificateType || c.fileType)}</span></td>
            <td>${escapeHtml(c.publicKeyAlgorithm || 'N/A')}</td>
            <td>${c.keySize ? `${c.keySize} bits` : 'N/A'}</td>
            <td>${escapeHtml(c.signatureAlgorithm || 'N/A')}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
    `}
  </div>

  <!-- 9. DEPENDENCY FINDINGS -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">9</span> Cryptographic Library &amp; Dependency Posture</div>
    ${dependencyFindings.length === 0 ? '<p style="font-size: 9.5pt; color: #64748b;">All discovered cryptographic operations originate from direct JDK/standard library providers.</p>' : `
    <table>
      <thead>
        <tr>
          <th>Library / Provider</th>
          <th>Discovered Algorithm</th>
          <th>Location</th>
          <th>Risk Assessment</th>
        </tr>
      </thead>
      <tbody>
        ${dependencyFindings.map((d, idx) => {
      const r = riskAssessments[idx];
      return `
            <tr>
              <td><strong class="font-mono">${escapeHtml(d.library || 'External Dependency')}</strong></td>
              <td class="font-mono">${escapeHtml(d.algorithm)}</td>
              <td class="font-mono">${escapeHtml(d.file)}:${d.line}</td>
              <td><span class="tag tag-${(r?.riskLevel || 'low').toLowerCase()}">${r?.riskLevel || 'LOW'}</span></td>
            </tr>
          `;
    }).join('')}
      </tbody>
    </table>
    `}
  </div>

  <!-- 10. PQC MIGRATION RECOMMENDATIONS -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">10</span> Post-Quantum Cryptography (PQC) Migration Roadmap</div>
    <table>
      <thead>
        <tr>
          <th>Current Algorithm</th>
          <th>Purpose</th>
          <th>NIST PQC Target</th>
          <th>Standard</th>
          <th>Strategy</th>
          <th>Priority</th>
        </tr>
      </thead>
      <tbody>
        ${pqcRecommendations.map((pqc) => `
          <tr>
            <td><strong class="font-mono">${escapeHtml(pqc.currentAlgorithm)}</strong></td>
            <td>${escapeHtml(pqc.currentPurpose)}</td>
            <td><strong class="font-mono" style="color: #7c3aed;">${escapeHtml(pqc.recommendedAlgorithm || 'Needs Analysis')}</strong></td>
            <td><span class="tag tag-purple">${escapeHtml(pqc.recommendedAlgorithm?.includes('KEM') ? 'FIPS 203' : pqc.recommendedAlgorithm?.includes('DSA') ? 'FIPS 204' : 'NIST PQC')}</span></td>
            <td><span class="tag tag-blue">${escapeHtml(pqc.migrationStrategy || 'DIRECT_PQC')}</span></td>
            <td><span class="tag tag-${(pqc.migrationPriority || 'medium').toLowerCase()}">${escapeHtml(pqc.migrationPriority || 'MEDIUM')}</span></td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  </div>

  <!-- 11. DETAILED FINDINGS TABLE -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">11</span> Detailed Cryptographic Findings (${findings.length} Total)</div>
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Algorithm</th>
          <th>Purpose</th>
          <th>Key Size</th>
          <th>Risk</th>
          <th>Quantum</th>
          <th>Source Location</th>
        </tr>
      </thead>
      <tbody>
        ${findings.map((f, i) => {
      const r = riskAssessments[i];
      return `
            <tr>
              <td>${i + 1}</td>
              <td><strong class="font-mono">${escapeHtml(f.algorithm)}</strong> ${f.variant ? `<small>(${escapeHtml(f.variant)})</small>` : ''}</td>
              <td>${escapeHtml(f.purpose)}</td>
              <td>${f.keySize ? `${f.keySize}b` : '-'}</td>
              <td><span class="tag tag-${(r?.riskLevel || 'low').toLowerCase()}">${r?.riskLevel || 'LOW'}</span></td>
              <td>${r?.quantumRisk === 'HIGH' ? '<span class="tag tag-critical">HIGH</span>' : '<span class="tag tag-low">NONE</span>'}</td>
              <td class="font-mono" style="font-size: 8pt;">${escapeHtml(f.file)}:${f.line}</td>
            </tr>
          `;
    }).join('')}
      </tbody>
    </table>
  </div>

  <!-- 12. CBOM SUMMARY -->
  <div class="section-block">
    <div class="section-title"><span class="section-number">12</span> Cryptography Bill of Materials (CBOM) Summary</div>
    <table>
      <tbody>
        <tr>
          <td style="width: 25%; font-weight: 700; background: #f8fafc;">BOM Format</td>
          <td style="width: 25%;" class="font-mono">${escapeHtml(cbom?.bomFormat || 'CycloneDX')}</td>
          <td style="width: 25%; font-weight: 700; background: #f8fafc;">Spec Version</td>
          <td style="width: 25%;" class="font-mono">${escapeHtml(cbom?.specVersion || '1.6')}</td>
        </tr>
        <tr>
          <td style="font-weight: 700; background: #f8fafc;">Serial Number</td>
          <td class="font-mono" style="font-size: 8pt;">${escapeHtml(cbom?.serialNumber || 'urn:uuid:ecdat-cbom')}</td>
          <td style="font-weight: 700; background: #f8fafc;">Tool / Vendor</td>
          <td>${escapeHtml(cbom?.metadata?.tool?.vendor || 'ECDAT')} (${escapeHtml(cbom?.metadata?.tool?.name || 'Scanner')})</td>
        </tr>
        <tr>
          <td style="font-weight: 700; background: #f8fafc;">Components Cataloged</td>
          <td><strong>${cbom?.components?.length || totalAssets}</strong> cryptographic components</td>
          <td style="font-weight: 700; background: #f8fafc;">Cryptographic Properties</td>
          <td>Algorithms, Key Sizes, Risk Annotations, PQC Mappings</td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- FOOTER -->
  <div class="footer-note">
    <div>ECDAT Enterprise Cryptographic Discovery &amp; Analysis Tool</div>
    <div>Confidential Security Report — For Authorized Internal Use Only</div>
  </div>

</body>
</html>`;
}

function escapeHtml(str?: string | null): string {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
