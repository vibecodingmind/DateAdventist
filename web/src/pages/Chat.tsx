import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { io } from 'socket.io-client';
import { api, apiOrigin, getStoredUser, getToken } from '../api';
import type { ChatMessage, Match } from '../types';

export default function Chat() {
  const { matchId } = useParams();
  const navigate = useNavigate();
  const me = getStoredUser()?.userId;
  const [match, setMatch] = useState<Match | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [text, setText] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!matchId) return;
    api
      .matches()
      .then((list) => setMatch(list.find((m) => m.matchId === matchId) || null))
      .catch((err: Error) => setError(err.message));
    api
      .messages(matchId)
      .then(setMessages)
      .catch((err: Error) => setError(err.message));

    const socket = io(apiOrigin() || window.location.origin, {
      auth: { token: getToken() },
    });
    socket.emit('chat:join', matchId);
    socket.on('chat:message', (payload: ChatMessage) => {
      if (payload.matchId === matchId) {
        setMessages((prev) => (prev.some((m) => m.messageId === payload.messageId) ? prev : [...prev, payload]));
      }
    });
    return () => {
      socket.disconnect();
    };
  }, [matchId]);

  const other = match?.otherProfile;
  const starters = useMemo(
    () => [
      match?.conversationStarter,
      'How do you usually spend Sabbath afternoon?',
      'What is your favorite verse right now?',
    ].filter(Boolean) as string[],
    [match]
  );

  async function send(e?: FormEvent, prompt?: string) {
    e?.preventDefault();
    if (!matchId) return;
    const body = (prompt || text).trim();
    if (!body) return;
    setText('');
    try {
      const msg = await api.sendMessage(matchId, body);
      setMessages((prev) => [...prev, msg]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not send');
    }
  }

  if (!matchId) {
    return (
      <div className="stack">
        <h2 style={{ margin: 0 }}>Messages</h2>
        <p className="muted">Open a match to start chatting.</p>
        <Link to="/matches">Go to matches</Link>
      </div>
    );
  }

  return (
    <div className="chat">
      <div className="spread">
        <button className="btn btn-ghost btn-small" onClick={() => navigate(-1)}>
          Back
        </button>
        <strong>{other?.fullName || 'Chat'}</strong>
        <button
          className="btn btn-ghost btn-small"
          onClick={async () => {
            if (!matchId || !confirm('Unmatch this connection?')) return;
            await api.unmatch(matchId);
            navigate('/matches');
          }}
        >
          Unmatch
        </button>
      </div>
      {error && <div className="error">{error}</div>}
      <div className="muted" style={{ fontSize: 12 }}>
        Safety tip: keep conversations on AdventHearts until trust is established.
      </div>
      <div className="bubbles">
        {messages.map((msg) => (
          <div key={msg.messageId} className={`bubble ${msg.senderId === me ? 'me' : 'them'}`}>
            {msg.text}
          </div>
        ))}
      </div>
      <div className="row" style={{ flexWrap: 'wrap' }}>
        {starters.map((s) => (
          <button key={s} className="btn btn-ghost btn-small" onClick={() => send(undefined, s)}>
            {s}
          </button>
        ))}
      </div>
      <form className="row" onSubmit={send}>
        <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Type a respectful message…" />
        <button className="btn btn-primary btn-small">Send</button>
      </form>
    </div>
  );
}
