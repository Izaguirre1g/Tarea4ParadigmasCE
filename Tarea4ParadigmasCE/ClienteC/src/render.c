#include <stdio.h>
#include "game_state.h"
#include "constants.h"
#include <SDL3/SDL.h>
#include "render.h"

void draw_rect(SDL_Renderer* r, int x, int y, int w, int h, Uint8 R, Uint8 G, Uint8 B, Uint8 A) {
    SDL_SetRenderDrawColor(r, R, G, B, A);
    SDL_FRect rect = {x, y, w, h};
    SDL_RenderFillRect(r, &rect);
}

void draw_line(SDL_Renderer* r, int x1, int y1, int x2, int y2, Uint8 R, Uint8 G, Uint8 B, Uint8 A) {
    SDL_SetRenderDrawColor(r, R, G, B, A);
    SDL_RenderLine(r, x1, y1, x2, y2);
}

void gfx_draw_env(Gfx* g, const GameState* gs) {
    SDL_Renderer* r = g->ren;

    // Fondo oscuro
    SDL_SetRenderDrawColor(r, COLOR_BG);
    SDL_RenderClear(r);

    // Plataformas
    for (int i = 0; i < N_PLAT; ++i)
        draw_rect(r, PLATFORMS[i][0], PLATFORMS[i][1], PLATFORMS[i][2], PLATFORMS[i][3], COLOR_PLATFORM);

    // Lianas
    for (int i = 0; i < N_LIANA; ++i)
        draw_line(r, LIANAS[i][0], LIANAS[i][1], LIANAS[i][2], LIANAS[i][3], COLOR_LIANA);

    // Frutas
    for (int i = 0; i < gs->fruitsCount; ++i) {
        Fruit f = gs->fruits[i];
        if (!f.active) continue;
        if (strstr(f.type, "BANANA"))
            draw_rect(r, f.x, f.y, 12, 12, COLOR_FRUIT_BANANA);
        else if (strstr(f.type, "NARANJA"))
            draw_rect(r, f.x, f.y, 12, 12, COLOR_FRUIT_NARANJA);
        else
            draw_rect(r, f.x, f.y, 12, 12, COLOR_FRUIT_CEREZA);
    }

    // Cocodrilos
    for (int i = 0; i < gs->crocsCount; ++i) {
        Croc c = gs->crocs[i];
        if (!c.alive) continue;
        if (c.isRed)
            draw_rect(g->ren, c.x, c.y, 28, 18, COLOR_CROC_RED);
        else
            draw_rect(g->ren, c.x, c.y, 28, 18, COLOR_CROC_BLUE);
    }



    // Jugador
    draw_rect(r, gs->player.x, gs->player.y, 24, 28, COLOR_PLAYER);

    // HUD (temporal, por consola)
    printf("Score: %d   Lives: %d\r", gs->player.score, gs->player.lives);
    fflush(stdout);

    SDL_RenderPresent(r);
}
