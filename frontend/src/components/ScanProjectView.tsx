import React, { useState, useRef } from 'react';
import type { ProjectAnalysisContext, BusinessCriticality, DataSensitivity } from '../types/analysis';

interface ScanProjectViewProps {
  onScanPath: (path: string, context: ProjectAnalysisContext) => void;
  onScanFile: (file: File, context: ProjectAnalysisContext) => void;
  isLoading: boolean;
  activeTarget?: string;
}

type SourceType = 'upload' | 'repository' | 'binaries' | 'container' | 'path';

export const ScanProjectView: React.FC<ScanProjectViewProps> = ({
  onScanPath,
  onScanFile,
  isLoading,
  activeTarget,
}) => {
  const [selectedSource, setSelectedSource] = useState<SourceType>('upload');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [repoUrl, setRepoUrl] = useState('');
  const [pathInput, setPathInput] = useState<string>('../test-target');
  const [uploadSubTab, setUploadSubTab] = useState<'archive' | 'directory'>('archive');
  const [validationError, setValidationError] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const [scope, setScope] = useState({
    cryptoApis: true,
    dependencies: true,
    certificates: true,
    quantumRisk: true,
    pqcMigration: true,
    cbom: true,
  });

  const [context, setContext] = useState<ProjectAnalysisContext>({
    applicationName: 'Enterprise Payment Gateway',
    businessCriticality: 'CRITICAL',
    dataSensitivity: 'HIGHLY_SENSITIVE',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10,
  });

  const toggleScope = (key: keyof typeof scope) => {
    setScope((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setSelectedFile(file);
      setValidationError(null);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      if (file.name.endsWith('.zip') || file.name.endsWith('.tar') || file.name.endsWith('.tar.gz')) {
        setSelectedFile(file);
        setValidationError(null);
      } else {
        setValidationError('Please upload a valid ZIP or TAR project archive.');
      }
    }
  };

  const handleRemoveFile = (e: React.MouseEvent) => {
    e.stopPropagation();
    setSelectedFile(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const handleStartAnalysis = (e: React.FormEvent) => {
    e.preventDefault();
    setValidationError(null);

    if (selectedSource === 'upload') {
      if (uploadSubTab === 'archive') {
        if (!selectedFile) {
          setValidationError('Please choose a ZIP project archive before starting analysis.');
          fileInputRef.current?.click();
          return;
        }
        onScanFile(selectedFile, context);
      } else {
        if (!pathInput.trim()) {
          setValidationError('Please enter a valid filesystem directory path.');
          return;
        }
        onScanPath(pathInput.trim(), context);
      }
    } else if (selectedSource === 'repository') {
      setValidationError('Git repository analysis requires backend service integration (Roadmap feature — Phase 5).');
    } else if (selectedSource === 'binaries') {
      setValidationError('Binary & artifact analysis is in active development (Roadmap feature — Phase 6/7).');
    } else if (selectedSource === 'container') {
      setValidationError('Container image inspection requires container daemon integration (Roadmap feature — Phase 8).');
    }
  };

  const isFormValid =
    selectedSource === 'upload' &&
    ((uploadSubTab === 'archive' && selectedFile !== null) ||
      (uploadSubTab === 'directory' && pathInput.trim().length > 0));

  return (
    <div className="scan-page-layout">
      {/* 1. PAGE HEADER */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Scan Project</h1>
          <p className="view-subtitle">
            Discover cryptographic artefacts across source code, binaries, configuration, libraries and container images.
          </p>
        </div>
        <div className="view-actions">
          <div className="scan-header-badge">
            <span className="scan-badge-dot"></span>
            <span>Engine Ready</span>
          </div>
        </div>
      </div>

      {isLoading ? (
        /* 7. FUTURE SCAN PROGRESS DESIGN */
        <div className="scan-progress-container">
          <div className="card-panel scan-progress-card">
            <div className="scan-progress-header">
              <div className="scan-progress-title-wrap">
                <div className="scan-spinner-enterprise"></div>
                <div>
                  <h3 className="scan-progress-title">ANALYSIS IN PROGRESS</h3>
                  <p className="scan-progress-subtitle">
                    Target: <code>{activeTarget || (selectedFile ? selectedFile.name : 'Project Archive')}</code>
                  </p>
                </div>
              </div>
              <div className="scan-progress-status-pill">Executing Pipeline</div>
            </div>

            <div className="scan-progress-bar-track">
              <div className="scan-progress-bar-fill"></div>
            </div>

            <div className="scan-steps-list">
              <div className="scan-step-item completed">
                <span className="scan-step-icon">✓</span>
                <div className="scan-step-info">
                  <span className="scan-step-name">Source Discovery</span>
                  <span className="scan-step-desc">Archive uncompressed and file tree indexed</span>
                </div>
                <span className="scan-step-status">Completed</span>
              </div>

              <div className="scan-step-item active">
                <span className="scan-step-icon">◉</span>
                <div className="scan-step-info">
                  <span className="scan-step-name">Cryptographic API Analysis</span>
                  <span className="scan-step-desc">Extracting Java AST primitives, algorithms, and key sizes</span>
                </div>
                <span className="scan-step-status">In Progress</span>
              </div>

              <div className="scan-step-item pending">
                <span className="scan-step-icon">○</span>
                <div className="scan-step-info">
                  <span className="scan-step-name">Dependency Analysis</span>
                  <span className="scan-step-desc">Resolving Maven pom.xml declarations and cryptographic libraries</span>
                </div>
                <span className="scan-step-status">Queued</span>
              </div>

              <div className="scan-step-item pending">
                <span className="scan-step-icon">○</span>
                <div className="scan-step-info">
                  <span className="scan-step-name">Certificate Analysis</span>
                  <span className="scan-step-desc">Parsing X.509 certificates and public-key properties</span>
                </div>
                <span className="scan-step-status">Queued</span>
              </div>

              <div className="scan-step-item pending">
                <span className="scan-step-icon">○</span>
                <div className="scan-step-info">
                  <span className="scan-step-name">Quantum Risk Assessment</span>
                  <span className="scan-step-desc">Calculating Mosca exposure conditions and Shor vulnerability</span>
                </div>
                <span className="scan-step-status">Queued</span>
              </div>

              <div className="scan-step-item pending">
                <span className="scan-step-icon">○</span>
                <div className="scan-step-info">
                  <span className="scan-step-name">PQC Recommendations</span>
                  <span className="scan-step-desc">Synthesizing NIST FIPS 203/204/205 post-quantum migration paths</span>
                </div>
                <span className="scan-step-status">Queued</span>
              </div>

              <div className="scan-step-item pending">
                <span className="scan-step-icon">○</span>
                <div className="scan-step-info">
                  <span className="scan-step-name">CBOM Generation</span>
                  <span className="scan-step-desc">Compiling CycloneDX 1.6 Cryptographic Bill of Materials</span>
                </div>
                <span className="scan-step-status">Queued</span>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <form onSubmit={handleStartAnalysis} className="scan-form-layout">
          {validationError && (
            <div className="scan-validation-alert" role="alert">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              <span>{validationError}</span>
              <button
                type="button"
                className="scan-alert-dismiss"
                onClick={() => setValidationError(null)}
                aria-label="Dismiss alert"
              >
                ✕
              </button>
            </div>
          )}

          {/* 2. ANALYSIS SOURCE SECTION */}
          <div className="card-panel scan-section-card">
            <div className="scan-section-header">
              <div>
                <h2 className="scan-section-title">Analysis Source</h2>
                <p className="scan-section-subtitle">
                  Select the ingestion target for cryptographic discovery and posture analysis
                </p>
              </div>
              <span className="scan-active-count-tag">
                Active Source: {selectedSource === 'upload' ? (uploadSubTab === 'archive' ? 'ZIP Upload' : 'Directory Path') : selectedSource.toUpperCase()}
              </span>
            </div>

            {/* 2x2 Grid of Source Options */}
            <div className="source-cards-grid">
              {/* CARD 1: Upload Project */}
              <div
                className={`source-card ${selectedSource === 'upload' ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedSource('upload');
                  setValidationError(null);
                }}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    setSelectedSource('upload');
                  }
                }}
              >
                <div className="source-card-top">
                  <div className="source-card-icon-pill primary">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                      <polyline points="17 8 12 3 7 8" />
                      <line x1="12" y1="3" x2="12" y2="15" />
                    </svg>
                  </div>
                  <div className="source-card-selection-radio">
                    <span className={`radio-circle ${selectedSource === 'upload' ? 'checked' : ''}`}></span>
                  </div>
                </div>

                <div className="source-card-content">
                  <h3 className="source-card-title">Upload Project</h3>
                  <p className="source-card-desc">ZIP or TAR project archive</p>
                </div>

                {selectedSource === 'upload' && (
                  <div className="source-card-active-body" onClick={(e) => e.stopPropagation()}>
                    {/* Switcher between ZIP Archive & Local Directory Path */}
                    <div className="source-subtab-row">
                      <button
                        type="button"
                        className={`source-subtab-btn ${uploadSubTab === 'archive' ? 'active' : ''}`}
                        onClick={() => setUploadSubTab('archive')}
                      >
                        ZIP Archive
                      </button>
                      <button
                        type="button"
                        className={`source-subtab-btn ${uploadSubTab === 'directory' ? 'active' : ''}`}
                        onClick={() => setUploadSubTab('directory')}
                      >
                        Directory Path
                      </button>
                    </div>

                    {uploadSubTab === 'archive' ? (
                      <div>
                        <input
                          ref={fileInputRef}
                          type="file"
                          accept=".zip,.tar,.tar.gz"
                          style={{ display: 'none' }}
                          onChange={handleFileChange}
                        />

                        {selectedFile ? (
                          /* 6. UPLOAD STATE - SELECTED FILE CARD */
                          <div className="file-selected-box">
                            <div className="file-selected-left">
                              <div className="file-type-icon">
                                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                                  <polyline points="14 2 14 8 20 8" />
                                  <line x1="12" y1="18" x2="12" y2="12" />
                                  <line x1="9" y1="15" x2="15" y2="15" />
                                </svg>
                              </div>
                              <div className="file-selected-meta">
                                <span className="file-name font-mono">{selectedFile.name}</span>
                                <span className="file-details">
                                  ZIP Archive • {formatFileSize(selectedFile.size)}
                                </span>
                                <span className="file-ready-tag">
                                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                                    <polyline points="20 6 9 17 4 12" />
                                  </svg>
                                  Ready for analysis
                                </span>
                              </div>
                            </div>
                            <button
                              type="button"
                              className="file-remove-btn"
                              onClick={handleRemoveFile}
                              title="Remove selected file"
                            >
                              Remove
                            </button>
                          </div>
                        ) : (
                          /* Dropzone Box */
                          <div
                            className={`source-dropzone-box ${isDragOver ? 'drag-over' : ''}`}
                            onDragOver={(e) => {
                              e.preventDefault();
                              setIsDragOver(true);
                            }}
                            onDragLeave={() => setIsDragOver(false)}
                            onDrop={handleDrop}
                            onClick={() => fileInputRef.current?.click()}
                          >
                            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" className="dropzone-svg">
                              <path d="M4 14.899A7 7 0 1 1 15.71 8h1.79a4.5 4.5 0 0 1 2.5 8.242" />
                              <path d="M12 12v9" />
                              <path d="m8 16 4-4 4 4" />
                            </svg>
                            <span className="dropzone-text">Drag &amp; drop archive here, or</span>
                            <button
                              type="button"
                              className="btn-secondary btn-sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                fileInputRef.current?.click();
                              }}
                            >
                              Choose File
                            </button>
                            <span className="dropzone-hint">Supports .zip or .tar archives up to 200MB</span>
                          </div>
                        )}
                      </div>
                    ) : (
                      /* Directory Path Input */
                      <div className="source-path-box">
                        <label className="source-path-label" htmlFor="source-dir-input">
                          Filesystem Directory Path
                        </label>
                        <input
                          id="source-dir-input"
                          type="text"
                          className="form-input font-mono"
                          value={pathInput}
                          onChange={(e) => setPathInput(e.target.value)}
                          placeholder="e.g. ../test-target or d:/ECDAT/test-target"
                        />
                        <div className="quick-presets-row">
                          <span className="quick-preset-label">Presets:</span>
                          <button
                            type="button"
                            className="btn-secondary btn-xs font-mono"
                            onClick={() => setPathInput('../test-target')}
                          >
                            ../test-target
                          </button>
                          <button
                            type="button"
                            className="btn-secondary btn-xs font-mono"
                            onClick={() => setPathInput('../test-target/src/main/java/demo')}
                          >
                            ../test-target/src/main/java/demo
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* CARD 2: Repository URL */}
              <div
                className={`source-card ${selectedSource === 'repository' ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedSource('repository');
                  setValidationError(null);
                }}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    setSelectedSource('repository');
                  }
                }}
              >
                <div className="source-card-top">
                  <div className="source-card-icon-pill indigo">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="18" r="3" />
                      <circle cx="6" cy="6" r="3" />
                      <circle cx="18" cy="6" r="3" />
                      <path d="M18 9v2a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V9" />
                      <path d="M12 12v3" />
                    </svg>
                  </div>
                  <div className="source-card-badge-wrap">
                    <span className="source-badge-roadmap">Backend integration required</span>
                  </div>
                </div>

                <div className="source-card-content">
                  <h3 className="source-card-title">Repository URL</h3>
                  <p className="source-card-desc">Analyze a Git repository</p>
                </div>

                <div className="source-card-active-body" onClick={(e) => e.stopPropagation()}>
                  <div className="source-repo-input-wrap">
                    <input
                      type="url"
                      className="form-input font-mono"
                      placeholder="Enter repository URL (e.g. https://github.com/org/repo.git)"
                      value={repoUrl}
                      onChange={(e) => setRepoUrl(e.target.value)}
                    />
                    <button
                      type="button"
                      className="btn-secondary btn-sm"
                      disabled={true}
                      title="Git repository cloning requires backend integration"
                    >
                      Analyze Repository
                    </button>
                  </div>
                </div>
              </div>

              {/* CARD 3: Files & Binaries */}
              <div
                className={`source-card ${selectedSource === 'binaries' ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedSource('binaries');
                  setValidationError(null);
                }}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    setSelectedSource('binaries');
                  }
                }}
              >
                <div className="source-card-top">
                  <div className="source-card-icon-pill alert">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
                      <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
                      <line x1="6" y1="6" x2="6.01" y2="6" />
                      <line x1="6" y1="18" x2="6.01" y2="18" />
                    </svg>
                  </div>
                  <div className="source-card-badge-wrap">
                    <span className="source-badge-roadmap">Coming soon</span>
                  </div>
                </div>

                <div className="source-card-content">
                  <h3 className="source-card-title">Files &amp; Binaries</h3>
                  <p className="source-card-desc">JAR, CLASS, configuration and certificate files</p>
                </div>

                <div className="source-card-active-body" onClick={(e) => e.stopPropagation()}>
                  <button
                    type="button"
                    className="btn-secondary btn-sm"
                    disabled={true}
                    title="Direct binary selection coming in upcoming release"
                  >
                    Select Files
                  </button>
                </div>
              </div>

              {/* CARD 4: Container Image */}
              <div
                className={`source-card ${selectedSource === 'container' ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedSource('container');
                  setValidationError(null);
                }}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    setSelectedSource('container');
                  }
                }}
              >
                <div className="source-card-top">
                  <div className="source-card-icon-pill warning">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
                      <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
                      <line x1="12" y1="22.08" x2="12" y2="12" />
                    </svg>
                  </div>
                  <div className="source-card-badge-wrap">
                    <span className="source-badge-roadmap">Coming soon</span>
                  </div>
                </div>

                <div className="source-card-content">
                  <h3 className="source-card-title">Container Image</h3>
                  <p className="source-card-desc">Analyze a container image or image archive</p>
                </div>

                <div className="source-card-active-body" onClick={(e) => e.stopPropagation()}>
                  <button
                    type="button"
                    className="btn-secondary btn-sm"
                    disabled={true}
                    title="Container layer scanning coming in upcoming release"
                  >
                    Select Image
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* 3. ANALYSIS SCOPE SECTION */}
          <div className="card-panel scan-section-card">
            <div className="scan-section-header">
              <div>
                <h2 className="scan-section-title">Analysis Scope</h2>
                <p className="scan-section-subtitle">
                  Select cryptographic discovery and quantum risk evaluation modules to execute
                </p>
              </div>
              <span className="scan-active-count-tag">
                {Object.values(scope).filter(Boolean).length} of 6 Modules Active
              </span>
            </div>

            {/* 6 Custom Scope Cards (3 columns desktop, 2 tablet, 1 mobile) */}
            <div className="scope-cards-grid">
              {/* 1. Cryptographic APIs */}
              <div
                className={`scope-card ${scope.cryptoApis ? 'selected' : ''}`}
                onClick={() => toggleScope('cryptoApis')}
                role="checkbox"
                aria-checked={scope.cryptoApis}
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    toggleScope('cryptoApis');
                  }
                }}
              >
                <div className="scope-card-header">
                  <div className="scope-card-icon-pill">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                    </svg>
                  </div>
                  <div className={`scope-check-indicator ${scope.cryptoApis ? 'active' : ''}`}>
                    {scope.cryptoApis && (
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                </div>
                <div className="scope-card-body">
                  <h4 className="scope-card-title">Cryptographic APIs</h4>
                  <p className="scope-card-desc">Discover cryptographic primitives and usage</p>
                </div>
              </div>

              {/* 2. Dependencies */}
              <div
                className={`scope-card ${scope.dependencies ? 'selected' : ''}`}
                onClick={() => toggleScope('dependencies')}
                role="checkbox"
                aria-checked={scope.dependencies}
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    toggleScope('dependencies');
                  }
                }}
              >
                <div className="scope-card-header">
                  <div className="scope-card-icon-pill">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M16.5 9.4 7.55 4.24a1.78 1.78 0 0 0-2.5 1.55v12.42a1.78 1.78 0 0 0 2.5 1.55L16.5 14.6a1.78 1.78 0 0 0 0-3.2z" />
                    </svg>
                  </div>
                  <div className={`scope-check-indicator ${scope.dependencies ? 'active' : ''}`}>
                    {scope.dependencies && (
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                </div>
                <div className="scope-card-body">
                  <h4 className="scope-card-title">Dependencies</h4>
                  <p className="scope-card-desc">Identify cryptographic libraries and versions</p>
                </div>
              </div>

              {/* 3. Certificates */}
              <div
                className={`scope-card ${scope.certificates ? 'selected' : ''}`}
                onClick={() => toggleScope('certificates')}
                role="checkbox"
                aria-checked={scope.certificates}
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    toggleScope('certificates');
                  }
                }}
              >
                <div className="scope-card-header">
                  <div className="scope-card-icon-pill">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                    </svg>
                  </div>
                  <div className={`scope-check-indicator ${scope.certificates ? 'active' : ''}`}>
                    {scope.certificates && (
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                </div>
                <div className="scope-card-body">
                  <h4 className="scope-card-title">Certificates</h4>
                  <p className="scope-card-desc">Inspect certificates and public-key properties</p>
                </div>
              </div>

              {/* 4. Quantum Risk */}
              <div
                className={`scope-card ${scope.quantumRisk ? 'selected' : ''}`}
                onClick={() => toggleScope('quantumRisk')}
                role="checkbox"
                aria-checked={scope.quantumRisk}
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    toggleScope('quantumRisk');
                  }
                }}
              >
                <div className="scope-card-header">
                  <div className="scope-card-icon-pill">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="12" r="3" />
                      <ellipse cx="12" cy="12" rx="10" ry="4" transform="rotate(30 12 12)" />
                      <ellipse cx="12" cy="12" rx="10" ry="4" transform="rotate(150 12 12)" />
                    </svg>
                  </div>
                  <div className={`scope-check-indicator ${scope.quantumRisk ? 'active' : ''}`}>
                    {scope.quantumRisk && (
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                </div>
                <div className="scope-card-body">
                  <h4 className="scope-card-title">Quantum Risk</h4>
                  <p className="scope-card-desc">Assess quantum vulnerability and Mosca exposure</p>
                </div>
              </div>

              {/* 5. PQC Migration */}
              <div
                className={`scope-card ${scope.pqcMigration ? 'selected' : ''}`}
                onClick={() => toggleScope('pqcMigration')}
                role="checkbox"
                aria-checked={scope.pqcMigration}
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    toggleScope('pqcMigration');
                  }
                }}
              >
                <div className="scope-card-header">
                  <div className="scope-card-icon-pill">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polyline points="16 3 21 3 21 8" />
                      <line x1="4" y1="20" x2="21" y2="3" />
                      <polyline points="21 16 21 21 16 21" />
                      <line x1="15" y1="15" x2="21" y2="21" />
                    </svg>
                  </div>
                  <div className={`scope-check-indicator ${scope.pqcMigration ? 'active' : ''}`}>
                    {scope.pqcMigration && (
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                </div>
                <div className="scope-card-body">
                  <h4 className="scope-card-title">PQC Migration</h4>
                  <p className="scope-card-desc">Map vulnerable primitives to PQC alternatives</p>
                </div>
              </div>

              {/* 6. CBOM */}
              <div
                className={`scope-card ${scope.cbom ? 'selected' : ''}`}
                onClick={() => toggleScope('cbom')}
                role="checkbox"
                aria-checked={scope.cbom}
                tabIndex={0}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    toggleScope('cbom');
                  }
                }}
              >
                <div className="scope-card-header">
                  <div className="scope-card-icon-pill">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                      <polyline points="14 2 14 8 20 8" />
                      <line x1="16" y1="13" x2="8" y2="13" />
                      <line x1="16" y1="17" x2="8" y2="17" />
                      <polyline points="10 9 9 9 8 9" />
                    </svg>
                  </div>
                  <div className={`scope-check-indicator ${scope.cbom ? 'active' : ''}`}>
                    {scope.cbom && (
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <polyline points="20 6 9 17 4 12" />
                      </svg>
                    )}
                  </div>
                </div>
                <div className="scope-card-body">
                  <h4 className="scope-card-title">CBOM</h4>
                  <p className="scope-card-desc">Generate cryptographic bill of materials</p>
                </div>
              </div>
            </div>
          </div>

          {/* 4. SCAN CONFIGURATION SECTION */}
          <div className="card-panel scan-section-card">
            <div className="scan-section-header">
              <div>
                <h2 className="scan-section-title">Scan Configuration</h2>
                <p className="scan-section-subtitle">
                  Configure enterprise business context and Mosca quantum timeline evaluation parameters
                </p>
              </div>
            </div>

            <div className="scan-config-grid">
              {/* Application Name */}
              <div className="scan-config-field">
                <label className="config-label" htmlFor="app-name-input">
                  Application / Service Name
                </label>
                <input
                  id="app-name-input"
                  type="text"
                  className="form-input"
                  value={context.applicationName}
                  onChange={(e) => setContext((prev) => ({ ...prev, applicationName: e.target.value }))}
                  placeholder="e.g. Enterprise Payment Gateway"
                />
                <span className="config-hint">Identifies the system within CBOM and compliance reports</span>
              </div>

              {/* Business Criticality */}
              <div className="scan-config-field">
                <label className="config-label" htmlFor="criticality-select">
                  Business Criticality
                </label>
                <select
                  id="criticality-select"
                  className="form-select"
                  value={context.businessCriticality}
                  onChange={(e) => setContext((prev) => ({ ...prev, businessCriticality: e.target.value as BusinessCriticality }))}
                >
                  <option value="CRITICAL">CRITICAL — Core financial / operational service</option>
                  <option value="HIGH">HIGH — Critical business workflow</option>
                  <option value="MEDIUM">MEDIUM — Internal business tool</option>
                  <option value="LOW">LOW — Non-critical utility</option>
                </select>
                <span className="config-hint">Directly affects overall risk scoring &amp; migration priority</span>
              </div>

              {/* Data Sensitivity */}
              <div className="scan-config-field">
                <label className="config-label" htmlFor="sensitivity-select">
                  Data Sensitivity
                </label>
                <select
                  id="sensitivity-select"
                  className="form-select"
                  value={context.dataSensitivity}
                  onChange={(e) => setContext((prev) => ({ ...prev, dataSensitivity: e.target.value as DataSensitivity }))}
                >
                  <option value="HIGHLY_SENSITIVE">HIGHLY_SENSITIVE — PII, PCI, credentials, financial</option>
                  <option value="CONFIDENTIAL">CONFIDENTIAL — Confidential business records</option>
                  <option value="INTERNAL">INTERNAL — General corporate data</option>
                  <option value="PUBLIC">PUBLIC — Publicly accessible information</option>
                </select>
                <span className="config-hint">Used to determine Mosca confidentiality exposure duration</span>
              </div>

              {/* Data Lifetime (Y) */}
              <div className="scan-config-field">
                <label className="config-label" htmlFor="lifetime-input">
                  Data Lifetime (Y)
                </label>
                <div className="input-with-unit">
                  <input
                    id="lifetime-input"
                    type="number"
                    min="1"
                    max="50"
                    className="form-input font-mono"
                    value={context.dataLifetimeYears}
                    onChange={(e) => setContext((prev) => ({ ...prev, dataLifetimeYears: parseInt(e.target.value, 10) || 1 }))}
                  />
                  <span className="input-unit-tag">years</span>
                </div>
                <span className="config-hint">Shelf-life requirement for protected cryptographic assets</span>
              </div>

              {/* Migration Time (X) */}
              <div className="scan-config-field">
                <label className="config-label" htmlFor="migration-time-input">
                  Migration Time (X)
                </label>
                <div className="input-with-unit">
                  <input
                    id="migration-time-input"
                    type="number"
                    min="1"
                    max="20"
                    className="form-input font-mono"
                    value={context.migrationTimeYears}
                    onChange={(e) => setContext((prev) => ({ ...prev, migrationTimeYears: parseInt(e.target.value, 10) || 1 }))}
                  />
                  <span className="input-unit-tag">years</span>
                </div>
                <span className="config-hint">Estimated duration required to migrate to post-quantum standards</span>
              </div>

              {/* Threat Horizon (Z) */}
              <div className="scan-config-field">
                <label className="config-label" htmlFor="threat-horizon-input">
                  Threat Horizon (Z)
                </label>
                <div className="input-with-unit">
                  <input
                    id="threat-horizon-input"
                    type="number"
                    min="1"
                    max="50"
                    className="form-input font-mono"
                    value={context.threatHorizonYears}
                    onChange={(e) => setContext((prev) => ({ ...prev, threatHorizonYears: parseInt(e.target.value, 10) || 1 }))}
                  />
                  <span className="input-unit-tag">years</span>
                </div>
                <span className="config-hint">Estimated years until Cryptographically Relevant Quantum Computer (CRQC)</span>
              </div>
            </div>
          </div>

          {/* 5. START ANALYSIS ACTION BAR */}
          <div className="scan-bottom-bar">
            <div className="scan-summary-status">
              <div className="summary-status-item">
                <span className="summary-label">Target:</span>
                <span className="summary-value font-mono">
                  {selectedSource === 'upload'
                    ? uploadSubTab === 'archive'
                      ? selectedFile ? selectedFile.name : 'No archive selected'
                      : pathInput
                    : selectedSource === 'repository'
                    ? repoUrl || 'No Git URL'
                    : selectedSource.toUpperCase()}
                </span>
              </div>
              <div className="summary-status-item">
                <span className="summary-label">Active Modules:</span>
                <span className="summary-value font-mono">
                  {Object.values(scope).filter(Boolean).length}/6
                </span>
              </div>
            </div>

            <div className="scan-cta-group">
              <button
                type="submit"
                className="btn-primary btn-start-analysis"
                disabled={isLoading || !isFormValid}
              >
                <span>Start Analysis</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <line x1="5" y1="12" x2="19" y2="12" />
                  <polyline points="12 5 19 12 12 19" />
                </svg>
              </button>
            </div>
          </div>
        </form>
      )}
    </div>
  );
};
