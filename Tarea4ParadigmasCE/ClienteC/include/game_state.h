#pragma once
#include <SDL2/SDL.h>

typedef struct {
    float x, y;
    int lives, score;
    int hasWon;        // 0 = jugando, 1 = victoria
    int gainedLife;    // 0 = normal, 1 = acaba de ganar vida
    int animFrame;     // Frame de animación
    float vx, vy;      // Velocidades para detectar estado
    int onLiana;       // 1 si está en liana, 0 si no
    int jumping;       // 1 si está saltando
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
    float x, y;
    char direction;  // 'R' = derecha, 'L' = izquierda
} Mario;

typedef struct {
    Player player;
    Croc crocs[32];
    Fruit fruits[32];
    Mario mario;     //Posición de Mario
    int crocsCount;
    int fruitsCount;
} GameState;

void gs_apply_line(GameState* gs, const char* line);
