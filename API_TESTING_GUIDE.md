# Testing the Complaint API - Complete Flow

## Step 1: Register a Citizen

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\":\"John Doe\",\"mobileNumber\":\"+919876543210\",\"email\":\"john@example.com\",\"address\":\"123 Street, Bangalore\",\"pinCode\":\"560001\",\"password\":\"SecurePass@123\"}"
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJyb2xlIjoiQ0lUSVpFTiIsInN1YiI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCIsImlhdCI6MTcwNzMwNjAwMCwiZXhwIjoxNzA3MzkyNDAwfQ.abc123...",
    "tokenType": "Bearer",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "email": "john@example.com",
    "role": "CITIZEN"
  },
  "timestamp": "2026-02-07T15:46:00"
}
```

**Save the token from the response!**

---

## Step 2: Login (Alternative to Registration)

If you already registered, you can login to get a fresh token:

```bash
curl -X POST http://localhost:8080/api/auth/citizen/login ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\":\"john@example.com\",\"password\":\"SecurePass@123\"}"
```

---

## Step 3: Create a Complaint (Using the Token)

**Replace `<YOUR_TOKEN>` with the actual token from Step 1 or 2:**

```bash
curl -X POST http://localhost:8080/api/citizen/complaints/create ^
  -H "Authorization: Bearer <YOUR_TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Broken Street Light\",\"description\":\"Street light not working on MG Road\",\"complaintType\":\"STREET_LIGHT\",\"latitude\":12.9716,\"longitude\":77.5946,\"locationText\":\"MG Road, Bangalore\"}"
```

**Example with actual token:**
```bash
curl -X POST http://localhost:8080/api/citizen/complaints/create ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJyb2xlIjoiQ0lUSVpFTiIsInN1YiI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCIsImlhdCI6MTcwNzMwNjAwMCwiZXhwIjoxNzA3MzkyNDAwfQ.abc123..." ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Broken Street Light\",\"description\":\"Street light not working on MG Road\",\"complaintType\":\"STREET_LIGHT\",\"latitude\":12.9716,\"longitude\":77.5946,\"locationText\":\"MG Road, Bangalore\"}"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Complaint created successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "title": "Broken Street Light",
    "description": "Street light not working on MG Road",
    "complaintType": "STREET_LIGHT",
    "status": "PENDING",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "locationText": "MG Road, Bangalore",
    "createdAt": "2026-02-07T15:47:00"
  },
  "timestamp": "2026-02-07T15:47:00"
}
```

---

## Common Issues & Solutions

### ❌ Issue: 403 Forbidden

**Possible Causes:**

1. **Missing or Invalid Token**
   - Make sure you include `Authorization: Bearer <token>` header
   - Token must be from registration or login response

2. **Expired Token**
   - Tokens expire after 24 hours
   - Login again to get a new token

3. **Wrong Role**
   - Only CITIZEN role can create complaints
   - Admin users will get 403 on this endpoint

4. **Malformed Authorization Header**
   - Must be: `Authorization: Bearer <token>`
   - NOT: `Authorization: <token>` (missing "Bearer")

### ❌ Issue: 401 Unauthorized

- Token is invalid or expired
- Login again to get a fresh token

---

## Using Postman

1. **Register/Login:**
   - Method: POST
   - URL: `http://localhost:8080/api/auth/register`
   - Body (JSON):
     ```json
     {
       "fullName": "John Doe",
       "mobileNumber": "+919876543210",
       "email": "john@example.com",
       "address": "123 Street, Bangalore",
       "pinCode": "560001",
       "password": "SecurePass@123"
     }
     ```
   - Copy the `token` from response

2. **Create Complaint:**
   - Method: POST
   - URL: `http://localhost:8080/api/citizen/complaints/create`
   - Headers:
     - `Authorization`: `Bearer <paste_token_here>`
     - `Content-Type`: `application/json`
   - Body (JSON):
     ```json
     {
       "title": "Broken Street Light",
       "description": "Street light not working on MG Road",
       "complaintType": "STREET_LIGHT",
       "latitude": 12.9716,
       "longitude": 77.5946,
       "locationText": "MG Road, Bangalore"
     }
     ```

---

## Valid Complaint Types

- `ROAD_DAMAGE`
- `STREET_LIGHT`
- `GARBAGE_COLLECTION`
- `WATER_SUPPLY`
- `DRAINAGE`
- `ILLEGAL_CONSTRUCTION`
- `NOISE_POLLUTION`
- `PUBLIC_PROPERTY_DAMAGE`
- `OTHER`

---

## Quick Test Script

```bash
# 1. Register and save token
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"fullName\":\"Test User\",\"mobileNumber\":\"+919999999999\",\"email\":\"test@test.com\",\"address\":\"Test Address\",\"pinCode\":\"560001\",\"password\":\"Test@1234\"}" > response.json

# 2. Extract token (manual step - copy from response.json)

# 3. Create complaint with token
curl -X POST http://localhost:8080/api/citizen/complaints/create -H "Authorization: Bearer YOUR_TOKEN_HERE" -H "Content-Type: application/json" -d "{\"title\":\"Test Complaint\",\"description\":\"Test Description\",\"complaintType\":\"OTHER\",\"latitude\":12.9716,\"longitude\":77.5946,\"locationText\":\"Test Location\"}"
```
