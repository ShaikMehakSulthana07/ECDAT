package com.ecdat.backend.input.security;

/**
 * Exception thrown during repository analysis operations.
 * Provides structured error codes for frontend handling.
 */
public class RepositoryAnalysisException extends RuntimeException {

    private final RepositoryErrorCode errorCode;

    public RepositoryAnalysisException(RepositoryErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RepositoryAnalysisException(RepositoryErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public RepositoryErrorCode getErrorCode() {
        return errorCode;
    }
}
