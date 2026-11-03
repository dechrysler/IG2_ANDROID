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
