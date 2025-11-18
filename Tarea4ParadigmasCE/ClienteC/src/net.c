#include "net.h"
#include <stdio.h>
#include <string.h>
#include <pthread.h>

#ifdef _WIN32
  #include <winsock2.h>
  #define CLOSESOCK closesocket
#else
  #include <sys/socket.h>
  #include <arpa/inet.h>
  #include <unistd.h>
  #define CLOSESOCK close
#endif

/* ============================================================
   Variables globales usadas por ambos clientes (admin y juego)
   ============================================================ */

static GameState* gstate = NULL;   // sólo para dkj_client
static int gsock = -1;

// JSON recibido desde ADMIN PLAYERS
static char g_players_json[2048] = {0};


/* ============================================================
   HILO DE RECEPCIÓN
   Admin_client:
       - si recibe JSON → llama admin_ui_update_players()
   Cliente normal:
       - procesa líneas del estado del juego
   ============================================================ */
static void* recv_thread(void* _) {
    (void)_;

    char buf[2048];

    for (;;) {

        int n = recv(gsock, buf, sizeof(buf) - 1, 0);
        if (n <= 0) break;

        buf[n] = 0;  // Terminar string recibida

        /* ===========================================================
           1) Detectar si es JSON para admin_client
           =========================================================== */
#ifdef ADMIN_CLIENT
        if (buf[0] == '{') {

            strncpy(g_players_json, buf, sizeof(g_players_json) - 1);

            extern void admin_ui_update_players(const char* json);
            admin_ui_update_players(g_players_json);

            continue;   // NO procesar como estado de juego
        }
#endif

        /* ===========================================================
           2) Cliente JUEGO → procesar frames
           =========================================================== */
        if (gstate) {
            int off = 0;
            for (int i = 0; i < n; i++) {

                if (buf[i] == '\n') {
                    buf[i] = 0;

                    // Reiniciar contadores al iniciar frame del jugador
                    if (strncmp(buf + off, "PLAYER", 6) == 0) {
                        gstate->crocsCount = 0;
                        gstate->fruitsCount = 0;
                    }

                    gs_apply_line(gstate, buf + off);
                    off = i + 1;
                }
            }
        }
    }

    printf("[NET] Conexión cerrada.\n");
    return NULL;
}


/* ============================================================
   Conectar al servidor
   ============================================================ */
int net_connect(const char* ip, int port) {

#ifdef _WIN32
    WSADATA wsa;
    WSAStartup(MAKEWORD(2, 2), &wsa);
#endif

    int s = socket(AF_INET, SOCK_STREAM, 0);
    if (s < 0) {
        perror("socket");
        return -1;
    }

    struct sockaddr_in a;
    memset(&a, 0, sizeof(a));
    a.sin_family = AF_INET;
    a.sin_port = htons(port);
    a.sin_addr.s_addr = inet_addr(ip);

    if (connect(s, (struct sockaddr*)&a, sizeof(a)) < 0) {
        perror("connect");
        CLOSESOCK(s);
        return -1;
    }

    gsock = s;
    printf("[NET] Conectado a %s:%d\n", ip, port);
    return s;
}


/* ============================================================
   Iniciar hilo receptor (solo modo juego usa gstate)
   ============================================================ */
void net_start_receiver(int sock, GameState* gs) {
    gstate = gs;     // NULL si es admin
    gsock = sock;

    pthread_t th;
    pthread_create(&th, NULL, recv_thread, NULL);
    pthread_detach(th);
}


/* ============================================================
   Enviar línea al servidor
   ============================================================ */
void net_send_line(int sock, const char* line) {
    if (!line) return;
    send(sock, line, (int)strlen(line), 0);
    send(sock, "\n", 1, 0);
}


/* ============================================================
   Cerrar socket
   ============================================================ */
void net_close(int sock) {
    if (sock >= 0) CLOSESOCK(sock);
#ifdef _WIN32
    WSACleanup();
#endif
}


/* ============================================================
   Admin client obtiene JSON más reciente
   ============================================================ */
const char* net_get_players_json() {
    return g_players_json;
}
