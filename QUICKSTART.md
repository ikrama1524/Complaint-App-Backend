# 🚀 How to Start the App

## 1. Setup Database (pgAdmin)
1. Create database: `civic_complaint_db`
2. Run the SQL schema from the artifacts folder

## 2. Configure (Optional)
Edit `application.yml` if your PostgreSQL password is not `postgres`

## 3. Run
```bash
mvn spring-boot:run
```

**That's it!** App runs on `http://localhost:8080/api`

---

## Test It
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"fullName\":\"John Doe\",\"mobileNumber\":\"+919876543210\",\"email\":\"john@example.com\",\"address\":\"123 Street\",\"pinCode\":\"560001\",\"password\":\"SecurePass@123\"}"
```

---

**Environment Variables (Optional):**
- `DB_USERNAME` - Database user (default: postgres)
- `DB_PASSWORD` - Database password (default: postgres)
- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: civic_complaint_db)
