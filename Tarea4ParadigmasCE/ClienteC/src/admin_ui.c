#define SDL_MAIN_HANDLED
#include "admin_ui.h"
#include "net.h"
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

/* ======================================================
                 VARIABLES GLOBALES
   ====================================================== */

static AdminUIState g_state = {0};
static int g_sock = -1;

// Radio buttons para tipo de cocodrilo
static RadioButton g_crocRadios[2] = {
    {{0}, "Rojo", 1},   // Seleccionado por defecto
    {{0}, "Azul", 0}
};

// Radio buttons para tipo de fruta
static RadioButton g_fruitRadios[3] = {
    {{0}, "Banana", 1},  // Seleccionado por defecto
    {{0}, "Naranja", 0},
    {{0}, "Cereza", 0}
};

/* ======================================================
                 FUNCIONES AUXILIARES DE DIBUJO
   ====================================================== */

void draw_text(SDL_Renderer* ren, TTF_Font* font, const char* text, int x, int y, SDL_Color color) {
    if (!font || !text) return;

    SDL_Surface* surf = TTF_RenderText_Blended(font, text, color);
    if (!surf) return;

    SDL_Texture* tex = SDL_CreateTextureFromSurface(ren, surf);
    if (!tex) {
        SDL_FreeSurface(surf);
        return;
    }

    SDL_Rect rect = {x, y, surf->w, surf->h};
    SDL_RenderCopy(ren, tex, NULL, &rect);

    SDL_FreeSurface(surf);
    SDL_DestroyTexture(tex);
}

void draw_section_title(SDL_Renderer* ren, TTF_Font* font, const char* title, int x, int y) {
    SDL_Color color = {255, 200, 0, 255};  // Amarillo/naranja
    draw_text(ren, font, title, x, y, color);
}

void draw_button(SDL_Renderer* ren, TTF_Font* font, const Button* btn) {
    // Fondo del botón
    SDL_SetRenderDrawColor(ren, 70, 130, 180, 255);
    SDL_RenderFillRect(ren, &btn->rect);

    // Borde
    SDL_SetRenderDrawColor(ren, 200, 200, 200, 255);
    SDL_RenderDrawRect(ren, &btn->rect);

    // Texto centrado
    SDL_Color textColor = {255, 255, 255, 255};
    draw_text(ren, font, btn->label,
              btn->rect.x + 10,
              btn->rect.y + (btn->rect.h - 20) / 2,
              textColor);
}

void draw_input_field(SDL_Renderer* ren, TTF_Font* font, const InputField* field) {
    // Label
    SDL_Color labelColor = {200, 200, 200, 255};
    draw_text(ren, font, field->label, field->rect.x, field->rect.y - 20, labelColor);

    // Fondo del input
    if (field->isActive) {
        SDL_SetRenderDrawColor(ren, 80, 80, 120, 255);  // Azul oscuro si está activo
    } else {
        SDL_SetRenderDrawColor(ren, 50, 50, 50, 255);   // Gris oscuro
    }
    SDL_RenderFillRect(ren, &field->rect);

    // Borde
    if (field->isActive) {
        SDL_SetRenderDrawColor(ren, 100, 150, 255, 255);  // Azul brillante
    } else {
        SDL_SetRenderDrawColor(ren, 150, 150, 150, 255);
    }
    SDL_RenderDrawRect(ren, &field->rect);

    // Texto del input
    if (strlen(field->text) > 0) {
        SDL_Color textColor = {255, 255, 255, 255};
        draw_text(ren, font, field->text,
                  field->rect.x + 5,
                  field->rect.y + 5,
                  textColor);
    }

    // Cursor parpadeante si está activo
    if (field->isActive) {
        static int cursorBlink = 0;
        cursorBlink = (cursorBlink + 1) % 60;
        if (cursorBlink < 30) {
            int textWidth = (int)strlen(field->text) * 10;  // Aproximado
            SDL_SetRenderDrawColor(ren, 255, 255, 255, 255);
            SDL_Rect cursor = {field->rect.x + 5 + textWidth, field->rect.y + 5, 2, 20};
            SDL_RenderFillRect(ren, &cursor);
        }
    }
}

void draw_radio_button(SDL_Renderer* ren, TTF_Font* font, const RadioButton* radio) {
    // Círculo exterior
    SDL_SetRenderDrawColor(ren, 200, 200, 200, 255);
    SDL_Rect outer = {radio->rect.x, radio->rect.y, 16, 16};
    SDL_RenderDrawRect(ren, &outer);

    // Círculo interior si está seleccionado
    if (radio->isSelected) {
        SDL_SetRenderDrawColor(ren, 100, 200, 100, 255);  // Verde
        SDL_Rect inner = {radio->rect.x + 4, radio->rect.y + 4, 8, 8};
        SDL_RenderFillRect(ren, &inner);
    }

    // Label
    SDL_Color textColor = {255, 255, 255, 255};
    draw_text(ren, font, radio->label, radio->rect.x + 25, radio->rect.y, textColor);
}

/* ======================================================
                 INICIALIZACIÓN DE UI
   ====================================================== */

void admin_ui_init_state(AdminUIState* state) {
    // Inicializar dropdown de jugadores
    state->playerDropdown.box = (SDL_Rect){20, 50, 360, 35};
    state->playerDropdown.isOpen = 0;
    state->playerDropdown.selectedIndex = -1;
    state->playerDropdown.count = 0;

    int yOffset = 120;  // Inicio del contenido

    // ===== SECCIÓN COCODRILOS =====
    // Radio buttons para tipo
    state->crocTypeGroup.buttons = g_crocRadios;
    state->crocTypeGroup.count = 2;
    state->crocTypeGroup.selectedIndex = 0;

    g_crocRadios[0].rect = (SDL_Rect){20, yOffset + 30, 16, 16};
    g_crocRadios[1].rect = (SDL_Rect){120, yOffset + 30, 16, 16};

    // Input fields
    state->crocLiana = (InputField){
        {20, yOffset + 70, 80, 30}, "", 2, 0, "Liana (1-6):"
    };
    state->crocAltura = (InputField){
        {120, yOffset + 70, 100, 30}, "", 4, 0, "Altura (0-540):"
    };

    // Botón crear cocodrilo
    state->btnCrearCroc = (Button){
        {20, yOffset + 120, 200, 35}, "CREAR COCODRILO", 1
    };

    yOffset += 180;

    // ===== SECCIÓN FRUTAS =====
    // Radio buttons para tipo
    state->fruitTypeGroup.buttons = g_fruitRadios;
    state->fruitTypeGroup.count = 3;
    state->fruitTypeGroup.selectedIndex = 0;

    g_fruitRadios[0].rect = (SDL_Rect){20, yOffset + 30, 16, 16};
    g_fruitRadios[1].rect = (SDL_Rect){120, yOffset + 30, 16, 16};
    g_fruitRadios[2].rect = (SDL_Rect){230, yOffset + 30, 16, 16};

    // Input fields
    state->fruitLiana = (InputField){
        {20, yOffset + 70, 80, 30}, "", 2, 0, "Liana (1-6):"
    };
    state->fruitAltura = (InputField){
        {120, yOffset + 70, 100, 30}, "", 4, 0, "Altura (0-540):"
    };
    state->fruitPuntos = (InputField){
        {240, yOffset + 70, 100, 30}, "", 4, 0, "Puntos (10-100):"
    };

    // Botón crear fruta
    state->btnCrearFruta = (Button){
        {20, yOffset + 120, 200, 35}, "CREAR FRUTA", 2
    };

    yOffset += 180;

    // ===== SECCIÓN ELIMINAR FRUTA =====
    state->delFruitLiana = (InputField){
        {20, yOffset + 30, 80, 30}, "", 2, 0, "Liana (1-6):"
    };
    state->delFruitAltura = (InputField){
        {120, yOffset + 30, 100, 30}, "", 4, 0, "Altura (0-540):"
    };

    // Botón eliminar fruta
    state->btnEliminarFruta = (Button){
        {20, yOffset + 80, 200, 35}, "ELIMINAR FRUTA", 3
    };

    // Botón actualizar lista (arriba)
    state->btnActualizarLista = (Button){
        {20, 100, 200, 35}, "ACTUALIZAR LISTA", 4
    };

    state->activeInput = NULL;
}

int admin_ui_init(AdminUI* ui) {
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        printf("Error SDL_Init: %s\n", SDL_GetError());
        return -1;
    }

    if (TTF_Init() < 0) {
        printf("Error TTF_Init: %s\n", TTF_GetError());
        return -1;
    }

    ui->win = SDL_CreateWindow("Admin Panel - DKJr",
                               SDL_WINDOWPOS_CENTERED,
                               SDL_WINDOWPOS_CENTERED,
                               400, 750,  // Más alto para todos los campos
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

    ui->font = TTF_OpenFont("assets/arial.ttf", 16);
    if (!ui->font) {
        printf("Advertencia: No se pudo cargar fuente\n");
    }

    admin_ui_init_state(&g_state);

    return 0;
}

void admin_ui_shutdown(AdminUI* ui) {
    if (ui->font) TTF_CloseFont(ui->font);
    if (ui->ren) SDL_DestroyRenderer(ui->ren);
    if (ui->win) SDL_DestroyWindow(ui->win);
    TTF_Quit();
    SDL_Quit();
}

/* ======================================================
                 ACTUALIZAR LISTA DE JUGADORES
   ====================================================== */

void admin_ui_update_players(const char* json) {
    printf("[ADMIN] JSON recibido: %s\n", json);

    g_state.playerDropdown.count = 0;
    const char* p = json;

    while ((p = strstr(p, "\"id\"")) != NULL) {
        int id;
        char name[32];

        if (sscanf(p, "\"id\":%d,\"name\":\"%31[^\"]\"", &id, name) == 2) {
            int i = g_state.playerDropdown.count++;
            g_state.playerDropdown.ids[i] = id;
            strncpy(g_state.playerDropdown.names[i], name, 31);
            g_state.playerDropdown.names[i][31] = '\0';

            printf("[ADMIN] Jugador encontrado: ID=%d, Name=%s\n", id, name);
        }
        p++;
    }

    printf("[ADMIN] Lista actualizada: %d jugadores\n", g_state.playerDropdown.count);
}

/* ======================================================
                 RENDERIZADO
   ====================================================== */

static void draw_dropdown(SDL_Renderer* ren, TTF_Font* font) {
    DropDown* dd = &g_state.playerDropdown;

    // Fondo
    SDL_SetRenderDrawColor(ren, 60, 60, 60, 255);
    SDL_RenderFillRect(ren, &dd->box);
    SDL_SetRenderDrawColor(ren, 200, 200, 200, 255);
    SDL_RenderDrawRect(ren, &dd->box);

    // Texto
    if (dd->selectedIndex >= 0) {
        char label[64];
        snprintf(label, sizeof(label), "Jugador: %s", dd->names[dd->selectedIndex]);
        SDL_Color color = {255, 255, 255, 255};
        draw_text(ren, font, label, dd->box.x + 10, dd->box.y + 8, color);
    } else {
        SDL_Color color = {150, 150, 150, 255};
        draw_text(ren, font, "Seleccionar jugador...", dd->box.x + 10, dd->box.y + 8, color);
    }

    // Opciones si está abierto
    if (dd->isOpen) {
        for (int i = 0; i < dd->count; i++) {
            SDL_Rect opt = {
                dd->box.x,
                dd->box.y + (i + 1) * 35,
                dd->box.w,
                35
            };

            SDL_SetRenderDrawColor(ren, 40, 40, 40, 255);
            SDL_RenderFillRect(ren, &opt);
            SDL_SetRenderDrawColor(ren, 200, 200, 200, 255);
            SDL_RenderDrawRect(ren, &opt);

            char label[64];
            snprintf(label, sizeof(label), "%s (ID: %d)", dd->names[i], dd->ids[i]);
            SDL_Color color = {255, 255, 255, 255};
            draw_text(ren, font, label, opt.x + 10, opt.y + 8, color);
        }
    }
}

void admin_ui_render(AdminUI* ui, AdminUIState* state) {
    SDL_Renderer* ren = ui->ren;
    TTF_Font* font = ui->font;

    // Fondo
    SDL_SetRenderDrawColor(ren, 30, 30, 30, 255);
    SDL_RenderClear(ren);

    // Título
    SDL_Color titleColor = {255, 255, 255, 255};
    draw_text(ren, font, "ADMINISTRADOR - DonCEy Kong Jr", 20, 10, titleColor);

    // Dropdown de jugadores
    draw_dropdown(ren, font);

    // Botón actualizar lista
    draw_button(ren, font, &state->btnActualizarLista);

    int yOffset = 150;

    // ===== SECCIÓN COCODRILOS =====
    draw_section_title(ren, font, "CREAR COCODRILO", 20, yOffset);

    // Radio buttons
    SDL_Color labelColor = {200, 200, 200, 255};
    draw_text(ren, font, "Tipo:", 20, yOffset + 10, labelColor);
    for (int i = 0; i < state->crocTypeGroup.count; i++) {
        draw_radio_button(ren, font, &state->crocTypeGroup.buttons[i]);
    }

    // Input fields
    draw_input_field(ren, font, &state->crocLiana);
    draw_input_field(ren, font, &state->crocAltura);

    // Botón
    draw_button(ren, font, &state->btnCrearCroc);

    yOffset += 180;

    // ===== SECCIÓN FRUTAS =====
    draw_section_title(ren, font, "CREAR FRUTA", 20, yOffset);

    // Radio buttons
    draw_text(ren, font, "Tipo:", 20, yOffset + 10, labelColor);
    for (int i = 0; i < state->fruitTypeGroup.count; i++) {
        draw_radio_button(ren, font, &state->fruitTypeGroup.buttons[i]);
    }

    // Input fields
    draw_input_field(ren, font, &state->fruitLiana);
    draw_input_field(ren, font, &state->fruitAltura);
    draw_input_field(ren, font, &state->fruitPuntos);

    // Botón
    draw_button(ren, font, &state->btnCrearFruta);

    yOffset += 180;

    // ===== SECCIÓN ELIMINAR FRUTA =====
    draw_section_title(ren, font, "ELIMINAR FRUTA", 20, yOffset);

    // Input fields
    draw_input_field(ren, font, &state->delFruitLiana);
    draw_input_field(ren, font, &state->delFruitAltura);

    // Botón
    draw_button(ren, font, &state->btnEliminarFruta);

    SDL_RenderPresent(ren);
}

/* ======================================================
                 MANEJO DE EVENTOS - SIGUIENTE PARTE
   ====================================================== */
/* ======================================================
   CONTINUACIÓN DE admin_ui.c - PARTE 2
   MANEJO DE EVENTOS Y COMANDOS
   ====================================================== */

/* ======================================================
                 MANEJO DE CLICKS
   ====================================================== */

void admin_ui_handle_click(AdminUIState* state, int x, int y, int sock) {

    // Click en botón ACTUALIZAR LISTA
    if (x >= state->btnActualizarLista.rect.x &&
        x <= state->btnActualizarLista.rect.x + state->btnActualizarLista.rect.w &&
        y >= state->btnActualizarLista.rect.y &&
        y <= state->btnActualizarLista.rect.y + state->btnActualizarLista.rect.h)
    {
        printf("[ADMIN] Actualizando lista de jugadores...\n");
        net_send_line(sock, "ADMIN PLAYERS");
        return;
    }

    // Click en dropdown de jugadores
    if (x >= state->playerDropdown.box.x &&
        x <= state->playerDropdown.box.x + state->playerDropdown.box.w &&
        y >= state->playerDropdown.box.y &&
        y <= state->playerDropdown.box.y + state->playerDropdown.box.h)
    {
        state->playerDropdown.isOpen = !state->playerDropdown.isOpen;
        return;
    }

    // Click en opciones del dropdown
    if (state->playerDropdown.isOpen) {
        for (int i = 0; i < state->playerDropdown.count; i++) {
            SDL_Rect opt = {
                state->playerDropdown.box.x,
                state->playerDropdown.box.y + (i + 1) * 35,
                state->playerDropdown.box.w,
                35
            };

            if (x >= opt.x && x <= opt.x + opt.w &&
                y >= opt.y && y <= opt.y + opt.h)
            {
                state->playerDropdown.selectedIndex = i;
                state->playerDropdown.isOpen = 0;

                int playerId = state->playerDropdown.ids[i];

                // Enviar comando SELECT al servidor
                char cmd[128];
                snprintf(cmd, sizeof(cmd), "ADMIN SELECT %d", playerId);
                net_send_line(sock, cmd);

                printf("[ADMIN] Jugador seleccionado: %s (ID: %d)\n",
                       state->playerDropdown.names[i], playerId);
                return;
            }
        }
    }

    // Click en radio buttons de COCODRILO
    for (int i = 0; i < state->crocTypeGroup.count; i++) {
        RadioButton* radio = &state->crocTypeGroup.buttons[i];
        if (x >= radio->rect.x && x <= radio->rect.x + 80 &&
            y >= radio->rect.y && y <= radio->rect.y + 16)
        {
            // Deseleccionar todos
            for (int j = 0; j < state->crocTypeGroup.count; j++) {
                state->crocTypeGroup.buttons[j].isSelected = 0;
            }
            // Seleccionar este
            radio->isSelected = 1;
            state->crocTypeGroup.selectedIndex = i;
            return;
        }
    }

    // Click en radio buttons de FRUTA
    for (int i = 0; i < state->fruitTypeGroup.count; i++) {
        RadioButton* radio = &state->fruitTypeGroup.buttons[i];
        if (x >= radio->rect.x && x <= radio->rect.x + 100 &&
            y >= radio->rect.y && y <= radio->rect.y + 16)
        {
            // Deseleccionar todos
            for (int j = 0; j < state->fruitTypeGroup.count; j++) {
                state->fruitTypeGroup.buttons[j].isSelected = 0;
            }
            // Seleccionar este
            radio->isSelected = 1;
            state->fruitTypeGroup.selectedIndex = i;
            return;
        }
    }

    // Click en input fields - COCODRILOS
    if (x >= state->crocLiana.rect.x && x <= state->crocLiana.rect.x + state->crocLiana.rect.w &&
        y >= state->crocLiana.rect.y && y <= state->crocLiana.rect.y + state->crocLiana.rect.h)
    {
        // Desactivar todos los demás
        state->crocLiana.isActive = 1;
        state->crocAltura.isActive = 0;
        state->fruitLiana.isActive = 0;
        state->fruitAltura.isActive = 0;
        state->fruitPuntos.isActive = 0;
        state->delFruitLiana.isActive = 0;
        state->delFruitAltura.isActive = 0;
        state->activeInput = &state->crocLiana;
        return;
    }

    if (x >= state->crocAltura.rect.x && x <= state->crocAltura.rect.x + state->crocAltura.rect.w &&
        y >= state->crocAltura.rect.y && y <= state->crocAltura.rect.y + state->crocAltura.rect.h)
    {
        state->crocLiana.isActive = 0;
        state->crocAltura.isActive = 1;
        state->fruitLiana.isActive = 0;
        state->fruitAltura.isActive = 0;
        state->fruitPuntos.isActive = 0;
        state->delFruitLiana.isActive = 0;
        state->delFruitAltura.isActive = 0;
        state->activeInput = &state->crocAltura;
        return;
    }

    // Click en input fields - FRUTAS
    if (x >= state->fruitLiana.rect.x && x <= state->fruitLiana.rect.x + state->fruitLiana.rect.w &&
        y >= state->fruitLiana.rect.y && y <= state->fruitLiana.rect.y + state->fruitLiana.rect.h)
    {
        state->crocLiana.isActive = 0;
        state->crocAltura.isActive = 0;
        state->fruitLiana.isActive = 1;
        state->fruitAltura.isActive = 0;
        state->fruitPuntos.isActive = 0;
        state->delFruitLiana.isActive = 0;
        state->delFruitAltura.isActive = 0;
        state->activeInput = &state->fruitLiana;
        return;
    }

    if (x >= state->fruitAltura.rect.x && x <= state->fruitAltura.rect.x + state->fruitAltura.rect.w &&
        y >= state->fruitAltura.rect.y && y <= state->fruitAltura.rect.y + state->fruitAltura.rect.h)
    {
        state->crocLiana.isActive = 0;
        state->crocAltura.isActive = 0;
        state->fruitLiana.isActive = 0;
        state->fruitAltura.isActive = 1;
        state->fruitPuntos.isActive = 0;
        state->delFruitLiana.isActive = 0;
        state->delFruitAltura.isActive = 0;
        state->activeInput = &state->fruitAltura;
        return;
    }

    if (x >= state->fruitPuntos.rect.x && x <= state->fruitPuntos.rect.x + state->fruitPuntos.rect.w &&
        y >= state->fruitPuntos.rect.y && y <= state->fruitPuntos.rect.y + state->fruitPuntos.rect.h)
    {
        state->crocLiana.isActive = 0;
        state->crocAltura.isActive = 0;
        state->fruitLiana.isActive = 0;
        state->fruitAltura.isActive = 0;
        state->fruitPuntos.isActive = 1;
        state->delFruitLiana.isActive = 0;
        state->delFruitAltura.isActive = 0;
        state->activeInput = &state->fruitPuntos;
        return;
    }

    // Click en input fields - ELIMINAR FRUTA
    if (x >= state->delFruitLiana.rect.x && x <= state->delFruitLiana.rect.x + state->delFruitLiana.rect.w &&
        y >= state->delFruitLiana.rect.y && y <= state->delFruitLiana.rect.y + state->delFruitLiana.rect.h)
    {
        state->crocLiana.isActive = 0;
        state->crocAltura.isActive = 0;
        state->fruitLiana.isActive = 0;
        state->fruitAltura.isActive = 0;
        state->fruitPuntos.isActive = 0;
        state->delFruitLiana.isActive = 1;
        state->delFruitAltura.isActive = 0;
        state->activeInput = &state->delFruitLiana;
        return;
    }

    if (x >= state->delFruitAltura.rect.x && x <= state->delFruitAltura.rect.x + state->delFruitAltura.rect.w &&
        y >= state->delFruitAltura.rect.y && y <= state->delFruitAltura.rect.y + state->delFruitAltura.rect.h)
    {
        state->crocLiana.isActive = 0;
        state->crocAltura.isActive = 0;
        state->fruitLiana.isActive = 0;
        state->fruitAltura.isActive = 0;
        state->fruitPuntos.isActive = 0;
        state->delFruitLiana.isActive = 0;
        state->delFruitAltura.isActive = 1;
        state->activeInput = &state->delFruitAltura;
        return;
    }

    // Click en botón CREAR COCODRILO
    if (x >= state->btnCrearCroc.rect.x &&
        x <= state->btnCrearCroc.rect.x + state->btnCrearCroc.rect.w &&
        y >= state->btnCrearCroc.rect.y &&
        y <= state->btnCrearCroc.rect.y + state->btnCrearCroc.rect.h)
    {
        // Validar jugador seleccionado
        if (state->playerDropdown.selectedIndex < 0) {
            printf("[ADMIN] Error: Debe seleccionar un jugador primero\n");
            return;
        }

        // Validar campos
        if (strlen(state->crocLiana.text) == 0 || strlen(state->crocAltura.text) == 0) {
            printf("[ADMIN] Error: Complete todos los campos\n");
            return;
        }

        int liana = atoi(state->crocLiana.text);
        int altura = atoi(state->crocAltura.text);

        if (liana < 1 || liana > 6) {
            printf("[ADMIN] Error: Liana debe estar entre 1-6\n");
            return;
        }

        if (altura < 0 || altura > 540) {
            printf("[ADMIN] Error: Altura debe estar entre 0-540\n");
            return;
        }

        // Determinar tipo
        const char* tipo = state->crocTypeGroup.buttons[state->crocTypeGroup.selectedIndex].label;

        // Enviar comando
        char cmd[256];
        snprintf(cmd, sizeof(cmd), "ADMIN CROC %s %d %d", tipo, liana, altura);
        net_send_line(sock, cmd);

        printf("[ADMIN] Comando enviado: %s\n", cmd);

        // Limpiar campos
        state->crocLiana.text[0] = '\0';
        state->crocAltura.text[0] = '\0';

        return;
    }

    // Click en botón CREAR FRUTA
    if (x >= state->btnCrearFruta.rect.x &&
        x <= state->btnCrearFruta.rect.x + state->btnCrearFruta.rect.w &&
        y >= state->btnCrearFruta.rect.y &&
        y <= state->btnCrearFruta.rect.y + state->btnCrearFruta.rect.h)
    {
        // Validar jugador seleccionado
        if (state->playerDropdown.selectedIndex < 0) {
            printf("[ADMIN] Error: Debe seleccionar un jugador primero\n");
            return;
        }

        // Validar campos
        if (strlen(state->fruitLiana.text) == 0 ||
            strlen(state->fruitAltura.text) == 0 ||
            strlen(state->fruitPuntos.text) == 0) {
            printf("[ADMIN] Error: Complete todos los campos\n");
            return;
        }

        int liana = atoi(state->fruitLiana.text);
        int altura = atoi(state->fruitAltura.text);
        int puntos = atoi(state->fruitPuntos.text);

        if (liana < 1 || liana > 6) {
            printf("[ADMIN] Error: Liana debe estar entre 1-6\n");
            return;
        }

        if (altura < 0 || altura > 540) {
            printf("[ADMIN] Error: Altura debe estar entre 0-540\n");
            return;
        }

        if (puntos < 10 || puntos > 100) {
            printf("[ADMIN] Error: Puntos deben estar entre 10-100\n");
            return;
        }

        // Determinar tipo
        const char* tipo = state->fruitTypeGroup.buttons[state->fruitTypeGroup.selectedIndex].label;

        // Convertir tipo a mayúsculas
        char tipoUpper[32];
        snprintf(tipoUpper, sizeof(tipoUpper), "%s", tipo);
        for (int i = 0; tipoUpper[i]; i++) {
            if (tipoUpper[i] >= 'a' && tipoUpper[i] <= 'z') {
                tipoUpper[i] = tipoUpper[i] - 32;
            }
        }

        // Enviar comando
        char cmd[256];
        snprintf(cmd, sizeof(cmd), "ADMIN FRUIT %s %d %d %d", tipoUpper, liana, altura, puntos);
        net_send_line(sock, cmd);

        printf("[ADMIN] Comando enviado: %s\n", cmd);

        // Limpiar campos
        state->fruitLiana.text[0] = '\0';
        state->fruitAltura.text[0] = '\0';
        state->fruitPuntos.text[0] = '\0';

        return;
    }

    // Click en botón ELIMINAR FRUTA
    if (x >= state->btnEliminarFruta.rect.x &&
        x <= state->btnEliminarFruta.rect.x + state->btnEliminarFruta.rect.w &&
        y >= state->btnEliminarFruta.rect.y &&
        y <= state->btnEliminarFruta.rect.y + state->btnEliminarFruta.rect.h)
    {
        // Validar jugador seleccionado
        if (state->playerDropdown.selectedIndex < 0) {
            printf("[ADMIN] Error: Debe seleccionar un jugador primero\n");
            return;
        }

        // Validar campos
        if (strlen(state->delFruitLiana.text) == 0 || strlen(state->delFruitAltura.text) == 0) {
            printf("[ADMIN] Error: Complete todos los campos\n");
            return;
        }

        int liana = atoi(state->delFruitLiana.text);
        int altura = atoi(state->delFruitAltura.text);

        if (liana < 1 || liana > 6) {
            printf("[ADMIN] Error: Liana debe estar entre 1-6\n");
            return;
        }

        if (altura < 0 || altura > 540) {
            printf("[ADMIN] Error: Altura debe estar entre 0-540\n");
            return;
        }

        // Enviar comando
        char cmd[256];
        snprintf(cmd, sizeof(cmd), "ADMIN DELFRUIT %d %d", liana, altura);
        net_send_line(sock, cmd);

        printf("[ADMIN] Comando enviado: %s\n", cmd);

        // Limpiar campos
        state->delFruitLiana.text[0] = '\0';
        state->delFruitAltura.text[0] = '\0';

        return;
    }
}

/* ======================================================
                 MANEJO DE TECLADO
   ====================================================== */

void admin_ui_handle_keypress(AdminUIState* state, SDL_Event* e) {
    if (!state->activeInput) return;

    if (e->type == SDL_KEYDOWN) {
        if (e->key.keysym.sym == SDLK_BACKSPACE) {
            int len = strlen(state->activeInput->text);
            if (len > 0) {
                state->activeInput->text[len - 1] = '\0';
            }
        }
        else if (e->key.keysym.sym == SDLK_RETURN || e->key.keysym.sym == SDLK_ESCAPE) {
            // Desactivar input actual
            state->activeInput->isActive = 0;
            state->activeInput = NULL;
        }
    }
    else if (e->type == SDL_TEXTINPUT) {
        int len = strlen(state->activeInput->text);
        if (len < state->activeInput->maxLen) {
            // Solo permitir números
            if (e->text.text[0] >= '0' && e->text.text[0] <= '9') {
                strncat(state->activeInput->text, e->text.text, 1);
            }
        }
    }
}

/* ======================================================
                 LOOP PRINCIPAL
   ====================================================== */

void admin_ui_run(int sock) {
    AdminUI ui;
    if (admin_ui_init(&ui) < 0) {
        return;
    }

    g_sock = sock;

    // Iniciar hilo receptor
    net_start_receiver(sock, NULL);  // NULL porque no es cliente de juego

    // Pedir lista inicial
    net_send_line(sock, "ADMIN PLAYERS");

    SDL_Event e;
    int running = 1;

    while (running) {
        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_QUIT) {
                running = 0;
            }
            else if (e.type == SDL_MOUSEBUTTONDOWN) {
                admin_ui_handle_click(&g_state, e.button.x, e.button.y, sock);
            }
            else if (e.type == SDL_KEYDOWN || e.type == SDL_TEXTINPUT) {
                admin_ui_handle_keypress(&g_state, &e);
            }
        }

        admin_ui_render(&ui, &g_state);
        SDL_Delay(16);  // ~60 FPS
    }

    admin_ui_shutdown(&ui);
}