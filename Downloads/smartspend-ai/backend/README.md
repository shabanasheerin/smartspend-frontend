# SmartSpend AI — Backend

Java 21 / Spring Boot 3.3.2 REST API for the SmartSpend AI expense tracker.

## What's built

- All 11 JPA entities: User, Role, RefreshToken, Category, Income, Expense, Budget,
  SavingsGoal, Notification, Receipt, AuditLog (+ VerificationToken for email/password flows)
- All repositories (Spring Data JPA, with Specification support for filtering)

**Auth** — register, login, refresh token, logout, change password, email verification
(token emailed on registration, resend endpoint, verify endpoint), forgot/reset password
(emails a reset link, 1-hour expiry, resetting revokes all refresh tokens). JWT access +
refresh tokens, BCrypt hashing, stateless sessions.

**Income** — add/update/delete/get, search+filter (source, date range, amount range, notes
keyword), paginated & sorted.

**Expense** — add/update/delete/get, search+filter (category, date range, amount range, notes
keyword), paginated & sorted, recurring-expense support with automatic next-occurrence
calculation and a daily scheduler that fires reminder notifications and advances the cycle.

**Category** — list system-defined + user-defined categories.

**Receipts** — upload a receipt file (JPEG/PNG/WEBP/PDF, 5MB max) against an expense, list, and
delete. Files are stored on local disk under `app.file.upload-dir` and served at `/uploads/**`.

**Budget** — create/update/delete monthly per-category budgets; usage %, remaining amount, and
alert-threshold flag are computed live from actual expenses; creating/updating an expense
automatically checks the relevant budget and fires a BUDGET_EXCEEDED notification once the
threshold is crossed.

**Savings Goals** — create/update/delete goals, add contributions, auto-marks a goal ACHIEVED
and fires a notification when the target is reached, abandon a goal, progress % and a simple
estimated completion date.

**Notifications** — list (paginated), unread count, mark-as-read. Fed by budget alerts, goal
achievements, password changes, and recurring-expense reminders.

**Dashboard** — one summary endpoint returning current balance, monthly income/expenses, total
savings, this-month category distribution, and a 6-month income/expense trend.

**AI Insights (rule-based)** — highest spending category, month-over-month comparison,
projected end-of-month spend, daily average. Each rule is an isolated method so an LLM call can
be dropped in per-rule later without touching the rest of the service.

**Reports** — JSON transaction report for daily/weekly/monthly/yearly periods, plus **CSV,
Excel (.xlsx via Apache POI), and PDF (via PDFBox)** export.

**Admin panel** — list/block/unblock/delete users, platform-wide stats, audit log listing,
create/delete global categories. Protected by `hasRole('ADMIN')` at both the method and route
level.

**Cross-cutting** — Spring Security (stateless JWT, BCrypt, CORS, method security, role-based
route protection), global exception handler with a consistent JSON envelope, Swagger/OpenAPI
with a Bearer scheme, startup data seeding (roles + default expense categories), externalized
config via `application.yml`.

**Tests** — Mockito unit tests for AuthService, BudgetService, GoalService, ReportService, and
GlobalExceptionHandler (the logic most worth pinning down: duplicate-budget rejection, percentage
math, goal-achievement transitions, file-generation sanity checks), plus a `@SpringBootTest`
context-load smoke test against H2. This is deliberately not full coverage — no controller/
repository/integration tests yet.

**Docker** — multi-stage `Dockerfile` (Maven build → JRE Alpine runtime), wired into the root
`docker-compose.yml` alongside MySQL and the frontend.

**CI** — `.github/workflows/ci.yml`: backend `mvn verify`, frontend `npm run build`, then a
Docker image build for both.

**Postman** — `postman/SmartSpend-AI.postman_collection.json`, covering every endpoint with a
token-capturing script on register/login.

## Not yet built

- Full test coverage (controllers, repositories, real integration tests)
- Refresh-token rotation / reuse detection (current implementation is single-token replace on
  refresh, not the more paranoid rotate-and-invalidate pattern)
- Rate limiting on auth endpoints
- A dedicated file-preview/thumbnail pipeline for receipts (raw file serving only)

## Prerequisites

- JDK 21, Maven 3.9+, MySQL 8+ (or just use `docker compose up` from the repo root)

## Setup (local, no Docker)

1. Create the database (or let it auto-create):
   ```sql
   CREATE DATABASE smartspend_db;
   ```

2. Set environment variables (or edit `application.yml` directly):
   ```
   DB_USERNAME=root
   DB_PASSWORD=your_mysql_password
   JWT_SECRET=<base64 string, 256-bit minimum>
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ```

3. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. API live at `http://localhost:8080` — Swagger UI at `http://localhost:8080/swagger-ui.html`

## Run tests

```bash
mvn test
```

## Try it

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Shab","email":"shab@example.com","password":"SecurePass123"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"shab@example.com","password":"SecurePass123"}'

# Use the returned accessToken as: Authorization: Bearer <token>

# List categories (to get a categoryId for expenses)
curl http://localhost:8080/api/v1/categories -H "Authorization: Bearer <token>"

# Add an expense
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"categoryId":1,"amount":450.00,"expenseDate":"2026-07-01","notes":"Groceries"}'

# Create a budget for that category
curl -X POST http://localhost:8080/api/v1/budgets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"categoryId":1,"limitAmount":5000,"month":7,"year":2026}'

# Dashboard summary
curl http://localhost:8080/api/v1/dashboard/summary -H "Authorization: Bearer <token>"

# AI insights
curl http://localhost:8080/api/v1/insights -H "Authorization: Bearer <token>"

# Export reports
curl "http://localhost:8080/api/v1/reports/export/csv?period=MONTHLY"   -H "Authorization: Bearer <token>" -o report.csv
curl "http://localhost:8080/api/v1/reports/export/excel?period=MONTHLY" -H "Authorization: Bearer <token>" -o report.xlsx
curl "http://localhost:8080/api/v1/reports/export/pdf?period=MONTHLY"   -H "Authorization: Bearer <token>" -o report.pdf

# Forgot password
curl -X POST http://localhost:8080/api/v1/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"shab@example.com"}'

# Upload a receipt for an expense
curl -X POST http://localhost:8080/api/v1/expenses/1/receipts \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/receipt.jpg"
```

## A note on receipt access control

Uploaded files are served from `/uploads/**`, which is `permitAll` in Spring Security (needed
for browsers to load `<img>` tags directly). URLs contain a random UUID, so they aren't
guessable, but they also aren't ownership-checked at request time. Fine for a portfolio/demo
project; flag it if you want a signed-URL or authenticated-download version instead.

## Note on verification

This code was hand-reviewed carefully (entity relationships, DTO field order, Lombok builders,
Spring Data derived queries, MapStruct mappings, JWT/Security wiring, scheduler logic, PDF/Excel
byte-stream generation) but **not compiled in a live Maven environment** — this sandbox has no
access to Maven Central. Run `mvn clean install` locally as your first step; flag anything that
doesn't compile and it'll get fixed immediately.
