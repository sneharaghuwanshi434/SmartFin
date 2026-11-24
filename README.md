# SmartFin – Full Stack Banking & Transaction Management System

A full-stack banking simulation application built using **Java, Spring Boot, PostgreSQL, React, and Tailwind CSS**. SmartFin enables users to manage bank accounts, perform transactions, track financial activity, and download transaction receipts through a responsive web interface.

> **Note:** SmartFin is an educational banking simulator. It does not connect to real banking systems or process real money.

## Features

- User registration and login
- JWT-based authentication and authorization
- Secure password hashing using BCrypt
- Bank account creation and management
- Deposit and withdrawal operations
- Account-to-account fund transfers
- Transaction history with filters and pagination
- Date-range transaction filtering
- Transaction analytics and visualizations
- Downloadable PDF transaction receipts
- Idempotency protection for transaction requests
- Responsive dashboard for desktop and mobile

## Tech Stack

### Backend
- Java
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- JWT Authentication
- Maven
- OpenPDF

### Frontend
- React
- JavaScript
- Tailwind CSS
- Axios
- React Router
- Recharts
- Lucide React
- Vite

## Project Structure

```text
SmartFin/
├── smartfin/                  # Spring Boot backend
│   ├── src/
│   └── pom.xml
│
├── smartfin-frontend/         # React frontend
│   ├── src/
│   ├── public/
│   └── package.json
│
└── README.md
```

## Security Features

- JWT-based stateless authentication
- BCrypt password hashing
- Protected API endpoints
- User-specific account and transaction access
- Environment variables for sensitive configuration
- Transaction validation and idempotency handling

## Getting Started

### Prerequisites

- Java (compatible with the project's Spring Boot version)
- Maven
- Node.js and npm
- PostgreSQL

### 1. Clone the Repository

```bash
git clone https://github.com/sneharaghuwanshi434/SmartFin.git
cd SmartFin
```

### 2. Configure the Database

Create a PostgreSQL database named:

```sql
CREATE DATABASE smartfin_db;
```

### 3. Environment Configuration

Before running the backend, configure the following environment variables:

| Variable | Description |
|---|---|
| `DB_PASSWORD` | Password for your local PostgreSQL database |
| `SMARTFIN_JWT_SECRET` | Secret key used for JWT token signing |

Set these variables in your system environment.

**Windows PowerShell example:**

```powershell
$env:DB_PASSWORD="your_postgresql_password"
$env:SMARTFIN_JWT_SECRET="your_jwt_secret"
```

Replace the example values with your actual credentials. These commands apply only to the current PowerShell session.

**Important:** Never hardcode passwords or JWT secrets in source code or commit sensitive credentials to GitHub.

### 4. Run the Backend

Open a terminal:

```bash
cd smartfin
mvn spring-boot:run
```

Backend runs at:

`http://localhost:8080`

### 5. Run the Frontend

Open another terminal from the project root:

```bash
cd smartfin-frontend
npm install
npm run dev
```

Frontend runs at:

`http://localhost:5173`

## API Endpoints

| Module | Endpoint | Method |
|---|---|---|
| Authentication | `/api/auth/register` | POST |
| Authentication | `/api/auth/login` | POST |
| Profile | `/api/auth/profile` | GET |
| Accounts | `/api/accounts` | GET, POST |
| Account Details | `/api/accounts/{accountNumber}` | GET |
| Deposit | `/api/transactions/deposit` | POST |
| Withdrawal | `/api/transactions/withdraw` | POST |
| Transfer | `/api/transactions/transfer` | POST |
| Transaction History | `/api/transactions/history` | GET |
| Transaction Analytics | `/api/transactions/analytics` | GET |
| PDF Receipt | `/api/transactions/{referenceNumber}/receipt` | GET |

## Author

**Sneha Raghuwanshi**

B.Tech – Electronics & Communication Engineering  
Oriental Institute of Science and Technology, Bhopal

[GitHub](https://github.com/sneharaghuwanshi434)

---

*SmartFin – Simplifying personal banking through software.*
