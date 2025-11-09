package server;

import network.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    public static void main(String[] args) {
        GameManager manager = new GameManager();

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Servidor corriendo en puerto 5000...");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Cliente conectado");
                ClientHandler handler = new ClientHandler(client, manager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
