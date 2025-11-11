#pragma once
#include <SDL3/SDL.h>

typedef struct {
    float x, y;
    int lives, score;
} Player;

typedef struct {
    float x, y;
    int alive;
    int isRed; // 1 = rojo, 0 = azul
} Croc;

typedef struct {
    float x, y;
    int points;
    int active;
    char type[16];
} Fruit;

typedef struct {
    Player player;
    Croc crocs[32];
    Fruit fruits[32];
    int crocsCount;
    int fruitsCount;
} GameState;

void gs_apply_line(GameState* gs, const char* line);
