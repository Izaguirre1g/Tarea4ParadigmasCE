cmake_minimum_required(VERSION 4.0)
project(ClienteC C)

set(CMAKE_C_STANDARD 11)

# Aquí definimos el ejecutable con la ruta correcta
add_executable(cliente src/main.c)

# En Windows, enlazar la librería de sockets
if (WIN32)
    target_link_libraries(cliente ws2_32)
endif()