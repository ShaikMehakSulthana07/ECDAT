import React, { useState, useMemo } from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface CryptoInventoryViewProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
  onSelectFinding: (index: number) => void;
  selectedIndex: number | null;
  searchQuery?: string;
}

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
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [lifecycleFilter, setLifecycleFilter] = useState<string>('ALL');
  const [purposeFilter, setPurposeFilter] = useState<string>('ALL');
  const [sortField, setSortField] = useState<'algorithm' | 'risk' | 'file' | 'purpose'>('risk');
  const [sortAsc, setSortAsc] = useState(false);

  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 15;

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
        const evidenceMatch = item.finding.evidence.toLowerCase().includes(q);
        const purposeMatch = item.finding.purpose.toLowerCase().includes(q);
        const catMatch = (item.finding.assetCategory || '').toLowerCase().includes(q);
        if (!algoMatch && !variantMatch && !fileMatch && !evidenceMatch && !purposeMatch && !catMatch) {
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

      // Category filter
      if (categoryFilter !== 'ALL') {
        if (item.finding.assetCategory !== categoryFilter) {
          return false;
        }
      }

      // Lifecycle filter
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
  }, [items, effectiveSearch, riskFilter, quantumFilter, categoryFilter, lifecycleFilter, purposeFilter]);

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
      } else if (sortField === 'file') {
        cmp = a.finding.file.localeCompare(b.finding.file);
      } else if (sortField === 'purpose') {
        cmp = a.finding.purpose.localeCompare(b.finding.purpose);
      }
      return sortAsc ? cmp : -cmp;
    });
    return list;
  }, [filteredItems, sortField, sortAsc]);

  // Pagination
  const totalPages = Math.ceil(sortedItems.length / pageSize) || 1;
  const paginatedItems = sortedItems.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  const uniqueCategories = useMemo(() => {
    return Array.from(new Set(findings.map((f) => f.assetCategory).filter(Boolean)));
  }, [findings]);

  const uniquePurposes = useMemo(() => {
    return Array.from(new Set(findings.map((f) => f.purpose)));
  }, [findings]);

  const handleSort = (field: 'algorithm' | 'risk' | 'file' | 'purpose') => {
    if (sortField === field) {
      setSortAsc(!sortAsc);
    } else {
      setSortField(field);
      setSortAsc(false);
    }
  };

  const resetFilters = () => {
    setLocalSearch('');
    setRiskFilter('ALL');
    setQuantumFilter('ALL');
    setCategoryFilter('ALL');
    setLifecycleFilter('ALL');
    setPurposeFilter('ALL');
    setCurrentPage(1);
  };

  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Cryptographic Asset Inventory</h1>
          <p className="view-subtitle">
            Comprehensive catalog of discovered cryptographic primitives, key lengths, purpose classifications, and source code locations.
          </p>
        </div>
      </div>

      <div className="card-panel">
        {/* Filter Controls Toolbar */}
        <div className="filter-toolbar">
          <div className="filter-controls-group">
            <input
              type="text"
              className="topbar-search-input"
              style={{ width: '240px' }}
              placeholder="Search assets &amp; code..."
              value={localSearch}
              onChange={(e) => {
                setLocalSearch(e.target.value);
                setCurrentPage(1);
              }}
            />

            <select
              className="filter-select"
              value={riskFilter}
              onChange={(e) => {
                setRiskFilter(e.target.value);
                setCurrentPage(1);
              }}
            >
              <option value="ALL">All Risk Levels</option>
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
              <option value="ALL">All Quantum Risk</option>
              <option value="HIGH">Quantum High (Vulnerable)</option>
              <option value="LOW">Quantum Low (Resistant)</option>
              <option value="NONE">None</option>
            </select>

            <select
              className="filter-select"
              value={categoryFilter}
              onChange={(e) => {
                setCategoryFilter(e.target.value);
                setCurrentPage(1);
              }}
            >
              <option value="ALL">All Categories</option>
              {uniqueCategories.map((c) => (
                <option key={c} value={c}>
                  {c?.replace('_', ' ')}
                </option>
              ))}
            </select>

            <select
              className="filter-select"
              value={lifecycleFilter}
              onChange={(e) => {
                setLifecycleFilter(e.target.value);
                setCurrentPage(1);
              }}
            >
              <option value="ALL">All Lifecycles</option>
              <option value="ACTIVE">Active</option>
              <option value="DEPRECATED">Deprecated</option>
              <option value="UNKNOWN">Unknown</option>
            </select>

            <select
              className="filter-select"
              value={purposeFilter}
              onChange={(e) => {
                setPurposeFilter(e.target.value);
                setCurrentPage(1);
              }}
            >
              <option value="ALL">All Purposes</option>
              {uniquePurposes.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </div>

          {(effectiveSearch || riskFilter !== 'ALL' || quantumFilter !== 'ALL' || categoryFilter !== 'ALL' || lifecycleFilter !== 'ALL' || purposeFilter !== 'ALL') && (
            <button className="btn-secondary btn-sm" onClick={resetFilters}>
              Reset Filters
            </button>
          )}
        </div>

        {/* Enterprise Data Table */}
        <div className="table-container">
          <table className="enterprise-table">
            <thead>
              <tr>
                <th style={{ width: '40px' }}>#</th>
                <th onClick={() => handleSort('algorithm')} style={{ cursor: 'pointer' }}>
                  Algorithm {sortField === 'algorithm' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th>Variant</th>
                <th onClick={() => handleSort('purpose')} style={{ cursor: 'pointer' }}>
                  Purpose {sortField === 'purpose' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th>Category</th>
                <th>Lifecycle</th>
                <th>Key Size</th>
                <th onClick={() => handleSort('risk')} style={{ cursor: 'pointer' }}>
                  Risk {sortField === 'risk' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th>Quantum Risk</th>
                <th>Confidence</th>
                <th onClick={() => handleSort('file')} style={{ cursor: 'pointer' }}>
                  Source {sortField === 'file' ? (sortAsc ? '▲' : '▼') : ''}
                </th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {paginatedItems.length === 0 ? (
                <tr>
                  <td colSpan={12} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    No cryptographic assets match the current filter criteria.
                  </td>
                </tr>
              ) : (
                paginatedItems.map((item) => {
                  const isSelected = selectedIndex === item.index;
                  const riskLevel = item.risk?.riskLevel || 'LOW';
                  const quantumRisk = item.risk?.quantumRisk || 'NONE';
                  const category = item.finding.assetCategory || 'UNKNOWN';
                  const lifecycle = item.finding.lifecycleStatus || 'UNKNOWN';

                  return (
                    <tr
                      key={item.index}
                      className={`clickable-row ${isSelected ? 'selected' : ''}`}
                      onClick={() => onSelectFinding(item.index)}
                    >
                      <td className="font-mono text-muted">{item.index + 1}</td>
                      <td>
                        <span className="algo-text">{item.finding.algorithm}</span>
                      </td>
                      <td>
                        {item.finding.variant ? (
                          <span className="tag-subtle">{item.finding.variant}</span>
                        ) : (
                          <span className="text-muted">—</span>
                        )}
                      </td>
                      <td>
                        <span className="tag-subtle">{item.finding.purpose}</span>
                      </td>
                      <td>
                        <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                          {category.replace('_', ' ')}
                        </span>
                      </td>
                      <td>
                        <span className={`risk-badge ${lifecycle === 'DEPRECATED' ? 'critical' : lifecycle === 'ACTIVE' ? 'low' : 'medium'}`}>
                          {lifecycle}
                        </span>
                      </td>
                      <td className="font-mono">
                        {item.finding.keySize ? `${item.finding.keySize} bits` : '—'}
                      </td>
                      <td>
                        <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                          {riskLevel} {item.risk ? `(${item.risk.riskScore})` : ''}
                        </span>
                      </td>
                      <td>
                        <span className={`quantum-badge ${quantumRisk.toLowerCase()}`}>
                          {quantumRisk === 'HIGH' ? 'VULNERABLE' : quantumRisk === 'LOW' ? 'SAFE' : quantumRisk}
                        </span>
                      </td>
                      <td>
                        <span className="tag-subtle font-mono">{item.finding.confidence}</span>
                      </td>
                      <td>
                        <span className="font-mono text-muted" style={{ fontSize: '11px' }} title={item.finding.file}>
                          {item.finding.file.split(/[\\/]/).pop()}:{item.finding.line}
                        </span>
                      </td>
                      <td>
                        <button
                          className="btn-secondary btn-sm"
                          style={{ padding: '3px 8px', fontSize: '11px' }}
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

        {/* Pagination Bar */}
        <div className="table-stats-bar">
          <span>
            Showing <strong>{paginatedItems.length}</strong> of <strong>{sortedItems.length}</strong> matching assets (Total: {findings.length})
          </span>
          {totalPages > 1 && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <button
                className="btn-secondary btn-sm"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(currentPage - 1)}
              >
                Previous
              </button>
              <span className="font-mono" style={{ fontSize: '12px' }}>
                Page {currentPage} of {totalPages}
              </span>
              <button
                className="btn-secondary btn-sm"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(currentPage + 1)}
              >
                Next
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
