Guía rápida: SDL en este proyecto (ClienteC)

Qué hace el proyecto
- El subproyecto `ClienteC` contiene un cliente en C que puede ejecutarse en consola o con una interfaz gráfica simple basada en SDL.
- El código incluye un ejemplo mínimo de ventana y bucle de render para servir como punto de partida.

Compatibilidad SDL
- El proyecto puede compilarse con SDL3 o con SDL2. CMake intentará usar SDL3 cuando esté disponible; si no, usará SDL2.
- Hay una opción CMake (`FORCE_SDL2`) para forzar el uso de SDL2 aunque SDL3 esté instalado.

Instalación recomendada (MSYS2 + MinGW, recomendado para CLion)
1. Instalar MSYS2 desde https://www.msys2.org/ y abrir la consola "MSYS2 MinGW 64-bit".
2. Actualizar paquetes y toolchain:
```bash
pacman -Syu
# cierra la consola si pacman lo solicita, vuelve a abrir "MSYS2 MinGW 64-bit" y ejecuta:
pacman -Su
pacman -S --needed mingw-w64-x86_64-toolchain mingw-w64-x86_64-cmake mingw-w64-x86_64-ninja
```
3. Instalar SDL2 o SDL3 (los nombres de paquete pueden variar por mirror):
```bash
pacman -Syu      # (opcional) actualiza todo el sistema
pacman -S mingw-w64-x86_64-gcc mingw-w64-x86_64-cmake mingw-w64-x86_64-SDL3

```

Compilar desde MSYS2 (ejemplo)
- Compilar intentando usar SDL3:
```bash
cd "C:/Users/kenfe/OneDrive/Documentos/Progra/Tarea4ParadigmasCE/Tarea4ParadigmasCE/ClienteC"
cmake -B build -G "MinGW Makefiles"
cmake --build build
cd build/bin
./dkj_client.exe
```
