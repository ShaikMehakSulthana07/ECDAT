import React from 'react';

interface ErrorAlertProps {
  message: string;
  errorType?: string;
  status?: number;
  onDismiss?: () => void;
  onRetry?: () => void;
}

export const ErrorAlert: React.FC<ErrorAlertProps> = ({
  message,
  errorType,
  status,
  onDismiss,
  onRetry,
}) => {
  return (
    <div className="error-banner">
      <div className="error-icon">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
      </div>

      <div className="error-content">
        <div className="error-header-row">
          <strong className="error-title">Analysis Request Failed</strong>
          {status !== undefined && status > 0 && (
            <span className="error-status-badge font-mono">HTTP {status}</span>
          )}
          {errorType && <span className="error-type-badge">{errorType}</span>}
        </div>
        <p className="error-message">{message}</p>
      </div>

      <div className="error-actions">
        {onRetry && (
          <button className="btn-secondary btn-sm" onClick={onRetry}>
            Retry
          </button>
        )}
        {onDismiss && (
          <button className="btn-text btn-sm" onClick={onDismiss}>
            Dismiss
          </button>
        )}
      </div>
    </div>
  );
};
