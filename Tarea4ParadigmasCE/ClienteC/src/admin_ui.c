#define SDL_MAIN_HANDLED
#include "admin_ui.h"
#include "net.h"
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>

/* ======================================================
                 VARIABLES GLOBALES
   ====================================================== */

static TTF_Font* ui_font = NULL;
static int sock_global = -1;  // Socket global para poder enviar comandos

typedef struct {
    int x, y, w, h;
    const char* label;
    const char* cmdTemplate;
    int needsPlayer;  // 1 = necesita jugador seleccionado, 0 = no necesita
} Button;

static Button buttons[32];
static int btnCount = 0;

/* Dropdown global */
static DropDown playerDropDown;

/* ======================================================
                 FUNCIONES DE TEXTO
   ====================================================== */

void admin_ui_draw_text(SDL_Renderer* ren, const char* txt, int x, int y) {
    SDL_Color color = {0, 0, 0, 255};
    SDL_Surface* surf = TTF_RenderText_Blended(ui_font, txt, color);
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
                 BOTONES
   ====================================================== */

static void addButton(const char* label, const char* templ, int x, int y, int needsPlayer) {
    buttons[btnCount++] = (Button){x, y, 160, 35, label, templ, needsPlayer};
}

/* ======================================================
                 INICIALIZACIÓN
   ====================================================== */

int admin_ui_init(AdminUI* ui) {

    ui->win = SDL_CreateWindow("Admin Panel – DKJr",
                               SDL_WINDOWPOS_CENTERED,
                               SDL_WINDOWPOS_CENTERED,
                               420, 600,
                               SDL_WINDOW_SHOWN);

    if (!ui->win) return -1;

    ui->ren = SDL_CreateRenderer(ui->win, -1, SDL_RENDERER_ACCELERATED);
    if (!ui->ren) return -1;

    /* Fuente */
    if (TTF_Init() < 0) {
        printf("Error TTF_Init: %s\n", TTF_GetError());
        return -1;
    }

    ui_font = TTF_OpenFont("assets/arial.ttf", 18);
    if (!ui_font) {
        printf("Error cargando fuente TTF\n");
        return -1;
    }

    /* Botones - NOTA: último parámetro indica si necesita jugador seleccionado */
    int y = 200;
    addButton("ACTUALIZAR LISTA", "PLAYERS", 30, y, 0);  // ← NO necesita jugador
    y += 45;

    y += 20;  // Espacio extra
    addButton("CROC ROJO" ,  "CROC ROJO 2 300", 30, y, 1); y += 45;  // 1 = necesita jugador
    addButton("CROC AZUL" ,  "CROC AZUL 3 250", 30, y, 1); y += 45;

    y += 10;
    addButton("BANANA" , "FRUTA BANANA 4 200", 30, y, 1); y += 45;
    addButton("NARANJA", "FRUTA NARANJA 5 150", 30, y, 1); y += 45;

    /* Dropdown de jugadores */
    playerDropDown.box = (SDL_Rect){ 220, 80, 170, 30 };
    playerDropDown.isOpen = 0;
    playerDropDown.selectedIndex = -1;
    playerDropDown.count = 0;

    return 0;
}

/* ======================================================
                 ACTUALIZAR LISTA DE JUGADORES (JSON)
   ====================================================== */

void admin_ui_update_players(const char* json) {

    playerDropDown.count = 0;

    const char* p = json;

    while ((p = strstr(p, "\"id\"")) != NULL) {
        int id;
        char name[32];

        if (sscanf(p, "\"id\":%d,\"name\":\"%31[^\"]\"", &id, name) == 2) {
            int i = playerDropDown.count++;
            playerDropDown.ids[i] = id;
            strncpy(playerDropDown.names[i], name, 31);
        }
        p++;
    }

    printf("[ADMIN] Lista de jugadores actualizada (%d jugadores)\n", playerDropDown.count);
}

/* ======================================================
                 RENDER
   ====================================================== */

static void drawDropdown(SDL_Renderer* ren) {

    SDL_SetRenderDrawColor(ren, 200, 200, 200, 255);
    SDL_RenderFillRect(ren, &playerDropDown.box);

    if (playerDropDown.selectedIndex >= 0)
        admin_ui_draw_text(ren, playerDropDown.names[playerDropDown.selectedIndex],
                           playerDropDown.box.x + 5, playerDropDown.box.y + 5);
    else
        admin_ui_draw_text(ren, "Seleccionar jugador",
                           playerDropDown.box.x + 5, playerDropDown.box.y + 5);

    if (playerDropDown.isOpen) {
        for (int i = 0; i < playerDropDown.count; i++) {
            SDL_Rect opt = { playerDropDown.box.x,
                             playerDropDown.box.y + 30 * (i+1),
                             playerDropDown.box.w,
                             30 };

            SDL_SetRenderDrawColor(ren, 180, 180, 180, 255);
            SDL_RenderFillRect(ren, &opt);

            admin_ui_draw_text(ren, playerDropDown.names[i], opt.x + 5, opt.y + 5);
        }
    }
}

void admin_ui_render(AdminUI* ui) {

    SDL_SetRenderDrawColor(ui->ren, 240, 240, 240, 255);
    SDL_RenderClear(ui->ren);

    /* Título */
    admin_ui_draw_text(ui->ren, "ADMINISTRADOR", 130, 20);
    admin_ui_draw_text(ui->ren, "Jugador objetivo:", 30, 85);

    /* Dropdown */
    drawDropdown(ui->ren);

    /* Botones */
    for (int i = 0; i < btnCount; i++) {
        SDL_Rect r = { buttons[i].x, buttons[i].y, buttons[i].w, buttons[i].h };
        SDL_SetRenderDrawColor(ui->ren, 190, 190, 190, 255);
        SDL_RenderFillRect(ui->ren, &r);
        SDL_SetRenderDrawColor(ui->ren, 0, 0, 0, 255);
        SDL_RenderDrawRect(ui->ren, &r);

        admin_ui_draw_text(ui->ren, buttons[i].label,
                           r.x + 8, r.y + 8);
    }

    SDL_RenderPresent(ui->ren);
}

/* ======================================================
                 MANEJO DE CLICS
   ====================================================== */

void admin_ui_handle_click(int x, int y, int sock) {

    /* CLICK EN DROPDOWN */
    if (x >= playerDropDown.box.x && x <= playerDropDown.box.x + playerDropDown.box.w &&
        y >= playerDropDown.box.y && y <= playerDropDown.box.y + playerDropDown.box.h)
    {
        playerDropDown.isOpen = !playerDropDown.isOpen;
        return;
    }

    /* OPCIONES DESPLEGADAS */
    if (playerDropDown.isOpen) {
        for (int i = 0; i < playerDropDown.count; i++) {

            SDL_Rect opt = { playerDropDown.box.x,
                             playerDropDown.box.y + 30 * (i+1),
                             playerDropDown.box.w,
                             30 };

            if (x >= opt.x && x <= opt.x + opt.w &&
                y >= opt.y && y <= opt.y + opt.h)
            {
                playerDropDown.selectedIndex = i;
                playerDropDown.isOpen = 0;

                // SELECCIONAR el jugador en el servidor
                int pid = playerDropDown.ids[i];
                char cmd[128];
                sprintf(cmd, "ADMIN SELECT %d", pid);
                net_send_line(sock, cmd);

                printf("[ADMIN] Jugador seleccionado: %s (ID: %d)\n",
                       playerDropDown.names[i], pid);
                return;
            }
        }
    }

    /* CLIC EN BOTONES */
    for (int i = 0; i < btnCount; i++) {

        Button* b = &buttons[i];

        if (x >= b->x && x <= b->x + b->w &&
            y >= b->y && y <= b->y + b->h)
        {
            // ← CAMBIO CLAVE: Verificar si necesita jugador SOLO para este botón
            if (b->needsPlayer && playerDropDown.selectedIndex < 0) {
                printf("[ADMIN] Seleccione un jugador primero.\n");
                return;
            }

            char cmd[128];

            // Si el botón NO necesita jugador (ej: "ACTUALIZAR LISTA")
            if (!b->needsPlayer) {
                sprintf(cmd, "ADMIN %s", b->cmdTemplate);
                net_send_line(sock, cmd);
                printf("[ADMIN -> SERVER] %s\n", cmd);

                // Si es PLAYERS, leer la respuesta JSON
                if (strcmp(b->cmdTemplate, "PLAYERS") == 0) {
                    // Esperar respuesta del servidor
                    SDL_Delay(100);  // pequeña pausa para que llegue la respuesta
                }
            } else {
                // Si SÍ necesita jugador, enviar comando completo
                sprintf(cmd, "ADMIN %s", b->cmdTemplate);
                net_send_line(sock, cmd);
                printf("[ADMIN -> SERVER] %s\n", cmd);
            }
            return;
        }
    }
}

/* ======================================================
                 THREAD PARA RECIBIR RESPUESTAS DEL SERVIDOR
   ====================================================== */

#ifdef _WIN32
#include <winsock2.h>
DWORD WINAPI receiver_thread(LPVOID arg) {
#else
#include <pthread.h>
void* receiver_thread(void* arg) {
#endif
    int sock = *(int*)arg;
    char buffer[8192];
    int n;

    while ((n = recv(sock, buffer, sizeof(buffer) - 1, 0)) > 0) {
        buffer[n] = '\0';

        // Si es un JSON (empieza con '['), actualizar la lista de jugadores
        if (buffer[0] == '[') {
            admin_ui_update_players(buffer);
        } else {
            printf("[SERVER RESPONDE] %s\n", buffer);
        }
    }

    return 0;
}

/* ======================================================
                 MAIN LOOP
   ====================================================== */

void admin_ui_run() {

    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        printf("SDL ERROR: %s\n", SDL_GetError());
        return;
    }

    AdminUI ui;

    if (admin_ui_init(&ui) < 0) {
        printf("No se pudo iniciar UI admin\n");
        return;
    }

    /* Conectar a servidor */
    sock_global = net_connect("127.0.0.1", 5000);
    if (sock_global < 0) {
        printf("No se pudo conectar al servidor\n");
        return;
    }

    /* Iniciar thread para recibir respuestas */
#ifdef _WIN32
    HANDLE thread = CreateThread(NULL, 0, receiver_thread, &sock_global, 0, NULL);
#else
    pthread_t thread;
    pthread_create(&thread, NULL, receiver_thread, &sock_global);
#endif

    SDL_Event e;
    int running = 1;

    while (running) {

        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_QUIT)
                running = 0;

            if (e.type == SDL_MOUSEBUTTONDOWN)
                admin_ui_handle_click(e.button.x, e.button.y, sock_global);
        }

        admin_ui_render(&ui);
        SDL_Delay(16);
    }

    SDL_DestroyRenderer(ui.ren);
    SDL_DestroyWindow(ui.win);
    SDL_Quit();
}
