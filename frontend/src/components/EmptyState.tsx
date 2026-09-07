import React from 'react';

interface EmptyStateProps {
  onQuickScan: () => void;
  isLoading: boolean;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ onQuickScan, isLoading }) => {
  return (
    <div className="card-panel empty-state-panel">
      <div className="empty-hero">
        <div className="empty-icon-shield">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            <path d="M12 8v4" />
            <path d="M12 16h.01" />
          </svg>
        </div>
        <h2 className="empty-title">Enterprise Cryptographic Discovery & Post-Quantum Analysis</h2>
        <p className="empty-desc">
          Analyze software repositories to discover cryptographic primitives, evaluate quantum vulnerability, identify NIST PQC migration candidates (ML-KEM, ML-DSA), and export a CycloneDX 1.6 Cryptography Bill of Materials (CBOM).
        </p>
      </div>

      {/* Feature Pillar Cards */}
      <div className="pillars-grid">
        <div className="pillar-card">
          <div className="pillar-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </div>
          <h4 className="pillar-title">AST Discovery Engine</h4>
          <p className="pillar-text">
            Deep JavaParser static AST extraction for AES, RSA, ECDSA, ECDH, SHA hashes, and TLS with key length detection.
          </p>
        </div>

        <div className="pillar-card">
          <div className="pillar-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
            </svg>
          </div>
          <h4 className="pillar-title">Explainable Risk Engine</h4>
          <p className="pillar-text">
            Deterministic rule-based scoring evaluating classical vulnerabilities and post-quantum exposure with exact factor attribution.
          </p>
        </div>

        <div className="pillar-card">
          <div className="pillar-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polygon points="12 2 2 7 12 12 22 7 12 2" />
              <polyline points="2 17 12 22 22 17" />
              <polyline points="2 12 12 17 22 12" />
            </svg>
          </div>
          <h4 className="pillar-title">NIST PQC Recommendations</h4>
          <p className="pillar-text">
            Purpose-aware migration mapping to FIPS 203 (ML-KEM), FIPS 204 (ML-DSA), and FIPS 205 (SLH-DSA) with priority tiers.
          </p>
        </div>

        <div className="pillar-card">
          <div className="pillar-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
            </svg>
          </div>
          <h4 className="pillar-title">CycloneDX 1.6 CBOM</h4>
          <p className="pillar-text">
            Standardized machine-readable Cryptography Bill of Materials embedding source code evidence and risk posture.
          </p>
        </div>
      </div>

      <div className="empty-cta-box">
        <span>Ready to evaluate your cryptographic posture?</span>
        <button className="btn-primary" onClick={onQuickScan} disabled={isLoading}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <polygon points="5 3 19 12 5 21 5 3" />
          </svg>
          Scan Default Target (<code>test-target</code>)
        </button>
      </div>
    </div>
  );
};
