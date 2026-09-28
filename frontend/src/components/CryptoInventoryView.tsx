import React, { useState, useMemo } from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface CryptoInventoryViewProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
  onSelectFinding: (index: number, tab?: 'overview' | 'mosca' | 'pqc' | 'evidence' | 'why_risky') => void;
  selectedIndex: number | null;
  searchQuery?: string;
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

export const CryptoInventoryView: React.FC<CryptoInventoryViewProps> = ({
  findings,
  riskAssessments,
  pqcRecommendations,
  onSelectFinding,
  selectedIndex,
  searchQuery = '',
}) => {
  const [localSearch, setLocalSearch] = useState('');
  const [riskFilter, setRiskFilter] = useState<string>('ALL');
  const [quantumFilter, setQuantumFilter] = useState<string>('ALL');
  const [purposeFilter, setPurposeFilter] = useState<string>('ALL');
  const [confidenceFilter, setConfidenceFilter] = useState<string>('ALL');
  const [sortField, setSortField] = useState<'algorithm' | 'risk' | 'file' | 'purpose'>('risk');
  const [sortAsc, setSortAsc] = useState(false);

  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 12;

  const effectiveSearch = searchQuery || localSearch;

  // Combined Finding Items
  const items = useMemo(() => {
    return findings.map((finding, idx) => ({
      index: idx,
      finding,
      risk: riskAssessments[idx] || null,
      pqc: pqcRecommendations[idx] || null,
    }));
  }, [findings, riskAssessments, pqcRecommendations]);

  // Filtered Items
  const filteredItems = useMemo(() => {
    return items.filter((item) => {
      // Search
      if (effectiveSearch.trim()) {
        const q = effectiveSearch.toLowerCase();
        const algoMatch = item.finding.algorithm.toLowerCase().includes(q);
        const variantMatch = (item.finding.variant || '').toLowerCase().includes(q);
        const fileMatch = item.finding.file.toLowerCase().includes(q);
        const purposeMatch = item.finding.purpose.toLowerCase().includes(q);
        if (!algoMatch && !variantMatch && !fileMatch && !purposeMatch) {
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
        const isVuln = item.risk?.quantumRisk === 'HIGH' || item.risk?.quantumRiskResult?.quantumVulnerable;
        if (quantumFilter === 'VULNERABLE' && !isVuln) return false;
        if (quantumFilter === 'SAFE' && isVuln) return false;
      }

      // Purpose filter
      if (purposeFilter !== 'ALL') {
        if (item.finding.purpose !== purposeFilter) {
          return false;
        }
      }

      // Confidence filter
      if (confidenceFilter !== 'ALL') {
        if (item.finding.confidence !== confidenceFilter) {
          return false;
        }
      }

      return true;
    });
  }, [items, effectiveSearch, riskFilter, quantumFilter, purposeFilter, confidenceFilter]);

  // Sorted Items
  const sortedItems = useMemo(() => {
    const list = [...filteredItems];
    list.sort((a, b) => {
      let cmp = 0;
      if (sortField === 'algorithm') {
        cmp = a.finding.algorithm.localeCompare(b.finding.algorithm);
      } else if (sortField === 'risk') {
        const scoreA = a.risk?.riskScore || 0;
        const scoreB = b.risk?.riskScore || 0;
        cmp = scoreA - scoreB;
      } else if (sortField === 'purpose') {
        cmp = a.finding.purpose.localeCompare(b.finding.purpose);
      } else if (sortField === 'file') {
        cmp = a.finding.file.localeCompare(b.finding.file);
      }
      return sortAsc ? cmp : -cmp;
    });
    return list;
  }, [filteredItems, sortField, sortAsc]);

  // Pagination
  const totalPages = Math.max(1, Math.ceil(sortedItems.length / pageSize));
  const paginatedItems = sortedItems.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  const handleSort = (field: 'algorithm' | 'risk' | 'file' | 'purpose') => {
    if (sortField === field) {
      setSortAsc(!sortAsc);
    } else {
      setSortField(field);
      setSortAsc(false);
    }
  };

  const purposes = useMemo(() => {
    return Array.from(new Set(findings.map((f) => f.purpose)));
  }, [findings]);

  return (
    <div>
      {/* Header */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Crypto Inventory</h1>
          <p className="view-subtitle">Discovered cryptographic assets, primitives, key sizes, and source locations</p>
        </div>
      </div>

      {/* Main Inventory Card */}
      <div className="card-panel">
        {/* Filter Toolbar matching Screen 6 Reference */}
        <div className="filter-toolbar">
          <div className="filter-controls-group">
            <div className="search-input-wrap">
              <svg className="search-icon-inside" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <input
                type="text"
                className="topbar-search-input search-input-pad"
                placeholder="Search algorithm, purpose, or file..."
                value={localSearch}
                onChange={(e) => {
                  setLocalSearch(e.target.value);
                  setCurrentPage(1);
                }}
              />
            </div>

            <select
              className="filter-select"
              value={riskFilter}
              onChange={(e) => {
                setRiskFilter(e.target.value);
                setCurrentPage(1);
              }}
            >
              <option value="ALL">All Risks</option>
              <option value="CRITICAL">Critical Risk</option>
              <option value="HIGH">High Risk</option>
              <option value="MEDIUM">Medium Risk</option>
              <option value="LOW">Low Risk</option>
            </select>

            <select
              className="filter-select"
              value={quantumFilter}
              onChange={(e) => {
                setQuantumFilter(e.target.value);
                setCurrentPage(1);
              }}
            >
              <option value="ALL">All Quantum Status</option>
              <option value="VULNERABLE">Vulnerable</option>
              <option value="SAFE">Safe</option>
            </select>

            {purposes.length > 1 && (
              <select
                className="filter-select"
                value={purposeFilter}
                onChange={(e) => {
                  setPurposeFilter(e.target.value);
                  setCurrentPage(1);
                }}
              >
                <option value="ALL">All Purposes</option>
                {purposes.map((p) => (
                  <option key={p} value={p}>{formatTitleCase(p)}</option>
                ))}
              </select>
            )}

            <select
              className="filter-select"
              value={confidenceFilter}
              onChange={(e) => {
                setConfidenceFilter(e.target.value);
                setCurrentPage(1);
              }}
            >
              <option value="ALL">All Confidence</option>
              <option value="HIGH">High Confidence</option>
              <option value="MEDIUM">Medium Confidence</option>
              <option value="LOW">Low Confidence</option>
            </select>
          </div>
        </div>

        {/* Table */}
        <div className="table-container">
          <table className="enterprise-table">
            <thead>
              <tr>
                <th style={{ width: '24%', cursor: 'pointer' }} onClick={() => handleSort('algorithm')}>
                  Algorithm {sortField === 'algorithm' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th style={{ width: '20%', cursor: 'pointer' }} onClick={() => handleSort('purpose')}>
                  Purpose {sortField === 'purpose' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th style={{ width: '12%', cursor: 'pointer' }} onClick={() => handleSort('risk')}>
                  Risk {sortField === 'risk' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th style={{ width: '16%' }}>Quantum Status</th>
                <th style={{ width: '20%', cursor: 'pointer' }} onClick={() => handleSort('file')}>
                  Location {sortField === 'file' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th style={{ width: '8%', textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {paginatedItems.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '36px', color: 'var(--text-muted)' }}>
                    No cryptographic assets match the selected filter criteria.
                  </td>
                </tr>
              ) : (
                paginatedItems.map(({ index, finding, risk }) => {
                  const riskLevel = risk?.riskLevel || 'LOW';
                  const isQuantumVuln = risk?.quantumRisk === 'HIGH' || risk?.quantumRiskResult?.quantumVulnerable;
                  const isSelected = selectedIndex === index;
                  const purposeDisplay = formatTitleCase(finding.purpose);

                  return (
                    <tr
                      key={index}
                      className={`clickable-row ${isSelected ? 'row-selected' : ''}`}
                      onClick={() => onSelectFinding(index)}
                    >
                      <td>
                        <div className="algo-cell-block">
                          <span className="algo-primary-title">{finding.algorithm}</span>
                          {(finding.keySize || (finding.variant && finding.variant !== finding.algorithm) || finding.mode || finding.padding) && (
                            <span className="algo-secondary-sub font-mono">
                              {finding.keySize ? `${finding.keySize} bits` : ''}
                              {finding.keySize && (finding.mode || (finding.variant && finding.variant !== finding.algorithm)) ? ' · ' : ''}
                              {finding.mode
                                ? `${finding.mode}${finding.padding ? ` / ${finding.padding}` : ''}`
                                : (finding.variant && finding.variant !== finding.algorithm ? finding.variant : '')}
                            </span>
                          )}
                        </div>
                      </td>
                      <td>
                        <span className="table-purpose-text">{purposeDisplay}</span>
                      </td>
                      <td>
                        <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                          {riskLevel}
                        </span>
                      </td>
                      <td>
                        <span className={`quantum-badge ${isQuantumVuln ? 'vulnerable' : 'safe'}`}>
                          <span className={`status-dot-sm ${isQuantumVuln ? 'vuln' : 'safe'}`}></span>
                          {isQuantumVuln ? 'Vulnerable' : 'Safe'}
                        </span>
                      </td>
                      <td>
                        <div className="source-location-cell font-mono" title={finding.file}>
                          <span className="source-file">{finding.file.split(/[\\/]/).pop()}</span>
                          <span className="source-line">:{finding.line}</span>
                        </div>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '6px', alignItems: 'center' }}>
                          <button
                            className="btn-secondary btn-sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              onSelectFinding(index, 'why_risky');
                            }}
                            title="Why is this risky? View 8-step evidence chain"
                            style={{ borderColor: 'var(--primary-border)', color: 'var(--accent-purple)' }}
                          >
                            Why Risky?
                          </button>
                          <button
                            className="btn-secondary btn-sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              onSelectFinding(index);
                            }}
                          >
                            View
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Footer Stats & Pagination matching Reference */}
        <div className="table-footer-bar">
          <div className="table-stats-info">
            Showing <strong>{paginatedItems.length}</strong> of <strong>{sortedItems.length}</strong> assets (Total: {findings.length})
          </div>

          {totalPages > 1 && (
            <div className="table-pagination">
              <button
                className="pagination-btn"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              >
                ‹
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
                <button
                  key={p}
                  className={`pagination-page-btn ${currentPage === p ? 'active' : ''}`}
                  onClick={() => setCurrentPage(p)}
                >
                  {p}
                </button>
              ))}
              <button
                className="pagination-btn"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
              >
                ›
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
