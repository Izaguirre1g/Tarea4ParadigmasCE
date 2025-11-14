package network;

import patterns.observer.Observer;
import server.GameManager;

import java.io.*;
import java.net.Socket;

/**
 * ClientHandler
 * -----------------------------------------------------
 * Maneja la comunicación con un cliente específico.
 * Implementa Observer para recibir actualizaciones del juego.
 */
public class ClientHandler implements Observer, Runnable {

    private static Integer nextId = 0;

    private final Integer clientId;
    private final Socket socket;
    private final GameManager manager;
    private PrintWriter out;

    public ClientHandler(Socket socket, GameManager manager) throws IOException {
        this.clientId = nextId++;
        this.socket = socket;
        this.manager = manager;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        manager.addObserver(this);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            System.out.println("Cliente " + clientId + " desconectado.");
        } finally {
            manager.removeObserver(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Procesa una línea recibida del cliente.
     */
    private void processLine(String line) {
        if (line.startsWith("INPUT")) {
            // Formato: INPUT 0 LEFT/RIGHT/UP/DOWN/JUMP/STOP
            String[] parts = line.split(" ");
            if (parts.length >= 3) {
                String command = parts[2];
                manager.handleInput(command);
            }
        } else if (line.startsWith("JOIN")) {
            System.out.println("Cliente " + clientId + " se unió al juego");
        }
    }

    @Override
    public void actualizar(Object mensaje) {
        if (mensaje instanceof String) {
            out.print((String) mensaje);
            out.flush();
        }
    }

    @Override
    public Integer getObserverId() {
        return clientId;
    }
}