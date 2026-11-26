-- Scenario test data. Regenerate with $zenwave-test-scenarios (place-order-flow).
-- DDL exported 2026-11-25 to target/ddl/schema.sql (export-ddl.sh -t NotificationRepositoryIntegrationTest); data.sql matches it.
-- Models: notifications-consumer service-all 0.2.0. Only aggregate: Notification (table notification, sequence notification_seq, increment 50).
-- Fixed test "now" (fixed Clock, still to be added by a board task): 2026-10-14T10:00:00Z.
-- JSON mirrors: src/test/resources/data/jpa/Notification/<k>.json, k = row position, id = 1 + (k-1)*50.
-- Business ids shared with orders-checkout (C-01..C-10), catalog-inventory (C-11..C-14) and payments-processing (C-15..C-19)
-- fixtures: same chain id = same ORD-0nn. New chains of this context start at C-30.
--
-- SCENARIO MATRIX: chain | business ids | Notification state (upstream state implied) | used by
-- C-01 | ORD-001, CUST-001 | ORDER_CONFIRMATION SENT (payment CAPTURED 13:06:30, order CONFIRMED) | outcome completed; sendOrderConfirmation redelivery no-op; repository tests findById(1)
-- C-03 | ORD-003, CUST-003 | no row (stock RELEASED PAYMENT_DECLINED, order cancel pending) | sendOrderCancelledNotification first delivery
-- C-06 | ORD-006, CUST-006 | ORDER_CANCELLED SENT, reason RESERVATION_EXPIRED | outcome cancelled by expiry; sendOrderCancelledNotification redelivery no-op
-- C-07 | ORD-007, CUST-007 | ORDER_CANCELLED SENT, reason PAYMENT_DECLINED | outcome cancelled by decline
-- C-08 | ORD-008, CUST-008 | ORDER_CANCELLED FAILED, reason PAYMENT_RETRY_EXHAUSTED | outcome notificationFailed (type ORDER_CANCELLED); redelivery no-op on FAILED
-- C-09 | ORD-009, CUST-009 | ORDER_CANCELLED SENT, reason PAYMENT_VOIDED | outcome cancelled after fulfillment / capture failure
-- C-10 | ORD-010, CUST-010 | ORDER_CANCELLED SENT, reason PAYMENT_VOID_FAILED, body says the hold may still show | outcome cancelled, void refused
-- C-12 | ORD-012 | no row (payment AUTHORIZED, capture next) | sendOrderConfirmation first delivery
-- C-13 | ORD-013, CUST-013, SKU-004 | STOCK_UNAVAILABLE SENT [SKU-004] (no order persisted) | outcome stockGone; sendStockUnavailableNotification redelivery no-op
-- C-14 | ORD-014, CUST-014, SKU-005 | no row (catalog: SKU-005 less than requested, mixed with SKU-002) | sendStockUnavailableNotification first delivery
-- C-30 | ORD-030, CUST-030, SKU-004 + SKU-005 | STOCK_UNAVAILABLE SENT [SKU-004, SKU-005]; off-chain | multiplicity (unavailableItems minlength(1), 2 items)
-- C-31 | ORD-031, CUST-031, SKU-004 | STOCK_UNAVAILABLE FAILED; off-chain | outcome notificationFailed (type STOCK_UNAVAILABLE)
-- C-32 | ORD-032, CUST-032 | ORDER_CONFIRMATION FAILED; off-chain | outcome notificationFailed (type ORDER_CONFIRMATION)
--
-- COVERAGE (need -> chain, or why no row)
-- Lifecycle: the three commands have @transition(to: [SENT, FAILED]) and no from:: each creates a Notification -> no start row; first
--   delivery -> C-12 (confirmation), C-03 (cancellation), C-14 (stock unavailable)
-- Branches: withEvents [<X>Sent | NotificationDeliveryFailed] -> stub-driven (the e-mail delivery port decides SENT or FAILED); finished
--   examples SENT C-01/C-06/C-13, FAILED C-08/C-31/C-32
-- Outcomes: completed C-01; stockGone C-13; orderCancelled by decline C-07, retries exhausted C-08 (FAILED), voided C-09, void refused C-10,
--   expiry C-06; notificationFailed per type: ORDER_CONFIRMATION C-32, STOCK_UNAVAILABLE C-31, ORDER_CANCELLED C-08
-- Multiplicity: unavailableItems minlength(1) -> skus with 2 SKUs C-30
-- Time: no timer in this context -> no row
-- Redelivery (ZFL: no-op, no event, when already in a target state): confirmation C-01, cancellation C-06 (SENT) / C-08 (FAILED),
--   stock unavailable C-13 (SENT) / C-31 (FAILED); the correlation key (orderId + type) is not in the model (A-02, decided T-05, ADR 0002)
-- Read side: no query endpoint (openapi.yml 0.2.0 paths: {}) -> no row
-- PENDING: no row - initial state set and left within one command; not reachable as a start state
--
-- ASSUMPTIONS: assumption | value used | open question
-- A-01 notificationId in fixtures | NTF-<nnn> (fixture ids only; the service generates a UUIDv7, T-04 decided 2026-11-25) | -
-- A-02 one Notification per orderId + type; redelivery finds it by that key | as listed | decided T-05, ADR 0002
-- A-03 channel: inputs carry only customerEmail, so every notification is EMAIL | 'EMAIL' | decided T-06, ADR 0002
-- A-04 recipient = input customerEmail; customer emails as in orders fixtures (customer@example.com, customer0nn@example.com) | as listed | -
-- A-05 subject / body texts; cancellation reason only in the body (Notification has no reason field; OrderCancelledNotificationSent.reason
--   comes from the input) | as listed | decided T-07, ADR 0002
-- A-06 skus = unavailable SKUs for STOCK_UNAVAILABLE, empty ('{}') for the other types (their inputs carry no SKUs) | as listed | -
-- A-07 failure_reason value | mailbox_unavailable | decided T-07, ADR 0002
-- A-08 enums stored as names (entity @Enumerated(STRING)); the modulith stores the enum id | 'EMAIL', 'SENT', ... | confirmed by the DDL (varchar check)
-- A-09 skus is a varchar array column (List<String> basic mapping, as in the modulith) | ARRAY[...] / '{}' | confirmed by the DDL (varchar(255) array)
-- A-10 FAILED rows use example.com recipients (orders' C-08 e-mail); the failure is stub-driven, not caused by the address | as listed | -
-- Timestamps: created_at = upstream event time + 10 s (C-01 capture 13:06:30, C-06..C-10 order cancelled_at), sent_at / failed_at = created_at + 10 s;
--   off-chain chains and C-13 at 2026-10-14T09:55-09:56, all before "now"

-- Notifications Consumer: notification
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-001', 'CUST-001', 'ORD-001', '{}', 'ORDER_CONFIRMATION', 'EMAIL', 'SENT', 'Your order ORD-001 is confirmed',
        'Your order ORD-001 is confirmed and on its way.',
        'customer@example.com', '2026-09-26T13:06:40Z', '2026-09-26T13:06:50Z', NULL, NULL); -- C-01
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-002', 'CUST-006', 'ORD-006', '{}', 'ORDER_CANCELLED', 'EMAIL', 'SENT', 'Your order ORD-006 was cancelled',
        'Your order ORD-006 was cancelled: RESERVATION_EXPIRED. No payment was captured.',
        'customer006@example.com', '2026-10-14T09:50:16Z', '2026-10-14T09:50:26Z', NULL, NULL); -- C-06
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-003', 'CUST-007', 'ORD-007', '{}', 'ORDER_CANCELLED', 'EMAIL', 'SENT', 'Your order ORD-007 was cancelled',
        'Your order ORD-007 was cancelled: PAYMENT_DECLINED. No payment was captured.',
        'customer007@example.com', '2026-10-14T09:51:17Z', '2026-10-14T09:51:27Z', NULL, NULL); -- C-07
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-004', 'CUST-008', 'ORD-008', '{}', 'ORDER_CANCELLED', 'EMAIL', 'FAILED', 'Your order ORD-008 was cancelled',
        'Your order ORD-008 was cancelled: PAYMENT_RETRY_EXHAUSTED. No payment was captured.',
        'customer008@example.com', '2026-10-14T09:51:38Z', NULL, '2026-10-14T09:51:48Z', 'mailbox_unavailable'); -- C-08
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-005', 'CUST-009', 'ORD-009', '{}', 'ORDER_CANCELLED', 'EMAIL', 'SENT', 'Your order ORD-009 was cancelled',
        'Your order ORD-009 was cancelled: PAYMENT_VOIDED. No payment was captured.',
        'customer009@example.com', '2026-10-14T09:51:49Z', '2026-10-14T09:51:59Z', NULL, NULL); -- C-09
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-006', 'CUST-010', 'ORD-010', '{}', 'ORDER_CANCELLED', 'EMAIL', 'SENT', 'Your order ORD-010 was cancelled',
        'Your order ORD-010 was cancelled: PAYMENT_VOID_FAILED. No payment was captured, but the hold may still show on your statement until it is released.',
        'customer010@example.com', '2026-10-14T09:51:50Z', '2026-10-14T09:52:00Z', NULL, NULL); -- C-10
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-007', 'CUST-013', 'ORD-013', ARRAY['SKU-004'], 'STOCK_UNAVAILABLE', 'EMAIL', 'SENT', 'Items unavailable for order ORD-013',
        'Your order ORD-013 could not be placed: SKU-004 is no longer available.',
        'customer013@example.com', '2026-10-14T09:55:13Z', '2026-10-14T09:55:23Z', NULL, NULL); -- C-13
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-008', 'CUST-030', 'ORD-030', ARRAY['SKU-004','SKU-005'], 'STOCK_UNAVAILABLE', 'EMAIL', 'SENT', 'Items unavailable for order ORD-030',
        'Your order ORD-030 could not be placed: SKU-004, SKU-005 are no longer available.',
        'customer030@example.com', '2026-10-14T09:55:30Z', '2026-10-14T09:55:40Z', NULL, NULL); -- C-30
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-009', 'CUST-031', 'ORD-031', ARRAY['SKU-004'], 'STOCK_UNAVAILABLE', 'EMAIL', 'FAILED', 'Items unavailable for order ORD-031',
        'Your order ORD-031 could not be placed: SKU-004 is no longer available.',
        'customer031@example.com', '2026-10-14T09:55:31Z', NULL, '2026-10-14T09:55:41Z', 'mailbox_unavailable'); -- C-31
INSERT INTO notification (id, version, notification_id, customer_id, order_id, skus, type, channel, status, subject, body, recipient, created_at, sent_at, failed_at, failure_reason)
VALUES (nextval('notification_seq'), 0, 'NTF-010', 'CUST-032', 'ORD-032', '{}', 'ORDER_CONFIRMATION', 'EMAIL', 'FAILED', 'Your order ORD-032 is confirmed',
        'Your order ORD-032 is confirmed and on its way.',
        'customer032@example.com', '2026-10-14T09:56:32Z', NULL, '2026-10-14T09:56:42Z', 'mailbox_unavailable'); -- C-32
