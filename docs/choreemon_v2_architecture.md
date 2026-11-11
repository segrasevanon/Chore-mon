# Choreémon v2 — Full Azure Cloud Architecture

> All 7 features are required. This document is the complete architecture design.
> No files modified. No resources created. Analysis only.

---

## What Changes Fundamentally

The app goes from:
```
One device → SharedPreferences → Local JSON
```
to:
```
Many devices → REST API → Azure SQL → Shared truth
```

Every piece of the current architecture is affected.

---

## 1. Full System Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                             │
│                                                              │
│  Android App (Kotlin)          Parent Web Portal (React)     │
│  ├── Auth (MSAL SDK)           ├── Auth (MSAL.js)            │
│  ├── API client (Retrofit)     ├── Quest management          │
│  ├── Local cache (Room DB)     ├── Approval queue            │
│  └── FCM push receiver         └── Cross-household rankings  │
└─────────────────────┬───────────────────────┬────────────────┘
                      │ HTTPS                 │ HTTPS
┌─────────────────────▼───────────────────────▼────────────────┐
│                  AZURE AD B2C                                 │
│  (Identity provider — issues JWT tokens to all clients)      │
└─────────────────────────────────────────────────────────────-┘
                      │ JWT Bearer tokens
┌─────────────────────▼────────────────────────────────────────┐
│              BACKEND API (Azure App Service B2)              │
│                   ASP.NET Core / Node.js                     │
│  ├── /api/households     ├── /api/quests                     │
│  ├── /api/members        ├── /api/completions                │
│  ├── /api/creatures      ├── /api/leaderboard                │
│  ├── /api/notifications  └── /api/photos (SAS token issue)   │
└───────────┬─────────────────────┬────────────────────────────┘
            │                     │
  ┌─────────▼──────┐   ┌──────────▼──────────────┐
  │  Azure SQL DB  │   │  Azure Blob Storage       │
  │  (Basic tier)  │   │  (photo proof uploads)    │
  └────────────────┘   └──────────────────────────┘
            │
  ┌─────────▼──────────────────────────────────────┐
  │        Azure Notification Hubs                  │
  │  API → Notification Hub → FCM → Android device  │
  └─────────────────────────────────────────────────┘

Supporting services:
  ├── Azure Application Insights (telemetry on the API)
  ├── Azure Key Vault (secrets: DB connection string, B2C secrets)
  └── Azure Static Web Apps (parent web portal hosting)
```

---

## 2. Azure SQL Schema

Mapped directly from [`GameModels.kt`](file:///c:/Users/rites/Downloads/ProjectCII/Chore-mon/app/src/main/java/com/choreemon/app/data/GameModels.kt):

```sql
-- Households (replaces GameState.household / inviteCode)
Households
├── id            UNIQUEIDENTIFIER PK
├── name          NVARCHAR(100)
├── invite_code   NVARCHAR(12) UNIQUE   -- e.g. "CHEN42"
├── pin_salt      NVARCHAR(32)
├── pin_hash      NVARCHAR(64)
├── week_start    DATE                  -- for weekly score resets
└── created_at    DATETIMEOFFSET

-- Members (replaces GameState.members)
Members
├── id            UNIQUEIDENTIFIER PK
├── household_id  FK → Households
├── b2c_user_id   NVARCHAR(128) NULLABLE  -- null = child profile
├── name          NVARCHAR(64)
├── avatar        INT
├── points        INT DEFAULT 0
├── weekly_points INT DEFAULT 0
└── is_parent     BIT DEFAULT 0

-- Creatures (replaces GameState.creatures)
Creatures
├── id            UNIQUEIDENTIFIER PK
├── owner_id      FK → Members
├── element       NVARCHAR(10)         -- WATER/EARTH/AIR/POISON
├── rarity        NVARCHAR(12)         -- COMMON/RARE/LEGENDARY
├── xp            INT DEFAULT 0
├── stage         INT DEFAULT 0
├── nickname      NVARCHAR(64)
├── adopted_date  DATE
├── loved_on      DATE NULLABLE
├── is_favorite   BIT DEFAULT 0
└── active        BIT DEFAULT 0        -- active companion flag

-- Chores (replaces GameState.chores)
Chores
├── id            UNIQUEIDENTIFIER PK
├── household_id  FK → Households
├── title         NVARCHAR(100)
├── description   NVARCHAR(500)
├── points        INT
├── element       NVARCHAR(10)
├── schedule      NVARCHAR(12)         -- DAILY/WEEKDAYS/WEEKENDS/ONCE
├── requires_approval BIT DEFAULT 0
├── archived      BIT DEFAULT 0
└── steps         NVARCHAR(MAX)        -- JSON array of step strings

-- ChoreAssignments (many-to-many: Chore ↔ Member)
ChoreAssignments
├── chore_id      FK → Chores
└── member_id     FK → Members

-- Completions (replaces GameState.completions)
Completions
├── id            UNIQUEIDENTIFIER PK
├── chore_id      FK → Chores
├── member_id     FK → Members
├── creature_id   FK → Creatures
├── date          DATE
├── title         NVARCHAR(100)
├── points        INT
├── xp            INT
├── status        NVARCHAR(10)         -- PENDING/APPROVED/REJECTED
└── photo_url     NVARCHAR(500) NULLABLE  -- Blob Storage URL

-- DeviceTokens (for push notifications)
DeviceTokens
├── id            UNIQUEIDENTIFIER PK
├── member_id     FK → Members
├── fcm_token     NVARCHAR(256)
├── platform      NVARCHAR(10)         -- ANDROID (iOS later)
└── updated_at    DATETIMEOFFSET

-- ClaimedMilestones (replaces GameState.claimedMilestones set)
ClaimedMilestones
├── member_id     FK → Members
├── milestone_key NVARCHAR(64)         -- "{memberId}:{milestone}"
└── claimed_at    DATETIMEOFFSET
```

**Why SQL over Cosmos DB for these features:**

| Query needed | SQL | Cosmos DB |
|---|---|---|
| Cross-household weekly leaderboard | `ORDER BY weekly_points DESC` — trivial | Requires cross-partition scan — expensive |
| Parent approval queue (pending completions by household) | Simple JOIN | Document model awkward |
| Streak calculation (consecutive approved dates) | Window functions / date arithmetic | Manual in app code |
| "All households where member completed X chores this week" | Aggregation query | Fan-out reads |
| Member XP + creature stage integrity | FK constraints + transactions | No FK enforcement |

The data is **inherently relational**: Members own Creatures, Chores are assigned to Members, Completions reference both. SQL is the right tool.

---

## 3. Azure Services — Final Selection

| Service | Tier | Purpose | Required? |
|---|---|---|---|
| **Azure AD B2C** | Free (≤50k MAU) | Auth for parents; JWT tokens | Yes |
| **Azure App Service** | B2 (~$18/mo) | REST API backend | Yes |
| **Azure SQL Database** | Basic ($5/mo) | All game data | Yes |
| **Azure Blob Storage** | LRS, Pay-as-go | Photo proof uploads | Yes |
| **Azure Notification Hubs** | Free (≤1M pushes/mo) | FCM push to Android | Yes |
| **Azure Static Web Apps** | Free tier | Parent web portal hosting | Yes |
| **Azure Key Vault** | Standard (~$0.03/secret/mo) | DB string, B2C secrets | Yes |
| **Azure Application Insights** | Pay-as-go (minimal) | API telemetry | Recommended |
| **Azure Container Registry** | — | Not needed — App Service deploys from git | No |
| **AKS / VMs** | — | Completely unjustified | No |

---

## 4. Azure for Students — Cost Reality Check

| Service | Monthly Cost | Notes |
|---|---|---|
| Azure AD B2C | $0 | Free up to 50,000 MAU |
| Azure App Service B2 | ~$18 | Cheapest tier with custom domains + always-on |
| Azure App Service B1 | ~$13 | Acceptable for dev; sleeps after 20 min idle |
| Azure SQL Basic | ~$5 | 2 GB storage, 5 DTUs — plenty for this app |
| Azure Blob Storage | ~$0.02 | Photos at ~$0.02/GB; student project = cents |
| Azure Notification Hubs Free | $0 | 1M pushes/month free |
| Azure Static Web Apps Free | $0 | Completely free for 2 apps |
| Azure Key Vault | ~$0.10 | Minimal secret operations |
| Application Insights | ~$0–2 | First 5 GB/month free |
| **Total (prod-like)** | **~$23–26/month** | |
| **Total (dev, B1)** | **~$18–20/month** | |

> **Azure for Students gives $100/year credit.**
> At ~$20/month, your credits last **~5 months**.
> At ~$25/month, your credits last **~4 months**.
> 
> Plan carefully. Shut down App Service when not actively developing.
> Use B1 (not B2) for development; only scale up for demos or load testing.

### Cost-saving moves:
1. Use **App Service F1 (Free)** during initial development — but it sleeps, has no custom domain, and has a 60-min/day CPU limit. Fine for coding, bad for demos.
2. Use **Azure SQL Serverless** instead of Basic — you only pay when queries run. Good for a dev environment that goes quiet overnight.
3. Set up **auto-shutdown schedules** for App Service during off-hours.

---

## 5. API Design

The backend API needs to expose the game domain cleanly. Each `GameRules` function in the current app maps to an API endpoint.

```
POST   /api/auth/household-pin          Verify parent PIN → issue session
POST   /api/households                  Create household (onboarding)
GET    /api/households/{id}             Full game state sync

GET    /api/members                     List household members
POST   /api/members                     Add member (parent only)
PUT    /api/members/{id}/active         Switch active member

GET    /api/chores                      List chores (filtered by schedule/day)
POST   /api/chores                      Create chore (parent only)
PUT    /api/chores/{id}                 Edit chore (parent only)
DELETE /api/chores/{id}                 Archive chore (parent only)

POST   /api/completions                 Submit chore completion
GET    /api/completions/pending         Pending approval queue (parent)
PUT    /api/completions/{id}/review     Approve/reject (parent only)

GET    /api/creatures                   List member's creatures
POST   /api/creatures/hatch             Hatch egg (deducts points, creates creature)
POST   /api/creatures/{id}/evolve       Evolve creature
POST   /api/creatures/{id}/love         Daily love action

GET    /api/leaderboard/weekly          Weekly scores — all households or just this one
GET    /api/leaderboard/household       Household-internal rankings

POST   /api/photos/sas                  Get a short-lived SAS URL → Android uploads directly to Blob

POST   /api/devices/register            Register FCM token for push notifications

GET    /api/milestones/claimable        Check for unclaimed milestone
POST   /api/milestones/claim            Claim milestone gift
```

---

## 6. Push Notification Flow

```
Android App completes quest → POST /api/completions
         ↓
Backend creates Completion (status=PENDING)
         ↓
Backend queries DeviceTokens for parent members in household
         ↓
Backend → Azure Notification Hubs → FCM
         ↓
Parent's Android phone receives: "Mia wants approval for Wash dishes 🧼"
         ↓
Parent taps notification → opens approval screen
         ↓
PUT /api/completions/{id}/review (approve=true)
         ↓
Backend updates DB, grants points + XP
         ↓
Backend → Notification Hubs → FCM → child's phone
         ↓
Child receives: "Your quest was approved! +10 pts 🎉"
```

---

## 7. Photo Proof Flow (Blob Storage SAS pattern)

The app must never hold a long-lived storage key. Use the **SAS token pattern**:

```
Android App → POST /api/photos/sas
                  ↓
             API generates short-lived (5 min) SAS URL for Blob Storage
                  ↓
             SAS URL returned to app
                  ↓
Android App → PUT {sas_url} (uploads photo directly to Blob Storage)
                  ↓
Android App → POST /api/completions (sends blob URL in body)
                  ↓
             API stores blob URL in Completions.photo_url
```

This means:
- No storage key on the device
- API controls which blobs can be written
- Photos are stored at: `photos/{householdId}/{memberId}/{completionId}.jpg`

---

## 8. Authentication Design

```
Parent flow:
  1. Parent downloads app → signs up with email via Azure AD B2C
  2. B2C issues JWT → app stores it → attaches to every API call
  3. Parent creates household → sets PIN for shared-device mode
  4. Parent adds child members (no B2C account needed for children)

Child flow (shared device):
  1. Parent hands phone to child → child selects their profile (no login)
  2. App sends household JWT (parent's) + memberId header
  3. API enforces: children cannot call parent-only endpoints

Web portal flow:
  1. Parent opens browser → authenticates via Azure AD B2C (same identity)
  2. MSAL.js acquires token → same API, same JWT
```

**Why Azure AD B2C and not Firebase Auth:**
- You're already in Azure; B2C integrates natively with App Service managed identity
- Free up to 50,000 MAU — more than enough for a student project
- Supports social login (Google, Microsoft) out of the box
- Issues standard JWTs that App Service can validate without extra code

---

## 9. Required Changes to the Android App

### Must-do (blockers)

| Change | Where | Effort |
|---|---|---|
| Add `INTERNET` permission to `AndroidManifest.xml` | `AndroidManifest.xml` | Trivial |
| Add MSAL Android SDK for B2C auth | `build.gradle.kts` dependency | Low |
| Add Retrofit + OkHttp for API calls | `build.gradle.kts` dependency | Low |
| Add Room database for local offline cache | `build.gradle.kts` dependency | Medium |
| Rewrite `GameRepository` to sync with API | `GameRepository.kt` | **High** — core change |
| Replace `SharedPreferences` JSON with Room cache | `GameRepository.kt` | High |
| Add FCM dependency + token registration | New file | Low |
| Change photo: upload to Blob SAS URL instead of storing URI | `QuestDetail.kt` | Medium |
| Update `GameViewModel` to handle async API calls | `GameViewModel.kt` | High |
| Add network error states to all UI screens | All `ui/` screens | Medium |

### Architecture shift: offline-first vs online-required

You must decide **before writing a line of backend code**:

| Mode | Behaviour | Complexity |
|---|---|---|
| **Online-required** | App shows error if no network. Simpler backend logic. | Low |
| **Offline-first** | App works offline; syncs when connected. Conflict resolution needed. | Very High |

**Recommendation: Online-required for v2.** Offline-first adds months of complexity (conflict resolution, vector clocks, merge strategies). The app already works offline as a local-only app — v2 is the cloud version.

---

## 10. Deployment Architecture

```
VS Code / Android Studio
        ↓  git push
   GitHub Repository
   ┌──────────────────────────────────────────────┐
   │                GitHub Actions                │
   │                                              │
   │  Job 1: Backend                              │
   │  ├── dotnet build / npm build                │
   │  ├── dotnet test                             │
   │  └── az webapp deploy → App Service          │
   │                                              │
   │  Job 2: Android                              │
   │  ├── Setup Android SDK 36                    │
   │  ├── gradle assembleRelease                  │
   │  ├── Sign APK                                │
   │  └── az storage blob upload → Blob Storage   │
   │                                              │
   │  Job 3: Web Portal                           │
   │  ├── npm run build                           │
   │  └── Deploy to Azure Static Web Apps         │
   └──────────────────────────────────────────────┘
```

**Infrastructure as Code: Bicep**

One `main.bicep` file provisions everything:
- Resource Group
- App Service Plan + App Service
- Azure SQL Server + Database
- Storage Account + Blob Container
- Notification Hubs Namespace + Hub
- Key Vault + secrets
- Static Web Apps
- Application Insights

---

## 11. Complete Resource List

```
Resource Group: rg-choreemon-prod
├── App Service Plan: asp-choreemon (B1 dev / B2 prod)
│   └── App Service: app-choreemon-api
│       └── App Settings: (APPLICATIONINSIGHTS_CONNECTION_STRING, KeyVaultUri)
├── Azure SQL Server: sql-choreemon-{unique}
│   └── Database: sqldb-choreemon (Basic / Serverless)
├── Storage Account: stchoremon{unique}
│   └── Blob Containers:
│       ├── photos/        (photo proofs, private)
│       └── apk-releases/  (built APKs, public)
├── Notification Hubs Namespace: ntfns-choreemon
│   └── Notification Hub: ntfh-choreemon
│       └── FCM credential (Google Firebase server key)
├── Azure AD B2C Tenant: choremonb2c.onmicrosoft.com
│   └── User flows: sign-up-sign-in, password-reset
├── Static Web Apps: stapp-choreemon-portal
├── Key Vault: kv-choreemon-{unique}
│   └── Secrets:
│       ├── SqlConnectionString
│       ├── B2cClientSecret
│       ├── NotificationHubConnectionString
│       └── StorageConnectionString (or use Managed Identity)
└── Application Insights: appi-choreemon
    └── Log Analytics Workspace: log-choreemon
```

---

## 12. Security Upgrades Required

| Item | Action |
|---|---|
| DB connection string | Store in Key Vault; App Service reads via Managed Identity |
| B2C client secret | Store in Key Vault |
| Notification Hub connection string | Store in Key Vault |
| FCM server key | Store in Key Vault |
| Android release keystore | GitHub Secret (base64-encoded) |
| Parent PIN | Keep SHA-256+salt, but now salted per-household in SQL — not in SharedPreferences |
| Photo URLs | Never public; always use short-lived SAS tokens |
| CORS | App Service CORS configured to allow only the Static Web Apps domain |
| HTTPS | Enforced by App Service (HTTP → HTTPS redirect on) |
| API Authorization | All endpoints require valid B2C JWT except POST /households |
| Managed Identity | App Service → SQL (no username/password needed in config) |

---

## 13. Phased Implementation Plan

### Phase 1 — Foundation (Week 1–2)
- Create Azure AD B2C tenant + user flows
- Create Azure SQL + schema
- Scaffold ASP.NET Core or Node.js API project
- Implement `/api/households` and `/api/auth`
- Implement JWT validation middleware
- Connect Android app with MSAL + basic Retrofit client

### Phase 2 — Core Game Loop (Week 3–4)
- Implement `/api/chores`, `/api/completions`, `/api/creatures`
- Replace `GameRepository.SharedPreferences` with API calls
- Add Room DB as local cache
- Implement weekly score reset (background job or on-read logic)

### Phase 3 — Push + Photos (Week 5)
- Register for FCM in Android app
- Implement `POST /api/devices/register`
- Implement Notification Hubs trigger on pending completion
- Implement SAS token endpoint + Android blob upload

### Phase 4 — Web Portal (Week 6)
- Scaffold React app (or Next.js)
- Implement parent approval queue UI
- Implement cross-household leaderboard
- Deploy to Azure Static Web Apps

### Phase 5 — CI/CD + Hardening (Week 7)
- Write Bicep templates for all resources
- Set up GitHub Actions for all three deployment jobs
- Add Application Insights to API
- Security review: CORS, HTTPS, Key Vault, Managed Identity

---

## 14. Final Architecture Summary

### Project Type
Cloud-connected Android game with REST backend, SQL database, push notifications, photo storage, authentication, and a parent web portal.

### Recommended Azure Architecture

```
[Android App]          [Parent Web Portal]
     │                       │
     │ HTTPS + JWT            │ HTTPS + JWT
     └──────────┬────────────┘
                ▼
        [Azure AD B2C]
                │ validates tokens
                ▼
      [Azure App Service]
       (REST API backend)
          │        │
          │        └──→ [Azure Notification Hubs] ──→ FCM ──→ Devices
          │
          ├──→ [Azure SQL Database]  (all game data)
          ├──→ [Azure Blob Storage]  (photo proofs via SAS)
          └──→ [Azure Key Vault]     (all secrets)

CI/CD: GitHub Actions → az webapp deploy + Static Web Apps deploy
Infra: Bicep
```

### Estimated Complexity
**Difficult** — This is a full product rebuild, not a deployment. Plan for 6–8 weeks of development time.

### Biggest Risks

| Risk | Mitigation |
|---|---|
| `GameRules` logic (pure Kotlin) must be duplicated in backend | Write backend in Kotlin (Ktor) to share domain logic, or accept duplication |
| Offline behaviour UX (what happens when API is unreachable?) | Decide online-required for v2; add offline-first in v3 |
| Azure for Students credits running out mid-project | Use Free/Serverless tiers in dev; prod only during demos |
| FCM setup requires a Firebase project (separate from Azure) | Create a free Firebase project just for FCM credentials; feed key into Notification Hubs |
| B2C tenant is a separate Azure tenant, not in the subscription | This is expected — B2C is always a separate directory; budget $0 for it |

### Next Step

Decide on the backend language/framework before writing any code:

| Option | Pros for this project |
|---|---|
| **Kotlin + Ktor** | Share `GameModels.kt` and `GameRules.kt` directly — no logic duplication |
| **ASP.NET Core (C#)** | Best Azure integration, great tooling, many Azure SDK samples |
| **Node.js + Express/Fastify** | Fastest to prototype; large ecosystem |

The Kotlin + Ktor option is particularly attractive here because your entire domain model and game rules are already written and tested in Kotlin — you can share them between the Android app and the backend as a multiplatform module.
