#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef _WIN32
  #define _WINSOCK_DEPRECATED_NO_WARNINGS
  #include <winsock2.h>
  #include <ws2tcpip.h>
  #pragma comment(lib, "ws2_32.lib")
  #define CLOSESOCK closesocket
#else
  #include <sys/socket.h>
  #include <arpa/inet.h>
  #include <unistd.h>
  #define CLOSESOCK close
#endif

#include <pthread.h>

int sock;

void *recibir_mensajes(void *arg) {
    char buffer[1024];
    while (1) {
        int bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (bytes <= 0) break;
        buffer[bytes] = '\0';
        printf("[Servidor] %s\n", buffer);
    }
    return NULL;
}

int main() {
#ifdef _WIN32
    WSADATA wsa;
    WSAStartup(MAKEWORD(2,2), &wsa);
#endif

    struct sockaddr_in serverAddr;
    sock = socket(AF_INET, SOCK_STREAM, 0);
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(5000);
    serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");

    if (connect(sock, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        printf("No se pudo conectar al servidor.\n");
        return 1;
    }
    printf("Conectado al servidor.\n");

    pthread_t hilo;
    pthread_create(&hilo, NULL, recibir_mensajes, NULL);
    pthread_detach(hilo);

    char mensaje[256];
    while (1) {
        printf("Escribe mensaje (o 'exit' para salir): ");
        fgets(mensaje, sizeof(mensaje), stdin);
        mensaje[strcspn(mensaje, "\n")] = '\0';

        if (strcmp(mensaje, "exit") == 0) break;

        send(sock, mensaje, strlen(mensaje), 0);
        send(sock, "\n", 1, 0);
    }

    CLOSESOCK(sock);
#ifdef _WIN32
    WSACleanup();
#endif
    printf("Cliente cerrado.\n");
    return 0;
}
