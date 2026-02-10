# 🚀 Start the Application

## Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL running

## Setup (One-time)

### 1. Create Database
In pgAdmin or PostgreSQL:
```sql
CREATE DATABASE civic_complaint_db;
```

### 2. Run Schema
Copy and execute the SQL from:
`C:\Users\KabirBagalkot\.gemini\antigravity\brain\b361a769-8680-4aba-b4a3-64a00b27fb7f\civic_complaint_schema.sql`

### 3. Update Password (if needed)
Edit `src/main/resources/application.yml`:
```yaml
datasource:
  password: your_postgres_password
```

## Run

```bash
cd C:\Users\KabirBagalkot\.gemini\antigravity\scratch\civic-complaint-api
mvn spring-boot:run
```

**App URL:** `http://localhost:8080/api`

## Test

Register a citizen:
```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\":\"John Doe\",\"mobileNumber\":\"+919876543210\",\"email\":\"john@example.com\",\"address\":\"123 Street\",\"pinCode\":\"560001\",\"password\":\"SecurePass@123\"}"
```

Login:
```bash
curl -X POST http://localhost:8080/api/auth/citizen/login ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\":\"john@example.com\",\"password\":\"SecurePass@123\"}"
```

---

**That's all you need!**
