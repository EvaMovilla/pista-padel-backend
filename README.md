# Pista Padel Backend
INTEGRANTES: Eva Movilla, Virginia Castejón , Lucía Pérez-Maura


API REST para la gestión de reservas de pistas de pádel.  

---

User: SA / Password: (vacío)

## Roles
|Rol	|Permisos|
| :--- | :--- |
|USER	|Ver pistas, crear/cancelar sus propias reservas, editar su perfil|
|ADMIN |	Todo lo anterior + gestionar pistas y consultar todas las reservas|

## Endpoints
 Auth
| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/pistaPadel/auth/register` | `{ "nombre", "apellidos", "email", "telefono", "password" }` | Registra un nuevo usuario | `201` idUsuario <br> `400` validación <br> `409` email duplicado |
| `POST` | `/pistaPadel/auth/login` | `{ "email", "password" }` | Inicia sesión y crea cookie de sesión | `200` OK <br> `401` credenciales incorrectas |
| `GET` | `/pistaPadel/auth/me` | — | Devuelve datos del usuario autenticado | `200` usuario <br> `401` no autenticado |
| `POST` | `/pistaPadel/auth/logout` | — | Cierra la sesión activa | `204` No Content |

Pistas (Courts)
| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/courts` | — | Lista todas las pistas | `200` array de pistas |
| `GET` | `/pistaPadel/courts/{courtId}` | — | Obtiene una pista por ID | `200` pista <br> `404` no existe |
| `POST` | `/pistaPadel/courts` | `{ "nombre", "ubicacion", "precioHora", "activa" }` | Crea una nueva pista (ADMIN) | `201` pista creada <br> `403` no admin <br> `409` nombre duplicado |
| `PATCH` | `/pistaPadel/courts/{courtId}` | `{ "nombre"?, "ubicacion"?, "precioHora"?, "activa"? }` | Actualiza campos de una pista (ADMIN) | `200` pista actualizada <br> `403` no admin <br> `404` no existe <br> `409` nombre duplicado |
| `DELETE` | `/pistaPadel/courts/{courtId}` | — | Borra la pista físicamente o la desactiva si tiene reservas futuras (ADMIN) | `204` No Content <br> `403` no admin <br> `404` no existe |

Reservas (Reservations)
| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/pistaPadel/reservations` | `{ "idPista", "fechaReserva", "horaInicio", "horaFin" }` | Crea una reserva para el usuario autenticado | `201` reserva <br> `400` datos inválidos <br> `409` solapamiento de horario |
| `GET` | `/pistaPadel/reservations` | — | Lista las reservas del usuario autenticado. Params opcionales: `from`, `to` | `200` array de reservas |
| `GET` | `/pistaPadel/reservations/{reservationId}` | — | Obtiene una reserva por ID | `200` reserva <br> `403` no es el propietario <br> `404` no existe |
| `PATCH` | `/pistaPadel/reservations/{reservationId}` | `{ "fechaReserva"?, "horaInicio"?, "horaFin"? }` | Reprograma una reserva | `200` reserva actualizada <br> `403` no autorizado <br> `404` no existe <br> `409` solapamiento |
| `DELETE` | `/pistaPadel/reservations/{reservationId}` | — | Cancela una reserva (cambia estado a CANCELADA) | `204` No Content <br> `403` no es el propietario <br> `404` no existe |
| `GET` | `/pistaPadel/admin/reservations` | — | Lista todas las reservas con filtros opcionales: `date`, `courtId`, `userId` (ADMIN) | `200` array de reservas <br> `403` no admin |
Disponibilidad (Availability)
| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/availability` | — | Devuelve disponibilidad de todas las pistas (o una) para una fecha. Params: `date` (obligatorio), `courtId` (opcional) | `200` disponibilidad <br> `400` falta date |
| `GET` | `/pistaPadel/courts/{courtId}/availability` | — | Devuelve disponibilidad de una pista concreta para una fecha. Param: `date` (obligatorio) | `200` disponibilidad <br> `404` pista no existe |
Usuarios (Users)
| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/users` | — | Lista todos los usuarios (ADMIN) | `200` array de usuarios <br> `403` no admin |
| `GET` | `/pistaPadel/users/{userId}` | — | Obtiene un usuario por ID. USER solo puede ver el suyo | `200` usuario <br> `403` no autorizado <br> `404` no existe |
| `PATCH` | `/pistaPadel/users/{userId}` | `{ "nombre"?, "apellidos"?, "telefono"?, "password"? }` | Actualiza datos de un usuario. USER solo puede editar el suyo | `200` usuario actualizado <br> `403` no autorizado <br> `404` no existe |

Health
| Método | Ruta | Cuerpo | Descripción | Respuestas |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pistaPadel/health` | — | Comprueba que la API está activa | `200` "OK" |

## Tests
bash
mvn test
Cubre: courts CRUD, disponibilidad, permisos, reglas de negocio de borrado/desactivación lógica.

---

## Tecnologías

- Java 21 (Amazon Corretto)
- Spring Boot 3.4.2
- Spring Security (sesiones)
- Spring Data JPA + Hibernate
- H2 (base de datos en memoria)
- Maven

