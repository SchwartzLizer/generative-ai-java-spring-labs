# ADR-0001: Keep the customer support platform as a modular monolith

- **Status:** Accepted
- **Date:** 2026-08-31
- **Deciders:** Repository owner (sole developer)
- **Scope:** `customer-support-platform` module only. The seven lab modules and Final Project Part A are unaffected.

## Context

The customer support platform runs as one Spring Boot deployable backed by one
PostgreSQL database. It contains four candidate bounded contexts — feedback
intake, feedback analysis, response drafting, and support operations — described
in [`docs/context-map.md`](../context-map.md).

The question raised was whether to decompose it into separate services. To answer
it honestly rather than by preference, the coupling between the candidate contexts
was traced through the code and the schema. Three findings decided it.

**1. One seam sits inside a single ACID transaction.**
`FeedbackAnalysisService.analyze()` is one `@Transactional` method that appends a
row to `feedback_analysis` **and** transitions the `Feedback` aggregate from `NEW`
to `ANALYZED`. That transition is guarded by optimistic locking — the `@Version`
`version` field on `Feedback`, backed by the `version BIGINT` column at
`customer-support-platform/src/main/resources/db/migration/V1__create_support_tables.sql`
line 8 — and by the in-aggregate state machine in `Feedback.changeStatus()`, which
throws `InvalidStateTransitionException` on an illegal move.

Splitting analysis away from intake converts that one method into a saga: a local
append, then a remote, retriable, idempotent status transition, plus a
compensating path for the case where the analysis row is already committed and the
transition is rejected. The consistency guarantee does not disappear; it becomes
application code that must be designed, tested, and operated.

**2. The read model touches every context in one transaction.**
`DashboardService.summary()` issues one count query per
constant of four separate enums — `FeedbackStatus`, `Sentiment`,
`SupportCategory`, and `Urgency` — plus three standalone counts, fanned across
three repositories inside a single `@Transactional(readOnly = true)`. The query
count is not fixed: it grows every time one of those enums gains a constant, which
is exactly why this read model resists being split. On top of that,
`DashboardPageController.dashboard()` renders a page that combines
those counts with a filtered, sorted, paginated feedback query. Across service
boundaries this requires either HTTP fan-out with no consistency guarantee, or an
event-fed CQRS read model — which means a broker or outbox to run, test, and
document. Cross-service pagination has no cheap correct answer.

**3. The strongest asset in this repository is that it runs in one command.**
The "Review the project in five minutes" and "Testing" sections of
`README.md` promise exactly that: `./mvnw.cmd verify` for the whole suite and
`docker compose up --build` for a
running system, backed by CI at `.github/workflows/ci.yml` line 21 and a single
image build at line 23. The `supportsFeedbackAnalysisDraftAndSummaryFlow` test in `SupportWorkflowIntegrationTest` proves
the entire submit → analyze → draft → summary flow over real HTTP in one test
class. There is no cheap equivalent of that test once the flow spans processes.

The system serves a synthetic support queue with no production traffic, no
independent scaling pressure on any context, and one developer. None of the
classical drivers for decomposition — independent deployability across teams,
divergent scaling profiles, technology heterogeneity, fault isolation across
organisational boundaries — currently apply.

## Decision

**Keep one deployable and one database. Make the context boundaries explicit in
documentation now, and treat microservice extraction as a costed, deferred option
rather than a goal.**

Concretely:

- The four bounded contexts are named, mapped, and given a glossary in
  [`docs/context-map.md`](../context-map.md), including the places where the
  current code violates its own boundaries.
- The extraction plan, per-seam cut cost, data ownership, and a saga design for
  the transactional seam are recorded in
  [`docs/microservice-decomposition.md`](../microservice-decomposition.md) so the
  work can be scheduled rather than discovered.
- A provider-neutral deployment topology is recorded in
  [`docs/cloud-deployment-topology.md`](../cloud-deployment-topology.md).

## Consequences

### Accepted, positively

- Consistency stays free. The `Feedback` state machine, optimistic locking, and
  the append-only histories in `feedback_analysis` and `response_draft` are
  enforced by the database in one transaction, not by retry logic.
- One command builds, tests, and runs everything. The five-minute review promise
  in `README.md` holds.
- Refactoring across contexts is a compiler problem, not a versioned wire
  contract problem. Boundaries can move cheaply while the model is still young.
- Local development needs one JVM and one database container
  (`compose.yaml`, 35 lines).

### Accepted, as costs

- **No independent deployability.** A change to the dashboard redeploys the
  analysis code. Acceptable at one developer; a real constraint at several teams.
- **No independent scaling.** The AI-calling path in `FeedbackAnalysisService` and
  the read-heavy dashboard path scale together whether or not their load profiles
  match.
- **Shared failure domain.** A memory leak or a thread-pool exhaustion anywhere
  takes down feedback intake, analysis, drafting, and the dashboard together.
- **Boundaries are currently convention, not constraint.** Nothing in the build
  fails when one context imports another's internals — and several already do
  (see the violations section of `docs/context-map.md`). Enforcing them with
  architecture tests is the recommended next step and is *not* implemented today.
- **Deferring the split does not make it cheaper.** It gets more expensive as the
  schema grows. The cost is recorded now precisely so it is not discovered later.

### Rough estimate of the deferred work

Extracting one service is estimated at **27–48 hours**; the full four-service
split at **70–120 hours**. These are rough estimates, not commitments. They assume
a single developer already fluent in this codebase, **no prior production
microservices experience**, and they exclude cloud provisioning, orchestration,
message-broker operations, distributed tracing, and load testing. The dominant
line item is not moving code — the platform is small — it is the saga, the read
model, inter-service authentication, the test rebuild, and the local-development
rework. Version compatibility between the pinned Spring Boot 4.1.1 (`pom.xml`
line 31) and any Spring Cloud component is unverified and could add 8–16 hours on
its own.

## Alternatives considered

**Extract one service (feedback analysis, together with the AI adapter).**
The most defensible partial move: `analysis` and `ai` are the most cohesive pair,
and `CustomerSupportAiClient` is already a clean port. Rejected for now because
the 27–48 hour estimate is dominated by the saga at
`FeedbackAnalysisService.analyze()` and by rebuilding the cross-process test
coverage that
`SupportWorkflowIntegrationTest` currently provides in one class — while the
resulting system would be strictly harder to run and no more capable. This
remains the first move if the decision is revisited.

**Full four-service split with a gateway and service discovery.**
Rejected. It would force a shared-database-versus-schema-per-service decision, an
inter-service authentication mechanism to replace the single
`InMemoryUserDetailsManager` built in `SecurityConfiguration.userDetailsService()`,
and an authorization server that the project's own design
constraints ruled out. At current scale this is complexity without a payer.

**Do nothing and leave the architecture undocumented.**
Rejected. The boundaries exist in the code but were invisible to a reader, and
several are already violated without anyone having decided to violate them.

## When to revisit

Any one of these should reopen this decision:

1. More than one team commits to this codebase and deployment coordination
   becomes a scheduling problem.
2. The analysis path's latency or cost profile diverges enough from the rest of
   the system that it needs its own scaling or its own failure budget.
3. A single context's data volume forces storage or indexing choices that harm
   the others.
4. A compliance boundary requires that customer message content be isolated from
   the operational read model.

Until one of those is true, the correct architecture for this system is the one
that is written down here.
