import { FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, saveSession } from './api';
import type { AuthUser } from './types';

export default function Welcome({ onAuthed }: { onAuthed: (user: AuthUser) => void }) {
  const [mode, setMode] = useState<'home' | 'login' | 'register' | 'reset'>('home');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [age, setAge] = useState(25);
  const [gender, setGender] = useState('Female');
  const [city, setCity] = useState('Silver Spring');
  const [country, setCountry] = useState('United States');
  const [token, setToken] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleAuth(user: AuthUser) {
    saveSession(user);
    onAuthed(user);
  }

  async function onLogin(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await handleAuth(await api.login(email, password));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setBusy(false);
    }
  }

  async function onRegister(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await handleAuth(
        await api.register({
          email,
          password,
          fullName,
          age,
          gender,
          city,
          country,
          relationshipIntention: 'Marriage',
        })
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setBusy(false);
    }
  }

  async function demo(kind: 'member' | 'admin') {
    setBusy(true);
    setError(null);
    try {
      const creds =
        kind === 'member'
          ? { email: 'john.adventist@gmail.com', password: 'password123' }
          : { email: 'admin@adventhearts.com', password: 'AdminPass2026!' };
      const user = kind === 'admin' ? await api.adminLogin(creds.email, creds.password) : await api.login(creds.email, creds.password);
      await handleAuth(user);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Demo login failed. Is the API running?');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="center">
      <div style={{ width: 'min(460px, 100%)' }}>
        <div className="brand">
          <div className="logo">♥</div>
          <h1>AdventHearts</h1>
          <div className="gold">Faith. Connection. Purpose.</div>
          <p className="muted">Faith-first Seventh-day Adventist dating for intentional, marriage-oriented relationships.</p>
        </div>
        {error && <div className="error">{error}</div>}
        {info && <div className="success">{info}</div>}

        {mode === 'home' && (
          <div className="stack">
            <button className="btn btn-primary" onClick={() => setMode('register')} disabled={busy}>
              Create AdventHearts Account
            </button>
            <button className="btn btn-ghost" onClick={() => setMode('login')} disabled={busy}>
              I Already Have an Account
            </button>
            <p className="muted" style={{ textAlign: 'center', fontSize: 12, marginTop: 8 }}>
              By creating an account you agree to the{' '}
              <Link to="/legal/terms">Terms of Service</Link> and{' '}
              <Link to="/legal/privacy">Privacy Policy</Link>.
            </p>
            {import.meta.env.DEV && (
              <>
                <p className="muted" style={{ textAlign: 'center', marginTop: 12 }}>
                  Quick demo (dev only)
                </p>
                <div className="row">
                  <button className="btn btn-ghost" onClick={() => demo('member')} disabled={busy}>
                    Member (Joshua)
                  </button>
                  <button className="btn btn-ghost" onClick={() => demo('admin')} disabled={busy}>
                    Admin
                  </button>
                </div>
              </>
            )}
          </div>
        )}

        {mode === 'login' && (
          <form className="card stack" onSubmit={onLogin}>
            <h2 style={{ margin: 0 }}>Sign in</h2>
            <input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
            <input placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
            <button className="btn btn-primary" disabled={busy}>
              Sign In
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => setMode('reset')}>
              Forgot password?
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => setMode('home')}>
              Back
            </button>
          </form>
        )}

        {mode === 'register' && (
          <form className="card stack" onSubmit={onRegister}>
            <h2 style={{ margin: 0 }}>Create account</h2>
            <input placeholder="Full name" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
            <input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
            <input placeholder="Password (8+ characters)" value={password} onChange={(e) => setPassword(e.target.value)} type="password" minLength={8} required />
            <div className="row">
              <input type="number" min={18} value={age} onChange={(e) => setAge(Number(e.target.value))} />
              <select value={gender} onChange={(e) => setGender(e.target.value)}>
                <option>Female</option>
                <option>Male</option>
              </select>
            </div>
            <input placeholder="City" value={city} onChange={(e) => setCity(e.target.value)} />
            <input placeholder="Country" value={country} onChange={(e) => setCountry(e.target.value)} />
            <button className="btn btn-primary" disabled={busy}>
              Create account
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => setMode('home')}>
              Back
            </button>
          </form>
        )}

        {mode === 'reset' && (
          <form
            className="card stack"
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              setError(null);
              try {
                if (token) {
                  const res = await api.resetPassword(token, password);
                  setInfo(res.message);
                } else {
                  const res = await api.forgotPassword(email);
                  setInfo(res.message);
                }
              } catch (err) {
                setError(err instanceof Error ? err.message : 'Reset failed');
              } finally {
                setBusy(false);
              }
            }}
          >
            <h2 style={{ margin: 0 }}>Reset password</h2>
            <input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
            <input placeholder="Reset token (after email arrives)" value={token} onChange={(e) => setToken(e.target.value)} />
            <input placeholder="New password" value={password} onChange={(e) => setPassword(e.target.value)} type="password" />
            <button className="btn btn-primary" disabled={busy}>
              {token ? 'Update password' : 'Send reset email'}
            </button>
            <button type="button" className="btn btn-ghost" onClick={() => setMode('login')}>
              Back
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
