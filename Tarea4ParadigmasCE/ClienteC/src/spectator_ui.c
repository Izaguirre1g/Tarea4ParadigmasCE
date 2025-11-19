#define SDL_MAIN_HANDLED
#include "spectator_ui.h"
#include "net.h"
#include "render.h"
#include "constants.h"
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <SDL2/SDL_image.h>
#include <stdio.h>
#include <string.h>

/* ======================================================
                 VARIABLES GLOBALES
   ====================================================== */

static TTF_Font* ui_font = NULL;
static PlayerDropDown playerDropDown;

// ← CAMBIO CLAVE: Usar Gfx completo en lugar de solo spritesheet
static Gfx gfx_spectator = {0};

/* ======================================================
                 FUNCIONES DE TEXTO
   ====================================================== */

void spectator_draw_text(SDL_Renderer* ren, TTF_Font* font, const char* txt, int x, int y) {
    if (!font || !txt) return;

    SDL_Color color = {255, 255, 255, 255}; // Blanco
    SDL_Surface* surf = TTF_RenderText_Blended(font, txt, color);
    if (!surf) return;

    SDL_Texture* tex = SDL_CreateTextureFromSurface(ren, surf);
    if (!tex) {
        SDL_FreeSurface(surf);
        return;
    }

    SDL_Rect r = {x, y, surf->w, surf->h};
    SDL_RenderCopy(ren, tex, NULL, &r);

    SDL_FreeSurface(surf);
    SDL_DestroyTexture(tex);
}

/* ======================================================
                 INICIALIZACIÓN
   ====================================================== */

int spectator_ui_init(SpectatorUI* ui) {

    ui->win = SDL_CreateWindow("ESPECTADOR - Donkey Kong Jr",
                               SDL_WINDOWPOS_CENTERED,
                               SDL_WINDOWPOS_CENTERED,
                               WIN_W, WIN_H + 120, // Espacio extra para controles
                               SDL_WINDOW_SHOWN);

    if (!ui->win) {
        printf("Error creando ventana: %s\n", SDL_GetError());
        return -1;
    }

    ui->ren = SDL_CreateRenderer(ui->win, -1, SDL_RENDERER_ACCELERATED);
    if (!ui->ren) {
        printf("Error creando renderer: %s\n", SDL_GetError());
        return -1;
    }

    // Cargar fuente
    if (TTF_Init() < 0) {
        printf("Error TTF_Init: %s\n", TTF_GetError());
        return -1;
    }

    ui_font = TTF_OpenFont("assets/arial.ttf", 18);
    if (!ui_font) {
        printf("Advertencia: No se pudo cargar fuente TTF\n");
    }

    // ← CAMBIO CLAVE: Inicializar Gfx completo usando gfx_init()
    gfx_spectator.win = ui->win;
    gfx_spectator.ren = ui->ren;

    // Llamar a gfx_init para cargar TODAS las texturas
    // (esto cargará spritesheet + texturas individuales)
    printf("[SPECTATOR] Inicializando sistema gráfico...\n");
    if (gfx_init(&gfx_spectator) < 0) {
        printf("[SPECTATOR] Advertencia: Error cargando gráficos\n");
        // No es fatal, continuamos
    }

    // Inicializar dropdown (a la derecha para evitar superposición)
    playerDropDown.box = (SDL_Rect){ WIN_W / 2 + 120, WIN_H + 30, 250, 35 };
    playerDropDown.isOpen = 0;
    playerDropDown.selectedIndex = -1;
    playerDropDown.count = 0;

    ui->selectedPlayerId = -1;
    ui->isConnected = 0;
    ui->spritesheet = gfx_spectator.spritesheet;

    return 0;
}

/* ======================================================
                 SHUTDOWN
   ====================================================== */

void spectator_ui_shutdown(SpectatorUI* ui) {
    // ← Usar gfx_shutdown() para limpiar correctamente
    gfx_shutdown(&gfx_spectator);

    if (ui_font) {
        TTF_CloseFont(ui_font);
    }
    TTF_Quit();
}

/* ======================================================
                 ACTUALIZAR LISTA DE JUGADORES
   ====================================================== */

void spectator_ui_update_players(const char* json) {

    printf("[SPECTATOR] JSON recibido: %s\n", json);

    playerDropDown.count = 0;

    const char* p = json;

    while ((p = strstr(p, "\"id\"")) != NULL) {
        int id;
        char name[32];

        if (sscanf(p, "\"id\":%d,\"name\":\"%31[^\"]\"", &id, name) == 2) {
            int i = playerDropDown.count++;
            playerDropDown.ids[i] = id;
            strncpy(playerDropDown.names[i], name, 31);
            playerDropDown.names[i][31] = '\0';

            printf("[SPECTATOR] Jugador encontrado: ID=%d, Name=%s\n", id, name);
        }
        p++;
    }

    printf("[SPECTATOR] Lista actualizada: %d jugadores disponibles\n", playerDropDown.count);
}

/* ======================================================
                 DIBUJAR DROPDOWN
   ====================================================== */

static void drawDropdown(SDL_Renderer* ren) {

    // Fondo del dropdown principal (más brillante para mayor visibilidad)
    SDL_SetRenderDrawColor(ren, 80, 80, 90, 255);
    SDL_RenderFillRect(ren, &playerDropDown.box);

    // Borde más grueso y visible
    SDL_SetRenderDrawColor(ren, 255, 255, 255, 255); // Blanco
    SDL_RenderDrawRect(ren, &playerDropDown.box);

    // Texto del dropdown
    if (playerDropDown.selectedIndex >= 0) {
        char label[64];
        snprintf(label, sizeof(label), "Observando: %s",
                 playerDropDown.names[playerDropDown.selectedIndex]);
        if (ui_font) {
            spectator_draw_text(ren, ui_font, label,
                               playerDropDown.box.x + 10, playerDropDown.box.y + 8);
        }
    } else {
        if (ui_font) {
            // Mostrar mensaje diferente si no hay jugadores
            if (playerDropDown.count == 0) {
                spectator_draw_text(ren, ui_font, "Actualiza primero la lista <-",
                                   playerDropDown.box.x + 10, playerDropDown.box.y + 8);
            } else {
                spectator_draw_text(ren, ui_font, "Seleccionar jugador...",
                                   playerDropDown.box.x + 10, playerDropDown.box.y + 8);
            }
        }
    }

    // Indicador visual de si hay jugadores
    if (playerDropDown.count > 0 && ui_font) {
        char countLabel[32];
        snprintf(countLabel, sizeof(countLabel), "(%d)", playerDropDown.count);
        spectator_draw_text(ren, ui_font, countLabel,
                           playerDropDown.box.x + playerDropDown.box.w - 40,
                           playerDropDown.box.y + 8);
    }

    // Si está abierto, mostrar opciones
    if (playerDropDown.isOpen) {
        printf("[DEBUG] Dibujando dropdown abierto con %d jugadores\n", playerDropDown.count);

        for (int i = 0; i < playerDropDown.count; i++) {
            SDL_Rect opt = {
                playerDropDown.box.x,
                playerDropDown.box.y + (i + 1) * 35,
                playerDropDown.box.w,
                35
            };

            // Fondo de la opción (más claro que el fondo del dropdown principal)
            SDL_SetRenderDrawColor(ren, 100, 100, 110, 255);
            SDL_RenderFillRect(ren, &opt);

            // Borde blanco visible
            SDL_SetRenderDrawColor(ren, 255, 255, 255, 255);
            SDL_RenderDrawRect(ren, &opt);

            // Texto de la opción
            char label[64];
            snprintf(label, sizeof(label), "%s (ID: %d)",
                     playerDropDown.names[i], playerDropDown.ids[i]);

            if (ui_font) {
                spectator_draw_text(ren, ui_font, label, opt.x + 10, opt.y + 8);
            }

            printf("[DEBUG] Opción %d: %s en Y=%d\n", i, label, opt.y);
        }
    } else {
        printf("[DEBUG] Dropdown cerrado (count=%d)\n", playerDropDown.count);
    }
}

/* ======================================================
                 DIBUJAR BOTÓN
   ====================================================== */

typedef struct {
    SDL_Rect rect;
    const char* label;
} Button;

static Button btnRefresh = {{0, 0, 200, 35}, "ACTUALIZAR LISTA"};

static void drawButton(SDL_Renderer* ren, const Button* btn) {
    // Fondo del botón
    SDL_SetRenderDrawColor(ren, 70, 130, 180, 255); // Azul
    SDL_RenderFillRect(ren, &btn->rect);

    // Borde del botón
    SDL_SetRenderDrawColor(ren, 200, 200, 200, 255);
    SDL_RenderDrawRect(ren, &btn->rect);

    // Texto del botón
    spectator_draw_text(ren, ui_font, btn->label,
                       btn->rect.x + 20, btn->rect.y + 8);
}

/* ======================================================
                 RENDER
   ====================================================== */

void spectator_ui_render(SpectatorUI* ui, const GameState* gs) {

    // Limpiar pantalla
    SDL_SetRenderDrawColor(ui->ren, COLOR_BG);
    SDL_RenderClear(ui->ren);

    if (ui->isConnected && gs) {
        // ← CAMBIO CLAVE: Usar gfx_draw_env() con Gfx completo
        // Esto renderizará con TODAS las texturas (sprites PNG)
        gfx_draw_env(&gfx_spectator, gs);
    } else {
        // Mensaje de que no hay jugador seleccionado
        if (ui_font) {
            spectator_draw_text(ui->ren, ui_font,
                               "Seleccione un jugador para observar",
                               WIN_W / 2 - 150, WIN_H / 2);
        }
    }

    // Dibujar barra de información del espectador en la parte inferior
    SDL_Rect info_bar = {0, WIN_H, WIN_W, 120};
    SDL_SetRenderDrawColor(ui->ren, 30, 30, 30, 255);
    SDL_RenderFillRect(ui->ren, &info_bar);

    // Título
    if (ui_font) {
        spectator_draw_text(ui->ren, ui_font, "MODO ESPECTADOR", 20, WIN_H + 10);
    }

    // Botón de actualizar (a la IZQUIERDA)
    btnRefresh.rect.x = WIN_W / 2 - 350;
    btnRefresh.rect.y = WIN_H + 75;
    drawButton(ui->ren, &btnRefresh);

    // Dropdown de jugadores (a la DERECHA - completamente separado)
    drawDropdown(ui->ren);

    SDL_RenderPresent(ui->ren);
}

/* ======================================================
                 MANEJO DE CLICKS
   ====================================================== */

void spectator_ui_handle_click(SpectatorUI* ui, int x, int y, int sock) {

    printf("[DEBUG] Click en X=%d, Y=%d\n", x, y);
    printf("[DEBUG] Botón rect: X=%d, Y=%d, W=%d, H=%d\n",
           btnRefresh.rect.x, btnRefresh.rect.y, btnRefresh.rect.w, btnRefresh.rect.h);
    printf("[DEBUG] Dropdown rect: X=%d, Y=%d, W=%d, H=%d\n",
           playerDropDown.box.x, playerDropDown.box.y, playerDropDown.box.w, playerDropDown.box.h);

    // Click en el botón ACTUALIZAR LISTA
    if (x >= btnRefresh.rect.x && x <= btnRefresh.rect.x + btnRefresh.rect.w &&
        y >= btnRefresh.rect.y && y <= btnRefresh.rect.y + btnRefresh.rect.h)
    {
        printf("[SPECTATOR] Actualizando lista de jugadores...\n");
        net_send_line(sock, "ADMIN PLAYERS");
        return;
    }

    // Click en el dropdown
    if (x >= playerDropDown.box.x && x <= playerDropDown.box.x + playerDropDown.box.w &&
        y >= playerDropDown.box.y && y <= playerDropDown.box.y + playerDropDown.box.h)
    {
        if (playerDropDown.count == 0) {
            printf("[SPECTATOR] ⚠️  No hay jugadores disponibles!\n");
            printf("[SPECTATOR] 👆 Haz click en 'ACTUALIZAR LISTA' primero (botón izquierdo)\n");
            printf("[SPECTATOR] 📍 Botón está en X=%d-%d, Y=%d-%d\n",
                   btnRefresh.rect.x, btnRefresh.rect.x + btnRefresh.rect.w,
                   btnRefresh.rect.y, btnRefresh.rect.y + btnRefresh.rect.h);
            return;
        }

        playerDropDown.isOpen = !playerDropDown.isOpen;
        printf("[SPECTATOR] Dropdown %s (count=%d)\n",
               playerDropDown.isOpen ? "abierto" : "cerrado",
               playerDropDown.count);
        return;
    }

    // Click en una opción del dropdown
    if (playerDropDown.isOpen) {
        printf("[DEBUG] Buscando click en opciones del dropdown...\n");
        for (int i = 0; i < playerDropDown.count; i++) {
            SDL_Rect opt = {
                playerDropDown.box.x,
                playerDropDown.box.y + (i + 1) * 35,
                playerDropDown.box.w,
                35
            };

            printf("[DEBUG] Opción %d: X=%d-%d, Y=%d-%d\n",
                   i, opt.x, opt.x + opt.w, opt.y, opt.y + opt.h);

            if (x >= opt.x && x <= opt.x + opt.w &&
                y >= opt.y && y <= opt.y + opt.h)
            {
                playerDropDown.selectedIndex = i;
                playerDropDown.isOpen = 0;

                int playerId = playerDropDown.ids[i];
                ui->selectedPlayerId = playerId;
                ui->isConnected = 1;

                // Enviar comando SPECTATE al servidor
                char cmd[128];
                snprintf(cmd, sizeof(cmd), "SPECTATE %d", playerId);
                net_send_line(sock, cmd);

                printf("[SPECTATOR] Comando enviado: %s\n", cmd);
                printf("[SPECTATOR] Observando a jugador %d (%s)\n",
                       playerId, playerDropDown.names[i]);
                return;
            }
        }
        printf("[DEBUG] Click no coincidió con ninguna opción\n");
    }
}