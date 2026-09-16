import React from 'react';

interface LoadingStateProps {
  target: string;
}

export const LoadingState: React.FC<LoadingStateProps> = ({ target }) => {
  return (
    <div className="card-panel loading-panel">
      <div className="loading-spinner-wrap">
        <div className="radar-spinner"></div>
      </div>
      <h3 className="loading-title">Analyzing Cryptographic Implementations</h3>
      <p className="loading-sub">
        Scanning Java source ASTs in <code className="target-path">{target}</code>
      </p>

      <div className="pipeline-steps-indicator">
        <div className="step-item active">
          <span className="step-dot"></span>
          <span>1. Discovering Cryptography (AST Parser)</span>
        </div>
        <div className="step-connector-v"></div>
        <div className="step-item active">
          <span className="step-dot"></span>
          <span>2. Assessing Risk (Scoring Engine)</span>
        </div>
        <div className="step-connector-v"></div>
        <div className="step-item active">
          <span className="step-dot"></span>
          <span>3. Evaluating PQC Migration (NIST Standards)</span>
        </div>
        <div className="step-connector-v"></div>
        <div className="step-item active">
          <span className="step-dot"></span>
          <span>4. Generating CycloneDX-inspired CBOM</span>
        </div>
      </div>
    </div>
  );
};
