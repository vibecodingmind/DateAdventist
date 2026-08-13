import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, clearSession } from '../api';
import type { Profile } from '../types';

export default function ProfilePage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [bio, setBio] = useState('');
  const [church, setChurch] = useState('');
  const [verse, setVerse] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  useEffect(() => {
    api
      .profile()
      .then((p) => {
        setProfile(p);
        setBio(p.bio);
        setChurch(p.localChurch);
        setVerse(p.favoriteVerse);
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  async function save(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const updated = await api.updateProfile({ bio });
      await api.updateFaith({ localChurch: church, favoriteVerse: verse });
      setProfile({ ...updated, localChurch: church, favoriteVerse: verse, bio });
      setInfo('Profile saved');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    }
  }

  async function onPhoto(file: File | undefined, kind: 'profile' | 'verification') {
    if (!file) return;
    try {
      const res = await api.uploadPhoto(file, kind);
      if (res.profile) setProfile(res.profile);
      setInfo(kind === 'verification' ? 'Verification selfie submitted' : 'Photo updated');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Upload failed');
    }
  }

  if (!profile) return <div className="muted">Loading profile…</div>;

  return (
    <div className="stack">
      <div className="spread">
        <h2 style={{ margin: 0 }}>Profile</h2>
        <button
          className="btn btn-ghost btn-small"
          onClick={() => {
            clearSession();
            navigate('/');
            window.location.reload();
          }}
        >
          Log out
        </button>
      </div>
      {error && <div className="error">{error}</div>}
      {info && <div className="success">{info}</div>}
      <div className="card stack" style={{ alignItems: 'center' }}>
        <img
          src={profile.primaryPhoto}
          alt={profile.fullName}
          style={{ width: 120, height: 120, borderRadius: '50%', objectFit: 'cover' }}
        />
        <h3 style={{ margin: 0 }}>
          {profile.fullName}, {profile.age}
        </h3>
        <div className="muted">
          {profile.occupation} · {profile.city}
        </div>
        <div className="row">
          {profile.isVerified && <span className="badge">VERIFIED</span>}
          {profile.isPremium && <span className="badge gold">GOLD</span>}
        </div>
        <label className="btn btn-ghost btn-small">
          Change photo
          <input type="file" accept="image/*" hidden onChange={(e) => onPhoto(e.target.files?.[0], 'profile')} />
        </label>
      </div>
      <form className="card stack" onSubmit={save}>
        <label>Bio</label>
        <textarea rows={4} value={bio} onChange={(e) => setBio(e.target.value)} />
        <label>Local church</label>
        <input value={church} onChange={(e) => setChurch(e.target.value)} />
        <label>Favorite verse</label>
        <input value={verse} onChange={(e) => setVerse(e.target.value)} />
        <button className="btn btn-primary">Save profile</button>
      </form>
      <div className="card stack">
        <strong>Safety & membership</strong>
        <label className="btn btn-ghost">
          Upload verification selfie
          <input type="file" accept="image/*" hidden onChange={(e) => onPhoto(e.target.files?.[0], 'verification')} />
        </label>
        <Link to="/subscription" className="btn btn-gold">
          Subscription
        </Link>
        <Link to="/settings" className="btn btn-ghost">
          Settings
        </Link>
        <Link to="/admin" className="btn btn-ghost">
          Admin panel
        </Link>
      </div>
    </div>
  );
}
