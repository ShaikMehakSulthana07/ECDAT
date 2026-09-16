const fs = require('fs');
const path = require('path');

async function run() {
  const results = {};

  console.log('==============================================');
  console.log('RUNNING COMPLETE ECDAT SYSTEM VERIFICATION');
  console.log('==============================================');

  const BASE_URL = 'http://localhost:8080';

  // Helper to send multipart zip upload
  async function uploadZip(zipPath, context) {
    const fileData = fs.readFileSync(zipPath);
    const blob = new Blob([fileData], { type: 'application/zip' });
    const formData = new FormData();
    formData.append('file', blob, path.basename(zipPath));
    if (context) {
      if (context.applicationName) formData.append('applicationName', context.applicationName);
      if (context.businessCriticality) formData.append('businessCriticality', context.businessCriticality);
      if (context.dataSensitivity) formData.append('dataSensitivity', context.dataSensitivity);
      if (context.dataLifetimeYears !== undefined) formData.append('dataLifetimeYears', context.dataLifetimeYears.toString());
      if (context.migrationTimeYears !== undefined) formData.append('migrationTimeYears', context.migrationTimeYears.toString());
      if (context.threatHorizonYears !== undefined) formData.append('threatHorizonYears', context.threatHorizonYears.toString());
    }

    const res = await fetch(`${BASE_URL}/api/analyze/upload`, {
      method: 'POST',
      body: formData
    });
    const status = res.status;
    let json = null;
    try {
      json = await res.json();
    } catch (e) {
      json = await res.text();
    }
    return { status, data: json };
  }

  // 1. Health check
  const healthRes = await fetch(`${BASE_URL}/api/health`);
  const healthJson = await healthRes.json();
  results.health = { status: healthRes.status, body: healthJson };
  console.log('1. Health check:', healthJson);

  // 2. Scenario 1 (Mosca Vulnerable: 10 + 3 = 13 > 10)
  const sc1 = await uploadZip('d:/ECDAT/test-target.zip', {
    applicationName: 'Test Target App',
    businessCriticality: 'CRITICAL',
    dataSensitivity: 'HIGHLY_SENSITIVE',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10
  });
  results.scenario1 = sc1;
  console.log('2. Scenario 1 Status:', sc1.status);
  console.log('   Total Findings:', sc1.data.findings?.length);
  console.log('   Risk Assessments:', sc1.data.riskAssessments?.length);
  console.log('   PQC Recommendations:', sc1.data.recommendations?.length);

  // 3. Scenario 2 (Mosca Safe: 3 + 2 = 5 <= 10)
  const sc2 = await uploadZip('d:/ECDAT/test-target.zip', {
    applicationName: 'Test Target App',
    businessCriticality: 'CRITICAL',
    dataSensitivity: 'HIGHLY_SENSITIVE',
    dataLifetimeYears: 3,
    migrationTimeYears: 2,
    threatHorizonYears: 10
  });
  results.scenario2 = sc2;
  console.log('3. Scenario 2 Status:', sc2.status);

  // 4. Scenario 3 (Mosca Critical Boundary: 5 + 5 = 10 == 10)
  const sc3 = await uploadZip('d:/ECDAT/test-target.zip', {
    applicationName: 'Test Target App',
    businessCriticality: 'CRITICAL',
    dataSensitivity: 'HIGHLY_SENSITIVE',
    dataLifetimeYears: 5,
    migrationTimeYears: 5,
    threatHorizonYears: 10
  });
  results.scenario3 = sc3;
  console.log('4. Scenario 3 Status:', sc3.status);

  // 5. Scenario A (Low Criticality & Public Sensitivity)
  const scA = await uploadZip('d:/ECDAT/test-target.zip', {
    applicationName: 'Low Crit App',
    businessCriticality: 'LOW',
    dataSensitivity: 'PUBLIC',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10
  });
  results.scenarioA = scA;
  console.log('5. Scenario A (Low / Public) Status:', scA.status);

  // 6. Project A (RSA + AES) vs Project B (ECDSA + ECDH)
  const projA = await uploadZip('d:/ECDAT/projectA.zip', {
    applicationName: 'Project A',
    businessCriticality: 'HIGH',
    dataSensitivity: 'CONFIDENTIAL',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10
  });
  const projB = await uploadZip('d:/ECDAT/projectB.zip', {
    applicationName: 'Project B',
    businessCriticality: 'HIGH',
    dataSensitivity: 'CONFIDENTIAL',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10
  });
  results.projectA = projA;
  results.projectB = projB;
  console.log('6. Project A Findings:', projA.data.findings?.map(f => f.algorithm));
  console.log('   Project B Findings:', projB.data.findings?.map(f => f.algorithm));

  // 7. Malicious Zip (Path Traversal)
  const malRes = await uploadZip('d:/ECDAT/malicious.zip', {
    applicationName: 'Malicious',
    businessCriticality: 'LOW',
    dataSensitivity: 'PUBLIC'
  });
  results.maliciousZip = malRes;
  console.log('7. Malicious Zip Rejection Status:', malRes.status, malRes.data);

  // 8. Too many entries Zip
  const entriesRes = await uploadZip('d:/ECDAT/too_many_entries.zip', {
    applicationName: 'Bomb',
    businessCriticality: 'LOW',
    dataSensitivity: 'PUBLIC'
  });
  results.tooManyEntriesZip = entriesRes;
  console.log('8. Too Many Entries Zip Rejection Status:', entriesRes.status, entriesRes.data);

  // 9. Path endpoint security
  const pathRes = await fetch(`${BASE_URL}/api/analyze`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      targetDirectory: 'C:/Windows/System32',
      projectContext: { applicationName: 'Unauthorized' }
    })
  });
  let pathData = null;
  try { pathData = await pathRes.json(); } catch(e) { pathData = await pathRes.text(); }
  results.pathEndpointSecurity = { status: pathRes.status, data: pathData };
  console.log('9. Path Endpoint Security Status:', pathRes.status, pathData);

  fs.writeFileSync('d:/ECDAT/verification_results.json', JSON.stringify(results, null, 2));
  console.log('==============================================');
  console.log('VERIFICATION COMPLETE. Saved to verification_results.json');
  console.log('==============================================');
}

run().catch(console.error);
