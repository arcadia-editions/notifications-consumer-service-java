# place-order-flow: test data review (gate item 13)

Date: 2026-11-26. Reviewed by claude on behalf of the person. Scope: the header of
[data.sql](../../src/test/resources/data.sql) (scenario matrix, coverage, assumptions), its 10 rows and the JSON mirrors
`src/test/resources/data/jpa/Notification/1..10.json`, against the board [docs/tasks.md](../tasks.md).

## Checked, no change

- 10 rows = 10 JSON mirrors (id `1 + (k-1)*50`, same `notificationId`, `orderId`, status, recipient, skus).
- Business ids match the upstream fixtures (same chain = same `ORD-0nn`): orders C-01, C-03, C-06..C-10; payments C-12
  (`CUST-012`, `customer012@example.com`); catalog C-13, C-14 (SKU-004, SKU-005).
- Texts and `mailbox_unavailable` = the ADR 0002 table; enums as names; `skus` `'{}'` for non-stock types (A-06).
- Every timestamp is before the fixed test "now" `2026-10-14T10:00:00Z` (C-01 keeps 2026-09-26 as upstream).

## Inconsistencies found and fixed

| # | Problem | Fix |
|---|---|---|
| D-01 | T-20 and T-21 (failed branch, first delivery) named C-08, C-31, C-32: those rows are already FAILED, so the same event is a redelivery (no-op, no event) and the test could not see `NotificationDeliveryFailed`. | New no-row chains C-34 (confirmation), C-35 (stock unavailable, SKU-004), C-36 (cancellation); T-20 uses them for the first delivery and C-08/C-31/C-32 for redelivery on FAILED; T-21 uses C-34..C-36. |
| D-02 | T-19 / T-17 asked for a first delivery per reason using C-06..C-10, which all have rows (redeliveries); PAYMENT_VOID_FAILED had no first-delivery input. | New no-row chain C-33 (PAYMENT_VOID_FAILED); T-17 uses C-03 ids with each reason (in-memory repository reset per test) and C-33; T-19 uses C-03, C-33 and C-06 (redelivery). |
| D-03 | Stand-in vs stub: the stand-in (T-22) fails `@fail.invalid` recipients, while FAILED rows use `example.com` (A-10). | Coherent once stated: FAILED rows are finished history (stub-driven at the time, A-10 kept); new failing inputs C-34..C-36 use `customer03n@fail.invalid` (A-11), so they fail both when a test tells the stub to fail (T-09) and with the stand-in. T-22 gets `test data:` (C-34 fails, C-12 sent) and `tests: StandInNotificationSenderTest`. |
| D-04 | C-12 matrix line had no customer id. | `ORD-012, CUST-012` (as payments' C-12). |
| D-05 | T-03 (Clock) had no `test data:`. | `data.sql header (fixed test "now" 2026-10-14T10:00:00Z)`. |
| D-06 | The outbox (ADR 0003) adds a test with no task. | T-23 `TransactionalOutboxIntegrationTest` (C-12) in S1. The `event_publication` table is created by Spring Modulith's schema initialization, not by `data.sql`. |

No row added or changed: C-33..C-36 are header-only chains (no Notification exists before their first delivery), so
the DDL check (2026-11-25) still holds. Chains of this context now: C-01, C-03, C-06..C-10, C-12..C-14, C-30..C-36;
assumptions A-01..A-11. Board: `bash .zenwave/bin/check-board.sh docs/tasks.md` -> `board OK: 23 tasks in 6 slices`.

## Coverage matrix vs board (after the fixes)

| Need | Chain | Task |
|---|---|---|
| confirmation first delivery sent / redelivery | C-12 / C-01 | T-11, T-12, T-13, T-23 |
| stock unavailable first delivery / redelivery / 2 SKUs | C-14 / C-13 / C-30 | T-14, T-15, T-16 |
| cancellation first delivery / redelivery / void refused | C-03 / C-06 / C-33 (C-10 text) | T-17, T-18, T-19 |
| failed first delivery per type | C-34, C-35, C-36 | T-20, T-21, T-22 |
| redelivery on FAILED | C-32, C-31, C-08 | T-20 |
| repository / finder | C-01 (id 1), C-13 | T-01, T-08 |
| consumed binding | C-12 | T-10 |
