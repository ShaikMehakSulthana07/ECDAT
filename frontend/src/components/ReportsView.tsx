import React from 'react';
import type { AnalysisResponse } from '../types/analysis';

interface ReportsViewProps {
  analysisData: AnalysisResponse | null;
}

export const ReportsView: React.FC<ReportsViewProps> = ({ analysisData }) => {
  const handleExportCBOM = () => {
    if (!analysisData?.cbom) return;
    const jsonStr = JSON.stringify(analysisData.cbom, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ecdat-cbom-${analysisData.cbom.serialNumber.replace(/[^a-zA-Z0-9]/g, '_')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleExportCSV = () => {
    if (!analysisData?.findings) return;
    const headers = ['Algorithm', 'Variant', 'Purpose', 'KeySize', 'RiskLevel', 'RiskScore', 'QuantumRisk', 'Confidence', 'File', 'Line', 'PQCRecommended'];
    const rows = analysisData.findings.map((f, idx) => {
      const risk = analysisData.riskAssessments[idx];
      const pqc = analysisData.pqcRecommendations[idx];
      return [
        `"${f.algorithm}"`,
        `"${f.variant || ''}"`,
        `"${f.purpose}"`,
        f.keySize || '',
        risk?.riskLevel || 'LOW',
        risk?.riskScore || 0,
        risk?.quantumRisk || 'NONE',
        f.confidence,
        `"${f.file}"`,
        f.line,
        `"${pqc?.recommendedAlgorithm || ''}"`,
      ].join(',');
    });

    const csvContent = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ecdat-inventory-export-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handlePrintSummary = () => {
    window.print();
  };

  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Security &amp; Governance Reports</h1>
          <p className="view-subtitle">
            Generate and export machine-readable and executive audit reports for cryptographic compliance and PQC migration readiness.
          </p>
        </div>
      </div>

      {!analysisData ? (
        <div className="card-panel" style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-muted)' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '6px' }}>
            No Analysis Data Loaded
          </div>
          <p style={{ fontSize: '12px' }}>
            Run a scan to generate executive reports and export datasets.
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '20px' }}>
          {/* Card 1: Executive Security Summary */}
          <div className="card-panel">
            <h3 className="card-panel-title" style={{ marginBottom: '6px' }}>
              Executive Security Summary
            </h3>
            <p className="card-panel-sub" style={{ marginBottom: '16px' }}>
              Comprehensive executive overview containing risk posture, Mosca quantum inequality calculations, and prioritized migration roadmap.
            </p>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '16px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <div>• Total Cryptographic Primitives: <strong>{analysisData.findings.length}</strong></div>
              <div>• High/Critical Risk Assets: <strong>{(analysisData.summary.criticalRiskCount || 0) + (analysisData.summary.highRiskCount || 0)}</strong></div>
              <div>• Quantum Vulnerable Primitives: <strong>{analysisData.summary.quantumHighRiskCount || 0}</strong></div>
            </div>
            <button className="btn-primary" onClick={handlePrintSummary}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="6 9 6 2 18 2 18 9" />
                <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2" />
                <rect x="6" y="14" width="12" height="8" />
              </svg>
              Print / Save Summary PDF
            </button>
          </div>

          {/* Card 2: Cryptographic Inventory CSV */}
          <div className="card-panel">
            <h3 className="card-panel-title" style={{ marginBottom: '6px' }}>
              Cryptographic Inventory (CSV)
            </h3>
            <p className="card-panel-sub" style={{ marginBottom: '16px' }}>
              Tabular export of all discovered cryptographic primitives, key lengths, declared purposes, file locations, and PQC target mappings.
            </p>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Format: Standard CSV (RFC 4180) · Compatible with Excel, SIEMs, and inventory tooling.
            </div>
            <button className="btn-secondary" onClick={handleExportCSV}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
              Export Inventory CSV
            </button>
          </div>

          {/* Card 3: Cryptography Bill of Materials (CBOM) */}
          <div className="card-panel">
            <h3 className="card-panel-title" style={{ marginBottom: '6px' }}>
              Cryptography Bill of Materials (CBOM JSON)
            </h3>
            <p className="card-panel-sub" style={{ marginBottom: '16px' }}>
              Structured, machine-readable inventory formatted using CycloneDX-inspired cryptographic component extensions.
            </p>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Includes cryptographic properties, detection evidence, risk evaluation, and PQC migration targets.
            </div>
            <button className="btn-primary" onClick={handleExportCBOM}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
              Download CBOM JSON
            </button>
          </div>

          {/* Card 4: Post-Quantum Cryptography Migration Plan */}
          <div className="card-panel">
            <h3 className="card-panel-title" style={{ marginBottom: '6px' }}>
              Post-Quantum Migration Blueprint
            </h3>
            <p className="card-panel-sub" style={{ marginBottom: '16px' }}>
              Direct mapping of classical public-key primitives to NIST FIPS 203, 204, and 205 standardized replacement algorithms.
            </p>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              PQC Migration Required Primitives: <strong>{analysisData.summary.pqcRecommendedCount || 0}</strong>
            </div>
            <button className="btn-secondary" onClick={handlePrintSummary}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="6 9 6 2 18 2 18 9" />
                <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2" />
              </svg>
              Print PQC Migration Blueprint
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
