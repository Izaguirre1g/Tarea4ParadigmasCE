#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <stdio.h>
#include <string.h>
#include "constants.h"
#include "net.h"

/* ======================================================
                 MENÚ DE SELECCIÓN DE MODO
   ====================================================== */

typedef enum {
    MODE_NONE = 0,
    MODE_PLAYER,
    MODE_SPECTATOR,
    MODE_ADMIN
} AppMode;

typedef struct {
    SDL_Rect rect;
    const char* label;
    AppMode mode;
} MenuButton;

static MenuButton menu_buttons[] = {
    {{0, 0, 300, 60}, "JUGAR", MODE_PLAYER},
    {{0, 0, 300, 60}, "ESPECTADOR", MODE_SPECTATOR},
    {{0, 0, 300, 60}, "ADMINISTRADOR", MODE_ADMIN}
};

static const int NUM_BUTTONS = 3;

/* ======================================================
                 FUNCIONES DE DIBUJO
   ====================================================== */

static void draw_text_centered(SDL_Renderer* ren, TTF_Font* font, const char* text,
                               int x, int y, int w, SDL_Color color) {
    if (!font || !text) return;

    SDL_Surface* surf = TTF_RenderText_Blended(font, text, color);
    if (!surf) return;

    SDL_Texture* tex = SDL_CreateTextureFromSurface(ren, surf);
    if (!tex) {
        SDL_FreeSurface(surf);
        return;
    }

    // Centrar el texto
    int text_x = x + (w - surf->w) / 2;
    int text_y = y + 15;

    SDL_Rect dst = {text_x, text_y, surf->w, surf->h};
    SDL_RenderCopy(ren, tex, NULL, &dst);

    SDL_FreeSurface(surf);
    SDL_DestroyTexture(tex);
}

static void draw_menu(SDL_Renderer* ren, TTF_Font* font, int hover_index) {
    // Fondo
    SDL_SetRenderDrawColor(ren, 20, 20, 30, 255);
    SDL_RenderClear(ren);

    // Título
    SDL_Color title_color = {255, 200, 50, 255};
    if (font) {
        SDL_Surface* title = TTF_RenderText_Blended(font, "DONKEY KONG JR", title_color);
        if (title) {
            SDL_Texture* tex = SDL_CreateTextureFromSurface(ren, title);
            if (tex) {
                SDL_Rect dst = {150, 50, title->w, title->h};
                SDL_RenderCopy(ren, tex, NULL, &dst);
                SDL_DestroyTexture(tex);
            }
            SDL_FreeSurface(title);
        }

        SDL_Color subtitle_color = {200, 200, 200, 255};
        SDL_Surface* subtitle = TTF_RenderText_Blended(font, "Selecciona un modo", subtitle_color);
        if (subtitle) {
            SDL_Texture* tex = SDL_CreateTextureFromSurface(ren, subtitle);
            if (tex) {
                SDL_Rect dst = {200, 100, subtitle->w, subtitle->h};
                SDL_RenderCopy(ren, tex, NULL, &dst);
                SDL_DestroyTexture(tex);
            }
            SDL_FreeSurface(subtitle);
        }
    }

    // Calcular posiciones de botones (centrados verticalmente)
    int start_y = 180;
    int spacing = 80;

    for (int i = 0; i < NUM_BUTTONS; i++) {
        menu_buttons[i].rect.x = 200;
        menu_buttons[i].rect.y = start_y + (i * spacing);

        SDL_Rect* r = &menu_buttons[i].rect;

        // Color del botón (más claro si está en hover)
        if (i == hover_index) {
            SDL_SetRenderDrawColor(ren, 100, 150, 200, 255); // Azul claro
        } else {
            SDL_SetRenderDrawColor(ren, 70, 100, 150, 255); // Azul oscuro
        }

        SDL_RenderFillRect(ren, r);

        // Borde
        SDL_SetRenderDrawColor(ren, 200, 200, 200, 255);
        SDL_RenderDrawRect(ren, r);

        // Texto del botón
        SDL_Color text_color = {255, 255, 255, 255};
        draw_text_centered(ren, font, menu_buttons[i].label,
                          r->x, r->y, r->w, text_color);
    }

    // Instrucciones
    if (font) {
        SDL_Color inst_color = {150, 150, 150, 255};
        SDL_Surface* inst = TTF_RenderText_Blended(font,
            "Haz clic en una opcion o presiona 1, 2, 3", inst_color);
        if (inst) {
            SDL_Texture* tex = SDL_CreateTextureFromSurface(ren, inst);
            if (tex) {
                SDL_Rect dst = {120, 480, inst->w, inst->h};
                SDL_RenderCopy(ren, tex, NULL, &dst);
                SDL_DestroyTexture(tex);
            }
            SDL_FreeSurface(inst);
        }
    }

    SDL_RenderPresent(ren);
}

/* ======================================================
                 DETECCIÓN DE HOVER
   ====================================================== */

static int get_hovered_button(int mouse_x, int mouse_y) {
    for (int i = 0; i < NUM_BUTTONS; i++) {
        SDL_Rect* r = &menu_buttons[i].rect;
        if (mouse_x >= r->x && mouse_x <= r->x + r->w &&
            mouse_y >= r->y && mouse_y <= r->y + r->h) {
            return i;
        }
    }
    return -1;
}

/* ======================================================
                 MENÚ PRINCIPAL
   ====================================================== */

static AppMode show_menu() {
    SDL_Window* win = SDL_CreateWindow("DK Jr - Launcher",
                                      SDL_WINDOWPOS_CENTERED,
                                      SDL_WINDOWPOS_CENTERED,
                                      700, 540,
                                      SDL_WINDOW_SHOWN);
    if (!win) {
        printf("Error creando ventana: %s\n", SDL_GetError());
        return MODE_NONE;
    }

    SDL_Renderer* ren = SDL_CreateRenderer(win, -1, SDL_RENDERER_ACCELERATED);
    if (!ren) {
        printf("Error creando renderer: %s\n", SDL_GetError());
        SDL_DestroyWindow(win);
        return MODE_NONE;
    }

    if (TTF_Init() < 0) {
        printf("Error TTF_Init: %s\n", TTF_GetError());
        SDL_DestroyRenderer(ren);
        SDL_DestroyWindow(win);
        return MODE_NONE;
    }

    TTF_Font* font = TTF_OpenFont("assets/arial.ttf", 24);
    if (!font) {
        printf("Advertencia: No se pudo cargar fuente\n");
    }

    AppMode selected_mode = MODE_NONE;
    int hover_index = -1;
    SDL_Event e;
    int running = 1;

    while (running) {
        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_QUIT) {
                running = 0;
            }
            else if (e.type == SDL_MOUSEMOTION) {
                hover_index = get_hovered_button(e.motion.x, e.motion.y);
            }
            else if (e.type == SDL_MOUSEBUTTONDOWN) {
                int clicked = get_hovered_button(e.button.x, e.button.y);
                if (clicked >= 0) {
                    selected_mode = menu_buttons[clicked].mode;
                    running = 0;
                }
            }
            else if (e.type == SDL_KEYDOWN) {
                switch (e.key.keysym.sym) {
                    case SDLK_1:
                        selected_mode = MODE_PLAYER;
                        running = 0;
                        break;
                    case SDLK_2:
                        selected_mode = MODE_SPECTATOR;
                        running = 0;
                        break;
                    case SDLK_3:
                        selected_mode = MODE_ADMIN;
                        running = 0;
                        break;
                    case SDLK_ESCAPE:
                        running = 0;
                        break;
                }
            }
        }

        draw_menu(ren, font, hover_index);
        SDL_Delay(16);
    }

    if (font) TTF_CloseFont(font);
    TTF_Quit();
    SDL_DestroyRenderer(ren);
    SDL_DestroyWindow(win);

    return selected_mode;
}

/* ======================================================
                 MAIN - LAUNCHER UNIFICADO
   ====================================================== */

// Declaraciones de funciones externas
extern int main_player(void);
extern void spectator_ui_main(void);
extern void admin_ui_run(int sock);

int main(void) {
    printf("===========================================\n");
    printf("   DONKEY KONG JR - LAUNCHER UNIFICADO\n");
    printf("===========================================\n\n");

    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        printf("Error SDL_Init: %s\n", SDL_GetError());
        return 1;
    }

    AppMode mode = show_menu();

    SDL_Quit();

    printf("\n");

    switch (mode) {
        case MODE_PLAYER:
            printf(">>> Iniciando MODO JUGADOR...\n\n");
            return main_player();

        case MODE_SPECTATOR:
            printf(">>> Iniciando MODO ESPECTADOR...\n\n");
            spectator_ui_main();
            return 0;

        case MODE_ADMIN:
            printf(">>> Iniciando MODO ADMINISTRADOR...\n\n");
            {
                int sock = net_connect(SERVER_IP, SERVER_PORT);
                if (sock < 0) {
                    printf("No se pudo conectar al servidor.\n");
                    return 1;
                }
                admin_ui_run(sock);
                net_close(sock);
            }
            return 0;

        case MODE_NONE:
        default:
            printf(">>> Saliendo...\n");
            return 0;
    }
}

