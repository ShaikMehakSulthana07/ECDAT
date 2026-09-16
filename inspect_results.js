const fs = require('fs');

const data = JSON.parse(fs.readFileSync('d:/ECDAT/verification_results.json', 'utf8'));

console.log('================================================================');
console.log('1. SCENARIO 1 (VULNERABLE: 10 + 3 = 13 > 10)');
console.log('================================================================');
const s1 = data.scenario1.data;
console.log('Context:', s1.context);
console.log('Summary:', s1.summary);
console.log('Risk Assessments count:', s1.riskAssessments.length);
console.log('Sample Risk Assessment:', s1.riskAssessments[0]);

console.log('\n================================================================');
console.log('2. SCENARIO 2 (SAFE: 3 + 2 = 5 <= 10)');
console.log('================================================================');
const s2 = data.scenario2.data;
console.log('Context:', s2.context);
console.log('Summary:', s2.summary);
console.log('Sample Risk Assessment:', s2.riskAssessments[0]);

console.log('\n================================================================');
console.log('3. SCENARIO 3 (CRITICAL BOUNDARY: 5 + 5 = 10 == 10)');
console.log('================================================================');
const s3 = data.scenario3.data;
console.log('Context:', s3.context);
console.log('Summary:', s3.summary);
console.log('Sample Risk Assessment:', s3.riskAssessments[0]);

console.log('\n================================================================');
console.log('4. BUSINESS CRITICALITY & SENSITIVITY PROPAGATION (SCENARIO A vs B)');
console.log('================================================================');
const scA = data.scenarioA.data;
console.log('Scenario A Context:', scA.context);
console.log('Scenario A Sample Risk:', scA.riskAssessments[0].riskScore, scA.riskAssessments[0].riskLevel);
console.log('Scenario 1 (Scenario B) Sample Risk:', s1.riskAssessments[0].riskScore, s1.riskAssessments[0].riskLevel);

console.log('\n================================================================');
console.log('5. CERTIFICATE DISCOVERY FINDINGS');
console.log('================================================================');
console.log('Certificate Findings count:', s1.certificateFindings?.length);
console.log('Certificate Findings:', JSON.stringify(s1.certificateFindings, null, 2));

console.log('\n================================================================');
console.log('6. PQC RECOMMENDATIONS & HYBRID MIGRATION');
console.log('================================================================');
console.log('PQC Recommendations count:', s1.pqcRecommendations?.length);
s1.pqcRecommendations.forEach((rec, idx) => {
  console.log(`[Rec ${idx+1}] Algo: ${rec.currentAlgorithm} | Purpose: ${rec.purpose} | Target: ${rec.recommendedAlgorithm} | Strategy: ${rec.migrationStrategy} | Conf: ${rec.confidence} | Reason: ${rec.reason}`);
});

console.log('\n================================================================');
console.log('7. DYNAMIC ALGORITHM DETECTION');
console.log('================================================================');
const dynamicFindings = s1.findings.filter(f => f.algorithm.includes('DYNAMIC') || f.algorithm.includes('UNKNOWN') || f.confidence === 'LOW' || f.evidence.includes('getInstance(algo)'));
console.log('Dynamic / Low-Confidence Findings:', JSON.stringify(dynamicFindings, null, 2));

console.log('\n================================================================');
console.log('8. FILE / LINE TRACEABILITY FOR RSA-2048');
console.log('================================================================');
const rsaFinding = s1.findings.find(f => f.algorithm === 'RSA' && f.keySize === 2048);
console.log('RSA Finding:', rsaFinding);
if (rsaFinding) {
  const rsaRisk = s1.riskAssessments.find(r => r.findingId === rsaFinding.id);
  const rsaPqc = s1.pqcRecommendations.find(p => p.findingId === rsaFinding.id);
  const rsaCbom = s1.cbom.components.find(c => c.cryptoProperties?.algorithm === 'RSA' && c.cryptoProperties?.keyLength === 2048);
  console.log('-> Risk Assessment:', rsaRisk);
  console.log('-> PQC Recommendation:', rsaPqc);
  console.log('-> CBOM Component:', JSON.stringify(rsaCbom, null, 2));
}

console.log('\n================================================================');
console.log('9. PROJECT A vs PROJECT B COMPARISON');
console.log('================================================================');
console.log('Project A findings:', data.projectA.data.findings.map(f => `${f.algorithm} (${f.filePath}:${f.lineNumber})`));
console.log('Project A CBOM components:', data.projectA.data.cbom.components.map(c => c.name));
console.log('Project B findings:', data.projectB.data.findings.map(f => `${f.algorithm} (${f.filePath}:${f.lineNumber})`));
console.log('Project B CBOM components:', data.projectB.data.cbom.components.map(c => c.name));
