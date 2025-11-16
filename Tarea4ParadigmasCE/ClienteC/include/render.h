#pragma once
#include "game_state.h"
#include <SDL2/SDL.h>

typedef struct {
    SDL_Window*   win;
    SDL_Renderer* ren;
    SDL_Texture*  spritesheet;
} Gfx;

int  gfx_init(Gfx* g);
void gfx_shutdown(Gfx* g);
void gfx_draw_env(Gfx* g, const GameState* gs);
