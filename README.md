# Auth Server - Microservicio OAuth2.0

Microservicio de autenticación y autorización construido con Spring Boot, implementando OAuth2.0 como Authorization Server para otros microservicios.

## Características

- ✅ **Autenticación OAuth2.0** con JWT (JSON Web Tokens)
- ✅ **Access Tokens** y **Refresh Tokens** con expiración configurable
- ✅ **Gestión de Usuarios y Roles** con persistencia en BD
- ✅ **Endpoints REST** estándar para autenticación
- ✅ **Respuestas Estandarizadas** con `StandardResponse`
- ✅ **Validación de Requests** con `StandardRequest`
- ✅ **Manejo de Errores Centralizado** con `GlobalExceptionHandler`
- ✅ **Seguridad** con BCrypt para hashing de contraseñas
- ✅ **Base de Datos** con PostgreSQL/H2

## Endpoints

### Autenticación

#### POST `/auth/login`
Inicia sesión con email y contraseña.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login exitoso",
  "statusCode": 200,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "user@example.com",
      "roles": ["USER"],
      "enabled": true
    }
  }
}
```

#### POST `/auth/register`
Registra un nuevo usuario.

**Request:**
```json
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "password123",
  "passwordConfirm": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "statusCode": 201,
  "data": { ... }
}
```

#### POST `/auth/refresh`
Renueva el access token usando el refresh token.

**Request:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Token renovado exitosamente",
  "statusCode": 200,
  "data": { ... }
}
```

#### POST `/auth/logout`
Cierra la sesión actual.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "success": true,
  "message": "Logout exitoso",
  "statusCode": 200
}
```

#### GET `/auth/userinfo`
Obtiene información del usuario autenticado.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:**
```json
{
  "success": true,
  "message": "Información de usuario obtenida exitosamente",
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com",
    "roles": ["USER"],
    "enabled": true
  }
}
```

## Configuración

### Base de Datos

#### PostgreSQL (Producción)
Modifica `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db
    username: postgres
    password: postgres
```

#### H2 (Desarrollo)
Ejecuta con el perfil `h2`:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

### JWT Configuration

Modifica `application.yml`:
```yaml
jwt:
  secret: mySecretKeyForAuthenticationServerWithMoreThan32CharactersLongForHS256
  expiration: 3600          # Access token: 1 hora
  refresh-expiration: 604800 # Refresh token: 7 días
```

## Estructura del Proyecto

```
src/main/java/com/hecttoy/auth/
├── AuthServerApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── UserDetailsServiceImpl.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── StandardRequest.java
│   ├── StandardResponse.java
│   └── auth/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       ├── RefreshTokenRequest.java
│       ├── AuthResponse.java
│       └── UserInfoDto.java
├── entity/
│   ├── User.java
│   ├── Role.java
│   └── RefreshToken.java
├── exception/
│   ├── AuthException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── RefreshTokenRepository.java
├── security/
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
└── service/
    ├── AuthService.java
    └── UserService.java
```

## Flujo de Autenticación

1. **Usuario registra** (`POST /auth/register`)
   - Valida datos
   - Encripta contraseña
   - Crea usuario en BD con rol USER

2. **Usuario inicia sesión** (`POST /auth/login`)
   - Valida credenciales
   - Genera JWT access token (1 hora)
   - Genera refresh token (7 días)
   - Almacena refresh token en BD
   - Retorna tokens al cliente

3. **Cliente realiza request** con `Authorization: Bearer {accessToken}`
   - Filtro JWT valida token
   - Carga usuario y roles del token
   - Establece contexto de seguridad

4. **Token expira** → Cliente usa refresh token
   - `POST /auth/refresh` con refresh token
   - Genera nuevo access token
   - Retorna nuevo token

5. **Usuario cierra sesión** (`POST /auth/logout`)
   - Revoca refresh token
   - Usuario debe re-autenticarse

## Ejecución

### Con Maven
```bash
# Build
mvn clean package

# Run (H2)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

# Run (PostgreSQL)
mvn spring-boot:run
```

### Con Docker
```bash
mvn clean package
docker build -t auth-server:latest .
docker run -p 8080:8080 auth-server:latest
```

## Dependencias

- **Spring Boot 3.1.6**
- **Spring Security**
- **Spring Data JPA**
- **JJWT 0.12.3** (JWT)
- **PostgreSQL Driver**
- **H2 Database**
- **Lombok**
- **Jakarta Validation**

## Ejemplo de Uso

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "passwordConfirm": "password123"
  }'

# 2. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'

# 3. Obtener info del usuario (usar el accessToken)
curl -X GET http://localhost:8080/api/v1/auth/userinfo \
  -H "Authorization: Bearer {accessToken}"

# 4. Renovar token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "{refreshToken}"
  }'

# 5. Logout
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer {accessToken}"
```

## Códigos de Error

- **200**: OK - Operación exitosa
- **201**: Created - Recurso creado exitosamente
- **400**: Bad Request - Datos inválidos o mal formados
- **401**: Unauthorized - Credenciales inválidas o token expirado
- **403**: Forbidden - Acceso denegado
- **404**: Not Found - Recurso no encontrado
- **500**: Internal Server Error - Error interno del servidor

## Licencia

MIT
