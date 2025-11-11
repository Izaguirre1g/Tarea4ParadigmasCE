#include <SDL3/SDL.h>
#include <stdio.h>
#include <string.h>
#include "constants.h"
#include "game_state.h"
#include "net.h"
#include "render.h"

int main(void) {
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        printf("Error iniciando SDL: %s\n", SDL_GetError());
        return 1;
    }

    Gfx gfx = {0};
    gfx.win = SDL_CreateWindow("DonCEy Kong Jr - Cliente", WIN_W, WIN_H, 0);
    if (!gfx.win) {
        printf("Error creando ventana: %s\n", SDL_GetError());
        SDL_Quit();
        return 1;
    }

    gfx.ren = SDL_CreateRenderer(gfx.win, NULL);
    if (!gfx.ren) {
        printf("Error creando renderer: %s\n", SDL_GetError());
        SDL_DestroyWindow(gfx.win);
        SDL_Quit();
        return 1;
    }

    GameState gs = {0};
    int sock = net_connect(SERVER_IP, SERVER_PORT);
    if (sock < 0) {
        printf("No se pudo conectar al servidor.\n");
        SDL_DestroyRenderer(gfx.ren);
        SDL_DestroyWindow(gfx.win);
        SDL_Quit();
        return 1;
    }

    net_start_receiver(sock, &gs);
    net_send_line(sock, "JOIN player DKJr");

    int running = 1;
    SDL_Event e;
    Uint64 last = SDL_GetTicks();  // control de FPS

    while (running) {
        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_EVENT_QUIT) running = 0;
            else if (e.type == SDL_EVENT_KEY_DOWN) {
                SDL_Keycode key = e.key.key;
                if (key == SDLK_ESCAPE) running = 0;
                else if (key == SDLK_LEFT)  net_send_line(sock, "INPUT 0 LEFT");
                else if (key == SDLK_RIGHT) net_send_line(sock, "INPUT 0 RIGHT");
                else if (key == SDLK_UP)    net_send_line(sock, "INPUT 0 UP");
                else if (key == SDLK_DOWN)  net_send_line(sock, "INPUT 0 DOWN");
                else if (key == SDLK_SPACE) net_send_line(sock, "INPUT 0 JUMP");
            }
            else if (e.type == SDL_EVENT_KEY_UP) {
                SDL_Keycode key = e.key.key;
                if (key == SDLK_LEFT || key == SDLK_RIGHT ||
                    key == SDLK_UP || key == SDLK_DOWN)
                    net_send_line(sock, "INPUT 0 STOP");
            }
        }

        // Control de 60 FPS (~16 ms)
        Uint64 now = SDL_GetTicks();
        if (now - last >= 16) {
            gfx_draw_env(&gfx, &gs);
            last = now;
        }
    }

    SDL_DestroyRenderer(gfx.ren);
    SDL_DestroyWindow(gfx.win);
    SDL_Quit();
    return 0;
}
