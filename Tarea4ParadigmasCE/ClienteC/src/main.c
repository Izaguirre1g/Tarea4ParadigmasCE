#include "constants.h"
#include "net.h"
#include "render.h"
#include "game_state.h"
#include <SDL3/SDL.h>
#include <stdio.h>
#include <string.h>

int main() {
    GameState gs; gs_init(&gs);

    int sock = net_connect(SERVER_IP, SERVER_PORT);
    if (sock < 0) { printf("No se pudo conectar.\n"); return 1; }

    // JOIN como jugador (cámbialo a spectator si quieres probar)
    net_send_line(sock, "JOIN player DKJr");
    net_start_receiver(sock, &gs);

    Gfx gfx; if (gfx_init(&gfx) != 0) return 1;

    int running = 1;
    while (running) {
        SDL_Event e;
        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_EVENT_QUIT) running = 0;
            if (e.type == SDL_EVENT_KEY_DOWN) {
                if (e.key.key == SDLK_ESCAPE) running = 0;
                else if (e.key.key == SDLK_LEFT)  net_send_line(sock, "INPUT 0 LEFT");
                else if (e.key.key == SDLK_RIGHT) net_send_line(sock, "INPUT 0 RIGHT");
                else if (e.key.key == SDLK_UP)    net_send_line(sock, "INPUT 0 UP");
                else if (e.key.key == SDLK_DOWN)  net_send_line(sock, "INPUT 0 DOWN");
                else if (e.key.key == SDLK_SPACE) net_send_line(sock, "INPUT 0 JUMP");
            }
            if (e.type == SDL_EVENT_KEY_UP) {
                if (e.key.key == SDLK_LEFT ||
                    e.key.key == SDLK_RIGHT ||
                    e.key.key == SDLK_UP ||
                    e.key.key == SDLK_DOWN)
                    net_send_line(sock, "INPUT 0 STOP");
            }
        }

        gfx_draw_env(&gfx, &gs);
        SDL_Delay(TICK_MS);
    }

    net_close(sock);
    gfx_shutdown(&gfx);
    return 0;
}
