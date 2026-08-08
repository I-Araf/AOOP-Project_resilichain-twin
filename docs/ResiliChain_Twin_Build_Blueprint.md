# ResiliChain Twin — End-to-End Build Blueprint

*A page-by-page, button-by-button, tool-by-tool construction plan built from your project proposal and pitch deck.*

---

## How to Read This Document

The project is broken into **13 build stages** (Stage 0 → Stage 12), roughly matching your official 4‑week plan but split into smaller, demo-able vertical slices — build one thin slice end-to-end (backend + UI) before moving to the next, so you always have something running for weekly check-ins.

For every stage you get:
- **Maps to** — which week of your official plan it belongs to
- **Goal** — what "done" looks like
- **Tools needed** — exact libraries/services, flagged **[FREE]** where relevant since you're keeping everything except Claude Code free
- **Backend work** — entities, services, endpoints
- **Pages built in this stage** — full spec: purpose, every button/element on the page, and exactly which page each button opens
- **Definition of done**

Every page has a permanent ID (**P1, P2, …**) so buttons can reference their destination unambiguously. Section 5 gives you a flat table of all pages, Section 6 a flat table of all tools, Section 7 the OOP/pattern map, Section 8 the folder structure.

---

## 1. Build Philosophy

1. **One scenario first, breadth later.** Your proposal's own FAQ says it best: freeze on the Port Chattogram, 36-hour closure, 12-shipment, 3-warehouse scenario. Build the *entire* pipeline (map → disruption → risk → AI brief → recovery plan → approval → replay) for that one scenario before adding more disruption types.
2. **Vertical slices, not horizontal layers.** Don't build "all backend" then "all frontend." Each stage below delivers a working feature end-to-end (DB → API → UI) so you can demo progress every week.
3. **Human-in-the-loop is non-negotiable in the UI.** Every AI-touched page (Incident Brief, Recovery Plan) must visually separate "what the AI suggests" from "what a human approved" — this is a judging criterion in your own deck.
4. **Reuse one shell, one design system.** Build the app shell (top bar + sidebar + theming) once in Stage 1; every later page drops into it.

---

## 2. Design System — Build This Before Any Real Page

Your requirement is "professional, aesthetic, futuristic." For a supply-chain **control room** product, that means a dark-first "mission control" aesthetic — not a generic light SaaS dashboard.

### 2.1 Color Palette (dark-first)
| Token | Hex | Use |
|---|---|---|
| `--bg-primary` | `#0B1120` | App background (deep navy-black) |
| `--bg-surface` | `#111827` | Cards, panels |
| `--bg-surface-glass` | `rgba(17,24,39,0.6)` + backdrop-blur | Overlays, modals, floating panels |
| `--border-subtle` | `#1F2937` | Card borders |
| `--accent-primary` | `#22D3EE` (cyan) | Live/active states, primary buttons, map route glow |
| `--accent-secondary` | `#6366F1` (indigo) | Secondary actions, AI-related UI |
| `--status-healthy` | `#10B981` (emerald) | Delivered, resolved, low risk |
| `--status-warning` | `#F59E0B` (amber) | Delayed, medium risk |
| `--status-critical` | `#EF4444` (red) | Disrupted, high risk, port closed |
| `--text-primary` | `#F1F5F9` | Headings |
| `--text-secondary` | `#94A3B8` | Body/meta text |

### 2.2 Typography
- **Headings/UI:** `Space Grotesk` or `Inter` — geometric, technical feel. **[FREE — Google Fonts]**
- **Data/numbers/IDs/risk scores:** `JetBrains Mono` or `IBM Plex Mono` — gives the "control room" readout feel. **[FREE]**

### 2.3 Visual language that reads as "futuristic" without gimmicks
- **Glassmorphism panels**: translucent cards with `backdrop-filter: blur(12px)`, 1px subtle border, soft cyan glow on hover/active (not on every element — reserve glow for live/critical states).
- **Animated route lines**: on the map, healthy routes are thin static lines; a disrupted route becomes a pulsing red dashed line (CSS animation or Framer Motion).
- **Animated counters**: KPI numbers (exposed orders, risk score) count up/down on change instead of snapping — small but reads as "alive."
- **Consistent status badges**: pill-shaped, color per §2.1, used identically on every page (map markers, tables, cards) so a judge can read state at a glance anywhere in the app.
- **Dark basemap** for Leaflet (e.g. CartoDB "Dark Matter" tiles — **[FREE]**, no API key needed for light demo traffic) so the map matches the app instead of looking like a bolted-on Google Maps widget.

### 2.4 Component library / tools
| Tool | Purpose | Cost |
|---|---|---|
| Tailwind CSS | Utility styling, enforces the design tokens above | **FREE** |
| shadcn/ui | Accessible, professional-looking base components (dialogs, tabs, tables, dropdowns) you skin with your tokens | **FREE** |
| Lucide-react | Icon set — clean, technical line icons | **FREE** |
| Framer Motion | Page transitions, pulsing/glow micro-interactions | **FREE** |
| Recharts | KPI charts, risk trend lines | **FREE** |
| React Flow | Dependency graph view | **FREE** |
| Leaflet.js + react-leaflet | Geographic map | **FREE** |

> **Note on your "free tools only" rule:** everything above and every tool named later in this document is free/open-source. The one external paid-adjacent service is **Gemini API**, which has a free tier generous enough for a course demo (rate-limited, not unlimited) — stay within it by caching AI responses per disruption scenario instead of re-calling on every page refresh.

---

## 3. Global App Shell (built once, used everywhere)

Every page after login lives inside one shell:

- **Top Bar** (fixed): Logo/product name · Global search (nodes/shipments) · Live connection status dot (green=connected to WebSocket, red=disconnected) · Notification bell (dropdown of recent alerts) · AI status chip ("Gemini: Active/Fallback") · Profile menu (Profile · Settings · Logout)
- **Left Sidebar** (collapsible to icon-only): role-aware nav items (see §5 for which pages each role sees)
- **Main content area**: the page itself, always inside the design system's panel style

This shell is not counted as its own "page" — it wraps every page P2 onward.

---

## 4. Stage-by-Stage Build Plan

### Stage 0 — Environment & Repo Setup
**Maps to:** Pre-Week 1
**Goal:** Empty-but-running full stack, one command to boot everything.

**Tools needed:**
- Java 17+, Spring Boot 3.x, Maven — **[FREE]**
- Node.js 20+, React 18, TypeScript, Vite — **[FREE]**
- PostgreSQL, Redis — run via Docker locally, **[FREE]**
- Docker + Docker Compose — **[FREE]**
- Git + GitHub (public repo = free GitHub Actions minutes) — **[FREE]**
- Postman (for manual API checks as you go) — **[FREE]**

**Backend work:** Spring Boot skeleton (`resilichain-api`) with health-check endpoint; `docker-compose.yml` running Postgres + Redis; Flyway or Spring's `ddl-auto` for schema bootstrap.
**Frontend work:** Vite + React + TS skeleton (`resilichain-web`) with Tailwind + shadcn/ui installed and your design tokens (§2.1–2.2) wired into `tailwind.config`.
**Pages built:** none yet.
**Definition of done:** `docker compose up` starts DB/cache; `mvn spring-boot:run` serves `/health`; `npm run dev` shows a blank themed shell with your color palette applied.

---

### Stage 1 — Design System, App Shell, Login
**Maps to:** Week 1
**Goal:** Anyone can log in and land on a themed (empty) dashboard.

**Tools needed:** Spring Security + JWT, BCrypt, React Router, shadcn/ui, Framer Motion.

**Backend work:** `User` entity (id, name, email, passwordHash, role enum: PLANNER/OPERATOR/MANAGER/ADMIN), `/auth/login` endpoint returning JWT, `/auth/me` for session check.

**Pages built:**

**P1 — Login Page**
- Purpose: authenticate and route to role-appropriate home.
- Layout: centered glass card over a subtle animated dark background (faint moving grid/route-line pattern for the "futuristic" feel), product name + tagline from your deck ("Detect risk in real time...").
- Buttons/elements: `Email` field, `Password` field, **[Sign In]** button, **[Forgot password?]** link (optional secondary page, low priority), error toast on bad credentials.
- Navigation: **[Sign In]** (success) → **P2 Dashboard Home** (role-aware redirect). **[Forgot password?]** → optional P1b (build last, or skip for MVP and just note "contact admin").

**P2 — Dashboard Home (shell/router stub)**
- Purpose: on login, silently route each role to their real home page; built here as a stub, fleshed out per role in later stages.
- Buttons/elements: none yet (just a loading state → redirect).
- Navigation: PLANNER → **P14 Planner Dashboard** (Stage 8) · OPERATOR → **P7 Live Shipment Tracking** (Stage 5) · MANAGER → **P17 Executive Dashboard** (Stage 10) · ADMIN → **P3 User Management** (Stage 2).

**Definition of done:** login works for a seeded admin user; shell (top bar + sidebar) renders with correct nav items per role.

---

### Stage 2 — Auth Administration (Users & Roles)
**Maps to:** Week 1
**Goal:** Admin can create accounts and assign roles instead of you seeding the DB by hand.

**Tools needed:** Spring Security method-level `@PreAuthorize`, Spring Data JPA.

**Backend work:** `/admin/users` CRUD endpoints, role-based route guards on every existing and future endpoint.

**Pages built:**

**P3 — Admin: User Management**
- Purpose: create users, assign one of the four roles, suspend accounts.
- Buttons/elements: table of users (name, email, role badge, status), **[+ New User]** button, per-row **[Edit]**, per-row **[Suspend/Reactivate]** toggle, search bar, role filter dropdown.
- Navigation: **[+ New User]** → opens **Create User modal** (in-page, not a new route) with Name/Email/Role fields and **[Create]**/**[Cancel]**. **[Edit]** → **Edit User modal**, same fields pre-filled.

**Definition of done:** admin can create a planner/operator/manager account from the UI and log in as each to see role-scoped navigation.

---

### Stage 3 — Network Master Data (the "world" the twin represents)
**Maps to:** Week 1
**Goal:** Admin can define the supply-chain network — this data feeds every later feature.

**Tools needed:** Spring Data JPA, PostgreSQL, Bean Validation (`@Valid`).

**Backend work:** Entities `NetworkNode` (abstract/base — see §7), `Supplier`, `Factory`, `Warehouse`, `Port` (all extend `NetworkNode`), `Route` (links two nodes, has cost/leadTime/capacity). CRUD endpoints for each.

**Pages built:**

**P4 — Admin: Network Master Data**
- Purpose: CRUD the physical network — nodes and routes.
- Buttons/elements: tabbed view (**Suppliers | Factories | Warehouses | Ports | Routes**), table per tab with columns relevant to that type (e.g., Warehouse: name, location, capacity, current stock), **[+ Add]** per tab, per-row **[Edit]**/**[Delete]**, map-pin picker for lat/long when adding a node.
- Navigation: **[+ Add]** → **Add Node/Route modal**. Row click → **P5 Node/Route Detail Page**.

**P5 — Node / Route Detail Page** *(shared component reused by the map in Stage 4)*
- Purpose: deep-dive on one node or route — its attributes, connected routes/nodes, current status.
- Buttons/elements: attribute panel (editable if Admin), **[View on Map]** button, **[Edit]** (Admin only), **[Back]**.
- Navigation: **[View on Map]** → **P6 Network Twin Map**, focused/zoomed to this node. **[Back]** → previous page (P4 or P6, whichever opened it).

**Definition of done:** admin can fully populate a demo network (12 shipments' worth of suppliers/factories/warehouses/ports/routes matching your Chattogram scenario).

---

### Stage 4 — Interactive Network Map & Digital Twin View
**Maps to:** Week 1→2 boundary
**Goal:** Planners can *see* the network as a live map and a dependency graph — this is your headline visual.

**Tools needed:** Leaflet.js, react-leaflet, React Flow, dark map tiles.

**Backend work:** `/network/graph` endpoint returning all nodes+routes with current status for the twin; `/network/nodes/{id}` for drill-down.

**Pages built:**

**P6 — Network Twin Map (Planner's main screen)**
- Purpose: the "digital twin" — geographic map of the whole network with live status colors, toggleable to a dependency graph.
- Buttons/elements:
  - **[Map View / Graph View]** toggle (switches Leaflet map ↔ React Flow graph, same data)
  - Layer checkboxes: Suppliers · Factories · Warehouses · Ports · Routes
  - Search/filter box (find a node by name)
  - **[Focus Mode]** toggle (dims everything except selected node's dependency chain)
  - Node markers (color-coded by status per §2.1) — clicking one opens a side panel
  - Route lines (color-coded; pulsing red if disrupted)
  - Floating **[+ New Disruption]** primary button (top-right, cyan, glowing)
  - Timeline scrubber at bottom (grayed out/disabled until Stage 9 replay is built)
- Navigation: click a node marker → slide-in side panel with summary + **[Open Full Detail]** → **P5 Node/Route Detail Page**. **[+ New Disruption]** → **P9 Disruption Simulator**.

**Definition of done:** the seeded network renders on both map and graph views with correct status colors; this is your "wow" screen for judges.

---

### Stage 5 — Live Event Pipeline: Simulator, Kafka, WebSocket, Shipment Lifecycle
**Maps to:** Week 2
**Goal:** Shipments move through real states, streamed live to every connected dashboard.

**Tools needed:** Apache Kafka **[FREE, self-hosted via Docker]** *(if Kafka setup time is a risk, a lighter free alternative is Redpanda, which is Kafka-API-compatible and much faster to stand up in Docker — swap-in only, no code change needed)*, Java TCP client/simulator, Spring WebSocket + STOMP, State design pattern for shipment lifecycle.

**Backend work:** `Shipment` entity with `State` (Planned → InTransit → Delayed → Rerouted → Delivered/Cancelled) implemented via the State pattern (§7); TCP simulator app that emits fake shipment/node events; Kafka topics (`shipment-events`, `disruption-events`); Kafka consumer updates Postgres + pushes over WebSocket/STOMP to subscribed clients.

**Pages built:**

**P7 — Operations: Live Shipment Tracking**
- Purpose: operator's main screen — every shipment, live status, updates in real time without refresh.
- Buttons/elements: sortable/filterable table (Shipment ID, Origin→Destination, State badge, ETA, Risk flag), status filter chips (All/Planned/In Transit/Delayed/Rerouted/Delivered), search box, live-update pulse indicator on rows that just changed.
- Navigation: row click → **P8 Shipment Detail**.

**P8 — Shipment Detail**
- Purpose: single shipment's full picture — timeline, route, notes, manual override.
- Buttons/elements: state timeline (visual stepper: Planned→...→Delivered, current step highlighted), route mini-map, **[Update Status]** dropdown + **[Save]** (operator only, drives the State pattern transition), **[Add Note]** text box + **[Post]** (distinguishes confirmed fact vs. assumption per your FAQ), **[Back to List]**.
- Navigation: **[Back to List]** → **P7**. Route mini-map click → **P6 Network Twin Map** focused on that route.

**Definition of done:** running the TCP simulator visibly updates P7 and P8 live via WebSocket, and node colors on P6 reflect shipment delays without a page refresh.

---

### Stage 6 — Disruption Simulator & Impact/Risk Engine
**Maps to:** Week 2→3
**Goal:** A planner can trigger your headline demo scenario and see traced impact.

**Tools needed:** Observer design pattern (risk/state change notifications), Redis (fast-changing risk scores), PostgreSQL (durable disruption records).

**Backend work:** `Disruption` entity (target node/route, severity, duration, startTime); `RiskEngine` service that traverses the graph from the disrupted node/route (breadth-first over `Route` edges), recalculates node/route/shipment risk scores, and predicts shortage windows from inventory + demand + lead time (deterministic Java model, per your proposal — not ML).

**Pages built:**

**P9 — Disruption Simulator**
- Purpose: create a disruption exactly like your Port Chattogram example.
- Buttons/elements: **[Select Target]** (click a node/route on an embedded mini-map or pick from dropdown), **Severity** slider (Low/Medium/High/Critical), **Duration** input (hours), **[Run on Safe Copy]** primary button (cyan glow — this is the "never touches live data" guarantee from your FAQ), **[Cancel]**.
- Navigation: **[Run on Safe Copy]** → **P10 Impact Analysis / Simulation Results** (loading animation while the engine traces the graph — this is a good spot for a short "tracing dependencies..." animated state, reinforcing the "live" feel).

**P10 — Impact Analysis / Simulation Results**
- Purpose: show what the disruption breaks — your "twelve affected shipments, three exposed warehouses" moment.
- Buttons/elements: summary KPI row (Affected Shipments, Exposed Warehouses, Est. Shortage Time, Network Risk Score — animated counters per §2.3), affected-shipments table, affected-nodes list with risk badges, embedded map with affected routes turned red (matches your deck's exact wording), **[Generate Incident Brief]** primary button, **[View on Full Map]** link.
- Navigation: **[Generate Incident Brief]** → **P11 Incident Brief**. **[View on Full Map]** → **P6**, filtered to only affected elements.

**Definition of done:** running the frozen Chattogram scenario produces exactly the affected counts you specified in your deck (12 shipments, 3 warehouses).

---

### Stage 7 — AI Incident Intelligence & Shortage Forecasting
**Maps to:** Week 3
**Goal:** The disruption gets an explainable, validated AI brief — with guardrails, not autonomous action.

**Tools needed:** Gemini API **[free tier]**, Spring WebClient (async HTTP to Gemini), a JSON schema you enforce server-side before ever showing AI output to a user, deterministic Java shortage model (already built in Stage 6, surfaced here).

**Backend work:** `IncidentIntelligenceService` — normalizes the disruption+impact data into a minimal prompt (no secrets/commercial data per your FAQ), calls Gemini requesting structured JSON (`explanation`, `assumptions[]`, `confidence`, `recommendedActions[]`), validates the response shape and confidence threshold, stores prompt+response in the audit log, falls back to a deterministic rule-based summary if Gemini is unavailable or returns invalid JSON (this fallback is what protects your demo from a live-API failure in front of judges).

**Pages built:**

**P11 — Incident Brief**
- Purpose: the human-readable AI explanation, clearly separated from raw data.
- Buttons/elements: "AI Brief" panel (indigo-accented per §2.1, with a small "Gemini" badge + confidence % chip; if fallback was used, an explicit "Deterministic fallback — AI unavailable" badge instead, never silently pretending), sections for **Evidence**, **Assumptions**, **Recommended Actions**, **[Regenerate]** (Planner only, rate-limited), **[Proceed to Recovery Plans]** primary button.
- Navigation: **[Proceed to Recovery Plans]** → **P12 Recovery Plan Comparison**.

**Definition of done:** the brief renders for your frozen scenario; killing your internet connection still produces a usable (labeled fallback) brief instead of a crash — rehearse this specifically, since your own risk list flags "AI errors" as a weakness.

---

### Stage 8 — Recovery Plan Engine, Approval Workflow, Planner Dashboard
**Maps to:** Week 3
**Goal:** Planner compares ranked alternatives and makes an accountable decision.

**Tools needed:** Strategy pattern (interchangeable scoring policies: cheapest/fastest/safest — per your deck), Command pattern (recovery actions are executable + reversible objects, enabling the "Replay" feature later), PostgreSQL for the decision record.

**Backend work:** `RecoveryPlanEngine` filters options that violate capacity/availability/route constraints, then scores remaining reroute/supplier/stock-transfer combinations via the three Strategy implementations; `RecoveryCommand` objects encapsulate each recovery action so they can be applied, rolled back, and replayed.

**Pages built:**

**P12 — Recovery Plan Comparison**
- Purpose: rank feasible responses by delay/cost/feasibility/residual risk — no single improvised option.
- Buttons/elements: card-per-option layout (Reroute via X / Alternate Supplier Y / Stock Transfer from Z), each card shows delay, cost, feasibility %, residual risk, component scores (from the active Strategy) with a small breakdown popover, **[Sort by: Cost / Delay / Risk]** dropdown, per-card **[Select]** button, **[Compare Selected]** if multiple chosen.
- Navigation: **[Select]** on a card → **P13 Approval / Decision Page**, pre-loaded with that option.

**P13 — Approval / Decision Page**
- Purpose: the accountability moment — approve, reject, or revise, with a mandatory reason.
- Buttons/elements: selected plan summary, **Reason** text box (required), **[Approve & Execute]** (green, primary), **[Reject]** (red, secondary), **[Request Revision]** (amber, secondary — sends back to P12 with a note), timestamp + approver identity auto-attached.
- Navigation: **[Approve & Execute]** → confirmation toast + **P14 Planner Dashboard** (now showing this as an active/resolved incident). **[Reject]**/**[Request Revision]** → back to **P12**.

**P14 — Planner Dashboard (fleshed out; was a stub since Stage 1)**
- Purpose: planner's home — network health at a glance, active disruptions, quick links.
- Buttons/elements: KPI row (Active Disruptions, Open Approvals, Network Risk Score), "Active Disruptions" list with per-row **[Open]**, "Recent Decisions" feed, **[+ New Disruption]** shortcut button, **[Open Network Map]** button.
- Navigation: **[Open]** on a disruption → **P10 Impact Analysis** (or P11/P12/P13 depending on how far that disruption has progressed — route based on its current stage). **[+ New Disruption]** → **P9**. **[Open Network Map]** → **P6**.

**Definition of done:** you can walk your full frozen scenario start to finish: P6 → P9 → P10 → P11 → P12 → P13 → back to P14, with the decision now visible in history.

---

### Stage 9 — Timeline, Replay & Audit History
**Maps to:** Week 3→4
**Goal:** Every incident can be reconstructed and reviewed — your accountability/explainability differentiator.

**Tools needed:** React (timeline scrubber component), PostgreSQL event log table, WebSocket (for live timeline updates during Stage 5-7 events), the Command pattern's stored actions (Stage 8) make replay possible without re-running side effects.

**Backend work:** `/incidents/{id}/timeline` returns every event (disruption created, risk recalculated, AI brief generated, plan approved, shipment state changes) in order with timestamps; a "replay" endpoint that streams those same events back at accelerated speed without touching live data.

**Pages built:**

**P15 — Timeline & Replay (Operations)**
- Purpose: scrub through a disruption's full lifecycle for review/training, per your FAQ's "replay a completed incident."
- Buttons/elements: horizontal timeline scrubber with event markers, **[▶ Play Replay]** / **[⏸ Pause]**, speed selector (1x/2x/4x), event detail panel that updates as the scrubber moves, **[Jump to Decision Point]** shortcut.
- Navigation: event marker click → jumps that event into the detail panel (same page, no navigation). **[Open Related Incident Brief]** → **P11** (read-only mode, historical).

**P16 — Audit History / Decision Log**
- Purpose: the full accountable record — who decided what, when, and why. Directly answers your FAQ's "limited record of who decided what" gap.
- Buttons/elements: filterable table (Date, Disruption, Decision, Approver, Reason snippet), **[View Full Record]** per row (expands AI prompt/response, validation result, approval reason), **[Export]** (ties to Stage 10 reporting).
- Navigation: **[View Full Record]** → expands inline (accordion), or **[Open Incident]** → **P15** for that incident's replay.

**Definition of done:** replaying your frozen scenario visually reconstructs the exact sequence from your deck's "Example Process" slide (Receive → Validate → Update Twin → Score → Build Options → Approve → Stream Replay).

---

### Stage 10 — Manager Views & Reporting
**Maps to:** Week 4 (can start earlier if ahead of schedule)
**Goal:** Non-technical decision-makers get a clean executive summary, exportable.

**Tools needed:** Recharts, a lightweight PDF export (e.g., server-side via a small Java PDF library, or client-side via a free JS PDF generator).

**Backend work:** `/reports/executive-summary` aggregating exposed orders, projected shortages, recovery cost, service impact across incidents; export endpoint producing PDF/CSV.

**Pages built:**

**P17 — Executive Dashboard (Manager)**
- Purpose: high-level severity/cost/service view, no operational detail.
- Buttons/elements: KPI cards (Disruption Severity, Exposed Orders, Recovery Cost, Service Impact), trend chart (Recharts line/area), recent-incidents summary list.
- Navigation: incident row click → **P18 Scenario Comparison** for that incident.

**P18 — Scenario Comparison (Manager)**
- Purpose: "why this option and not the others" — reuses recovery plan data from P12 in a manager-readable comparison.
- Buttons/elements: side-by-side cards (approved option highlighted, alternates grayed), rationale summary pulled from the Incident Brief (P11), **[Back to Executive View]**.
- Navigation: **[Back to Executive View]** → **P17**.

**P19 — Reports & Export**
- Purpose: download incident summaries and decision histories for stakeholders.
- Buttons/elements: date-range picker, incident multi-select, **[Export as PDF]**, **[Export as CSV]**, download-history list of previous exports.
- Navigation: exports trigger a file download (no page change).

**Definition of done:** a manager account, with zero access to admin/simulator screens, can still fully understand and export any resolved incident.

---

### Stage 11 — Admin Configuration & System Health
**Maps to:** Week 4
**Goal:** The platform is operable, not just demoable — admin can tune it and see its own health.

**Tools needed:** Spring Security refinement (fine-grained `@PreAuthorize`), Docker health checks, Micrometer/Actuator (optional, for system metrics) **[FREE]**.

**Backend work:** `/admin/config` for simulation thresholds and alert rules; `/admin/health` aggregating Kafka/DB/Redis/Gemini connectivity; `/admin/connectors` for TCP simulator/Kafka source management.

**Pages built:**

**P20 — Admin: System Configuration**
- Purpose: tune thresholds, alert rules, and approved AI usage settings without a redeploy.
- Buttons/elements: form sections (Risk Thresholds, Alert Rules, AI Settings — model, confidence floor, fallback toggle), **[Save Changes]**, **[Reset to Defaults]**.
- Navigation: **[Save Changes]** → toast confirmation, stays on page.

**P21 — Admin: System Health & Connectors**
- Purpose: one dashboard for integration health, failed validations, and the ability to suspend a misbehaving source.
- Buttons/elements: status tiles (Kafka, PostgreSQL, Redis, Gemini API, WebSocket — green/red), failed-validation log table, connector list (TCP simulator, Kafka topics) with per-row **[Suspend]**/**[Resume]**, **[View Full Audit Log]**.
- Navigation: **[View Full Audit Log]** → **P16 Audit History**.

**Definition of done:** unplugging a dependency (e.g., stopping the Kafka container) visibly flags red on P21 without crashing the rest of the app.

---

### Stage 12 — Security Hardening, Testing, Dockerization, CI/CD, Demo Rehearsal
**Maps to:** Week 4
**Goal:** Everything from Stages 1–11 is reliable, containerized, and tested — no new pages, this is a polish pass.

**Tools needed:** JUnit 5, Testcontainers (spin up real Postgres/Kafka in tests), Docker Compose (full stack: api, web, simulator, postgres, redis, kafka/redpanda), GitHub Actions (build → test → quality gate on every push), Postman collection covering your frozen scenario end-to-end.

**Work in this stage:**
- Role-guard every endpoint again explicitly (don't trust earlier stages' guards were complete).
- Add loading/error/empty states to every page built above (a blank screen during a live-API hiccup is the single most common thing that breaks a judged demo).
- One `docker-compose.yml` that boots the entire system with one command — this is explicitly a strength in your own deck ("Dockerized services... repeatable deployment").
- GitHub Actions pipeline: build backend + frontend, run JUnit/Testcontainers suite, fail the build on test failure.
- Full rehearsal of the Port Chattogram scenario from a cold start (containers freshly booted) at least twice before presenting.

**Definition of done:** a stranger can `git clone` → `docker compose up` → open the app → log in as each of the 4 roles → run the frozen scenario, with no manual setup steps and no console errors.

---

## 5. Consolidated Page Inventory

| ID | Page | Role(s) | Built in | Reached from |
|---|---|---|---|---|
| P1 | Login | Public | Stage 1 | — |
| P2 | Dashboard Home (router) | All | Stage 1 | P1 |
| P3 | User Management | Admin | Stage 2 | Sidebar / P2 |
| P4 | Network Master Data | Admin | Stage 3 | Sidebar |
| P5 | Node/Route Detail | Admin, Planner | Stage 3 | P4, P6 |
| P6 | Network Twin Map | Planner | Stage 4 | Sidebar, P5, P10, P14 |
| P7 | Live Shipment Tracking | Operator | Stage 5 | Sidebar, P2 |
| P8 | Shipment Detail | Operator | Stage 5 | P7 |
| P9 | Disruption Simulator | Planner | Stage 6 | P6, P14 |
| P10 | Impact Analysis | Planner | Stage 6 | P9 |
| P11 | Incident Brief (AI) | Planner | Stage 7 | P10, P15 (read-only) |
| P12 | Recovery Plan Comparison | Planner | Stage 8 | P11 |
| P13 | Approval/Decision | Planner | Stage 8 | P12 |
| P14 | Planner Dashboard | Planner | Stage 8 (stub in 1) | Sidebar, P2 |
| P15 | Timeline & Replay | Operator | Stage 9 | Sidebar, P16 |
| P16 | Audit History | Planner, Admin | Stage 9 | Sidebar, P21 |
| P17 | Executive Dashboard | Manager | Stage 10 | Sidebar, P2 |
| P18 | Scenario Comparison | Manager | Stage 10 | P17 |
| P19 | Reports & Export | Manager | Stage 10 | Sidebar |
| P20 | System Configuration | Admin | Stage 11 | Sidebar |
| P21 | System Health & Connectors | Admin | Stage 11 | Sidebar |

**Total: 21 pages** across 4 roles, sharing 1 global shell.

---

## 6. Consolidated Tools-by-Feature Table

| Feature | Primary tools |
|---|---|
| Auth & roles | Spring Security, JWT, BCrypt |
| Network master data | Spring Data JPA, PostgreSQL |
| Map visualization | Leaflet.js, react-leaflet, dark tile layer |
| Dependency graph | React Flow |
| Live events | Apache Kafka (or Redpanda), Java TCP simulator |
| Real-time UI sync | Spring WebSocket + STOMP |
| Shipment lifecycle | State design pattern |
| Risk/impact engine | Java domain services, Observer pattern, Redis |
| AI incident brief | Gemini API, Spring WebClient, JSON schema validation |
| Shortage forecasting | Deterministic Java model |
| Recovery ranking | Strategy design pattern |
| Approval/execution | Command design pattern |
| Replay | Stored event log + Command pattern |
| Reporting/export | Recharts, PDF/CSV export |
| Styling | Tailwind CSS, shadcn/ui, Framer Motion, Lucide-react |
| Testing | JUnit 5, Testcontainers, Postman |
| Deployment | Docker, Docker Compose, GitHub Actions |

Every item above is free/open-source except Gemini API's paid tiers beyond the free quota — stay inside the free tier by caching AI responses per frozen scenario run rather than calling on every page load.

---

## 7. OOP Entities & Design Pattern Map

| Entity/Pattern | Where it's used | Which page(s) expose it |
|---|---|---|
| `NetworkNode` (base) → `Supplier`, `Factory`, `Warehouse`, `Port` | Inheritance/polymorphism | P4, P5, P6 |
| `Route` | Links two nodes | P4, P5, P6 |
| `Shipment` + **State pattern** | Planned→Transit→Delayed→Rerouted→Delivered | P7, P8 |
| `Disruption` | Simulated event | P9, P10 |
| **Observer pattern** | Push risk/state changes to subscribers | P6, P7 live updates |
| **Strategy pattern** | Cost/fastest/safest route scoring | P12 |
| **Command pattern** | Apply/roll back a recovery action, enables replay | P13, P15 |

---

## 8. Suggested Repo Structure (good for Claude Code sessions)

```
resilichain-twin/
├── resilichain-api/          # Spring Boot backend
│   ├── src/main/java/.../domain/       (entities: NetworkNode, Shipment, Disruption...)
│   ├── src/main/java/.../service/      (RiskEngine, RecoveryPlanEngine, IncidentIntelligenceService)
│   ├── src/main/java/.../pattern/      (strategy/, state/, command/, observer/)
│   ├── src/main/java/.../api/          (REST controllers, one per feature area)
│   └── src/test/java/...
├── resilichain-web/          # React + TS frontend
│   ├── src/pages/            (P1..P21, one folder per page)
│   ├── src/components/shell/ (TopBar, Sidebar)
│   ├── src/components/ui/    (shadcn components skinned with design tokens)
│   ├── src/lib/theme.ts      (§2.1–2.2 tokens)
│   └── src/lib/ws.ts         (WebSocket/STOMP client)
├── resilichain-simulator/    # TCP/Kafka event simulator
├── docker-compose.yml
├── .github/workflows/ci.yml
└── CLAUDE.md                 # project conventions for Claude Code sessions
```

---

## 9. Final Demo Script Checklist (ties back to your deck's own example)

1. Log in as **Admin** → briefly show P3/P4 (network is pre-seeded, don't build live).
2. Switch to **Planner** → **P6 Network Twin Map**, everything green.
3. **[+ New Disruption]** → **P9**, select Port Chattogram, 36 hours, **[Run on Safe Copy]**.
4. **P10** — show 12 shipments, 3 warehouses, red routes on the map.
5. **[Generate Incident Brief]** → **P11** — read out evidence/assumptions/confidence.
6. **[Proceed to Recovery Plans]** → **P12** — show ranked options with component scores.
7. **[Select]** best option → **P13** — enter reason, **[Approve & Execute]**.
8. Switch to **Operator** → **P15 Timeline & Replay** — replay the whole incident.
9. Switch to **Manager** → **P17/P18** — show the executive, judge-friendly summary.
10. Back to **Admin** → **P16 Audit History** — show the full accountable record.

This walks every one of your deck's "Eight Connected Features" in under 5 minutes.
