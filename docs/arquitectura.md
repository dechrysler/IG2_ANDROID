# Arquitectura MVC

Java y vistas XML de Android.

- model: entidades y acceso a datos, sin dependencias de la interfaz. Se añadirán modelos cuando implementemos funcionalidades de dominio.
- view: presentación y ajustes visuales. Los diseños XML permanecen en res/layout y las imágenes en res/drawable*, según las convenciones de Android.
- controller: recibe acciones y coordina modelos y vistas. MainActivity es el controlador de entrada.

## Bienvenida, paso 1

Fondo independiente de los futuros textos, adorno y botones. ImageView usa centerCrop para cubrir la pantalla sin deformar: se recortan bordes según la relación de aspecto. La referencia es vertical; en horizontal el recorte será mayor.

La imagen está en drawable-nodpi para evitar escalado automático por densidad. El fondo se extiende bajo las barras; el contenedor de contenido respeta barras y recortes de pantalla.

## Recurso generado

Herramienta: image_gen integrada. Referencia: ima.jpeg aportada por el usuario.

Prompt: crear fondo vertical 9:16 conservando textura de papel marfil, montañas del Himalaya en acuarela terracota en la parte superior y sol arriba a la derecha. Eliminar todos los textos, loto, líneas, botones y contornos; reconstruir el papel y dejar libre la zona inferior para vistas nativas.

## Integración con el backend

La app mantiene el patrón MVC y añade una capa de red y sesión:

```text
app/src/main/java/com/example/sabordelhimalaya/
├── controller/          Activities de cliente
├── model/
│   ├── dto/             DTOs de request/response del backend
│   ├── remote/          ApiClient (Retrofit + OkHttp) y ApiService
│   ├── CartRepository   cesta local persistida en SharedPreferences privado
│   └── ...              Categoria, Plato, Menu, Pedido, LineaPedido, Usuario
├── session/             SessionManager (token + perfil, sin contraseña)
├── util/                errores, formatos es-ES, URLs de imágenes, roles
└── view/                vistas y construcción dinámica de la carta
```

### URL de la API

- Por defecto: `http://10.0.2.2:8080/api` (emulador).
- Se define en `BuildConfig.API_BASE_URL` y se puede sobrescribir sin tocar código:

```powershell
.\gradlew.bat assembleDebug -PAPI_BASE_URL=http://192.168.1.50:8080/api
```

- El tráfico HTTP sin cifrar está permitido para desarrollo (`usesCleartextTraffic`).
- Las imágenes se resuelven uniendo la base de la API con `imagenUrl`.

### Permisos de red

Además de `INTERNET`, en Android 16 o superior (targetSdk 37) se necesita
`ACCESS_LOCAL_NETWORK` para acceder al backend en la red local o al host del
emulador. MainActivity lo solicita al arrancar.

### Sesión y roles

- El token se guarda en `SharedPreferences` privado junto al perfil público.
- Nunca se guarda la contraseña.
- Al arrancar se valida el token con `GET /auth/me`.
- Ante un `401` se borra la sesión y se vuelve al login.
- Solo se acepta el rol `CLIENTE`; los tokens de `CHEF` o `ENCARGADO` no se
  guardan y la app muestra el mensaje de que es solo para clientes.
