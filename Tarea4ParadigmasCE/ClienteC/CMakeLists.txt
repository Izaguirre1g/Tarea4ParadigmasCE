cmake_minimum_required(VERSION 4.0)
project(ClienteC C)

set(CMAKE_C_STANDARD 11)

# Opción para forzar SDL2 aunque SDL3 esté instalada
option(FORCE_SDL2 "Prefer SDL2 even if SDL3 is found" OFF)

# Aquí definimos el ejecutable con la ruta correcta
add_executable(cliente src/main.c)

# En Windows, enlazar la librería de sockets
if (WIN32)
    target_link_libraries(cliente ws2_32)
endif()

# Opción para habilitar interfaz gráfica con SDL
option(USE_SDL "Enable SDL3 GUI (adds cliente_sdl executable)" OFF)

if (USE_SDL)
    if (NOT FORCE_SDL2)
        # Intentamos encontrar SDL3 (instalado vía vcpkg, MSYS2 o paquete sistem)
        # Preferimos el modo CONFIG que funciona con vcpkg/SDL3Config.cmake
        find_package(SDL3 CONFIG QUIET)
    endif()

    if (SDL3_FOUND AND NOT FORCE_SDL2)
        message(STATUS "Found SDL3 (using SDL3)")
        add_executable(cliente_sdl src/sdl_demo.c)
        target_link_libraries(cliente_sdl PRIVATE SDL3::SDL3)
    else()
        # Fallback o forzado a SDL2 si SDL3 no está disponible o si FORCE_SDL2 está ON
        if (FORCE_SDL2)
            message(STATUS "FORCE_SDL2 is ON: preferring SDL2 even if SDL3 is present")
        endif()
        find_package(SDL2 QUIET)
        if (SDL2_FOUND)
            message(STATUS "Using SDL2")
            add_executable(cliente_sdl src/sdl_demo.c)
            target_compile_definitions(cliente_sdl PRIVATE USE_SDL2)
            # SDL2 on MinGW needs SDL2main to provide WinMain wrapper
            # SDL2main depends on SDL2; link SDL2main first then SDL2 so the linker
            # can resolve symbols properly (order matters for static libraries).
            target_link_libraries(cliente_sdl PRIVATE SDL2::SDL2main SDL2::SDL2)
        else()
            message(FATAL_ERROR "Neither SDL3 nor SDL2 were found. Instala SDL3 o SDL2 y vuelve a intentar.")
        endif()
    endif()

    # Si el proyecto necesita también ws2_32 para networking, enlazarlo en Windows
    if (WIN32)
        target_link_libraries(cliente_sdl PRIVATE ws2_32)
    endif()
endif()
