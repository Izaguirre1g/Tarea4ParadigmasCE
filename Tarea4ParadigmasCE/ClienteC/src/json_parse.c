#include "json_parse.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

int parse_players_json(const char* json, PlayerInfo out[], int max) {
    int count = 0;
    const char* p = json;

    while (*p && count < max) {

        // Buscar "id"
        p = strstr(p, "\"id\"");
        if (!p) break;

        p = strchr(p, ':');
        if (!p) break;
        p++;

        int id = atoi(p);

        // Buscar "name"
        p = strstr(p, "\"name\"");
        if (!p) break;
        p = strchr(p, ':');
        if (!p) break;
        p++;

        // Saltar espacios y comillas
        while (*p == ' ' || *p == '\"') p++;

        char name[64] = {0};
        int i = 0;

        while (*p && *p != '\"' && i < 63) {
            name[i++] = *p++;
        }

        out[count].id = id;
        strncpy(out[count].name, name, 63);
        count++;
    }

    return count;
}
