#ifndef NET_H
#define NET_H

#include "game_state.h"

int net_connect(const char* ip, int port);
void net_start_receiver(int sock, GameState* gs);
void net_send_line(int sock, const char* line);
void net_close(int sock);

const char* net_get_players_json();  // NUEVO

#endif
