import React, { useState, useMemo } from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface PQCMigrationViewProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
  onSelectFinding: (index: number) => void;
}

export const PQCMigrationView: React.FC<PQCMigrationViewProps> = ({
  findings,
  riskAssessments,
  pqcRecommendations,
  onSelectFinding,
}) => {
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');
  const [strategyFilter, setStrategyFilter] = useState<string>('ALL');

  const items = useMemo(() => {
    return pqcRecommendations.map((rec, idx) => ({
      index: idx,
      rec,
      finding: findings[idx],
      risk: riskAssessments[idx],
    }));
  }, [pqcRecommendations, findings, riskAssessments]);

  const filteredItems = useMemo(() => {
    return items.filter(({ rec }) => {
      if (statusFilter !== 'ALL' && rec.recommendationStatus !== statusFilter) {
        return false;
      }
      if (priorityFilter !== 'ALL' && rec.migrationPriority !== priorityFilter) {
        return false;
      }
      if (strategyFilter !== 'ALL' && (rec.migrationStrategy || 'DIRECT_PQC') !== strategyFilter) {
        return false;
      }
      return true;
    });
  }, [items, statusFilter, priorityFilter, strategyFilter]);

  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Post-Quantum Cryptography (PQC) Migration</h1>
          <p className="view-subtitle">
            Target cryptographic primitives mapped to standardized NIST Post-Quantum Cryptography algorithms (FIPS 203, 204, 205).
          </p>
        </div>
      </div>

      {/* NIST Standards Reference Banner */}
      <div className="card-panel" style={{ borderLeft: '4px solid var(--pqc-accent)' }}>
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">NIST Post-Quantum Cryptography Standards Alignment</h3>
            <span className="card-panel-sub">
              Deterministic mappings distinguishing digital signatures (ML-DSA), key encapsulation (ML-KEM), and stateless hash signatures (SLH-DSA).
            </span>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '14px' }}>
          <div style={{ padding: '12px', background: 'var(--bg-surface-subtle)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--pqc-accent)', textTransform: 'uppercase' }}>
              FIPS 203 · ML-KEM
            </div>
            <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', marginTop: '2px' }}>
              Module-Lattice Key Encapsulation
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
              Target for ECDH, DH, and asymmetric key establishment.
            </div>
          </div>

          <div style={{ padding: '12px', background: 'var(--bg-surface-subtle)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--pqc-accent)', textTransform: 'uppercase' }}>
              FIPS 204 · ML-DSA
            </div>
            <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', marginTop: '2px' }}>
              Module-Lattice Digital Signatures
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
              Target for RSA signatures, ECDSA, and certificate signing.
            </div>
          </div>

          <div style={{ padding: '12px', background: 'var(--bg-surface-subtle)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-sm)' }}>
            <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--pqc-accent)', textTransform: 'uppercase' }}>
              FIPS 205 · SLH-DSA
            </div>
            <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', marginTop: '2px' }}>
              Stateless Hash-Based Signatures
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
              Alternative signature standard with zero lattice assumptions.
            </div>
          </div>
        </div>
      </div>

      {/* Migration Table & Filters */}
      <div className="card-panel">
        <div className="filter-toolbar">
          <div className="filter-controls-group">
            <select
              className="filter-select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">All Recommendation Statuses</option>
              <option value="RECOMMENDED">Recommended (Action Required)</option>
              <option value="CONDITIONAL">Conditional</option>
              <option value="NEEDS_ANALYSIS">Needs Analysis</option>
              <option value="NOT_REQUIRED">Not Required</option>
            </select>

            <select
              className="filter-select"
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
            >
              <option value="ALL">All Priorities</option>
              <option value="CRITICAL">Critical Priority</option>
              <option value="HIGH">High Priority</option>
              <option value="MEDIUM">Medium Priority</option>
              <option value="LOW">Low Priority</option>
            </select>

            <select
              className="filter-select"
              value={strategyFilter}
              onChange={(e) => setStrategyFilter(e.target.value)}
            >
              <option value="ALL">All Migration Strategies</option>
              <option value="DIRECT_PQC">Direct PQC</option>
              <option value="HYBRID">Hybrid Transition</option>
              <option value="NEEDS_ANALYSIS">Needs Analysis</option>
              <option value="NO_ACTION">No Action</option>
            </select>
          </div>

          {(statusFilter !== 'ALL' || priorityFilter !== 'ALL' || strategyFilter !== 'ALL') && (
            <button
              className="btn-secondary btn-sm"
              onClick={() => {
                setStatusFilter('ALL');
                setPriorityFilter('ALL');
                setStrategyFilter('ALL');
              }}
            >
              Reset Filters
            </button>
          )}
        </div>

        <div className="table-container">
          <table className="enterprise-table">
            <thead>
              <tr>
                <th>Current Algorithm</th>
                <th>Declared Purpose</th>
                <th>Risk Tier</th>
                <th>Recommended PQC Target</th>
                <th>Migration Strategy</th>
                <th>Priority</th>
                <th>Recommendation Status</th>
                <th>Source Location</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.length === 0 ? (
                <tr>
                  <td colSpan={9} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    No PQC recommendations match the selected filters.
                  </td>
                </tr>
              ) : (
                filteredItems.map(({ index, rec, finding, risk }) => {
                  const riskLevel = risk?.riskLevel || 'LOW';
                  const strategy = rec.migrationStrategy || (rec.recommendedAlgorithm ? 'DIRECT_PQC' : 'NEEDS_ANALYSIS');

                  return (
                    <tr
                      key={index}
                      className="clickable-row"
                      onClick={() => onSelectFinding(index)}
                    >
                      <td>
                        <span className="algo-text">{rec.currentAlgorithm}</span>
                      </td>
                      <td>
                        <span className="tag-subtle">{rec.currentPurpose}</span>
                      </td>
                      <td>
                        <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                          {riskLevel}
                        </span>
                      </td>
                      <td>
                        {rec.recommendedAlgorithm ? (
                          <strong className="font-mono" style={{ color: 'var(--pqc-accent)' }}>
                            {rec.recommendedAlgorithm}
                          </strong>
                        ) : (
                          <span className="text-muted font-mono" style={{ fontSize: '11px' }}>
                            {rec.recommendationStatus === 'NOT_REQUIRED' ? 'Not Required' : 'Needs Analysis'}
                          </span>
                        )}
                      </td>
                      <td>
                        <span className="tag-subtle font-mono">{strategy}</span>
                      </td>
                      <td>
                        <span className={`risk-badge ${rec.migrationPriority.toLowerCase()}`}>
                          {rec.migrationPriority}
                        </span>
                      </td>
                      <td>
                        <span className={`pqc-status-badge ${rec.recommendationStatus.toLowerCase()}`}>
                          {rec.recommendationStatus}
                        </span>
                      </td>
                      <td>
                        <span className="font-mono text-muted" style={{ fontSize: '11px' }}>
                          {finding.file.split(/[\\/]/).pop()}:{finding.line}
                        </span>
                      </td>
                      <td>
                        <button
                          className="btn-secondary btn-sm"
                          style={{ padding: '3px 8px', fontSize: '11px' }}
                          onClick={(e) => {
                            e.stopPropagation();
                            onSelectFinding(index);
                          }}
                        >
                          Inspect
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
