import type { AnalysisResponse, ErrorResponse, ProjectAnalysisContext } from '../types/analysis';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  errorType?: string;

  constructor(message: string, status: number = 500, errorType?: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorType = errorType;
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let errorMessage = `Request failed with status ${response.status} (${response.statusText})`;
    let errorType = 'HTTP_ERROR';

    try {
      const errorJson = (await response.json()) as ErrorResponse;
      if (errorJson && errorJson.message) {
        errorMessage = errorJson.message;
        errorType = errorJson.error || errorType;
      }
    } catch {
      // Body was not valid JSON, use status text
    }

    throw new ApiError(errorMessage, response.status, errorType);
  }

  return (await response.json()) as T;
}

export const apiService = {
  getBaseUrl(): string {
    return BASE_URL;
  },

  async checkHealth(): Promise<{ status: string }> {
    try {
      const response = await fetch(`${BASE_URL}/health`, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
        },
      });
      return await handleResponse<{ status: string }>(response);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(
        'Backend service is unreachable. Please verify the backend is running at ' + BASE_URL,
        0,
        'NETWORK_ERROR'
      );
    }
  },

  async analyzeDirectory(path: string, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    try {
      const requestBody = context ? { path, context } : { path };
      const response = await fetch(`${BASE_URL}/api/analyze`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify(requestBody),
      });
      return await handleResponse<AnalysisResponse>(response);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(
        'Network error while connecting to backend analysis API.',
        0,
        'NETWORK_ERROR'
      );
    }
  },

  async analyzeArchive(file: File, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      if (context) {
        if (context.applicationName) formData.append('applicationName', context.applicationName);
        if (context.businessCriticality) formData.append('businessCriticality', context.businessCriticality);
        if (context.dataSensitivity) formData.append('dataSensitivity', context.dataSensitivity);
        if (context.dataLifetimeYears) formData.append('dataLifetimeYears', context.dataLifetimeYears.toString());
        if (context.migrationTimeYears) formData.append('migrationTimeYears', context.migrationTimeYears.toString());
        if (context.threatHorizonYears) formData.append('threatHorizonYears', context.threatHorizonYears.toString());
      }

      const response = await fetch(`${BASE_URL}/api/analyze/upload`, {
        method: 'POST',
        body: formData,
      });
      return await handleResponse<AnalysisResponse>(response);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(
        'Network error while uploading archive for analysis.',
        0,
        'NETWORK_ERROR'
      );
    }
  },

  /**
   * Primary method for ZIP archive analysis in Phase 4.
   */
  async analyzeZip(file: File, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    return this.analyzeArchive(file, context);
  },

  /**
   * Roadmap stub for Git Repository scanning (Phase 5).
   */
  async analyzeRepository(_url: string, _context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    throw new ApiError(
      'Git repository analysis requires backend service integration (Roadmap feature - Phase 5).',
      400,
      'UNSUPPORTED_INPUT_TYPE'
    );
  },

  /**
   * Roadmap stub for loose files/binaries scanning (Phase 6 & 7).
   */
  async analyzeFiles(_files: File[], _context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    throw new ApiError(
      'Loose file/binary scanning is currently in active development (Roadmap feature - Phase 6/7).',
      400,
      'UNSUPPORTED_INPUT_TYPE'
    );
  },

  /**
   * Roadmap stub for Container Image scanning (Phase 8).
   */
  async analyzeContainer(_image: string, _context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    throw new ApiError(
      'Container image inspection requires container daemon integration (Roadmap feature - Phase 8).',
      400,
      'UNSUPPORTED_INPUT_TYPE'
    );
  },
};
