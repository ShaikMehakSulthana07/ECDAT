import React, { useState } from 'react';
import type { CBOMDocument } from '../types/analysis';

interface CBOMViewerProps {
  cbom: CBOMDocument;
}

export const CBOMViewer: React.FC<CBOMViewerProps> = ({ cbom }) => {
  const [viewMode, setViewMode] = useState<'components' | 'json'>('components');
  const [copied, setCopied] = useState(false);
  const [selectedCompIndex, setSelectedCompIndex] = useState<number>(0);

  const jsonString = JSON.stringify(cbom, null, 2);

  const handleCopyJson = () => {
    navigator.clipboard.writeText(jsonString);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadJson = () => {
    const blob = new Blob([jsonString], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `cbom-cyclonedx-${cbom.serialNumber.replace(/[^a-zA-Z0-9]/g, '_')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const currentComponent = cbom.components && cbom.components[selectedCompIndex];

  return (
    <div className="cbom-viewer-container">
      {/* Metadata Overview Card */}
      <div className="card-panel">
        <div className="panel-header">
          <div className="panel-title-group">
            <h3 className="panel-title">CycloneDX 1.6 Cryptography Bill of Materials (CBOM)</h3>
            <span className="panel-sub">Machine-readable inventory of cryptographic assets & posture</span>
          </div>

          <div className="cbom-header-actions">
            <button
              className={`toggle-btn ${viewMode === 'components' ? 'active' : ''}`}
              onClick={() => setViewMode('components')}
            >
              Component Inspector
            </button>
            <button
              className={`toggle-btn ${viewMode === 'json' ? 'active' : ''}`}
              onClick={() => setViewMode('json')}
            >
              Raw CBOM JSON
            </button>
            <button className="btn-secondary btn-sm" onClick={handleDownloadJson} title="Download JSON file">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
              Export JSON
            </button>
          </div>
        </div>

        {/* Metadata Badges */}
        <div className="cbom-meta-grid">
          <div className="meta-item">
            <span className="meta-label">BOM Format</span>
            <strong className="meta-val">{cbom.bomFormat}</strong>
          </div>
          <div className="meta-item">
            <span className="meta-label">Spec Version</span>
            <strong className="meta-val font-mono">{cbom.specVersion}</strong>
          </div>
          <div className="meta-item">
            <span className="meta-label">Document Version</span>
            <strong className="meta-val font-mono">v{cbom.version}</strong>
          </div>
          <div className="meta-item">
            <span className="meta-label">Serial Number</span>
            <strong className="meta-val font-mono truncate" title={cbom.serialNumber}>
              {cbom.serialNumber}
            </strong>
          </div>
          <div className="meta-item">
            <span className="meta-label">Generator Tool</span>
            <strong className="meta-val">
              {cbom.metadata?.tool?.name || 'ECDAT'} v{cbom.metadata?.tool?.version || '0.0.1'}
            </strong>
          </div>
          <div className="meta-item">
            <span className="meta-label">Asset Components</span>
            <strong className="meta-val font-mono text-highlight">
              {cbom.components?.length || 0} Assets
            </strong>
          </div>
        </div>
      </div>

      {viewMode === 'components' ? (
        <div className="cbom-components-layout">
          {/* Component List */}
          <div className="card-panel comp-list-panel">
            <h4 className="subpanel-title">Cryptographic Assets ({cbom.components?.length || 0})</h4>
            <div className="comp-list">
              {cbom.components && cbom.components.length > 0 ? (
                cbom.components.map((comp, idx) => {
                  const props = comp.cryptoProperties;
                  const isSelected = selectedCompIndex === idx;
                  return (
                    <div
                      key={idx}
                      className={`comp-list-item ${isSelected ? 'selected' : ''}`}
                      onClick={() => setSelectedCompIndex(idx)}
                    >
                      <div className="comp-item-header">
                        <span className="comp-algo">{props?.algorithm || comp.name}</span>
                        <span className="comp-type badge-subtle">{comp.type}</span>
                      </div>
                      <div className="comp-desc truncate">{comp.description}</div>
                      <div className="comp-meta-row">
                        <span className="font-mono text-muted">
                          {props?.sourceFile?.split(/[\\/]/).pop()}:{props?.sourceLine}
                        </span>
                        {props?.risk?.riskLevel && (
                          <span className={`risk-badge small ${props.risk.riskLevel.toLowerCase()}`}>
                            {props.risk.riskLevel}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="text-muted p-4">No CBOM components found.</div>
              )}
            </div>
          </div>

          {/* Component Details */}
          <div className="card-panel comp-detail-panel">
            {currentComponent ? (
              <div className="comp-detail-content">
                <div className="comp-detail-header">
                  <div>
                    <span className="badge-subtle">{currentComponent.type}</span>
                    <h3 className="comp-detail-title">{currentComponent.name}</h3>
                    <p className="comp-detail-desc">{currentComponent.description}</p>
                  </div>
                </div>

                {currentComponent.cryptoProperties && (
                  <div className="crypto-props-section">
                    <h4 className="section-subtitle-sm">Crypto Properties</h4>
                    <dl className="property-list">
                      <div className="prop-row">
                        <dt>Algorithm</dt>
                        <dd className="font-bold">{currentComponent.cryptoProperties.algorithm}</dd>
                      </div>
                      <div className="prop-row">
                        <dt>Variant</dt>
                        <dd>{currentComponent.cryptoProperties.algorithmVariant || '—'}</dd>
                      </div>
                      <div className="prop-row">
                        <dt>Purpose</dt>
                        <dd>{currentComponent.cryptoProperties.purpose}</dd>
                      </div>
                      {currentComponent.cryptoProperties.assetCategory && (
                        <div className="prop-row">
                          <dt>Asset Category</dt>
                          <dd><span className="purpose-tag">{currentComponent.cryptoProperties.assetCategory.replace('_', ' ')}</span></dd>
                        </div>
                      )}
                      {currentComponent.cryptoProperties.lifecycleStatus && (
                        <div className="prop-row">
                          <dt>Lifecycle Status</dt>
                          <dd>
                            <span className={`lifecycle-badge ${currentComponent.cryptoProperties.lifecycleStatus.toLowerCase()}`}>
                              {currentComponent.cryptoProperties.lifecycleStatus}
                            </span>
                          </dd>
                        </div>
                      )}
                      {currentComponent.cryptoProperties.usageCategory && (
                        <div className="prop-row">
                          <dt>Usage Mode</dt>
                          <dd><span className="badge-subtle">{currentComponent.cryptoProperties.usageCategory}</span></dd>
                        </div>
                      )}
                      {currentComponent.cryptoProperties.library && (
                        <div className="prop-row">
                          <dt>Library</dt>
                          <dd>{currentComponent.cryptoProperties.library}</dd>
                        </div>
                      )}
                      <div className="prop-row">
                        <dt>Key Size</dt>
                        <dd>{currentComponent.cryptoProperties.keySize ? `${currentComponent.cryptoProperties.keySize} bits` : '—'}</dd>
                      </div>
                      <div className="prop-row">
                        <dt>Source File</dt>
                        <dd className="font-mono">{currentComponent.cryptoProperties.sourceFile}</dd>
                      </div>
                      <div className="prop-row">
                        <dt>Source Line</dt>
                        <dd className="font-mono">Line {currentComponent.cryptoProperties.sourceLine}</dd>
                      </div>
                      <div className="prop-row">
                        <dt>Code Evidence</dt>
                        <dd>
                          <code className="evidence-inline">{currentComponent.cryptoProperties.evidence}</code>
                        </dd>
                      </div>
                      <div className="prop-row">
                        <dt>Detection Confidence</dt>
                        <dd>{currentComponent.cryptoProperties.confidence}</dd>
                      </div>
                    </dl>

                    {/* Embedded Risk */}
                    {currentComponent.cryptoProperties.risk && (
                      <div className="cbom-subblock">
                        <h5 className="subblock-title">Embedded Risk Evaluation</h5>
                        <div className="risk-tag-row">
                          <span className={`risk-badge ${currentComponent.cryptoProperties.risk.riskLevel?.toLowerCase()}`}>
                            Level: {currentComponent.cryptoProperties.risk.riskLevel} ({currentComponent.cryptoProperties.risk.riskScore}/100)
                          </span>
                          <span className={`quantum-badge ${currentComponent.cryptoProperties.risk.quantumRisk?.toLowerCase()}`}>
                            Quantum: {currentComponent.cryptoProperties.risk.quantumRisk}
                          </span>
                        </div>
                      </div>
                    )}

                    {/* Embedded PQC */}
                    {currentComponent.cryptoProperties.pqcRecommendation && (
                      <div className="cbom-subblock">
                        <h5 className="subblock-title">Embedded PQC Migration Guidance</h5>
                        <div className="pqc-cbom-row">
                          <span>Status: <strong>{currentComponent.cryptoProperties.pqcRecommendation.recommendationStatus}</strong></span>
                          {currentComponent.cryptoProperties.pqcRecommendation.recommendedAlgorithm && (
                            <span>Target: <strong className="text-pqc">{currentComponent.cryptoProperties.pqcRecommendation.recommendedAlgorithm}</strong></span>
                          )}
                          <span>Priority: <strong>{currentComponent.cryptoProperties.pqcRecommendation.migrationPriority}</strong></span>
                        </div>
                        {currentComponent.cryptoProperties.pqcRecommendation.rationale && (
                          <p className="pqc-cbom-rationale">{currentComponent.cryptoProperties.pqcRecommendation.rationale}</p>
                        )}
                      </div>
                    )}

                    {/* Custom Properties */}
                    {currentComponent.properties && currentComponent.properties.length > 0 && (
                      <div className="cbom-subblock">
                        <h5 className="subblock-title">Custom CycloneDX Extension Properties</h5>
                        <ul className="custom-props-list">
                          {currentComponent.properties.map((p, pIdx) => (
                            <li key={pIdx} className="custom-prop-item">
                              <code>{p.name}</code> = <span className="font-mono">{p.value}</span>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                )}
              </div>
            ) : (
              <div className="text-muted p-4">Select an asset component to view its CBOM attributes.</div>
            )}
          </div>
        </div>
      ) : (
        <div className="card-panel json-viewer-panel">
          <div className="json-toolbar">
            <span className="font-mono text-muted">CycloneDX v1.6 Document JSON</span>
            <button className="btn-secondary btn-sm" onClick={handleCopyJson}>
              {copied ? '✓ Copied to clipboard' : 'Copy JSON'}
            </button>
          </div>
          <pre className="json-code-block">
            <code>{jsonString}</code>
          </pre>
        </div>
      )}
    </div>
  );
};
