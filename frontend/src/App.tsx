import { useState, useEffect } from 'react';
import { LoginScreen } from './components/LoginScreen';
import { Sidebar, type NavigationPage } from './components/Sidebar';
import { TopBar } from './components/TopBar';
import { DashboardView } from './components/DashboardView';
import { ScanProjectView } from './components/ScanProjectView';
import { CryptoInventoryView } from './components/CryptoInventoryView';
import { CertificatesView } from './components/CertificatesView';
import { DependenciesView } from './components/DependenciesView';
import { QuantumRiskView } from './components/QuantumRiskView';
import { PQCMigrationView } from './components/PQCMigrationView';
import { CBOMViewer } from './components/CBOMViewer';
import { ReportsView } from './components/ReportsView';
import { SettingsView } from './components/SettingsView';
import { AssetDetailDrawer } from './components/AssetDetailDrawer';
import { apiService, ApiError } from './services/api';
import type { AnalysisResponse, ProjectAnalysisContext } from './types/analysis';
import './App.css';

interface UserSession {
  email: string;
  name: string;
  org: string;
}

const DEFAULT_USER: UserSession = {
  email: 'security.analyst@enterprise.corp',
  name: 'Security Analyst',
  org: 'Enterprise Cryptographic Operations',
};

function App() {
  // Theme State (Default to 'dark' with localStorage persistence)
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    const saved = localStorage.getItem('ecdat_theme');
    if (saved === 'light' || saved === 'dark') return saved;
    return 'dark';
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('ecdat_theme', theme);
  }, [theme]);

  const handleToggleTheme = () => {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };

  // Authentication State
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
    return localStorage.getItem('ecdat_auth') === 'true';
  });
  const [user, setUser] = useState<UserSession>(() => {
    const saved = localStorage.getItem('ecdat_user');
    if (saved) {
      try { return JSON.parse(saved); } catch { return DEFAULT_USER; }
    }
    return DEFAULT_USER;
  });

  // Navigation & Shell State
  const [activePage, setActivePage] = useState<NavigationPage>('dashboard');
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Analysis & Data State
  const [backendStatus, setBackendStatus] = useState<'UP' | 'DOWN' | 'CHECKING'>('CHECKING');
  const [analysisData, setAnalysisData] = useState<AnalysisResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [activeTarget, setActiveTarget] = useState<string>('');
  const [error, setError] = useState<{ message: string; errorType?: string; status?: number } | null>(null);
  const [selectedFindingIndex, setSelectedFindingIndex] = useState<number | null>(null);

  // Health check on mount
  const checkHealth = async () => {
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

  useEffect(() => {
    checkHealth();
  }, []);

  const handleLogin = (newUser: UserSession) => {
    setUser(newUser);
    setIsAuthenticated(true);
    localStorage.setItem('ecdat_auth', 'true');
    localStorage.setItem('ecdat_user', JSON.stringify(newUser));
  };

  const handleLogout = () => {
    setIsAuthenticated(false);
    localStorage.removeItem('ecdat_auth');
    localStorage.removeItem('ecdat_user');
  };

  const handleScanPath = async (path: string, context?: ProjectAnalysisContext) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(path);
    setActivePage('scan');

    try {
      let response: AnalysisResponse;
      try {
        response = await apiService.analyzeDirectory(path, context);
      } catch (firstErr: unknown) {
        if (
          firstErr instanceof ApiError &&
          firstErr.status === 400 &&
          firstErr.message.includes('does not exist') &&
          !path.startsWith('../') &&
          !path.includes(':')
        ) {
          response = await apiService.analyzeDirectory('../' + path, context);
        } else {
          throw firstErr;
        }
      }

      setAnalysisData(response);
      setBackendStatus('UP');
      setActivePage('dashboard');
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

  const handleScanFile = async (file: File, context?: ProjectAnalysisContext) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(file.name);
    setActivePage('scan');

    try {
      const response = await apiService.analyzeZip(file, context);
      setAnalysisData(response);
      setBackendStatus('UP');
      setActivePage('dashboard');
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

  const handleScanRepository = async (url: string, context?: ProjectAnalysisContext) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(url);
    setActivePage('scan');

    try {
      const response = await apiService.analyzeRepository(url, context);
      setAnalysisData(response);
      setBackendStatus('UP');
      setActivePage('dashboard');
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
          message: 'An unexpected error occurred during repository analysis.',
        });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleScanConfiguration = async (file: File, context?: ProjectAnalysisContext) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(file.name);
    setActivePage('scan');

    try {
      const response = await apiService.analyzeConfigurationFile(file, context);
      setAnalysisData(response);
      setBackendStatus('UP');
      setActivePage('dashboard');
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
          message: 'An unexpected error occurred during configuration file analysis.',
        });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleScanBinary = async (file: File, context?: ProjectAnalysisContext) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(file.name);
    setActivePage('scan');

    try {
      const response = await apiService.analyzeBinaryFile(file, context);
      setAnalysisData(response);
      setBackendStatus('UP');
      setActivePage('dashboard');
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
          message: 'An unexpected error occurred during binary file analysis.',
        });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleScanContainer = async (file: File, context?: ProjectAnalysisContext) => {
    setIsLoading(true);
    setError(null);
    setActiveTarget(file.name);
    setActivePage('scan');

    try {
      const response = await apiService.analyzeContainerImage(file, context);
      setAnalysisData(response);
      setBackendStatus('UP');
      setActivePage('dashboard');
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
          message: 'An unexpected error occurred during container image analysis.',
        });
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickScan = () => {
    handleScanPath('test-target');
  };

  // If not authenticated, render Phase 1 login screen
  if (!isAuthenticated) {
    return <LoginScreen onLogin={handleLogin} />;
  }

  return (
    <div className="app-shell">
      {/* Persistent Left Sidebar */}
      <Sidebar
        activePage={activePage}
        onNavigate={(page) => {
          setActivePage(page);
          setError(null);
        }}
        isCollapsed={isSidebarCollapsed}
        onToggleCollapse={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
        user={user}
        onLogout={handleLogout}
        assetCount={analysisData?.findings?.length || 0}
        certCount={analysisData?.certificateFindings?.length || 0}
        quantumCount={analysisData?.summary?.quantumHighRiskCount || 0}
      />

      {/* Main App Content Wrapper */}
      <div className="main-wrapper">
        <TopBar
          activePage={activePage}
          backendStatus={backendStatus}
          searchQuery={searchQuery}
          onSearchChange={(q) => {
            setSearchQuery(q);
            if (q && activePage !== 'inventory') {
              setActivePage('inventory');
            }
          }}
          targetPath={analysisData?.sourcePath || activeTarget}
          theme={theme}
          onToggleTheme={handleToggleTheme}
          user={user}
          onNavigateSettings={() => setActivePage('settings')}
        />

        <main className="main-content-area">
          {/* Error Alert */}
          {error && (
            <div className="error-alert-box" role="alert">
              <div className="error-alert-content">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                <div>
                  <strong>Analysis Request Failed:</strong> {error.message}
                  {error.status ? ` (HTTP ${error.status})` : ''}
                </div>
              </div>
              <button
                className="btn-secondary btn-sm"
                onClick={() => setError(null)}
              >
                Dismiss
              </button>
            </div>
          )}

          {/* View Routing */}
          {activePage === 'dashboard' && (
            <DashboardView
              analysisData={analysisData}
              onNavigate={(page) => setActivePage(page)}
              onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
              onQuickScan={handleQuickScan}
              isLoading={isLoading}
            />
          )}

          {activePage === 'scan' && (
            <ScanProjectView
              onScanPath={handleScanPath}
              onScanFile={handleScanFile}
              onScanRepository={handleScanRepository}
              onScanConfiguration={handleScanConfiguration}
              onScanBinary={handleScanBinary}
              onScanContainer={handleScanContainer}
              isLoading={isLoading}
              activeTarget={activeTarget}
            />
          )}

          {activePage === 'inventory' && (
            <CryptoInventoryView
              findings={analysisData?.findings || []}
              riskAssessments={analysisData?.riskAssessments || []}
              pqcRecommendations={analysisData?.pqcRecommendations || []}
              onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
              selectedIndex={selectedFindingIndex}
              searchQuery={searchQuery}
            />
          )}

          {activePage === 'certificates' && (
            <CertificatesView
              certificates={analysisData?.certificateFindings || []}
            />
          )}

          {activePage === 'dependencies' && (
            <DependenciesView
              findings={analysisData?.findings || []}
              riskAssessments={analysisData?.riskAssessments || []}
            />
          )}

          {activePage === 'quantum' && (
            <QuantumRiskView
              findings={analysisData?.findings || []}
              riskAssessments={analysisData?.riskAssessments || []}
              context={analysisData?.context}
              onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
            />
          )}

          {activePage === 'pqc' && (
            <PQCMigrationView
              findings={analysisData?.findings || []}
              riskAssessments={analysisData?.riskAssessments || []}
              pqcRecommendations={analysisData?.pqcRecommendations || []}
              onSelectFinding={(idx) => setSelectedFindingIndex(idx)}
            />
          )}

          {activePage === 'cbom' && (
            analysisData?.cbom ? (
              <CBOMViewer cbom={analysisData.cbom} />
            ) : (
              <div className="card-panel" style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-muted)' }}>
                <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '6px' }}>
                  No CBOM Document Available
                </div>
                <p style={{ fontSize: '12px' }}>
                  Execute a project scan to generate the Cryptography Bill of Materials.
                </p>
              </div>
            )
          )}

          {activePage === 'reports' && (
            <ReportsView
              analysisData={analysisData}
              onNavigateScan={() => setActivePage('scan')}
            />
          )}

          {activePage === 'settings' && (
            <SettingsView
              user={user}
              backendStatus={backendStatus}
              onHealthCheck={checkHealth}
              theme={theme}
              onToggleTheme={handleToggleTheme}
              onLogout={handleLogout}
            />
          )}
        </main>
      </div>

      {/* Asset Detail Slide-Over Drawer */}
      {selectedFindingIndex !== null && analysisData && analysisData.findings[selectedFindingIndex] && (
        <AssetDetailDrawer
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
    </div>
  );
}

export default App;
