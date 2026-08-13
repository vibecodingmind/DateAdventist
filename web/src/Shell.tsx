import { NavLink, Outlet } from 'react-router-dom';

export function Shell() {
  return (
    <>
      <div className="page">
        <Outlet />
      </div>
      <nav className="nav">
        <NavLink to="/discover" className={({ isActive }) => (isActive ? 'active' : '')}>
          Discover
        </NavLink>
        <NavLink to="/likes" className={({ isActive }) => (isActive ? 'active' : '')}>
          Likes
        </NavLink>
        <NavLink to="/matches" className={({ isActive }) => (isActive ? 'active' : '')}>
          Matches
        </NavLink>
        <NavLink to="/messages" className={({ isActive }) => (isActive ? 'active' : '')}>
          Chat
        </NavLink>
        <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : '')}>
          Profile
        </NavLink>
      </nav>
    </>
  );
}
