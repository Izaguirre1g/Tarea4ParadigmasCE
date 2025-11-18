#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <stdio.h>
#include <string.h>
#include "spectator_ui.h"
#include "constants.h"
#include "net.h"
#include "game_state.h"
#include "render.h"

int main(int argc, char* argv[]) {
    (void)argc;
    (void)argv;

    printf("===========================================\n");
    printf("  DONKEY KONG JR - CLIENTE ESPECTADOR\n");
    printf("===========================================\n");

    // Inicializar SDL
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        printf("Error SDL_Init: %s\n", SDL_GetError());
        return 1;
    }

    if (TTF_Init() < 0) {
        printf("Error TTF_Init: %s\n", TTF_GetError());
        SDL_Quit();
        return 1;
    }

    // Crear interfaz del espectador
    SpectatorUI ui;
    if (spectator_ui_init(&ui) < 0) {
        printf("Error inicializando interfaz espectador\n");
        TTF_Quit();
        SDL_Quit();
        return 1;
    }

    // Estado del juego (lo que se observa)
    GameState gs = {0};
    gs.player.x = 0;
    gs.player.y = 0;
    gs.player.lives = 0;
    gs.player.score = 0;

    // Conectar al servidor
    printf("[SPECTATOR] Conectando a %s:%d...\n", SERVER_IP, SERVER_PORT);
    int sock = net_connect(SERVER_IP, SERVER_PORT);
    if (sock < 0) {
        printf("No se pudo conectar al servidor\n");
        spectator_ui_shutdown(&ui);
        TTF_Quit();
        SDL_Quit();
        return 1;
    }

    printf("[SPECTATOR] Conectado al servidor\n");

    // Iniciar hilo receptor para recibir frames
    net_start_receiver(sock, &gs);

    // Esperar un poco para que se establezca la conexión
    SDL_Delay(200);

    // Pedir lista de jugadores disponibles automáticamente
    printf("[SPECTATOR] Solicitando lista de jugadores...\n");
    // Enviar múltiples veces para asegurar
    net_send_line(sock, "ADMIN PLAYERS");
    SDL_Delay(100);
    net_send_line(sock, "ADMIN PLAYERS");
    SDL_Delay(100);
    net_send_line(sock, "ADMIN PLAYERS");

    // Esperar respuesta
    SDL_Delay(300);

    // Loop principal
    int running = 1;
    SDL_Event e;
    Uint32 last = SDL_GetTicks();

    printf("[SPECTATOR] Iniciando loop principal\n");
    printf("[SPECTATOR] Click en 'ACTUALIZAR LISTA' para ver jugadores\n");
    printf("[SPECTATOR] Presiona ESC para salir\n");

    while (running) {

        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_QUIT) {
                running = 0;
            }
            else if (e.type == SDL_KEYDOWN) {
                if (e.key.keysym.sym == SDLK_ESCAPE) {
                    running = 0;
                }
            }
            else if (e.type == SDL_MOUSEBUTTONDOWN) {
                spectator_ui_handle_click(&ui, e.button.x, e.button.y, sock);
            }
        }

        // Renderizar a 60 FPS
        Uint32 now = SDL_GetTicks();
        if (now - last >= 16) {
            spectator_ui_render(&ui, &gs);
            last = now;
        }

        SDL_Delay(1);
    }

    // Limpieza
    printf("[SPECTATOR] Cerrando conexión...\n");
    net_close(sock);
    spectator_ui_shutdown(&ui);
    TTF_Quit();
    SDL_Quit();

    printf("[SPECTATOR] Desconectado\n");
    return 0;
}