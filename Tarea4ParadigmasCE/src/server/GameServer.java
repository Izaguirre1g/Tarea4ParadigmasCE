package server;

import network.ClientHandler;
import utils.GameConstants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * GameServer
 * -----------------------------------------------------
 * Ahora solo acepta sockets y crea ClientHandler.
 * Cada jugador tendrá su propio GameManager a través de PlayerRegistry.
 */
public class GameServer {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(GameConstants.SERVER_PORT)) {
            System.out.println("Servidor corriendo en puerto " + GameConstants.SERVER_PORT + "...");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + client.getInetAddress());

                ClientHandler handler = new ClientHandler(client);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


