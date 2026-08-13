import { useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { clearSession, getStoredUser } from './api';
import type { AuthUser } from './types';
import Welcome from './Welcome';
import { Shell } from './Shell';
import Discover from './pages/Discover';
import Likes from './pages/Likes';
import Matches from './pages/Matches';
import Chat from './pages/Chat';
import ProfilePage from './pages/Profile';
import SubscriptionPage from './pages/Subscription';
import Settings from './pages/Settings';
import Admin from './pages/Admin';
import LegalPage from './pages/LegalPage';

export default function App() {
  const [user, setUser] = useState<AuthUser | null>(getStoredUser());

  return (
    <Routes>
      <Route path="/legal/terms" element={<LegalPage kind="terms" />} />
      <Route path="/legal/privacy" element={<LegalPage kind="privacy" />} />
      {!user ? (
        <Route path="*" element={<Welcome onAuthed={setUser} />} />
      ) : (
        <Route element={<Shell />}>
          <Route path="/discover" element={<Discover />} />
          <Route path="/likes" element={<Likes />} />
          <Route path="/matches" element={<Matches />} />
          <Route path="/messages" element={<Chat />} />
          <Route path="/chat/:matchId" element={<Chat />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/subscription" element={<SubscriptionPage />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="/admin" element={<Admin />} />
          <Route
            path="/"
            element={
              user.role === 'USER' ? (
                <Navigate to="/discover" replace />
              ) : (
                <Navigate to="/admin" replace />
              )
            }
          />
          <Route
            path="*"
            element={
              <div className="card">
                Page not found.{' '}
                <button
                  className="btn btn-ghost btn-small"
                  onClick={() => {
                    clearSession();
                    setUser(null);
                  }}
                >
                  Sign out
                </button>
              </div>
            }
          />
        </Route>
      )}
    </Routes>
  );
}
