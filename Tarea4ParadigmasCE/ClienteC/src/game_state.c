#include "game_state.h"
#include <string.h>
#include <stdio.h>
#include "constants.h"

void gs_apply_line(GameState* gs, const char* line) {
    if (strncmp(line, "PLAYER", 6) == 0) {
        // Debug: Mostrar la línea completa que llega
        static int debug_count = 0;
        debug_count++;
        if (debug_count % 60 == 0) {  // Cada 60 líneas (aprox 1 segundo)
            printf("[DEBUG PARSER] Línea recibida: %s\n", line);
        }

        // Leer estado completo del jugador incluyendo velocidades
        int won = 0;
        int gainedLife = 0;
        int jumping = 0;
        int onLiana = 0;
        float vx = 0.0f, vy = 0.0f;

        // Intentar leer formato completo
        int matched = sscanf(line, "PLAYER %*d x=%f y=%f vx=%f vy=%f lives=%d score=%d jumping=%d onliana=%d won=%d gained_life=%d",
               &gs->player.x, &gs->player.y, &vx, &vy,
               &gs->player.lives, &gs->player.score,
               &jumping, &onLiana, &won, &gainedLife);

        // Debug: Mostrar cuántos campos se parsearon
        if (debug_count % 60 == 0) {
            printf("[DEBUG PARSER] Campos parseados: %d, onLiana leído: %d\n", matched, onLiana);
        }

        // Si no tiene velocidades (formato antiguo), usar valores por defecto
        if (matched < 8) {
            sscanf(line, "PLAYER %*d x=%f y=%f lives=%d score=%d won=%d gained_life=%d",
                   &gs->player.x, &gs->player.y, &gs->player.lives, &gs->player.score, &won, &gainedLife);
            vx = 0.0f;
            vy = 0.0f;
            jumping = 0;
            onLiana = 0;
        }

        gs->player.vx = vx;
        gs->player.vy = vy;
        gs->player.jumping = jumping;
        gs->player.onLiana = onLiana;
        gs->player.hasWon = won;
        gs->player.gainedLife = gainedLife;
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