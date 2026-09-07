import React, { useState, useRef } from 'react';

interface ScanInputProps {
  onScanPath: (path: string) => void;
  onScanFile: (file: File) => void;
  isLoading: boolean;
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
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handlePathSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!pathInput.trim() || isLoading) return;
    onScanPath(pathInput.trim());
  };

  const handleFileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile || isLoading) return;
    onScanFile(selectedFile);
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
