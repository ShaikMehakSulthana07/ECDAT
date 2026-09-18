import React from 'react';
import type { CertificateArtifactFinding } from '../types/analysis';

interface CertificatesViewProps {
  certificates?: CertificateArtifactFinding[];
}

// Helper to convert ALL_CAPS_SNAKE to readable Title Case
const formatTitleCase = (str: string): string => {
  if (!str) return '';
  return str
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

export const CertificatesView: React.FC<CertificatesViewProps> = ({
  certificates = [],
}) => {
  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Certificate &amp; Cryptographic Artifacts</h1>
          <p className="view-subtitle">
            Discovered X.509 public certificates, PEM keystores, and cryptographic artifacts evaluated for key size and signature algorithm quantum readiness.
          </p>
        </div>
      </div>

      <div className="card-panel">
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">Discovered Public Certificates &amp; Key Artifacts ({certificates.length})</h3>
            <span className="card-panel-sub">
              Scanned from project repository root, resources, and keystores (private key material is strictly unparsed).
            </span>
          </div>
        </div>

        {certificates.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-muted)' }}>
            <div style={{ marginBottom: '8px', fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>
              No Public Certificates Detected
            </div>
            <p style={{ fontSize: '12px', maxWidth: '440px', margin: '0 auto' }}>
              The target repository does not contain standalone .crt, .pem, .der, or .jks certificate files.
            </p>
          </div>
        ) : (
          <div className="table-container">
            <table className="enterprise-table">
              <thead>
                <tr>
                  <th style={{ width: '22%' }}>Artifact Filename</th>
                  <th style={{ width: '12%' }}>Type</th>
                  <th style={{ width: '18%' }}>Public Key Algorithm</th>
                  <th style={{ width: '18%' }}>Signature Algorithm</th>
                  <th style={{ width: '14%' }}>Validity Period</th>
                  <th style={{ width: '16%' }}>Quantum Status</th>
                </tr>
              </thead>
              <tbody>
                {certificates.map((cert, idx) => {
                  const isRsaOrEcc =
                    (cert.publicKeyAlgorithm || '').includes('RSA') ||
                    (cert.publicKeyAlgorithm || '').includes('EC');

                  return (
                    <tr key={idx}>
                      <td>
                        <strong className="algo-primary-title font-mono">{cert.fileName}</strong>
                      </td>
                      <td>
                        <span className="table-purpose-text">{formatTitleCase(cert.certificateType || cert.fileType || 'X.509')}</span>
                      </td>
                      <td>
                        <div className="algo-cell-block">
                          <span className="algo-primary-title">{cert.publicKeyAlgorithm || 'RSA'}</span>
                          {cert.keySize && (
                            <span className="algo-secondary-sub font-mono">{cert.keySize} bits</span>
                          )}
                        </div>
                      </td>
                      <td>
                        <span className="font-mono text-secondary" style={{ fontSize: '12px' }}>
                          {cert.signatureAlgorithm || 'SHA256withRSA'}
                        </span>
                      </td>
                      <td>
                        <span className="font-mono text-muted" style={{ fontSize: '11px' }}>
                          {cert.validityDates || 'Active'}
                        </span>
                      </td>
                      <td>
                        <span className={`quantum-badge ${isRsaOrEcc ? 'vulnerable' : 'safe'}`}>
                          <span className={`status-dot-sm ${isRsaOrEcc ? 'vuln' : 'safe'}`}></span>
                          {isRsaOrEcc ? 'Vulnerable (Shor)' : 'Quantum Safe'}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
