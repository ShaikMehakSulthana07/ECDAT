import React, { useState, useMemo } from 'react';
import type { CBOMDocument } from '../types/analysis';

interface CBOMViewerProps {
  cbom: CBOMDocument;
}

export const CBOMViewer: React.FC<CBOMViewerProps> = ({ cbom }) => {
  const [viewMode, setViewMode] = useState<'components' | 'json'>('components');
  const [searchTerm, setSearchTerm] = useState('');
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
    a.download = `cbom-${cbom.serialNumber.replace(/[^a-zA-Z0-9]/g, '_')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const components = cbom.components || [];

  const filteredComponents = useMemo(() => {
    if (!searchTerm.trim()) return components;
    const q = searchTerm.toLowerCase();
    return components.filter((c) => {
      const nameMatch = c.name.toLowerCase().includes(q);
      const descMatch = (c.description || '').toLowerCase().includes(q);
      const algoMatch = (c.cryptoProperties?.algorithm || '').toLowerCase().includes(q);
      return nameMatch || descMatch || algoMatch;
    });
  }, [components, searchTerm]);

  const currentComponent = filteredComponents[selectedCompIndex] || filteredComponents[0] || null;

  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Cryptography Bill of Materials (CBOM)</h1>
          <p className="view-subtitle">
            ECDAT Cryptography Bill of Materials · CycloneDX-inspired machine-readable inventory of cryptographic assets, algorithms, and post-quantum migration posture.
          </p>
        </div>
        <div className="view-actions">
          <div style={{ display: 'flex', gap: '6px' }}>
            <button
              className={`btn-secondary ${viewMode === 'components' ? 'active' : ''}`}
              style={viewMode === 'components' ? { backgroundColor: 'var(--primary-subtle)', borderColor: 'var(--primary-border)', color: '#fff' } : {}}
              onClick={() => setViewMode('components')}
            >
              Component Inspector
            </button>
            <button
              className={`btn-secondary ${viewMode === 'json' ? 'active' : ''}`}
              style={viewMode === 'json' ? { backgroundColor: 'var(--primary-subtle)', borderColor: 'var(--primary-border)', color: '#fff' } : {}}
              onClick={() => setViewMode('json')}
            >
              Raw CBOM JSON
            </button>
          </div>
          <button className="btn-primary" onClick={handleDownloadJson}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
            Export JSON
          </button>
        </div>
      </div>

      {/* CBOM Metadata Header Card */}
      <div className="card-panel">
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">ECDAT Cryptography Bill of Materials</h3>
            <span className="card-panel-sub">Document Metadata &amp; Specification Parameters</span>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: '12px' }}>
          <div style={{ padding: '10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
            <span style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>BOM Format</span>
            <div className="font-mono font-bold" style={{ marginTop: '2px' }}>{cbom.bomFormat}</div>
          </div>
          <div style={{ padding: '10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
            <span style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Spec Version</span>
            <div className="font-mono font-bold" style={{ marginTop: '2px' }}>{cbom.specVersion}</div>
          </div>
          <div style={{ padding: '10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
            <span style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Document Version</span>
            <div className="font-mono font-bold" style={{ marginTop: '2px' }}>v{cbom.version}</div>
          </div>
          <div style={{ padding: '10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
            <span style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Generator Tool</span>
            <div style={{ fontSize: '12px', fontWeight: 600, marginTop: '2px' }}>
              {cbom.metadata?.tool?.name || 'ECDAT'}
            </div>
          </div>
          <div style={{ padding: '10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
            <span style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Asset Components</span>
            <div className="font-mono font-bold" style={{ marginTop: '2px', color: 'var(--pqc-accent)' }}>
              {components.length} Assets
            </div>
          </div>
          <div style={{ padding: '10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', overflow: 'hidden' }}>
            <span style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Serial Number</span>
            <div className="font-mono text-muted" style={{ fontSize: '11px', marginTop: '2px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={cbom.serialNumber}>
              {cbom.serialNumber}
            </div>
          </div>
        </div>
      </div>

      {viewMode === 'components' ? (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '20px' }}>
          {/* Component List */}
          <div className="card-panel" style={{ maxHeight: '720px', display: 'flex', flexDirection: 'column' }}>
            <div style={{ marginBottom: '12px', display: 'flex', gap: '8px' }}>
              <input
                type="text"
                className="topbar-search-input"
                placeholder="Filter CBOM components..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {filteredComponents.length === 0 ? (
                <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '12px' }}>
                  No components match the search filter.
                </div>
              ) : (
                filteredComponents.map((comp, idx) => {
                  const props = comp.cryptoProperties;
                  const isSelected = selectedCompIndex === idx;
                  const riskLevel = props?.risk?.riskLevel || 'LOW';

                  return (
                    <div
                      key={idx}
                      onClick={() => setSelectedCompIndex(idx)}
                      style={{
                        padding: '10px 12px',
                        borderRadius: 'var(--radius-sm)',
                        backgroundColor: isSelected ? 'var(--primary-subtle)' : 'var(--bg-surface-subtle)',
                        border: isSelected ? '1px solid var(--primary-border)' : '1px solid var(--border-subtle)',
                        cursor: 'pointer',
                        transition: 'all 0.12s ease',
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <strong className="algo-text">{props?.algorithm || comp.name}</strong>
                        <span className={`risk-badge ${riskLevel.toLowerCase()}`} style={{ fontSize: '10px' }}>
                          {riskLevel}
                        </span>
                      </div>
                      <div className="text-muted" style={{ fontSize: '11px', marginTop: '2px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {comp.description}
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '6px', fontSize: '10px', color: 'var(--text-muted)' }}>
                        <span className="font-mono">
                          {props?.sourceFile?.split(/[\\/]/).pop()}:{props?.sourceLine}
                        </span>
                        <span className="tag-subtle" style={{ fontSize: '9px', padding: '1px 4px' }}>
                          {props?.assetCategory?.replace('_', ' ') || comp.type}
                        </span>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>

          {/* Component Details */}
          <div className="card-panel">
            {currentComponent ? (
              <div>
                <div style={{ marginBottom: '16px', paddingBottom: '12px', borderBottom: '1px solid var(--border-subtle)' }}>
                  <span className="tag-subtle font-mono">{currentComponent.type}</span>
                  <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)', marginTop: '6px' }}>
                    {currentComponent.name}
                  </h3>
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                    {currentComponent.description}
                  </p>
                </div>

                {currentComponent.cryptoProperties && (
                  <div>
                    <h4 style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '10px' }}>
                      Cryptographic Properties
                    </h4>
                    <table className="props-table">
                      <tbody>
                        <tr>
                          <th>Algorithm</th>
                          <td className="font-bold font-mono">{currentComponent.cryptoProperties.algorithm}</td>
                        </tr>
                        <tr>
                          <th>Variant</th>
                          <td>{currentComponent.cryptoProperties.algorithmVariant || '—'}</td>
                        </tr>
                        <tr>
                          <th>Declared Purpose</th>
                          <td><span className="tag-subtle">{currentComponent.cryptoProperties.purpose}</span></td>
                        </tr>
                        <tr>
                          <th>Key Size</th>
                          <td className="font-mono">{currentComponent.cryptoProperties.keySize ? `${currentComponent.cryptoProperties.keySize} bits` : '—'}</td>
                        </tr>
                        <tr>
                          <th>Source Location</th>
                          <td className="font-mono">{currentComponent.cryptoProperties.sourceFile}:{currentComponent.cryptoProperties.sourceLine}</td>
                        </tr>
                        <tr>
                          <th>Detection Evidence</th>
                          <td>
                            <code style={{ fontSize: '11px', color: '#e2e8f0', background: '#090d14', padding: '3px 6px', borderRadius: '3px', display: 'block', wordBreak: 'break-all' }}>
                              {currentComponent.cryptoProperties.evidence}
                            </code>
                          </td>
                        </tr>
                      </tbody>
                    </table>

                    {/* Embedded PQC */}
                    {currentComponent.cryptoProperties.pqcRecommendation && (
                      <div style={{ marginTop: '16px', padding: '12px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
                        <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--pqc-accent)', textTransform: 'uppercase', marginBottom: '6px' }}>
                          Embedded PQC Migration Metadata
                        </div>
                        <div style={{ display: 'flex', gap: '12px', fontSize: '12px' }}>
                          <span>Status: <strong>{currentComponent.cryptoProperties.pqcRecommendation.recommendationStatus}</strong></span>
                          {currentComponent.cryptoProperties.pqcRecommendation.recommendedAlgorithm && (
                            <span>Target: <strong className="font-mono" style={{ color: 'var(--pqc-accent)' }}>{currentComponent.cryptoProperties.pqcRecommendation.recommendedAlgorithm}</strong></span>
                          )}
                          <span>Priority: <strong>{currentComponent.cryptoProperties.pqcRecommendation.migrationPriority}</strong></span>
                        </div>
                        {currentComponent.cryptoProperties.pqcRecommendation.rationale && (
                          <p style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '6px', lineHeight: 1.4 }}>
                            {currentComponent.cryptoProperties.pqcRecommendation.rationale}
                          </p>
                        )}
                      </div>
                    )}

                    {/* Custom Extension Properties */}
                    {currentComponent.properties && currentComponent.properties.length > 0 && (
                      <div style={{ marginTop: '16px' }}>
                        <h4 style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '8px' }}>
                          Custom Extension Properties
                        </h4>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px' }}>
                          {currentComponent.properties.map((p, pIdx) => (
                            <div key={pIdx} style={{ fontSize: '11px', background: 'var(--bg-surface-subtle)', padding: '6px 8px', borderRadius: '3px', border: '1px solid var(--border-subtle)' }}>
                              <span className="font-mono text-muted">{p.name}:</span> <strong className="font-mono">{p.value}</strong>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            ) : (
              <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-muted)' }}>
                Select a component to inspect CBOM properties.
              </div>
            )}
          </div>
        </div>
      ) : (
        <div className="card-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span className="font-mono text-muted" style={{ fontSize: '12px' }}>
              CycloneDX-Inspired CBOM Document JSON
            </span>
            <button className="btn-secondary btn-sm" onClick={handleCopyJson}>
              {copied ? '✓ Copied to clipboard' : 'Copy JSON'}
            </button>
          </div>
          <pre
            style={{
              padding: '16px',
              backgroundColor: '#06090e',
              border: '1px solid var(--border-default)',
              borderRadius: 'var(--radius-sm)',
              fontFamily: 'var(--font-mono)',
              fontSize: '12px',
              color: '#93c5fd',
              maxHeight: '600px',
              overflowY: 'auto',
              lineHeight: 1.5,
            }}
          >
            <code>{jsonString}</code>
          </pre>
        </div>
      )}
    </div>
  );
};
