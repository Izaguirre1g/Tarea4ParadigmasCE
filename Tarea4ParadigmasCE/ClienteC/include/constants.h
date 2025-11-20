#pragma once
#include <SDL2/SDL.h>

#define WIN_W  960
#define WIN_H  540
#define TICK_MS 16        // 60 FPS

// --- Red ---
#define SERVER_IP   "127.0.0.1"
#define SERVER_PORT 5000

// --- Tamaños máximos ---
#define MAX_PLAYERS  2
#define MAX_CROCS    16
#define MAX_FRUITS   32

// --- Colores ---
#define COLOR_BG         0,0,0,255           // Negro puro
#define COLOR_PLAYER     193,154,107,255
#define COLOR_PLATFORM   100,180,255,255
#define COLOR_LIANA      0,200,0,255
#define COLOR_CROC_RED   220,40,40,255
#define COLOR_CROC_BLUE  60,100,255,255
#define COLOR_FRUIT_BANANA 255,255,80,255
#define COLOR_FRUIT_NARANJA 255,140,0,255
#define COLOR_FRUIT_CEREZA 255,0,100,255
#define COLOR_HUD        255,255,255,255
#define COLOR_CAGE       180,140,60,255
#define COLOR_CAGE_BARS  80,60,40,255

// --- Jaula de Donkey Kong ---
#define CAGE_X 20
#define CAGE_Y 85

#define CAGE_W 70
#define CAGE_H 45

// PLATAFORMAS FINALES
#define N_PLAT 12
static const float PLATFORMS[N_PLAT][4] = {
    {0,   130, 440, 12},  // SUPERIOR izquierda
    {420, 160, 240, 12},  // SUPERIOR derecha (la que agregaste)
    {150, 280, 150, 12},  // Media ARRIBA (izquierda)
    {200, 380, 150, 12},  // Media ABAJO (izquierda, escalonada)
    {680, 330, 280, 12},  // DERECHA
    {50,  520, 80,  12},  // Base izq 1
    {140, 520, 80,  12},  // Base izq 2
    {230, 520, 80,  12},  // Base izq 3
    {500, 470, 70,  12},  // Islita 1
    {600, 470, 70,  12},  // Islita 2
    {700, 470, 70,  12},  // Islita 3
    {800, 470, 70,  12}   // Islita 4
};

// LIANAS - 9 en total
// Formato: {X, Y_inicio, X, Y_fin}
#define N_LIANA 9
static const float LIANAS[N_LIANA][9] = {
    // ════════════════════════════════════════════════════════════════
    // LADO IZQUIERDO (2 lianas)
    // ════════════════════════════════════════════════════════════════

    // LIANA 1: Desde plataforma superior (Y=130) hasta abajo
    // X=150 (cerca del borde izquierdo de la plataforma superior)
    {20.0, 130.0, 150.0, 515.0},

    // LIANA 2: Desde primera plataforma escalonada (Y=280) hasta abajo
    // X=250 (sobre la primera plataforma escalonada)
    {100.0, 130.0, 150.0, 490.0},

    // ════════════════════════════════════════════════════════════════
    // CENTRO (3 lianas)
    // ════════════════════════════════════════════════════════════════

    // LIANA 3: Desde plataforma superior (Y=130) hasta abajo
    // X=370 (cerca del borde derecho de la plataforma superior)
    {220.0, 280.0, 220.0, 500.0},

    // LIANA 4: Entre plataforma superior (Y=130) y nueva plataforma (Y=160)
    // X=440 (en el espacio entre las dos plataformas superiores)
    {370.0, 130.0, 370.0, 470.0},

    // LIANA 5: Desde nueva plataforma (Y=160) hasta abajo
    // X=480 (cerca del inicio de la plataforma Y=160)
    {480, 160, 480, 400},

    // ════════════════════════════════════════════════════════════════
    // LADO DERECHO (4 lianas)
    // ════════════════════════════════════════════════════════════════

    // LIANA 6: Desde nueva plataforma superior derecha (Y=160), pegada al inicio
    // X=670 (casi al borde derecho de la nueva plataforma)
    {570, 160, 670, 430},

    // LIANA 7: Desde nueva plataforma superior derecha (Y=160), un poco separada
    // X=730 (separada ~60px de la liana 6)
    {660, 160, 730, 410},

    // LIANA 8: Liana larga desde ARRIBA (sin conectar a plataforma)
    // X=810 (lado derecho, cerca de la última islita)
    // Y=50 (empieza desde arriba, por encima de todo)
    {760, 50, 810, 430},

    // LIANA 9: Otra liana larga desde ARRIBA (sin conectar)
    // X=880 (más a la derecha que la liana 8)
    // Y=50 (empieza desde arriba, por encima de todo)
    {890, 50, 880, 430}
};