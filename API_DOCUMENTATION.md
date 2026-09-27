# Resource Booking API - Detailed API Documentation

Base URL: `http://localhost:8080`

All endpoints except `/api/auth/login` require JWT Bearer token authentication.

---

## Authentication

### Login
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_ADMIN"]
}
```

**Error Responses:**
- `400 Bad Request` - Missing username/password
- `401 Unauthorized` - Invalid credentials

---

## Resources API

### Get All Available Resources (Paginated)
```
GET /api/resources
```

**Query Parameters:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| page | 0 | Page number (0-indexed) |
| size | 10 | Page size (max 100) |
| sortBy | name | Field to sort by (name, price, createdAt) |
| sortDir | asc | Sort direction (asc, desc) |

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Conference Room A",
      "description": "Large conference room with projector and whiteboard",
      "price": 50.00,
      "isAvailable": true
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 5,
  "totalPages": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

---

### Get Resource by ID
```
GET /api/resources/{id}
```

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Conference Room A",
  "description": "Large conference room with projector and whiteboard",
  "price": 50.00,
  "isAvailable": true
}
```

**Error:** `404 Not Found` - Resource not found

---

### Create Resource (ADMIN only)
```
POST /api/resources
```

**Headers:** 
- `Authorization: Bearer <admin_token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "New Meeting Room",
  "description": "Small meeting room for 4 people",
  "price": 40.00,
  "isAvailable": true
}
```

**Validation:**
- `name`: Required, max 100 chars
- `description`: Optional, max 500 chars
- `price`: Required, must be > 0

**Response (200 OK):**
```json
{
  "id": 6,
  "name": "New Meeting Room",
  "description": "Small meeting room for 4 people",
  "price": 40.00,
  "isAvailable": true
}
```

**Errors:**
- `400 Bad Request` - Validation errors
- `403 Forbidden` - Not admin

---

### Update Resource (ADMIN only)
```
PUT /api/resources/{id}
```

**Headers:** `Authorization: Bearer <admin_token>`, `Content-Type: application/json`

**Request Body:** Same as create (all fields optional except validation)

**Response (200 OK):** Updated resource object

**Errors:** `404 Not Found`, `403 Forbidden`, `400 Bad Request`

---

### Delete Resource (ADMIN only)
```
DELETE /api/resources/{id}
```

**Headers:** `Authorization: Bearer <admin_token>`

**Response:** `204 No Content`

**Errors:** `404 Not Found`, `403 Forbidden`

---

### Search Resources with Filters
```
GET /api/resources/search
```

**Query Parameters:**
| Parameter | Required | Description |
|-----------|----------|-------------|
| page | No | Page number (default: 0) |
| size | No | Page size (default: 10) |
| sortBy | No | Sort field (default: name) |
| sortDir | No | Sort direction (default: asc) |
| name | No | Partial name match (case-insensitive) |
| minPrice | No | Minimum price filter |
| maxPrice | No | Maximum price filter |

**Headers:** `Authorization: Bearer <token>`

**Example:**
```
GET /api/resources/search?name=Conference&minPrice=30&maxPrice=100&page=0&size=5&sortBy=price&sortDir=desc
```

**Response:** Same paginated format as GET /api/resources

---

## Reservations API

### Create Reservation (USER/ADMIN)
```
POST /api/reservations
```

**Headers:** `Authorization: Bearer <token>`, `Content-Type: application/json`

**Request Body:**
```json
{
  "resourceId": 1,
  "startTime": "2025-01-15T10:00:00",
  "endTime": "2025-01-15T12:00:00",
  "totalPrice": 100.00
}
```

**Validation:**
- `resourceId`: Required, must exist
- `startTime`: Required, present or future
- `endTime`: Required, present or future, after startTime
- `totalPrice`: Required, must be > 0

**Business Rules:**
- Resource must be available
- No overlapping reservations for same resource (PENDING/CONFIRMED)
- User ID taken from JWT (not request)

**Response (200 OK):**
```json
{
  "id": 1,
  "resourceId": 1,
  "startTime": "2025-01-15T10:00:00",
  "endTime": "2025-01-15T12:00:00",
  "totalPrice": 100.00,
  "status": "PENDING"
}
```

**Errors:**
- `400 Bad Request` - Validation or business rule violation
- `404 Not Found` - Resource not found
- `409 Conflict` - Time slot already booked

---

### Get My Reservations (USER/ADMIN)
```
GET /api/reservations/my-reservations
```

**Query Parameters:** page, size, sortBy, sortDir (same as resources)

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "resourceId": 1,
      "startTime": "2025-01-15T10:00:00",
      "endTime": "2025-01-15T12:00:00",
      "totalPrice": 100.00,
      "status": "PENDING"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

---

### Get My Reservations by Status (USER/ADMIN)
```
GET /api/reservations/my-reservations/status/{status}
```

**Path Variable:** `status` - PENDING, CONFIRMED, or CANCELLED

**Query Parameters:** page, size, sortBy, sortDir

**Headers:** `Authorization: Bearer <token>`

**Response:** Same paginated format

---

### Get All Reservations (ADMIN only)
```
GET /api/reservations/all
```

**Query Parameters:** page, size, sortBy, sortDir

**Headers:** `Authorization: Bearer <admin_token>`

**Response:** Paginated list of ALL reservations (all users)

---

### Get Reservations by Status (ADMIN only)
```
GET /api/reservations/status/{status}
```

**Path Variable:** `status` - PENDING, CONFIRMED, or CANCELLED

**Query Parameters:** page, size, sortBy, sortDir

**Headers:** `Authorization: Bearer <admin_token>`

**Response:** Paginated list filtered by status

---

### Update Reservation Status (ADMIN only)
```
PUT /api/reservations/{id}/status
```

**Path Variable:** `id` - Reservation ID

**Query Parameter:** `status` - PENDING, CONFIRMED, or CANCELLED

**Headers:** `Authorization: Bearer <admin_token>`

**Response (200 OK):** Updated reservation with new status

**Errors:** `404 Not Found`, `403 Forbidden`

---

### Cancel Reservation (USER/ADMIN)
```
PUT /api/reservations/{id}/cancel
```

**Path Variable:** `id` - Reservation ID

**Headers:** `Authorization: Bearer <token>`

**Rules:**
- Users can only cancel their own reservations
- Admins can cancel any reservation
- Already cancelled reservations cannot be cancelled again

**Response:** `204 No Content`

**Errors:**
- `403 Forbidden` - Not owner and not admin
- `404 Not Found` - Reservation not found
- `400 Bad Request` - Already cancelled

---

## Reservation Status Flow

```
PENDING ──► CONFIRMED
   │           │
   ▼           ▼
CANCELLED ◄───┘
```

- New reservations start as **PENDING**
- Admins can confirm: **PENDING → CONFIRMED**
- Users/Admins can cancel: **PENDING → CANCELLED** or **CONFIRMED → CANCELLED**
- Cancelled reservations cannot be changed

---

## Error Response Format

### Validation Errors (400)
```json
{
  "fieldName": "error message",
  "startTime": "Start time must be in the present or future"
}
```

### Generic Errors (401, 403, 404, 500)
```json
{
  "error": "Error description"
}
```

---

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success (GET, PUT) |
| 201 | Created (not used, returns 200) |
| 204 | No Content (DELETE, cancel) |
| 400 | Bad Request / Validation Error |
| 401 | Unauthorized (invalid/missing token) |
| 403 | Forbidden (insufficient role) |
| 404 | Not Found |
| 409 | Conflict (overlapping reservation) |
| 500 | Internal Server Error |

---

## Swagger/OpenAPI

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec at:
```
http://localhost:8080/v3/api-docs
```