#include "game_state.h"
#include <string.h>
#include <stdio.h>
#include "constants.h"

void gs_apply_line(GameState* gs, const char* line) {
    if (strncmp(line, "PLAYER", 6) == 0) {
        sscanf(line, "PLAYER %*d x=%f y=%f lives=%d score=%d",
               &gs->player.x, &gs->player.y, &gs->player.lives, &gs->player.score);
    }
    else if (strncmp(line, "CROC", 4) == 0) {
        if (gs->crocsCount >= 32) return;
        int i = gs->crocsCount++;
        char type[16];
        sscanf(line, "CROC %*s type=%s x=%f y=%f alive=%d",
               type, &gs->crocs[i].x, &gs->crocs[i].y, &gs->crocs[i].alive);
        gs->crocs[i].isRed = (strncmp(type, "RED", 3) == 0);
    }
    else if (strncmp(line, "FRUIT", 5) == 0) {
        if (gs->fruitsCount >= MAX_FRUITS) return;
        int i = gs->fruitsCount++;
        char type[16];
        sscanf(line, "FRUIT %*s type=%s x=%f y=%f points=%d active=%d",
               type, &gs->fruits[i].x, &gs->fruits[i].y, &gs->fruits[i].points, &gs->fruits[i].active);
        strncpy(gs->fruits[i].type, type, sizeof(gs->fruits[i].type)-1);
    }
}
