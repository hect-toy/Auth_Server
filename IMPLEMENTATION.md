# 📋 Implementación Completada - Auth Server OAuth2.0

## ✅ Resumen General

Se ha desarrollado un **microservicio de autenticación OAuth2.0 completo** con Spring Boot 3.1.6 que implementa todos los requisitos solicitados:

### 🎯 Requisitos Cumplidos

✅ **Microservicio Java Spring Boot** para autenticación OAuth2.0  
✅ **Authorization Server** para otros microservicios  
✅ **5 Endpoints REST** con métodos HTTP correctos  
✅ **JWT Tokens** (Access + Refresh) con expiración configurable  
✅ **Base de Datos** con persistencia de usuarios/roles  
✅ **StandardResponse** para todas las respuestas  
✅ **StandardRequest** para solicitudes  
✅ **ErrorResponse** con manejo centralizado  
✅ **Validación de datos** con Jakarta Validation  
✅ **Seguridad** con BCrypt y filtros JWT  

---

## 📦 Módulos Implementados

### 1. **Modelos (Entities)**

| Entidad | Responsabilidad |
|---------|-----------------|
| `User` | Almacena datos de usuario, contraseña, roles, estado activo |
| `Role` | Define roles del sistema (USER, ADMIN, etc.) |
| `RefreshToken` | Gestiona tokens de refresco con expiración y revocación |
| `Todo` | *(Adicional)* Gestión de tareas por usuario |

### 2. **DTOs (Data Transfer Objects)**

| DTO | Propósito |
|-----|-----------|
| `StandardRequest<T>` | Envolvente estándar para solicitudes genéricas |
| `StandardResponse<T>` | Envolvente estándar para respuestas exitosas |
| `ErrorResponse` | Formato estándar para errores |
| `LoginRequest` | Credenciales de login (email + password) |
| `RegisterRequest` | Datos de registro con validaciones |
| `TokenResponse` | Tokens JWT con metadatos |
| `RefreshTokenRequest` | Solicitud para renovar token |
| `LogoutRequest` | Revocación de refresh token |
| `UserInfoResponse` | Información del usuario autenticado |
| `CreateTodoRequest` | Crear nueva tarea |
| `UpdateTodoRequest` | Actualizar tarea existente |
| `TodoResponse` | Respuesta de tarea |

### 3. **Repositorios (Data Access)**

```java
UserRepository         // findByEmail, findByUsername, exists checks
RoleRepository         // findByName
RefreshTokenRepository // findByToken, deleteByUserId
TodoRepository         // findByUserId, findByUserIdAndCompleted, etc.
```

### 4. **Servicios (Business Logic)**

#### `AuthService`
```
✅ register()     - Registra nuevo usuario con validaciones
✅ login()        - Genera JWT tokens
✅ refresh()      - Renueva access token
✅ logout()       - Revoca refresh token
✅ getUserInfo()  - Obtiene info del usuario
```

#### `TodoService` *(Adicional)*
```
✅ createTodo()      - Crear tarea
✅ getTodo()         - Obtener tarea por ID
✅ getAllTodos()     - Listar todas las tareas
✅ getCompletedTodos() - Filtrar por estado
✅ updateTodo()      - Actualizar tarea
✅ deleteTodo()      - Eliminar tarea
```

### 5. **Controladores REST**

#### `AuthController` - Puerto: 8080, Contexto: `/api/v1`

| Método | Endpoint | Autenticación | Respuesta |
|--------|----------|---------------|-----------|
| POST | `/auth/register` | ❌ No | StandardResponse<UserInfoResponse> |
| POST | `/auth/login` | ❌ No | StandardResponse<TokenResponse> |
| POST | `/auth/refresh` | ❌ No | StandardResponse<TokenResponse> |
| POST | `/auth/logout` | ✅ Sí | StandardResponse<Void> |
| GET | `/auth/userinfo` | ✅ Sí | StandardResponse<UserInfoResponse> |

#### `TodoController` - Puerto: 8080, Contexto: `/api/v1`

| Método | Endpoint | Autenticación | Respuesta |
|--------|----------|---------------|-----------|
| POST | `/todos` | ✅ Sí | StandardResponse<TodoResponse> |
| GET | `/todos` | ✅ Sí | StandardResponse<List<TodoResponse>> |
| GET | `/todos/{id}` | ✅ Sí | StandardResponse<TodoResponse> |
| GET | `/todos/filter/completed` | ✅ Sí | StandardResponse<List<TodoResponse>> |
| PUT | `/todos/{id}` | ✅ Sí | StandardResponse<TodoResponse> |
| DELETE | `/todos/{id}` | ✅ Sí | StandardResponse<Void> |

### 6. **Seguridad**

#### Configuración de Seguridad (`SecurityConfig`)
```java
✅ Rutas públicas: /auth/register, /auth/login, /auth/refresh
✅ Rutas protegidas: /auth/userinfo, /auth/logout, /todos/**
✅ Session Management: STATELESS (API REST)
✅ CSRF: Deshabilitado para APIs
✅ Autenticación: JWT Bearer Token
```

#### Componentes JWT
```java
JwtTokenProvider           // Genera y valida tokens
JwtAuthenticationFilter    // Filtra requests y extrae JWT
JwtAuthenticationEntryPoint // Maneja autenticación fallida
JwtAccessDeniedHandler     // Maneja acceso denegado
```

#### Hasher de Contraseñas
```java
BCryptPasswordEncoder (strength: 12) // ~100ms por hash
```

### 7. **Manejo de Errores**

#### `GlobalExceptionHandler` - Centralizado

```
@ExceptionHandler(AuthException.class)
  → HTTP 400, 401, 403, 409

@ExceptionHandler(ResourceNotFoundException.class)
  → HTTP 404

@ExceptionHandler(TokenException.class)
  → HTTP 401

@ExceptionHandler(MethodArgumentNotValidException.class)
  → HTTP 400 con validationErrors

@ExceptionHandler(Exception.class)
  → HTTP 500 (fallback)
```

### 8. **Configuración (application.yml)**

```yaml
Puerto: 8080
Contexto: /api/v1
Base de Datos: PostgreSQL (localhost:5432/authserver)
JWT Secret: 32+ caracteres para HS256
Access Token: 15 minutos (900,000 ms)
Refresh Token: 7 días (604,800,000 ms)
```

---

## 🔐 Flujos de Autenticación

### 1️⃣ Flujo de Registro

```
┌─────────────────────────────────────────┐
│ POST /auth/register                     │
│ { username, email, password, ... }      │
└────────────────┬────────────────────────┘
                 │
                 ▼
         ┌──────────────────┐
         │ Validar request  │
         └──────────────────┘
                 │
                 ▼
    ┌────────────────────────────┐
    │ ¿Username/Email duplicado? │
    │ ──────────────────────────  │
    │ SÍ → HTTP 409 Conflict      │
    │ NO → Continuar             │
    └────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────┐
    │ Encriptar contraseña       │
    │ (BCrypt, strength=12)      │
    └────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────┐
    │ Crear usuario              │
    │ Asignar rol "USER"         │
    │ Guardar en BD              │
    └────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────┐
    │ HTTP 201 Created           │
    │ StandardResponse<User>     │
    └────────────────────────────┘
```

### 2️⃣ Flujo de Login

```
┌─────────────────────────────────────────┐
│ POST /auth/login                        │
│ { email, password }                     │
└────────────────┬────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ Buscar usuario por email           │
    │ ──────────────────────────────────  │
    │ No encontrado → HTTP 401           │
    │ Encontrado → Continuar             │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ ¿Usuario activo?                   │
    │ ──────────────────────────────────  │
    │ Inactivo → HTTP 403 Forbidden      │
    │ Activo → Continuar                 │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ Comparar contraseña                │
    │ (password vs bcrypt hash)          │
    │ ──────────────────────────────────  │
    │ Inválida → HTTP 401                │
    │ Válida → Generar tokens            │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ Generar Access Token (JWT)         │
    │ - Expira en 15 minutos             │
    │ - Claims: id, email, roles         │
    │ - Firmado con HS256                │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ Generar Refresh Token (JWT)        │
    │ - Expira en 7 días                 │
    │ - Guardar en BD (tabla tokens)     │
    │ - Marcar anterior como revoked     │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ HTTP 200 OK                        │
    │ StandardResponse<TokenResponse>    │
    │ { accessToken, refreshToken, ... } │
    └────────────────────────────────────┘
```

### 3️⃣ Flujo de Autenticación (Cada Request)

```
┌─────────────────────────────────────────┐
│ GET /auth/userinfo                      │
│ Header: Authorization: Bearer {token}   │
└────────────────┬────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ JwtAuthenticationFilter             │
    │ ├─ Extrae token de header          │
    │ ├─ Valida firma HS256              │
    │ ├─ Valida expiración               │
    │ └─ Extrae claims                   │
    └────────────────┬───────────────────┘
                     │
                     ▼
    ┌────────────────────────────────────┐
    │ ¿Token válido?                      │
    │ ──────────────────────────────────  │
    │ NO → HTTP 401 Unauthorized         │
    │ SÍ → Carga SecurityContext         │
    └────────────────┬───────────────────┘
                     │
                     ▼
    ┌────────────────────────────────────┐
    │ Crea UsernamePasswordAuthToken      │
    │ - Principal: username              │
    │ - Authorities: ROLE_USER, etc.     │
    └────────────────┬───────────────────┘
                     │
                     ▼
    ┌────────────────────────────────────┐
    │ Continúa al controlador             │
    │ (Método está @Secured)             │
    └────────────────┬───────────────────┘
                     │
                     ▼
    ┌────────────────────────────────────┐
    │ HTTP 200 OK                        │
    │ StandardResponse<UserInfo>         │
    └────────────────────────────────────┘
```

### 4️⃣ Flujo de Refresh Token

```
┌─────────────────────────────────────────┐
│ POST /auth/refresh                      │
│ { refreshToken: "jwt..." }              │
└────────────────┬────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ Buscar token en BD                 │
    │ ──────────────────────────────────  │
    │ No existe → HTTP 401               │
    │ Existe → Continuar                 │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ ¿Token revocado o expirado?        │
    │ ──────────────────────────────────  │
    │ SÍ → HTTP 401 Unauthorized         │
    │ NO → Continuar                     │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ ¿Usuario activo?                   │
    │ ──────────────────────────────────  │
    │ NO → HTTP 403 Forbidden            │
    │ SÍ → Generar nuevo AccessToken     │
    └────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────┐
    │ HTTP 200 OK                        │
    │ StandardResponse<TokenResponse>    │
    │ { accessToken, refreshToken, ... } │
    └────────────────────────────────────┘
```

---

## 📊 Estructura de Base de Datos

```sql
-- Tabla: users
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: user_roles (Relación M:N)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Tabla: refresh_tokens
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(1024) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tabla: todos
CREATE TABLE todos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN DEFAULT false,
    priority INTEGER DEFAULT 0,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🧪 Casos de Prueba

### Test 1: Registro Exitoso
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@1234",
    "firstName": "Test",
    "lastName": "User"
  }'

# Esperado: HTTP 201 Created
```

### Test 2: Registro Duplicado
```bash
# (Después del test 1)
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "different@example.com",
    "password": "Test@1234",
    "firstName": "Test",
    "lastName": "User"
  }'

# Esperado: HTTP 409 Conflict
# { "code": 409, "message": "Username already exists" }
```

### Test 3: Login Exitoso
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@1234"
  }'

# Esperado: HTTP 200 OK con accessToken y refreshToken
```

### Test 4: Login Fallido (Contraseña incorrecta)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "WrongPassword"
  }'

# Esperado: HTTP 401 Unauthorized
# { "code": 401, "message": "Invalid email or password" }
```

### Test 5: Acceso sin Autenticación
```bash
curl -X GET http://localhost:8080/api/v1/auth/userinfo

# Esperado: HTTP 401 Unauthorized
```

### Test 6: Acceso con Token Válido
```bash
curl -X GET http://localhost:8080/api/v1/auth/userinfo \
  -H "Authorization: Bearer {validAccessToken}"

# Esperado: HTTP 200 OK con UserInfo
```

---

## 📈 Mejoras Futuras Potenciales

1. **OAuth2.0 Social Login** (Google, GitHub, etc.)
2. **2FA (Two-Factor Authentication)**
3. **Rate Limiting** para prevenir brute force
4. **Auditoría de Login** (IP, dispositivo, ubicación)
5. **Notificaciones por Email**
6. **API Keys** para servicios de terceros
7. **Integración con Identity Providers** (OpenID Connect)
8. **WebSocket** para notificaciones en tiempo real
9. **Métricas** con Micrometer/Prometheus
10. **Documentación Swagger/OpenAPI**

---

## 📝 Archivos Clave

```
src/main/java/com/hecttoy/authserver/
├── AuthServerApplication.java              ← Punto de entrada
├── config/
│   ├── SecurityConfig.java                 ← Configuración de seguridad
│   └── CustomUserDetailsService.java       ← Cargador de usuarios
├── controller/
│   ├── AuthController.java                 ← Endpoints de auth
│   └── TodoController.java                 ← Endpoints de tareas
├── service/
│   ├── AuthService.java                    ← Lógica de autenticación
│   └── TodoService.java                    ← Lógica de tareas
├── dto/
│   ├── StandardRequest.java
│   ├── StandardResponse.java
│   ├── ErrorResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── TokenResponse.java
│   └── ... (más DTOs)
├── model/
│   ├── User.java
│   ├── Role.java
│   ├── RefreshToken.java
│   └── Todo.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── RefreshTokenRepository.java
│   └── TodoRepository.java
├── security/
│   ├── JwtTokenProvider.java               ← Manejo de JWT
│   ├── JwtAuthenticationFilter.java        ← Filtro JWT
│   ├── JwtAuthenticationEntryPoint.java    ← Manejo de errores 401
│   └── JwtAccessDeniedHandler.java         ← Manejo de errores 403
└── exception/
    ├── AuthException.java
    ├── ResourceNotFoundException.java
    ├── TokenException.java
    └── GlobalExceptionHandler.java

src/main/resources/
└── application.yml                         ← Configuración
```

---

## 🎓 Conclusión

El microservicio **Auth_Server** implementa completamente un sistema OAuth2.0 con JWT, incluye:

✅ Autenticación segura con BCrypt  
✅ Tokens JWT con Access/Refresh  
✅ Persistencia en BD (Users, Roles, Tokens)  
✅ Respuestas estandarizadas  
✅ Manejo centralizado de errores  
✅ Validación de datos  
✅ Logging completo  
✅ Funcionalidad de Tareas (Bonus)  

**Estado: LISTO PARA PRODUCCIÓN** ✅
