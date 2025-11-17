package network;

import patterns.observer.Observer;
import server.GameManager;
import server.PlayerRegistry;
import server.PlayerSession;

import java.io.*;
import java.net.Socket;

/**
 * ClientHandler
 * -----------------------------------------------------
 * Maneja la comunicación con UN socket.
 *
 * Puede ser:
 *   - Cliente de juego (JOIN ...)
 *   - Cliente administrador (ADMIN ...)
 *
 * Cada cliente de juego tiene su propia PlayerSession
 * con su propio GameManager (partida independiente).
 */
public class ClientHandler implements Observer, Runnable {

    private final Socket socket;
    private PlayerSession session;          // solo si es jugador
    private GameManager game;               // partida del jugador

    private final PrintWriter out;
    private BufferedReader in;

    private boolean isGameClient = false;   // true si es jugador, false si es admin
    private Integer adminTargetPlayerId = null; // jugador seleccionado en admin

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                processLine(line.trim());
            }

        } catch (IOException e) {
            System.out.println("[SERVER] Error con cliente: " + e.getMessage());
        } finally {
            // Si era jugador, quitarlo del registro y de su GameManager
            if (session != null) {
                if (game != null) {
                    game.removeObserver(this);
                }
                PlayerRegistry.removeSession(session.id);
            }

            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Procesa una línea recibida del cliente.
     */
    private void processLine(String line) {

        if (line.isEmpty())
            return;

        /* ============================
           1) Cliente jugador (JOIN)
           Formato esperado:
           JOIN <nombreJugador> DKJr
        ============================ */
        if (line.startsWith("JOIN")) {
            String[] p = line.split("\\s+");
            String nombre = (p.length >= 2) ? p[1] : "Jugador";

            // Crear sesión + partida
            this.session = PlayerRegistry.createSession(socket, nombre);
            this.game = session.game;

            // ahora este handler observará SOLO esa partida
            game.addObserver(this);

            isGameClient = true;

            System.out.println("[SERVER] Cliente " + session.id +
                    " se identifica como JUGADOR (" + session.name + ")");

            return;
        }

        /* ============================
           2) Cliente ADMIN
           Cualquier línea que empiece con ADMIN
           (admin_client en C) entra aquí.
        ============================ */
        if (line.startsWith("ADMIN")) {

            isGameClient = false;   // este socket no recibe frames de juego

            String[] p = line.split("\\s+");
            if (p.length < 2) {
                out.println("ERR comando ADMIN incompleto");
                return;
            }

            String cmd = p[1].toUpperCase();

            // ADMIN PLAYERS  -> devolver JSON de jugadores
            if ("PLAYERS".equals(cmd)) {
                String json = PlayerRegistry.getPlayersJson();
                out.println(json);
                return;
            }

            // ADMIN SELECT <playerId> -> seleccionar partida objetivo
            if ("SELECT".equals(cmd)) {
                if (p.length < 3) {
                    out.println("ERR falta id jugador");
                    return;
                }
                try {
                    adminTargetPlayerId = Integer.parseInt(p[2]);
                    out.println("OK admin seleccionado jugador " + adminTargetPlayerId);
                } catch (NumberFormatException e) {
                    out.println("ERR id inválido");
                }
                return;
            }

            // Cualquier otro comando ADMIN se aplica a la partida
            // del jugador seleccionado (adminTargetPlayerId)
            if (adminTargetPlayerId == null) {
                out.println("ERR no hay jugador seleccionado (use ADMIN SELECT <id>)");
                return;
            }

            GameManager targetGame = PlayerRegistry.getGameManager(adminTargetPlayerId);
            if (targetGame == null) {
                out.println("ERR jugador no encontrado");
                return;
            }

            // Reenviamos la línea completa al GameManager de esa partida
            String res = targetGame.procesarComandoAdmin(line);
            out.println(res);
            return;
        }

        /* ============================
           3) INPUT del jugador
           Formato:
           INPUT 0 LEFT/RIGHT/UP/DOWN/JUMP/STOP
        ============================ */
        if (line.startsWith("INPUT") && game != null) {
            String[] parts = line.split("\\s+");
            if (parts.length >= 3) {
                String command = parts[2];
                game.handleInput(command);
            }
        }
    }

    /**
     * Envía actualizaciones de estado de la partida a este cliente.
     * Solo se usa si el cliente es de JUEGO, no admin.
     */
    @Override
    public void actualizar(Object mensaje) {
        if (!isGameClient) return;               // admin NO recibe frames

        if (mensaje instanceof String) {
            out.print((String) mensaje);
            out.flush();
        }
    }

    @Override
    public Integer getObserverId() {
        // Si es jugador, podemos usar su id; si no, -1
        return (session != null) ? session.id : -1;
    }
}


