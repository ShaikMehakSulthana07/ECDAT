import { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { ScanInput } from './components/ScanInput';
import { DashboardSummary } from './components/DashboardSummary';
import { RiskOverview } from './components/RiskOverview';
import { FindingsTable } from './components/FindingsTable';
import { FindingDetailsModal } from './components/FindingDetailsModal';
import { PQCRecommendations } from './components/PQCRecommendations';
import { CBOMViewer } from './components/CBOMViewer';
import { SourceTraceability } from './components/SourceTraceability';
import { LoadingState } from './components/LoadingState';
import { ErrorAlert } from './components/ErrorAlert';
import { EmptyState } from './components/EmptyState';
import { apiService, ApiError } from './services/api';
import type { AnalysisResponse } from './types/analysis';
import './App.css';

type TabType = 'overview' | 'findings' | 'pqc' | 'cbom' | 'traceability';

function App() {
  const [backendStatus, setBackendStatus] = useState<'UP' | 'DOWN' | 'CHECKING'>('CHECKING');
  const [analysisData, setAnalysisData] = useState<AnalysisResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [activeTarget, setActiveTarget] = useState<string>('');
  const [error, setError] = useState<{ message: string; errorType?: string; status?: number } | null>(null);
  const [activeTab, setActiveTab] = useState<TabType>('overview');
  const [selectedFindingIndex, setSelectedFindingIndex] = useState<number | null>(null);

  // Check backend health on initial load
  useEffect(() => {
    const checkStatus = async () => {
      try {
        const health = await apiService.checkHealth();
        if (health && health.status === 'UP') {
          setBackendStatus('UP');
        } else {
          setBackendStatus('DOWN');
        }
      } catch {
        setBackendStatus('DOWN');
      }
    };

    checkStatus();
  }, []);

  const handleScanPath = async (path: string) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(path);

    try {
      let response: AnalysisResponse;
      try {
        response = await apiService.analyzeDirectory(path);
      } catch (firstErr: unknown) {
        if (
          firstErr instanceof ApiError &&
          firstErr.status === 400 &&
          firstErr.message.includes('does not exist') &&
          !path.startsWith('../') &&
          !path.includes(':')
        ) {
          response = await apiService.analyzeDirectory('../' + path);
        } else {
          throw firstErr;
        }
      }

      setAnalysisData(response);
      setBackendStatus('UP');
      setActiveTab('overview');
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError({
          message: err.message,
          errorType: err.errorType,
          status: err.status,
        });
        if (err.status === 0) {
          setBackendStatus('DOWN');
        }
      } else {
        setError({
          message: 'An unexpected error occurred during directory analysis.',
        });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleScanFile = async (file: File) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(file.name);

    try {
      const response = await apiService.analyzeArchive(file);
      setAnalysisData(response);
      setBackendStatus('UP');
      setActiveTab('overview');
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError({
          message: err.message,
          errorType: err.errorType,
          status: err.status,
        });
        if (err.status === 0) {
          setBackendStatus('DOWN');
        }
      } else {
        setError({
          message: 'An unexpected error occurred during archive upload and analysis.',
        });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickScan = () => {
    handleScanPath('test-target');
  };

  const handleNewScan = () => {
    setAnalysisData(null);
    setError(null);
    setSelectedFindingIndex(null);
    setActiveTab('overview');
  };

  // Keyboard navigation for modal
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && selectedFindingIndex !== null) {
        setSelectedFindingIndex(null);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [selectedFindingIndex]);

  return (
    <div className="app-layout">
      <Navbar
        backendStatus={backendStatus}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        hasResults={!!analysisData}
        totalFindings={analysisData?.summary?.totalFindings || 0}
        onNewScan={handleNewScan}
      />

      <main className="main-content">
        <div className="content-container">
          {/* Scan Input Section */}
          <section className="scan-section">
            <ScanInput
              onScanPath={handleScanPath}
              onScanFile={handleScanFile}
              isLoading={isLoading}
            />
          </section>

          {/* Error Alert */}
          {error && (
            <ErrorAlert
              message={error.message}
              errorType={error.errorType}
              status={error.status}
              onDismiss={() => setError(null)}
              onRetry={activeTarget ? () => handleScanPath(activeTarget) : undefined}
            />
          )}

          {/* Loading Indicator */}
          {isLoading && <LoadingState target={activeTarget} />}

          {/* Analysis Results */}
          {!isLoading && analysisData && (
            <div className="results-container">
              {/* Tab 1: Overview */}
              {activeTab === 'overview' && (
                <div className="tab-pane">
                  <DashboardSummary
                    summary={analysisData.summary}
                    sourcePath={analysisData.sourcePath}
                    onNavigateTab={(tab) => setActiveTab(tab)}
                  />
                  <RiskOverview
                    riskAssessments={analysisData.riskAssessments}
                    summary={analysisData.summary}
                  />
                  <div className="mt-6">
                    <div className="panel-header-inline">
                      <h3 className="panel-title">Discovered Assets Quick View</h3>
                      <button
                        className="btn-link"
                        onClick={() => setActiveTab('findings')}
                      >
                        View Full Findings Table ({analysisData.findings.length}) →
                      </button>
                    </div>
                    <FindingsTable
                      findings={analysisData.findings}
                      riskAssessments={analysisData.riskAssessments}
                      pqcRecommendations={analysisData.pqcRecommendations}
                      onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
                      selectedIndex={selectedFindingIndex}
                    />
                  </div>
                </div>
              )}

              {/* Tab 2: Findings */}
              {activeTab === 'findings' && (
                <div className="tab-pane">
                  <FindingsTable
                    findings={analysisData.findings}
                    riskAssessments={analysisData.riskAssessments}
                    pqcRecommendations={analysisData.pqcRecommendations}
                    onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
                    selectedIndex={selectedFindingIndex}
                  />
                </div>
              )}

              {/* Tab 3: PQC Migration */}
              {activeTab === 'pqc' && (
                <div className="tab-pane">
                  <PQCRecommendations
                    pqcRecommendations={analysisData.pqcRecommendations}
                    findings={analysisData.findings}
                    onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
                  />
                </div>
              )}

              {/* Tab 4: CycloneDX CBOM */}
              {activeTab === 'cbom' && (
                <div className="tab-pane">
                  <CBOMViewer cbom={analysisData.cbom} />
                </div>
              )}

              {/* Tab 5: Source Traceability */}
              {activeTab === 'traceability' && (
                <div className="tab-pane">
                  <SourceTraceability
                    findings={analysisData.findings}
                    riskAssessments={analysisData.riskAssessments}
                    pqcRecommendations={analysisData.pqcRecommendations}
                    onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
                  />
                </div>
              )}
            </div>
          )}

          {/* Empty / Landing State */}
          {!isLoading && !analysisData && !error && (
            <EmptyState onQuickScan={handleQuickScan} isLoading={isLoading} />
          )}
        </div>
      </main>

      {/* Finding Details Modal */}
      {selectedFindingIndex !== null && analysisData && (
        <FindingDetailsModal
          finding={analysisData.findings[selectedFindingIndex]}
          riskAssessment={analysisData.riskAssessments[selectedFindingIndex]}
          pqcRecommendation={analysisData.pqcRecommendations[selectedFindingIndex]}
          findingIndex={selectedFindingIndex}
          totalFindings={analysisData.findings.length}
          onClose={() => setSelectedFindingIndex(null)}
          onNavigatePrev={
            selectedFindingIndex > 0
              ? () => setSelectedFindingIndex(selectedFindingIndex - 1)
              : undefined
          }
          onNavigateNext={
            selectedFindingIndex < analysisData.findings.length - 1
              ? () => setSelectedFindingIndex(selectedFindingIndex + 1)
              : undefined
          }
        />
      )}

      {/* Footer */}
      <footer className="app-footer">
        <div className="footer-container">
          <span>ECDAT — Enterprise Cryptographic Discovery & Analysis Tool</span>
          <span className="footer-sih">Smart India Hackathon 2026 · Problem Statement SIH26164</span>
          <span>Aligned with NIST FIPS 203, 204, 205 & CycloneDX 1.6</span>
        </div>
      </footer>
    </div>
  );
}

export default App;
