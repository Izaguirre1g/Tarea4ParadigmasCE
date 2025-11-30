#ifndef SPECTATOR_UI_H
#define SPECTATOR_UI_H

#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include "game_state.h"

/* Estructura principal del espectador */
typedef struct {
    SDL_Window* win;
    SDL_Renderer* ren;
    SDL_Texture* spritesheet;
    int selectedPlayerId;
    int isConnected;
} SpectatorUI;

/* Dropdown de jugadores para seleccionar a quién observar */
typedef struct {
    SDL_Rect box;
    int isOpen;
    int selectedIndex;
    int count;
    int ids[32];
    char names[32][32];
} PlayerDropDown;

/* Funciones principales */
int spectator_ui_init(SpectatorUI* ui);
void spectator_ui_shutdown(SpectatorUI* ui);
void spectator_ui_render(SpectatorUI* ui, const GameState* gs);
void spectator_ui_handle_click(SpectatorUI* ui, int x, int y, int sock);
void spectator_ui_update_players(const char* json);
void spectator_ui_run();

/* Función para dibujar texto */
void spectator_draw_text(SDL_Renderer* ren, TTF_Font* font, const char* txt, int x, int y);

#endif // SPECTATOR_UI_H