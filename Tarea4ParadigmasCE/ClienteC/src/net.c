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

static GameState* gstate = NULL;
static int gsock = -1;

static void* recv_thread(void* _){
    char buf[1024];
    int off = 0;
    for (;;) {
        int n = recv(gsock, buf+off, (int)sizeof(buf)-1-off, 0);
        if (n <= 0) break;
        int end = off + n; buf[end] = 0;
        // parse por líneas
        int start = 0;
        for (int i=0;i<end;i++){
            if (buf[i]=='\n') {
                buf[i]=0;
                if (gstate) gs_apply_line(gstate, buf+start);
                start = i+1;
            }
        }
        off = end - start;
        memmove(buf, buf+start, off);
    }
    return NULL;
}

int net_connect(const char* ip, int port){
#ifdef _WIN32
    WSADATA wsa; WSAStartup(MAKEWORD(2,2), &wsa);
#endif
    int s = socket(AF_INET, SOCK_STREAM, 0);
    struct sockaddr_in a; memset(&a,0,sizeof(a));
    a.sin_family = AF_INET; a.sin_port = htons(port);
    a.sin_addr.s_addr = inet_addr(ip);
    if (connect(s, (struct sockaddr*)&a, sizeof(a)) < 0) {
        perror("connect"); return -1;
    }
    gsock = s;
    return s;
}

void net_start_receiver(int sock, GameState* gs){
    gstate = gs; gsock = sock;
    pthread_t th; pthread_create(&th, NULL, recv_thread, NULL);
    pthread_detach(th);
}

void net_send_line(int sock, const char* line){
    if (!line) return;
    send(sock, line, (int)strlen(line), 0);
    send(sock, "\n", 1, 0);
}

void net_close(int sock){
    CLOSESOCK(sock);
#ifdef _WIN32
    WSACleanup();
#endif
}
