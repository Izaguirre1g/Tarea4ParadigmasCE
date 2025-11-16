#include "render.h"
#include "constants.h"
#include "sprites.h"
#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <stdio.h>
#include <string.h>

static void draw_rect(SDL_Renderer* r, float x, float y, float w, float h,
                      int red, int green, int blue, int alpha) {
    SDL_SetRenderDrawColor(r, red, green, blue, alpha);
    SDL_Rect rect = {(int)x, (int)y, (int)w, (int)h};
    SDL_RenderFillRect(r, &rect);
}

static void draw_sprite(SDL_Renderer* r, SDL_Texture* tex, SpriteID id,
                       float x, float y, float scale) {
    if (!tex) return;

    SDL_Rect src = SPRITE_RECTS[id];
    SDL_Rect dst = {
        (int)x,
        (int)y,
        (int)(src.w * scale),
        (int)(src.h * scale)
    };
    SDL_RenderCopy(r, tex, &src, &dst);
}

int gfx_init(Gfx* g) {
    printf("\n");
    printf("==========================================\n");
    printf("  DONKEY KONG JR - CLIENTE SDL2\n");
    printf("==========================================\n");

    // Inicializar SDL2
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        printf("[ERROR] SDL_Init: %s\n", SDL_GetError());
        return -1;
    }
    printf("[OK] SDL2 inicializado\n");

    // Crear ventana
    g->win = SDL_CreateWindow("DonCEy Kong Jr - Cliente",
                              SDL_WINDOWPOS_CENTERED,
                              SDL_WINDOWPOS_CENTERED,
                              WIN_W, WIN_H,
                              SDL_WINDOW_SHOWN);
    if (!g->win) {
        printf("[ERROR] Ventana: %s\n", SDL_GetError());
        SDL_Quit();
        return -1;
    }
    printf("[OK] Ventana creada (%dx%d)\n", WIN_W, WIN_H);

    // Crear renderer
    g->ren = SDL_CreateRenderer(g->win, -1, SDL_RENDERER_ACCELERATED | SDL_RENDERER_PRESENTVSYNC);
    if (!g->ren) {
        printf("[ERROR] Renderer: %s\n", SDL_GetError());
        SDL_DestroyWindow(g->win);
        SDL_Quit();
        return -1;
    }
    printf("[OK] Renderer creado\n");

    // Inicializar SDL_image
    int imgFlags = IMG_INIT_PNG;
    if (!(IMG_Init(imgFlags) & imgFlags)) {
        printf("[ERROR] SDL_image: %s\n", IMG_GetError());
        SDL_DestroyRenderer(g->ren);
        SDL_DestroyWindow(g->win);
        SDL_Quit();
        return -1;
    }
    printf("[OK] SDL_image inicializado (soporte PNG)\n");

    // Cargar spritesheet
    printf("\n>>> Cargando: assets/dkjr.png\n");
    SDL_Surface* surf = IMG_Load("assets/dkjr.png");

    if (!surf) {
        printf("[WARN] PNG no encontrado: %s\n", IMG_GetError());
        printf("[INFO] Intentando BMP como fallback...\n");

        surf = SDL_LoadBMP("assets/dkjr.bmp");

        if (!surf) {
            printf("[ERROR] Tampoco se encontró BMP: %s\n", SDL_GetError());
            printf("[WARN] Continuando SIN sprites (usará rectángulos)\n");
            printf("==========================================\n\n");
            g->spritesheet = NULL;
            return 0;  // No es error fatal, solo no hay sprites
        }
    }

    printf("[OK] Imagen cargada: %dx%d píxeles\n", surf->w, surf->h);

    // Configurar transparencia (color key = blanco)
    SDL_SetColorKey(surf, SDL_TRUE, SDL_MapRGB(surf->format, 255, 255, 255));
    printf("[OK] Transparencia configurada (blanco como transparente)\n");

    // Crear textura desde surface
    g->spritesheet = SDL_CreateTextureFromSurface(g->ren, surf);
    SDL_FreeSurface(surf);

    if (!g->spritesheet) {
        printf("[ERROR] Textura: %s\n", SDL_GetError());
        return -1;
    }

    printf("[OK] Textura GPU creada\n");
    printf("\n*** ✅ SPRITES CARGADOS EXITOSAMENTE ***\n");
    printf("==========================================\n\n");
    return 0;
}

void gfx_shutdown(Gfx* g) {
    if (g->spritesheet) SDL_DestroyTexture(g->spritesheet);
    if (g->ren) SDL_DestroyRenderer(g->ren);
    if (g->win) SDL_DestroyWindow(g->win);
    IMG_Quit();
    SDL_Quit();
    printf("[GFX] Sistema gráfico cerrado\n");
}

void gfx_draw_env(Gfx* g, const GameState* gs) {
    SDL_Renderer* r = g->ren;
    static int anim_frame = 0;
    anim_frame++;

    // Fondo negro
    SDL_SetRenderDrawColor(r, COLOR_BG);
    SDL_RenderClear(r);

    // Plataformas (azules)
    SDL_SetRenderDrawColor(r, COLOR_PLATFORM);
    for (int i = 0; i < N_PLAT; ++i) {
        SDL_Rect rect = {
            (int)PLATFORMS[i][0],
            (int)PLATFORMS[i][1],
            (int)PLATFORMS[i][2],
            (int)PLATFORMS[i][3]
        };
        SDL_RenderFillRect(r, &rect);
    }

    // Lianas (verdes, triple línea para grosor)
    SDL_SetRenderDrawColor(r, COLOR_LIANA);
    for (int i = 0; i < N_LIANA; ++i) {
        int x = (int)LIANAS[i][0];
        int y1 = (int)LIANAS[i][1];
        int y2 = (int)LIANAS[i][3];
        SDL_RenderDrawLine(r, x-1, y1, x-1, y2);
        SDL_RenderDrawLine(r, x,   y1, x,   y2);
        SDL_RenderDrawLine(r, x+1, y1, x+1, y2);
    }

    // Frutas
    for (int i = 0; i < gs->fruitsCount; ++i) {
        Fruit f = gs->fruits[i];
        if (!f.active) continue;

        if (g->spritesheet) {
            // CON SPRITES
            SpriteID sprite = SPRITE_BANANA;

            if (strstr(f.type, "NARANJA") || strstr(f.type, "Naranja")) {
                sprite = SPRITE_APPLE;  // Naranja → manzana (sprite similar)
            } else if (strstr(f.type, "CEREZA") || strstr(f.type, "Cereza")) {
                sprite = SPRITE_GRAPES; // Cereza → uvas (sprite similar)
            }

            draw_sprite(r, g->spritesheet, sprite, f.x - 12, f.y - 12, 1.5f);
        } else {
            // SIN SPRITES (fallback a rectángulos)
            if (strstr(f.type, "BANANA"))
                draw_rect(r, f.x, f.y, 12, 12, COLOR_FRUIT_BANANA);
            else if (strstr(f.type, "NARANJA"))
                draw_rect(r, f.x, f.y, 12, 12, COLOR_FRUIT_NARANJA);
            else
                draw_rect(r, f.x, f.y, 12, 12, COLOR_FRUIT_CEREZA);
        }
    }

    // Cocodrilos (con animación)
    for (int i = 0; i < gs->crocsCount; ++i) {
        Croc c = gs->crocs[i];
        if (!c.alive) continue;

        if (g->spritesheet) {
            // CON SPRITES ANIMADOS
            int frame = (anim_frame / 15) % 2;  // Cambiar frame cada 15 ticks
            SpriteID sprite;

            if (c.isRed) {
                sprite = frame ? SPRITE_CROC_RED2 : SPRITE_CROC_RED1;
            } else {
                sprite = frame ? SPRITE_CROC_BLUE2 : SPRITE_CROC_BLUE1;
            }

            draw_sprite(r, g->spritesheet, sprite, c.x - 14, c.y - 14, 1.75f);
        } else {
            // SIN SPRITES (fallback a rectángulos)
            if (c.isRed)
                draw_rect(r, c.x, c.y, 28, 18, COLOR_CROC_RED);
            else
                draw_rect(r, c.x, c.y, 28, 18, COLOR_CROC_BLUE);
        }
    }

    // Jugador (DK Jr)
    if (g->spritesheet) {
        // CON SPRITE
        draw_sprite(r, g->spritesheet, SPRITE_DKJR_STAND,
                   gs->player.x - 12, gs->player.y - 12, 2.0f);
    } else {
        // SIN SPRITE (fallback a rectángulo café)
        draw_rect(r, gs->player.x, gs->player.y, 24, 28, COLOR_PLAYER);
    }

    // HUD en consola (Score y vidas)
    printf("\rScore: %d   Lives: %d   ", gs->player.score, gs->player.lives);
    fflush(stdout);

    // Presentar frame
    SDL_RenderPresent(r);
}