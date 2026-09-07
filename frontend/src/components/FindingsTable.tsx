import React, { useState, useMemo } from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface FindingsTableProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
  onSelectFinding: (index: number) => void;
  selectedIndex: number | null;
}

export const FindingsTable: React.FC<FindingsTableProps> = ({
  findings,
  riskAssessments,
  pqcRecommendations,
  onSelectFinding,
  selectedIndex,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [riskFilter, setRiskFilter] = useState<string>('ALL');
  const [quantumFilter, setQuantumFilter] = useState<string>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [lifecycleFilter, setLifecycleFilter] = useState<string>('ALL');
  const [purposeFilter, setPurposeFilter] = useState<string>('ALL');

  // Build combined finding items
  const combinedItems = useMemo(() => {
    return findings.map((finding, idx) => {
      const risk = riskAssessments[idx] || null;
      const pqc = pqcRecommendations[idx] || null;
      return {
        index: idx,
        finding,
        risk,
        pqc,
      };
    });
  }, [findings, riskAssessments, pqcRecommendations]);

  // Filtered items
  const filteredItems = useMemo(() => {
    return combinedItems.filter((item) => {
      // Search
      if (searchTerm.trim()) {
        const term = searchTerm.toLowerCase();
        const algoMatch = item.finding.algorithm.toLowerCase().includes(term);
        const variantMatch = (item.finding.variant || '').toLowerCase().includes(term);
        const fileMatch = item.finding.file.toLowerCase().includes(term);
        const evidenceMatch = item.finding.evidence.toLowerCase().includes(term);
        const purposeMatch = item.finding.purpose.toLowerCase().includes(term);
        const categoryMatch = (item.finding.assetCategory || '').toLowerCase().includes(term);
        const lifecycleMatch = (item.finding.lifecycleStatus || '').toLowerCase().includes(term);
        if (!algoMatch && !variantMatch && !fileMatch && !evidenceMatch && !purposeMatch && !categoryMatch && !lifecycleMatch) {
          return false;
        }
      }

      // Risk filter
      if (riskFilter !== 'ALL') {
        if (!item.risk || item.risk.riskLevel !== riskFilter) {
          return false;
        }
      }

      // Quantum filter
      if (quantumFilter !== 'ALL') {
        if (!item.risk || item.risk.quantumRisk !== quantumFilter) {
          return false;
        }
      }

      // Category filter (Phase 7)
      if (categoryFilter !== 'ALL') {
        if (item.finding.assetCategory !== categoryFilter) {
          return false;
        }
      }

      // Lifecycle filter (Phase 7)
      if (lifecycleFilter !== 'ALL') {
        if (item.finding.lifecycleStatus !== lifecycleFilter) {
          return false;
        }
      }

      // Purpose filter
      if (purposeFilter !== 'ALL') {
        if (item.finding.purpose !== purposeFilter) {
          return false;
        }
      }

      return true;
    });
  }, [combinedItems, searchTerm, riskFilter, quantumFilter, categoryFilter, lifecycleFilter, purposeFilter]);

  const uniqueCategories = useMemo(() => {
    return Array.from(new Set(findings.map((f) => f.assetCategory).filter(Boolean)));
  }, [findings]);

  const uniquePurposes = useMemo(() => {
    return Array.from(new Set(findings.map((f) => f.purpose)));
  }, [findings]);

  return (
    <div className="card-panel">
      <div className="table-controls-header">
        <div className="search-bar-wrap">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            className="search-input"
            placeholder="Search by algorithm, file, category, lifecycle, code snippet..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {searchTerm && (
            <button className="clear-search-btn" onClick={() => setSearchTerm('')}>
              ×
            </button>
          )}
        </div>

        <div className="filter-group">
          {/* Asset Category Filter (Phase 7) */}
          <select
            className="filter-select"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            <option value="ALL">All Categories</option>
            {uniqueCategories.map((c) => (
              <option key={c} value={c}>
                {c?.replace('_', ' ')}
              </option>
            ))}
          </select>

          {/* Lifecycle Status Filter (Phase 7) */}
          <select
            className="filter-select"
            value={lifecycleFilter}
            onChange={(e) => setLifecycleFilter(e.target.value)}
          >
            <option value="ALL">All Lifecycles</option>
            <option value="ACTIVE">Active</option>
            <option value="DEPRECATED">Deprecated</option>
            <option value="UNKNOWN">Unknown</option>
          </select>

          {/* Risk Level Filter */}
          <select
            className="filter-select"
            value={riskFilter}
            onChange={(e) => setRiskFilter(e.target.value)}
          >
            <option value="ALL">All Risk Levels</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          {/* Quantum Risk Filter */}
          <select
            className="filter-select"
            value={quantumFilter}
            onChange={(e) => setQuantumFilter(e.target.value)}
          >
            <option value="ALL">All Quantum Risk</option>
            <option value="HIGH">Quantum High</option>
            <option value="LOW">Quantum Low</option>
            <option value="NONE">Quantum None</option>
          </select>

          {/* Purpose Filter */}
          <select
            className="filter-select"
            value={purposeFilter}
            onChange={(e) => setPurposeFilter(e.target.value)}
          >
            <option value="ALL">All Purposes</option>
            {uniquePurposes.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="table-results-info">
        <span>Showing <strong>{filteredItems.length}</strong> of <strong>{findings.length}</strong> enterprise cryptographic assets</span>
        {(searchTerm || riskFilter !== 'ALL' || quantumFilter !== 'ALL' || categoryFilter !== 'ALL' || lifecycleFilter !== 'ALL' || purposeFilter !== 'ALL') && (
          <button
            className="reset-filters-btn"
            onClick={() => {
              setSearchTerm('');
              setRiskFilter('ALL');
              setQuantumFilter('ALL');
              setCategoryFilter('ALL');
              setLifecycleFilter('ALL');
              setPurposeFilter('ALL');
            }}
          >
            Reset filters
          </button>
        )}
      </div>

      <div className="table-responsive">
        <table className="data-table interactive-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Algorithm</th>
              <th>Variant</th>
              <th>Asset Category</th>
              <th>Lifecycle</th>
              <th>Key Size</th>
              <th>Risk Level</th>
              <th>Quantum Risk</th>
              <th>Confidence</th>
              <th>Source Location</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.length === 0 ? (
              <tr>
                <td colSpan={11} className="table-empty-cell">
                  No cryptographic findings match your current search and filter criteria.
                </td>
              </tr>
            ) : (
              filteredItems.map((item) => {
                const isSelected = selectedIndex === item.index;
                const riskLevel = item.risk?.riskLevel || 'LOW';
                const quantumRisk = item.risk?.quantumRisk || 'NONE';
                const confidence = item.finding.confidence || 'HIGH';
                const category = item.finding.assetCategory || 'UNKNOWN';
                const lifecycle = item.finding.lifecycleStatus || 'UNKNOWN';

                return (
                  <tr
                    key={item.index}
                    className={`clickable-row ${isSelected ? 'row-selected' : ''}`}
                    onClick={() => onSelectFinding(item.index)}
                  >
                    <td className="font-mono text-muted">{item.index + 1}</td>
                    <td>
                      <span className="algo-name">{item.finding.algorithm}</span>
                    </td>
                    <td>
                      {item.finding.variant ? (
                        <span className="badge-subtle">{item.finding.variant}</span>
                      ) : (
                        <span className="text-muted">—</span>
                      )}
                    </td>
                    <td>
                      <span className="purpose-tag">{category.replace('_', ' ')}</span>
                    </td>
                    <td>
                      <span className={`lifecycle-badge ${lifecycle.toLowerCase()}`}>
                        {lifecycle}
                      </span>
                    </td>
                    <td>
                      {item.finding.keySize ? (
                        <span className="font-mono">{item.finding.keySize} bits</span>
                      ) : (
                        <span className="text-muted">—</span>
                      )}
                    </td>
                    <td>
                      <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                        {riskLevel} {item.risk ? `(${item.risk.riskScore})` : ''}
                      </span>
                    </td>
                    <td>
                      <span className={`quantum-badge ${quantumRisk.toLowerCase()}`}>
                        {quantumRisk}
                      </span>
                    </td>
                    <td>
                      <span className={`confidence-badge ${confidence.toLowerCase()}`}>
                        {confidence}
                      </span>
                    </td>
                    <td>
                      <div className="location-cell">
                        <span className="file-path" title={item.finding.file}>
                          {item.finding.file.split(/[\\/]/).pop()}
                        </span>
                        <span className="line-num font-mono">L:{item.finding.line}</span>
                      </div>
                    </td>
                    <td>
                      <button
                        className="btn-details"
                        onClick={(e) => {
                          e.stopPropagation();
                          onSelectFinding(item.index);
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
  );
};
