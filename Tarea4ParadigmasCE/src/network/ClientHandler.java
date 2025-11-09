package network;

import server.GameManager;

import java.io.*;
import java.net.Socket;

/**
 * ClientHandler
 * -----------------------------------------------------
 * Atiende la comunicación individual con un cliente.
 * Lee comandos (JOIN, INPUT, ADMIN...) y reenvía los
 * datos del juego desde el GameManager.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameManager manager;
    private PrintWriter out;

    public ClientHandler(Socket socket, GameManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    /** Envía una línea de texto al cliente */
    public void sendLine(String line) {
        if (out != null) {
            out.println(line);
            out.flush();
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            out = new PrintWriter(socket.getOutputStream(), true);
            manager.addClient(this);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Cliente> " + line);
                if (line.startsWith("INPUT")) {
                    manager.processInput(line);
                }
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + e.getMessage());
        } finally {
            manager.removeClient(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
