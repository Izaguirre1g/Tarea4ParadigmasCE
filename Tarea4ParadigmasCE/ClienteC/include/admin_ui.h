#ifndef ADMIN_UI_H
#define ADMIN_UI_H

#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>

typedef struct {
    SDL_Window* win;
    SDL_Renderer* ren;
} AdminUI;

/* Dropdown de jugadores */
typedef struct {
    SDL_Rect box;
    int isOpen;
    int selectedIndex;
    int count;
    int ids[32];
    char names[32][32];
} DropDown;

int admin_ui_init(AdminUI* ui);
void admin_ui_render(AdminUI* ui);
void admin_ui_handle_click(int x, int y, int sock);
void admin_ui_handle_text(const char* text);
void admin_ui_run();
void admin_ui_update_players(const char* json);

#endif
