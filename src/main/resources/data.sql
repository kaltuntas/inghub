INSERT INTO `customer`
VALUES (1, '2025-01-21 17:16:35', '2025-01-22 18:53:45', 'kayhan', 'altuntas', 1000.00, 340.00);

INSERT INTO `loan`
VALUES (1, '2025-01-22 11:59:35', '2025-01-22 18:08:35', 1, 120.00, 6, 0, 0.10);

INSERT INTO `loan_installment`
VALUES (1, '2025-01-22 11:59:35', '2025-01-22 18:31:19', 1, 20.00, 0.00, '2025-02-01', NULL, 0),
       (2, '2025-01-22 11:59:35', '2025-01-22 18:31:19', 1, 20.00, 0.00, '2025-03-01', NULL, 0),
       (3, '2025-01-22 11:59:35', '2025-01-22 18:31:19', 1, 20.00, 0.00, '2025-04-01', NULL, 0),
       (4, '2025-01-22 11:59:35', '2025-01-22 18:31:55', 1, 20.00, 0.00, '2025-05-01', NULL, 0),
       (5, '2025-01-22 11:59:35', '2025-01-22 18:08:35', 1, 20.00, 0.00, '2025-06-01', NULL, 0),
       (6, '2025-01-22 11:59:35', '2025-01-22 18:08:35', 1, 20.00, 0.00, '2025-07-01', NULL, 0);

INSERT INTO `hotel`
VALUES (1, '2025-01-22 11:59:35', '2025-01-22 18:31:19', 'hotel1', 'Istanbul', '123'),
       (2, '2025-01-22 11:59:35', '2025-01-22 18:31:19', 'hotel1', 'Ankara', '456'),
       (3, '2025-01-22 11:59:35', '2025-01-22 18:31:19', 'hotel3', 'Izmir', '789');

ALTER TABLE customer ALTER COLUMN id RESTART WITH 2;
ALTER TABLE loan ALTER COLUMN id RESTART WITH 2;
ALTER TABLE loan_installment ALTER COLUMN id RESTART WITH 7;
ALTER TABLE hotel ALTER COLUMN id RESTART WITH 4;
