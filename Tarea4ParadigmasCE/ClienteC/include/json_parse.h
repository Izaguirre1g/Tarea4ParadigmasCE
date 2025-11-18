#ifndef JSON_PARSE_H
#define JSON_PARSE_H

typedef struct {
    int id;
    char name[64];
} PlayerInfo;

int parse_players_json(const char* json, PlayerInfo out[], int max);

#endif
