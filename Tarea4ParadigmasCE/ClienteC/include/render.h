#pragma once
#include "game_state.h"
#include <SDL2/SDL.h>

typedef struct {
    SDL_Window*   win;
    SDL_Renderer* ren;
    SDL_Texture*  spritesheet;

    // Texturas individuales para entidades
    SDL_Texture*  tex_player;
    SDL_Texture*  tex_croc_red;
    SDL_Texture*  tex_croc_blue;
    SDL_Texture*  tex_fruit_banana;
    SDL_Texture*  tex_fruit_orange;
    SDL_Texture*  tex_fruit_strawberry;
} Gfx;

int  gfx_init(Gfx* g);
void gfx_shutdown(Gfx* g);
void gfx_draw_env(Gfx* g, const GameState* gs);
