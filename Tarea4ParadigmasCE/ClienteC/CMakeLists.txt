cmake_minimum_required(VERSION 3.16)
project(DonkeyKongJr_Client C)

set(CMAKE_C_STANDARD 11)

include_directories(include)

# ======================================================
#   Buscar SDL2, SDL2_image y SDL2_ttf UNA SOLA VEZ
# ======================================================
find_package(PkgConfig REQUIRED)
pkg_check_modules(SDL2 REQUIRED sdl2)
pkg_check_modules(SDL2_IMAGE REQUIRED SDL2_image)
pkg_check_modules(SDL2_TTF REQUIRED SDL2_ttf)

# ======================================================
#   EJECUTABLE 1: dkj_client  (jugador)
# ======================================================
file(GLOB GAME_SRC
        src/main.c
        src/render.c
        src/game_state.c
        src/net.c
        src/json_parse.c
)

add_executable(dkj_client ${GAME_SRC})

target_include_directories(dkj_client PRIVATE
        ${SDL2_INCLUDE_DIRS}
        ${SDL2_IMAGE_INCLUDE_DIRS}
        ${SDL2_TTF_INCLUDE_DIRS}
)

target_link_libraries(dkj_client PRIVATE
        ${SDL2_LIBRARIES}
        ${SDL2_IMAGE_LIBRARIES}
        ${SDL2_TTF_LIBRARIES}
)

if(WIN32)
    target_link_libraries(dkj_client PRIVATE ws2_32)
endif()


# ======================================================
#   EJECUTABLE 2: admin_client  (interfaz admin)
# ======================================================
add_executable(admin_client
        src/admin_main.c
        src/admin_ui.c
        src/net.c
        src/json_parse.c
        src/game_state.c   # requerido por net.c
)

target_include_directories(admin_client PRIVATE
        ${SDL2_INCLUDE_DIRS}
        ${SDL2_IMAGE_INCLUDE_DIRS}
        ${SDL2_TTF_INCLUDE_DIRS}
)

target_link_libraries(admin_client PRIVATE
        ${SDL2_LIBRARIES}
        ${SDL2_IMAGE_LIBRARIES}
        ${SDL2_TTF_LIBRARIES}
)

if(WIN32)
    target_link_libraries(admin_client PRIVATE ws2_32)
endif()


# ======================================================
#   SALIDA DE BINARIOS
# ======================================================
set_target_properties(dkj_client PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

set_target_properties(admin_client PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# ======================================================
#   COPIAR ASSETS AL JUGADOR (el admin NO usa assets)
# ======================================================
add_custom_command(TARGET dkj_client POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory
        ${CMAKE_SOURCE_DIR}/assets
        $<TARGET_FILE_DIR:dkj_client>/assets
)

# ============================================
# Ejecutable: Cliente Espectador
# ============================================
add_executable(spectator_client
        src/spectator_main.c
        src/spectator_ui.c
        src/net.c
        src/game_state.c
        src/json_parse.c
        src/render.c
)

target_include_directories(spectator_client PRIVATE include)
target_compile_definitions(spectator_client PRIVATE SPECTATOR_CLIENT)  # ← ESTA LÍNEA ES CLAVE
target_link_libraries(spectator_client ${SDL2_LIBRARIES} ${SDL2_IMAGE_LIBRARIES} ${SDL2_TTF_LIBRARIES})

if(WIN32)
    target_link_libraries(spectator_client ws2_32)
else()
    target_link_libraries(spectator_client pthread)
endif()

set_target_properties(spectator_client PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
)