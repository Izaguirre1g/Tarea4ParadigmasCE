#include "render.h"
#include "constants.h"

int gfx_init(Gfx* g){
    if (SDL_Init(SDL_INIT_VIDEO) < 0) return -1;
    g->win = SDL_CreateWindow("Donkey Kong Jr", WIN_W, WIN_H, 0);
    if (!g->win) return -2;
    g->ren = SDL_CreateRenderer(g->win, NULL);
    if (!g->ren) return -3;
    return 0;
}

void gfx_shutdown(Gfx* g){
    if (g->ren) SDL_DestroyRenderer(g->ren);
    if (g->win) SDL_DestroyWindow(g->win);
    SDL_Quit();
}

static void draw_rect(SDL_Renderer* r, float x,float y,float w,float h){
    SDL_FRect rc = {x,y,w,h};
    SDL_SetRenderDrawColor(r, 255, 0, 0, 255);
    SDL_RenderFillRect(r, &rc);
}

void gfx_draw(Gfx* g, const GameState* gs){
    SDL_SetRenderDrawColor(g->ren, 30,144,255,255);
    SDL_RenderClear(g->ren);

    // Jugadores (rojo)
    for (int i=0;i<gs->playersCount;i++){
        if (gs->players[i].active)
            draw_rect(g->ren, gs->players[i].pos.x, gs->players[i].pos.y, 28, 28);
    }

    // Cocodrilos (verde/azul)
    for (int i=0;i<gs->crocsCount;i++){
        if (!gs->crocs[i].alive) continue;
        if (gs->crocs[i].type==CROC_RED) SDL_SetRenderDrawColor(g->ren, 200,0,0,255);
        else                              SDL_SetRenderDrawColor(g->ren, 0,100,255,255);
        SDL_FRect rc = {gs->crocs[i].pos.x, gs->crocs[i].pos.y, 24, 24};
        SDL_RenderFillRect(g->ren, &rc);
    }

    // Frutas (amarillo)
    for (int i=0;i<gs->fruitsCount;i++){
        if (!gs->fruits[i].active) continue;
        SDL_SetRenderDrawColor(g->ren, 255,220,0,255);
        SDL_FRect rc = {gs->fruits[i].pos.x, gs->fruits[i].pos.y, 16, 16};
        SDL_RenderFillRect(g->ren, &rc);
    }

    SDL_RenderPresent(g->ren);
}
