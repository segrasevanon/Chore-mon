from pathlib import Path
import hashlib
import json
import re

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent
OUT.mkdir(parents=True, exist_ok=True)

SOURCES = {
    'A1': ('Chore-mon/app/src/main/java/com/choreemon/app/MainActivity.kt', ['fun ChoreemonApp', 'Lifecycle.Event.ON_STOP', 'ParentPinDialog']),
    'A2': ('Chore-mon/app/src/main/java/com/choreemon/app/data/GameViewModel.kt', ['fun update', 'fun createHousehold', 'fun addMember']),
    'A3': ('Chore-mon/app/src/main/java/com/choreemon/app/data/GameRepository.kt', ['object SaveCodec', 'object ParentPin', 'class GameRepository']),
    'A4': ('Chore-mon/app/src/main/java/com/choreemon/app/data/GameModels.kt', ['data class GameState', 'object GameRules', 'fun claimMilestone']),
    'A5': ('Chore-mon/app/src/main/AndroidManifest.xml', ['android:allowBackup', 'android:exported']),
    'A6': ('Chore-mon/app/src/main/java/com/choreemon/app/ui/AdminScreen.kt', ['fun ParentPinDialog', 'attempts >= 5']),
    'A7': ('Chore-mon/app/src/main/java/com/choreemon/app/ui/QuestDetail.kt', ['fun PhotoProof', 'takePersistableUriPermission']),
    'A8': ('Chore-mon/app/build.gradle.kts', ['minSdk', 'dependencies']),
    'A9': ('Chore-mon/app/src/test/java/com/choreemon/app/data/GameRulesTest.kt', ['class GameRulesTest']),
    'A10': ('Chore-mon/app/src/androidTest/java/com/choreemon/app/GameFlowTest.kt', ['class GameFlowTest']),
    'A11': ('Chore-mon/verification/QA.md', ['21 tests passed', '4 instrumented tests']),
    'B1': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/Application.kt', ['fun Application.module', 'anyHost()', 'validate {', 'authenticate("jwt")']),
    'B2': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/config/ServiceRegistry.kt', ['fun create', 'if (config.isAzure)', 'FcmDirectService']),
    'B3': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/config/AppConfig.kt', ['fun load', 'expiryHours']),
    'B4': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/db/DatabaseFactory.kt', ['fun init', 'maximumPoolSize', 'suspend fun <T> dbQuery']),
    'B5': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/db/tables/Tables.kt', ['object Households', 'object Completions']),
    'B6': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/AuthRoutes.kt', ['post("/register")', 'post("/login")', 'post("/join")', 'post("/device-token")']),
    'B7': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/HouseholdsRoutes.kt', ['pinSalt = household.pinSalt', 'post("/verify-pin")', 'put("/pin")']),
    'B8': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/MembersRoutes.kt', ['fun Route.membersRoutes', 'post("/milestone")']),
    'B9': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/ChoresRoutes.kt', ['post {', 'put("/{id}")', 'delete("/{id}")']),
    'B10': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/CompletionsRoutes.kt', ['post {', 'post("/{id}/review")', 'updateStatus']),
    'B11': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/CreaturesRoutes.kt', ['post("/hatch")', 'post("/{id}/evolve")', 'patch("/{id}")']),
    'B12': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/PhotosRoutes.kt', ['post("/upload")', 'get("/{filename}")']),
    'B13': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/LeaderboardRoutes.kt', ['weeklyPoints', 'totalPoints = m.points']),
    'B14': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/HouseholdRepository.kt', ['fun createHousehold', 'fun updateWeekStart', 'fun generateInviteCode']),
    'B15': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/MemberRepository.kt', ['fun updatePoints', 'fun resetWeeklyPoints', 'fun getDeviceTokensForHousehold']),
    'B16': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/ChoreRepository.kt', ['fun findByHousehold', 'val filtered', 'fun updateChore']),
    'B17': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/CompletionRepository.kt', ['fun createCompletion', 'fun updateStatus']),
    'B18': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/CreatureRepository.kt', ['fun addXp', 'fun evolve', 'fun setActive']),
    'B19': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/LocalJwtAuth.kt', ['Algorithm.HMAC256', 'fun verifier', 'fun generateToken']),
    'B20': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/services/azure/AzureB2CAuth.kt', ['Algorithm.none()', 'return ""']),
    'B21': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/LocalFileStorage.kt', ['override suspend fun upload', 'override suspend fun getFile']),
    'B22': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/services/azure/AzureBlobStorage.kt', ['override suspend fun upload', 'return null']),
    'B23': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/FcmDirectService.kt', ['if (serverKey.isNullOrBlank())', 'httpClient.post', 'logger.error']),
    'B24': ('choreemon-backend/src/main/kotlin/com/choreemon/backend/domain/GameModels.kt', ['data class GameState', 'object GameRules']),
    'B25': ('choreemon-backend/build.gradle.kts', ['version =', 'val exposed_version', 'archiveClassifier']),
    'B26': ('choreemon-backend/Dockerfile', ['FROM', 'COPY --from', 'USER choreemon']),
    'B27': ('choreemon-backend/docker-compose.yml', ['services:', 'env_file:', 'volumes:']),
    'B28': ('choreemon-backend/gradle/wrapper/gradle-wrapper.properties', ['distributionUrl']),
}

evidence = []
for key, (name, needles) in SOURCES.items():
    raw = (ROOT / name).read_bytes()
    lines = raw.decode('utf-8-sig').splitlines()
    locators = []
    for needle in needles:
        matches = [i + 1 for i, line in enumerate(lines) if needle in line]
        if not matches:
            raise ValueError(f'Missing source anchor {key}: {needle}')
        locators.append({'symbol': needle, 'lines': matches})
    evidence.append({'id': key, 'path': name, 'sha256': hashlib.sha256(raw).hexdigest(), 'locators': locators})

schema = []
for file in sorted((ROOT / 'choreemon-backend/src/main/resources/db/migration').glob('*.sql')):
    raw = file.read_bytes()
    text = raw.decode('utf-8-sig')
    key = file.name.split('__')[0]
    evidence.append({'id': key, 'path': file.relative_to(ROOT).as_posix(), 'sha256': hashlib.sha256(raw).hexdigest(), 'locators': [{'symbol': 'complete migration', 'lines': [1, len(text.splitlines())]}]})
    for match in re.finditer(r'CREATE TABLE (\w+)\s*\((.*?)\n\);', text, re.S):
        name, body = match.groups()
        cols = []
        for line in body.splitlines():
            m = re.match(r'\s*(\w+)\s+(NVARCHAR\([^)]*\)|INT|BIT)\s+(NOT NULL|NULL)(.*)', line)
            if m:
                field, dtype, nullable, suffix = m.groups()
                default = re.search(r'DEFAULT\s+([^,]+)', suffix)
                cols.append({'name': field, 'type': dtype, 'nullable': nullable == 'NULL', 'default': default.group(1).strip() if default else ''})
        pks = re.search(r'PRIMARY KEY\s*\(([^)]*)\)', body)
        pk = [x.strip() for x in pks.group(1).split(',')] if pks else [c['name'] for c in cols if re.search(r'\b' + c['name'] + r'\s+[^\n]*PRIMARY KEY', body)]
        fks = [{'column': a, 'parent': b, 'parent_column': c} for a, b, c in re.findall(r'FOREIGN KEY\s*\((\w+)\)\s+REFERENCES\s+(\w+)\((\w+)\)', body)]
        schema.append({'name': name, 'columns': cols, 'pk': pk, 'fk': fks, 'migration': key})

(OUT / 'evidence.json').write_text(json.dumps({'review_date': '2026-10-19', 'android_commit': 'fc5e250a0eb17c9fb585d2f8687a3061ff0238b4', 'sources': evidence}, indent=2), encoding='utf-8')
(OUT / 'schema.json').write_text(json.dumps(schema, indent=2), encoding='utf-8')
source_index = '# Choreemon source index\n\nReviewed on 19 October 2026. Paths are relative to the ProjectCII workspace. Evidence IDs used in the documents resolve below. SHA256 hashes and exact symbol locations are in evidence.json. The Android checkout reports commit fc5e250a0eb17c9fb585d2f8687a3061ff0238b4; the backend directory has no Git metadata.\n\n'
for entry in evidence:
    source_index += f"## {entry['id']}\n\n[{entry['path']}](../../{entry['path']})\n\n"
    for loc in entry['locators']:
        source_index += f"- {loc['symbol']}: lines {', '.join(map(str, loc['lines']))}\n"
    source_index += '\n'
(OUT / 'source-index.md').write_text(source_index, encoding='utf-8')

DESIGN = r'''# Choreemon software design document

Reviewed 19 October 2026 | Audience Engineering and technical reviewers

## System overview

Choreemon turns household chores into quests. Members earn points, hatch creatures, and develop their active companion through experience points. Adults manage quests and review submissions. The implemented Android experience runs entirely on one shared device. A separate Kotlin backend implements an API and SQL persistence, but the Android project has no API client, network permission, or authentication integration. These are two independent execution paths today. [A1, A2, A5, A8, B1]

The main engineering decision is to preserve the offline app as a working product while treating backend integration as unfinished work. Azure authentication and storage are placeholders, and backend authorization and transaction handling need remediation before exposure to untrusted clients. The accompanying threat model gives the security findings and acceptance criteria.

## Scope and implementation status

| Area | Observed implementation |
| --- | --- |
| Android product | Compose screens, local profiles, quests, approvals, creature collection, egg hatching, rankings, settings and local photos |
| Android persistence | One version 1 JSON aggregate in private SharedPreferences |
| Backend | Ktor REST routes, repositories, SQL Server schema and Flyway migrations |
| Local authentication | Email and BCrypt password login with HMAC signed JWTs; invite code enrollment |
| Azure mode | Selects an unsigned JWT verifier and a storage adapter that returns URLs without uploading bytes |
| Notifications | Backend attempts legacy FCM sends or logs messages when its server key is absent; Android has no FCM integration |
| Proposed infrastructure | Parent web portal, Room cache, MSAL integration, managed secrets and Azure deployment are described in older documents but have no corresponding implementation here |

The two existing root documents remain historical design inputs. Their ASP.NET or Node backend and complete Azure platform diagram do not describe the current Kotlin implementation. Source code and migrations take precedence in this document. [B1, B2, B20, B22, B23]

## Architecture

![Implemented architecture](diagrams/architecture.png)

The left path is the shipped Android design. Compose reads a StateFlow owned by GameViewModel. User actions invoke pure GameRules functions or direct state transformations, persist the new aggregate, and publish it to the UI. The right path is the independent backend. Requests pass through Ktor plugins and authentication to routes, repositories, and SQL Server. ServiceRegistry chooses authentication and storage adapters using ENV. There is no implemented connection between the two paths. [A1, A2, B1, B2]

| Component | Responsibility | Evidence |
| --- | --- | --- |
| MainActivity and ui package | Manual route state, tabs, dialogs, animation, lifecycle refresh and parent session gate | A1 |
| GameViewModel | StateFlow state, local actions, household creation and save orchestration | A2 |
| GameRules and model types | Quest eligibility, reward calculation, mood, streaks, evolution and milestone rules | A4, B24 |
| SaveCodec and GameRepository | Explicit JSON encoding, private preferences, recovery copies | A3 |
| Application | Plugin installation, JSON, CORS, errors, JWT validation and route registration | B1 |
| ServiceRegistry | Composition root for adapters and repositories | B2 |
| DatabaseFactory | Local database bootstrap, Flyway, Hikari connection pool and suspended transactions | B4 |
| Repository classes | Exposed SQL queries and per-operation transactions | B14 to B18 |
| Service interfaces | Authentication, file storage and notifications | B2, B19 to B23 |

Android targets SDK 36, supports SDK 26 and later, and compiles for Java 17. The root build declares Android Gradle Plugin 9.0.1 and Compose compiler plugin 2.2.10. Its app uses Compose BOM 2025.04.01. Backend build declarations pin Kotlin 2.2.20, Ktor 3.0.3, Exposed 0.55.0, Flyway 10.17.3, HikariCP 5.1.0 and SQL Server JDBC 12.8.1. These are source declarations, not a dependency vulnerability assessment. [A8, B25]

## Android state and lifecycle

GameState holds the household name, display invite code, current profile and companion, members, creatures, chores, completions, week, PIN verifier, preferences and milestone keys. These are immutable data classes with copied lists. The local Member type has no role or online identity. Switching profiles selects another household member and their first companion without separate login. [A2, A4]

At startup, GameRepository reads the state key from choreemon_household preferences. No saved state produces the demonstration household. A malformed or unsupported save is copied to a recovery key and the demo is returned. SaveCodec checks version 1, a valid active member, nonnegative member balances and at least one creature per member. It does not comprehensively validate all reference IDs, dates, enum ranges or active creature ownership. [A3]

GameViewModel.update refreshes the current week, transforms state, calls SharedPreferences.apply and emits the new state. apply updates preferences asynchronously on disk; this is not a synchronous durability acknowledgement. The UI refreshes on resume and every 60 seconds while composed. Parent authorization is an in-memory flag cleared on ON_STOP. It gates management screens; the ViewModel itself is not a separate authorization boundary. [A1, A2, A3]

The system picker supplies photo URIs. The app attempts to retain read permission and stores the URI rather than copying image bytes. Rendering tolerates inaccessible images with a placeholder. The app disables Android backup. Clearing app data or uninstalling removes progress, and revoking a URI grant or removing its source image may make proof unavailable. [A3, A5, A7]

## Domain behavior

| Rule | Implemented behavior |
| --- | --- |
| Quest eligibility | Active member must be assigned; chore must be due and unarchived; no nonrejected completion may already occupy the relevant day or one-time quest |
| Schedules | DAILY, WEEKDAYS, WEEKENDS and ONCE; one-time eligibility depends on completion history |
| Rewards | Approved quest points increase wallet and weekly score; active creature gets rounded points multiplied by mood, with XP at least 1 |
| Mood | HAPPY at 0 to 1 days since adoption or latest approved chore; CONTENT at 2 to 3; SAD thereafter; daily love lifts one step |
| Progression | Level is XP divided by 40 plus 1; explicit evolution at 360 and 960 XP; three stages |
| Egg cost and odds | Classic 50 points with 60/35/5 percent common/rare/legendary; Enchanted 100 with 25/65/10; Aurora 200 with 10/60/30 |
| Family selection | Water, Earth, Air and Poison families are selected independently of rarity; duplicates allowed |
| Spending | Egg cost reduces wallet only; weekly ranking score is retained |
| Streak | Distinct consecutive approved submission dates; yesterday can anchor an unfinished current day |
| Milestone | Current floor of approved completion count divided by 10 yields a key; an unclaimed positive key grants 25 wallet points only |
| Weekly rollover | Android resets weekly points on Monday through refresh; backend reset helper methods exist but have no callers |

These rules are implemented in separately maintained Android and backend files. They are similar copies with differences in defaults, visibility and signatures, rather than a shared Gradle module. Milestone claims use the current count bucket; the implementation does not enumerate every previously missed reward bucket. Dates use LocalDate.now on each runtime without a household timezone setting. [A4, B24, B14, B15]

## Core workflows

### Local quest completion and review

The quest screen requires its checklist to be checked before enabling submission. GameRules independently checks assignment, schedule and completion history, calculates a reward, and creates a completion snapshot containing title, points, XP, companion and optional photo URI. The checklist itself is UI state and is not persisted as evidence. A quest requiring approval becomes PENDING with no reward; otherwise it becomes APPROVED and grants points and XP in the same state transformation. [A4, A7]

Parent review requires the completion to be PENDING. Approval changes status and grants its stored reward exactly once in sequential local state use. Rejection changes it to REJECTED so the member may retry. Delayed approval credits the currently refreshed week while preserving the original submission date used by the streak. This sequential guarantee does not establish concurrent backend safety. [A2, A4]

### Local hatching and evolution

Hatching checks balance, rolls rarity and family, deducts wallet points and adds the creature before the reveal animation. The UI can then store a nickname. Evolution checks ownership and the next stage threshold. Local household setup creates one member, a free starter, four starter quests and a new salted PIN verifier. Demo mode seeds balances and history and uses its documented demonstration PIN. [A1, A2, A4]

### Backend enrollment and authentication

Register creates a household, a parent with email and BCrypt password hash, a starter creature and a signed token in local mode. Login verifies credentials and returns a member, household and token. Public join accepts a reusable household code, creates a child member and starter and issues a child token. No parent approval, expiry or usage limit is attached to that code. Children created by join have no email or password, and no renewal or account recovery flow is implemented. Each repository write commits separately, so enrollment can leave partial records on failure. [B6, B14, B19]

### Backend quest completion and review

Submission loads household lists, calls GameRules.complete, discards the returned state, builds another Completion, and writes it through the repository. Automatic rewards then update the member and creature in separate transactions. Review reads status, changes status, and applies the stored reward through separate calls. The pure rule checks are useful, but do not serialize concurrent requests or recover partial commits. Hatching similarly creates a creature before deducting its cost. [B10, B11, B15, B17, B18]

### Backend notifications and files

Quest creation notifies assigned member tokens. Pending completion notifications are intended for parents, but the repository selects all household members other than the submitter; it does not filter is_parent. Network sends are awaited by the request, not queued durably. Missing FCM configuration logs titles and bodies. The configured sender uses the legacy /fcm/send endpoint, not HTTP v1. [B9, B10, B15, B23]

Photo upload writes each multipart file through the selected adapter and returns the last URL. Local mode writes to uploads and serves through an authenticated API route. It has no photo metadata table or tenant owner binding. Azure mode returns a Blob-shaped URL without writing the stream, and its read method always returns null. MinIO is started by Compose but is not called by the selected local file adapter. [B2, B12, B21, B22, B27]

## API inventory

All paths below are under /api unless shown otherwise. JWT refers to Ktor's configured verifier. Parent checks rely on the token claim. The table describes current guards and their gaps, not desired policy. [B1, B6 to B13]

| Method and path | Current guard | Main behavior or gap |
| --- | --- | --- |
| GET /health | Public | Static status ok; does not test DB or file storage |
| POST /auth/register | Public | Creates household and parent; nonblank email and password only |
| POST /auth/login | Public | Credential check and token issuance |
| POST /auth/join | Public invite code | Creates child and issues token |
| POST /auth/device-token | JWT | Saves token for userId |
| GET /household | JWT household | Aggregate state includes PIN salt and hash |
| POST /household/verify-pin | JWT household | Verifies supplied PIN; returns a boolean, not elevated authority |
| PUT /household/pin | JWT parent | Changes household PIN; length check only |
| GET /members | JWT household | Lists household members |
| POST /members | JWT parent | Adds member with requested isParent role |
| GET /members/{id}/streak | JWT household | Computes requested ID against household history |
| POST /members/milestone | JWT user and household | Claims key then adds 25 points separately |
| GET /chores | JWT household | Lists active chores; double where call causes query failure |
| POST /chores | JWT parent | Creates chore; assigned IDs not checked for same household |
| PUT /chores/{id} | JWT parent | Updates by ID without household predicate |
| DELETE /chores/{id} | JWT parent | Archives by ID without household predicate |
| GET /completions | JWT household | Household completion history |
| POST /completions | JWT user and household | Creates completion and possibly rewards |
| POST /completions/{id}/review | JWT parent | Reviews by ID without household predicate |
| GET /creatures | JWT user | Lists owned creatures |
| POST /creatures/hatch | JWT user and household | Creates creature, then debits wallet |
| POST /creatures/{id}/evolve | JWT owner | Checks threshold, then increments stage separately |
| POST /creatures/{id}/love | JWT owner | Stores today's love date |
| PATCH /creatures/{id} | JWT owner | Nickname, favorite and active selection |
| POST /photos/upload | JWT | Multipart upload without tenant metadata |
| GET /photos/{filename} | JWT | Any authenticated principal can request a known filename |

IllegalArgumentException maps to 400, SecurityException to 403, NoSuchElementException to 404 and other throwables to a generic 500. Some domain error calls therefore become 500 responses. No pagination, API version prefix, request idempotency key, token refresh or explicit logout endpoint is implemented. [B1]

## Persistence design

The backend has eight tables and ten foreign keys across seven migrations. Households owns Members and Chores. Members owns Creatures, DeviceTokens and ClaimedMilestones. ChoreAssignments represents the many-to-many assignment of chores to members. Completions references one chore, member and creature, preserving the reward snapshot when the quest is edited. See the ER document for every column, nullability, default and constraint. [B5, V1 to V7]

SQL types are NVARCHAR, INT and BIT. UUIDs, dates and timestamps are strings. steps is JSON in NVARCHAR(MAX), and photo_url is an unstructured string. Creature species, level, mood, streak and rank are computed values. Preferences and onboarding flags are not separate backend tables. is_active is a database flag; the public Creature DTO omits it, while household state returns one activeCreature ID. [A4, B5, B7, B24]

Each dbQuery opens an Exposed suspended transaction on Dispatchers.IO. Hikari uses a pool of 10 and READ_COMMITTED isolation. Flyway migrations execute at startup before pool connection. Development mode attempts to create the database with an interpolated DB_NAME, so that value must remain trusted configuration. Azure mode enables JDBC encryption and certificate checking; local mode disables encryption. [B4]

## Operational design and known gaps

| Finding | Consequence and recommended direction |
| --- | --- |
| Double where calls in chore listing and notification recipient lookup | Exposed 0.55 rejects a second where clause. Combine predicates or use andWhere; cover household reads and pending submission notifications. |
| Backend weekly reset is unwired | Persist week transition and counter reset atomically, or derive weekly scores from dated reward entries. Current aggregate reconstruction defaults to the current week, hiding the missed reset. |
| Transactions stop at repository methods | Add an application service transaction for enrollment, completion, review, hatch and milestone; use conditional writes and concurrency tests. |
| Validation differs across app and server | Reuse a shared domain module or contract fixtures, validate names, point bounds, assigned household IDs, steps and photo references on the server. |
| Household fetch builds an aggregate from independent reads | Responses may mix concurrent states; define snapshot consistency and paginate long history. |
| Local save rewrites the whole aggregate | History growth increases JSON processing, memory and disk work; define retention or move to structured persistence if scale requires it. |
| Docker build and wrapper differ | Backend wrapper declares Gradle 9.8.1; Docker uses Gradle 8.10. Its COPY expects a versioned JAR, while existing build output is choreemon-backend-all.jar. Verify actual clean build filename before relying on Docker. |
| Compose depends on absent .env in this snapshot | Prepare environment configuration and remove the unused MinIO dependency or implement an actual storage adapter. |
| Static health and request logging only | Add dependency readiness, metrics and security audit events with sensitive values removed. |

The double where behavior is confirmed by the [Exposed 0.55 source](https://raw.githubusercontent.com/JetBrains/Exposed/0.55.0/exposed-core/src/main/kotlin/org/jetbrains/exposed/sql/Query.kt). These are implementation findings; a clean build, live API, database or deployed cloud environment was not exercised during this review. [B4, B14 to B18, B25 to B28]

## Recommended integration design

First secure and stabilize the existing API. Centralize tenant and role authorization, reject incomplete auth modes at startup, remove PIN verifier fields from responses, and implement transactional commands with database constraints. Then implement real private photo storage and verified FCM HTTP v1 delivery, using an outbox if delivery must survive request failures. [B1, B7, B9 to B12, B20, B22]

After those prerequisites, add an Android API repository and an explicitly chosen online identity flow. Treat the server as the authority for balances, XP, review status and random egg results. Introduce a versioned DTO contract independent of the local save shape. Salted SHA256 Android PIN hashes and BCrypt backend PIN hashes are incompatible; credentials require a deliberate reset or enrollment strategy rather than copying verifiers. [A3, B19]

Define a one-time import process for local households, ID remapping, photo upload, conflict policy, token storage, offline queued actions and retries before enabling synchronization. Preserve local data until an acknowledged import succeeds. Resolve household timezone, child session renewal, parent membership changes and photo retention as explicit product decisions. These are recommendations, not current functionality.

## Verification plan

The repository contains 21 Android domain tests and four instrumentation tests, with historical passing results dated 17 October 2026. Those results cover local game flows and persistence. There are no backend test source files in the reviewed snapshot. No tests were rerun for this documentation-only analysis; existing logs do not validate the separate backend. [A9, A10, A11]

Before integration, require a clean backend build and migration run against SQL Server; two-household authorization cases for every ID route; parallel completion, review, hatch and milestone requests; Monday rollover and delayed approvals; failure injection between writes; protected photo access and upload limits; invalid identity tokens; and notification delivery failure handling. Keep domain rule parity checks between the two Kotlin implementations.

## Evidence and companion artifacts

Evidence IDs resolve in source-index.md, with source hashes and line anchors in evidence.json. The ER document records the actual migration schema, and the threat model records abuse cases, risk scores and remediation acceptance criteria. The source review date is 19 October 2026. Runtime capacity, network topology, cloud controls, backup restoration and dependency advisories require separate validation.
'''

THREAT = r'''# Choreemon threat model

Reviewed 19 October 2026 | Audience Engineering and security reviewers

## Security assessment

The offline Android app has a small remote attack surface because it has no network integration. Its parent PIN is a local management gate on a shared device. The separate backend is not ready for untrusted network exposure. Its Azure verifier accepts unsigned tokens, several parent operations do not enforce household ownership, photo access is not tenant scoped, and business commands lack atomic transaction boundaries. These are source findings; no live compromise or deployed exposure is asserted. [A1, A5, B1, B9 to B12, B20]

Prioritize the conditional authentication bypass, default signing configuration, household authorization, PIN verifier disclosure and concurrent reward integrity before connecting clients. Production controls such as TLS termination, network restrictions, managed identity and monitoring are recommendations until their deployed configuration is verified.

## Scope and method

This model covers the Android source, Ktor routes, authentication and storage adapters, repositories, migrations, Dockerfile and Compose configuration. It uses STRIDE: spoofing, tampering, repudiation, information disclosure, denial of service and elevation of privilege. The unit of analysis is a data flow crossing a trust boundary. Local device access, authenticated children, parents from another household and external attackers are distinct threat actors.

Code paths and migration constraints were inspected directly. External references support security practices and version-specific behavior. This is a static assessment, not a penetration test, cloud configuration audit, legal compliance assessment or dependency advisory scan. Finding confidence indicates source evidence; exploitability may still depend on deployment mode, known target IDs or concurrent request timing.

## Assets and intended policy

| Asset | Protection requirement |
| --- | --- |
| Parent account and JWT signing material | Only the legitimate identity may act with parent authority; signing secrets remain server side |
| Household membership and invite code | Enrollment requires authorized household intent; unrelated households remain isolated |
| Member names and child activity history | Limit disclosure to authorized household participants and operational need |
| Photo proof | Private household object access, controlled collection, retention and deletion |
| PIN verifier and password hashes | Never disclose verifiers to clients; prevent cheap guessing and protect stored credentials |
| Wallet, XP, weekly score and milestone history | Rewards and spending remain consistent under retries and concurrency |
| SQL database and uploaded files | Integrity, bounded resource use, backups and tested restoration |
| Device messaging tokens and notification content | Bind recipients to authorized members and minimize personal data in logs and lock-screen messages |

Intended policy: a child acts on their own profile and companions, while a parent manages only their own household. Existing shared-device profile switching is intentional and provides no private separation among local children. Parent PIN verification is not an online login and its boolean response must not be treated as a token upgrade. [A1, B7, B11]

## Data flows and trust boundaries

![Trust boundaries](diagrams/trust-boundaries.png)

| Boundary | Crossing | Existing control and limitation |
| --- | --- | --- |
| TB1 Shared device to parent UI | A child attempts management operations | PIN dialog, five attempts then 30-second UI delay, relock on background; dialog state is not a persistent security service |
| TB2 Android process to local storage and media provider | Game JSON and selected image URI | Private app preferences, backup disabled, picker grant; JSON has no app-level integrity protection |
| TB3 API client to Ktor | Credentials, JWTs, JSON and multipart bodies | JWT wrapper, parent or owner checks on some routes; no application rate limits or deployment TLS configuration demonstrated |
| TB4 Ktor to SQL and filesystem | Reads, reward writes and photo bytes | SQL foreign keys and per-method transactions; tenant scoping and command atomicity are incomplete |
| TB5 Backend to identity and cloud adapters | Authentication or storage in Azure mode | Current adapters are stubs; verifier accepts none algorithm and storage does not upload bytes |
| TB6 Backend to FCM and logs | Tokens, names, quest descriptions and result data | Legacy sender, error token logging and development body logging; durable delivery and privacy filtering absent |

The Android and API client paths are separate. TB3 represents arbitrary or future API clients; it does not imply that the current app sends requests. SQL Server and filesystem operators are privileged actors. Tenant data read through API authorization remains untrusted even when the request contains a valid token. [A3, A5, A6, B1, B4, B19 to B23]

## Risk scoring

Likelihood and impact are scored from 1 to 5. Likelihood 1 requires unusual privileged conditions, 3 requires a practical prerequisite, and 5 needs little effort once the interface is reachable. Impact 1 affects a minor local action, 3 affects one member or availability, and 5 compromises household isolation or broad authentication. Score is likelihood multiplied by impact: 20 to 25 Critical, 12 to 19 High, 6 to 11 Medium and 1 to 5 Low. These are prioritization estimates rather than CVSS scores.

| ID | Finding | STRIDE | L x I | Priority |
| --- | --- | --- | --- | --- |
| T01 | Unsigned JWTs accepted in Azure mode | S E | 5 x 5 = 25 | Critical conditional |
| T02 | Default signing secret can remain active | S E | 4 x 5 = 20 | Critical conditional |
| T03 | Parent mutations omit household ownership | T E | 4 x 5 = 20 | Critical |
| T04 | PIN verifier and reusable invite code disclosure | I S | 4 x 4 = 16 | High |
| T05 | Photo reads lack owner metadata and tenant checks | I | 3 x 5 = 15 | High |
| T06 | Reward and purchase commands can race or partially commit | T R | 4 x 4 = 16 | High |
| T07 | Public enrollment and credential endpoints lack abuse controls | S D E | 4 x 4 = 16 | High |
| T08 | File handling lacks application validation and containment | T I D | 3 x 4 = 12 | High |
| T09 | Missing server validation permits invalid game data | T D | 4 x 3 = 12 | High |
| T10 | Development services and database authority may be exposed | I T D | 3 x 4 = 12 | High conditional |
| T11 | Notification audience and logging disclose family activity | I | 3 x 3 = 9 | Medium |
| T12 | Local PIN and save rely on device security | S T E | 3 x 3 = 9 | Medium local |
| T13 | Audit and session lifecycle leave abuse hard to contain | R S | 3 x 3 = 9 | Medium |
| T14 | Unbounded history and request work can exhaust service capacity | D | 3 x 3 = 9 | Medium |

All findings remain open in this documentation delivery. Conditional risks apply only when the specified configuration or deployment exposure occurs. High confidence means the relevant code pattern is present; it does not mean an exploit was run.

## Detailed threat register

### T01 Unsigned tokens in Azure mode

AzureB2CAuth.verifier uses JWT.require(Algorithm.none()) without issuer or audience restrictions. Application validates only that userId and householdId are present; parent routes trust isParent. An external actor reaching an ENV=azure instance can supply an unsigned token claiming known or chosen household identifiers and parent authority. Token generation in that adapter also returns an empty string, so normal enrollment is incomplete. Confidence is high; exposure is conditional on selecting Azure mode. [B1, B2, B20]

Remediation: fail startup for an unimplemented provider. Implement signature verification using the selected identity provider metadata and key rotation, require the expected issuer, API audience and expiry, and resolve internal membership and roles from trusted server state. A generic valid identity token must not be sufficient to select an arbitrary household. Acceptance: unsigned, wrong-key, wrong-issuer, wrong-audience, expired and missing-required-claim tokens return 401; a valid signed identity cannot assert another member's household or parent role.

### T02 Default signing configuration

AppConfig supplies a fixed development JWT secret and a seven-day token duration when configuration is absent. LocalJwtAuth uses HMAC256 and verifies issuer and token signature. If a reachable instance retains the known fallback, attackers can mint valid signed tokens with arbitrary internal claims. The default being in source does not establish that a real production secret has leaked. Confidence is high and deployment dependent. [B3, B19]

Remediation: require a strong externally supplied secret for nondevelopment environments, reject fallback values, rotate credentials and document affected token invalidation. Keep issuer and audience configuration explicit. Acceptance: startup fails with missing or sample signing values outside development; tokens from an old signing key stop authorizing according to a documented rotation procedure.

### T03 Cross household parent operations

PUT and DELETE /chores/{id} check isParent but update a chore by ID without comparing its household. POST /completions/{id}/review checks parent status and loads the completion by ID with no household predicate. A parent in household A who learns a household B object UUID can edit or archive B's quest or approve or reject B's pending completion. Assignment writes also accept member IDs from another household; foreign keys establish existence, not common tenancy. UUID unpredictability reduces discovery but is not authorization. Confidence is high. [B9, B10, B16, B17, V4, V5]

Remediation: require householdId in repository mutations and derive ownership through the authoritative chore or member relationship. Verify all assigned members belong to the same household. Return a consistent 403 or 404 for inaccessible IDs. Acceptance: with two households and valid parent tokens, every foreign object mutation fails and leaves chores, assignments, status, wallet and XP unchanged. The design follows [OWASP object authorization guidance](https://api-security.owasp.org/editions/2023/en/0xa1-broken-object-level-authorization/).

### T04 Household secrets in API responses

GET /household copies pinSalt and pinHash into GameState for any household JWT, including children. Register, login and join also serialize HouseholdRecord, which contains the verifier fields and invite code. BCrypt increases guessing cost, but the PIN has low entropy and the verifier enables offline attempts. The public join route allows the reusable invite code to create another child token. Confidence is high. [B6, B7, B14, B19]

Remediation: return dedicated household DTOs without verifiers, expose enrollment material only to authorized parents, rotate leaked invite codes, and use limited or expiring enrollment grants with parent confirmation. Acceptance: recursively inspect every auth and household response and find no PIN salt, PIN hash or password hash; child tokens cannot retrieve invitation credentials; reused or expired invitation grants fail.

### T05 Cross household photo access

All photo routes require a JWT through Application, so anonymous file reads are not established. However GET /photos/{filename} checks neither household nor owner, and uploaded files have no ownership row. Any authenticated member who learns a filename can request that object. An unguessable filename is not an access policy, and a photo URL can be disclosed in application data. Confidence is high. [B1, B12, B21]

Remediation: add photo metadata with household, uploader, content status, retention and object key. Authorize before streaming or issuing a short-lived private object URL. Require referenced proof to belong to the completion's household. Acceptance: same-household permitted reads succeed; anonymous and foreign-household reads fail; copying a proof ID or URL into another household's completion is rejected.

### T06 Nonatomic game commands

Completion insertion, points updates and XP updates are separate transactions. Review reads PENDING and later updates unconditionally, so simultaneous reviewers can both pass the status check and attempt rewards. Hatching checks a prior balance, creates a creature and later debits through a read-modify-write update that clamps balances to zero. Parallel requests can obtain more creatures than the wallet should fund. Milestone insertion and its 25-point update are separate; a failure after insertion can lose the reward permanently even though the composite primary key prevents a second identical key. Concurrent point and XP writes can also overwrite each other. Confidence is high for missing atomicity; exact race outcomes require runtime testing. [B8, B10, B11, B15, B17, B18, V5, V7]

Remediation: transact each command end to end. Use conditional status transitions, atomic counter increments, a conditional debit requiring enough balance, a unique business occurrence key or equivalent constraint, and idempotency keys for retries. Model rejection and one-time schedules explicitly so a uniqueness constraint does not block legitimate retries. Acceptance: parallel requests preserve wallet and XP arithmetic, approve once, charge every successful hatch, prevent duplicate eligible completions and grant each milestone exactly once; injected failures roll back all associated state.

### T07 Authentication and enrollment abuse

Register and login have no route-level rate limits. Register accepts any nonblank password; there is no verification or recovery flow. Public join uses a reusable six-character household code with no expiry, parent confirmation or household member cap. Backend PIN verification also has no attempt limit. A known or guessed code admits a new child profile with household read access. Brute-force cost is deployment dependent; no claim is made that the entire code space is cheap to exhaust. Child tokens have no implemented renewal route. Confidence is high. [B6, B7, B14]

Remediation: add quotas per IP, account and household; bound hashing cost through request limits; use expiring enrollment grants; require parent acceptance; define password policy, account recovery and child session lifecycle. Acceptance: sustained attempts are throttled, expired or replayed invites fail, parent confirmation is enforced and member limits hold under concurrency.

### T08 Upload validation and filesystem containment

PhotosRoutes accepts the client extension and MIME declaration and writes each file part. No application byte budget, image signature check, pixel limit, file-count rule or cleanup policy is configured. Ktor or an ingress may impose its own limits, which were not verified. LocalFileStorage resolves File(baseDir, fileName) without a canonical containment check. Traversal through encoded paths or malicious filename suffixes is a hypothesis requiring route and container validation; source alone does not prove an arbitrary-file-read exploit. Confidence is high for missing checks and medium for traversal exploitability. [B12, B21]

Remediation: bound bytes and file count, decode and reencode approved image formats with pixel limits, generate the entire object name server side, verify normalized containment, reject path separators and implement cleanup and per-household storage quotas. Follow the [OWASP upload guidance](https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html). Acceptance: oversized, deceptive and disallowed files fail before persistence, malformed or traversal names cannot escape uploads, and one household cannot consume unlimited storage.

### T09 Server validation gaps

Backend create and update routes do not apply the Android quest validator. Numeric point bounds, step counts and assigned household IDs are unchecked. Invalid point values can affect wallet arithmetic and may overflow counters; long fields can reach database errors. PIN update checks length 4 to 6 but does not require digits. A known parent's token can therefore create invalid state, while cross-household assignment compounds T03. Confidence is high. [A4, B7, B9, B16, V2 to V5]

Remediation: validate merged update state, constrain enums and lengths, validate related IDs and add database CHECK constraints for nonnegative counters and permitted stages and values. Acceptance: negative or excessive points, oversized inputs, empty or excessive steps and foreign assignments return a defined 4xx response and do not persist data.

### T10 Development infrastructure exposure

Compose publishes database, API and MinIO ports without a loopback-only binding. Configuration includes sample database and MinIO credentials; the default database user is sa. Docker starts a nonroot API process, which helps limit local privilege, but the SQL login is separate and highly privileged by default. Local JDBC encryption is disabled. No TLS ingress, firewall or secret-management configuration is present here. These risks apply if development configuration is exposed beyond a trusted host; actual network reachability was not tested. [B3, B4, B26, B27]

Remediation: bind development services to loopback where appropriate, isolate database and storage networks, use a least-privileged application login, separate migration authority, require production secrets and configure HTTPS and SQL encryption. Acceptance: external probes cannot reach internal services, the runtime SQL identity cannot create arbitrary databases or administer the server, and sample secrets are rejected in production.

### T11 Notification and logging privacy

The pending-review recipient method does not filter parents. It also calls where twice, which Exposed 0.55 rejects, so the current path can fail after the completion is already committed. Fixing only that query would expose review messages to all other household members. Development notification logs include titles and bodies containing member and quest details; failures include the full device token. FCM responses are not checked for delivery errors. Confidence is high. [B10, B15, B23]

Remediation: combine household, parent and exclusion predicates; minimize lock-screen content and logs; redact token values; handle delivery errors and invalid registrations. Use the current [FCM HTTP v1 protocol](https://firebase.google.com/docs/cloud-messaging/send/v1-api) or supported Admin SDK. Acceptance: only parent tokens receive review-needed messages, logs omit tokens and personal message bodies, and non-success sends trigger bounded retry or recorded failure.

### T12 Local save and PIN trust

Android uses a salted SHA256 verifier for a four-digit PIN in the same private JSON save as points and progress. Device sandboxing and backup disabling are useful protections, and the app applies an in-dialog five-attempt cooldown and background relock. Someone able to extract or modify app-private data can guess the PIN cheaply or change progress; a user may reset dialog-scoped cooldown state by reopening or restarting the flow. These are local risks, not remote account takeover. Confidence is high. [A1, A3, A5, A6]

Remediation: decide whether the shared-device gate is sufficient for the product. For stronger assurance, use a costed password KDF and device-protected key material, persist cooldown state with a suitable clock policy, and move online parent authority to authenticated server roles. Acceptance: local UI throttling survives intended lifecycle events; malformed saves fail safely; server authorization does not rely on a client PIN verifier or modified local JSON.

### T13 Audit and session lifecycle

JWTs default to seven days, and the server does not look up current membership or role on each authenticated call. No revocation, refresh or server logout implementation exists. Generic request logging does not supply an immutable review audit trail with actor, household, time, decision and previous state. Stolen tokens remain useful until expiry or key invalidation, and disputed reviews are difficult to attribute. Confidence is high for missing code; operational audit retention is unverified. [B1, B3, B6, B10, B19]

Remediation: define bounded sessions and revocation, validate current membership for sensitive commands, record security and review audit events, and alert on authorization failures and abuse patterns. Acceptance: revoked or removed identities lose access within the chosen bound and every review decision has a correlated actor and state transition without logging credentials.

### T14 Resource exhaustion

Household and completion reads load full history; completion submission reconstructs several household lists. Photo storage has no application quota and notification sends run sequentially within the request. Open registration can create records and invoke expensive BCrypt operations. A pool of 10 connections and unbounded history create predictable capacity pressure. Exact throughput is unknown. Confidence is high for the workload pattern. [B4, B6, B7, B10, B12, B23]

Remediation: bound requests, add pagination and retention, limit per-household objects, cap notification fanout and use asynchronous delivery when needed. Acceptance: load tests with maximum supported household history stay within documented latency and memory limits, and abusive clients cannot monopolize database or storage capacity.

## Existing protections and assessment limits

Local JWT verification uses an explicit HMAC algorithm and issuer. Password hashing uses BCrypt cost 12. Most data queries use the Exposed DSL rather than string concatenation. Foreign keys and composite keys protect basic references and duplicate milestone keys. Creature update, love and evolution routes explicitly check owner IDs. Android uses private storage, no network permission, a system photo picker, backup disabling and parent relock. The runtime container declares a nonroot user. [A1, A3, A5, B4, B11, B19, B26, V1 to V7]

These controls do not close the separate tenant, transaction and provider gaps. No direct server fetch of photoUrl was found, so SSRF from that field is not established. CORS anyHost broadens browser access but does not itself bypass bearer authentication. The database bootstrap interpolates a trusted configuration name, which is different from a demonstrated request-driven SQL injection. Dependencies were inventoried without assigning unverified CVEs. Azure storage and identity placeholders are not evidence that cloud services are configured securely or even deployed.

## Remediation sequence and ownership

Suggested ownership below is by role; no individual assignment is implied.

| Order | Suggested owner | Required outcome |
| --- | --- | --- |
| 1 Before any untrusted access | Backend and identity engineers | Close T01 to T04; fail incomplete auth modes and default secrets; authorize tenant-scoped mutations and remove verifiers |
| 2 Before mobile integration | Backend and data engineers | Close T05 to T09; transactional rewards, constrained data and private validated photos |
| 3 Before deployment review | Platform and backend engineers | Address T10, T11, T13 and T14; verify ingress, identities, observability, quotas and delivery |
| 4 Before stronger shared-device security claims | Android engineers and product owner | Resolve T12 and document the expected device trust model |

The double where query failure, unwired weekly rollover, Azure fake upload and Docker packaging mismatch also need functional fixes. A stable backend is necessary to run meaningful negative security tests. Each finding should be closed by its stated acceptance cases and code review; this document does not mark any finding remediated.

## Security validation scenarios

1. Create two households with separate parent and child test identities. Exercise every route with a missing token, child token, foreign parent token and authorized token.
2. Verify token algorithm, signature, issuer, audience and expiry rejection in every enabled environment; missing provider configuration must fail startup.
3. Inspect all response shapes for verifiers, invitation material and unrelated member data; test photo access using foreign household identities.
4. Submit parallel completion, approval, hatch, evolution and milestone commands. Inject failures after each SQL step and confirm atomic outcomes and retry behavior.
5. Test invitation replay, registration floods, credential attempts, PIN attempts and storage quotas using bounded local test fixtures.
6. Test upload byte, pixel, MIME, extension and traversal cases, including multiple file parts and interrupted streams.
7. Test Monday rollover, server and household timezone policy, delayed review and client clock changes with reproducible clocks.
8. Verify audit events and redaction, notification recipients, negative FCM responses and session revocation. Run restore exercises for database and photo storage.

## References and evidence

Evidence IDs resolve in source-index.md and evidence.json. The schema is documented separately in er-diagram.md. Sources consulted on 19 October 2026 include [OWASP API object authorization](https://api-security.owasp.org/editions/2023/en/0xa1-broken-object-level-authorization/), [OWASP file uploads](https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html), [Microsoft access token validation](https://learn.microsoft.com/en-us/entra/identity-platform/access-tokens#validate-tokens), [Firebase HTTP v1 sends](https://firebase.google.com/docs/cloud-messaging/send/v1-api) and the [Exposed 0.55 query implementation](https://raw.githubusercontent.com/JetBrains/Exposed/0.55.0/exposed-core/src/main/kotlin/org/jetbrains/exposed/sql/Query.kt). Recommendations based on these references are separate from the source-confirmed implementation findings.
'''

ER = '''# Choreemon entity relationship model

Reviewed 19 October 2026 | Physical schema from Flyway migrations V1 through V7

## Schema overview

The backend defines eight tables and ten foreign keys. This is the schema declared by the migration files, not a database introspection result. The Android app stores one JSON aggregate in SharedPreferences and does not create these SQL tables. Its entity lists map conceptually to this model, while assignments are embedded member ID lists and milestones are string keys in the local save. [A3, A4, B5, V1 to V7]

![Entity relationships with primary and foreign keys](diagrams/er-overview.png)

The diagram uses one at the referenced end and zero or many at the referencing end. Every declared foreign key is NOT NULL. A parent row can exist without children. Composite primary keys in ChoreAssignments and ClaimedMilestones identify a pair. The full diagram with every column is available as diagrams/er-full.svg, with editable Mermaid source in diagrams/er-diagram.mmd and Graphviz source in diagrams/er-full.dot.

## Relationships

| Parent | Child and foreign key | Cardinality |
| --- | --- | --- |
| Households.id | Members.household_id | One household to zero or many members |
| Households.id | Chores.household_id | One household to zero or many chores |
| Members.id | Creatures.owner_id | One member to zero or many creatures |
| Chores.id | ChoreAssignments.chore_id | One chore to zero or many assignments |
| Members.id | ChoreAssignments.member_id | One member to zero or many assignments |
| Chores.id | Completions.chore_id | One chore to zero or many completions |
| Members.id | Completions.member_id | One member to zero or many completions |
| Creatures.id | Completions.creature_id | One creature to zero or many completions |
| Members.id | DeviceTokens.member_id | One member to zero or many device tokens |
| Members.id | ClaimedMilestones.member_id | One member to zero or many milestone claims |

## Data dictionary

Types and defaults below are taken directly from migration SQL. PK means primary key; FK means foreign key; UK means unique key or index. Blank default means none is declared. All columns are NOT NULL except Members.auth_email and Members.auth_password_hash. Empty strings are stored values rather than SQL NULL.

'''

descriptions = {
    'Households': 'Tenant root, invite code and parent PIN verifier. week_start and created_at are strings. The API currently exposes verifier fields and must use a safer DTO. [B7, V1]',
    'Members': 'Household profile and counters. Email and password hash are optional, permitting child profiles without login credentials. points is spendable balance, not lifetime earned points. [B13, B15, V2]',
    'Creatures': 'Owned companions. Element, rarity and stage determine displayed species; XP determines level. is_active is persisted but not included as a field in the public Creature model. [B18, B24, V3]',
    'Chores': 'Household quests. steps contains a JSON array encoded as text. Archiving is a flag, preserving references and historical completion snapshots. [B16, V4]',
    'ChoreAssignments': 'Join table for chore-to-member assignments. Its composite key rejects duplicate pairs, but does not enforce matching household IDs between chore and member. [V4]',
    'Completions': 'Submission and reward snapshot. title, points and xp preserve values at submission. photo_url is an unstructured reference, with no Photos table or FK. [B10, B17, V5]',
    'DeviceTokens': 'Messaging registrations associated with members. The repository checks for a matching member and token before inserting, but no corresponding unique database constraint exists. [B15, V6]',
    'ClaimedMilestones': 'Award claim keys. The composite primary key prevents duplicate claims for the same member and key; it does not transact the wallet award with the claim. [B8, V7]',
}
for table in schema:
    ER += f"### {table['name']}\n\n{descriptions[table['name']]}\n\n| Column | SQL type | Key | Nullable | Default |\n| --- | --- | --- | --- | --- |\n"
    for col in table['columns']:
        keys = []
        if col['name'] in table['pk']:
            keys.append('PK')
        keys += [f"FK to {fk['parent']}.{fk['parent_column']}" for fk in table['fk'] if fk['column'] == col['name']]
        if table['name'] == 'Households' and col['name'] == 'invite_code':
            keys.append('UK')
        if table['name'] == 'Members' and col['name'] == 'auth_email':
            keys.append('UK when not null')
        ER += f"| {col['name']} | {col['type']} | {', '.join(keys)} | {'Yes' if col['nullable'] else 'No'} | {col['default']} |\n"
    ER += '\n'

ER += '''## Indexes and integrity rules

V1 declares unique invite_code. V2 indexes Members.household_id and creates a filtered unique index on nonnull auth_email. V3 indexes Creatures.owner_id. V4 indexes Chores.household_id. V5 indexes Completions.member_id and status. V6 indexes DeviceTokens.member_id. Each table also has its declared primary key. No cascading delete or update behavior is explicitly declared, so SQL Server uses its default referential actions. [V1 to V7]

The migration DDL is the physical type authority. Exposed maps text columns through varchar or text abstractions, while migrations declare NVARCHAR; the ORM declarations do not replace the migration schema. SQL constraints do not validate JSON steps, permitted enum strings, nonnegative points or XP, stage bounds, date formatting, a single active creature per owner, same-household assignments, or completion member-to-creature ownership. There is no unique quest occurrence constraint for nonrejected completions. [B5, V1 to V7]

Completion records do not store household_id directly; household filtering joins through Members. The three independent completion FKs do not ensure that chore, member and creature belong to the same household or owner. ChoreAssignments similarly lacks a composite tenant relationship. Those invariants require application checks or a redesigned schema with enforceable tenant keys. [B17, V4, V5]

## Mapping from Android state

| Android value | Backend representation |
| --- | --- |
| GameState.household and inviteCode | Households.name and invite_code; Android has no household ID field |
| GameState.members | Members rows; backend adds household, identity and parent role fields |
| Creature.owner | Creatures.owner_id |
| GameState.activeCreature | Creatures.is_active selected by owner and returned as one aggregate ID |
| Chore.assigned | ChoreAssignments rows |
| Chore.steps | JSON text in Chores.steps |
| Completion.photo | Completions.photo_url; local Android value is a content URI |
| GameState.claimedMilestones | ClaimedMilestones rows scoped to a member |
| Preferences and onboarded | No dedicated backend persistence |
| Mood, level, species, streak and rank | Derived values; no independent columns |

Android PIN hashes use salted SHA256 while backend hashes use BCrypt on PIN plus a salt. Copying the local hash will not produce a working backend verifier. Local content URIs also cannot become server proof URLs without uploading and authorizing the corresponding image. [A3, B19]

## Recommended schema changes

1. Add database constraints for valid numeric ranges and a filtered unique active-creature-per-owner rule, coordinated with transactional active selection.
2. Design quest occurrence identity for daily and one-time schedules, including rejected retries, and enforce it with atomic command processing.
3. Add photo metadata and private object keys with household and uploader FKs, content status, size and retention fields.
4. Add a reward or wallet ledger and idempotency records if durable reconciliation, historical weekly totals and safe retries are required.
5. Add audit and notification outbox tables if review attribution and reliable asynchronous delivery are required.
6. Consider native DATE and timestamp types, validated steps JSON, token uniqueness and tenant-aware relationship constraints in future migrations.

These recommendations are not present in V1 through V7 and are intentionally excluded from the physical ER diagram. Any schema change needs a new migration and tests against actual SQL Server behavior. Evidence IDs resolve in source-index.md and source hashes are recorded in evidence.json.
'''

for name, text in [('design-document.md', DESIGN), ('threat-model.md', THREAT), ('er-diagram.md', ER)]:
    # Make each evidence ID clickable without changing the prose.
    def refs(match):
        parts = match.group(1).split(', ')
        if all(re.fullmatch(r'[ABV]\d+', part) for part in parts):
            return ', '.join(f'[{part}](source-index.md#{part.lower()})' for part in parts)
        return match.group(0)
    text = re.sub(r'\[([^\[\]\n]+)\](?!\()', refs, text)
    (OUT / name).write_text(text.strip() + '\n', encoding='utf-8')

mermaid = ['erDiagram']
for table in schema:
    for fk in table['fk']:
        mermaid.append(f"    {fk['parent']} ||--o{{ {table['name']} : {fk['column']}")
for table in schema:
    mermaid.append(f"    {table['name']} {{")
    for col in table['columns']:
        keys = []
        if col['name'] in table['pk']:
            keys.append('PK')
        if any(fk['column'] == col['name'] for fk in table['fk']):
            keys.append('FK')
        if (table['name'], col['name']) in [('Households', 'invite_code'), ('Members', 'auth_email')]:
            keys.append('UK')
        comment = 'nullable' if col['nullable'] else 'required'
        if table['name'] == 'Members' and col['name'] == 'auth_email':
            comment += '; unique only when not null'
        dtype = col['type'].replace('(', '_').replace(')', '')
        mermaid.append(f"        {dtype} {col['name']} {','.join(keys)} \"{comment}\"")
    mermaid.append('    }')
(OUT / 'diagrams').mkdir(exist_ok=True)
(OUT / 'diagrams/er-diagram.mmd').write_text('\n'.join(mermaid) + '\n', encoding='utf-8')

(OUT / 'README.md').write_text('''# Choreemon codebase documentation

Generated from source reviewed on 19 October 2026. The Android app runs offline; the independent backend is not integrated with it.

- [Software design document](design-document.md) and [Word copy](design-document.docx)
- [Entity relationship document and data dictionary](er-diagram.md) and [Word copy](er-diagram.docx)
- [Full ER diagram](diagrams/er-full.svg), [key relationship overview](diagrams/er-overview.svg) and [editable Mermaid source](diagrams/er-diagram.mmd)
- [Threat model](threat-model.md) and [Word copy](threat-model.docx)
- [Source evidence index](source-index.md), [source hashes](evidence.json) and [extracted schema](schema.json)

The design distinguishes implemented components, placeholders and recommendations. The threat model contains 14 prioritized findings and validation cases. No application source was changed and no live deployment was exercised. Existing Android test results are historical and were not rerun.

To update: run build_documents.py with the bundled Python runtime, render_diagrams.cjs with the bundled Node runtime, then export_docx.py with the bundled Python runtime. render_diagrams.cjs expects NODE_PATH to point to the bundled node_modules. Word copies must be rendered and visually checked after regeneration. QA scripts and images are in the local .qa directory and are not deliverables.
''', encoding='utf-8')
print(json.dumps({'tables': len(schema), 'foreign_keys': sum(len(x['fk']) for x in schema), 'sources': len(evidence), 'markdown_documents': 3}))
