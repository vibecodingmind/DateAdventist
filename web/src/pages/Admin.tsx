import { useEffect, useState } from 'react';
import { api } from '../api';

export default function Admin() {
  const [metrics, setMetrics] = useState<{
    activeUsers: number;
    totalMatches: number;
    pendingVerifications: number;
    openReports: number;
    monthlyRevenue: number;
  } | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .adminDashboard()
      .then(setMetrics)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <div className="stack">
      <h2 style={{ margin: 0 }}>Admin</h2>
      {error && <div className="error">{error} — sign in with an admin account.</div>}
      {metrics && (
        <div className="grid">
          <div className="card">
            <div className="muted">Active users</div>
            <h2>{metrics.activeUsers}</h2>
          </div>
          <div className="card">
            <div className="muted">Matches</div>
            <h2>{metrics.totalMatches}</h2>
          </div>
          <div className="card">
            <div className="muted">Pending verifications</div>
            <h2>{metrics.pendingVerifications}</h2>
          </div>
          <div className="card">
            <div className="muted">Open reports</div>
            <h2>{metrics.openReports}</h2>
          </div>
        </div>
      )}
    </div>
  );
}
