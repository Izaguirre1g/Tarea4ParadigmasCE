#pragma once
#include <SDL2/SDL.h>

// Dimensiones del spritesheet entities_sheet.png
#define SPRITESHEET_W 308
#define SPRITESHEET_H 287

#define SPRITE_SMALL_W 32
#define SPRITE_SMALL_H 32

#define SPRITE_LARGE_W 44
#define SPRITE_LARGE_H 44

typedef enum {
    // FILA 1: DK JR
    SPRITE_DKJR_STAND = 0,
    SPRITE_DKJR_WALK1,
    SPRITE_DKJR_WALK2,
    SPRITE_DKJR_CLIMB1,
    SPRITE_DKJR_CLIMB2,
    SPRITE_DKJR_JUMP,
    SPRITE_DKJR_HANG,
    SPRITE_DKJR_FALL,
    
    // FILA 2: COCODRILOS
    SPRITE_CROC_RED1,
    SPRITE_CROC_RED2,
    SPRITE_CROC_BLUE1,
    SPRITE_CROC_BLUE2,
    SPRITE_CROC_PINK1,
    SPRITE_CROC_PINK2,
    SPRITE_BIRD1,
    SPRITE_BIRD2,
    
    // FILA 3: FRUTAS
    SPRITE_BANANA,
    SPRITE_STRAWBERRY,
    SPRITE_APPLE,
    SPRITE_PINEAPPLE,
    SPRITE_GRAPES,
    SPRITE_KEY,
    
    // FILA 4: DONKEY KONG
    SPRITE_DK_LEFT,
    SPRITE_DK_CAGE,
    SPRITE_DK_RIGHT,
} SpriteID;

// Coordenadas ajustadas para entities_sheet.png (308x287)
// Layout típico: sprites organizados en una cuadrícula
static const SDL_Rect SPRITE_RECTS[] = {
    // FILA 1: DK JR - sprites 44x44 en la parte superior
    {0,    0,  44, 44},  // SPRITE_DKJR_STAND
    {44,   0,  44, 44},  // SPRITE_DKJR_WALK1
    {88,   0,  44, 44},  // SPRITE_DKJR_WALK2
    {132,  0,  44, 44},  // SPRITE_DKJR_CLIMB1
    {176,  0,  44, 44},  // SPRITE_DKJR_CLIMB2
    {220,  0,  44, 44},  // SPRITE_DKJR_JUMP
    {264,  0,  44, 44},  // SPRITE_DKJR_HANG
    {0,    44, 44, 44},  // SPRITE_DKJR_FALL

    // FILA 2: COCODRILOS - sprites 32x32
    {0,    88,  32, 32},  // SPRITE_CROC_RED1
    {32,   88,  32, 32},  // SPRITE_CROC_RED2
    {64,   88,  32, 32},  // SPRITE_CROC_BLUE1
    {96,   88,  32, 32},  // SPRITE_CROC_BLUE2
    {128,  88,  32, 32},  // SPRITE_CROC_PINK1
    {160,  88,  32, 32},  // SPRITE_CROC_PINK2
    {192,  88,  32, 32},  // SPRITE_BIRD1
    {224,  88,  32, 32},  // SPRITE_BIRD2

    // FILA 3: FRUTAS - sprites 32x32
    {0,    120, 32, 32},  // SPRITE_BANANA
    {32,   120, 32, 32},  // SPRITE_STRAWBERRY
    {64,   120, 32, 32},  // SPRITE_APPLE
    {96,   120, 32, 32},  // SPRITE_PINEAPPLE
    {128,  120, 32, 32},  // SPRITE_GRAPES
    {160,  120, 32, 32},  // SPRITE_KEY

    // FILA 4: DONKEY KONG - sprites 44x44
    {0,    152, 44, 44},  // SPRITE_DK_LEFT
    {44,   152, 44, 44},  // SPRITE_DK_CAGE
    {88,   152, 44, 44},  // SPRITE_DK_RIGHT
};