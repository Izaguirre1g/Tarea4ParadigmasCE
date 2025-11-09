#pragma once
#include "game_state.h"

typedef struct {
    SDL_Window*   win;
    SDL_Renderer* ren;
} Gfx;

int  gfx_init(Gfx* g);
void gfx_shutdown(Gfx* g);
void gfx_draw(Gfx* g, const GameState* gs);
