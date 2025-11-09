package network;

import server.GameManager;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameManager manager;
    private PrintWriter out;

    public ClientHandler(Socket socket, GameManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    public void sendLine(String line) {
        out.println(line);
        out.flush();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            manager.addClient(this);
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Recibido: " + line);
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado");
        } finally {
            manager.removeClient(this);
        }
    }
}
