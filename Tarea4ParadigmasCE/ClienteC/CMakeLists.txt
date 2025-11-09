cmake_minimum_required(VERSION 3.16)
project(DonkeyKongJr_Client C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# --- Rutas ---
include_directories(include)
file(GLOB SRC_FILES src/*.c)

# --- Buscar SDL3 ---
# En sistemas modernos (vcpkg, brew, MSYS2) se instala como SDL3Config.cmake
find_package(SDL3 REQUIRED)

# --- Crear ejecutable ---
add_executable(dkj_client ${SRC_FILES})

# --- Enlace de librerías ---
target_link_libraries(dkj_client PRIVATE SDL3::SDL3)

if(WIN32)
    target_link_libraries(dkj_client PRIVATE ws2_32)
elseif(UNIX AND NOT APPLE)
    target_link_libraries(dkj_client PRIVATE pthread)
endif()

# pthread solo en sistemas UNIX
if(UNIX AND NOT APPLE)
    target_link_libraries(dkj_client PRIVATE pthread)
endif()

# --- Configuración visual ---
set_target_properties(dkj_client PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
)
if(WIN32)
    # Ruta por defecto en MSYS2/MinGW64
    set(SDL3_DLL "C:/msys64/mingw64/bin/SDL3.dll")

    # Si la DLL existe, crea un paso post-build que la copie junto al .exe
    if(EXISTS "${SDL3_DLL}")
        add_custom_command(TARGET dkj_client POST_BUILD
                COMMAND ${CMAKE_COMMAND} -E copy_if_different
                "${SDL3_DLL}" $<TARGET_FILE_DIR:dkj_client>
                COMMENT "Copiando SDL3.dll al directorio bin..."
        )
    else()
        message(WARNING "No se encontró SDL3.dll en ${SDL3_DLL}. Copiala manualmente si falla la ejecución.")
    endif()
endif()