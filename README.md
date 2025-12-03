# Auth Server - Microservicio OAuth2.0 con JWT

Microservicio de autenticación y autorización construido con **Spring Boot 3.1.6**, implementando **OAuth2.0** como Authorization Server para otros microservicios.

## 🎯 Características Principales

- ✅ **Autenticación OAuth2.0** con JWT (JSON Web Tokens)
- ✅ **Access Tokens** y **Refresh Tokens** con expiración configurable (15 min / 7 días)
- ✅ **Gestión de Usuarios y Roles** con persistencia en BD (PostgreSQL/H2)
- ✅ **5 Endpoints REST** estándar para autenticación
- ✅ **Respuestas Estandarizadas** con `StandardResponse<T>`
- ✅ **Solicitudes Validadas** con `StandardRequest<T>`
- ✅ **Manejo de Errores Centralizado** con `ErrorResponse` y `GlobalExceptionHandler`
- ✅ **Seguridad** con BCrypt para hashing de contraseñas (2^12 rounds)
- ✅ **Filtro JWT** para validación automática en cada request
- ✅ **Roles Basados en Acceso (RBAC)** con autorización por rol
- ✅ **Logging Completo** con SLF4J

## 📋 Endpoints REST

### 1️⃣ POST `/auth/register` - Registro de Usuario
Registra un nuevo usuario en el sistema.

**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "User registered successfully",
  "status": "SUCCESS",
  "timestamp": 1701662400000,
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "active": true,
    "roles": ["USER"],
    "createdAt": "2025-12-03T23:30:00",
    "updatedAt": "2025-12-03T23:30:00"
  }
}
```

---

### 2️⃣ POST `/auth/login` - Autenticación
Inicia sesión y obtiene tokens JWT.

**Request:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Login successful",
  "status": "SUCCESS",
  "timestamp": 1701662400000,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwic3ViIjoiam9obmRvZSIsImVtYWlsIjoiam9obkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlVTRVIiXSwiaWF0IjoxNzAxNjYyNDAwLCJleHAiOjE3MDE2NjMzMDB9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG5kb2UiLCJpYXQiOjE3MDE2NjI0MDAsImV4cCI6MTcwMjI2NzIwMH0...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "scope": "read write"
  }
}
```

---

### 3️⃣ POST `/auth/refresh` - Renovar Token
Genera un nuevo access token usando el refresh token.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Token refreshed successfully",
  "status": "SUCCESS",
  "timestamp": 1701662400000,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwic3ViIjoiam9obmRvZSIsImVtYWlsIjoiam9obkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlVTRVIiXSwiaWF0IjoxNzAxNjY0MDAwLCJleHAiOjE3MDE2NjQ5MDB9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "scope": "read write"
  }
}
```

---

### 4️⃣ POST `/auth/logout` - Cerrar Sesión
Revoca el refresh token y cierra la sesión.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Logout successful",
  "status": "SUCCESS",
  "timestamp": 1701662400000,
  "data": null
}
```

---

### 5️⃣ GET `/auth/userinfo` - Información del Usuario
Obtiene la información del usuario autenticado.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "User info retrieved successfully",
  "status": "SUCCESS",
  "timestamp": 1701662400000,
  "data": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "active": true,
    "roles": ["USER"],
    "createdAt": "2025-12-03T23:30:00",
    "updatedAt": "2025-12-03T23:30:00"
  }
}
```

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    Cliente / Microservicio                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP Request
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  JwtAuthenticationFilter                     │
│         (Valida JWT en header Authorization)                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    AuthController                            │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────┐ │
│  │ /auth/register  │  │ /auth/login      │  │ /auth/refresh
│  │ /auth/logout    │  │ /auth/userinfo   │  │             │ │
│  └─────────────────┘  └──────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      AuthService                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │ - Validar credenciales                                  │ │
│  │ - Generar JWT tokens                                    │ │
│  │ - Gestionar refresh tokens                              │ │
│  │ - Mapeo de usuarios a DTOs                              │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│            Repositories & JPARepositories                    │
│  ┌──────────────────┐ ┌──────────────────┐ ┌─────────────┐  │
│  │ UserRepository   │ │ RoleRepository   │ │ RefreshToken│  │
│  └──────────────────┘ └──────────────────┘ └─────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│          Base de Datos (PostgreSQL / H2)                    │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐ │
│  │ users        │ │ roles        │ │ user_roles           │ │
│  │ - id         │ │ - id         │ │ - user_id (FK)       │ │
│  │ - username   │ │ - name       │ │ - role_id (FK)       │ │
│  │ - email      │ │ - description│ │                      │ │
│  │ - password   │ └──────────────┘ └──────────────────────┘ │
│  │ - firstName  │                                            │
│  │ - lastName   │    ┌──────────────────────────┐           │
│  │ - active     │    │ refresh_tokens           │           │
│  │ - createdAt  │    │ - id                     │           │
│  │ - updatedAt  │    │ - token                  │           │
│  └──────────────┘    │ - user_id (FK)           │           │
│                      │ - expiryDate             │           │
│                      │ - revoked                │           │
│                      │ - createdAt              │           │
│                      └──────────────────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

## 🔐 Flujo de Seguridad

### 1. Registro
```
Usuario → POST /auth/register
         → Validar datos
         → Verificar username/email únicos
         → Encriptar contraseña (BCrypt)
         → Crear usuario con rol "USER"
         → Retornar UserInfoResponse
```

### 2. Login
```
Usuario → POST /auth/login
        → Validar credenciales
        → Generar JWT accessToken (15 min)
        → Generar JWT refreshToken (7 días)
        → Guardar refreshToken en BD
        → Retornar TokenResponse
```

### 3. Autenticación en Request
```
Cliente → GET /auth/userinfo + Bearer Token
       → JwtAuthenticationFilter extrae token
       → JwtTokenProvider valida firma
       → Obtiene claims (username, roles)
       → Carga usuario del token
       → Establece SecurityContext
       → Continúa al controlador
```

### 4. Refresh Token
```
Cliente → POST /auth/refresh + refreshToken
       → Validar que token existe en BD
       → Validar que no esté revocado
       → Validar fecha expiración
       → Generar nuevo accessToken
       → Retornar nuevo token
```

### 5. Logout
```
Usuario → POST /auth/logout + refreshToken
       → Marcar refreshToken como revoked=true
       → Guardar en BD
       → Cliente descarta tokens
       → Siguiente request sin token → 401
```

## 📦 DTOs Estándar

### StandardRequest<T>
```java
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1701662400000,
  "data": { /* Payload específico */ }
}
```

### StandardResponse<T>
```java
{
  "code": 200,
  "message": "Operación exitosa",
  "status": "SUCCESS",
  "timestamp": 1701662400000,
  "data": { /* Payload específico */ },
  "path": "/api/v1/auth/login"
}
```

### ErrorResponse
```java
{
  "code": 400,
  "message": "Validación fallida",
  "status": "ERROR",
  "timestamp": 1701662400000,
  "path": "/api/v1/auth/login",
  "exception": "MethodArgumentNotValidException",
  "validationErrors": {
    "email": "Email format is invalid",
    "password": "Password must be at least 8 characters"
  }
}
```

## ⚙️ Configuración

### `application.yml`

```yaml
spring:
  application:
    name: auth-server
  jpa:
    hibernate:
      ddl-auto: update  # create-drop, validate, update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  datasource:
    url: jdbc:postgresql://localhost:5432/authserver
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

server:
  port: 8080
  servlet:
    context-path: /api/v1

app:
  jwtSecret: mySecretKeyForJWTTokenGenerationThatIsAtLeast32CharactersLong123456
  jwtAccessTokenExpiration: 900000 # 15 minutos en millisegundos
  jwtRefreshTokenExpiration: 604800000 # 7 días en millisegundos

logging:
  level:
    root: INFO
    com.hecttoy: DEBUG
```

## 🚀 Instalación y Ejecución

### Requisitos
- Java 17+
- Maven 3.8+
- PostgreSQL 12+ (opcional, usa H2 por defecto)

### Pasos

1. **Clonar el repositorio**
```bash
git clone https://github.com/hect-toy/Auth_Server.git
cd Auth_Server
```

2. **Compilar**
```bash
mvn clean package
```

3. **Ejecutar**
```bash
# H2 (base de datos en memoria)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

# PostgreSQL (requiere DB corriendo)
mvn spring-boot:run
```

4. **Verificar**
```bash
curl http://localhost:8080/api/v1/health
```

## 📝 Ejemplos de Uso

### 1. Registrar Usuario
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Iniciar Sesión
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

### 3. Obtener Info del Usuario
```bash
curl -X GET http://localhost:8080/api/v1/auth/userinfo \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 4. Renovar Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }'
```

### 5. Cerrar Sesión
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }'
```

## 🔍 Códigos HTTP

| Código | Descripción |
|--------|-------------|
| **200** | OK - Operación exitosa |
| **201** | Created - Recurso creado |
| **400** | Bad Request - Datos inválidos |
| **401** | Unauthorized - Token inválido/expirado |
| **403** | Forbidden - Acceso denegado |
| **404** | Not Found - Recurso no encontrado |
| **409** | Conflict - Recurso duplicado (email/username) |
| **500** | Internal Server Error - Error del servidor |

## 📂 Estructura de Directorios

```
src/
├── main/
│   ├── java/com/hecttoy/authserver/
│   │   ├── AuthServerApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── CustomUserDetailsService.java
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── dto/
│   │   │   ├── StandardRequest.java
│   │   │   ├── StandardResponse.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── TokenResponse.java
│   │   │   ├── RefreshTokenRequest.java
│   │   │   ├── LogoutRequest.java
│   │   │   └── UserInfoResponse.java
│   │   ├── exception/
│   │   │   ├── AuthException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── TokenException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── Role.java
│   │   │   └── RefreshToken.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── RoleRepository.java
│   │   │   └── RefreshTokenRepository.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   └── JwtAccessDeniedHandler.java
│   │   └── service/
│   │       └── AuthService.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/hecttoy/authserver/
```

## 🧪 Pruebas

```bash
mvn test
```

## 📚 Dependencias Principales

| Dependencia | Versión | Propósito |
|-------------|---------|----------|
| Spring Boot Starter Web | 3.1.6 | Framework web |
| Spring Boot Starter Security | 3.1.6 | Seguridad |
| Spring Boot Starter Data JPA | 3.1.6 | ORM/Persistencia |
| JJWT | 0.12.3 | Generación y validación de JWT |
| PostgreSQL Driver | latest | Base de datos |
| Lombok | latest | Reducción de boilerplate |
| Jakarta Validation | latest | Validación de datos |

## 🛡️ Consideraciones de Seguridad

- ✅ Contraseñas hasheadas con BCrypt (2^12 rounds)
- ✅ Tokens JWT firmados con HS256
- ✅ Refresh tokens almacenados en BD y revocables
- ✅ CORS habilitado para microservicios
- ✅ CSRF deshabilitado para APIs stateless
- ✅ Sesiones sin estado (stateless)
- ✅ Validación de input en todas las entradas
- ✅ Error handling centralizado sin revelar detalles internos

## 📄 Licencia

MIT

## 👨‍💻 Autor

Desarrollado por: **hect-toy**

---

**Última actualización**: Diciembre 2025
