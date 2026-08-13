import { useEffect, useState } from 'react';
import { api } from '../api';
import type { Profile } from '../types';

export default function Discover() {
  const [profiles, setProfiles] = useState<Profile[]>([]);
  const [index, setIndex] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [matchName, setMatchName] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api
      .discover()
      .then((list) => {
        setProfiles(list);
        setIndex(0);
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  const current = profiles[index];

  async function act(kind: 'like' | 'pass' | 'super') {
    if (!current || busy) return;
    setBusy(true);
    setError(null);
    try {
      if (kind === 'pass') {
        await api.pass(current.userId);
      } else {
        const result = await api.like(current.userId, kind === 'super');
        if (result.isMatch) setMatchName(current.fullName);
      }
      setIndex((i) => i + 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Action failed');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="stack">
      <div className="spread">
        <h2 style={{ margin: 0 }}>Discover</h2>
        <span className="muted">{profiles.length ? `${Math.min(index + 1, profiles.length)} / ${profiles.length}` : 'Loading'}</span>
      </div>
      {error && <div className="error">{error}</div>}
      {matchName && (
        <div className="success">
          It is a match with {matchName}! Open Matches to start a Sabbath conversation.
          <button className="btn btn-small btn-ghost" style={{ marginLeft: 8, width: 'auto' }} onClick={() => setMatchName(null)}>
            Close
          </button>
        </div>
      )}
      {!current ? (
        <div className="card">
          <p>No more profiles right now. Check Likes or come back after more members join.</p>
        </div>
      ) : (
        <>
          <div className="swipe">
            <img src={current.primaryPhoto || '/placeholder.svg'} alt={current.fullName} />
            <div className="swipe-meta">
              <div className="row">
                <h2 style={{ margin: 0 }}>
                  {current.fullName}, {current.age}
                </h2>
                {current.isVerified && <span className="badge">VERIFIED</span>}
                {current.isPremium && <span className="badge gold">GOLD</span>}
                {current.compatibilityScore != null && <span className="badge green">{current.compatibilityScore}%</span>}
              </div>
              <div className="muted">
                {current.occupation} · {current.city}
              </div>
              <p>{current.bio}</p>
              <div className="muted">{current.localChurch}</div>
            </div>
          </div>
          <div className="actions">
            <button className="circle" onClick={() => act('pass')} disabled={busy} aria-label="Pass">
              ✕
            </button>
            <button className="circle super" onClick={() => act('super')} disabled={busy} aria-label="Super like">
              ★
            </button>
            <button className="circle like" onClick={() => act('like')} disabled={busy} aria-label="Like">
              ♥
            </button>
          </div>
        </>
      )}
    </div>
  );
}
