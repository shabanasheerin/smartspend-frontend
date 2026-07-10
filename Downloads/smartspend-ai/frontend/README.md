# SmartSpend AI — Frontend

React + Vite + Tailwind CSS SPA for the SmartSpend AI expense tracker.

## Stack

- React 18, React Router v6
- Redux Toolkit (auth + UI state)
- Axios (with automatic JWT refresh on 401)
- Tailwind CSS (dark mode via class strategy)
- Chart.js / react-chartjs-2 (dashboard charts)
- React Hook Form (all forms)
- Framer Motion (modal transitions)
- react-hot-toast (notifications)
- lucide-react (icons)

## What's built

- Auth pages: Login, Register, Forgot Password, Reset Password, Verify Email
- Protected app shell: collapsible Sidebar, Navbar (theme toggle, unread notification badge, logout)
- Dashboard: stat cards, income/expense trend line chart, category distribution doughnut chart, AI insight cards
- Income: list, add/edit modal, delete
- Expenses: list, add/edit modal (with recurring toggle), delete, receipt upload
- Budgets: current-month cards with progress bars and alert coloring, create/delete
- Goals: progress cards, create goal, contribute funds, delete
- Notifications: list with read/unread state, mark-as-read
- Reports: run a report by period, view as a table, export CSV/Excel/PDF
- Admin panel: user list, block/unblock/delete, platform stats
- Dark mode (persisted), responsive layout (mobile sidebar drawer)

## Not yet built

- Redux slices for income/expense/etc (currently local component state + direct API calls,
  which is simpler for this size of app — flag it if you specifically want RTK Query/slices
  for every feature)
- Toasts for silent background failures (e.g. the unread-count fetch fails silently)
- E2E / component tests
- Profile page (photo upload, currency/timezone/theme preference — backend fields exist,
  no dedicated screen yet)

## Setup

```bash
npm install
npm run dev
```

The dev server runs at `http://localhost:5173` and proxies `/api` and `/uploads` to
`http://localhost:8080` (see `vite.config.js`) — so run the backend first.

## Build

```bash
npm run build
```

Outputs to `dist/`. The Docker image serves this via Nginx (see `Dockerfile` / `nginx.conf`),
which also reverse-proxies `/api`, `/uploads`, and `/swagger-ui` to the `backend` container.

## Environment

No `.env` is required for local dev (the Vite proxy handles it). For a non-Docker production
build talking to a different backend host, set `VITE_API_BASE_URL` and update `apiClient.js`
accordingly — it's currently hardcoded to the relative `/api/v1` path, which works with both
the dev proxy and the Nginx reverse proxy.
