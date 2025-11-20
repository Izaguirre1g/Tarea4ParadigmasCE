#pragma once
#include "game_state.h"
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>

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

    // Texturas para HUD y objetivos
    SDL_Texture*  tex_jail;          // Jaula de DK
    SDL_Texture*  tex_heart;         // Corazón para vidas
    SDL_Texture*  tex_scoreholder;   // Fondo para puntuación

    // Texturas (elementos del nivel)
    SDL_Texture*  tex_donkey_kong;   // Donkey Kong
    SDL_Texture*  tex_mario;         // Mario
    SDL_Texture*  tex_liana;         // Textura de liana
    SDL_Texture*  tex_platform;      // Textura de plataforma

    //Texturas para decoración visual
    SDL_Texture*  tex_background;    // Fondo del juego
    SDL_Texture*  tex_water;         // Agua/decoración base

    // Fuente para texto
    TTF_Font*     font;              // Fuente para números y texto
} Gfx;

int  gfx_init(Gfx* g);
void gfx_shutdown(Gfx* g);
void gfx_draw_env(Gfx* g, const GameState* gs);