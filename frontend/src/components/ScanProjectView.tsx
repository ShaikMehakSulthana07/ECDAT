import React, { useState, useRef } from 'react';
import type { BusinessCriticality, DataSensitivity, ProjectAnalysisContext } from '../types/analysis';

interface ScanProjectViewProps {
  onScanPath: (path: string, context: ProjectAnalysisContext) => void;
  onScanFile: (file: File, context: ProjectAnalysisContext) => void;
  isLoading: boolean;
  activeTarget?: string;
}

export const ScanProjectView: React.FC<ScanProjectViewProps> = ({
  onScanPath,
  onScanFile,
  isLoading,
  activeTarget,
}) => {
  const [mode, setMode] = useState<'upload' | 'path'>('upload');
  const [pathInput, setPathInput] = useState<string>('../test-target');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [context, setContext] = useState<ProjectAnalysisContext>({
    applicationName: 'Enterprise Payment Gateway',
    businessCriticality: 'CRITICAL',
    dataSensitivity: 'HIGHLY_SENSITIVE',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10,
  });

  const handlePathSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!pathInput.trim() || isLoading) return;
    onScanPath(pathInput.trim(), context);
  };

  const handleFileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile || isLoading) return;
    onScanFile(selectedFile, context);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (file.name.endsWith('.zip')) {
        setSelectedFile(file);
      }
    }
  };

  return (
    <div className="scan-container">
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Scan Project</h1>
          <p className="view-subtitle">
            Upload a project archive to discover cryptographic assets, dependencies, certificates, and quantum migration risks.
          </p>
        </div>
      </div>

      {isLoading ? (
        <div className="progress-stepper">
          <div className="stepper-header">
            <span className="spinner"></span>
            <span>Executing Enterprise Cryptographic Discovery Pipeline...</span>
            <span className="font-mono text-muted" style={{ fontSize: '12px', marginLeft: 'auto' }}>
              Target: {activeTarget}
            </span>
          </div>

          <div className="stepper-steps">
            <div className="stepper-step active">
              <div className="step-indicator">1</div>
              <span>Upload &amp; Safe Isolated Archive Extraction</span>
            </div>
            <div className="stepper-step active">
              <div className="step-indicator">2</div>
              <span>AST Source Analysis (JCA/JCE Primitives, Keys, Signatures)</span>
            </div>
            <div className="stepper-step active">
              <div className="step-indicator">3</div>
              <span>Dependency Discovery (Maven POM Cryptographic Libraries)</span>
            </div>
            <div className="stepper-step active">
              <div className="step-indicator">4</div>
              <span>Certificate &amp; Cryptographic Artifact Scanning</span>
            </div>
            <div className="stepper-step active">
              <div className="step-indicator">5</div>
              <span>Quantum Risk &amp; Mosca Theorem Assessment</span>
            </div>
            <div className="stepper-step active">
              <div className="step-indicator">6</div>
              <span>Post-Quantum Cryptography (PQC) Recommendation Engine</span>
            </div>
            <div className="stepper-step active">
              <div className="step-indicator">7</div>
              <span>ECDAT Cryptography Bill of Materials (CBOM) Generation</span>
            </div>
          </div>
        </div>
      ) : (
        <div className="scan-card">
          <div className="scan-mode-tabs">
            <button
              type="button"
              className={`scan-tab-btn ${mode === 'upload' ? 'active' : ''}`}
              onClick={() => setMode('upload')}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="9" />
              </svg>
              Project Archive (.ZIP)
            </button>
            <button
              type="button"
              className={`scan-tab-btn ${mode === 'path' ? 'active' : ''}`}
              onClick={() => setMode('path')}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
              </svg>
              Directory Path Analysis
            </button>
          </div>

          {mode === 'upload' ? (
            <form onSubmit={handleFileSubmit}>
              <div
                className={`dropzone-box ${isDragOver ? 'drag-over' : ''}`}
                onDragOver={(e) => {
                  e.preventDefault();
                  setIsDragOver(true);
                }}
                onDragLeave={() => setIsDragOver(false)}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
              >
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".zip"
                  style={{ display: 'none' }}
                  onChange={handleFileChange}
                />
                <div className="dropzone-icon">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="17 8 12 3 7 8" />
                    <line x1="12" y1="3" x2="12" y2="9" />
                  </svg>
                </div>
                {selectedFile ? (
                  <div>
                    <div className="dropzone-primary-text font-bold font-mono">{selectedFile.name}</div>
                    <div className="dropzone-subtext">{(selectedFile.size / 1024).toFixed(1)} KB · Ready to scan</div>
                  </div>
                ) : (
                  <div>
                    <div className="dropzone-primary-text">Drag and drop project ZIP here, or <strong>Browse files</strong></div>
                    <div className="dropzone-subtext">Accepted archive format: <code>.zip</code> (Java source or multi-module repo)</div>
                  </div>
                )}
              </div>

              {/* Security Information Box */}
              <div className="security-assurance-box">
                <div className="security-assurance-title">Security &amp; Isolation Assurance</div>
                <ul className="security-assurance-list">
                  <li>Archive extraction is isolated in sandboxed temporary workspace.</li>
                  <li>Path traversal (Zip Slip) protection enabled.</li>
                  <li>Maximum archive entry count &amp; uncompressed size limits enforced.</li>
                </ul>
              </div>

              {/* Context Toggle */}
              <div className="context-config-panel">
                <button
                  type="button"
                  className="context-toggle-btn"
                  onClick={() => setShowAdvanced(!showAdvanced)}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="12" cy="12" r="3" />
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
                  </svg>
                  {showAdvanced ? 'Hide' : 'Configure'} Project Context &amp; Mosca Parameters
                </button>

                {showAdvanced && (
                  <div className="context-grid">
                    <div className="form-group">
                      <label className="form-label">Application Name</label>
                      <input
                        type="text"
                        className="form-input"
                        value={context.applicationName}
                        onChange={(e) => setContext({ ...context, applicationName: e.target.value })}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Business Criticality</label>
                      <select
                        className="form-input"
                        value={context.businessCriticality}
                        onChange={(e) => setContext({ ...context, businessCriticality: e.target.value as BusinessCriticality })}
                      >
                        <option value="CRITICAL">Critical</option>
                        <option value="HIGH">High</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="LOW">Low</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Data Sensitivity</label>
                      <select
                        className="form-input"
                        value={context.dataSensitivity}
                        onChange={(e) => setContext({ ...context, dataSensitivity: e.target.value as DataSensitivity })}
                      >
                        <option value="HIGHLY_SENSITIVE">Highly Sensitive</option>
                        <option value="CONFIDENTIAL">Confidential</option>
                        <option value="INTERNAL">Internal</option>
                        <option value="PUBLIC">Public</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label className="form-label">Data Lifetime (years, Y)</label>
                      <input
                        type="number"
                        min="1"
                        max="50"
                        className="form-input"
                        value={context.dataLifetimeYears}
                        onChange={(e) => setContext({ ...context, dataLifetimeYears: parseInt(e.target.value) || 10 })}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Migration Time (years, X)</label>
                      <input
                        type="number"
                        min="1"
                        max="20"
                        className="form-input"
                        value={context.migrationTimeYears}
                        onChange={(e) => setContext({ ...context, migrationTimeYears: parseInt(e.target.value) || 3 })}
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Threat Horizon (years, Z)</label>
                      <input
                        type="number"
                        min="1"
                        max="30"
                        className="form-input"
                        value={context.threatHorizonYears}
                        onChange={(e) => setContext({ ...context, threatHorizonYears: parseInt(e.target.value) || 10 })}
                      />
                    </div>
                  </div>
                )}
              </div>

              <div style={{ marginTop: '24px', display: 'flex', justifyContent: 'flex-end' }}>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={!selectedFile || isLoading}
                  style={{ padding: '10px 20px', fontSize: '13px' }}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polygon points="5 3 19 12 5 21 5 3" />
                  </svg>
                  Upload &amp; Analyze Archive
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handlePathSubmit}>
              <div className="form-group">
                <label className="form-label" htmlFor="target-path">
                  Filesystem Directory Path
                </label>
                <input
                  id="target-path"
                  type="text"
                  className="form-input font-mono"
                  placeholder="e.g. ../test-target or d:/ECDAT/test-target"
                  value={pathInput}
                  onChange={(e) => setPathInput(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '10px' }}>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Quick Presets:</span>
                <button
                  type="button"
                  className="btn-secondary btn-sm font-mono"
                  onClick={() => setPathInput('../test-target')}
                >
                  ../test-target
                </button>
                <button
                  type="button"
                  className="btn-secondary btn-sm font-mono"
                  onClick={() => setPathInput('../test-target/src/main/java/demo')}
                >
                  ../test-target/src/main/java/demo
                </button>
              </div>

              <div style={{ marginTop: '24px', display: 'flex', justifyContent: 'flex-end' }}>
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={!pathInput.trim() || isLoading}
                  style={{ padding: '10px 20px', fontSize: '13px' }}
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polygon points="5 3 19 12 5 21 5 3" />
                  </svg>
                  Run Directory Analysis
                </button>
              </div>
            </form>
          )}
        </div>
      )}
    </div>
  );
};
