import React from 'react';
import type { CryptoFinding, RiskAssessment } from '../types/analysis';

interface DependenciesViewProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
}

interface AggregatedDependency {
  library: string;
  usageCategory: string;
  primitives: string[];
  maxRiskScore: number;
  highestRiskLevel: string;
  count: number;
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

export const DependenciesView: React.FC<DependenciesViewProps> = ({
  findings,
  riskAssessments,
}) => {
  // Aggregate findings by library
  const dependenciesMap = findings.reduce((acc, finding, idx) => {
    const libName = finding.library || 'Java Cryptography Architecture (JCA)';
    const risk = riskAssessments[idx];
    const riskScore = risk?.riskScore || 0;
    const riskLevel = risk?.riskLevel || 'LOW';

    if (!acc[libName]) {
      acc[libName] = {
        library: libName,
        usageCategory: finding.usageCategory || 'DIRECT_USAGE',
        primitives: [],
        maxRiskScore: riskScore,
        highestRiskLevel: riskLevel,
        count: 0,
      };
    }

    acc[libName].count += 1;
    acc[libName].maxRiskScore = Math.max(acc[libName].maxRiskScore, riskScore);
    if (!acc[libName].primitives.includes(finding.algorithm)) {
      acc[libName].primitives.push(finding.algorithm);
    }
    return acc;
  }, {} as Record<string, AggregatedDependency>);

  const dependencies = Object.values(dependenciesMap);

  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Cryptographic Dependencies</h1>
          <p className="view-subtitle">
            Cryptographic libraries, security providers, and build dependencies identified across source code and build descriptors.
          </p>
        </div>
      </div>

      <div className="card-panel">
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">Cryptographic Security Libraries &amp; Frameworks ({dependencies.length})</h3>
            <span className="card-panel-sub">
              Identified through static AST calls and Maven dependency analysis.
            </span>
          </div>
        </div>

        <div className="table-container">
          <table className="enterprise-table">
            <thead>
              <tr>
                <th style={{ width: '25%' }}>Library / Security Provider</th>
                <th style={{ width: '15%' }}>Usage Mode</th>
                <th style={{ width: '22%' }}>Cryptographic Primitives</th>
                <th style={{ width: '10%' }}>Instances</th>
                <th style={{ width: '10%' }}>Peak Risk</th>
                <th style={{ width: '10%' }}>Risk Tier</th>
                <th style={{ width: '18%' }}>PQC Posture</th>
              </tr>
            </thead>
            <tbody>
              {dependencies.map((dep, idx) => (
                <tr key={idx}>
                  <td>
                    <strong className="algo-primary-title">{dep.library}</strong>
                  </td>
                  <td>
                    <span className="table-purpose-text">{formatTitleCase(dep.usageCategory)}</span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                      {dep.primitives.map((p) => (
                        <span key={p} className="matrix-chip-algo font-mono font-bold" style={{ fontSize: '11.5px' }}>
                          {p}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="font-mono" style={{ fontSize: '13px' }}>{dep.count}</td>
                  <td className="font-mono">
                    <span style={{ color: dep.highestRiskLevel === 'CRITICAL' ? 'var(--risk-critical)' : 'var(--text-primary)', fontWeight: 700, fontSize: '13px' }}>
                      {dep.maxRiskScore}/100
                    </span>
                  </td>
                  <td>
                    <span className={`risk-badge ${dep.highestRiskLevel.toLowerCase()}`}>
                      {dep.highestRiskLevel}
                    </span>
                  </td>
                  <td>
                    <span className="table-threat-text" style={{ fontSize: '11.5px' }}>
                      Migrate to NIST FIPS 203/204/205
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
