import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { apiService, ApiError } from '../services/api';

describe('ECDAT API Service Tests', () => {
  const originalFetch = globalThis.fetch;

  beforeEach(() => {
    vi.resetAllMocks();
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
  });

  it('checkHealth returns health status when UP', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'UP' }),
    });

    const result = await apiService.checkHealth();
    expect(result.status).toBe('UP');
    expect(globalThis.fetch).toHaveBeenCalledWith('http://localhost:8080/health', expect.any(Object));
  });

  it('checkHealth throws ApiError on network error', async () => {
    globalThis.fetch = vi.fn().mockRejectedValue(new Error('Failed to fetch'));

    await expect(apiService.checkHealth()).rejects.toThrow(ApiError);
  });

  it('analyzeDirectory returns AnalysisResponse on success', async () => {
    const mockResult = {
      status: 'SUCCESS',
      sourcePath: 'test-target',
      findings: [],
      riskAssessments: [],
      pqcRecommendations: [],
      cbom: { bomFormat: 'CycloneDX', specVersion: '1.6', serialNumber: '1', version: 1, components: [], metadata: { tool: { vendor: 'ECDAT', name: 'ECDAT', version: '1' } } },
      summary: { totalFindings: 0, lowRiskCount: 0, mediumRiskCount: 0, highRiskCount: 0, criticalRiskCount: 0, quantumHighRiskCount: 0, pqcRecommendedCount: 0, pqcConditionalCount: 0, pqcNeedsAnalysisCount: 0, pqcNotRequiredCount: 0 },
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockResult,
    });

    const result = await apiService.analyzeDirectory('test-target');
    expect(result.status).toBe('SUCCESS');
    expect(result.sourcePath).toBe('test-target');
  });

  it('analyzeDirectory parses structured ErrorResponse on HTTP 400', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      statusText: 'Bad Request',
      json: async () => ({
        timestamp: '2026-09-07T12:00:00Z',
        status: 400,
        error: 'Bad Request',
        message: 'Source path does not exist: invalid/path',
        path: '/api/analyze',
      }),
    });

    await expect(apiService.analyzeDirectory('invalid/path')).rejects.toThrow(
      'Source path does not exist: invalid/path'
    );
  });

  it('analyzeArchive sends multipart form data', async () => {
    const mockFile = new File(['dummy zip content'], 'project.zip', { type: 'application/zip' });
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'SUCCESS', sourcePath: 'temp/project.zip' }),
    });

    const result = await apiService.analyzeArchive(mockFile);
    expect(result.status).toBe('SUCCESS');
    expect(globalThis.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/analyze/upload',
      expect.objectContaining({
        method: 'POST',
      })
    );
  });

  it('analyzeZip delegates to analyzeArchive', async () => {
    const mockFile = new File(['dummy zip content'], 'project.zip', { type: 'application/zip' });
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'SUCCESS', sourcePath: 'temp/project.zip' }),
    });

    const result = await apiService.analyzeZip(mockFile);
    expect(result.status).toBe('SUCCESS');
    expect(globalThis.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/analyze/upload',
      expect.objectContaining({
        method: 'POST',
      })
    );
  });

  it('analyzeRepository sends POST request to /api/analyze/repository', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'SUCCESS', findings: [] }),
    }) as any;

    await apiService.analyzeRepository('https://github.com/org/repo.git');

    expect(globalThis.fetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/analyze/repository'),
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({
          'Content-Type': 'application/json',
        }),
      })
    );
  });

  it('analyzeFiles throws explicit unsupported ApiError', async () => {
    await expect(apiService.analyzeFiles([])).rejects.toThrow(
      'File and binary scanning is not yet available.'
    );
  });

  it('analyzeContainer throws explicit unsupported ApiError', async () => {
    await expect(apiService.analyzeContainer('docker.io/app:latest')).rejects.toThrow(
      'Container image scanning is not yet available.'
    );
  });

  it('getCapabilities fetches capabilities from backend', async () => {
    const mockCapabilities = {
      inputs: [
        { type: 'ZIP_ARCHIVE', supported: true, displayName: 'ZIP / TAR Archive', description: 'Uploaded archive' },
        { type: 'SOURCE_FILE', supported: true, displayName: 'Source File', description: 'Java source file' },
        { type: 'REPOSITORY_URL', supported: false, plannedPhase: 'PHASE_5', displayName: 'Repository URL', description: 'Git repo' },
      ],
    };

    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockCapabilities,
    });

    const result = await apiService.getCapabilities();
    expect(result.length).toBe(3);
    expect(result[0].type).toBe('ZIP_ARCHIVE');
    expect(result[0].supported).toBe(true);
    expect(globalThis.fetch).toHaveBeenCalledWith('http://localhost:8080/api/analyze/capabilities', expect.any(Object));
  });

  it('analyzeSourceFile sends multipart form data to /api/analyze/source', async () => {
    const mockFile = new File(['public class Demo {}'], 'Demo.java', { type: 'text/x-java-source' });
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'SUCCESS', sourcePath: 'temp/Demo.java', inputType: 'SOURCE_FILE' }),
    });

    const result = await apiService.analyzeSourceFile(mockFile);
    expect(result.status).toBe('SUCCESS');
    expect(globalThis.fetch).toHaveBeenCalledWith(
      'http://localhost:8080/api/analyze/source',
      expect.objectContaining({
        method: 'POST',
      })
    );
  });
});
