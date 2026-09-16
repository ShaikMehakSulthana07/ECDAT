import React, { useState, useRef } from 'react';
import type { BusinessCriticality, DataSensitivity } from '../types/analysis';

interface ScanInputProps {
  onScanPath: (path: string, context: ProjectContext) => void;
  onScanFile: (file: File, context: ProjectContext) => void;
  isLoading: boolean;
}

interface ProjectContext {
  applicationName: string;
  businessCriticality: BusinessCriticality;
  dataSensitivity: DataSensitivity;
  dataLifetimeYears: number;
  migrationTimeYears: number;
  threatHorizonYears: number;
}

export const ScanInput: React.FC<ScanInputProps> = ({
  onScanPath,
  onScanFile,
  isLoading,
}) => {
  const [mode, setMode] = useState<'path' | 'upload'>('path');
  const [pathInput, setPathInput] = useState<string>('../test-target');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  // Project context state
  const [projectContext, setProjectContext] = useState<ProjectContext>({
    applicationName: '',
    businessCriticality: 'MEDIUM',
    dataSensitivity: 'INTERNAL',
    dataLifetimeYears: 10,
    migrationTimeYears: 3,
    threatHorizonYears: 10,
  });

  const handlePathSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!pathInput.trim() || isLoading) return;
    onScanPath(pathInput.trim(), projectContext);
  };

  const handleFileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile || isLoading) return;
    onScanFile(selectedFile, projectContext);
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
    <div className="scan-card">
      <div className="scan-tabs">
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
          Zip Archive Upload
        </button>
      </div>

      {/* Project Analysis Context */}
      <div className="context-section">
        <button
          type="button"
          className="context-toggle"
          onClick={() => setShowAdvanced(!showAdvanced)}
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="3" />
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
          </svg>
          {showAdvanced ? 'Hide' : 'Show'} Analysis Configuration
        </button>

        {showAdvanced && (
          <div className="context-form">
            <div className="context-grid">
              <div className="context-field">
                <label>Application Name</label>
                <input
                  type="text"
                  className="text-input"
                  placeholder="e.g. Payment Service"
                  value={projectContext.applicationName}
                  onChange={(e) => setProjectContext({...projectContext, applicationName: e.target.value})}
                  disabled={isLoading}
                />
              </div>
              
              <div className="context-field">
                <label>Business Criticality</label>
                <select
                  className="select-input"
                  value={projectContext.businessCriticality}
                  onChange={(e) => setProjectContext({...projectContext, businessCriticality: e.target.value as BusinessCriticality})}
                  disabled={isLoading}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium (Default)</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
              
              <div className="context-field">
                <label>Data Sensitivity</label>
                <select
                  className="select-input"
                  value={projectContext.dataSensitivity}
                  onChange={(e) => setProjectContext({...projectContext, dataSensitivity: e.target.value as DataSensitivity})}
                  disabled={isLoading}
                >
                  <option value="PUBLIC">Public</option>
                  <option value="INTERNAL">Internal (Default)</option>
                  <option value="CONFIDENTIAL">Confidential</option>
                  <option value="HIGHLY_SENSITIVE">Highly Sensitive</option>
                </select>
              </div>
              
              <div className="context-field">
                <label>Data Lifetime (years)</label>
                <input
                  type="number"
                  className="number-input"
                  min="1"
                  max="50"
                  value={projectContext.dataLifetimeYears}
                  onChange={(e) => setProjectContext({...projectContext, dataLifetimeYears: parseInt(e.target.value) || 10})}
                  disabled={isLoading}
                />
              </div>
              
              <div className="context-field">
                <label>Migration Time (years)</label>
                <input
                  type="number"
                  className="number-input"
                  min="1"
                  max="20"
                  value={projectContext.migrationTimeYears}
                  onChange={(e) => setProjectContext({...projectContext, migrationTimeYears: parseInt(e.target.value) || 3})}
                  disabled={isLoading}
                />
              </div>
              
              <div className="context-field">
                <label>Threat Horizon (years)</label>
                <input
                  type="number"
                  className="number-input"
                  min="1"
                  max="30"
                  value={projectContext.threatHorizonYears}
                  onChange={(e) => setProjectContext({...projectContext, threatHorizonYears: parseInt(e.target.value) || 10})}
                  disabled={isLoading}
                />
              </div>
            </div>
            
            <div className="context-note">
              <strong>Note:</strong> These values are used for Mosca-style quantum risk assessment: 
              Migration Time + Data Lifetime &gt; Threat Horizon
            </div>
          </div>
        )}
      </div>

      {mode === 'path' ? (
        <form onSubmit={handlePathSubmit} className="scan-form">
          <div className="input-group">
            <label htmlFor="path-input" className="input-label">
              Target Project Directory
            </label>
            <div className="input-with-button">
              <input
                id="path-input"
                type="text"
                className="text-input"
                placeholder="e.g. ../test-target or d:/ECDAT/test-target"
                value={pathInput}
                onChange={(e) => setPathInput(e.target.value)}
                disabled={isLoading}
              />
              <button
                type="submit"
                className="btn-primary"
                disabled={isLoading || !pathInput.trim()}
              >
                {isLoading ? (
                  <>
                    <span className="spinner"></span>
                    Analyzing...
                  </>
                ) : (
                  <>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polygon points="5 3 19 12 5 21 5 3" />
                    </svg>
                    Run Discovery & Analysis
                  </>
                )}
              </button>
            </div>
          </div>

          <div className="quick-presets">
            <span className="quick-label">Quick presets:</span>
            <button
              type="button"
              className="preset-chip"
              onClick={() => setPathInput('../test-target')}
              disabled={isLoading}
            >
              <code>../test-target</code>
            </button>
            <button
              type="button"
              className="preset-chip"
              onClick={() => setPathInput('../test-target/src/main/java/demo')}
              disabled={isLoading}
            >
              <code>../test-target/src/main/java/demo</code>
            </button>
          </div>
        </form>
      ) : (
        <form onSubmit={handleFileSubmit} className="scan-form">
          <div
            className={`dropzone ${isDragOver ? 'drag-over' : ''} ${selectedFile ? 'has-file' : ''}`}
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
              disabled={isLoading}
            />
            {selectedFile ? (
              <div className="file-selected-info">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#38bdf8" strokeWidth="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                  <polyline points="10 9 9 9 8 9" />
                </svg>
                <div>
                  <div className="file-name">{selectedFile.name}</div>
                  <div className="file-size">{(selectedFile.size / 1024).toFixed(1)} KB</div>
                </div>
                <button
                  type="button"
                  className="btn-text"
                  onClick={(e) => {
                    e.stopPropagation();
                    setSelectedFile(null);
                  }}
                >
                  Change
                </button>
              </div>
            ) : (
              <div className="dropzone-prompt">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17 8 12 3 7 8" />
                  <line x1="12" y1="3" x2="12" y2="9" />
                </svg>
                <div className="dropzone-text">
                  <strong>Click to select</strong> or drag and drop a <code>.zip</code> archive
                </div>
                <div className="dropzone-sub">Java source code repository archive</div>
              </div>
            )}
          </div>

          <div className="upload-actions">
            <button
              type="submit"
              className="btn-primary"
              disabled={isLoading || !selectedFile}
            >
              {isLoading ? (
                <>
                  <span className="spinner"></span>
                  Analyzing Archive...
                </>
              ) : (
                <>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <polygon points="5 3 19 12 5 21 5 3" />
                  </svg>
                  Upload & Analyze
                </>
              )}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
