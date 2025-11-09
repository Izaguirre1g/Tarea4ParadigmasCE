#include "constants.h"
#include "game_state.h"
#include <SDL3/SDL.h>

typedef struct {
    SDL_Window* win;
    SDL_Renderer* ren;
} Gfx;

int gfx_init(Gfx* g) {
    if (SDL_Init(SDL_INIT_VIDEO) < 0) return -1;
    g->win = SDL_CreateWindow("Donkey Kong Jr – Entorno", WIN_W, WIN_H, 0);
    if (!g->win) return -2;
    g->ren = SDL_CreateRenderer(g->win, NULL);
    if (!g->ren) return -3;
    return 0;
}

void gfx_shutdown(Gfx* g) {
    SDL_DestroyRenderer(g->ren);
    SDL_DestroyWindow(g->win);
    SDL_Quit();
}

/* --- Funciones auxiliares --- */
static void draw_rect(SDL_Renderer* r, int x, int y, int w, int h,
                      Uint8 R, Uint8 G, Uint8 B, Uint8 A) {
    SDL_SetRenderDrawColor(r, R,G,B,A);
    SDL_FRect rc = {x,y,w,h};
    SDL_RenderFillRect(r, &rc);
}

static void draw_line(SDL_Renderer* r, int x1,int y1,int x2,int y2,
                      Uint8 R,Uint8 G,Uint8 B,Uint8 A) {
    SDL_SetRenderDrawColor(r,R,G,B,A);
    SDL_RenderLine(r,x1,y1,x2,y2);
}

/* --- Dibujo principal --- */
void gfx_draw_env(Gfx* g, const GameState* gs) {
    SDL_SetRenderDrawColor(g->ren, COLOR_BG);
    SDL_RenderClear(g->ren);

    //* --- Plataformas (celestes) --- */
	for (int i = 0; i < N_PLAT; ++i) {
    	draw_rect(g->ren,
        	(int)PLATFORMS[i][0],
        	(int)PLATFORMS[i][1],
        	(int)PLATFORMS[i][2],
        	(int)PLATFORMS[i][3],
        	COLOR_PLATFORM);
	}

    /* --- Lianas verdes --- */
   	for (int i = 0; i < N_LIANA; ++i) {
    	int x1 = (int)LIANAS[i][0], y1 = (int)LIANAS[i][1];
    	int x2 = (int)LIANAS[i][2], y2 = (int)LIANAS[i][3];
    	draw_line(g->ren, x1, y1, x2, y2, COLOR_LIANA);
	}
    /* --- Frutas (del GameState) --- */
    for (int i = 0; i < gs->fruitsCount; i++) {
        Fruit f = gs->fruits[i];
        if (!f.active) continue;

        Uint8 R=255,G=230,B=60,A=255;
        if (f.points >= 70) { R=255;G=60;B=60; }     // roja
        else if (f.points >= 50) { R=255;G=230;B=60; } // amarilla
        else { R=200;G=60;B=255; }                   // morada
        draw_rect(g->ren, f.pos.x, f.pos.y, 12, 12, R,G,B,A);
    }

    /* --- Cocodrilos --- */
    for (int i = 0; i < gs->crocsCount; i++) {
        Croc c = gs->crocs[i];
        if (!c.alive) continue;
        if (c.type == CROC_RED)
            draw_rect(g->ren, c.pos.x, c.pos.y, 28, 18, COLOR_CROC_RED);
        else
            draw_rect(g->ren, c.pos.x, c.pos.y, 28, 18, COLOR_CROC_BLUE);
    }

    /* --- Jugadores (del servidor) --- */
    for (int i = 0; i < gs->playersCount; i++) {
        Player p = gs->players[i];
        if (!p.active) continue;
        draw_rect(g->ren, p.pos.x, p.pos.y, 24, 28, COLOR_PLAYER);
    }

    SDL_RenderPresent(g->ren);
}
