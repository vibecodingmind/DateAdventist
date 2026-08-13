import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

export default function LegalPage({ kind }: { kind: 'terms' | 'privacy' }) {
  const [title, setTitle] = useState(kind === 'terms' ? 'Terms of Service' : 'Privacy Policy');
  const [text, setText] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    api
      .legal(kind)
      .then((data) => {
        if (cancelled) return;
        setTitle(data.title);
        setText(data.text);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof Error ? err.message : 'Could not load this page.');
      });
    return () => {
      cancelled = true;
    };
  }, [kind]);

  return (
    <div className="center" style={{ alignItems: 'flex-start' }}>
      <div className="stack" style={{ width: 'min(720px, 100%)', paddingTop: 24 }}>
        <Link to="/" className="muted" style={{ textDecoration: 'none' }}>
          ← AdventHearts
        </Link>
        <h1 style={{ fontSize: 28 }}>{title}</h1>
        {error && <div className="error">{error}</div>}
        {!error && !text && <p className="muted">Loading…</p>}
        {text && (
          <div className="card" style={{ whiteSpace: 'pre-wrap', lineHeight: 1.55 }}>
            {text}
          </div>
        )}
      </div>
    </div>
  );
}
