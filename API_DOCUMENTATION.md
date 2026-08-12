# AdventHearts API & System Architecture Documentation

## Executive Overview
This document provides a comprehensive technical specification for the **AdventHearts** platform. It details the centralized Retrofit network architecture, Room local database entity mapping, complete backend API route specifications (headers, request payloads, response schemas, error formats), and a security audit of server-side Role-Based Access Control (RBAC) middleware for administrative endpoints.

---

## 1. Centralized Networking & Retrofit API Client Architecture

### Client Overview (`AdventHeartsApiClient`)
All HTTP communication in the Android client passes through a unified `AdventHeartsApiClient` configured with `OkHttpClient` interceptors.

Key Responsibilities:
- **Base URL Management**: Dynamically routes requests to `https://ais-dev-76mcn3mxut2jc3whyrmhu6-709051202870.europe-west2.run.app/api/v1/`
- **Header Injection**: Automatically appends `Content-Type: application/json` and `Authorization: Bearer <JWT_TOKEN>` headers.
- **Global Error Handling**: Intercepts HTTP error statuses (`400`, `401`, `403`, `404`, `500`) and triggers corresponding user authentication or error state flows.

### Interceptor Code Pattern
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            
        AuthTokenManager.getToken()?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        val response = chain.proceed(requestBuilder.build())
        
        when (response.code) {
            401 -> AuthTokenManager.onTokenExpired()
            403 -> Log.e("API_SECURITY", "Server RBAC access denied: ${original.url}")
        }
        response
    }
    .build()
```

---

## 2. Room Database Schema & Backend Parity Mapping

The local Room persistence layer (`AdventHeartsDatabase`) maps directly to backend core domain entities.

### Entity Parity Breakdown

| Core Domain Entity | Room Entity (`tableName`) | Key Mapped Fields | Relation / Notes |
| :--- | :--- | :--- | :--- |
| **User** | `user_accounts` | `userId`, `email`, `passwordHash`, `role` (`USER`, `MODERATOR`, `ADMIN`, `SUPER_ADMIN`), `isEmailVerified` | 1-to-1 with `ProfileEntity` |
| **Profile** | `profiles` | `userId`, `fullName`, `age`, `gender`, `country`, `city`, `bio`, `primaryPhoto`, `photoUrls`, `isVerified`, `isPremium` | Embedded Faith & Lifestyle profile attributes |
| **FaithProfile** | `profiles` (embedded) | `adventistAffiliation`, `yearsAsAdventist`, `localChurch`, `isBaptized`, `faithImportance`, `churchInvolvement`, `sabbathObservance`, `diet` | Standardized Adventist lifestyle metrics |
| **Match** | `matches` | `matchId`, `user1Id`, `user2Id`, `compatibilityScore`, `conversationStarter`, `matchedAt` | Relates two users upon mutual like |
| **Message** | `messages` | `messageId`, `matchId`, `senderId`, `receiverId`, `text`, `timestamp`, `isRead` | Child of `MatchEntity` |
| **Report** | `reports` | `reportId`, `reporterId`, `reportedUserId`, `reason`, `details`, `status` (`OPEN`, `RESOLVED`) | Escalated to admin moderation queue |

---

## 3. Backend API Route Reference

### Authentication Endpoints (`/api/v1/auth`)

#### `POST /api/v1/auth/register`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "Password123!",
    "fullName": "Joshua Miller"
  }
  ```
- **Success Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "token": "jwt_token_sample",
      "user": { "id": "usr_101", "email": "user@example.com", "role": "USER" }
    }
  }
  ```

#### `POST /api/v1/auth/login`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "Password123!"
  }
  ```
- **Success Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "token": "jwt_token_sample",
      "user": { "id": "usr_101", "email": "user@example.com", "role": "USER" }
    }
  }
  ```

---

### Discovery & Matching Endpoints (`/api/v1/discover`)

#### `GET /api/v1/discover`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Description**: Returns recommended profile feed.
- **Success Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": [
      { "id": "usr_1", "fullName": "Sarah Jenkins", "age": 26, "isPremium": true, "tier": "GOLD" },
      { "id": "usr_2", "fullName": "David Miller", "age": 29, "isPremium": false, "tier": "FREE" }
    ]
  }
  ```

#### `POST /api/v1/discover/advanced-search`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Middleware**: `requireSubscription('PLUS' | 'GOLD')`
- **Request Body**:
  ```json
  {
    "SabbathObservance": "Sunset to Sunset",
    "dietaryPreference": "Vegetarian",
    "churchAttendance": "Weekly"
  }
  ```
- **Error Response (`403 Forbidden`)**:
  ```json
  {
    "success": false,
    "error": { "code": "SUBSCRIPTION_REQUIRED", "message": "Gold subscription required for advanced search." }
  }
  ```

#### `POST /api/v1/discover/:userId/super-like`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Middleware**: `requireSubscription('GOLD')`
- **Success Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "targetUserId": "usr_1",
      "isSuperLiked": true,
      "isMatch": true,
      "matchId": "match_1770623000000"
    }
  }
  ```

---

### Subscription & Checkout Endpoints (`/api/v1/subscriptions`)

#### `GET /api/v1/subscriptions/plans`
- **Headers**: `Content-Type: application/json`
- **Success Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "plans": [
        {
          "id": "plan_gold_monthly",
          "name": "AdventHearts Gold",
          "tier": "GOLD",
          "priceMonthly": 14.99,
          "priceAnnually": 119.88,
          "currency": "USD",
          "badgeText": "MOST POPULAR"
        }
      ]
    }
  }
  ```

#### `POST /api/v1/subscriptions/checkout`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "planId": "plan_gold_monthly",
    "paymentProvider": "stripe",
    "billingCycle": "MONTHLY"
  }
  ```
- **Success Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "transactionId": "tx_stripe_1770623000000",
      "provider": "stripe",
      "status": "INITIATED",
      "checkoutUrl": "https://checkout.stripe.com/pay/cs_test_adventhearts_tx_stripe_1770623000000"
    }
  }
  ```

#### `POST /api/v1/subscriptions/confirm-payment`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  { "transactionId": "tx_stripe_1770623000000" }
  ```
- **Success Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "active": true,
      "tier": "GOLD",
      "expiresAt": "2026-09-10T00:00:00.000Z"
    }
  }
  ```

---

## 4. Admin Routes & RBAC Security Audit

Server-side security is strictly enforced via Express middleware (`requireAuth` + `requireRole`).

| Endpoint Route | Method | Required RBAC Role | Server Security Status |
| :--- | :--- | :--- | :--- |
| `/api/v1/admin/login` | `POST` | Public / Public Auth | Returns role-scoped token |
| `/api/v1/admin/dashboard` | `GET` | `MODERATOR`+ | Enforced server-side |
| `/api/v1/admin/verifications` | `GET` | `MODERATOR`+ | Enforced server-side |
| `/api/v1/admin/verifications/:id/approve` | `POST` | `MODERATOR`+ | Enforced server-side |
| `/api/v1/admin/reports` | `GET` | `MODERATOR`+ | Enforced server-side |
| `/api/v1/admin/users` | `GET` | `ADMIN`+ | Enforced server-side |
| `/api/v1/admin/analytics` | `GET` | `ADMIN`+ | Enforced server-side |
| `/api/v1/admin/settings` | `GET` | `SUPER_ADMIN` | Enforced server-side |
| `/api/v1/admin/audit-logs` | `GET` | `SUPER_ADMIN` | Enforced server-side |

---

## 5. Global Error Format

All error responses from the backend follow a standardized JSON envelope:
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED_ROLE",
    "message": "Access denied. Action requires SUPER_ADMIN permissions."
  }
}
```
