#pragma once
#include <SDL3/SDL.h>

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
#define COLOR_FRUIT_RED  255,60,60,255
#define COLOR_FRUIT_YEL  255,230,60,255
#define COLOR_FRUIT_PUR  200,60,255,255

#define N_PLAT 3
static const float PLATFORMS[N_PLAT][4] = {
    {100, 525, 760, 15},  // suelo
    {200, 360, 500, 10},
    {150, 240, 300, 10}
};

#define N_LIANA 3
static const float LIANAS[N_LIANA][4] = {
    {300, 100, 300, 525},
    {480,  60, 480, 360},
    {660, 100, 660, 525}
};