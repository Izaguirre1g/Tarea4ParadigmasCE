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

    private final Socket socket;       // Socket asociado al cliente
    private final Integer clientId;    // Identificador único del cliente
    private final GameServer server;   // Referencia al servidor principal
    private PrintWriter out;           // Flujo de salida hacia el cliente

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
     * Bucle principal del cliente: recibe mensajes del cliente
     * y los reenvía al servidor para difusión.
     */
    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter outWriter = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            this.out = outWriter;

            System.out.println("[Servidor] Cliente #" + clientId + " conectado.");
            out.println("Bienvenido Cliente #" + clientId);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("[Cliente #" + clientId + "] " + line);
                server.notificarObservadores("Cliente #" + clientId + ": " + line);
            }

        } catch (IOException e) {
            System.out.println("[Servidor] Cliente #" + clientId + " desconectado.");
        } finally {
            server.eliminarObservador(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
