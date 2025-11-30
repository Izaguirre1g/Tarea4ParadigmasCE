#include <SDL2/SDL.h>
#include <stdio.h>
#include <string.h>
#include "constants.h"
#include "game_state.h"
#include "net.h"
#include "render.h"

int main_player(void) {
    // =============================
    //  Inicializar gráficos (SDL2)
    // =============================
    Gfx gfx = {0};
    if (gfx_init(&gfx) < 0) {
        printf("Error iniciando gráficos\n");
        return 1;
    }

    // =============================
    //  Estado del juego local
    // =============================
    GameState gs = {0};
    gs.player.x = 200;
    gs.player.y = 500;
    gs.player.lives = 3;
    gs.player.score = 0;

    // =============================
    //  Conectar al servidor
    // =============================
    int sock = net_connect(SERVER_IP, SERVER_PORT);
    if (sock < 0) {
        printf("No se pudo conectar al servidor.\n");
        gfx_shutdown(&gfx);
        return 1;
    }

    // Iniciar hilo receptor
    net_start_receiver(sock, &gs);
    net_send_line(sock, "JOIN player DKJr");

    // =============================
    //  Loop principal del juego
    // =============================
    int running = 1;
    SDL_Event e;
    Uint32 last = SDL_GetTicks();

    while (running) {

        while (SDL_PollEvent(&e)) {

            if (e.type == SDL_QUIT) running = 0;

            else if (e.type == SDL_KEYDOWN) {
                SDL_Keycode key = e.key.keysym.sym;

                if (key == SDLK_ESCAPE) running = 0;
                else if (key == SDLK_LEFT)  net_send_line(sock, "INPUT 0 LEFT");
                else if (key == SDLK_RIGHT) net_send_line(sock, "INPUT 0 RIGHT");
                else if (key == SDLK_UP)    net_send_line(sock, "INPUT 0 UP");
                else if (key == SDLK_DOWN)  net_send_line(sock, "INPUT 0 DOWN");
                else if (key == SDLK_SPACE) net_send_line(sock, "INPUT 0 JUMP");
            }

            else if (e.type == SDL_KEYUP) {
                SDL_Keycode key = e.key.keysym.sym;

                if (key == SDLK_LEFT || key == SDLK_RIGHT ||
                    key == SDLK_UP || key == SDLK_DOWN)
                {
                    net_send_line(sock, "INPUT 0 STOP");
                }
            }
        }

        // =============================
        //     Dibujar a 60 FPS
        // =============================
        Uint32 now = SDL_GetTicks();
        if (now - last >= 16) {
            gfx_draw_env(&gfx, &gs);
            last = now;
        }

        SDL_Delay(1);
    }

    // =============================
    //    Limpieza
    // =============================
    net_close(sock);
    gfx_shutdown(&gfx);
    return 0;
}

