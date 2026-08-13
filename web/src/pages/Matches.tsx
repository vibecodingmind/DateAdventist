import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import type { Match } from '../types';

export default function Matches() {
  const [matches, setMatches] = useState<Match[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .matches()
      .then(setMatches)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <div className="stack">
      <h2 style={{ margin: 0 }}>Your connections</h2>
      {error && <div className="error">{error}</div>}
      {!matches.length && <div className="card muted">No matches yet. Like members in Discover to begin.</div>}
      <div className="grid">
        {matches.map((match) => {
          const p = match.otherProfile;
          if (!p) return null;
          return (
            <Link key={match.matchId} to={`/chat/${match.matchId}`} className="tile">
              <img src={p.primaryPhoto} alt={p.fullName} />
              <span>
                {p.fullName.split(' ')[0]}, {p.age} · {match.compatibilityScore}%
              </span>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
