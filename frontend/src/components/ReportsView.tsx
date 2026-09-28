import React, { useState } from 'react';
import type { AnalysisResponse } from '../types/analysis';
import { generateExecutiveReportHtml } from '../utils/reportGenerator';

interface ReportsViewProps {
  analysisData: AnalysisResponse | null;
  onNavigateScan?: () => void;
}

interface GeneratedReportItem {
  id: string;
  name: string;
  type: 'Full Analysis' | 'CBOM' | 'Findings';
  format: 'PDF' | 'JSON' | 'CSV';
  generatedAt: string;
  target: string;
  size: string;
  downloadHandler: () => void;
}

export const ReportsView: React.FC<ReportsViewProps> = ({ analysisData, onNavigateScan }) => {
  const [generatingType, setGeneratingType] = useState<'full' | 'cbom' | 'findings' | null>(null);
  const [feedback, setFeedback] = useState<{
    type: 'success' | 'error';
    message: string;
    details?: string;
  } | null>(null);

  // Real report generation history for the active session (starts empty - no fake records!)
  const [recentReports, setRecentReports] = useState<GeneratedReportItem[]>([]);
  const [showPreviewModal, setShowPreviewModal] = useState(false);

  // Dynamic statistics from real AnalysisResponse
  const targetPath = analysisData?.sourcePath || analysisData?.context?.applicationName || 'Target Project';
  const targetName = targetPath ? targetPath.split(/[\\/]/).pop() || targetPath : 'project.zip';
  const totalAssets = analysisData?.cryptoAssets?.length || analysisData?.findings?.length || 0;
  const quantumVulnerable = analysisData?.summary?.quantumHighRiskCount ?? (
    analysisData?.findings?.filter((_, idx) => analysisData?.riskAssessments[idx]?.quantumRisk === 'HIGH').length || 0
  );
  const highCriticalRisk = (analysisData?.summary?.highRiskCount || 0) + (analysisData?.summary?.criticalRiskCount || 0);
  const migrationRequired = analysisData?.pqcRecommendations?.filter(
    (r) => r.recommendationStatus === 'RECOMMENDED' || r.recommendationStatus === 'CONDITIONAL'
  ).length || 0;

  const dateSlug = new Date().toISOString().split('T')[0];
  const timestampFormatted = new Date().toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  // 1. Full Analysis Report Generation (PDF / Executive Document)
  const handleGenerateFullReport = () => {
    if (!analysisData) {
      setFeedback({
        type: 'error',
        message: 'Cannot generate report: No project analysis is currently loaded.',
      });
      return;
    }

    setGeneratingType('full');
    setFeedback(null);

    try {
      const reportHtml = generateExecutiveReportHtml(analysisData);
      const blob = new Blob([reportHtml], { type: 'text/html;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const reportFileName = `ECDAT-Security-Assessment-${targetName.replace(/[^a-zA-Z0-9.-]/g, '_')}-${dateSlug}.pdf`;
      const sizeKb = `${(blob.size / 1024).toFixed(1)} KB`;

      // Trigger print window for direct PDF creation/save
      const printWindow = window.open(url, '_blank');
      if (printWindow) {
        printWindow.onload = () => {
          try {
            printWindow.focus();
            printWindow.print();
          } catch {
            // Window focus/print fallback
          }
        };
      } else {
        // Fallback if popup blocker intercepted: download executive HTML report directly
        const a = document.createElement('a');
        a.href = url;
        a.download = reportFileName.replace('.pdf', '.html');
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      }

      // Add to session report history
      const reportItem: GeneratedReportItem = {
        id: `report-${Date.now()}`,
        name: reportFileName,
        type: 'Full Analysis',
        format: 'PDF',
        generatedAt: timestampFormatted,
        target: targetName,
        size: sizeKb,
        downloadHandler: () => {
          const w = window.open(url, '_blank');
          if (w) {
            w.onload = () => {
              try {
                w.print();
              } catch {
                // Ignore
              }
            };
          }
        },
      };

      setRecentReports((prev) => [reportItem, ...prev]);
      setFeedback({
        type: 'success',
        message: 'Full Analysis Report generated successfully.',
        details: 'Print / Save as PDF document opened. An entry has been added to your session history below.',
      });
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      setFeedback({
        type: 'error',
        message: `Failed to generate Full Analysis Report: ${errorMsg}`,
      });
    } finally {
      setGeneratingType(null);
    }
  };

  // 2. CBOM Export (JSON)
  const handleExportCBOM = () => {
    if (!analysisData?.cbom) {
      setFeedback({
        type: 'error',
        message: 'Cannot export CBOM: No Cryptography Bill of Materials is available in current analysis.',
      });
      return;
    }

    setGeneratingType('cbom');
    setFeedback(null);

    try {
      const jsonStr = JSON.stringify(analysisData.cbom, null, 2);
      const blob = new Blob([jsonStr], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const fileName = `CBOM-${targetName.replace(/[^a-zA-Z0-9.-]/g, '_')}-${dateSlug}.json`;
      const sizeKb = `${(blob.size / 1024).toFixed(1)} KB`;

      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      const reportItem: GeneratedReportItem = {
        id: `cbom-${Date.now()}`,
        name: fileName,
        type: 'CBOM',
        format: 'JSON',
        generatedAt: timestampFormatted,
        target: targetName,
        size: sizeKb,
        downloadHandler: () => {
          const dl = document.createElement('a');
          dl.href = url;
          dl.download = fileName;
          document.body.appendChild(dl);
          dl.click();
          document.body.removeChild(dl);
        },
      };

      setRecentReports((prev) => [reportItem, ...prev]);
      setFeedback({
        type: 'success',
        message: 'CBOM Report exported successfully.',
        details: `Downloaded ${fileName} (${sizeKb}) in CycloneDX 1.6-inspired format.`,
      });
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      setFeedback({
        type: 'error',
        message: `Failed to export CBOM: ${errorMsg}`,
      });
    } finally {
      setGeneratingType(null);
    }
  };

  // 3. Findings Export (CSV)
  const handleExportCSV = () => {
    if (!analysisData?.findings || analysisData.findings.length === 0) {
      setFeedback({
        type: 'error',
        message: 'Cannot export findings: No cryptographic findings present in current analysis.',
      });
      return;
    }

    setGeneratingType('findings');
    setFeedback(null);

    try {
      const headers = [
        'Algorithm',
        'Variant',
        'Purpose',
        'KeySize',
        'RiskLevel',
        'RiskScore',
        'QuantumRisk',
        'Confidence',
        'File',
        'Line',
        'PQCRecommendedTarget',
        'MigrationStrategy',
        'AssetCategory',
        'LifecycleStatus',
        'Library',
      ];

      const rows = analysisData.findings.map((f, idx) => {
        const risk = analysisData.riskAssessments ? analysisData.riskAssessments[idx] : null;
        const pqc = analysisData.pqcRecommendations ? analysisData.pqcRecommendations[idx] : null;
        return [
          `"${f.algorithm || ''}"`,
          `"${f.variant || ''}"`,
          `"${f.purpose || ''}"`,
          f.keySize || '',
          risk?.riskLevel || 'LOW',
          risk?.riskScore || 0,
          risk?.quantumRisk || 'NONE',
          f.confidence || 'HIGH',
          `"${f.file || ''}"`,
          f.line || 0,
          `"${pqc?.recommendedAlgorithm || ''}"`,
          `"${pqc?.migrationStrategy || ''}"`,
          `"${f.assetCategory || ''}"`,
          `"${f.lifecycleStatus || ''}"`,
          `"${f.library || ''}"`,
        ].join(',');
      });

      const csvContent = [headers.join(','), ...rows].join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const fileName = `Findings-${targetName.replace(/[^a-zA-Z0-9.-]/g, '_')}-${dateSlug}.csv`;
      const sizeKb = `${(blob.size / 1024).toFixed(1)} KB`;

      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      const reportItem: GeneratedReportItem = {
        id: `findings-${Date.now()}`,
        name: fileName,
        type: 'Findings',
        format: 'CSV',
        generatedAt: timestampFormatted,
        target: targetName,
        size: sizeKb,
        downloadHandler: () => {
          const dl = document.createElement('a');
          dl.href = url;
          dl.download = fileName;
          document.body.appendChild(dl);
          dl.click();
          document.body.removeChild(dl);
        },
      };

      setRecentReports((prev) => [reportItem, ...prev]);
      setFeedback({
        type: 'success',
        message: 'Findings Report exported successfully.',
        details: `Downloaded ${fileName} (${analysisData.findings.length} findings, ${sizeKb}).`,
      });
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      setFeedback({
        type: 'error',
        message: `Failed to export findings: ${errorMsg}`,
      });
    } finally {
      setGeneratingType(null);
    }
  };

  const isAnyGenerating = generatingType !== null;
  const isAnalysisLoaded = analysisData !== null;

  return (
    <div className="reports-page-layout">
      {/* 1. Page Header */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Reports</h1>
          <p className="view-subtitle">
            Generate and export cryptographic security assessments from the current analysis.
          </p>
        </div>
        <div className="view-actions">
          {isAnalysisLoaded ? (
            <div className="report-header-badge active">
              <span className="report-badge-dot pulse" />
              <span className="font-mono">{targetName}</span>
            </div>
          ) : (
            <div className="report-header-badge disabled">
              <span className="report-badge-dot neutral" />
              <span>No Analysis Active</span>
            </div>
          )}
        </div>
      </div>

      {/* Alert / Feedback Notification */}
      {feedback && (
        <div className={`report-status-alert ${feedback.type}`} role="alert">
          <div className="report-alert-icon">
            {feedback.type === 'success' ? (
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
            ) : (
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            )}
          </div>
          <div className="report-alert-body">
            <strong>{feedback.message}</strong>
            {feedback.details && <p>{feedback.details}</p>}
          </div>
          <button
            className="report-alert-dismiss"
            onClick={() => setFeedback(null)}
            aria-label="Dismiss alert"
          >
            ×
          </button>
        </div>
      )}

      {/* 2. Current Analysis Card */}
      <div className="card-panel report-current-analysis-card">
        <div className="report-current-header">
          <div className="report-current-left">
            <span className="report-section-tag">CURRENT ANALYSIS</span>
            <h2 className="report-current-title font-mono">
              {isAnalysisLoaded ? targetName : 'No Analysis Loaded'}
            </h2>
          </div>
          {isAnalysisLoaded && (
            <div className="report-current-meta">
              {analysisData.context?.applicationName && (
                <span className="report-meta-pill">
                  App: <strong>{analysisData.context.applicationName}</strong>
                </span>
              )}
              {analysisData.context?.businessCriticality && (
                <span className="report-meta-pill">
                  Criticality: <strong>{analysisData.context.businessCriticality}</strong>
                </span>
              )}
              {analysisData.context?.dataSensitivity && (
                <span className="report-meta-pill">
                  Sensitivity: <strong>{analysisData.context.dataSensitivity}</strong>
                </span>
              )}
            </div>
          )}
        </div>

        {isAnalysisLoaded ? (
          <div className="report-metrics-grid">
            <div className="report-metric-tile">
              <span className="report-metric-label">Total Crypto Assets</span>
              <span className="report-metric-value">{totalAssets}</span>
              <span className="report-metric-sub">Discovered cryptographic primitives</span>
            </div>

            <div className="report-metric-tile danger">
              <span className="report-metric-label">Quantum Vulnerable</span>
              <span className="report-metric-value danger-val">{quantumVulnerable}</span>
              <span className="report-metric-sub">Asymmetric algorithms at risk</span>
            </div>

            <div className="report-metric-tile warning">
              <span className="report-metric-label">High / Critical Risk</span>
              <span className="report-metric-value warning-val">{highCriticalRisk}</span>
              <span className="report-metric-sub">Severity assessment score</span>
            </div>

            <div className="report-metric-tile primary">
              <span className="report-metric-label">Migration Required</span>
              <span className="report-metric-value primary-val">{migrationRequired}</span>
              <span className="report-metric-sub">NIST FIPS 203/204 targets</span>
            </div>
          </div>
        ) : (
          <div className="report-empty-analysis-box">
            <div className="report-empty-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="16" x2="12" y2="12" />
                <line x1="12" y1="8" x2="12.01" y2="8" />
              </svg>
            </div>
            <div className="report-empty-content">
              <h4>Run a project analysis to generate reports.</h4>
              <p>
                Reports require active cryptographic findings, Mosca exposure parameters, and PQC migration data. Ingest a project archive or directory to enable live report generation.
              </p>
            </div>
            {onNavigateScan && (
              <button className="btn-primary btn-sm" onClick={onNavigateScan}>
                Go to Scan Project →
              </button>
            )}
          </div>
        )}
      </div>

      {/* 3. Generate Report Section (3 Cards) */}
      <div className="report-section-container">
        <div className="report-section-title-row">
          <div>
            <h2 className="report-section-heading">Generate Report</h2>
            <p className="report-section-desc">
              Select an export format to synthesize cryptographic insights, compliance artifacts, and migration blueprints.
            </p>
          </div>
          {isAnalysisLoaded && (
            <button
              className="btn-secondary btn-sm"
              onClick={() => setShowPreviewModal(true)}
              title="Preview 12-section executive summary report"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <span>Preview Executive Report</span>
            </button>
          )}
        </div>

        <div className="report-generators-grid">
          {/* Card 1: Full Analysis Report */}
          <div className={`card-panel report-generator-card ${generatingType === 'full' ? 'generating' : ''}`}>
            <div className="report-card-top">
              <div className="report-card-icon-pill pdf">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                  <polyline points="10 9 9 9 8 9" />
                </svg>
              </div>
              <span className="report-format-badge pdf font-mono">PDF</span>
            </div>

            <div className="report-card-body">
              <h3 className="report-card-title">Full Analysis Report</h3>
              <p className="report-card-desc">
                Complete cryptographic security assessment including asset inventory, risk findings, quantum exposure, Mosca assessment and PQC migration recommendations.
              </p>

              <div className="report-feature-pills">
                <span className="feature-pill">12 Comprehensive Sections</span>
                <span className="feature-pill">Mosca Assessment</span>
                <span className="feature-pill">NIST PQC Roadmap</span>
                <span className="feature-pill">CBOM Summary</span>
              </div>
            </div>

            <div className="report-card-action">
              <button
                className="btn-primary report-action-btn"
                onClick={handleGenerateFullReport}
                disabled={!isAnalysisLoaded || isAnyGenerating}
              >
                {generatingType === 'full' ? (
                  <>
                    <span className="scan-btn-spinner" />
                    <span>Generating...</span>
                  </>
                ) : (
                  <span>Generate Full Report →</span>
                )}
              </button>
            </div>
          </div>

          {/* Card 2: CBOM Report */}
          <div className={`card-panel report-generator-card ${generatingType === 'cbom' ? 'generating' : ''}`}>
            <div className="report-card-top">
              <div className="report-card-icon-pill json">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="16 18 22 12 16 6" />
                  <polyline points="8 6 2 12 8 18" />
                </svg>
              </div>
              <span className="report-format-badge json font-mono">JSON</span>
            </div>

            <div className="report-card-body">
              <h3 className="report-card-title">CBOM Report</h3>
              <p className="report-card-desc">
                Cryptographic Bill of Materials containing discovered cryptographic components and their security properties.
              </p>

              <div className="report-feature-pills">
                <span className="feature-pill">CycloneDX 1.6-Inspired</span>
                <span className="feature-pill">Crypto Extensions</span>
                <span className="feature-pill">Algorithm Metadata</span>
                <span className="feature-pill">Machine-Readable</span>
              </div>
            </div>

            <div className="report-card-action">
              <button
                className="btn-secondary report-action-btn json-btn"
                onClick={handleExportCBOM}
                disabled={!isAnalysisLoaded || isAnyGenerating}
              >
                {generatingType === 'cbom' ? (
                  <>
                    <span className="scan-btn-spinner" />
                    <span>Generating...</span>
                  </>
                ) : (
                  <span>Export CBOM →</span>
                )}
              </button>
            </div>
          </div>

          {/* Card 3: Findings Report */}
          <div className={`card-panel report-generator-card ${generatingType === 'findings' ? 'generating' : ''}`}>
            <div className="report-card-top">
              <div className="report-card-icon-pill csv">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                  <line x1="3" y1="9" x2="21" y2="9" />
                  <line x1="3" y1="15" x2="21" y2="15" />
                  <line x1="9" y1="3" x2="9" y2="21" />
                  <line x1="15" y1="3" x2="15" y2="21" />
                </svg>
              </div>
              <span className="report-format-badge csv font-mono">CSV</span>
            </div>

            <div className="report-card-body">
              <h3 className="report-card-title">Findings Report</h3>
              <p className="report-card-desc">
                Detailed cryptographic findings including algorithms, purposes, risk classifications and source locations.
              </p>

              <div className="report-feature-pills">
                <span className="feature-pill">Raw Findings Log</span>
                <span className="feature-pill">Source Line Numbers</span>
                <span className="feature-pill">Key Sizes &amp; Variants</span>
                <span className="feature-pill">Spreadsheet Ready</span>
              </div>
            </div>

            <div className="report-card-action">
              <button
                className="btn-secondary report-action-btn csv-btn"
                onClick={handleExportCSV}
                disabled={!isAnalysisLoaded || isAnyGenerating}
              >
                {generatingType === 'findings' ? (
                  <>
                    <span className="scan-btn-spinner" />
                    <span>Generating...</span>
                  </>
                ) : (
                  <span>Export Findings →</span>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 4. Recent Reports (Real Session Data Only) */}
      <div className="card-panel report-history-card">
        <div className="report-history-header">
          <div>
            <h2 className="report-history-title">Recent Reports</h2>
            <p className="report-history-sub">
              Exports generated during the active application session
            </p>
          </div>
          {recentReports.length > 0 && (
            <span className="scan-active-count-tag">
              {recentReports.length} {recentReports.length === 1 ? 'Report' : 'Reports'} Available
            </span>
          )}
        </div>

        {recentReports.length === 0 ? (
          <div className="report-history-empty">
            <div className="history-empty-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="12" y1="18" x2="12" y2="12" />
                <line x1="9" y1="15" x2="15" y2="15" />
              </svg>
            </div>
            <h4 className="history-empty-title">No reports generated yet.</h4>
            <p className="history-empty-sub">
              Generate a report above to make it available here.
            </p>
          </div>
        ) : (
          <div className="table-container">
            <table className="enterprise-table">
              <thead>
                <tr>
                  <th>Report Name</th>
                  <th>Type</th>
                  <th>Format</th>
                  <th>Generated At</th>
                  <th>Target</th>
                  <th>File Size</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {recentReports.map((report) => (
                  <tr key={report.id}>
                    <td>
                      <strong className="algo-text font-mono">{report.name}</strong>
                    </td>
                    <td>
                      <span className="report-type-label">{report.type}</span>
                    </td>
                    <td>
                      <span className={`report-format-badge sm ${report.format.toLowerCase()} font-mono`}>
                        {report.format}
                      </span>
                    </td>
                    <td className="text-muted font-mono" style={{ fontSize: '11px' }}>
                      {report.generatedAt}
                    </td>
                    <td className="text-secondary font-mono" style={{ fontSize: '11px' }}>
                      {report.target}
                    </td>
                    <td className="text-muted font-mono" style={{ fontSize: '11px' }}>
                      {report.size}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        className="btn-secondary btn-sm font-mono"
                        onClick={report.downloadHandler}
                        title={`Re-download ${report.name}`}
                      >
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                          <polyline points="7 10 12 15 17 10" />
                          <line x1="12" y1="15" x2="12" y2="3" />
                        </svg>
                        <span>Download</span>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* 5. Executive Report Preview Modal */}
      {showPreviewModal && analysisData && (
        <div className="drawer-overlay" onClick={() => setShowPreviewModal(false)}>
          <div
            className="card-panel report-preview-modal"
            onClick={(e) => e.stopPropagation()}
            style={{
              maxWidth: '960px',
              width: '90vw',
              maxHeight: '88vh',
              display: 'flex',
              flexDirection: 'column',
              padding: '24px',
              overflow: 'hidden',
              margin: 'auto',
            }}
          >
            <div className="report-modal-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary)' }}>
                  Executive Security Assessment Preview
                </h3>
                <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                  12-Section Cryptographic Posture &amp; Post-Quantum Migration Report
                </p>
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  className="btn-primary btn-sm"
                  onClick={() => {
                    setShowPreviewModal(false);
                    handleGenerateFullReport();
                  }}
                >
                  Print / Save as PDF
                </button>
                <button
                  className="btn-secondary btn-sm"
                  onClick={() => setShowPreviewModal(false)}
                >
                  Close
                </button>
              </div>
            </div>

            <div
              className="report-modal-body"
              style={{
                flex: 1,
                overflowY: 'auto',
                border: '1px solid var(--border-subtle)',
                borderRadius: 'var(--radius-md)',
                backgroundColor: '#ffffff',
                color: '#1e293b',
                padding: '20px',
              }}
              dangerouslySetInnerHTML={{ __html: generateExecutiveReportHtml(analysisData) }}
            />
          </div>
        </div>
      )}
    </div>
  );
};
