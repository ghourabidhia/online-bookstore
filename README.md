# BNP Online Bookstore - Backend

## Overview

Online Bookstore REST API developed with:

* Java 21
* Spring Boot 4
* Spring Security JWT
* Spring Data JPA
* PostgreSQL
* Swagger / OpenAPI
* JUnit 5 & Mockito

---

# Prerequisites

Before starting, make sure the following tools are installed:

* Java 21
* Maven 3.9+
* PostgreSQL 14+
* Git

Verify installations:

```bash
java -version
mvn -version
psql --version
```

---

# Clone the Project

```bash
git clone <repository-url>
cd online-bookstore
```

---

# Create PostgreSQL Database

Connect to PostgreSQL:

```bash
psql -U postgres
```

Create the database:

```sql
CREATE DATABASE bookstore;
```

Verify:

```sql
\l
```

You should see:

```text
bookstore
```

Exit PostgreSQL:

```sql
\q
```

---

# Configure Database Connection

Open:

```text
src/main/resources/application.properties
```

Update the database configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookstore
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Replace `your_password` with your PostgreSQL password.

---

# Application Properties Reference

All keys found in `src/main/resources/application.properties`:

| Property | Default value | Description |
|---|---|---|
| `spring.application.name` | `online-bookstore` | Application name used in logs |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/bookstore` | PostgreSQL connection URL |
| `spring.datasource.username` | `postgres` | Database username |
| `spring.datasource.password` | _(set yours)_ | Database password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-creates or updates tables from entities on startup |
| `spring.jpa.show-sql` | `true` | Prints every SQL query to the console |
| `spring.jpa.properties.hibernate.format_sql` | `true` | Pretty-prints SQL queries |
| `spring.sql.init.mode` | `never` | Controls whether `data.sql` runs on startup (`always` / `never`) |
| `jwt.secret` | _(long key)_ | Secret key used to sign JWT tokens — must be at least 32 characters |
| `jwt.expiration` | `86400000` | Token validity in milliseconds (86 400 000 ms = 24 hours) |

---

# Initial Data Loading

The project contains a:

```text
src/main/resources/data.sql
```

file that inserts 36 sample books.

For the first application startup:

```properties
spring.sql.init.mode=always
```

This will populate the database with sample books.

After the first successful startup, change it to:

```properties
spring.sql.init.mode=never
```

to prevent re-executing the initialization script.

---

# Build the Application

```bash
mvn clean install
```

Expected result:

```text
BUILD SUCCESS
```

---

# Run the Application

```bash
mvn spring-boot:run
```

or

```bash
java -jar target/online-bookstore-1.0.0.jar
```

Expected log:

```text
Started OnlineBookstoreApplication
```

---

# Access the Application

Base URL:

```text
http://localhost:8080
```

---

# Swagger Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI Documentation:

```text
http://localhost:8080/v3/api-docs
```

---

# API Endpoints

## Public endpoints (no token required)

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `POST` | `/api/auth/register` | Create a new account | `201 Created` |
| `POST` | `/api/auth/login` | Log in and receive a JWT token | `200 OK` |
| `GET` | `/api/books` | Get a paginated list of books | `200 OK` |

Books support pagination and sorting via query parameters:

```
GET /api/books?page=0&size=12&sort=title
```

## Protected endpoints (JWT required)

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/cart` | Get the current user's cart | `200 OK` |
| `POST` | `/api/cart/items` | Add a book to the cart | `201 Created` |
| `PUT` | `/api/cart/items/{id}` | Update the quantity of a cart item | `204 No Content` |
| `DELETE` | `/api/cart/items/{id}` | Remove an item from the cart | `204 No Content` |
| `POST` | `/api/orders` | Place an order from the current cart | `201 Created` |
| `GET` | `/api/orders` | Get all orders for the current user (newest first) | `200 OK` |

---

# Request / Response Examples

## Authentication

### Register — `POST /api/auth/register`

Response `201 Created`:

### Login — `POST /api/auth/login`


Response `200 OK`:



---

## Books

### Get books — `GET /api/books?page=0&size=12&sort=title`

Response `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "price": 45.90,
      "stock": 50
    }
  ],
  "totalElements": 36,
  "totalPages": 3,
  "number": 0,
  "size": 12,
  "first": true,
  "last": false
}
```

The `stock` field is `0` when a book is out of stock.

---

## Cart

### Add book to cart — `POST /api/cart/items`

Request:

```json
{
  "bookId": 1,
  "quantity": 2
}
```

Validation rules:
- `bookId` — required, must not be null
- `quantity` — required, minimum value is 1

Response `201 Created` (no body).

---

### Update quantity — `PUT /api/cart/items/{id}`

Request:

```json
{
  "quantity": 3
}
```

Validation rules:
- `quantity` — required, minimum value is 1

Response `204 No Content` (no body).

---

### Get cart — `GET /api/cart`

Response `200 OK`:

```json
{
  "items": [
    {
      "id": 5,
      "bookTitle": "Clean Code",
      "quantity": 2,
      "price": 45.90
    }
  ],
  "totalPrice": 91.80
}
```

---

## Orders

### Place order — `POST /api/orders`

No request body. The order is built from the current user's cart.

Response `201 Created`:

```json
{
  "id": 12,
  "totalPrice": 91.80,
  "orderDate": "2026-06-24T10:30:00",
  "items": [
    {
      "bookTitle": "Clean Code",
      "quantity": 2,
      "price": 45.90
    }
  ]
}
```

The cart is cleared automatically after the order is placed. Stock is reduced for each book. The price per item is locked at the value at the time of purchase.

---

### Get order history — `GET /api/orders`

Response `200 OK`:

```json
[
  {
    "id": 12,
    "totalPrice": 91.80,
    "orderDate": "2026-06-24T10:30:00",
    "items": [
      {
        "bookTitle": "Clean Code",
        "quantity": 2,
        "price": 45.90
      }
    ]
  }
]
```

---


## Using JWT in Swagger

1. Login using `POST /api/auth/login`
2. Copy the JWT token from the response
3. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`
4. Click **Authorize** (top right)
5. Enter:

```text
Bearer YOUR_TOKEN
```

Example:

```text
Bearer eyJhbGciOiJIUzM4NCJ9...
```

6. Click **Authorize**
7. All protected endpoints are now available

Token expiry: **24 hours** (configurable via `jwt.expiration`).

---

# Error Handling

All errors are caught by `GlobalExceptionHandler` and returned in a consistent format.

## Standard error response

Most errors return this structure:

```json
{
  "timestamp": "2026-06-24T10:30:00",
  "status": 404,
  "message": "Book not found",
  "path": "/api/cart/items"
}
```

## Validation error response

Input validation failures return a field-level map instead:

```json
{
  "email": "Email format is invalid",
  "password": "Password must contain minimum 8 characters"
}
```

## Error codes reference

| HTTP status | Exception | When it happens |
|-------------|-----------|-----------------|
| `400 Bad Request` | `BusinessException` | Business rule violation — duplicate email, empty cart, insufficient stock |
| `400 Bad Request` | `MethodArgumentNotValidException` | Request body fails `@Valid` constraints |
| `401 Unauthorized` | `UnauthorizedException` | Request reaches a protected endpoint with no valid token |
| `403 Forbidden` | Spring Security | Token is missing entirely on a protected route |
| `404 Not Found` | `ResourceNotFoundException` | Requested resource (book, cart item, user) does not exist |

---

# Main Features

## Books

* Get all books (paginated, sorted by title)
* Stock level included in each book response

## Shopping Cart

* Add book to cart (merges quantity if book already in cart)
* Update quantity of an existing cart item
* Remove item from cart
* View full cart with total price

## Orders

* Place order from current cart contents
* Validate stock before confirming
* Reduce book stock atomically
* Calculate and lock in total price at purchase time
* Clear cart automatically after order is placed
* Retrieve full order history (newest first)

## Security

* User registration with BCrypt password hashing
* JWT login (token valid 24 hours)
* Stateless authentication — no sessions, no cookies
* Public routes: `/api/auth/**`, `/api/books/**`, Swagger UI
* Protected routes: `/api/cart/**`, `/api/orders/**`

---

# Project Architecture

```text
src/main/java/com/bnp/bookstore/

├── controllers/     REST endpoints — AuthController, BookController, CartController, OrderController
├── services/        Business logic interfaces + impl/ subdirectory with implementations
├── repositories/    Spring Data JPA interfaces
├── entities/        JPA entities — User, Book, CartItem, Order, OrderItem, Role (enum)
├── dto/
│   ├── request/     RegisterRequest, LoginRequest, AddCartItemRequest, UpdateCartItemRequest
│   └── response/    AuthResponse, BookResponse, CartResponse, CartItemResponse,
│                    OrderResponse, OrderItemResponse
├── mapper/          MapStruct mappers — BookMapper, UserMapper, OrderMapper
├── security/        JwtService, JwtAuthenticationFilter, SecurityConfig, CustomUserDetailsService
├── exception/       GlobalExceptionHandler, ResourceNotFoundException, BusinessException,
│                    UnauthorizedException, ErrorResponse
└── config/          AppConfig (PasswordEncoder bean), OpenApiConfig (Swagger bearer auth)
```

Architecture principles:

* Strict layered architecture: Controller → Service (interface + impl) → Repository → Entity
* DTOs mapped via MapStruct — never exposed between layers
* SOLID, Clean Code
* Global Exception Handling
* Transactional order placement with automatic rollback on failure
* JWT Authentication
* OpenAPI / Swagger documentation
* Unit & Integration Testing

---

# Run Tests

Execute all tests:

```bash
mvn test
```

Generate coverage report:

```bash
mvn verify
```

### Test strategy

* **Controller layer** — `@WebMvcTest` with `MockMvc` and `@MockitoBean` for services.
* **Service layer** — `@ExtendWith(MockitoExtension.class)` with `@Mock` and `@InjectMocks`. No Spring context loaded.
* **Repository layer** — `@DataJpaTest` against H2. Only repositories with custom query methods are tested.
