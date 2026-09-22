import type { AnalysisResponse, ErrorResponse, ProjectAnalysisContext, InputCapability } from '../types/analysis';

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
   * Fetches backend ingestion capabilities and roadmap status.
   */
  async getCapabilities(): Promise<InputCapability[]> {
    try {
      const response = await fetch(`${BASE_URL}/api/analyze/capabilities`, {
        method: 'GET',
        headers: {
          Accept: 'application/json',
        },
      });
      const data = await handleResponse<{ inputs: InputCapability[] }>(response);
      return data.inputs || [];
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      return [
        { type: 'ZIP_ARCHIVE', supported: true, displayName: 'ZIP / TAR Archive', description: 'Uploaded compressed project archive (.zip)' },
        { type: 'SOURCE_FILE', supported: true, displayName: 'Source File', description: 'Direct Java source file (.java)' },
        { type: 'DIRECTORY', supported: true, displayName: 'Local Directory', description: 'Local filesystem project directory' },
        { type: 'REPOSITORY_URL', supported: true, plannedPhase: 'PHASE_5', displayName: 'Repository URL', description: 'Remote Git repository URL (e.g., GitHub, GitLab)' },
        { type: 'CONFIGURATION_FILE', supported: true, plannedPhase: 'PHASE_6', displayName: 'Configuration File', description: 'Application configuration file (e.g. application.yml)' },
        { type: 'BINARY_FILE', supported: true, plannedPhase: 'PHASE_7', displayName: 'Binary File', description: 'Compiled Java Archive or bytecode (.jar, .class)' },
        { type: 'CONTAINER_IMAGE', supported: true, plannedPhase: 'PHASE_8', displayName: 'Container Image', description: 'Container image or image archive (.tar, .tar.gz)' },
      ];
    }
  },

  /**
   * Direct individual source file analysis (Java source .java).
   */
  async analyzeSourceFile(file: File, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
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

      const response = await fetch(`${BASE_URL}/api/analyze/source`, {
        method: 'POST',
        body: formData,
      });
      return await handleResponse<AnalysisResponse>(response);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(
        'Network error while uploading source file for analysis.',
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
   * Analyzes a public Git repository URL.
   */
  async analyzeRepository(url: string, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    try {
      const requestBody = context ? { repositoryUrl: url, context } : { repositoryUrl: url };
      const response = await fetch(`${BASE_URL}/api/analyze/repository`, {
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
        'Network error while connecting to repository analysis API.',
        0,
        'NETWORK_ERROR'
      );
    }
  },

  /**
   * Analyzes an uploaded configuration file (.properties, .yml, .yaml, .xml, .conf, .cfg, .ini).
   */
  async analyzeConfigurationFile(file: File, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
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

      const response = await fetch(`${BASE_URL}/api/analyze/configuration`, {
        method: 'POST',
        body: formData,
      });
      return await handleResponse<AnalysisResponse>(response);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(
        'Network error while uploading configuration file for analysis.',
        0,
        'NETWORK_ERROR'
      );
    }
  },

  /**
   * Analyzes an uploaded binary file (.jar, .class).
   */
  async analyzeBinaryFile(file: File, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
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

      const response = await fetch(`${BASE_URL}/api/analyze/binary`, {
        method: 'POST',
        body: formData,
      });
      return await handleResponse<AnalysisResponse>(response);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(
        'Network error while uploading binary file for analysis.',
        0,
        'NETWORK_ERROR'
      );
    }
  },

  /**
   * Roadmap stub for loose files/binaries scanning (Phase 6 & 7).
   */
  async analyzeFiles(_files: File[], _context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    throw new ApiError(
      'File and binary scanning is not yet available.',
      400,
      'UNSUPPORTED_INPUT_TYPE'
    );
  },

  /**
   * Analyzes an uploaded container image archive (.tar, .tar.gz, .tgz).
   */
  async analyzeContainerImage(file: File, context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
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

      const response = await fetch(`${BASE_URL}/api/analyze/container`, {
        method: 'POST',
        body: formData,
      });
      return await handleResponse<AnalysisResponse>(response);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        throw err;
      }
      throw new ApiError(
        'Network error while uploading container image for analysis.',
        0,
        'NETWORK_ERROR'
      );
    }
  },

  /**
   * Roadmap stub for Container Image scanning (Phase 8).
   */
  async analyzeContainer(_image: string, _context?: ProjectAnalysisContext): Promise<AnalysisResponse> {
    throw new ApiError(
      'Container image scanning is not yet available.',
      400,
      'UNSUPPORTED_INPUT_TYPE'
    );
  },
};
