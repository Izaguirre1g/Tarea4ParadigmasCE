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
# intentar SDL3 (si está disponible en tu mirror)
pacman -S mingw-w64-x86_64-SDL3 || true
# si SDL3 no está disponible o prefieres SDL2
pacman -S mingw-w64-x86_64-SDL2
```

Compilar desde MSYS2 (ejemplo)
- Compilar intentando usar SDL3 (por defecto):
```bash
cd /c/Users/kenfe/OneDrive/Documentos/Progra/Tarea4ParadigmasCE/Tarea4ParadigmasCE/ClienteC
rm -rf build
mkdir -p build && cd build
cmake -G "MinGW Makefiles" -DUSE_SDL=ON -DCMAKE_PREFIX_PATH=/mingw64 ..
mingw32-make
./cliente_sdl.exe
```
- Forzar SDL2 si quieres asegurar compatibilidad:
```bash
rm -rf build
mkdir -p build && cd build
cmake -G "MinGW Makefiles" -DUSE_SDL=ON -DFORCE_SDL2=ON -DCMAKE_PREFIX_PATH=/mingw64 ..
mingw32-make
./cliente_sdl.exe
```

Ejecución desde cmd.exe (Windows)
- Si ejecutas el ejecutable desde la línea de comandos de Windows, asegúrate de que las DLLs de MSYS2 estén en el PATH o copia las DLLs al directorio del ejecutable:
```cmd
cd /d C:\ruta\al\repositorio\ClienteC\build
set PATH=C:\msys64\mingw64\bin;%PATH%
cliente_sdl.exe
```
