import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import type { LikeReceived, Match, Subscription } from '../types';

export default function Likes() {
  const [likes, setLikes] = useState<LikeReceived[]>([]);
  const [matches, setMatches] = useState<Match[]>([]);
  const [sub, setSub] = useState<Subscription | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([api.likes(), api.matches(), api.subscription()])
      .then(([l, m, s]) => {
        setLikes(l);
        setMatches(m);
        setSub(s);
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  const premium = Boolean(sub?.active);

  return (
    <div className="stack">
      <div className="spread">
        <h2 style={{ margin: 0 }}>Likes you</h2>
        {!premium && (
          <Link to="/subscription" className="btn btn-gold btn-small">
            Unlock with Gold
          </Link>
        )}
      </div>
      {error && <div className="error">{error}</div>}
      {!likes.length && <div className="card muted">No incoming likes yet.</div>}
      <div className="grid">
        {likes.map((like) => {
          const p = like.profile;
          if (!p) return null;
          const matched = matches.find((m) => m.otherProfile?.userId === p.userId);
          return (
            <div key={p.userId} className="tile" style={{ filter: premium ? undefined : 'blur(7px)' }}>
              <img src={p.primaryPhoto} alt={p.fullName} />
              <span>
                {premium ? p.fullName : 'Gold member'} {like.isSuperLike ? '★' : ''}
                {matched && (
                  <Link to={`/chat/${matched.matchId}`} style={{ display: 'block', fontSize: 12 }}>
                    Open chat
                  </Link>
                )}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
