# Notifications Consumer Service (Java): ZenWave SDD overlay

What every zenwave-sdd skill and agent needs to know about this project: where the models are, its shape, its
commands, and where its test data is. `AGENTS.md` points here.

The placeholders marked `<after generation: ...>` and `<after test data: ...>` are filled in by the skill that
produces the fact (`zenwave-test-data` / `zenwave-test-scenarios` the test data row; `zenwave-gate-check` suggests the
full-suite duration).

## Where things are

Paths are relative to this repository's root. The models live in sibling repositories that must be cloned next to
this one: `../arcadia-editions-architecture` (flows) and `../notifications-consumer-api` (this context's ZDL and contracts).
The flow also references the other contexts' ZDLs: `../orders-checkout-api`, `../catalog-inventory-api`,
`../payments-processing-api`, `../fulfillment-shipping-api`.

| What | Path |
|---|---|
| Business flows (ZFL) | `../arcadia-editions-architecture/business-flows/*.zfl` (today: `place-order-flow.zfl`) |
| Domain models (ZDL), one per bounded context | `../notifications-consumer-api/domain-model.zdl` (this service); other contexts: `../<context>-api/domain-model.zdl` |
| API contracts | `../notifications-consumer-api/openapi.yml` (no operations: `paths: {}`), `../notifications-consumer-api/asyncapi.yml` (published events), `../notifications-consumer-api/asyncapi-client.yml` (consumed events), Avro schemas in `../notifications-consumer-api/avro/` |
| Glossary and ADRs | no `GLOSSARY.md` (the ZDL/ZFL identifiers are the terms, gate 2026-11-26); `docs/adr/` |
| Domain summary | none: `../notifications-consumer-api/SUMMARY.md`; gate review notes in `docs/gate/` |
| Board of the flow in progress | `docs/tasks.md` (archived boards in `docs/tasks/`) |
| Model gap proposals | `docs/proposals/` |
| Test data and scenario matrix | `src/test/resources/data.sql` (header: scenario and coverage matrices, chains `C-01`..`C-36` (this context: C-01, C-03, C-06..C-10, C-12..C-14, C-30..C-36), assumptions A-01..A-11, the fixed test "now" `2026-10-14T10:00:00Z` read by tests through a fixed `Clock`, not yet in the code; DDL exported 2026-11-25 to target/ddl/schema.sql, matches) and `src/test/resources/data/jpa/Notification/<n>.json` |
| Generation script | `zenwave-scripts.zw` |

**`zenwave-scripts.zw` and `pom.xml` read the models from released packages** (`@import(...)` and `classpath:`):

- artifacts and versions: `com.arcadiaeditions.notifications.customer-communications.notifications-consumer:service-all:0.2.0`
  (the `@import` of `zenwave-scripts.zw` and `pom.xml` `notifications-consumer-api.version` must match) and, for the
  consumed Avro types (`StockUnavailableItem`), `com.arcadiaeditions.orders.checkout.orders-checkout:service-all:0.2.0`
  (`orders-checkout-api.version`). Repository: `https://maven.pkg.github.com/arcadia-editions/arcadia-editions-architecture`
  (server id `github` in `~/.m2/settings.xml`). Folder inside the jar:
  `com/arcadiaeditions/notifications/customer-communications/notifications-consumer/`.
- `openapi.yml` has no operations (`paths: {}`): this service exposes no REST API; it is pinned anyway so the OpenAPI
  generators keep working when operations are added.
- regenerate: `ZW_FILE=zenwave-scripts.zw node ../api-product-workflows/scripts/zenwave/generate-from-zdl.mjs . .`
  (`jbang zw` does not read `.zw` files; the script turns the `@import` into `-d`). Never `--force`.
- extract the models when their repository is not cloned:

  ```bash
  P=com/arcadiaeditions/notifications/customer-communications/notifications-consumer; V=$(grep -o 'service-all:[^")]*' zenwave-scripts.zw | head -1 | cut -d: -f2)
  JAR=~/.m2/repository/$P/service-all/$V/service-all-$V.jar
  mkdir -p target/models && unzip -o -q -j "$JAR" "$P/*.zdl" "$P/*.yml" -d target/models
  ```

- staleness check (prints nothing when the packaged copy matches the working copy):
  `for f in domain-model.zdl openapi.yml asyncapi.yml asyncapi-client.yml; do diff -q --strip-trailing-cr "../notifications-consumer-api/$f" "target/models/$f"; done`
- release a model change: in `notifications-consumer-api`, run `artifact-release.yml` (workflow_dispatch) for each changed
  artifact (`domain-model`, `openapi`, `asyncapi-all`; it stamps the file versions and updates the architecture manifest),
  then `service-all`; bump `notifications-consumer-api.version` and the `@import`; regenerate.
- residual coupling to `main` (not pinned by the package): the generated `asyncapi-client.yml` `$ref`s the producers'
  `asyncapi.yml` on `main` (`orders-checkout-api`, `payments-processing-api`) and `api-contract-commons` error topics and
  message traits on `refs/heads/main`; `domain-model.zdl` `apis { asyncapi client ... }` points at `main` of
  `orders-checkout-api` and `payments-processing-api`, and its `asyncapiOverlayFiles` / `openapiOverlayFiles` at
  `api-contract-commons` `refs/heads/main`.

**The ZFL's `@zdl(...)` paths** (`../../<context>-api/domain-model.zdl`, relative to `business-flows/`) resolve to
the sibling clones above; no mapping needed. This service owns `notifications-consumer-api/domain-model.zdl` only.

## Shape

- Deployment: **microservices** (one bounded context per repository; Kafka and AsyncAPI with Avro payloads, Schema
  Registry; ZDL consumers use `@asyncapi`).
- Code already in place: yes (`zenwave-tasks-from-code-and-models`). Generated with ZenWave SDK: yes (2.8.1, commit
  `e0cf81d`); service methods are still the generated `TODO`s and the AsyncAPI consumer adapters in `adapters.events`
  are stubs (`UnsupportedOperationException`, `// TODO CUSTOM_REQUIRED`).
- Stack: Java 25, Spring Boot 4.1.2, Spring Cloud Stream (Kafka), Spring Modulith 2.1.3 events (outbox, ADR 0003),
  MapStruct, ArchUnit, Maven (no wrapper: `mvn`).
- Persistence: `jpa` (PostgreSQL; Testcontainers via docker-compose in tests).
- Organization prefix and packages: `com.arcadiaeditions` (base package `com.arcadiaeditions.notifications.consumer`),
  `CleanHexagonalProjectLayout`: `core.domain`, `core.application`, `core.inbound`, `core.outbound`,
  `adapters.web`, `infrastructure` (JPA), `config`; generated AsyncAPI SDK outside the hexagon in
  `asyncapi.provider` (`asyncapi.yml`) and `asyncapi.client` (`asyncapi-client.yml`), Avro models in `events.avro`.

## Commands

```bash
# JDK and tools: Java 25 and Maven on PATH (SDKMAN), JBang with the `zw` alias, Docker
export JAVA_HOME=~/.sdkman/candidates/java/current
# regenerate after a model change (the person runs it; never the orchestrator or its workers):
# the 3 plugin blocks of zenwave-scripts.zw, in order (BackendApplicationDefaultPlugin, OpenAPIControllersPlugin,
# SpringWebTestClientPlugin); the IDE play button shows each `jbang zw -p <Plugin> ...` command. Never --force.
# Then: mvn -DskipTests clean test-compile (runs the OpenAPI / AsyncAPI Maven generators) and review git diff.
# compile, and collect MapStruct warnings
mkdir -p target && mvn -q -DskipTests clean compile 2>&1 | tee target/compile.log
# focused tests of a task (the implementer runs only these)
mvn -q test -Dtest='<TestClass>'
# full suite with integration tests and containers (once per slice review)
# about 4 minutes (233 s, 2026-11-26, with the outbox); needs Docker (Testcontainers starts the docker-compose PostgreSQL / Kafka / Schema Registry).
# Surefire failures stop the build before the integration tests: add -Dmaven.test.failure.ignore=true to see both.
# NotificationRepositoryIntegrationTest runs once, with Failsafe.
# known failing at the baseline (2026-11-25, unchanged 2026-11-26), each fixed by a board task (docs/tasks.md tests:):
#   NotificationRepositoryIntegrationTest.updateTest: asserts Instant.now() -> T-01
mvn -q verify
# lint the models
bash .zenwave/bin/lint-zdl.sh --strict ../notifications-consumer-api/domain-model.zdl
# export the Hibernate DDL (JPA)
bash .claude/skills/zenwave-test-data/scripts/export-ddl.sh
```

The orchestrator gives the workers the focused command (with the task's `tests:`) and the reviewer the full one.

## Project rules

- Naming conventions (ids, channels, topics): `.claude/skills/zenwave-grill-models/references/zenwave-languages/asyncapi-naming-conventions.md`;
  shared AsyncAPI traits, bindings and error topics in `../api-contract-commons/`.
- Tests: `*Test` run with Surefire (`mvn test`), `*IT` / `*IntegrationTest` with Failsafe (`mvn verify`). Prefer
  in-memory repositories and vertical tests over Mockito; adapters to external resources are tested with
  Testcontainers.
- Generated at build time into `target/` (never edit): OpenAPI interfaces and `*DTO`s, the AsyncAPI SDK.
- Format with `mvn spotless:apply`.
- Decisions already taken that the models don't show: `docs/adr/` (0001 core.application uses the generated
  producers, 0002 notification rules and stand-in sender, 0003 transactional outbox with Spring Modulith).
- Transactional outbox (ADR 0003): events are stored in `event_publication` and reach Kafka after commit (Kafka
  tests await them). Always call the generated producer with its headers object,
  `eventsProducer.onX(payload, new NotificationsConsumerEventsProducer.XHeaders())`: the one-argument overload throws
  `NullPointerException` with the outbox template (zenwave-sdk 2.8.1). For a reference
  implementation of the same flow, see `../arcadia-editions-modulith-service-java/docs/zenwave-sdd-overlay.md`
  (Project rules) and its ADRs.
