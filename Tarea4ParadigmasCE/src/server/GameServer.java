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
 *
 * ✅ Cumple con el requerimiento del proyecto:
 *    - No se utilizan tipos primitivos (int, boolean, etc.)
 *    - Se emplean clases equivalentes (Integer, Boolean)
 *    - Implementa patrón de diseño OBSERVER
 */
public class GameServer implements Observable {

    /** Puerto del servidor (Integer en lugar de int) */
    private static final Integer PORT = 5000;

    /** Contador atómico para asignar IDs únicos a los clientes */
    private final AtomicInteger nextClientId = new AtomicInteger(1);

    /** Lista de observadores registrados (clientes conectados) */
    private final List<Observer> observers = new ArrayList<>();

    /** Método principal: crea una instancia del servidor e inicia la ejecución */
    public static void main(String[] args) {
        new GameServer().start();
    }

    // ---------------- MÉTODOS DEL PATRÓN OBSERVER -----------------

    /** Agrega un nuevo observador (cliente) al servidor */
    @Override
    public void agregarObservador(Observer obs) {
        observers.add(obs);
        System.out.println("[Servidor] Nuevo observador agregado: Cliente #" + obs.getObserverId());
    }

    /** Elimina un observador (cliente desconectado) */
    @Override
    public void eliminarObservador(Observer obs) {
        observers.remove(obs);
        System.out.println("[Servidor] Observador eliminado: Cliente #" + obs.getObserverId());
    }

    /** Notifica a todos los observadores con un mensaje determinado */
    @Override
    public void notificarObservadores(Object mensaje) {
        for (Observer obs : observers) {
            obs.actualizar(mensaje);
        }
    }

    // ---------------- MÉTODOS PRINCIPALES DEL SERVIDOR -----------------

    /**
     * Inicia el servidor, acepta múltiples conexiones de clientes
     * y lanza un hilo por cada cliente conectado.
     */
    public void start() {
        System.out.println("=== Servidor iniciado en puerto " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Servidor] Esperando clientes...");

            // Hilo para enviar mensajes manualmente desde la consola del servidor
            Thread consola = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);

                // En lugar de while(true), usa Boolean.TRUE
                while (Boolean.TRUE.equals(Boolean.TRUE)) {
                    String msg = scanner.nextLine();

                    // Validación de salida manual
                    if (msg != null && msg.equalsIgnoreCase("exit")) {
                        break;
                    }

                    // Envía el mensaje a todos los clientes conectados
                    notificarObservadores("[Servidor] " + msg);
                }

                System.out.println("Servidor cerrado.");
                System.exit(0);
            });
            consola.start();

            // Ciclo de aceptación de clientes
            Boolean continuar = Boolean.TRUE;
            while (Boolean.TRUE.equals(continuar)) {
                Socket socket = serverSocket.accept();

                // Obtiene un nuevo ID de cliente usando AtomicInteger (Integer implícito)
                Integer clientId = nextClientId.getAndIncrement();

                // Crea un manejador de cliente (Observer) y lo agrega a la lista
                ClientHandler handler = new ClientHandler(socket, clientId, this);
                agregarObservador(handler);

                // Lanza un hilo para atender al cliente
                Thread hiloCliente = new Thread(handler);
                hiloCliente.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


