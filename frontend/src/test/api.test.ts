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
});
