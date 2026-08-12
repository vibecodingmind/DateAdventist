# AdventHearts Backend Admin Setup & Operational Guide

## Overview
This document provides complete instructions for setting up, seeding, accessing, and operating the **AdventHearts** administrative backend and portal.

---

## 1. Local Admin Portal URLs & Entry Points

- **Frontend Navigation Route**: `admin_login` -> `admin_dashboard`
- **In-App Direct Access**: Tap **Admin Portal** from the main app menu or Profile screen.
- **Backend Base API Route**: `https://ais-dev-76mcn3mxut2jc3whyrmhu6-709051202870.europe-west2.run.app/api/v1/admin`
- **Development Server Host**: `http://localhost:3000/api/v1/admin`

---

## 2. Super Admin Initial Setup Process (CLI Seed Command)

To initialize the Super Admin account safely on the server without exposing an open registration endpoint:

1. Navigate to the backend directory:
   ```bash
   cd /backend
   ```
2. Configure optional environment variables in `.env`:
   ```env
   SUPER_ADMIN_EMAIL=superadmin@adventhearts.com
   SUPER_ADMIN_PASSWORD=AdminPass2026!
   ```
3. Execute the seed script:
   ```bash
   npm run seed:admin
   ```
4. Output:
   ```text
   --- AdventHearts Super Admin Seed Tool ---
   ✅ Super Admin Account successfully provisioned!
   Email: superadmin@adventhearts.com
   Role: SUPER_ADMIN
   User ID: usr_super_admin
   Credentials hash generated securely with Argon2id.
   ```

---

## 3. Login Procedure

1. Launch the **AdventHearts** application and navigate to the **Admin Login** screen.
2. Enter administrative credentials:
   - **Super Admin**: `superadmin@adventhearts.com` / `AdminPass2026!`
   - **Admin**: `admin@adventhearts.com` / `AdminPass2026!`
   - **Moderator**: `moderator@adventhearts.com` / `AdminPass2026!`
   *(Or click one of the quick dev role buttons: **SuperAdmin**, **Admin**, **Moderator**).*
3. Tap **Login to Admin Dashboard**.
4. The system issues a `POST /api/v1/admin/login` request, receives a role-scoped JWT token, and stores it in the `AuthTokenManager`.
5. You are redirected to `admin_dashboard` with active role badges and restricted access according to your RBAC level.

---

## 4. Complete List of Protected Admin API Routes & RBAC Roles

All `/api/v1/admin/*` endpoints are protected by `requireAuth` and `requireRole` middleware.

| Route | HTTP Method | Required Role Level | Description |
| :--- | :--- | :--- | :--- |
| `/api/v1/admin/login` | `POST` | Public / Public Auth | Authenticate admin credentials and return role-scoped JWT |
| `/api/v1/admin/dashboard` | `GET` | `MODERATOR`+ | High-level metrics: user count, pending verifications, report queues |
| `/api/v1/admin/verifications` | `GET` | `MODERATOR`+ | List pending user selfie verification submissions |
| `/api/v1/admin/verifications/:userId/approve` | `POST` | `MODERATOR`+ | Approve user photo verification and award Verified checkmark badge |
| `/api/v1/admin/reports` | `GET` | `MODERATOR`+ | List content and profile moderation reports |
| `/api/v1/admin/users` | `GET` | `ADMIN`+ | Full user account management table |
| `/api/v1/admin/analytics` | `GET` | `ADMIN`+ | Platform financial conversion & MRR metrics |
| `/api/v1/admin/settings` | `GET` | `SUPER_ADMIN` | Stripe/PayPal gateway keys and maintenance mode settings |
| `/api/v1/admin/audit-logs` | `GET` | `SUPER_ADMIN` | Immutable admin activity audit log history |

---

## 5. Security & RBAC Enforcement Summary

- **Server-Side Security**: All endpoints enforce `requireRole(...)` at the Express route level. Client-side attempts to bypass RBAC will receive `403 Forbidden`.
- **Token Invalidation**: Unauthenticated requests or expired JWT tokens return `401 Unauthorized` and trigger re-authentication.
