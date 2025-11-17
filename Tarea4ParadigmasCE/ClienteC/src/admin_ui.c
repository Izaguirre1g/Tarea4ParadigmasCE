#include "admin_ui.h"
#include "net.h"

#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>

/* ============================================================
   CONFIGURACIÓN DE UI Y BOTONES
   ============================================================ */

/**
 * Representa un botón clickeable.
 */
typedef struct {
    int x, y, w, h;           // posición y tamaño
    const char* label;        // texto del botón
    const char* cmd;          // comando a enviar al servidor
    int dropdown;             // ¿es un botón que abre menú desplegable?
} Button;

static Button buttons[32];
static int btn_count = 0;

/* Dropdown dinámico para eliminar cocodrilos */
static int croc_ids[64];
static int croc_count = 0;
static bool dropdown_open = false;
static int dropdown_x = 0;
static int dropdown_y = 0;


/* ============================================================
   Crear botón
   ============================================================ */
static void add_button(int x, int y, int w, int h,
                       const char* label, const char* cmd,
                       int isDropdown)
{
    buttons[btn_count++] = (Button){
        .x=x, .y=y, .w=w, .h=h,
        .label=label,
        .cmd=cmd,
        .dropdown=isDropdown
    };
}

/* ============================================================
   Dibujar texto TTF
   ============================================================ */
static void draw_text(SDL_Renderer* ren, TTF_Font* font,
                      const char* text, int x, int y)
{
    SDL_Color textColor = {0, 0, 0, 255};   // NEGRO
    SDL_Surface* surf = TTF_RenderText_Blended(font, text,textColor);
    SDL_Texture* tex = SDL_CreateTextureFromSurface(ren, surf);

    SDL_Rect dst = {x, y, surf->w, surf->h};
    SDL_RenderCopy(ren, tex, NULL, &dst);

    SDL_FreeSurface(surf);
    SDL_DestroyTexture(tex);
}

/* ============================================================
   Dibujar botón
   ============================================================ */
static void draw_button(AdminUI* ui, Button* b)
{
    SDL_Rect r = {b->x, b->y, b->w, b->h};

    // Fondo claro
    SDL_SetRenderDrawColor(ui->ren, 230,230,230,255);
    SDL_RenderFillRect(ui->ren, &r);

    // Borde
    SDL_SetRenderDrawColor(ui->ren, 40,40,40,255);
    SDL_RenderDrawRect(ui->ren, &r);

    // Texto
    draw_text(ui->ren, ui->font, b->label, b->x + 10, b->y + 10);
}

/* ============================================================
   Dibujar dropdown (lista de cocodrilos)
   ============================================================ */
static void draw_dropdown(AdminUI* ui)
{
    if (!dropdown_open) return;

    int item_h = 32;

    for (int i = 0; i < croc_count; i++)
    {
        SDL_Rect r = { dropdown_x, dropdown_y + i*item_h, 180, item_h };

        SDL_SetRenderDrawColor(ui->ren, 70,70,70,255);
        SDL_RenderFillRect(ui->ren, &r);

        SDL_SetRenderDrawColor(ui->ren, 20,20,20,255);
        SDL_RenderDrawRect(ui->ren, &r);

        char txt[32];
        sprintf(txt, "Croc ID %d", croc_ids[i]);
        draw_text(ui->ren, ui->font, txt, r.x + 10, r.y + 5);
    }
}

/* ============================================================
   Render general de UI
   ============================================================ */
void admin_ui_render(AdminUI* ui)
{
    SDL_SetRenderDrawColor(ui->ren, 20,20,20,255);
    SDL_RenderClear(ui->ren);

    for (int i = 0; i < btn_count; i++)
        draw_button(ui, &buttons[i]);

    draw_dropdown(ui);

    SDL_RenderPresent(ui->ren);
}

/* ============================================================
   Manejar clics
   ============================================================ */
void admin_ui_handle_click(int x, int y, int sock)
{
    /* --- Si está abierto el dropdown, detecta selección --- */
    if (dropdown_open)
    {
        int item_h = 32;

        for (int i = 0; i < croc_count; i++)
        {
            if (x >= dropdown_x && x <= dropdown_x + 180 &&
                y >= dropdown_y + i*item_h &&
                y <= dropdown_y + (i+1)*item_h)
            {
                char cmd[64];
                sprintf(cmd, "ADMIN DEL_ID %d", croc_ids[i]);
                net_send_line(sock, cmd);

                printf("[ADMIN] Eliminando CROC %d\n", croc_ids[i]);
            }
        }

        dropdown_open = false;
        return;
    }

    /* --- Buscar clic en botones --- */
    for (int i = 0; i < btn_count; i++)
    {
        Button* b = &buttons[i];

        if (x >= b->x && x <= b->x + b->w &&
            y >= b->y && y <= b->y + b->h)
        {
            /* Si es dropdown, abrirlo */
            if (b->dropdown)
            {
                dropdown_open = true;
                dropdown_x = b->x;
                dropdown_y = b->y + b->h;
                return;
            }

            /* Enviar comando directo */
            net_send_line(sock, b->cmd);
            printf("[ADMIN] Enviado: %s\n", b->cmd);
        }
    }
}

/* ============================================================
   Inicializar UI
   ============================================================ */
int admin_ui_init(AdminUI* ui)
{
    SDL_Init(SDL_INIT_VIDEO);
    TTF_Init();

    ui->win = SDL_CreateWindow("Panel Administrador – DKJr",
                               SDL_WINDOWPOS_CENTERED,
                               SDL_WINDOWPOS_CENTERED,
                               380, 600,
                               SDL_WINDOW_SHOWN);
    ui->ren = SDL_CreateRenderer(ui->win, -1, SDL_RENDERER_ACCELERATED);

    ui->font = TTF_OpenFont("assets/font.ttf", 22);
    if (!ui->font)
        printf("Error cargando fuente TTF: %s\n", TTF_GetError());

    int x = 20, y = 40;

    add_button(x, y, 170, 40, "CROC ROJO",   "ADMIN CROC ROJO", 0); y+=50;
    add_button(x, y, 170, 40, "CROC AZUL",   "ADMIN CROC AZUL", 0); y+=70;

    add_button(x, y, 170, 40, "BANANA",      "ADMIN FRUTA BANANA", 0); y+=50;
    add_button(x, y, 170, 40, "NARANJA",     "ADMIN FRUTA NARANJA", 0); y+=50;
    add_button(x, y, 170, 40, "CEREZA",      "ADMIN FRUTA CEREZA", 0); y+=70;

    add_button(x, y, 170, 40, "ELIMINAR CROC", "", 1);  // dropdown
    y+=50;

    add_button(x, y, 170, 40, "LISTAR",      "ADMIN LIST", 0);

    /* TEMPORAL: llenar lista de IDs para el dropdown */
    croc_count = 3;
    croc_ids[0] = 5;
    croc_ids[1] = 9;
    croc_ids[2] = 12;

    return 0;
}

/* ============================================================
   Loop principal de ejecución
   ============================================================ */
void admin_ui_run()
{
    AdminUI ui;

    if (admin_ui_init(&ui) < 0)
    {
        printf("Error inicializando admin UI.\n");
        return;
    }

    int sock = net_connect("127.0.0.1", 5000);

    SDL_Event e;
    int running = 1;

    while (running)
    {
        while (SDL_PollEvent(&e))
        {
            if (e.type == SDL_QUIT)
                running = 0;

            else if (e.type == SDL_MOUSEBUTTONDOWN)
                admin_ui_handle_click(e.button.x, e.button.y, sock);
        }

        admin_ui_render(&ui);
        SDL_Delay(16);
    }

    SDL_DestroyRenderer(ui.ren);
    SDL_DestroyWindow(ui.win);
    TTF_Quit();
    SDL_Quit();
}



