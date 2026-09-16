const fs = require('fs');

const data = JSON.parse(fs.readFileSync('d:/ECDAT/verification_results.json', 'utf8'));
const s1 = data.scenario1.data;

console.log('--- Findings ---');
s1.findings.forEach((f, i) => {
  console.log(`[${i}] ${f.algorithm} (variant: ${f.variant}, keySize: ${f.keySize}) in ${f.file}:${f.line} -> evidence: ${f.evidence}`);
});

console.log('\n--- Risk Assessments ---');
s1.riskAssessments.forEach((r, i) => {
  console.log(`[${i}] ${r.originalFinding.algorithm} (${r.originalFinding.file}:${r.originalFinding.line}) -> Risk: ${r.riskLevel} (score: ${r.riskScore}), Quantum: ${r.quantumRisk}, Mosca: met=${r.quantumRiskResult?.moscaConditionMet}, Urgency: ${r.quantumRiskResult?.migrationUrgency}`);
});

console.log('\n--- PQC Recommendations ---');
s1.pqcRecommendations.forEach((p, i) => {
  console.log(`[${i}] Current: ${p.currentAlgorithm} (${p.currentPurpose}) -> Recommended: ${p.recommendedAlgorithm}, Strategy: ${p.migrationStrategy}, Priority: ${p.migrationPriority}, Status: ${p.recommendationStatus}`);
});

console.log('\n--- CBOM Components ---');
s1.cbom.components.forEach((c, i) => {
  console.log(`[${i}] Name: ${c.name}, Algo: ${c.cryptoProperties?.algorithm}, KeyLen: ${c.cryptoProperties?.keyLength}, Type: ${c.cryptoProperties?.assetType}, File: ${c.evidence?.occurrences?.[0]?.location}`);
});
