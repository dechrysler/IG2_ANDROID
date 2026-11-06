# Integración de la app Android con el backend

Guía de contexto y contrato para conectar la aplicación Android de clientes
**Sabor del Himalaya** con el backend REST del proyecto.

> Este documento **no modifica el código** de la app. Describe qué hace hoy la
> aplicación, qué falta y qué endpoints tiene que consumir.
>
> Fuente de verdad: implementación real del backend
> (`Backend-Restaurant-IS2`). Si hay diferencias entre este documento y el
> backend, mandan el código, Swagger o las colecciones Postman.

---

## Índice

1. [Estado actual de la app Android](#1-estado-actual-de-la-app-android)
2. [Qué tiene que implementar la app](#2-qué-tiene-que-implementar-la-app)
3. [Contexto del backend](#3-contexto-del-backend)
4. [Configuración de red en Android](#4-configuración-de-red-en-android)
5. [Autenticación: registro, activación y login](#5-autenticación-registro-activación-y-login)
6. [Restricción de roles: solo clientes](#6-restricción-de-roles-solo-clientes)
7. [Catálogo de platos y menú del día](#7-catálogo-de-platos-y-menú-del-día)
8. [Pedidos del cliente](#8-pedidos-del-cliente)
9. [Seguimiento del pedido](#9-seguimiento-del-pedido)
10. [Modelos de datos y adaptación de los modelos actuales](#10-modelos-de-datos-y-adaptación-de-los-modelos-actuales)
11. [Errores y manejo de respuestas](#11-errores-y-manejo-de-respuestas)
12. [Arquitectura recomendada](#12-arquitectura-recomendada)
13. [Checklist por pantalla](#13-checklist-por-pantalla)
14. [Plan de pruebas end-to-end](#14-plan-de-pruebas-end-to-end)
15. [Limitaciones conocidas y hallazgos](#15-limitaciones-conocidas-y-hallazgos)
16. [Criterios de aceptación](#16-criterios-de-aceptación)
17. [Referencias](#17-referencias)

---

## 1. Estado actual de la app Android

La app está construida en **Java + XML** con arquitectura MVC:

```text
app/src/main/java/com/example/sabordelhimalaya/
├── controller/   # Activities: MainActivity, InicioSesion, RegistroUsuario,
│                 # ActivacionCuenta, Principal2, Cesta, Pedidos
├── model/        # Categoria, Plato, MenuDiario, Pedido, Usuario, EstadoPedidos
└── view/         # Vistas y ajustes de presentación
```

### Qué hace hoy

- **Bienvenida (`MainActivity`)**: pantalla inicial con botones de login y
  registro.
- **Login (`InicioSesion`)**: formulario visual. Al pulsar “Iniciar sesión”
  navega directamente a la carta, sin validar credenciales ni llamar al
  backend.
- **Registro (`RegistroUsuario`)**: formulario visual. Al pulsar “Registrarse”
  navega a la pantalla de activación, sin llamar al backend.
- **Activación (`ActivacionCuenta`)**: formulario visual de código. Al enviar
  vuelve al inicio; “Reenviar código” muestra un aviso de que aún no está
  disponible.
- **Carta (`Principal2`)**: muestra categorías y secciones generadas desde un
  `enum` local. Los platos son datos estáticos; no vienen de la API.
- **Cesta (`Cesta`)**: solo tiene el estado vacío “Añade artículos a tu
  pedido”. No hay carrito real.
- **Pedidos (`Pedidos`)**: solo tiene el estado vacío “Aún no tiene pedidos”.
  No hay listado ni seguimiento.
- **Navegación inferior**: Carta, Cesta y Pedidos.

Los propios textos de la app lo confirman:

- `activation_pending`: “Pantalla de prueba: el envío y la verificación de
  códigos aún no están disponibles.”
- `registration_pending`: “El registro de usuarios aún no está disponible.”

### Qué no existe todavía

- Capa de red (no hay Retrofit, OkHttp ni llamadas HTTP).
- Permiso `INTERNET` en `AndroidManifest.xml`.
- URL base de la API ni configuración de entorno.
- DTOs de petición/respuesta del backend.
- Sesión: no se guarda token, ni usuario, ni rol.
- Carrito real.
- Listado, creación, modificación, cancelación y seguimiento de pedidos.
- Manejo de errores HTTP y de red.
- Validación de formularios contra las reglas del backend.

### Modelos actuales que hay que adaptar

| Modelo actual | Problema                                                                 |
| ------------- | ------------------------------------------------------------------------ |
| `Categoria`   | Solo tiene 6 valores; el backend tiene 10 categorías fijas.              |
| `MenuDiario`  | Estructura fija (entrantes/principales/postres/bebidas); el backend usa una colección libre de productos. |
| `Pedido`      | `idPedido` es `int`; el backend usa `UUID` en `String`.                   |
| `Plato`       | `precio` es `double`; el backend usa `BigDecimal` (número JSON).          |
| `Usuario`     | Guarda `contrasena` en claro; la app nunca debe almacenar la contraseña.  |

---

## 2. Qué tiene que implementar la app

La app Android es **exclusivamente para clientes**. Debe cubrir:

1. **Login, registro y activación de cliente**
   - Registro con nombre, dirección, correo, login y contraseña.
   - Activación con código de 6 dígitos.
   - Login con JWT.
   - **Bloquear el acceso a `CHEF` y `ENCARGADO`**: esta app es solo para
     `CLIENTE`.
2. **Catálogo**
   - Ver las categorías de la carta.
   - Ver los platos disponibles de cada categoría.
   - Ver el menú del día (o el mensaje de que no está disponible).
3. **Pedido**
   - Añadir platos y menús a una cesta local con cantidades.
   - Ver el resumen y el total calculado por el servidor.
   - Confirmar el pedido.
   - Consultar “mis pedidos”.
   - Ver el detalle y el estado del pedido.
   - Opcional (HU09/HU10): modificar o cancelar dentro de las reglas.
4. **Seguimiento**
   - Mostrar el estado actual (`RECIBIDO`, `EN_PREPARACION`, `LISTO`,
     `ENTREGADO`).
   - Mostrar la información de cancelación, si existe.
   - Mostrar la estimación de llegada cuando el pedido está `ENTREGADO`.
   - Refrescar periódicamente (no hay push ni WebSocket).

### Fuera de alcance de esta app

- Gestión de productos, menús o disponibilidad (web de gestión).
- Cambio de estado de pedidos (Encargado).
- Facturación.
- Pagos online.
- Seguimiento del repartidor.
- Registro o login de Chef/Encargado.
- Panel de administración.

---

## 3. Contexto del backend

### Ubicación y stack

- Repositorio: `..\Backend-Restaurant-IS2` (desde la raíz de `IG2_ANDROID`).
- Java 21, Spring Boot 3.2, PostgreSQL 16, JWT, BCrypt.
- API base: `http://localhost:8080/api`.
- El servidor escucha en `0.0.0.0:8080`; el contexto es `/api`.

### Arranque recomendado (Docker)

Desde `Backend-Restaurant-IS2`:

```powershell
docker compose up -d --build
docker compose logs -f backend
docker compose down
```

Servicios:

| Recurso        | URL                                         |
| -------------- | ------------------------------------------- |
| API            | `http://localhost:8080/api`                 |
| Swagger UI     | `http://localhost:8080/api/swagger-ui.html` |
| OpenAPI JSON   | `http://localhost:8080/api/v3/api-docs`     |
| Mailpit        | `http://localhost:8025`                     |

### Arranque local (opcional)

```powershell
docker compose up -d postgres mailpit
.\mvnw.cmd spring-boot:run
```

### Datos sembrados

Al arrancar se crean, si no existen:

| Login       | Rol         | Contraseña         |
| ----------- | ----------- | ------------------ |
| `chef`      | `CHEF`      | `chef12345`        |
| `encargado` | `ENCARGADO` | `encargado12345`   |

También se carga un catálogo de ejemplo de 20 productos. **No se siembran
menús**: el menú del día lo crea el Chef o el Encargado desde la web/API.

No hay clientes sembrados: el cliente se registra desde la app.

### Código de activación en desarrollo

Por defecto `MAIL_MOCK=true`. **No se envía correo real**; el código de 6
dígitos aparece en:

- Los logs del backend.
- Un archivo temporal:
  - Docker: `Backend-Restaurant-IS2/tmp/codigo-activacion-tmp.txt`.
  - Local: `Backend-Restaurant-IS2/codigo-activacion-tmp.txt`.

También se puede usar Mailpit (`MAIL_MOCK=false`) y abrir
`http://localhost:8025`.

### Colecciones Postman

Desde la raíz de `IG2_ANDROID`:

```text
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Auth.postman_collection.json
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Pedidos.postman_collection.json
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Management.postman_collection.json
```

La colección de **Pedidos** contiene el flujo completo de HU06–HU10, las
pruebas de permisos y ejemplos de todos los payloads.

### Documentación del backend

```text
..\Backend-Restaurant-IS2\README.md
..\Backend-Restaurant-IS2\docs\PROJECT_SPECIFICATION.md
..\Backend-Restaurant-IS2\docs\EMAIL_SETUP.md
```

---

## 4. Configuración de red en Android

### 4.1 Permiso de Internet

El `AndroidManifest.xml` actual **no** declara `INTERNET`. Hay que añadir:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 4.2 Tráfico HTTP sin cifrar

En desarrollo el backend usa HTTP. En Android 9+ el tráfico cleartext está
bloqueado por defecto. Opciones:

- Rápida, solo desarrollo:

```xml
<application
    android:usesCleartextTraffic="true"
    ... >
```

- Recomendada, limitada a la IP/puerto de desarrollo:
  `res/xml/network_security_config.xml` con un `domain-config` para
  `10.0.2.2` y la IP LAN.

### 4.3 URL base según el dispositivo

| Escenario                    | URL base                          |
| ---------------------------- | --------------------------------- |
| Emulador Android             | `http://10.0.2.2:8080/api`        |
| Móvil físico en la misma Wi-Fi | `http://<IP-LAN-del-PC>:8080/api` |
| Navegador del PC             | `http://localhost:8080/api`       |

Para obtener la IP LAN en Windows:

```powershell
ipconfig
```

Requisitos para el móvil físico:

- Mismo Wi-Fi que el PC.
- Firewall de Windows permitiendo el puerto `8080` en redes privadas.
- Backend escuchando en `0.0.0.0` (ya configurado).

### 4.4 URL base en el código

No hardcodear la URL en las Activities. Usar una sola fuente:

- `BuildConfig.API_BASE_URL` definido por `buildConfigField`, o
- un recurso `string.xml` de configuración.

Ejemplo conceptual en `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/api\"")
    }
    buildFeatures {
        buildConfig = true
    }
}
```

La URL debe incluir el prefijo `/api`.

### 4.5 Imágenes

`imagenUrl` viene como ruta relativa, por ejemplo:

```json
"/uploads/productos/abc.png"
```

La URL completa se construye uniendo la base de la API:

```text
http://10.0.2.2:8080/api/uploads/productos/abc.png
```

Si `imagenUrl` es `null`, mostrar un placeholder. No concatenar `localhost`
dentro de la app.

---

## 5. Autenticación: registro, activación y login

Todas las rutas de autenticación son **públicas** y están bajo `/auth`.

### 5.1 `POST /auth/register`

Registra un cliente. El rol asignado por el backend es siempre `CLIENTE`; el
body **no acepta rol**.

**Request:**

```json
{
  "nombre": "Juan Pérez",
  "direccion": "Calle Mayor 123, Madrid",
  "correoElectronico": "juan@test.com",
  "login": "juanperez",
  "contrasena": "password123",
  "contrasenaConfirmacion": "password123"
}
```

**Validaciones del backend:**

| Campo                    | Regla                                      |
| ------------------------ | ------------------------------------------ |
| `nombre`                 | Obligatorio, máximo 100 caracteres         |
| `direccion`              | Obligatorio, máximo 200 caracteres         |
| `correoElectronico`      | Obligatorio, formato email, máximo 100     |
| `login`                  | Obligatorio, máximo 50; único              |
| `contrasena`             | Obligatorio, máximo 100                    |
| `contrasenaConfirmacion` | Obligatorio y debe coincidir               |

**Response `200`:**

```json
{
  "mensaje": "Hemos enviado a tu correo un código de activación de la cuenta",
  "token": null,
  "usuarioId": "uuid",
  "nombre": "Juan Pérez",
  "correoElectronico": "juan@test.com",
  "rol": "CLIENTE"
}
```

> El registro **no devuelve token**. La cuenta queda pendiente de activación.

**Errores frecuentes (`400`):**

- `"El login ya está en uso"`
- `"El correo electrónico ya está registrado"`
- `"Las contraseñas no coinciden"`
- Errores de validación por campo en `errores`.

**Qué debe hacer la app:**

- Validar localmente los campos y la confirmación de contraseña.
- Llamar a `/auth/register`.
- Guardar el correo para la pantalla de activación.
- Navegar a activación mostrando el mensaje del backend.
- No guardar la contraseña.

### 5.2 `POST /auth/activate`

Activa la cuenta con el código de 6 dígitos.

**Request:**

```json
{
  "correoElectronico": "juan@test.com",
  "codigo": "482913"
}
```

**Response `200`:**

```json
{
  "mensaje": "Cuenta activada correctamente. ¡Bienvenido!",
  "token": "eyJhbGciOi...",
  "usuarioId": "uuid",
  "nombre": "Juan Pérez",
  "correoElectronico": "juan@test.com",
  "rol": "CLIENTE"
}
```

- El código tiene 6 dígitos y caduca a los **10 minutos**.
- La activación devuelve un **JWT válido**. La app puede guardarlo y entrar
  directamente a la carta, sin pedir login otra vez.
- Errores: `"Código de activación inválido o expirado"`,
  `"La cuenta ya está activada"`.

### 5.3 `POST /auth/resend-code`

**Request:**

```json
{
  "correoElectronico": "juan@test.com"
}
```

**Response `200`:** mensaje informativo, sin token.

- Hay un **cooldown de 5 minutos** entre reenvíos.
- Si se pide antes: `400` con
  `"Debes esperar N minuto(s) para solicitar un nuevo código"`.
- Si no existe la cuenta: `404` con
  `"No hay cuenta registrada con ese correo"`.
- Si ya está activa: `400` con `"La cuenta ya está activada"`.

### 5.4 `POST /auth/login`

**Request:**

```json
{
  "login": "juanperez",
  "contrasena": "password123"
}
```

**Response `200`:**

```json
{
  "mensaje": "Login exitoso",
  "token": "eyJhbGciOi...",
  "usuarioId": "uuid",
  "nombre": "Juan Pérez",
  "correoElectronico": "juan@test.com",
  "rol": "CLIENTE"
}
```

Reglas:

- El `login` distingue mayúsculas/minúsculas.
- Credenciales incorrectas: `400` con
  `"Usuario o contraseña incorrectos"`.
- Cuenta pendiente de activación: `400` con
  `"La cuenta no está activada. Revisa tu correo para el código de activación."`.
- **No es `401`**: el `401` se reserva para tokens ausentes/inválidos en rutas
  protegidas. La app debe tratar el `400` de login como error de credenciales.

### 5.5 `GET /auth/me`

Requiere token. Sirve para restaurar la sesión al abrir la app.

**Response `200`:**

```json
{
  "usuarioId": "uuid",
  "nombre": "Juan Pérez",
  "login": "juanperez",
  "correoElectronico": "juan@test.com",
  "rol": "CLIENTE"
}
```

- Si el token es inválido o caducado: `401 NO_AUTORIZADO`.
- Si la cuenta no está activa: `400`.
- La app debe comprobar aquí también que `rol == "CLIENTE"`.

### 5.6 Sesión y token

- Guardar el JWT en almacenamiento seguro:
  - `EncryptedSharedPreferences` (androidx.security) o
  - `SharedPreferences` privado como mínimo, nunca público.
- En cada petición protegida:

```http
Authorization: Bearer <token>
```

- El token dura **24 horas** por defecto. No hay refresh token.
- Si una petición devuelve `401`:
  1. borrar el token,
  2. borrar el usuario en sesión,
  3. navegar al login.
- Si devuelve `403`: mostrar error de permisos; no cerrar sesión
  automáticamente salvo que la app lo decida.
- No guardar la contraseña ni el hash.
- Al cerrar sesión, borrar token y datos de usuario; no borrar el carrito si se
  quiere conservar, pero es opcional.

---

## 6. Restricción de roles: solo clientes

El backend **permite** que `chef` y `encargado` hagan login en `/auth/login`
porque comparten el mismo endpoint. La restricción de que esta app es solo para
clientes es **responsabilidad de la app Android**.

### Regla obligatoria

Después de `POST /auth/login` o `POST /auth/activate`, comprobar `rol`:

```text
si rol != "CLIENTE":
    no guardar el token
    mostrar: "Esta aplicación es solo para clientes. Usa la web de gestión."
    volver al login / no navegar a la carta
```

También al restaurar la sesión con `GET /auth/me`.

### Defensa en profundidad del backend

Aunque un token de Chef/Encargado se colara en la app:

- `POST /orders` y `/orders/**` de cliente llaman a `OrderService`, que exige
  `rol == CLIENTE` y cuenta activa.
- Respuesta: `400 ERROR_NEGOCIO` con
  `"Solo los clientes pueden realizar pedidos"`.

Pero **no confiar solo en eso**: la app debe bloquear el login de personal y
no ofrecer ningún registro de Chef/Encargado. El registro público siempre crea
`CLIENTE`.

### Prueba manual

1. Login con `chef` / `chef12345`.
2. La app debe rechazar la sesión y mostrar el mensaje.
3. Repetir con `encargado` / `encargado12345`.
4. Login con un cliente activo → debe entrar a la carta.

---

## 7. Catálogo de platos y menú del día

Estas rutas son **públicas**: no requieren token. Aun así, se pueden llamar con
token; el backend no lo exige.

### 7.1 `GET /catalog/categories`

Lista las 10 categorías fijas.

**Response `200`:**

```json
[
  {
    "numero": 1,
    "nombre": "ENTRANTES",
    "descripcion": "Entrantes (incluye sopas)"
  }
]
```

| Número | Enum              | Descripción                     |
| ------ | ----------------- | ------------------------------- |
| 1      | `ENTRANTES`       | Entrantes (incluye sopas)       |
| 2      | `ENSALADAS`       | Ensaladas                       |
| 3      | `MOMOS`           | Momos                           |
| 4      | `DAL_BHAT`        | Dal Bhat y platos tradicionales |
| 5      | `TANDOORI`        | Tandoori                        |
| 6      | `CURRYS`          | Currys (carne y vegetarianos)   |
| 7      | `ARROCES_NOODLES` | Arroces y noodles               |
| 8      | `ACOMPANAMIENTOS` | Acompañamientos                 |
| 9      | `POSTRES`         | Postres                         |
| 10     | `BEBIDAS`         | Bebidas                         |

Recomendación: construir las pestañas/secciones desde esta respuesta, no desde
un `enum` local, para no desincronizarse.

### 7.2 `GET /catalog/categories/{numero}/dishes`

Devuelve **solo productos disponibles** de la categoría.

**Response `200`:**

```json
[
  {
    "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
    "nombre": "Samosa",
    "descripcion": "Empanadilla nepalí rellena de patata y especias",
    "imagenUrl": "/uploads/productos/samosa.png",
    "categoria": 1,
    "categoriaNombre": "Entrantes (incluye sopas)",
    "precio": 4.5,
    "disponible": true
  }
]
```

- `imagenUrl` puede ser `null`.
- `categoria` es el número 1–10.
- No hay endpoint público para listar productos ocultos: solo se muestran los
  disponibles.
- No hay búsqueda ni paginación; la app puede filtrar en memoria si lo
  necesita.

### 7.3 `GET /menu/today`

Devuelve el menú del día actual, solo si está disponible.

**Con menú:**

```json
{
  "disponible": true,
  "mensaje": null,
  "menu": {
    "id": "b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e",
    "nombre": "Menú del día",
    "descripcion": "Entrante, principal y bebida",
    "productos": [
      {
        "id": "uuid-1",
        "nombre": "Samosa",
        "descripcion": "Entrantes",
        "imagenUrl": "/uploads/productos/samosa.png",
        "categoria": 1,
        "categoriaNombre": "Entrantes (incluye sopas)",
        "precio": 4.5,
        "disponible": true
      }
    ],
    "precio": 12.5,
    "precioPersonalizado": 12.5,
    "disponible": true,
    "esMenuDelDia": true,
    "fechaMenuDelDia": "2026-11-06",
    "creadoEn": "2026-11-06T10:00:00"
  }
}
```

**Sin menú o menú oculto:**

```json
{
  "disponible": false,
  "mensaje": "El menú del día no está disponible hoy",
  "menu": null
}
```

Reglas:

- Un menú es una **colección de productos**, no una estructura fija
  3/3/2. No asumir entrante/principal/postre/bebida.
- `precio` es el precio efectivo: si `precioPersonalizado` tiene valor, se usa
  ese; si no, es la suma de los productos.
- `productos` ya excluye los no disponibles en la vista pública.
- Solo hay un menú del día por fecha y caduca al terminar el día.
- El cliente puede pedir el menú completo con `menuId` y una cantidad.

### 7.3.1 `GET /menu/available`

Devuelve todos los menús públicos disponibles, incluido el menú del día. Es la
ruta que usa la app para la pantalla **Menús**, donde el cliente puede elegir
entre varios menús y no solo el del día.

**Response `200`:** array de `MenuResponse`, con `productos` filtrados a los
disponibles. El backend ordena por fecha de creación descendente.

```json
[
  {
    "id": "uuid-menu",
    "nombre": "Menú degustación",
    "descripcion": "Entrante, momos, curry y postre",
    "productos": [],
    "precio": 24.9,
    "precioPersonalizado": 24.9,
    "disponible": true,
    "esMenuDelDia": false,
    "fechaMenuDelDia": null,
    "creadoEn": "2026-11-06T12:00:00"
  }
]
```

### 7.3.2 `GET /menu/{id}`

Detalle público de un menú disponible. Se usa en la pantalla de detalle del
menú. Si el menú no existe o está oculto, responde `404 NO_ENCONTRADO`.

### 7.4 Imágenes

- Usar Glide/Picasso/Coil para cargar `imagenUrl` construida con la base.
- Placeholder si es `null` o falla la carga.
- No bloquear la UI mientras se descargan.

---

## 8. Pedidos del cliente

Todas estas rutas requieren JWT de un usuario **`CLIENTE` activo**. Si se usa
un token de Chef/Encargado, el backend responde `400 ERROR_NEGOCIO` con
`"Solo los clientes pueden realizar pedidos"`.

### 8.1 Estructura de una línea de pedido

```json
{
  "platoId": "uuid-producto",
  "menuId": null,
  "cantidad": 2
}
```

Reglas:

- Cada línea debe indicar **exactamente uno** de `platoId` o `menuId`.
- `cantidad` debe ser un entero **mayor que 0**.
- El pedido debe tener **al menos una línea**.
- No se envían precios: el servidor los calcula y los congela.
- Se aceptan alias (`productoId`, `dishId`, `menuDelDiaId`, `quantity`), pero
  se recomienda usar los nombres canónicos.

### 8.2 `POST /orders/preview`

Calcula el resumen sin guardar el pedido. Es el paso previo a confirmar.

**Request:**

```json
{
  "items": [
    { "platoId": "uuid-producto", "cantidad": 2 },
    { "menuId": "uuid-menu", "cantidad": 1 }
  ],
  "direccionEntrega": "Calle Mayor 123, Madrid"
}
```

- `direccionEntrega` es opcional. Si se omite o va vacía, el backend usa la
  dirección del cliente registrado.
- `direccion` también se acepta como alias.

**Response `200`:**

```json
{
  "clienteId": "uuid-cliente",
  "direccionEntrega": "Calle Mayor 123, Madrid",
  "items": [
    {
      "tipo": "PRODUCTO",
      "platoId": "uuid-producto",
      "menuId": null,
      "nombre": "Samosa",
      "componentes": [],
      "cantidad": 2,
      "precioUnitario": 4.5,
      "importeLinea": 9.0
    },
    {
      "tipo": "MENU",
      "platoId": null,
      "menuId": "uuid-menu",
      "nombre": "Menú del día",
      "componentes": ["Samosa", "Curry", "Naan"],
      "cantidad": 1,
      "precioUnitario": 12.5,
      "importeLinea": 12.5
    }
  ],
  "importeTotal": 21.5
}
```

### 8.3 `POST /orders`

Confirma y crea el pedido.

**Request:** mismo body que `/orders/preview`.

**Response `201`:** `OrderResponse` (ver sección 8.7).

Efectos:

- Genera `numeroPedido` con formato `PED-AAAAMMDD-NNNN`.
- Registra `fechaHora`.
- Estado inicial `RECIBIDO`.
- `cancelacion` inicial `SIN_CANCELAR`.
- Guarda el precio unitario de cada línea.

**Errores frecuentes (`400 ERROR_NEGOCIO`):**

- `"El pedido debe tener al menos un producto o menú"`
- `"Cada línea debe indicar un producto o un menú, pero no ambos"`
- `"La cantidad debe ser mayor que 0"`
- `"El producto 'X' no está disponible"`
- `"El menú 'X' no está disponible"`
- `"El menú del día no está configurado para hoy"`
- `"La dirección de entrega es obligatoria"`
- `"Solo los clientes pueden realizar pedidos"`

Si el producto/menú ya no está disponible, la app debe refrescar el catálogo y
pedir al usuario que revise la cesta.

### 8.4 `GET /orders/my`

Lista los pedidos del cliente autenticado, ordenados por fecha descendente.

**Response `200`:** array de `OrderResponse`.

### 8.5 `GET /orders/{id}`

Detalle de un pedido propio.

- Si el pedido no existe o pertenece a otro cliente: `404 NO_ENCONTRADO` con
  `"Pedido no encontrado"` (no se revela su existencia).
- Si el `id` no es un UUID válido: `400 PARAMETRO_INVALIDO`.

### 8.6 `PUT /orders/{id}` y `DELETE /orders/{id}`

#### Modificar (HU09)

`PUT /orders/{id}` **sustituye todas las líneas** del pedido. No es un PATCH:
hay que enviar la cesta completa.

**Request:** mismo body que la creación (items + dirección opcional).

Solo se permite si:

- El pedido es del cliente.
- No está cancelado.
- Sigue en estado `RECIBIDO`.
- Han pasado **10 minutos o menos** desde la confirmación.

Si no: `400` con `"El pedido ya no puede modificarse"`.

La respuesta `200` devuelve el pedido actualizado y recalcula el total.

#### Cancelar (HU10)

```http
DELETE /orders/{id}?observaciones=Ya%20no%20lo%20quiero
```

- Solo se permite en estado `RECIBIDO` y si no está ya cancelado.
- Si han pasado **10 minutos o menos** desde la confirmación:
  `cancelacion = CANCELADO_SIN_CARGO`.
- Si han pasado **más de 10 minutos**:
  `cancelacion = CANCELADO_CON_CARGO` (cargo del 100 %).
- El estado sigue siendo `RECIBIDO`; la cancelación se refleja en
  `cancelacion` y `observacionesCancelacion`.
- La respuesta es `200` con `OrderResponse`.
- Si no se puede cancelar: `400` con `"El pedido ya no puede cancelarse"`.

`observaciones` es opcional.

### 8.7 `OrderResponse`

```json
{
  "id": "0e7c2f4a-6d1b-4a8e-9f3c-2b1a4c5d6e7f",
  "numeroPedido": "PED-20261106-0001",
  "clienteId": "uuid-cliente",
  "clienteNombre": "Juan Pérez",
  "direccionEntrega": "Calle Mayor 123, Madrid",
  "estado": "RECIBIDO",
  "cancelacion": "SIN_CANCELAR",
  "fechaHora": "2026-11-06T13:45:12",
  "fechaModificacion": "2026-11-06T13:45:12",
  "fechaEntrega": null,
  "items": [
    {
      "tipo": "PRODUCTO",
      "platoId": "uuid-producto",
      "menuId": null,
      "nombre": "Samosa",
      "componentes": [],
      "cantidad": 2,
      "precioUnitario": 4.5,
      "importeLinea": 9.0
    }
  ],
  "importeTotal": 9.0,
  "observacionesCancelacion": null,
  "modificable": true,
  "cancelable": true,
  "estimacionLlegada": null
}
```

Campos importantes para la app:

| Campo               | Uso en la app                                                   |
| ------------------- | --------------------------------------------------------------- |
| `numeroPedido`      | Mostrar el identificador legible del pedido                     |
| `estado`            | Estado del seguimiento                                          |
| `cancelacion`       | Badge de cancelado y motivo                                     |
| `fechaHora`         | Fecha/hora del pedido                                           |
| `fechaEntrega`      | Momento de entrega al repartidor                                |
| `items`             | Detalle de líneas                                               |
| `importeTotal`      | Total a mostrar (no recalcular)                                 |
| `modificable`       | Mostrar/ocultar botón “Modificar”                               |
| `cancelable`        | Mostrar/ocultar botón “Cancelar”                                |
| `estimacionLlegada` | Solo si `ENTREGADO`; mostrar como ETA                           |

### 8.8 Reglas de estado y ventana de 10 minutos

```text
RECIBIDO ──PUT /orders/{id}/state (Encargado)──► EN_PREPARACION
EN_PREPARACION ──PUT /orders/{id}/state (Encargado)──► LISTO
LISTO ──POST /orders/{id}/deliver (Encargado)──► ENTREGADO
```

- El cliente **no** cambia estados; solo el Encargado.
- El cliente puede modificar o cancelar mientras el pedido esté en `RECIBIDO`
  y no cancelado.
- La ventana de modificación/cancelación sin cargo es de **10 minutos** desde
  la confirmación. Modificar no amplía el plazo.
- `cancelable` es `true` en `RECIBIDO` aunque hayan pasado más de 10 minutos;
  en ese caso la cancelación será **con cargo**.
- `modificable` es `true` solo en `RECIBIDO`, no cancelado y dentro de los 10
  minutos.
- `ENTREGADO` significa “entregado al repartidor”, no recibido por el cliente.

La app debe usar los flags `modificable` y `cancelable` de la respuesta y
refrescar el pedido antes de operar; no calcularlos solo con la hora local.

---

## 9. Seguimiento del pedido

No hay WebSocket, SSE ni push. El seguimiento se hace con **polling** contra
`GET /orders/my` o `GET /orders/{id}`.

Recomendaciones:

- En la pantalla de listado: refrescar cada **20–30 segundos** mientras está
  visible; detener al hacer `onPause`/`onStop`.
- En el detalle: refrescar cada **15–30 segundos**.
- Refrescar también al volver del background y al hacer “pull to refresh”.
- No hacer polling agresivo ni en segundo plano.
- Si la red falla, mantener los últimos datos y mostrar un aviso discreto.

### Etiquetas de estado para la UI

| Estado           | Texto sugerido      | Color/indicador |
| ---------------- | ------------------- | --------------- |
| `RECIBIDO`       | Recibido            | Neutro          |
| `EN_PREPARACION` | En preparación      | Azafrán/ámbar   |
| `LISTO`          | Listo               | Verde           |
| `ENTREGADO`      | Entregado           | Verde oscuro    |

### Cancelación

- Si `cancelacion != SIN_CANCELAR`, mostrar un badge:
  - `CANCELADO_SIN_CARGO`: “Cancelado sin cargo”.
  - `CANCELADO_CON_CARGO`: “Cancelado con cargo (100 %)”.
- Mostrar `observacionesCancelacion` si existe.
- No mostrar acciones de modificar/cancelar sobre un pedido cancelado.
- Un pedido cancelado no avanza de estado.

### Estimación de llegada

- `estimacionLlegada` solo viene con valor cuando `estado = ENTREGADO`.
- Es `fechaEntrega + 30 minutos`.
- Formatear como hora local; si ya pasó, mostrar “Entrega estimada: ya debería
  haber llegado” o similar.
- Antes de `ENTREGADO` no inventar una ETA.

### Stepper sugerido

```text
[Recibido] → [En preparación] → [Listo] → [Entregado]
```

- Marcar los estados completados.
- Si está cancelado, mostrar el estado alcanzado y el badge de cancelación.

---

## 10. Modelos de datos y adaptación de los modelos actuales

### DTOs mínimos recomendados

```text
RegisterRequest        { nombre, direccion, correoElectronico, login, contrasena, contrasenaConfirmacion }
ActivateRequest        { correoElectronico, codigo }
LoginRequest           { login, contrasena }
ResendCodeRequest      { correoElectronico }
AuthResponse           { mensaje, token, usuarioId, nombre, correoElectronico, rol }
UserProfile            { usuarioId, nombre, login, correoElectronico, rol }

CategoryResponse       { numero, nombre, descripcion }
DishResponse           { id, nombre, descripcion, imagenUrl, categoria, categoriaNombre, precio, disponible }
MenuResponse           { id, nombre, descripcion, productos, precio, precioPersonalizado, disponible,
                         esMenuDelDia, fechaMenuDelDia, creadoEn }
TodayMenuResponse      { disponible, mensaje, menu }

OrderItemRequest       { platoId, menuId, cantidad }
CreateOrderRequest     { items, direccionEntrega }
OrderItemResponse      { tipo, platoId, menuId, nombre, componentes, cantidad, precioUnitario, importeLinea }
OrderResponse          { id, numeroPedido, clienteId, clienteNombre, direccionEntrega, estado,
                         cancelacion, fechaHora, fechaModificacion, fechaEntrega, items,
                         importeTotal, observacionesCancelacion, modificable, cancelable,
                         estimacionLlegada }
OrderPreviewResponse   { clienteId, direccionEntrega, items, importeTotal }
```

### Tipos

| JSON                         | Java recomendado                                  |
| ---------------------------- | ------------------------------------------------- |
| UUID                         | `String`                                          |
| `LocalDateTime`              | `java.time.LocalDateTime` (minSdk 31)             |
| `LocalDate`                  | `java.time.LocalDate`                             |
| Importes (`4.5`, `12.50`)    | `java.math.BigDecimal`                            |
| Enums (`estado`, `tipo`…)    | `String` o `enum` con `@SerializedName`           |
| `null`                       | Campos anulables; usar `@Nullable` o comprobar    |

### Cambios respecto a los modelos actuales

- **`Pedido`**: `idPedido` (`int`) → `id` (`String/UUID`); añadir
  `numeroPedido`, `estado`, `cancelacion`, `items`, `importeTotal`, fechas y
  flags. `precioTotal` (`double`) → `BigDecimal`.
- **`Plato`**: añadir `id`, `imagenUrl`, `categoria` numérica,
  `categoriaNombre` y `disponible`. `precio` → `BigDecimal`.
- **`MenuDiario`**: sustituir la estructura fija por `Menu` con
  `List<Plato> productos` y `precio`.
- **`Categoria`**: usar la respuesta de `/catalog/categories` o ampliar el
  enum a las 10 categorías reales.
- **`Usuario`**: eliminar `contrasena` del modelo de sesión. Guardar solo
  `usuarioId`, `nombre`, `login`, `correoElectronico` y `rol`.

---

## 11. Errores y manejo de respuestas

### Formato de error de negocio

```json
{
  "codigo": "ERROR_NEGOCIO",
  "mensaje": "El pedido ya no puede cancelarse"
}
```

### Formato de error de validación

```json
{
  "codigo": "VALIDACION_FALLIDA",
  "mensaje": "Errores de validación",
  "errores": {
    "precio": "El precio debe ser mayor que 0",
    "correoElectronico": "Formato de correo electrónico inválido"
  }
}
```

### Tabla de códigos

| HTTP  | Código               | Cuándo                                             |
| ----- | -------------------- | -------------------------------------------------- |
| `400` | `ERROR_NEGOCIO`      | Regla de negocio incumplida                        |
| `400` | `VALIDACION_FALLIDA` | Campos inválidos; revisar `errores`                |
| `400` | `PARAMETRO_INVALIDO` | UUID/fecha/estado con formato inválido             |
| `400` | `FORMATO_INVALIDO`   | JSON mal formado o enum desconocido                |
| `401` | `NO_AUTORIZADO`      | Token ausente, inválido o caducado                 |
| `403` | `ACCESO_DENEGADO`    | Rol incorrecto                                     |
| `404` | `NO_ENCONTRADO`      | Recurso inexistente o pedido de otro cliente       |
| `409` | `CONFLICTO_DATOS`    | Conflicto de integridad (por ejemplo, duplicado)   |
| `500` | `ERROR_INTERNO`      | Error inesperado del servidor                      |

### Manejo en la app

- Parsear siempre el body de error; mostrar `mensaje`.
- En `VALIDACION_FALLIDA`, mapear `errores` a los campos del formulario.
- En `401`: borrar token y volver al login.
- En `403`: mostrar “No tienes permisos para esta operación”.
- En errores de red/timeout: mensaje de conexión y botón de reintentar.
- No reintentar automáticamente `POST /orders` (puede duplicar el pedido).
- Los GET se pueden reintentar manualmente.
- Timeout recomendado: 15–20 segundos.

---

## 12. Arquitectura recomendada

La app es MVC con Activities y vistas XML. Para no romper la estructura
existente, se propone añadir una capa de red y sesión sin reescribir la UI:

```text
app/src/main/java/com/example/sabordelhimalaya/
├── controller/          # Activities actuales
├── view/                # Vistas actuales
├── model/
│   ├── dto/             # DTOs de request/response
│   ├── remote/          # ApiClient, ApiService (Retrofit/OkHttp)
│   └── ...              # modelos de dominio adaptados
├── session/             # SessionManager (token + usuario)
└── util/                # formatos, errores, imágenes
```

### Dependencias a añadir

- **Retrofit 2** + `converter-gson` (o OkHttp + Gson).
- **Glide** o **Picasso** para imágenes.
- **androidx.security:security-crypto** para token cifrado (o
  `SharedPreferences` privado + Keystore).
- `INTERNET` en el manifest.

No hay que añadir coroutines: la app es Java. Retrofit con `Call.enqueue()`
funciona bien.

### `ApiClient` y `ApiService`

- Un singleton `ApiClient` con `baseUrl = BuildConfig.API_BASE_URL`.
- Un interceptor que añada `Authorization: Bearer <token>` si existe.
- Un `ApiService` con los endpoints:

```java
@POST("auth/register") Call<AuthResponse> register(@Body RegisterRequest body);
@POST("auth/activate") Call<AuthResponse> activate(@Body ActivateRequest body);
@POST("auth/login") Call<AuthResponse> login(@Body LoginRequest body);
@POST("auth/resend-code") Call<AuthResponse> resendCode(@Body ResendCodeRequest body);
@GET("auth/me") Call<UserProfile> me();

@GET("catalog/categories") Call<List<CategoryResponse>> categories();
@GET("catalog/categories/{numero}/dishes") Call<List<DishResponse>> dishes(@Path("numero") int numero);
@GET("menu/today") Call<TodayMenuResponse> todayMenu();

@POST("orders/preview") Call<OrderPreviewResponse> preview(@Body CreateOrderRequest body);
@POST("orders") Call<OrderResponse> create(@Body CreateOrderRequest body);
@GET("orders/my") Call<List<OrderResponse>> myOrders();
@GET("orders/{id}") Call<OrderResponse> order(@Path("id") String id);
@PUT("orders/{id}") Call<OrderResponse> update(@Path("id") String id, @Body CreateOrderRequest body);
@DELETE("orders/{id}") Call<OrderResponse> cancel(@Path("id") String id, @Query("observaciones") String observaciones);
```

### `SessionManager`

- Guardar/leer/borrar token.
- Guardar perfil (`usuarioId`, `nombre`, `login`, `correo`, `rol`).
- `isLoggedIn()` y `isCliente()`.
- Al arrancar: si hay token, llamar a `/auth/me`; si falla o el rol no es
  `CLIENTE`, limpiar y mostrar login.

### Carrito

- Mantener el carrito **en memoria** (o persistirlo localmente si se desea)
  como lista de líneas:

```text
CartItem { tipo: PRODUCTO|MENU, id: UUID, nombre, precio, cantidad, componentes }
```

- El carrito no se envía al backend hasta `/orders/preview` o `/orders`.
- Al confirmar, enviar solo `platoId`/`menuId` y `cantidad`; nunca precios.
- Limpiar el carrito solo cuando `POST /orders` responde `201`.
- Si el backend rechaza una línea por disponibilidad, refrescar catálogo y
  marcar la línea como no disponible.

### Formato de fechas e importes

- Fechas: `LocalDateTime.parse(...)`, `DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("es","ES"))`.
- Importes: `BigDecimal` y `NumberFormat.getCurrencyInstance(new Locale("es","ES"))`.
- Zona horaria: mostrar los `LocalDateTime` del backend tal cual; no convertirlos
  a otra zona sin necesidad. Para “hoy” y filtros, usar la fecha del servidor.

---

## 13. Checklist por pantalla

### 13.1 Bienvenida (`MainActivity`)

- [ ] Si hay token válido y rol `CLIENTE`, ir directo a la carta.
- [ ] Si no, mostrar login/registro.
- [ ] No borrar la sesión sin comprobarla.

### 13.2 Login (`InicioSesion`)

- [ ] Campos `login` y `contrasena` con validación de no vacío.
- [ ] Llamar a `POST /auth/login`.
- [ ] Mostrar loading y deshabilitar el botón.
- [ ] Si `rol != CLIENTE`: no guardar token, mostrar mensaje y no navegar.
- [ ] Si `CLIENTE`: guardar token/perfil y navegar a la carta.
- [ ] Mostrar errores del backend (`mensaje`).
- [ ] Enlace a registro.

### 13.3 Registro (`RegistroUsuario`)

- [ ] Campos: nombre, dirección, correo, login, contraseña y confirmación.
  > El layout actual no tiene confirmación de contraseña; hay que añadirla o
  > validar en el flujo.
- [ ] Validar longitudes y formato de correo localmente.
- [ ] Llamar a `POST /auth/register`.
- [ ] Mostrar mensaje del backend.
- [ ] Navegar a activación pasando el correo.
- [ ] Manejar login/correo duplicados y errores por campo.
- [ ] No permitir elegir rol.

### 13.4 Activación (`ActivacionCuenta`)

- [ ] Mostrar el correo registrado (preferiblemente no editable).
- [ ] Campo de código de 6 dígitos.
- [ ] Llamar a `POST /auth/activate`.
- [ ] Guardar el token devuelto y navegar a la carta.
- [ ] “Reenviar código” → `POST /auth/resend-code`, respetar cooldown y mostrar
  mensaje.
- [ ] Manejar código inválido/expirado y cuenta ya activa.
- [ ] Explicar cómo obtener el código en desarrollo (log/archivo/Mailpit) solo
  en builds de depuración.

### 13.5 Carta (`Principal2`)

- [ ] Cargar categorías desde `GET /catalog/categories`.
- [ ] Cargar platos por categoría desde
  `GET /catalog/categories/{numero}/dishes`.
- [ ] Mostrar imagen, nombre, descripción y precio.
- [ ] Sección “Menú del día” con `GET /menu/today`.
- [ ] Si no hay menú, mostrar el mensaje del backend.
- [ ] Botón “Añadir” a la cesta con cantidad.
- [ ] Estados de carga, vacío y error.
- [ ] Refrescar al volver a la pantalla.
- [ ] Logout.

### 13.6 Cesta (`Cesta`)

- [ ] Listar líneas con nombre, cantidad, precio unitario e importe.
- [ ] Botones +/− y eliminar.
- [ ] Subtotal calculado localmente solo como referencia.
- [ ] Campo de dirección de entrega precargado con la dirección del cliente
  (si se conoce) y editable.
- [ ] Botón “Revisar pedido” → `POST /orders/preview`.
- [ ] Mostrar el resumen y total del servidor.
- [ ] Botón “Confirmar pedido” → `POST /orders`.
- [ ] Deshabilitar el botón mientras se envía (evitar duplicados).
- [ ] Si `201`: limpiar cesta y navegar al detalle del pedido.
- [ ] Si `400`: mostrar mensaje, refrescar catálogo si es disponibilidad.
- [ ] Cesta vacía: estado vacío actual.

### 13.7 Pedidos (`Pedidos`)

- [ ] Cargar `GET /orders/my`.
- [ ] Estado vacío si no hay pedidos.
- [ ] Cada tarjeta: número, fecha, estado, total, cancelación.
- [ ] Pulsar → detalle.
- [ ] Refrescar al entrar y al volver.
- [ ] Pull to refresh o botón.

### 13.8 Detalle y seguimiento

- [ ] Cargar `GET /orders/{id}`.
- [ ] Mostrar número, fecha, dirección, items, total.
- [ ] Stepper de estados.
- [ ] Badge de cancelación y observaciones.
- [ ] Botón “Modificar” si `modificable`.
- [ ] Botón “Cancelar” si `cancelable`; confirmación explícita; avisar de
  posible cargo si han pasado más de 10 minutos.
- [ ] Polling cada 15–30 s mientras la pantalla está visible.
- [ ] Mostrar `estimacionLlegada` si existe.
- [ ] Detener polling en `onPause`/`onDestroy`.
- [ ] Manejar `404` (pedido borrado/ajeno) volviendo al listado.

### 13.9 Perfil / logout (opcional)

- [ ] Mostrar nombre, login, correo y rol.
- [ ] Logout: borrar token/perfil y volver al login.
- [ ] No hay endpoint para editar perfil ni cambiar contraseña.

---

## 14. Plan de pruebas end-to-end

### 14.1 Preparación

1. Arrancar el backend:

```powershell
cd ..\..\Backend-Restaurant-IS2
docker compose up -d --build
```

2. Comprobar que la API responde:

```powershell
Invoke-RestMethod http://localhost:8080/api/catalog/categories
```

3. En el emulador, configurar `http://10.0.2.2:8080/api`. En móvil físico,
   `http://<IP-LAN>:8080/api`.

### 14.2 Flujo de cliente

1. Registrarse desde la app con un correo nuevo.
2. Abrir el archivo del código:

```powershell
Get-Content ..\..\Backend-Restaurant-IS2\tmp\codigo-activacion-tmp.txt
```

   o mirar el log del backend / Mailpit.
3. Introducir el código en la app → debe activar y entrar.
4. Comprobar que se cargan las 10 categorías y los platos.
5. Comprobar el menú del día:
   - Si no hay, debe mostrar “El menú del día no está disponible hoy”.
   - Para probar con menú, crear uno desde la web de gestión o con Postman.
6. Añadir un plato y/o menú a la cesta.
7. Revisar el pedido → comprobar que el total coincide con el servidor.
8. Confirmar → recibir `201` y ver el número `PED-...`.
9. Abrir “Mis pedidos” → debe aparecer.
10. Abrir el detalle → estado `RECIBIDO`.
11. Desde la web de gestión/Postman, avanzar a `EN_PREPARACION` y `LISTO`.
12. Refrescar la app (o esperar el polling) → debe reflejar los estados.
13. Marcar entregado desde la web → la app debe mostrar `ENTREGADO` y la ETA.
14. Dentro de 10 minutos, probar modificar y cancelar.
15. Probar cancelar después de 10 minutos (manual) → `CANCELADO_CON_CARGO`.

### 14.3 Pruebas de roles

1. Login con `chef` / `chef12345` → la app debe rechazar la sesión.
2. Login con `encargado` / `encargado12345` → la app debe rechazar la sesión.
3. Si se fuerza un token de Chef en una llamada a `/orders`, el backend debe
   responder `400` con `"Solo los clientes pueden realizar pedidos"`.

### 14.4 Pruebas de error

- Registro con login/correo duplicado.
- Registro con contraseñas distintas.
- Activación con código incorrecto/expirado.
- Reenvío antes del cooldown.
- Login con contraseña incorrecta.
- Token borrado o caducado → `401` y vuelta al login.
- Producto oculto entre la carga del catálogo y la confirmación → `400` y
  refresco.
- Pedido de otro cliente → `404`.
- Sin conexión → mensaje de red y reintento.

### 14.5 Postman

Importar:

```text
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Auth.postman_collection.json
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Pedidos.postman_collection.json
```

La colección de pedidos encadena tokens, IDs e importes y prueba permisos.

---

## 15. Limitaciones conocidas y hallazgos

Estas notas vienen de revisar el backend actual. No impiden desarrollar la app,
pero conviene tenerlas presentes:

1. **El login de Chef/Encargado está permitido por el backend.** La app debe
   rechazarlo explícitamente comprobando `rol`. No existe un endpoint separado
   de login para clientes.
2. **No hay push ni WebSocket.** El seguimiento es por polling.
3. **No hay idempotencia en `POST /orders`.** Un doble toque o un reintento de
   red puede crear dos pedidos. Deshabilitar el botón mientras se envía y no
   reintentar automáticamente.
4. **Generación del número de pedido sin bloqueo.** El backend calcula
   “último + 1”. Dos pedidos concurrentes podrían chocar con la restricción
   única. La app debe manejar `409 CONFLICTO_DATOS` o un `500` puntual y
   permitir reintentar la confirmación.
5. **Precio del menú con productos ocultos.** Si un menú no tiene precio
   personalizado y algún producto está oculto, la vista pública puede mostrar
   la suma de los productos visibles mientras el pedido cobra la suma de todos
   los productos del menú. **El total fiable es el de `POST /orders/preview`.**
   La app debe previsualizar antes de confirmar.
6. **Historial no congelado al 100 %.** El precio unitario sí se congela, pero
   el nombre y los componentes se leen de las entidades actuales. Si se edita
   un menú después, un pedido antiguo puede mostrar componentes nuevos.
7. **Facturación por fecha de creación** (no afecta a la app de cliente, pero
   explica por qué no debe usar fechas de entrega para nada).
8. **Zona horaria del servidor.** El backend usa `LocalDate.now()` para “hoy”,
   menú del día y rangos. En Docker suele ser UTC. Si el restaurante opera en
   otra zona, los cortes de día pueden no coincidir con la fecha local.
9. **Sin paginación.** `GET /orders/my` devuelve todos los pedidos del cliente.
10. **Sin edición de perfil ni cambio de contraseña.** No hay endpoints para
    actualizar nombre, dirección o contraseña. La dirección se fija en el
    registro y se puede sobrescribir en cada pedido con `direccionEntrega`.
11. **Sin notas generales del pedido.** Solo existen observaciones de
    cancelación.
12. **Sin pagos online.** El pedido no tiene estado de pago.
13. **Sin seguimiento del repartidor.** `ENTREGADO` significa entregado al
    repartidor; la ETA es una estimación de 30 minutos.
14. **Sin refresh token.** El JWT dura 24 horas; al caducar hay que volver a
    iniciar sesión.
15. **Código de activación en modo mock.** En desarrollo no se envía correo real;
    hay que leerlo del log o del archivo temporal.

---

## 16. Criterios de aceptación

La integración se considera terminada cuando:

- [ ] Un cliente puede registrarse, recibir/leer el código, activar la cuenta y
  entrar a la app.
- [ ] Un `CHEF` o `ENCARGADO` **no** puede entrar en la app; se muestra un
  mensaje claro y no se guarda su token.
- [ ] La sesión se restaura con `/auth/me` y se cierra ante `401`.
- [ ] La carta muestra las 10 categorías y los platos reales del backend.
- [ ] El menú del día se muestra con sus productos y precio, o el mensaje de no
  disponible.
- [ ] El carrito permite añadir, cambiar cantidades y eliminar líneas.
- [ ] El resumen y el total se obtienen de `/orders/preview`.
- [ ] Confirmar un pedido devuelve `201`, muestra el número y aparece en
  “Mis pedidos”.
- [ ] El detalle muestra estado, líneas, total, cancelación y ETA cuando
  corresponde.
- [ ] El seguimiento se actualiza por polling sin bloquear la UI.
- [ ] Modificar/cancelar respetan `modificable`/`cancelable` y las reglas de
  10 minutos.
- [ ] Se manejan errores `400/401/403/404/409/500` y errores de red.
- [ ] No hay URLs, tokens ni contraseñas hardcodeadas.
- [ ] Funciona en emulador y en un móvil físico de la misma Wi-Fi.
- [ ] El build de la app compila y no se rompe la navegación existente.

---

## 17. Referencias

### Backend

```text
..\Backend-Restaurant-IS2\README.md
..\Backend-Restaurant-IS2\docs\PROJECT_SPECIFICATION.md
..\Backend-Restaurant-IS2\docs\EMAIL_SETUP.md
```

### Controladores y reglas relevantes

| Área                    | Archivo                                                                 |
| ----------------------- | ----------------------------------------------------------------------- |
| Autenticación           | `presentation/auth/AuthController.java`                                 |
| Pedidos del cliente     | `presentation/order/OrderController.java`                               |
| Catálogo                | `presentation/catalog/CatalogController.java`                           |
| Menú del día            | `presentation/catalog/MenuController.java`                              |
| Seguridad y roles       | `infrastructure/security/SecurityConfig.java`                           |
| Reglas de pedido        | `domain/model/Pedido.java`, `application/order/OrderService.java`       |
| Respuestas de pedido    | `presentation/order/dto/OrderResponse.java`                             |

Rutas relativas desde la raíz de `Backend-Restaurant-IS2`:

```text
src/main/java/com/sabordelhimalaya/...
```

### Swagger

- `http://localhost:8080/api/swagger-ui.html`
- `http://localhost:8080/api/v3/api-docs`

### Colecciones Postman

```text
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Auth.postman_collection.json
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Pedidos.postman_collection.json
..\Backend-Restaurant-IS2\postman\Sabor-del-Himalaya-Management.postman_collection.json
```
