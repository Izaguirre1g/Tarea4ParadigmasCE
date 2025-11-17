//
// Created by Jose on 16/11/2025.
//
#define SDL_MAIN_HANDLED   // Importante en Windows antes de SDL
#include <SDL2/SDL.h>

#include <stdio.h>
#include "admin_ui.h"

int main() {

    printf("Cargando panel de administración...\n");

    admin_ui_run();  // Abre la ventana independiente del admin

    printf("Panel admin cerrado.\n");
    return 0;
}
