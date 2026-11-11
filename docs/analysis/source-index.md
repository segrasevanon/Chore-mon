# Choreemon source index

Reviewed on 19 October 2026. Paths are relative to the ProjectCII workspace. Evidence IDs used in the documents resolve below. SHA256 hashes and exact symbol locations are in evidence.json. The Android checkout reports commit fc5e250a0eb17c9fb585d2f8687a3061ff0238b4; the backend directory has no Git metadata.

## A1

[Chore-mon/app/src/main/java/com/choreemon/app/MainActivity.kt](../../Chore-mon/app/src/main/java/com/choreemon/app/MainActivity.kt)

- fun ChoreemonApp: lines 52
- Lifecycle.Event.ON_STOP: lines 84
- ParentPinDialog: lines 167

## A2

[Chore-mon/app/src/main/java/com/choreemon/app/data/GameViewModel.kt](../../Chore-mon/app/src/main/java/com/choreemon/app/data/GameViewModel.kt)

- fun update: lines 15
- fun createHousehold: lines 53
- fun addMember: lines 46

## A3

[Chore-mon/app/src/main/java/com/choreemon/app/data/GameRepository.kt](../../Chore-mon/app/src/main/java/com/choreemon/app/data/GameRepository.kt)

- object SaveCodec: lines 15
- object ParentPin: lines 50
- class GameRepository: lines 56

## A4

[Chore-mon/app/src/main/java/com/choreemon/app/data/GameModels.kt](../../Chore-mon/app/src/main/java/com/choreemon/app/data/GameModels.kt)

- data class GameState: lines 49
- object GameRules: lines 66
- fun claimMilestone: lines 151

## A5

[Chore-mon/app/src/main/AndroidManifest.xml](../../Chore-mon/app/src/main/AndroidManifest.xml)

- android:allowBackup: lines 4
- android:exported: lines 9

## A6

[Chore-mon/app/src/main/java/com/choreemon/app/ui/AdminScreen.kt](../../Chore-mon/app/src/main/java/com/choreemon/app/ui/AdminScreen.kt)

- fun ParentPinDialog: lines 147
- attempts >= 5: lines 162

## A7

[Chore-mon/app/src/main/java/com/choreemon/app/ui/QuestDetail.kt](../../Chore-mon/app/src/main/java/com/choreemon/app/ui/QuestDetail.kt)

- fun PhotoProof: lines 30
- takePersistableUriPermission: lines 48

## A8

[Chore-mon/app/build.gradle.kts](../../Chore-mon/app/build.gradle.kts)

- minSdk: lines 12
- dependencies: lines 29

## A9

[Chore-mon/app/src/test/java/com/choreemon/app/data/GameRulesTest.kt](../../Chore-mon/app/src/test/java/com/choreemon/app/data/GameRulesTest.kt)

- class GameRulesTest: lines 8

## A10

[Chore-mon/app/src/androidTest/java/com/choreemon/app/GameFlowTest.kt](../../Chore-mon/app/src/androidTest/java/com/choreemon/app/GameFlowTest.kt)

- class GameFlowTest: lines 20

## A11

[Chore-mon/verification/QA.md](../../Chore-mon/verification/QA.md)

- 21 tests passed: lines 15
- 4 instrumented tests: lines 21

## B1

[choreemon-backend/src/main/kotlin/com/choreemon/backend/Application.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/Application.kt)

- fun Application.module: lines 22
- anyHost(): lines 37
- validate {: lines 69
- authenticate("jwt"): lines 89

## B2

[choreemon-backend/src/main/kotlin/com/choreemon/backend/config/ServiceRegistry.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/config/ServiceRegistry.kt)

- fun create: lines 32
- if (config.isAzure): lines 36
- FcmDirectService: lines 9, 47

## B3

[choreemon-backend/src/main/kotlin/com/choreemon/backend/config/AppConfig.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/config/AppConfig.kt)

- fun load: lines 14
- expiryHours: lines 26, 52

## B4

[choreemon-backend/src/main/kotlin/com/choreemon/backend/db/DatabaseFactory.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/db/DatabaseFactory.kt)

- fun init: lines 13
- maximumPoolSize: lines 36
- suspend fun <T> dbQuery: lines 61

## B5

[choreemon-backend/src/main/kotlin/com/choreemon/backend/db/tables/Tables.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/db/tables/Tables.kt)

- object Households: lines 5
- object Completions: lines 64

## B6

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/AuthRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/AuthRoutes.kt)

- post("/register"): lines 41
- post("/login"): lines 99
- post("/join"): lines 128
- post("/device-token"): lines 170

## B7

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/HouseholdsRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/HouseholdsRoutes.kt)

- pinSalt = household.pinSalt: lines 43
- post("/verify-pin"): lines 51
- put("/pin"): lines 65

## B8

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/MembersRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/MembersRoutes.kt)

- fun Route.membersRoutes: lines 23
- post("/milestone"): lines 79

## B9

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/ChoresRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/ChoresRoutes.kt)

- post {: lines 45
- put("/{id}"): lines 81
- delete("/{id}"): lines 107

## B10

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/CompletionsRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/CompletionsRoutes.kt)

- post {: lines 34
- post("/{id}/review"): lines 97
- updateStatus: lines 114

## B11

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/CreaturesRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/CreaturesRoutes.kt)

- post("/hatch"): lines 32
- post("/{id}/evolve"): lines 65
- patch("/{id}"): lines 101

## B12

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/PhotosRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/PhotosRoutes.kt)

- post("/upload"): lines 14
- get("/{filename}"): lines 37

## B13

[choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/LeaderboardRoutes.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/routes/LeaderboardRoutes.kt)

- weeklyPoints: lines 15, 27, 34
- totalPoints = m.points: lines 35

## B14

[choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/HouseholdRepository.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/HouseholdRepository.kt)

- fun createHousehold: lines 34
- fun updateWeekStart: lines 71
- fun generateInviteCode: lines 84

## B15

[choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/MemberRepository.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/MemberRepository.kt)

- fun updatePoints: lines 87
- fun resetWeeklyPoints: lines 95
- fun getDeviceTokensForHousehold: lines 138

## B16

[choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/ChoreRepository.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/ChoreRepository.kt)

- fun findByHousehold: lines 91
- val filtered: lines 93
- fun updateChore: lines 124

## B17

[choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/CompletionRepository.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/CompletionRepository.kt)

- fun createCompletion: lines 24
- fun updateStatus: lines 60

## B18

[choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/CreatureRepository.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/repositories/CreatureRepository.kt)

- fun addXp: lines 70
- fun evolve: lines 77
- fun setActive: lines 99

## B19

[choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/LocalJwtAuth.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/LocalJwtAuth.kt)

- Algorithm.HMAC256: lines 12
- fun verifier: lines 14
- fun generateToken: lines 19

## B20

[choreemon-backend/src/main/kotlin/com/choreemon/backend/services/azure/AzureB2CAuth.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/services/azure/AzureB2CAuth.kt)

- Algorithm.none(): lines 15
- return "": lines 20

## B21

[choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/LocalFileStorage.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/LocalFileStorage.kt)

- override suspend fun upload: lines 18
- override suspend fun getFile: lines 25

## B22

[choreemon-backend/src/main/kotlin/com/choreemon/backend/services/azure/AzureBlobStorage.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/services/azure/AzureBlobStorage.kt)

- override suspend fun upload: lines 10
- return null: lines 16

## B23

[choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/FcmDirectService.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/services/local/FcmDirectService.kt)

- if (serverKey.isNullOrBlank()): lines 28
- httpClient.post: lines 43
- logger.error: lines 49

## B24

[choreemon-backend/src/main/kotlin/com/choreemon/backend/domain/GameModels.kt](../../choreemon-backend/src/main/kotlin/com/choreemon/backend/domain/GameModels.kt)

- data class GameState: lines 53
- object GameRules: lines 71

## B25

[choreemon-backend/build.gradle.kts](../../choreemon-backend/build.gradle.kts)

- version =: lines 7, 18, 19, 20, 21
- val exposed_version: lines 19
- archiveClassifier: lines 83

## B26

[choreemon-backend/Dockerfile](../../choreemon-backend/Dockerfile)

- FROM: lines 2, 8
- COPY --from: lines 11
- USER choreemon: lines 13

## B27

[choreemon-backend/docker-compose.yml](../../choreemon-backend/docker-compose.yml)

- services:: lines 3
- env_file:: lines 44
- volumes:: lines 15, 36, 49, 58

## B28

[choreemon-backend/gradle/wrapper/gradle-wrapper.properties](../../choreemon-backend/gradle/wrapper/gradle-wrapper.properties)

- distributionUrl: lines 3

## V1

[choreemon-backend/src/main/resources/db/migration/V1__create_households.sql](../../choreemon-backend/src/main/resources/db/migration/V1__create_households.sql)

- complete migration: lines 1, 10

## V2

[choreemon-backend/src/main/resources/db/migration/V2__create_members.sql](../../choreemon-backend/src/main/resources/db/migration/V2__create_members.sql)

- complete migration: lines 1, 14

## V3

[choreemon-backend/src/main/resources/db/migration/V3__create_creatures.sql](../../choreemon-backend/src/main/resources/db/migration/V3__create_creatures.sql)

- complete migration: lines 1, 15

## V4

[choreemon-backend/src/main/resources/db/migration/V4__create_chores.sql](../../choreemon-backend/src/main/resources/db/migration/V4__create_chores.sql)

- complete migration: lines 1, 22

## V5

[choreemon-backend/src/main/resources/db/migration/V5__create_completions.sql](../../choreemon-backend/src/main/resources/db/migration/V5__create_completions.sql)

- complete migration: lines 1, 17

## V6

[choreemon-backend/src/main/resources/db/migration/V6__create_device_tokens.sql](../../choreemon-backend/src/main/resources/db/migration/V6__create_device_tokens.sql)

- complete migration: lines 1, 9

## V7

[choreemon-backend/src/main/resources/db/migration/V7__create_claimed_milestones.sql](../../choreemon-backend/src/main/resources/db/migration/V7__create_claimed_milestones.sql)

- complete migration: lines 1, 7

