import { useState, useEffect } from 'react'
import './App.css'

interface HealthResponse {
  status: string
}

function App() {
  const [backendStatus, setBackendStatus] = useState<string>('Checking...')
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const checkBackend = async () => {
      try {
        const response = await fetch('http://localhost:8080/health')
        const data: HealthResponse = await response.json()
        setBackendStatus(data.status)
      } catch {
        setBackendStatus('Unavailable')
      } finally {
        setIsLoading(false)
      }
    }

    checkBackend()
  }, [])

  return (
    <div className="app">
      <div className="container">
        <h1 className="title">ECDAT</h1>
        <p className="subtitle">Enterprise Cryptographic Discovery & Analysis Tool</p>
        
        <div className="status-container">
          <h2>Backend Status</h2>
          <div className={`status ${isLoading ? 'loading' : backendStatus.toLowerCase()}`}>
            {isLoading ? 'Checking...' : backendStatus}
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
