//
// Created by Jose on 16/11/2025.
//

#ifndef ADMIN_UI_H
#define ADMIN_UI_H

#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>

/**
 * Estructura principal que representa la ventana
 * y recursos gráficos del panel de administración.
 */
typedef struct {
    SDL_Window* win;
    SDL_Renderer* ren;
    TTF_Font* font;
} AdminUI;

/**
 * Inicializa la ventana, renderer y fuente TTF.
 */
int admin_ui_init(AdminUI* ui);

/**
 * Dibuja todos los botones y elementos visuales.
 */
void admin_ui_render(AdminUI* ui);

/**
 * Maneja clics del mouse sobre botones y dropdowns.
 */
void admin_ui_handle_click(int x, int y, int sock);

/**
 * Maneja texto ingresado (no usado todavía, pero útil para expandir).
 */
void admin_ui_handle_text(const char* text);

/**
 * Función principal: crea la ventana SDL,
 * procesa eventos y llama render loops.
 */
void admin_ui_run();

#endif
