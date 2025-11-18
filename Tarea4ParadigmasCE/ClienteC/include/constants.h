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
#define COLOR_BG         10,10,20,255
#define COLOR_PLAYER     193,154,107,255
#define COLOR_PLATFORM   100,180,255,255
#define COLOR_LIANA      0,200,0,255
#define COLOR_CROC_RED   220,40,40,255
#define COLOR_CROC_BLUE  60,100,255,255
#define COLOR_FRUIT_BANANA 255,255,80,255
#define COLOR_FRUIT_NARANJA 255,140,0,255
#define COLOR_FRUIT_CEREZA 255,0,100,255
#define COLOR_HUD        255,255,255,255
#define COLOR_CAGE       180,140,60,255    // color dorado para la jaula
#define COLOR_CAGE_BARS  80,60,40,255      // color oscuro para las barras

// --- Jaula de Donkey Kong ---
#define CAGE_X 150
#define CAGE_Y 80
#define CAGE_W 60
#define CAGE_H 40

#define N_PLAT 5
static const float PLATFORMS[N_PLAT][4] = {
    {100, 525, 760, 15},  // suelo
    {150, 420, 600, 10},  // plataforma baja
    {250, 320, 400, 10},  // plataforma media
    {100, 220, 500, 10},  // plataforma alta
    {0,   120, 300, 10}   // parte superior (donde está Donkey Kong)
};

#define N_LIANA 6
static const float LIANAS[N_LIANA][4] = {
    {160, 120, 160, 525}, // izquierda
    {240, 120, 240, 525},
    {400, 220, 400, 525}, // central
    {480, 220, 480, 525},
    {640, 120, 640, 525}, // derecha
    {720, 120, 720, 525}
};