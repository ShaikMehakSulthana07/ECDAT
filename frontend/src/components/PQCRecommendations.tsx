import React, { useState, useMemo } from 'react';
import type { PQCRecommendation, CryptoFinding } from '../types/analysis';

interface PQCRecommendationsProps {
  pqcRecommendations: PQCRecommendation[];
  findings: CryptoFinding[];
  onSelectFinding: (index: number) => void;
}

export const PQCRecommendations: React.FC<PQCRecommendationsProps> = ({
  pqcRecommendations,
  findings,
  onSelectFinding,
}) => {
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL');

  const items = useMemo(() => {
    return pqcRecommendations.map((rec, idx) => ({
      index: idx,
      rec,
      finding: findings[idx],
    }));
  }, [pqcRecommendations, findings]);

  const filteredItems = useMemo(() => {
    return items.filter(({ rec }) => {
      if (statusFilter !== 'ALL' && rec.recommendationStatus !== statusFilter) {
        return false;
      }
      if (priorityFilter !== 'ALL' && rec.migrationPriority !== priorityFilter) {
        return false;
      }
      return true;
    });
  }, [items, statusFilter, priorityFilter]);

  return (
    <div className="pqc-view-container">
      {/* Standards Header Banner */}
      <div className="card-panel nist-standards-banner">
        <div className="standards-info">
          <h3 className="panel-title">NIST Post-Quantum Cryptography Standards Alignment</h3>
          <p className="panel-sub">
            Purpose-aware migration recommendations aligned with finalized NIST Post-Quantum Cryptography standards:
          </p>
          <div className="standards-chips">
            <div className="standard-chip">
              <strong>FIPS 203</strong>
              <span>ML-KEM (Module-Lattice Key Encapsulation)</span>
            </div>
            <div className="standard-chip">
              <strong>FIPS 204</strong>
              <span>ML-DSA (Module-Lattice Digital Signatures)</span>
            </div>
            <div className="standard-chip">
              <strong>FIPS 205</strong>
              <span>SLH-DSA (Stateless Hash-Based Digital Signatures)</span>
            </div>
          </div>
        </div>
      </div>

      {/* Filter Controls */}
      <div className="card-panel">
        <div className="table-controls-header">
          <div className="panel-title-group">
            <h3 className="panel-title">Migration Candidates & Advisory Guidance</h3>
            <span className="panel-sub">
              Showing {filteredItems.length} of {pqcRecommendations.length} recommendations
            </span>
          </div>

          <div className="filter-group">
            <select
              className="filter-select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">All Statuses</option>
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
          </div>
        </div>

        {/* Cards Grid */}
        <div className="pqc-cards-grid">
          {filteredItems.length === 0 ? (
            <div className="empty-pqc-message">
              No PQC recommendations match the selected filters.
            </div>
          ) : (
            filteredItems.map(({ index, rec, finding }) => (
              <div
                key={index}
                className={`pqc-rec-card status-${rec.recommendationStatus.toLowerCase()}`}
                onClick={() => onSelectFinding(index)}
              >
                <div className="pqc-card-top">
                  <div className="current-algo-badge">
                    <span className="algo-name">{rec.currentAlgorithm}</span>
                    <span className="purpose-tag">{rec.currentPurpose}</span>
                  </div>

                  <div className="pqc-badges">
                    <span className={`pqc-status-badge ${rec.recommendationStatus.toLowerCase()}`}>
                      {rec.recommendationStatus}
                    </span>
                    <span className={`priority-badge ${rec.migrationPriority.toLowerCase()}`}>
                      {rec.migrationPriority} Priority
                    </span>
                  </div>
                </div>

                {/* Target Migration Block */}
                <div className="migration-target-row">
                  <div className="target-col">
                    <span className="target-label">Current State</span>
                    <div className="target-value current">
                      {rec.currentAlgorithm}
                      <span className="quantum-tag">{rec.quantumRisk} Quantum Risk</span>
                    </div>
                  </div>
                  <div className="migration-arrow">⟶</div>
                  <div className="target-col">
                    <span className="target-label">Recommended PQC Target</span>
                    <div className="target-value replacement">
                      {rec.recommendedAlgorithm ? (
                        <strong>{rec.recommendedAlgorithm}</strong>
                      ) : (
                        <span className="text-muted">No Direct PQC Replacement</span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Alternative algorithms */}
                {rec.alternativeAlgorithms && rec.alternativeAlgorithms.length > 0 && (
                  <div className="rec-field">
                    <span className="field-label">Alternative Standard:</span>
                    <div className="alt-algos">
                      {rec.alternativeAlgorithms.map((alt) => (
                        <span key={alt} className="badge-subtle">{alt}</span>
                      ))}
                    </div>
                  </div>
                )}

                {/* Rationale */}
                <div className="rec-field">
                  <span className="field-label">Rationale:</span>
                  <p className="rationale-text">{rec.rationale}</p>
                </div>

                {/* Considerations */}
                {rec.considerations && rec.considerations.length > 0 && (
                  <div className="rec-field">
                    <span className="field-label">Key Considerations:</span>
                    <ul className="mini-considerations">
                      {rec.considerations.slice(0, 2).map((c, i) => (
                        <li key={i}>{c}</li>
                      ))}
                      {rec.considerations.length > 2 && (
                        <li className="text-muted">+{rec.considerations.length - 2} more considerations (click to inspect)</li>
                      )}
                    </ul>
                  </div>
                )}

                <div className="pqc-card-footer">
                  <span className="source-location font-mono">
                    {finding.file.split(/[\\/]/).pop()}:Line {finding.line}
                  </span>
                  <button className="btn-link">View Finding Details →</button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
