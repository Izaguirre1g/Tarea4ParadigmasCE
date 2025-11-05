package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// ======================================================
//  Patrón OBSERVER aplicado al servidor DonkeyKong Jr.
// ======================================================

public class GameServer implements Observable {

    private static final Integer PORT = 5000;
    private final AtomicInteger nextClientId = new AtomicInteger(1);
    private final List<Observer> observers = new ArrayList<>();

    public static void main(String[] args) {
        new GameServer().start();
    }

    // ---------------- Métodos del Observer -----------------

    @Override
    public void agregarObservador(Observer obs) {
        observers.add(obs);
        System.out.println("[Servidor] Nuevo observador agregado: Cliente #" + obs.getObserverId());
    }

    @Override
    public void eliminarObservador(Observer obs) {
        observers.remove(obs);
        System.out.println("[Servidor] Observador eliminado: Cliente #" + obs.getObserverId());
    }

    @Override
    public void notificarObservadores(String mensaje) {
        for (Observer obs : observers) {
            obs.actualizar(mensaje);
        }
    }

    // ---------------- SERVIDOR PRINCIPAL -----------------

    public void start() {
        System.out.println("=== Servidor iniciado en puerto " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Servidor] Esperando clientes...");

            // Hilo que permite enviar mensajes manualmente por consola (broadcast)
            Thread consola = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String msg = scanner.nextLine();
                    if (msg.equalsIgnoreCase("exit")) break;
                    notificarObservadores("[Servidor] " + msg);
                }
                System.out.println("Servidor cerrado.");
                System.exit(0);
            });
            consola.start();

            // Acepta múltiples clientes
            while (true) {
                Socket socket = serverSocket.accept();
                Integer clientId = nextClientId.getAndIncrement();

                ClientHandler handler = new ClientHandler(socket, clientId, this);
                agregarObservador(handler); // Se registra como observador

                Thread hiloCliente = new Thread(handler);
                hiloCliente.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------- CLASE CLIENTHANDLER -----------------

    static class ClientHandler implements Runnable, Observer {
        private final Socket socket;
        private final Integer clientId;
        private final GameServer server;
        private PrintWriter out;

        ClientHandler(Socket socket, Integer clientId, GameServer server) {
            this.socket = socket;
            this.clientId = clientId;
            this.server = server;
        }

        @Override
        public Integer getObserverId() {
            return clientId;
        }

        @Override
        public void actualizar(String data) {
            if (out != null) {
                out.println(data);
            }
        }

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

                    // Cuando el cliente escribe algo, el servidor lo reenvía a todos
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
}

// ======================================================
//  Interfaces del Patrón Observer de acuerdo a instrucciones del proyecto
// ======================================================

interface Observable {
    void agregarObservador(Observer obs);
    void eliminarObservador(Observer obs);
    void notificarObservadores(String mensaje);
}

interface Observer {
    void actualizar(String data);
    Integer getObserverId();
}

