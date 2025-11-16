cmake_minimum_required(VERSION 3.16)
project(DonkeyKongJr_Client C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# --- Rutas ---
include_directories(include)
file(GLOB SRC_FILES src/*.c)

# --- Usar pkg-config para SDL2 y SDL2_image ---
find_package(PkgConfig REQUIRED)
pkg_check_modules(SDL2 REQUIRED sdl2)
pkg_check_modules(SDL2_IMAGE REQUIRED SDL2_image)

# --- Crear ejecutable ---
add_executable(dkj_client ${SRC_FILES})

# --- Incluir directorios de headers ---
target_include_directories(dkj_client PRIVATE
        ${SDL2_INCLUDE_DIRS}
        ${SDL2_IMAGE_INCLUDE_DIRS}
)

# --- Enlace de librerías ---
target_link_libraries(dkj_client PRIVATE
        ${SDL2_LIBRARIES}
        ${SDL2_IMAGE_LIBRARIES}
)

if(WIN32)
    target_link_libraries(dkj_client PRIVATE ws2_32)
endif()

if(UNIX AND NOT APPLE)
    target_link_libraries(dkj_client PRIVATE pthread)
endif()

# --- Configuración de salida ---
set_target_properties(dkj_client PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
)

# --- Copiar assets ---
add_custom_command(TARGET dkj_client POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_directory
        ${CMAKE_SOURCE_DIR}/assets $<TARGET_FILE_DIR:dkj_client>/assets
        COMMENT "Copiando carpeta assets..."
)

# --- Copiar DLLs en Windows ---
if(WIN32)
    set(SDL2_DLL "C:/msys64/mingw64/bin/SDL2.dll")
    set(SDL2_IMAGE_DLL "C:/msys64/mingw64/bin/SDL2_image.dll")

    if(EXISTS "${SDL2_DLL}")
        add_custom_command(TARGET dkj_client POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy_if_different
                "${SDL2_DLL}" $<TARGET_FILE_DIR:dkj_client>
                COMMENT "Copiando SDL2.dll..."
        )
    endif()

    if(EXISTS "${SDL2_IMAGE_DLL}")
        add_custom_command(TARGET dkj_client POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy_if_different
                "${SDL2_IMAGE_DLL}" $<TARGET_FILE_DIR:dkj_client>
                COMMENT "Copiando SDL2_image.dll..."
        )
    endif()
endif()