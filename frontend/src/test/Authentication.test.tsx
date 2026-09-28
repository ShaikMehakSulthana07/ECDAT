import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { LoginScreen } from '../components/LoginScreen';
import App from '../App';

describe('Authentication & Session Prototype Tests', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('1. Renders LoginScreen with explicit Prototype Authentication and Demo Workspace disclaimers', () => {
    const handleLogin = vi.fn();
    render(<LoginScreen onLogin={handleLogin} />);

    // Brand and headline
    expect(screen.getAllByText('CRYPTAGUARD').length).toBeGreaterThanOrEqual(1);

    // Explicit prototype badge
    expect(
      screen.getByText(/Prototype Authentication · Demo Workspace/i)
    ).toBeInTheDocument();

    // Field labels & hints
    expect(screen.getByLabelText(/Email Address \/ Analyst Handle/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Session Passphrase \(Demo Only\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Any value accepted \(non-validating\)/i)).toBeInTheDocument();

    // Footer disclaimer
    expect(
      screen.getByText(/Prototype Session · Client-side demonstration only · No credentials transmitted or validated/i)
    ).toBeInTheDocument();

    // Button label
    expect(
      screen.getByRole('button', { name: /Enter Demo Workspace/i })
    ).toBeInTheDocument();
  });

  it('2. Does not pre-fill hardcoded password dots or secrets', () => {
    render(<LoginScreen onLogin={vi.fn()} />);

    const passwordInput = screen.getByLabelText(/Session Passphrase/i) as HTMLInputElement;
    expect(passwordInput.value).toBe('');
    expect(passwordInput.placeholder).toMatch(/Enter any demo passphrase/i);
  });

  it('3. Successfully formats analyst handle and invokes onLogin on form submission', () => {
    const handleLogin = vi.fn();
    render(<LoginScreen onLogin={handleLogin} />);

    const emailInput = screen.getByLabelText(/Email Address \/ Analyst Handle/i);
    fireEvent.change(emailInput, { target: { value: 'alice.crypto@enterprise.org' } });

    const passwordInput = screen.getByLabelText(/Session Passphrase/i);
    fireEvent.change(passwordInput, { target: { value: 'demo1234' } });

    const submitBtn = screen.getByRole('button', { name: /Enter Demo Workspace/i });
    fireEvent.click(submitBtn);

    expect(handleLogin).toHaveBeenCalledTimes(1);
    expect(handleLogin).toHaveBeenCalledWith({
      email: 'alice.crypto@enterprise.org',
      name: 'Alice Crypto',
      org: 'Enterprise Cryptographic Operations',
    });
  });

  it('4. Allows password visibility toggle without compromising any secret', () => {
    render(<LoginScreen onLogin={vi.fn()} />);

    const passwordInput = screen.getByLabelText(/Session Passphrase/i) as HTMLInputElement;
    expect(passwordInput.type).toBe('password');

    const toggleBtn = screen.getByLabelText(/Toggle password visibility/i);
    fireEvent.click(toggleBtn);
    expect(passwordInput.type).toBe('text');

    fireEvent.click(toggleBtn);
    expect(passwordInput.type).toBe('password');
  });

  it('5. Full App: Unauthenticated user sees LoginScreen; entering workspace stores session and unlocks App shell', () => {
    render(<App />);

    // Initially unauthenticated
    expect(
      screen.getByText(/Prototype Authentication · Demo Workspace/i)
    ).toBeInTheDocument();

    // Submit demo login
    const submitBtn = screen.getByRole('button', { name: /Enter Demo Workspace/i });
    fireEvent.click(submitBtn);

    // Verify localStorage persistence
    expect(localStorage.getItem('ecdat_auth')).toBe('true');
    expect(localStorage.getItem('ecdat_user')).toBeTruthy();

    // App shell is unlocked and TopBar displays Demo Workspace
    expect(screen.getAllByText('Demo Workspace').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText(/Dashboard/i).length).toBeGreaterThanOrEqual(1);
  });

  it('6. Full App: Logout clears session and restores LoginScreen', () => {
    localStorage.setItem('ecdat_auth', 'true');
    localStorage.setItem(
      'ecdat_user',
      JSON.stringify({
        email: 'analyst@ecdat.local',
        name: 'Security Analyst',
        org: 'SecOps',
      })
    );

    render(<App />);

    // Shell should be visible
    expect(screen.getAllByText('Demo Workspace').length).toBeGreaterThanOrEqual(1);

    // Click logout
    const logoutBtn = screen.getByRole('button', { name: /Logout/i });
    fireEvent.click(logoutBtn);

    // Verification
    expect(localStorage.getItem('ecdat_auth')).toBeNull();
    expect(
      screen.getByText(/Prototype Authentication · Demo Workspace/i)
    ).toBeInTheDocument();
  });
});
