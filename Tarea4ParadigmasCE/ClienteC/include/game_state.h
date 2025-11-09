#pragma once
#include <SDL3/SDL.h>
#include <stdbool.h>
#include "constants.h"

typedef struct { int v; } Score;     // value-objects simples
typedef struct { int v; } Lives;

typedef struct { float x, y; } Vec2;

typedef enum { CROC_RED=0, CROC_BLUE=1 } CrocType;

typedef struct {
    int   id;
    Vec2  pos;
    Lives lives;
    Score score;
    bool  active;
} Player;

typedef struct {
    int id;
    CrocType type;
    int liana;
    Vec2 pos;
    bool alive;
} Croc;

typedef struct {
    int id;
    int liana;
    Vec2 pos;
    int points;
    bool active;
} Fruit;

typedef struct {
    int levelIdx;
    float speedScale;
    int ladders;
} LevelInfo;

typedef struct {
    Player players[MAX_PLAYERS];
    int    playersCount;

    Croc   crocs[MAX_CROCS];
    int    crocsCount;

    Fruit  fruits[MAX_FRUITS];
    int    fruitsCount;

    LevelInfo level;
    Uint64 timeMs;
} GameState;

void gs_init(GameState* gs);
void gs_apply_line(GameState* gs, const char* line);
