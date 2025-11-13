package network;

import patterns.observer.Observer;
import server.GameServer;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * ClientHandler
 * --------------------------------
 * Hilo que maneja la conexión con un cliente específico.
 * Implementa la interfaz Observer, lo que permite recibir notificaciones
 * del servidor (Observable) y reenviarlas al cliente conectado.
 */
public class ClientHandler implements Runnable, Observer {

    /** Socket asociado al cliente */
    private final Socket socket;

    /** Identificador único del cliente */
    private final Integer clientId;

    /** Referencia al servidor principal (Observable) */
    private final GameServer server;

    /** Flujo de salida hacia el cliente */
    private PrintWriter out;

    /**
     * Constructor del manejador de cliente.
     */
    public ClientHandler(Socket socket, Integer clientId, GameServer server) {
        this.socket = socket;
        this.clientId = clientId;
        this.server = server;
    }

    /**
     * Devuelve el identificador del observador (cliente).
     */
    @Override
    public Integer getObserverId() {
        return clientId;
    }

    /**
     * Método llamado por el Observable (servidor)
     * cuando hay un nuevo mensaje que notificar.
     */
    @Override
    public void actualizar(Object data) {
        if (out != null) {
            out.println(data); // Envía el mensaje al cliente
        }
    }

    /**
     * Bucle principal del cliente:
     * recibe mensajes desde el cliente y los reenvía al servidor.
     */
    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter outWriter = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), Boolean.TRUE)
        ) {
            this.out = outWriter;

            System.out.println("[Servidor] Cliente #" + clientId + " conectado.");
            out.println("Bienvenido Cliente #" + clientId);

            String line;
            Boolean continuar = Boolean.TRUE;

            // Bucle sin tipos primitivos
            while (Boolean.TRUE.equals(continuar)) {
                line = in.readLine();

                if (line == null) {
                    continuar = Boolean.FALSE;
                } else {
                    System.out.println("[Cliente #" + clientId + "] " + line);
                    server.notificarObservadores("Cliente #" + clientId + ": " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("[Servidor] Cliente #" + clientId + " desconectado.");
        } finally {
            server.eliminarObservador(this);
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}

