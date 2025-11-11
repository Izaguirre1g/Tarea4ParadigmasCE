package network;

import patterns.observer.Observer;
import server.GameManager;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Observer, Runnable {
    private final Socket socket;
    private final GameManager manager;
    private PrintWriter out;

    public ClientHandler(Socket socket, GameManager manager) throws IOException {
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
                manager.processInput(line);
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado.");
        } finally {
            manager.removeObserver(this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    @Override
    public void update(String gameState) {
        out.println(gameState);
    }
}
