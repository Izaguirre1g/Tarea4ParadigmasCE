//
// Created by Jose on 16/11/2025.
//
#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <stdio.h>
#include "admin_ui.h"
#include "net.h"
#include "constants.h"   // donde tengas SERVER_IP / SERVER_PORT

int main(void) {
    printf("Cargando panel de administración...\n");

    int sock = net_connect(SERVER_IP, SERVER_PORT);
    if (sock < 0) {
        printf("No se pudo conectar al servidor.\n");
        return 1;
    }

    admin_ui_run(sock);

    net_close(sock);
    return 0;
}
