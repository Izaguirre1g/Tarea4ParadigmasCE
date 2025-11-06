#include <stdio.h>      // Para funciones estándar de entrada/salida (printf, fgets, etc.)
#include <stdlib.h>     // Para exit(), malloc(), etc.
#include <string.h>     // Para funciones de manejo de cadenas (strlen, strcmp, etc.)

// -----------------------------------------------------------------------------
// Configuración de compatibilidad entre Windows y Linux
// -----------------------------------------------------------------------------
#ifdef _WIN32
  #define _WINSOCK_DEPRECATED_NO_WARNINGS   // Evita advertencias por funciones antiguas de winsock
  #include <winsock2.h>                     // Librería de sockets para Windows
  #pragma comment(lib, "ws2_32.lib")        // Enlaza automáticamente la librería ws2_32.dll
  #define CLOSESOCK closesocket             // Alias: en Windows se usa closesocket()
#else
  #include <sys/socket.h>                   // Librería de sockets en Linux/Unix
  #include <arpa/inet.h>                    // Para inet_addr(), htons(), etc.
  #include <unistd.h>                       // Para close()
  #define CLOSESOCK close                   // Alias: en Linux se usa close()
#endif

#include <pthread.h>                        // Para crear hilos (pthread_create, pthread_detach, etc.)

// -----------------------------------------------------------------------------
// Variable global del socket de cliente
// -----------------------------------------------------------------------------
int sock;

/**
 * Hilo que recibe mensajes del servidor de manera continua.
 * --------------------------------------------------------
 * Esta función se ejecuta en paralelo al hilo principal.
 * Se mantiene escuchando (bloqueante) los datos que envía el servidor
 * y los imprime en la consola del cliente.
 */
void *recibir_mensajes(void *arg) {
    char buffer[1024];                      // Buffer para almacenar mensajes recibidos

    while (1) {
        // recv() espera datos del servidor. Retorna la cantidad de bytes recibidos.
        int bytes = recv(sock, buffer, sizeof(buffer) - 1, 0);

        // Si el servidor cierra la conexión o hay error, se sale del bucle.
        if (bytes <= 0) break;

        // Agrega terminador de cadena ('\0') para imprimir correctamente.
        buffer[bytes] = '\0';

        // Muestra el mensaje recibido en consola.
        printf("[Servidor] %s\n", buffer);
    }

    return NULL; // Termina el hilo de recepción.
}

/**
 * Función principal del cliente.
 * -------------------------------
 * - Configura la conexión con el servidor Java.
 * - Lanza un hilo para recibir mensajes.
 * - Envía los mensajes que el usuario escriba.
 */
int main() {

#ifdef _WIN32
    // Inicialización de la librería Winsock en Windows.
    WSADATA wsa;
    WSAStartup(MAKEWORD(2,2), &wsa);
#endif

    // -------------------------------------------------------------------------
    // 1. Crear socket y definir dirección del servidor
    // -------------------------------------------------------------------------
    struct sockaddr_in serverAddr;          // Estructura que contiene IP y puerto del servidor
    sock = socket(AF_INET, SOCK_STREAM, 0); // Crea un socket TCP (AF_INET = IPv4, SOCK_STREAM = TCP)

    serverAddr.sin_family = AF_INET;        // Tipo de protocolo: IPv4
    serverAddr.sin_port = htons(5000);      // Puerto del servidor (5000), convertido a formato de red
    serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1"); // IP del servidor (localhost)

    // -------------------------------------------------------------------------
    // 2. Intentar conectar con el servidor Java
    // -------------------------------------------------------------------------
    if (connect(sock, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        printf("No se pudo conectar al servidor.\n");
        return 1;                           // Sale si la conexión falla
    }

    printf("Conectado al servidor.\n");

    // -------------------------------------------------------------------------
    // 3. Lanzar un hilo que escuche los mensajes del servidor
    // -------------------------------------------------------------------------
    pthread_t hilo;                         // Identificador del hilo
    pthread_create(&hilo, NULL, recibir_mensajes, NULL); // Crea el hilo de recepción
    pthread_detach(hilo);                   // Lo marca como independiente (no necesita join)

    // -------------------------------------------------------------------------
    // 4. Bucle principal: el usuario escribe y envía mensajes
    // -------------------------------------------------------------------------
    char mensaje[256];

    while (1) {
        printf("Escribe mensaje (o 'exit' para salir): ");

        // Lee una línea completa desde teclado (stdin)
        fgets(mensaje, sizeof(mensaje), stdin);

        // Elimina el salto de línea al final del texto
        mensaje[strcspn(mensaje, "\n")] = '\0';

        // Si el usuario escribe "exit", se corta la conexión.
        if (strcmp(mensaje, "exit") == 0) break;

        // Envía el mensaje al servidor (sin incluir el terminador '\0')
        send(sock, mensaje, strlen(mensaje), 0);

        // Envía un salto de línea para que el servidor lo lea por línea completa
        send(sock, "\n", 1, 0);
    }

    // -------------------------------------------------------------------------
    // 5. Cerrar conexión y limpiar recursos
    // -------------------------------------------------------------------------
    CLOSESOCK(sock);                        // Cierra el socket (cross-platform)
#ifdef _WIN32
    WSACleanup();                           // Limpia la librería Winsock en Windows
#endif

    printf("Cliente cerrado.\n");
    return 0;
}

