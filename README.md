# IBSBMS – Islami Bank Share & Bond Management System

IBSBMS (Islami Bank Share & Bond Management System) is a web-based shareholder management system developed as part of an internship project.

The system manages shareholder account information and controls shareholder **Create** and **Modify** operations through a **Maker–Checker approval workflow**.

---

## Features

* Shareholder account listing
* Search shareholder accounts by Folio/BO number
* Create new shareholder records
* Modify existing shareholder information
* Maker–Checker approval workflow
* Checker approval, rejection, and return for modification
* Maker view of submitted requests
* Returned request resubmission
* Approval and rejection history
* Business audit tracking
* Role-based access control
* Dashboard with database-driven statistics
* Read-only shareholder details view

---

## Technology Stack

### Backend

* Java 21
* Spring Boot 4.1.1
* Spring MVC
* Spring Data JPA
* Hibernate
* Maven
* Spring Security

### Frontend

* HTML5
* CSS3
* Thymeleaf
* Bootstrap 5.3.3

### Database

* Oracle Database
* Oracle JDBC Driver (ojdbc17)

### Development Tools

* IntelliJ IDEA
* Git
* GitHub

---

## System Architecture

The application follows a layered architecture:

```text
+-----------------------------+
|          Browser            |
|     Thymeleaf + Bootstrap   |
+-------------+---------------+
              |
              v
+-----------------------------+
|        Controller Layer     |
|      Spring MVC Controllers |
+-------------+---------------+
              |
              v
+-----------------------------+
|         Service Layer       |
|    Business Logic / Workflow|
+-------------+---------------+
              |
              v
+-----------------------------+
|       Repository Layer      |
|       Spring Data JPA       |
+-------------+---------------+
              |
              v
+-----------------------------+
|       Oracle Database       |
+-----------------------------+
```

---

## Maker–Checker Workflow

The system uses a Maker–Checker workflow to control shareholder changes.

```text
             MAKER
               |
               | Create / Modify
               v
        Submit Request
               |
               v
       PENDING_CHECKER
               |
        +------+------+
        |             |
        v             v
     CHECKER      CHECKER
     Approve      Reject
        |             |
        v             v
    APPROVED       REJECTED
```

A Checker can also return a request for modification:

```text
Maker
  |
  v
Submit
  |
  v
Pending Checker
  |
  v
Return for Modification
  |
  v
Maker modifies request
  |
  v
Resubmit
  |
  v
Pending Checker
```

---

## User Roles

### Maker

The Maker can:

* View shareholder accounts
* Create shareholder requests
* Modify shareholder information
* Submit requests for approval
* View their submitted requests
* View returned requests
* Modify and resubmit returned requests

### Checker

The Checker can:

* View shareholder accounts
* View pending approval requests
* Review shareholder changes
* Approve requests
* Reject requests
* Return requests for modification
* View rejected requests
* View returned requests

---

## Database Design

The main shareholder tables are:

```text
T_ACCOUNT_SHARE
        |
        +---- T_ADDRESS_SHARE
        |
        +---- T_BANKINFO_SHARE
```

Workflow-related tables include:

```text
T_SHAREHOLDER_CHANGE_REQUEST
              |
              v
      T_APPROVAL_REQUEST
              |
              v
       T_APPROVAL_ACTION
              |
              v
       T_BUSINESS_AUDIT
```

### Main Tables

| Table                          | Purpose                                  |
| ------------------------------ | ---------------------------------------- |
| `T_ACCOUNT_SHARE`              | Stores shareholder account information   |
| `T_ADDRESS_SHARE`              | Stores shareholder address information   |
| `T_BANKINFO_SHARE`             | Stores shareholder bank information      |
| `T_SHAREHOLDER_CHANGE_REQUEST` | Stores proposed shareholder changes      |
| `T_APPROVAL_REQUEST`           | Stores approval workflow state           |
| `T_APPROVAL_ACTION`            | Stores approval/rejection/return history |
| `T_BUSINESS_AUDIT`             | Stores business audit information        |

---

## Shareholder Operations

### Create

The Maker enters:

* Basic shareholder information
* Address information
* Optional bank information

The system creates a change request and sends it to the Checker.

The shareholder master record is finalized after approval.

### Modify

The Maker selects an existing approved shareholder and edits permitted information.

The system stores:

* Previous values
* New values
* Maker information
* Change request information

The changes are applied to the master data only after Checker approval.

Financial/shareholding values such as shares, suspense, bonus, and balance are not modified through the shareholder modification workflow.

---

## Project Structure

The main application structure follows the standard Spring Boot organization:

```text
src/
└── main/
    ├── java/
    │   └── com.example.ibsbms/
    │       ├── config/
    │       ├── controller/
    │       ├── dto/
    │       ├── entity/
    │       ├── enums/
    │       ├── repository/
    │       └── service/
    │
    └── resources/
        ├── static/
        │   └── css/
        ├── templates/
        │   ├── approval/
        │   ├── shareholder/
        │   └── fragments/
        └── application.properties
```

---

## Security

Spring Security is used for authentication and role-based authorization.

The system currently uses two roles:

```text
MAKER
CHECKER
```

Examples:

```text
/shareholders/create       → MAKER
/shareholders/*/edit       → MAKER
/shareholders/returned/**  → MAKER

/approvals/**              → CHECKER

/shareholders              → MAKER + CHECKER
```

---

## Dashboard

The dashboard displays database-driven statistics including:

* Total Shareholders
* Active Accounts
* Pending Approvals
* Dormant Accounts

The values are retrieved from Oracle Database through the repository and service layers rather than being hardcoded.

---

## Running the Application

### Prerequisites

Make sure the following are installed:

* Java 21
* Maven
* Oracle Database access
* Git

### Configure Database

Update the Oracle database configuration in:

```text
src/main/resources/application.properties
```

The application requires access to the Oracle database used by the IBSBMS system.

### Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

---

## Development Notes

The application follows a separation of responsibilities:

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
Oracle Database
```

Controllers handle HTTP requests, services contain business logic, and repositories handle database access.

Workflow actions are recorded separately from the final shareholder master data to maintain an approval and audit trail.

---

## Project Purpose

This project was developed as part of an internship to gain practical experience in:

* Spring Boot application development
* Enterprise web application architecture
* Oracle database integration
* Spring Data JPA
* Role-based security
* Maker–Checker workflow implementation
* Thymeleaf-based UI development
* Git and GitHub-based development
* Business audit and approval processes

---

## Status

**Project:** IBSBMS – Islami Bank Share & Bond Management System

**Type:** Internship Project

**Technology:** Java / Spring Boot / Thymeleaf / Oracle Database

```


