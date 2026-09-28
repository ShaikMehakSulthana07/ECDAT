import React from 'react';
import type { Provenance } from '../types/analysis';

interface ProvenanceBadgeProps {
  provenance: Provenance;
  size?: 'sm' | 'md';
}

const ProvenanceBadge: React.FC<ProvenanceBadgeProps> = ({ provenance, size = 'sm' }) => {
  const getBadgeStyle = (prov: Provenance) => {
    switch (prov) {
      case 'OBSERVED':
        return { bg: 'var(--badge-success-bg)', color: 'var(--badge-success-text)', label: 'Observed' };
      case 'INFERRED':
        return { bg: 'var(--badge-info-bg)', color: 'var(--badge-info-text)', label: 'Inferred' };
      case 'DEPENDENCY_METADATA':
        return { bg: 'var(--badge-warning-bg)', color: 'var(--badge-warning-text)', label: 'Dependency' };
      case 'USER_PROVIDED':
        return { bg: 'var(--badge-primary-bg)', color: 'var(--badge-primary-text)', label: 'User' };
      case 'DEFAULT_ASSUMPTION':
        return { bg: 'var(--badge-neutral-bg)', color: 'var(--badge-neutral-text)', label: 'Default' };
      case 'UNKNOWN':
        return { bg: 'var(--badge-muted-bg)', color: 'var(--badge-muted-text)', label: 'Unknown' };
      default:
        return { bg: 'var(--badge-muted-bg)', color: 'var(--badge-muted-text)', label: 'Unknown' };
    }
  };

  const style = getBadgeStyle(provenance);
  const padding = size === 'sm' ? '2px 6px' : '4px 8px';
  const fontSize = size === 'sm' ? '10px' : '11px';

  return (
    <span
      style={{
        display: 'inline-block',
        padding,
        fontSize,
        fontWeight: 500,
        backgroundColor: style.bg,
        color: style.color,
        borderRadius: '4px',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
      }}
      title={`Provenance: ${provenance}`}
    >
      {style.label}
    </span>
  );
};

export default ProvenanceBadge;
