#include "render.h"
#include "constants.h"
#include "sprites.h"
#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_ttf.h>
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
static void draw_texture_bottom(SDL_Renderer* r, SDL_Texture* tex,
                                float x, float y, float scale) {
    if (!tex) return;

    int w, h;
    SDL_QueryTexture(tex, NULL, NULL, &w, &h);

    // La textura se dibuja de modo que su parte inferior
    // coincida exactamente con la coordenada y del jugador
    // Simplemente alineamos la base del sprite con la coordenada y
    SDL_Rect dst = {
        (int)(x - (w * scale) / 2),              // Centrado horizontalmente
        (int)(y - (h * scale)),                  // Parte inferior del sprite en Y
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

    // Inicializar SDL_ttf
    if (TTF_Init() == -1) {
        printf("[ERROR] SDL_ttf: %s\n", TTF_GetError());
        IMG_Quit();
        SDL_DestroyRenderer(g->ren);
        SDL_DestroyWindow(g->win);
        SDL_Quit();
        return -1;
    }
    printf("[OK] SDL_ttf inicializado\n");

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
    printf("\n***SPRITES CARGADOS EXITOSAMENTE ***\n");
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
        printf("[OK] Cocodrilo rojo cargado\n");
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
        printf("[OK] Cocodrilo azul cargado\n");
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
            printf("[OK] Banana cargada\n");
        } else {
            printf("[ERROR] Banana: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_fruit_banana = NULL;
        printf("[WARN] fruit_bananas.png no encontrado: %s\n", IMG_GetError());
    }

    // Naranja
    temp = IMG_Load("assets/fruit_oranges.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_fruit_orange = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_fruit_orange) {
            printf("[OK] Naranja cargada\n");
        } else {
            printf("[ERROR] Naranja: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_fruit_orange = NULL;
        printf("[WARN] fruit_oranges.png no encontrado: %s\n", IMG_GetError());
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

    // Jaula de Donkey Kong
    temp = IMG_Load("assets/jail.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_jail = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_jail) {
            printf("[OK] ✓ Jaula (jail.png) cargada\n");
        } else {
            printf("[ERROR] ✗ Jaula: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_jail = NULL;
        printf("[WARN] ✗ jail.png no encontrado: %s\n", IMG_GetError());
    }

    // Corazón (vidas)
    temp = IMG_Load("assets/heart.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_heart = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_heart) {
            printf("[OK] ✓ Corazón (heart.png) cargado\n");
        } else {
            printf("[ERROR] ✗ Corazón: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_heart = NULL;
        printf("[WARN] ✗ heart.png no encontrado: %s\n", IMG_GetError());
    }

    // Scoreholder (fondo de puntuación)
    temp = IMG_Load("assets/scoreholder.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_scoreholder = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_scoreholder) {
            printf("[OK] ✓ Scoreholder (scoreholder.png) cargado\n");
        } else {
            printf("[ERROR] ✗ Scoreholder: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_scoreholder = NULL;
        printf("[WARN] ✗ scoreholder.png no encontrado: %s\n", IMG_GetError());
    }

    printf("\n>>> Cargando sprites de (DK, Mario, Lianas, Plataformas)...\n");

    // Donkey Kong (dentro de la jaula)
    temp = IMG_Load("assets/dk.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_donkey_kong = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_donkey_kong) {
            printf("[OK] ✓ Donkey Kong (dk.png) cargado\n");
        } else {
            printf("[ERROR] ✗ Donkey Kong: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_donkey_kong = NULL;
        printf("[WARN] ✗ dk.png no encontrado: %s\n", IMG_GetError());
    }

    // Mario (el villano)
    temp = IMG_Load("assets/mario.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_mario = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_mario) {
            printf("[OK] ✓ Mario (mario.png) cargado\n");
        } else {
            printf("[ERROR] ✗ Mario: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_mario = NULL;
        printf("[WARN] ✗ mario.png no encontrado: %s\n", IMG_GetError());
    }

    // Textura de Liana
    temp = IMG_Load("assets/liana.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_liana = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_liana) {
            printf("[OK] ✓ Liana (liana.png) cargada\n");
        } else {
            printf("[ERROR] ✗ Liana: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_liana = NULL;
        printf("[WARN] ✗ liana.png no encontrado: %s\n", IMG_GetError());
    }

    // Textura de Plataforma
    temp = IMG_Load("assets/platform.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_platform = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_platform) {
            printf("[OK] ✓ Plataforma (platform.png) cargada\n");
        } else {
            printf("[ERROR] ✗ Plataforma: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_platform = NULL;
        printf("[WARN] ✗ platform.png no encontrado: %s\n", IMG_GetError());
    }

    printf("*** SPRITES CARGADOS ***\n");

    printf("\n>>> Cargando texturas decorativas (background, agua)...\n");

    // Background (fondo del juego)
    temp = IMG_Load("assets/background.png");
    if (temp) {
        // NO usar color key en el background (queremos todo el fondo)
        g->tex_background = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_background) {
            printf("[OK] ✓ Background (background.png) cargado\n");
        } else {
            printf("[ERROR] ✗ Background: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_background = NULL;
        printf("[WARN] ✗ background.png no encontrado: %s\n", IMG_GetError());
    }

    // Water (agua/decoración base)
    // Nota: El archivo se llama "Water.png" con mayúscula
    temp = IMG_Load("assets/Water.png");
    if (temp) {
        SDL_SetColorKey(temp, SDL_TRUE, SDL_MapRGB(temp->format, 255, 255, 255));
        g->tex_water = SDL_CreateTextureFromSurface(g->ren, temp);
        SDL_FreeSurface(temp);
        if (g->tex_water) {
            printf("[OK] ✓ Water (Water.png) cargado\n");
        } else {
            printf("[ERROR] ✗ Water: Falló crear textura: %s\n", SDL_GetError());
        }
    } else {
        g->tex_water = NULL;
        printf("[WARN] ✗ Water.png no encontrado: %s\n", IMG_GetError());
    }

    printf("*** Decoraciones cargadas ***\n");

    // Cargar fuente para texto
    const char* font_files[] = {
        "assets/arial.ttf",
        "assets/arialbd.ttf",
        NULL
    };

    g->font = NULL;
    for (int i = 0; font_files[i] != NULL; i++) {
        g->font = TTF_OpenFont(font_files[i], 24);  // Tamaño 24
        if (g->font) {
            printf("[OK] ✓ Fuente cargada: %s\n", font_files[i]);
            break;
        }
    }

    if (!g->font) {
        printf("[WARN] ✗ No se pudo cargar ninguna fuente TTF\n");
        printf("[WARN] Los números de puntuación no se mostrarán\n");
    }

    printf("==========================================\n\n");

    return 0;
}

void gfx_shutdown(Gfx* g) {
    if (g->font) TTF_CloseFont(g->font);
    if (g->tex_scoreholder) SDL_DestroyTexture(g->tex_scoreholder);
    if (g->tex_heart) SDL_DestroyTexture(g->tex_heart);
    if (g->tex_jail) SDL_DestroyTexture(g->tex_jail);
    if (g->tex_fruit_strawberry) SDL_DestroyTexture(g->tex_fruit_strawberry);
    if (g->tex_fruit_orange) SDL_DestroyTexture(g->tex_fruit_orange);
    if (g->tex_fruit_banana) SDL_DestroyTexture(g->tex_fruit_banana);
    if (g->tex_croc_blue) SDL_DestroyTexture(g->tex_croc_blue);
    if (g->tex_croc_red) SDL_DestroyTexture(g->tex_croc_red);
    if (g->tex_player) SDL_DestroyTexture(g->tex_player);
    if (g->spritesheet) SDL_DestroyTexture(g->spritesheet);
    if (g->ren) SDL_DestroyRenderer(g->ren);
    if (g->win) SDL_DestroyWindow(g->win);
    if (g->tex_platform) SDL_DestroyTexture(g->tex_platform);
    if (g->tex_liana) SDL_DestroyTexture(g->tex_liana);
    if (g->tex_mario) SDL_DestroyTexture(g->tex_mario);
    if (g->tex_donkey_kong) SDL_DestroyTexture(g->tex_donkey_kong);
    if (g->tex_water) SDL_DestroyTexture(g->tex_water);
    if (g->tex_background) SDL_DestroyTexture(g->tex_background);
    TTF_Quit();
    IMG_Quit();
    SDL_Quit();
    printf("[GFX] Sistema gráfico cerrado\n");
}

void gfx_draw_env(Gfx* g, const GameState* gs) {
    SDL_Renderer* r = g->ren;
    static int anim_frame = 0;
    anim_frame++;

    // FONDO NEGRO
    SDL_SetRenderDrawColor(r, 0, 0, 0, 255);
    SDL_RenderClear(r);

    // SOPORTES PARA LAS 4 ISLITAS (ajustar índices)
    SDL_SetRenderDrawColor(r, 180, 100, 40, 255);
    int islita_positions[4] = {535, 635, 735, 835};

    for (int i = 0; i < 4; i++) {
        int x = islita_positions[i];
        int top_y = 482;
        int bottom_y = 540;

        for (int offset = -4; offset <= 4; offset += 2) {
            SDL_RenderDrawLine(r, x + offset, top_y, x + offset, bottom_y);
        }
    }

    // PLATAFORMAS DEL NIVEL (índices 0-4, ahora son 5)
    if (g->tex_platform) {
        int tex_w, tex_h;
        SDL_QueryTexture(g->tex_platform, NULL, NULL, &tex_w, &tex_h);

        for (int i = 0; i < 5; ++i) {
            int plat_x = (int)PLATFORMS[i][0];
            int plat_y = (int)PLATFORMS[i][1];
            int plat_w = (int)PLATFORMS[i][2];
            int plat_h = (int)PLATFORMS[i][3];

            for (int x = plat_x; x < plat_x + plat_w; x += tex_w) {
                int remaining_w = (plat_x + plat_w) - x;
                int draw_w = (remaining_w < tex_w) ? remaining_w : tex_w;
                SDL_Rect src = {0, 0, draw_w, tex_h};
                SDL_Rect dst = {x, plat_y, draw_w, plat_h};
                SDL_RenderCopy(r, g->tex_platform, &src, &dst);
            }
        }
    } else {
        SDL_SetRenderDrawColor(r, COLOR_PLATFORM);
        for (int i = 0; i < 5; ++i) {
            SDL_Rect rect = {
                (int)PLATFORMS[i][0],
                (int)PLATFORMS[i][1],
                (int)PLATFORMS[i][2],
                (int)PLATFORMS[i][3]
            };
            SDL_RenderFillRect(r, &rect);
        }
    }

    // PLATAFORMAS BASE IZQUIERDA (índices 5-7)
    if (g->tex_platform) {
        int tex_w, tex_h;
        SDL_QueryTexture(g->tex_platform, NULL, NULL, &tex_w, &tex_h);

        for (int i = 5; i < 8; ++i) {
            int plat_x = (int)PLATFORMS[i][0];
            int plat_y = (int)PLATFORMS[i][1];
            int plat_w = (int)PLATFORMS[i][2];
            int plat_h = (int)PLATFORMS[i][3];

            for (int x = plat_x; x < plat_x + plat_w; x += tex_w) {
                int remaining_w = (plat_x + plat_w) - x;
                int draw_w = (remaining_w < tex_w) ? remaining_w : tex_w;
                SDL_Rect src = {0, 0, draw_w, tex_h};
                SDL_Rect dst = {x, plat_y, draw_w, plat_h};
                SDL_RenderCopy(r, g->tex_platform, &src, &dst);
            }
        }
    } else {
        SDL_SetRenderDrawColor(r, COLOR_PLATFORM);
        for (int i = 5; i < 8; ++i) {
            SDL_Rect rect = {
                (int)PLATFORMS[i][0],
                (int)PLATFORMS[i][1],
                (int)PLATFORMS[i][2],
                (int)PLATFORMS[i][3]
            };
            SDL_RenderFillRect(r, &rect);
        }
    }

    // ISLITAS BASE DERECHA (índices 8-11)
    if (g->tex_platform) {
        int tex_w, tex_h;
        SDL_QueryTexture(g->tex_platform, NULL, NULL, &tex_w, &tex_h);

        for (int i = 8; i < 12; ++i) {
            int plat_x = (int)PLATFORMS[i][0];
            int plat_y = (int)PLATFORMS[i][1];
            int plat_w = (int)PLATFORMS[i][2];
            int plat_h = (int)PLATFORMS[i][3];

            for (int x = plat_x; x < plat_x + plat_w; x += tex_w) {
                int remaining_w = (plat_x + plat_w) - x;
                int draw_w = (remaining_w < tex_w) ? remaining_w : tex_w;
                SDL_Rect src = {0, 0, draw_w, tex_h};
                SDL_Rect dst = {x, plat_y, draw_w, plat_h};
                SDL_RenderCopy(r, g->tex_platform, &src, &dst);
            }
        }
    } else {
        SDL_SetRenderDrawColor(r, COLOR_PLATFORM);
        for (int i = 8; i < 12; ++i) {
            SDL_Rect rect = {
                (int)PLATFORMS[i][0],
                (int)PLATFORMS[i][1],
                (int)PLATFORMS[i][2],
                (int)PLATFORMS[i][3]
            };
            SDL_RenderFillRect(r, &rect);
        }
    }

    // Lianas
    if (g->tex_liana) {
        int tex_w, tex_h;
        SDL_QueryTexture(g->tex_liana, NULL, NULL, &tex_w, &tex_h);

        for (int i = 0; i < N_LIANA; ++i) {
            int liana_x = (int)LIANAS[i][0];
            int liana_y1 = (int)LIANAS[i][1];
            int liana_y2 = (int)LIANAS[i][3];

            for (int y = liana_y1; y < liana_y2; y += tex_h) {
                int remaining_h = liana_y2 - y;
                int draw_h = (remaining_h < tex_h) ? remaining_h : tex_h;
                SDL_Rect src = {0, 0, tex_w, draw_h};
                SDL_Rect dst = {liana_x - tex_w/2, y, tex_w, draw_h};
                SDL_RenderCopy(r, g->tex_liana, &src, &dst);
            }
        }
    } else {
        SDL_SetRenderDrawColor(r, COLOR_LIANA);
        for (int i = 0; i < N_LIANA; ++i) {
            int x = (int)LIANAS[i][0];
            int y1 = (int)LIANAS[i][1];
            int y2 = (int)LIANAS[i][3];
            SDL_RenderDrawLine(r, x-1, y1, x-1, y2);
            SDL_RenderDrawLine(r, x,   y1, x,   y2);
            SDL_RenderDrawLine(r, x+1, y1, x+1, y2);
        }
    }

    // Jaula
    if (g->tex_jail) {
        SDL_Rect dst = {CAGE_X, CAGE_Y, CAGE_W, CAGE_H};
        SDL_RenderCopy(r, g->tex_jail, NULL, &dst);
    } else {
        draw_rect(r, CAGE_X, CAGE_Y, CAGE_W, CAGE_H, COLOR_CAGE);
        SDL_SetRenderDrawColor(r, COLOR_CAGE_BARS);
        for (int i = 0; i < 5; i++) {
            int barX = CAGE_X + 5 + i * 12;
            SDL_RenderDrawLine(r, barX, CAGE_Y, barX, CAGE_Y + CAGE_H);
            SDL_RenderDrawLine(r, barX+1, CAGE_Y, barX+1, CAGE_Y + CAGE_H);
        }
        SDL_Rect topBar = {CAGE_X, CAGE_Y, CAGE_W, 3};
        SDL_Rect bottomBar = {CAGE_X, CAGE_Y + CAGE_H - 3, CAGE_W, 3};
        SDL_RenderFillRect(r, &topBar);
        SDL_RenderFillRect(r, &bottomBar);
    }

    // Donkey Kong
    if (g->tex_donkey_kong) {
        int dk_size = 35;
        SDL_Rect dk_dst = {
            CAGE_X + (CAGE_W - dk_size) / 2,
            CAGE_Y + (CAGE_H - dk_size) / 2,
            dk_size, dk_size
        };
        SDL_RenderCopy(r, g->tex_donkey_kong, NULL, &dk_dst);
    }

    // Mario
    if (g->tex_mario) {
        int mario_size = 40;
        SDL_Rect mario_dst = {
            CAGE_X + CAGE_W + 15,
            CAGE_Y - 5,
            mario_size, mario_size
        };
        SDL_RenderCopy(r, g->tex_mario, NULL, &mario_dst);
    }

    // Frutas
    for (int i = 0; i < gs->fruitsCount; ++i) {
        Fruit f = gs->fruits[i];
        if (!f.active) continue;

        if ((strstr(f.type, "Banana") || strstr(f.type, "BANANA")) && g->tex_fruit_banana) {
            draw_texture(r, g->tex_fruit_banana, f.x, f.y, 1.2f);
        } else if ((strstr(f.type, "Naranja") || strstr(f.type, "NARANJA")) && g->tex_fruit_orange) {
            draw_texture(r, g->tex_fruit_orange, f.x, f.y, 1.2f);
        } else if ((strstr(f.type, "Cereza") || strstr(f.type, "CEREZA")) && g->tex_fruit_strawberry) {
            draw_texture(r, g->tex_fruit_strawberry, f.x, f.y, 1.2f);
        }
    }

    // Cocodrilos
    for (int i = 0; i < gs->crocsCount; ++i) {
        Croc c = gs->crocs[i];
        if (!c.alive) continue;

        int frame = (anim_frame / 15) % 2;
        float scale = frame ? 1.5f : 1.4f;

        if (c.isRed && g->tex_croc_red) {
            draw_texture(r, g->tex_croc_red, c.x, c.y, scale);
        } else if (!c.isRed && g->tex_croc_blue) {
            draw_texture(r, g->tex_croc_blue, c.x, c.y, scale);
        } else {
            if (c.isRed)
                draw_rect(r, c.x - 14, c.y - 9, 28, 18, COLOR_CROC_RED);
            else
                draw_rect(r, c.x - 14, c.y - 9, 28, 18, COLOR_CROC_BLUE);
        }
    }

    // Jugador
    if (g->tex_player) {
        draw_texture_bottom(r, g->tex_player, gs->player.x, gs->player.y + 28, 1.5f);
    } else {
        draw_rect(r, gs->player.x - 12, gs->player.y, 24, 28, COLOR_PLAYER);
    }

    // HUD
    SDL_SetRenderDrawBlendMode(r, SDL_BLENDMODE_BLEND);
    SDL_SetRenderDrawColor(r, 0, 0, 0, 200);
    SDL_Rect hud_bg = {0, 0, WIN_W, 50};
    SDL_RenderFillRect(r, &hud_bg);
    SDL_SetRenderDrawBlendMode(r, SDL_BLENDMODE_NONE);

    if (g->font) {
        SDL_Color white = {255, 255, 255, 255};
        SDL_Color yellow = {255, 255, 0, 255};

        SDL_Surface* text_1up = TTF_RenderText_Solid(g->font, "1UP", yellow);
        if (text_1up) {
            SDL_Texture* tex_1up = SDL_CreateTextureFromSurface(r, text_1up);
            SDL_Rect rect_1up = {20, 8, text_1up->w, text_1up->h};
            SDL_RenderCopy(r, tex_1up, NULL, &rect_1up);
            SDL_DestroyTexture(tex_1up);
            SDL_FreeSurface(text_1up);
        }

        char score_text[32];
        snprintf(score_text, sizeof(score_text), "%06d", gs->player.score);
        SDL_Surface* text_score = TTF_RenderText_Solid(g->font, score_text, white);
        if (text_score) {
            SDL_Texture* tex_score = SDL_CreateTextureFromSurface(r, text_score);
            SDL_Rect rect_score = {20, 28, text_score->w, text_score->h};
            SDL_RenderCopy(r, tex_score, NULL, &rect_score);
            SDL_DestroyTexture(tex_score);
            SDL_FreeSurface(text_score);
        }

        SDL_Surface* text_high = TTF_RenderText_Solid(g->font, "HIGH SCORE", yellow);
        if (text_high) {
            SDL_Texture* tex_high = SDL_CreateTextureFromSurface(r, text_high);
            SDL_Rect rect_high = {WIN_W/2 - text_high->w/2, 8, text_high->w, text_high->h};
            SDL_RenderCopy(r, tex_high, NULL, &rect_high);
            SDL_DestroyTexture(tex_high);
            SDL_FreeSurface(text_high);
        }

        char high_text[32];
        snprintf(high_text, sizeof(high_text), "%06d", gs->player.score);
        SDL_Surface* text_highnum = TTF_RenderText_Solid(g->font, high_text, white);
        if (text_highnum) {
            SDL_Texture* tex_highnum = SDL_CreateTextureFromSurface(r, text_highnum);
            SDL_Rect rect_highnum = {WIN_W/2 - text_highnum->w/2, 28, text_highnum->w, text_highnum->h};
            SDL_RenderCopy(r, tex_highnum, NULL, &rect_highnum);
            SDL_DestroyTexture(tex_highnum);
            SDL_FreeSurface(text_highnum);
        }
    }

    // Vidas
    if (g->tex_heart) {
        int heart_size = 25;
        int heart_spacing = 30;
        int start_x = WIN_W - 110;
        int start_y = 12;

        for (int i = 0; i < gs->player.lives && i < 3; i++) {
            SDL_Rect heart_dst = {
                start_x + (i * heart_spacing),
                start_y,
                heart_size, heart_size
            };
            SDL_RenderCopy(r, g->tex_heart, NULL, &heart_dst);
        }
    }

    if (gs->player.hasWon) {
        printf("¡VICTORIA! Score: %d   Lives: %d   *** GANASTE ***   ",
               gs->player.score, gs->player.lives);
    } else {
        printf("\rScore: %d   Lives: %d   ", gs->player.score, gs->player.lives);
    }
    fflush(stdout);

    SDL_RenderPresent(r);
}