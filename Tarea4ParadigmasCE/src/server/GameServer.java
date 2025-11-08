package server;

import network.ClientHandler;
import patterns.observer.Observable;
import patterns.observer.Observer;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameServer
 * -------------------------------
 * Servidor principal del proyecto Donkey Kong Jr.
 * Implementa el patrón Observer, permitiendo notificar
 * a todos los clientes conectados cada vez que se envía un mensaje.
 */
public class GameServer implements Observable {

    private static final Integer PORT = 5000; // Puerto de escucha
    private final AtomicInteger nextClientId = new AtomicInteger(1); // Contador de IDs de cliente
    private final List<Observer> observers = new ArrayList<>(); // Lista de observadores (clientes)

    public static void main(String[] args) {
        new GameServer().start();
    }

    // ---------------- Métodos del patrón Observer -----------------

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
    public void notificarObservadores(Object mensaje) {
        for (Observer obs : observers) {
            obs.actualizar(mensaje);
        }
    }


    // ---------------- SERVIDOR PRINCIPAL -----------------

    /**
     * Inicia el servidor, acepta conexiones y lanza hilos por cliente.
     */
    public void start() {
        System.out.println("=== Servidor iniciado en puerto " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Servidor] Esperando clientes...");

            // Hilo para enviar mensajes manuales desde la consola del servidor
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

                // Crea un manejador de cliente (Observer) y lo agrega
                ClientHandler handler = new ClientHandler(socket, clientId, this);
                agregarObservador(handler);

                Thread hiloCliente = new Thread(handler);
                hiloCliente.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

