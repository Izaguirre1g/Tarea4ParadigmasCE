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

static void draw_texture(SDL_Renderer* r, SDL_Texture* tex,
                        float x, float y, float scale) {
    if (!tex) return;

    int w, h;
    SDL_QueryTexture(tex, NULL, NULL, &w, &h);

    SDL_Rect dst = {
        (int)(x - (w * scale) / 2),
        (int)(y - (h * scale) / 2),
        (int)(w * scale),
        (int)(h * scale)
    };
    SDL_RenderCopy(r, tex, NULL, &dst);
}

// Dibuja una textura alineada por la parte inferior (para el jugador)
// La posición (x, y) representa donde debe estar la parte inferior del jugador
// expectedHeight es la altura de la hitbox del jugador (28px según la física)
static void draw_texture_bottom(SDL_Renderer* r, SDL_Texture* tex,
                                float x, float y, float scale) {
    if (!tex) return;

    int w, h;
    SDL_QueryTexture(tex, NULL, NULL, &w, &h);

    // Altura esperada del jugador según la física del juego
    // Ajustado para que el sprite se alinee perfectamente con la plataforma
    const float expectedHeight = 32.0f;  // 16px sprite * 2.0 scale = 32px

    SDL_Rect dst = {
        (int)(x - (w * scale) / 2),              // Centrado horizontalmente
        (int)(y - expectedHeight),                // Alineado según hitbox esperada
        (int)(w * scale),
        (int)(h * scale)
    };
    SDL_RenderCopy(r, tex, NULL, &dst);
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

    // Cargar spritesheet - intentar varios archivos
    const char* sprite_files[] = {
        "assets/entities_sheet.png",
        "assets/sheet1.png",
        "assets/donkeykong_sheet.png",
        "assets/dkjr.png",
        NULL
    };

    SDL_Surface* surf = NULL;
    const char* loaded_file = NULL;

    for (int i = 0; sprite_files[i] != NULL; i++) {
        printf(">>> Intentando cargar: %s\n", sprite_files[i]);
        surf = IMG_Load(sprite_files[i]);
        if (surf) {
            loaded_file = sprite_files[i];
            printf("[OK] ✓ Archivo cargado exitosamente\n");
            break;
        } else {
            printf("[WARN] ✗ No encontrado: %s\n", IMG_GetError());
        }
    }

    if (!surf) {
        printf("[ERROR] No se pudo cargar ningún spritesheet\n");
        printf("[WARN] Continuando SIN sprites (usará rectángulos)\n");
        printf("==========================================\n\n");
        g->spritesheet = NULL;
        return 0;  // No es error fatal, solo no hay sprites
    }

    printf("\n>>> Spritesheet cargado: %s\n", loaded_file);

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

    // Cargar sprites individuales para cocodrilos y frutas
    printf(">>> Cargando sprites individuales...\n");

    SDL_Surface* temp;

    // Jugador DK Jr
    temp = IMG_Load("assets/dkjr.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_player = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_player) {
            printf("[OK] ✓ Jugador DK Jr cargado\n");
        } else {
            printf("[ERROR] ✗ Jugador: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_player = NULL;
        printf("[WARN] ✗ dkjr.png no encontrado: %s\n", IMG_GetError());
    }

    // Cocodrilo rojo
    temp = IMG_Load("assets/kremling_red.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_croc_red = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        printf("[OK] ✓ Cocodrilo rojo cargado\n");
    } else {
        g->tex_croc_red = NULL;
        printf("[WARN] ✗ kremling_red.png no encontrado\n");
    }

    // Cocodrilo azul
    temp = IMG_Load("assets/kremling_blue.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_croc_blue = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        printf("[OK] ✓ Cocodrilo azul cargado\n");
    } else {
        g->tex_croc_blue = NULL;
        printf("[WARN] ✗ kremling_blue.png no encontrado\n");
    }

    // Banana
    temp = IMG_Load("assets/fruit_bananas.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_fruit_banana = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_fruit_banana) {
            printf("[OK] ✓ Banana cargada\n");
        } else {
            printf("[ERROR] ✗ Banana: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_fruit_banana = NULL;
        printf("[WARN] ✗ fruit_bananas.png no encontrado: %s\n", IMG_GetError());
    }

    // Naranja
    temp = IMG_Load("assets/fruit_oranges.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_fruit_orange = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_fruit_orange) {
            printf("[OK] ✓ Naranja cargada\n");
        } else {
            printf("[ERROR] ✗ Naranja: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_fruit_orange = NULL;
        printf("[WARN] ✗ fruit_oranges.png no encontrado: %s\n", IMG_GetError());
    }

    // Fresa/Cereza
    temp = IMG_Load("assets/fruit_strawberry.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_fruit_strawberry = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        printf("[OK] ✓ Fresa cargada\n");
    } else {
        g->tex_fruit_strawberry = NULL;
        printf("[WARN] ✗ fruit_strawberry.png no encontrado\n");
    }

    printf("==========================================\n\n");

    return 0;
}

void gfx_shutdown(Gfx* g) {
    if (g->tex_fruit_strawberry) SDL_DestroyTexture(g->tex_fruit_strawberry);
    if (g->tex_fruit_orange) SDL_DestroyTexture(g->tex_fruit_orange);
    if (g->tex_fruit_banana) SDL_DestroyTexture(g->tex_fruit_banana);
    if (g->tex_croc_blue) SDL_DestroyTexture(g->tex_croc_blue);
    if (g->tex_croc_red) SDL_DestroyTexture(g->tex_croc_red);
    if (g->tex_player) SDL_DestroyTexture(g->tex_player);
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

        // Usar texturas individuales PNG
        // El servidor envía: "Banana", "Naranja", "Cereza" (mayúscula inicial)
        if ((strstr(f.type, "Banana") || strstr(f.type, "BANANA")) && g->tex_fruit_banana) {
            draw_texture(r, g->tex_fruit_banana, f.x, f.y, 1.2f);
        } else if ((strstr(f.type, "Naranja") || strstr(f.type, "NARANJA")) && g->tex_fruit_orange) {
            draw_texture(r, g->tex_fruit_orange, f.x, f.y, 1.2f);
        } else if ((strstr(f.type, "Cereza") || strstr(f.type, "CEREZA")) && g->tex_fruit_strawberry) {
            draw_texture(r, g->tex_fruit_strawberry, f.x, f.y, 1.2f);
        } else {
            // Fallback a rectángulos de colores
            if (strstr(f.type, "Banana") || strstr(f.type, "BANANA"))
                draw_rect(r, f.x - 8, f.y - 8, 16, 16, COLOR_FRUIT_BANANA);
            else if (strstr(f.type, "Naranja") || strstr(f.type, "NARANJA"))
                draw_rect(r, f.x - 8, f.y - 8, 16, 16, COLOR_FRUIT_NARANJA);
            else
                draw_rect(r, f.x - 8, f.y - 8, 16, 16, COLOR_FRUIT_CEREZA);
        }
    }

    // Cocodrilos (con animación de escala)
    for (int i = 0; i < gs->crocsCount; ++i) {
        Croc c = gs->crocs[i];
        if (!c.alive) continue;

        // Animación simple: alternar escala ligeramente
        int frame = (anim_frame / 15) % 2;
        float scale = frame ? 1.5f : 1.4f;

        // Usar texturas individuales PNG
        if (c.isRed && g->tex_croc_red) {
            draw_texture(r, g->tex_croc_red, c.x, c.y, scale);
        } else if (!c.isRed && g->tex_croc_blue) {
            draw_texture(r, g->tex_croc_blue, c.x, c.y, scale);
        } else {
            // Fallback a rectángulos de colores
            if (c.isRed)
                draw_rect(r, c.x - 14, c.y - 9, 28, 18, COLOR_CROC_RED);
            else
                draw_rect(r, c.x - 14, c.y - 9, 28, 18, COLOR_CROC_BLUE);
        }
    }

    // Jugador (DK Jr)
    if (g->tex_player) {
        // Usar textura individual PNG del jugador
        // Agregar pequeño offset hacia abajo (+8px) para compensar desajuste visual
        // entre la física del servidor y la posición del sprite
        draw_texture(r, g->tex_player, gs->player.x, gs->player.y + 8, 1.5f);
    } else {
        // SIN SPRITE (fallback a rectángulo café)
        draw_rect(r, gs->player.x - 12, gs->player.y - 14, 24, 28, COLOR_PLAYER);
    }

    // HUD en consola (Score y vidas)
    printf("\rScore: %d   Lives: %d   ", gs->player.score, gs->player.lives);
    fflush(stdout);

    // Presentar frame
    SDL_RenderPresent(r);
}