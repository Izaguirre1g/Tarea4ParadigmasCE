package server;

import network.ClientHandler;
import utils.GameConstants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * GameServer
 * -----------------------------------------------------
 * Punto de entrada del servidor. Abre el socket y
 * delega la lógica del juego al GameManager.
 */
public class GameServer {
    public static void main(String[] args) {
        GameManager manager = new GameManager();

        try (ServerSocket serverSocket = new ServerSocket(GameConstants.SERVER_PORT)) {
            System.out.println("Servidor corriendo en puerto " + GameConstants.SERVER_PORT + "...");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + client.getInetAddress());
                ClientHandler handler = new ClientHandler(client, manager);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
