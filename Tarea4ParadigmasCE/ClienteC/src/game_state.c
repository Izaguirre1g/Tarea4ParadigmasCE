#include "game_state.h"
#include <string.h>
#include <stdio.h>

void gs_init(GameState* gs) {
    memset(gs, 0, sizeof(*gs));
    gs->playersCount = 0;
    gs->crocsCount = 0;
    gs->fruitsCount = 0;
    gs->level.levelIdx = 1;
    gs->level.speedScale = 1.0f;
}

static void parse_kv_int(const char* s, const char* key, int* out) {
    char fmt[64]; sprintf(fmt, "%[^=]=%%d");
    char kbuf[64]; int v;
    if (sscanf(s, fmt, kbuf, &v) == 2 && strcmp(kbuf, key)==0) *out = v;
}

void gs_apply_line(GameState* gs, const char* line) {
    // Ejemplos: "PLAYER 0 x=100 y=200 lives=3 score=50"
    if (strncmp(line, "PLAYER", 6)==0) {
        int id=0,x=0,y=0,lv=3,sc=0;
        sscanf(line, "PLAYER %d x=%d y=%d lives=%d score=%d", &id,&x,&y,&lv,&sc);
        if (id >=0 && id < MAX_PLAYERS) {
            if (id+1 > gs->playersCount) gs->playersCount = id+1;
            gs->players[id].id = id;
            gs->players[id].pos.x = (float)x;
            gs->players[id].pos.y = (float)y;
            gs->players[id].lives.v = lv;
            gs->players[id].score.v = sc;
            gs->players[id].active = true;
        }
        return;
    }
    if (strncmp(line, "CROC", 4)==0) {
        int id=0, liana=0, x=0, y=0, alive=1; char type[16];
        if (sscanf(line, "CROC %d type=%15s liana=%d x=%d y=%d alive=%d",
                   &id, type, &liana, &x, &y, &alive)==6) {
            if (id>=0 && id<MAX_CROCS) {
                if (id+1 > gs->crocsCount) gs->crocsCount = id+1;
                Croc* c = &gs->crocs[id];
                c->id = id;
                c->type = (strcmp(type,"RED")==0)? CROC_RED : CROC_BLUE;
                c->liana = liana;
                c->pos.x = (float)x; c->pos.y = (float)y;
                c->alive = alive!=0;
            }
        }
        return;
    }
    if (strncmp(line, "FRUIT", 5)==0) {
        int id=0, liana=0, x=0, y=0, pts=0, active=1;
        if (sscanf(line, "FRUIT %d liana=%d x=%d y=%d points=%d active=%d",
                   &id, &liana, &x, &y, &pts, &active)==6) {
            if (id>=0 && id<MAX_FRUITS) {
                if (id+1 > gs->fruitsCount) gs->fruitsCount = id+1;
                Fruit* f = &gs->fruits[id];
                f->id=id; f->liana=liana; f->pos.x=x; f->pos.y=y;
                f->points=pts; f->active=active!=0;
            }
        }
        return;
    }
    if (strncmp(line, "STATE", 5)==0) {
        int t=0; float sp=1.0f;
        sscanf(line, "STATE TIME=%d SPEED=%f", &t, &sp);
        gs->timeMs = (Uint64)t;
        gs->level.speedScale = sp;
        return;
    }
    if (strncmp(line, "LEVEL", 5)==0) {
        int idx=1, ladd=5; float s=1.0f;
        sscanf(line, "LEVEL %d LADDERS=%d VEL_SCALE=%f", &idx,&ladd,&s);
        gs->level.levelIdx=idx; gs->level.ladders=ladd; gs->level.speedScale=s;
        return;
    }
}
