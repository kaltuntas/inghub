CREATE TABLE IF NOT EXISTS customer
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    idate             TIMESTAMP      NOT NULL,
    udate             TIMESTAMP DEFAULT NULL,
    name              VARCHAR(50)    NOT NULL,
    surname           VARCHAR(50)    NOT NULL,
    credit_limit      DECIMAL(15, 2) NOT NULL,
    used_credit_limit DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS loan
(
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    idate                 TIMESTAMP      NOT NULL,
    udate                 TIMESTAMP DEFAULT NULL,
    customer_id           BIGINT         NOT NULL,
    loan_amount           DECIMAL(15, 2) NOT NULL,
    number_of_installment SMALLINT       NOT NULL,
    is_paid               BOOLEAN        NOT NULL,
    interest_rate         DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE TABLE IF NOT EXISTS loan_installment
(
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    idate        TIMESTAMP      NOT NULL,
    udate        TIMESTAMP DEFAULT NULL,
    loan_id      BIGINT         NOT NULL,
    amount       DECIMAL(15, 2) NOT NULL,
    paid_amount  DECIMAL(15, 2) NOT NULL,
    due_date     DATE           NOT NULL,
    payment_date TIMESTAMP DEFAULT NULL,
    is_paid      BOOLEAN        NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (loan_id) REFERENCES loan (id)
);

CREATE TABLE IF NOT EXISTS hotel
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    idate        TIMESTAMP   NOT NULL,
    udate        TIMESTAMP DEFAULT NULL,
    name         VARCHAR(50) NOT NULL,
    city         VARCHAR(50) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);
