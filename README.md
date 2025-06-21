
# ING Credit Module

## 📝 Overview
The **ING Credit Module** is a software solution designed to manage customer loans. Key functionalities include:

- Creating loans for customers
- Listing existing loans
- Processing loan payments
- Listing loan installments

This project is built with **Java** and **Spring Boot** and uses **Maven** for dependency management and builds.

---

## ✨ Features

### Loan Management
- **Loan Creation**: Create loans for customers with detailed information.
- **Loan Listing**: Retrieve a list of loans filtered by `loanAmount`, `installmentAmount`, and `isPaid` parameters, with sorting and pagination options.
- **Loan Payments**: Process payments for existing loans.
- **Loan Installment Listing**: Retrieve detailed installments for a loan.

### Additional Features
- **API Documentation**: Integrated with Swagger UI for exploring and testing APIs.
- **Authentication**: Secured via basic authentication (username and password).
- **Database**: Uses an in-memory **H2 database**, with tables and dummy data auto-created on startup.

---

## 🚀 Installation

Follow these steps to set up the ING Credit Module on your local machine:

### Prerequisites
- **Java Development Kit (JDK)** 17 or later
- **Maven** 3.6 or later

### Steps

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd inghub-credit
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```  

The application will now be accessible at: `http://localhost:8080`.

---

## 🔧 Usage

### Authentication
All APIs are secured with basic authentication. Use the `--user` flag in `curl` commands to pass the credentials (`user` and `12345`).

## 📖 API Endpoints

### Loan Management
- `GET /api/v1/loans/` - List loans by customer
- `GET /api/v1/loans/{loanId}/installments` - List installments for a loan
- `POST /api/v1/loans` - Create a loan
- `POST /api/v1/loans/pay` - Make a payment for a loan

---

### Examples

#### 1. Create a Loan
```bash
curl -X POST http://localhost:8080/api/v1/loans -H "Content-Type: application/json" -d '{"customerId":1, "loanAmount":1, "numberOfInstallment":6, "interestRate":0.1}' --user user:12345
```

#### 2. List Loans
```bash
curl -X GET 'http://localhost:8080/api/v1/loans?customerId=1' --user user:12345
```
```bash
curl -X GET 'http://localhost:8080/api/v1/loans?customerId=1&isPaid=false' \
--user user:12345
```

```bash
curl -X GET 'http://localhost:8080/api/v1/loans?customerId=1&isPaid=false&pageSize=20&pageNumber=1&sort=+numberOfInstallment' \
--user user:12345
```

#### 3. Make a Payment
```bash
curl -X POST http://localhost:8080/api/v1/loans/pay -H "Content-Type: application/json" -d '{"loanId":20, "paidAmount":10}' --user user:12345
```

#### 4. List Loan Installments
```bash
curl -X GET 'http://localhost:8080/api/v1/loans/20/installments' --user user:12345
```

---



## 🖥️ API Documentation

Swagger UI is integrated for interactive API documentation. Once the application is running, visit:

[Swagger UI](http://localhost:8080/swagger-ui.html)

---

## 🗄️ Database Schema

The ING Credit Module uses an H2 in-memory database. Below are the tables created on startup:

### 1. `customer` Table
```sql
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
```

### 2. `loan` Table
```sql
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
```

### 3. `loan_installment` Table
```sql
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
```

---

## 📂 Project Structure

```plaintext
src/main/java
  |-- com.inghub.credit
       |-- config       # DB and Security Configuration
       |-- constant     # Constants
       |-- controller   # API controllers
       |-- domain       # Entity classes
       |-- exception    # Exception handling
       |-- repository   # Data access layer
       |-- request      # Request and DTO classes
       |-- response     # Response and DTO classes
       |-- service      # Business logic layer
```
--- 
