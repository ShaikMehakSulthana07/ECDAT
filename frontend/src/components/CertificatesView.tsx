import React from 'react';
import type { CertificateArtifactFinding } from '../types/analysis';

interface CertificatesViewProps {
  certificates?: CertificateArtifactFinding[];
}

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
                  <th>Artifact Filename</th>
                  <th>Certificate Type</th>
                  <th>Public Key Algorithm</th>
                  <th>Key Size</th>
                  <th>Signature Algorithm</th>
                  <th>Validity Dates</th>
                  <th>Confidence</th>
                  <th>Quantum Status</th>
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
                        <strong className="font-mono text-primary">{cert.fileName}</strong>
                      </td>
                      <td>
                        <span className="tag-subtle">{cert.certificateType || cert.fileType || 'X.509'}</span>
                      </td>
                      <td>
                        <span className="algo-text">{cert.publicKeyAlgorithm || 'RSA'}</span>
                      </td>
                      <td className="font-mono">
                        {cert.keySize ? `${cert.keySize} bits` : '—'}
                      </td>
                      <td>
                        <span className="tag-subtle font-mono">{cert.signatureAlgorithm || 'SHA256withRSA'}</span>
                      </td>
                      <td>
                        <span className="font-mono text-muted" style={{ fontSize: '11px' }}>
                          {cert.validityDates || 'Validity active'}
                        </span>
                      </td>
                      <td>
                        <span className="tag-subtle font-mono">{cert.confidence || 'HIGH'}</span>
                      </td>
                      <td>
                        <span className={`quantum-badge ${isRsaOrEcc ? 'vulnerable' : 'safe'}`}>
                          {isRsaOrEcc ? 'VULNERABLE (SHOR)' : 'RESISTANT'}
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
