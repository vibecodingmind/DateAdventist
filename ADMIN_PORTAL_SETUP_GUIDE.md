# AdventHearts Admin Portal Setup & Operational Guide

## Overview
This document serves as the official administrative setup and security operation manual for **AdventHearts**. It outlines the client portal entry points, super admin initialization procedure, role-based access control (RBAC) levels, and protected administrative API routes.

---

## 1. Admin Portal Navigation & URL Paths

### Client Portal Route & Entry Point
- **Portal Login Screen**: `admin_login` (Accessible in-app via Profile -> Admin Portal or direct route `admin_login`)
- **Dashboard Screen**: `admin_dashboard` (Accessible upon successful authentication)
- **Backend API Base Endpoint**: `https://ais-dev-76mcn3mxut2jc3whyrmhu6-709051202870.europe-west2.run.app/api/v1/admin`

---

## 2. Initial Super Admin Setup & Seed Procedure

### CLI Seed Execution
To initialize or restore the initial Super Admin account and administrative seed records on the server:

1. Open the backend environment directory (`/backend`).
2. Run the seed CLI script:
   ```bash
   npm run seed:admin
   ```
   *This initializes the database schema, creates default role profiles, and logs the initial audit record (`SUPER_ADMIN_CREATED`).*

### Default Provisioned Administrative Credentials

| Role Level | Email Address | Default Password | Granted Permissions |
| :--- | :--- | :--- | :--- |
| **SUPER_ADMIN** | `superadmin@adventhearts.com` | `AdminPass2026!` | Full administrative access, Payment Gateways (Stripe/PayPal), Audit Logs, RBAC configuration |
| **ADMIN** | `admin@adventhearts.com` | `AdminPass2026!` | User management table, Platform Analytics, Financial metrics, Moderation |
| **MODERATOR** | `moderator@adventhearts.com` | `AdminPass2026!` | Verification Queue (Selfie photo approvals), User Reports Queue, Metrics Dashboard |

---

## 3. Step-by-Step Login Procedure

1. Launch the **AdventHearts** Android application.
2. Navigate to the **Profile** tab or tap the **Admin Portal** button on top navigation.
3. On the **Admin Portal Login Screen**:
   - Enter your assigned administrative email address (e.g., `superadmin@adventhearts.com`).
   - Enter your secure administrative password.
   - Alternatively, tap one of the **Quick Dev Role** selector buttons (`SuperAdmin`, `Admin`, `Moderator`) for rapid testing.
4. Tap **Login to Admin Dashboard**.
5. The system authenticates with `/api/v1/admin/login`, returns a role-scoped JWT token, and redirects to the **Admin Dashboard**.
6. The dashboard displays active RBAC badge indicator in the header (e.g., `SUPER_ADMIN`, `ADMIN`, `MODERATOR`).

---

## 4. Complete Protected Admin Routes & APIs

All administrative API endpoints require a valid JWT Bearer token and are enforced server-side by `requireAuth` and `requireRole` middleware.

### 1. Authentication
- **`POST /api/v1/admin/login`**
  - **Access**: Public
  - **Description**: Verifies credentials and returns role-scoped JWT token.

### 2. High-Level Metrics & Dashboard
- **`GET /api/v1/admin/dashboard`**
  - **Access**: `MODERATOR`, `ADMIN`, `SUPER_ADMIN`
  - **Description**: Retrieves high-level active user count, open reports count, pending verifications count, and system health status.

### 3. Moderation & Verification Queues
- **`GET /api/v1/admin/verifications`**
  - **Access**: `MODERATOR`, `ADMIN`, `SUPER_ADMIN`
  - **Description**: Returns queue of pending photo selfie verifications awaiting manual review.
- **`POST /api/v1/admin/verifications/:userId/approve`**
  - **Access**: `MODERATOR`, `ADMIN`, `SUPER_ADMIN`
  - **Description**: Approves a user's verification selfie and awards the **Verified Blue Checkmark**.
- **`GET /api/v1/admin/reports`**
  - **Access**: `MODERATOR`, `ADMIN`, `SUPER_ADMIN`
  - **Description**: Fetches open profile and content moderation reports.

### 4. User Management & Analytics
- **`GET /api/v1/admin/users`**
  - **Access**: `ADMIN`, `SUPER_ADMIN`
  - **Description**: Fetches user account table including verification status, roles, and premium status.
- **`GET /api/v1/admin/analytics`**
  - **Access**: `ADMIN`, `SUPER_ADMIN`
  - **Description**: Fetches financial conversion metrics, monthly recurring revenue (MRR), and match performance.

### 5. System Configuration & Audit Logs
- **`GET /api/v1/admin/settings`**
  - **Access**: `SUPER_ADMIN` only
  - **Description**: Retrieves Stripe and PayPal payment gateway settings, sandbox mode toggles, and maintenance mode status.
- **`GET /api/v1/admin/audit-logs`**
  - **Access**: `SUPER_ADMIN` only
  - **Description**: Displays immutable system audit log trail detailing administrative actions and timestamped operator records.

---

## 5. Security & RBAC Enforcement Audit Summary

- **Client vs Server Boundary**: Client-side screens filter visible controls based on the logged-in role (`SUPER_ADMIN`, `ADMIN`, `MODERATOR`). However, every backend request independently validates the user's role in Express middleware (`requireRole`). Attempting to call `/api/v1/admin/settings` with an `ADMIN` or `MODERATOR` token returns a `403 Forbidden` response.
- **Token Invalidation**: Unauthenticated or expired requests return `401 Unauthorized`, automatically redirecting the user to the admin login view.
