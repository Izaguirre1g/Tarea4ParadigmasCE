package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer {

    private static final Integer PORT = 5000;
    private final AtomicInteger nextClientId = new AtomicInteger(1);

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        System.out.println("=== Servidor iniciado en puerto " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado: " + socket.getInetAddress());

            ClientHandler handler = new ClientHandler(socket, nextClientId.getAndIncrement());
            Thread hiloCliente = new Thread(handler);
            hiloCliente.start();

            // 🔹 Leer mensajes desde la consola del servidor y enviarlos al cliente
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                String msg = scanner.nextLine();
                if (msg.equalsIgnoreCase("exit")) break;
                out.println(msg);
            }

            socket.close();
            System.out.println("Servidor cerrado.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final Integer clientId;

        ClientHandler(Socket socket, Integer clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        public Integer getClientId() {
            return clientId;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            ) {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("[Cliente #" + clientId + "] " + line);
                }
            } catch (IOException e) {
                System.out.println("Cliente #" + clientId + " desconectado.");
            }
        }
    }
}
