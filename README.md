# E-Commerce API

API REST desarrollada con Spring Boot para la gestión de un sistema de comercio electrónico.

Permite autenticación mediante JWT, gestión de productos y categorías, carrito persistente, generación de órdenes, filtros, paginación y control de acceso por roles.

## Funcionalidades

- Registro de usuarios
- Inicio de sesión con JWT
- Refresh Tokens
- Roles USER y ADMIN
- Gestión de productos
- Gestión de categorías
- Carrito persistente
- Generación de órdenes
- Historial de órdenes
- Soft Delete
- Paginación
- Filtros de búsqueda
- Validaciones de negocio
- Manejo global de excepciones
- Testing unitario
- Documentación con Swagger/OpenAPI

## Tecnologías

- Java 21
- Spring Boot 4.0.6
- Spring Security
- Spring Data JPA
- MySQL
- JWT (JJWT)
- Hibernate
- Maven
- Lombok
- Swagger/OpenAPI
- JUnit 5
- Mockito

## Arquitectura

El proyecto sigue una arquitectura en capas:

Controller
↓
Service
↓
Repository
↓
Base de Datos

Además utiliza:

- DTOs para entrada y salida de datos
- Mappers para conversiones
- Spring Security para autenticación y autorización
- JWT para sesiones sin estado
- Exception Handlers para errores centralizados

## Seguridad

La autenticación se realiza mediante JWT.

Características implementadas:

- Access Token
- Refresh Token
- Roles USER y ADMIN
- Endpoints protegidos con Spring Security
- Autorización basada en roles
- Logout mediante invalidación de Refresh Token

## Endpoints principales

### Autenticación


| Método | Endpoint |
|----------|----------|
| `POST` | `/auth/registro` |
| `POST` | `/auth/login` |
| `POST` | `/auth/refresh-token` |
| `POST` | `/auth/logout` |

### Productos

| Método | Endpoint |
|----------|----------|
| `POST` | `/productos` |
| `GET` | `/productos` |
| `PUT` | `/productos/{id}` |
| `DELETE` | `/productos/{id}` |

### Categorías

| Método | Endpoint |
|----------|----------|
| `POST` | `/categorias` |
| `GET` | `/categorias` |
| `PUT` | `/categorias/{id}` |
| `DELETE` | `/categorias/{id}` |

### Carrito

| Método | Endpoint |
|----------|----------|
| `POST` | `/carrito` |
| `DELETE` | `/carrito/{productoId}` |
| `GET` | `/carrito` |

### Órdenes

| Método | Endpoint |
|----------|----------|
| `POST` | `/ordenes/checkout` |
| `GET` | `/ordenes` |

## Testing

Se implementaron pruebas unitarias utilizando:

- JUnit 5
- Mockito
- MockMvc

Cobertura realizada sobre:

- Controllers
- Services

Los tests validan:

- Casos exitosos
- Casos de error
- Validaciones
- Excepciones de negocio

## Documentación API

La documentación interactiva está disponible mediante Swagger/OpenAPI.

Una vez iniciada la aplicación:
- [Ver documentación](http://localhost:8080/swagger-ui/index.html)

Desde Swagger es posible:

- Consultar todos los endpoints
- Ver DTOs de entrada y salida
- Autenticarse mediante JWT
- Ejecutar pruebas directamente desde la interfaz

## Estado del proyecto

### Funcionalidades implementadas

✅ Registro de usuarios

✅ Login con JWT

✅ Refresh Token

✅ Roles USER y ADMIN

✅ Gestión de productos

✅ Gestión de categorías

✅ Carrito persistente

✅ Generación de órdenes

✅ Historial de órdenes

✅ Soft Delete

✅ Paginación

✅ Filtros

✅ Validaciones

✅ Manejo global de excepciones

✅ Testing

✅ Swagger/OpenAPI