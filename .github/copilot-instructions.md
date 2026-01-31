# Copilot / AI Agent Instructions — digitalSansadProject

Purpose: concise, actionable notes to get an AI coding agent productive in this mono-repo.

- Big Picture
  - This repo is a multi-service system: three Java Spring Boot services (`auth-service`, `sow-service`, `translation-service`), a Next.js frontend (`frontend/`), and a `database/` folder with DB scripts/migrations.
  - Java services use Maven with bundled wrappers (`mvnw`, `mvnw.cmd`) and Spring Boot. Database migrations are managed by Flyway SQL files under `src/main/resources/db/migration` (compiled copies may appear under `target/classes/db/migration`).
  - Frontend is a Next.js app (app-router) in `frontend/` — client-side API calls centralised in `frontend/src/utils/api.tsx` and high-level services in `frontend/src/services/`.

- Where to look first (quick map)
  - `auth-service/src/main/java/com/digitalSansad/` — authentication, security filters (e.g. `security/JWTAuthenticationFilter.java`), controllers and services.
  - `*/src/main/resources/application.yaml` or `application.properties` — per-service config (ports, datasources, base-URLs). Use these files to discover default ports and external endpoints.
  - `frontend/src/app/` — Next.js routes (login, signup, verify-otp, admin, home).
  - `frontend/src/utils/api.tsx` and `frontend/src/services/*` — where the frontend maps to backend endpoints.
  - `target/classes/db/migration` — compiled Flyway SQLs are useful when migrations differ from `src` (keep both in sync).

- Build / run / debug commands
  - Java (Windows):
    - Build: `cd auth-service && mvnw.cmd -DskipTests package` (or run in each service directory). For Unix use `./mvnw`.
    - Run jar: `java -jar target/<artifact>.jar` or `mvnw.cmd spring-boot:run`.
  - Frontend:
    - Install & dev: `cd frontend && npm install && npm run dev` (package.json at `frontend/package.json`).
  - Tests: `cd <service> && mvnw.cmd test` (or `./mvnw test`).

- Project-specific conventions & patterns (do not assume defaults)
  - Maven wrapper present — prefer using `mvnw`/`mvnw.cmd` instead of system `mvn` for reproducible builds.
  - Flyway migrations live under `src/main/resources/db/migration`. New schema changes require adding a `V{n}__desc.sql` file.
  - Package root is `com.digitalSansad` — keep new classes in appropriate subpackages (e.g., `auth`, `security`, `controller`, `service`, `repository`).
  - Security filters and JWT handling live under `auth-service/src/main/java/com/digitalSansad/auth/security`.
  - Frontend uses the app-router (`frontend/src/app/*`) and React server components — prefer editing `page.tsx` files and `components/` rather than older `pages/` style.

- Integration points & common edit patterns
  - To add a backend REST endpoint: create controller under `auth-service/src/main/java/com/digitalSansad/.../controller`, update service layer, and if it needs DB change add a Flyway migration in the `db/migration` folder.
  - After changing migrations: run `mvnw.cmd -DskipTests package` and restart service; Flyway runs on startup using the configured datasource.
  - To wire frontend to a new endpoint: update `frontend/src/utils/api.tsx` (base URL) and add convenience wrappers in `frontend/src/services/*`.

- Notable quirks discovered
  - Tests tree shows `digigtalSansad` typo under `test/` packages — tests or refactors might use a slightly different package path; be careful when moving test classes.
  - Compiled resources under `target/` sometimes expose the current DB migrations — use them to compare what was packaged versus `src`.

- What an AI agent should do first when modifying code
  1. Inspect `application.yaml` in the target service to confirm ports & datasource values before launching locally.
 2. If changing DB schema, add a Flyway `V` migration and update any repository/entity code; run a local build to ensure migrations apply.
 3. Update frontend client wrappers in `frontend/src/services/*` and `frontend/src/utils/api.tsx` when changing REST surfaces.

- Example snippets (where to change)
  - Add controller: `auth-service/src/main/java/com/digitalSansad/auth/controller/MyController.java`
  - Add migration: `auth-service/src/main/resources/db/migration/V6__add_new_table.sql`
  - Frontend API wrapper: `frontend/src/utils/api.tsx` and `frontend/src/services/authService.tsx`

If anything here is unclear or you want more detail (example run scripts, docker-compose recommendations, or VS Code launch configs), tell me which part to expand. Feedback welcome. 
