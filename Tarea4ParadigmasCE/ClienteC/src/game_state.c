#include "game_state.h"
#include <string.h>
#include <stdio.h>
#include "constants.h"

void gs_apply_line(GameState* gs, const char* line) {
    if (strncmp(line, "PLAYER", 6) == 0) {
        // Intentar leer con won y gained_life
        int won = 0;
        int gainedLife = 0;  // ← NUEVO

        // Primero intentar leer con gained_life
        int matched = sscanf(line, "PLAYER %*d x=%f y=%f lives=%d score=%d won=%d gained_life=%d",
               &gs->player.x, &gs->player.y, &gs->player.lives, &gs->player.score, &won, &gainedLife);

        // Si no tiene gained_life (formato antiguo), usar 0
        if (matched < 6) {
            sscanf(line, "PLAYER %*d x=%f y=%f lives=%d score=%d won=%d",
                   &gs->player.x, &gs->player.y, &gs->player.lives, &gs->player.score, &won);
            gainedLife = 0;
        }

        gs->player.hasWon = won;
        gs->player.gainedLife = gainedLife;  // ← NUEVO
    }
    else if (strncmp(line, "CAGE", 4) == 0) {
        // El servidor envía info de la jaula (ya está en constants.h)
        // No necesitamos procesarla dinámicamente por ahora
    }
    else if (strncmp(line, "MARIO", 5) == 0) {
        // Formato: "MARIO 0 x=140 y=130 dir=R"
        char dir[4];
        sscanf(line, "MARIO %*d x=%f y=%f dir=%s",
               &gs->mario.x, &gs->mario.y, dir);
        gs->mario.direction = dir[0];  // 'R' o 'L'
    }
    else if (strncmp(line, "CROC", 4) == 0) {
        if (gs->crocsCount >= 32) return;
        int i = gs->crocsCount++;
        char type[16];
        // Formato del servidor: "CROC 1 type=RED x=160 y=300 alive=1"
        sscanf(line, "CROC %*d type=%s x=%f y=%f alive=%d",
               type, &gs->crocs[i].x, &gs->crocs[i].y, &gs->crocs[i].alive);
        gs->crocs[i].isRed = (strncmp(type, "RED", 3) == 0);
    }
    else if (strncmp(line, "FRUIT", 5) == 0) {
        if (gs->fruitsCount >= MAX_FRUITS) return;
        int i = gs->fruitsCount++;
        char type[16];
        // Formato del servidor: "FRUIT 1 type=BANANA x=240 y=250 points=70 active=1"
        sscanf(line, "FRUIT %*d type=%s x=%f y=%f points=%d active=%d",
               type, &gs->fruits[i].x, &gs->fruits[i].y, &gs->fruits[i].points, &gs->fruits[i].active);
        strncpy(gs->fruits[i].type, type, sizeof(gs->fruits[i].type)-1);
    }
}