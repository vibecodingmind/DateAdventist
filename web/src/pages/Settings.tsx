import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, clearSession } from '../api';

export default function Settings() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  return (
    <div className="stack">
      <h2 style={{ margin: 0 }}>Settings</h2>
      {error && <div className="error">{error}</div>}
      <div className="card muted">
        Discovery filters, privacy toggles, and notifications sync from your account on the API. Delete is permanent.
      </div>
      <button
        className="btn btn-ghost"
        onClick={() => {
          clearSession();
          navigate('/');
          window.location.reload();
        }}
      >
        Log out
      </button>
      <button
        className="btn btn-primary"
        onClick={async () => {
          if (!confirm('Permanently delete your AdventHearts account?')) return;
          try {
            await api.deleteAccount();
            clearSession();
            navigate('/');
            window.location.reload();
          } catch (err) {
            setError(err instanceof Error ? err.message : 'Delete failed');
          }
        }}
      >
        Delete account
      </button>
    </div>
  );
}
