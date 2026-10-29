# Cambios de Dificultad (Forge 1.20.1)

40 cambios de dificultad que se activan **uno cada 20 minutos** de partida (el reloj se pausa si no hay jugadores).
Ninguno te quita habilidades (puedes pegar, picar, construir, etc.): solo cambian mobs y mundo.

- **Ruleta roja**: al activarse un cambio sale la animación de tu ruleta.zip (mismos cuadros, la flecha cae en ROJO) con tu `ruleta.ogg`.
- **Chat** (en rojo) y **mensaje arriba a la izquierda** durante 1 minuto cuando la ruleta se detiene. **U** lo oculta/muestra.
- **G**: menú "Cambios de Dificultad" con una página por cambio activo (flechas, rueda del ratón o A/D), el botón de la ruleta muestra la lista de todos. Calavera pixel art abajo a la derecha.
- Comandos (operadores): `/cambiosdificultad estado | siguiente | fijar <n> | reiniciar`.
- Las teclas G y U se pueden cambiar en Controles.

## Cómo sacar el .jar desde GitHub
1. Crea un repositorio y sube TODO el contenido de esta carpeta (incluida `extra/frames.zip`, pesa ~15 MB).
2. Si `.github/workflows/build.yml` no se subió (las carpetas ocultas a veces se omiten): en GitHub pulsa **Add file → Create new file**, escribe `.github/workflows/build.yml` como nombre y pega el contenido de ese archivo.
3. Pestaña **Actions** → espera el check verde (3-6 min).
4. El .jar queda en **Releases → "Cambios de Dificultad (descarga)"** (y también como artefacto del Action).
5. Ponlo en la carpeta `mods` de Forge 1.20.1 (47.x) del servidor y de cada jugador.
