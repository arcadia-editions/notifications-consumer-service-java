# 0002. Notification rules and stand-in sender

Status: accepted, 2026-11-26 (demo: simplest rule that runs; board T-05, T-06, T-07, T-22)

## Decision

- **Redelivery (T-05):** one Notification per `orderId` + `type` (data.sql A-02). A redelivered event finds it with
  `findByOrderIdAndType` and does nothing more (no second send, no event).
- **Channel (T-06):** always `EMAIL` to the input's `customerEmail` (A-03). SMS and PUSH stay unused.
- **Texts (T-07)**, as in the fixtures (A-05):

| Type | Subject | Body |
|---|---|---|
| ORDER_CONFIRMATION | `Your order <orderId> is confirmed` | `Your order <orderId> is confirmed and on its way.` |
| ORDER_CANCELLED | `Your order <orderId> was cancelled` | `Your order <orderId> was cancelled: <reason>. No payment was captured.` |
| ORDER_CANCELLED, reason PAYMENT_VOID_FAILED | same | `Your order <orderId> was cancelled: PAYMENT_VOID_FAILED. No payment was captured, but the hold may still show on your statement until it is released.` |
| STOCK_UNAVAILABLE | `Items unavailable for order <orderId>` | `Your order <orderId> could not be placed: <skus joined by ", "> is/are no longer available.` (`is` for one sku, `are` for more) |

- **Failure reason (T-07):** a single code, `mailbox_unavailable` (A-07).
- **Provider (T-22):** no real e-mail provider in the demo. The runtime adapter behind the T-09 port is a stand-in
  (as the modulith's `StandInNotificationSender`): it logs the message and answers sent; a recipient ending in
  `@fail.invalid` answers failed with `mailbox_unavailable`. Tests keep the in-memory stub of T-09.

## Consequences

No credentials or SMTP in docker-compose. A real provider is a new adapter behind the same port.
