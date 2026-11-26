# place-order-flow: grill of the models (gate item 4)

Date: 2026-11-26. Run by claude on behalf of the person (delegated HITL review; demo project: simplest option that
runs). Skill: `zenwave-grill-models`.

Models: [domain-model.zdl](../../../notifications-consumer-api/domain-model.zdl) (service-all 0.2.0),
[place-order-flow.zfl](../../../arcadia-editions-architecture/business-flows/place-order-flow.zfl) (no change since
`baseline/place-order-flow-5`), producers' ZDLs (`orders-checkout-api`, `payments-processing-api`) for the consumed
events. Paths of this context: `PaymentCaptured > sendOrderConfirmation`, `StockUnavailable >
sendStockUnavailableNotification`, `OrderCancelled > sendOrderCancelledNotification`, each `[sent]` or `[failed]`.

## Deterministic checks

| Check | Result |
|---|---|
| `bash .zenwave/bin/lint-zdl.sh --strict ../notifications-consumer-api/domain-model.zdl` | `OK`, no warnings |
| `@zdl("../../notifications-consumer-api/domain-model.zdl")` resolves | yes |
| ZFL commands `sendOrderConfirmation`, `sendStockUnavailableNotification`, `sendOrderCancelledNotification` in the ZDL | yes (zdl:118-128) |
| Emitted events `OrderConfirmationSent`, `StockUnavailableNotificationSent`, `OrderCancelledNotificationSent`, `NotificationDeliveryFailed` | yes (zdl:131-175) |
| Trigger events exist upstream | `PaymentCaptured` payments zdl:248, `StockUnavailable` orders zdl:163, `OrderCancelled` orders zdl:196 |
| `@asyncapi` channels exist in the producers' `asyncapi.yml` | `payment-captured-event-v1`, `stock-unavailable-event-v1`, `order-cancelled-event-v1`: yes |
| `@compensates` on this context's steps | none (nothing to name) |

## Findings and decisions

| # | Area | Finding (model lines) | Decision |
|---|---|---|---|
| G-01 | Correlation | The ZFL redelivery rule (zfl:67-68) needs to find the Notification of a redelivered event; the ZDL has no key for it (zdl:56-71, `orderId` optional, no compound unique). | Already decided (T-05, ADR 0002): one Notification per `orderId` + `type`, `findByOrderIdAndType` (T-08). No model change: all three inputs require `orderId`. |
| G-02 | Concurrency | Two copies of one event processed at once could create two Notifications (no DB unique constraint). | As designed for the demo: the producers key their topics by `orderId`, one consumer group reads one partition in order. No unique constraint (would be a model change for no demo gain). |
| G-03 | Lineage | Required Notification fields: `notificationId`, `channel`, `subject`, `body`, `createdAt` have no upstream source. | `notificationId` UUIDv7 by the service (T-04); `channel` always EMAIL (T-06); texts per ADR 0002 (T-07); `createdAt`/`sentAt`/`failedAt` from an injected Clock (T-03); `recipient` = input `customerEmail`; `customerId`, `orderId` from the input. |
| G-04 | Lineage | `OrderCancelledNotificationSent.reason` (zdl:157) has no Notification field; `NotificationDeliveryFailed.reason` (zdl:173) neither. | `reason` = input `reason` (T-17); failed `reason` = `failureReason` (T-20). No model change (A-05). |
| G-05 | Lineage / contract | `skus` is a non-null Avro array in `NotificationDeliveryFailed` and `StockUnavailableNotificationSent`; the confirmation and cancellation inputs carry no SKUs. | Empty list for the other types (A-06); never null. |
| G-06 | Handled / ended | Every emitted event is an `end` outcome (zfl:340-345); `[sent]` and `[failed]` both end. | As designed. Nothing to add. |
| G-07 | Compensated | A sent e-mail cannot be undone; nothing in the flow runs after it. | As designed. |
| G-08 | Lifecycle | `@lifecycle(initial: PENDING)` with `@transition(to: [SENT, FAILED])` and no `from`: PENDING is never persisted. FAILED is terminal: no resend. | As designed. A FAILED notification is left to ops through `NotificationDeliveryFailed`; redelivery on FAILED is a no-op (ZFL rule, T-20). |
| G-09 | Repetition | The delivery port can throw (bug, outage) instead of answering failed. | The port answers `failed` with `mailbox_unavailable` for a refused recipient (ADR 0002); an exception rolls back and goes to the consumer's retry topics / DLQ declared in `asyncapi-client.yml`. |
| G-10 | Repetition / outbox | With the outbox (ADR 0003) events leave after commit; the e-mail is sent inside the transaction, so a failed commit after a send repeats the e-mail on redelivery. | Accepted for the demo (at-least-once e-mail). Recorded in ADR 0003 consequences (delivery at-least-once). |
| G-11 | Repetition | Can one order get both ORDER_CONFIRMATION and ORDER_CANCELLED, or two cancellations? | No: after `PaymentCaptured` the ZFL has no cancellation path, and `cancelOrder` is a no-op on CANCELLED (zfl:303). Different types never collide on the key anyway. |
| G-12 | Contracts | `OrderCancelled.reason` is a `String` (orders zdl:202, `Order.cancellationReason`), not an enum: the PAYMENT_VOID_FAILED text (zfl:322-327) depends on a literal. | Compare with the literal `PAYMENT_VOID_FAILED`; any other value gets the generic cancellation body with the value as is (ADR 0002 table). |
| G-13 | Contracts | Avro timestamps are strings (`sentAt`, `failedAt`). | ISO-8601 `Instant.toString()` (generated `BaseMapper.asCharSequence(Instant)`). |
| G-14 | Contracts | `@rest("/notifications")` on the service but no REST operation (`openapi.yml` `paths: {}`). | As designed: no controller, no API tests (board header). |
| G-15 | Contracts | Consumed channels `$ref` the producers' `asyncapi.yml` on `main` (residual coupling, overlay). | Accepted; noted in the overlay. The Avro types are pinned by `service-all 0.2.0`. |
| G-16 | Language | No `GLOSSARY.md` or `docs/domain.md` in this repo (nor in the sibling repos). | The ZDL/ZFL identifiers are the terms for the demo; overlay row updated to say so. |
| G-17 | Decisions | ADRs 0001 (producers from core.application), 0002 (rules, stand-in), 0003 (outbox) checked against the models. | No contradiction. ADR 0003 fixed: `spring-modulith-starter-jdbc` (not `-jpa`) and the null-headers note. |
| G-18 | Test data | A first delivery that fails, and a void-refused first delivery, had no chain without a row (every FAILED / PAYMENT_VOID_FAILED chain already had a row, so a test would hit the redelivery no-op). | New no-row chains C-33..C-36 in the `data.sql` header (gate item 13 doc). |

## Proposals

None. No finding needs a model change for the demo; `docs/proposals/` stays empty.

## Frontier

Empty: every finding above is decided (as designed, a board task, an ADR, or test data).
