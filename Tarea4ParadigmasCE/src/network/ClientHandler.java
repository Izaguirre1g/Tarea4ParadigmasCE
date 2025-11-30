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

    private Boolean isGameClient = false;   // true si es jugador, false si es admin
    private Integer adminTargetPlayerId = null; // jugador seleccionado en admin
    private Boolean isSpectator = false;

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
       2) ADMIN PLAYERS - ACCESO UNIVERSAL
       Permite que cualquier cliente pida la lista
        ============================ */
        if (line.equals("ADMIN PLAYERS")) {
            String json = PlayerRegistry.getPlayersJson();
            out.println(json);
            System.out.println("[SERVER] Enviando lista de jugadores a cliente: " + json);
            return;
        }

        /* ============================
           2) Cliente ADMIN
           Cualquier línea que empiece con ADMIN
           (admin_client en C) entra aquí.
        ============================ */
        if (line.startsWith("ADMIN")) {
            isGameClient = false;

            String[] p = line.split("\\s+");
            if (p.length < 2) {
                out.println("ERR comando ADMIN incompleto");
                return;
            }

            String cmd = p[1].toUpperCase();

            // ========================================
            // COMANDO: ADMIN PLAYERS
            // ========================================
            if ("PLAYERS".equals(cmd)) {
                String json = PlayerRegistry.getPlayersJson();
                out.println(json);
                System.out.println("[SERVER] Enviando lista de jugadores: " + json);
                return;
            }

            // ========================================
            // COMANDO: ADMIN SELECT <playerId>
            // ========================================
            if ("SELECT".equals(cmd)) {
                if (p.length < 3) {
                    out.println("ERR falta id jugador");
                    return;
                }
                try {
                    adminTargetPlayerId = Integer.parseInt(p[2]);
                    out.println("OK admin seleccionado jugador " + adminTargetPlayerId);
                    System.out.println("[SERVER] Admin seleccionó jugador ID: " + adminTargetPlayerId);
                } catch (NumberFormatException e) {
                    out.println("ERR id inválido");
                }
                return;
            }

            // ========================================
            // Para los siguientes comandos, necesitamos un jugador seleccionado
            // ========================================
            if (adminTargetPlayerId == null) {
                out.println("ERR no hay jugador seleccionado (use ADMIN SELECT <id>)");
                return;
            }

            GameManager targetGame = PlayerRegistry.getGameManager(adminTargetPlayerId);
            if (targetGame == null) {
                out.println("ERR jugador no encontrado");
                return;
            }

            // ========================================
            // COMANDO: ADMIN CROC <TIPO> <LIANA> <ALTURA>
            // ========================================
            if ("CROC".equals(cmd)) {
                if (p.length < 5) {
                    out.println("ERR formato: ADMIN CROC <TIPO> <LIANA> <ALTURA>");
                    return;
                }

                try {
                    String tipo = p[2].toUpperCase();     // ROJO o AZUL
                    int liana = Integer.parseInt(p[3]);   // 0-8 (del cliente)
                    int altura = Integer.parseInt(p[4]);  // 0-540

                    // Validar parámetros
                    if (liana < 0 || liana > 8) {
                        out.println("ERR liana debe estar entre 0-8");
                        return;
                    }

                    if (altura < 0 || altura > 540) {
                        out.println("ERR altura debe estar entre 0-540");
                        return;
                    }

                    if (!tipo.equals("ROJO") && !tipo.equals("AZUL")) {
                        out.println("ERR tipo debe ser ROJO o AZUL");
                        return;
                    }

                    // Convertir índice 0-8 a 1-9 para GameManager
                    int lianaNum = liana + 1;

                    // Crear cocodrilo en la posición especificada
                    targetGame.crearCocodriloAdmin(tipo, lianaNum, altura);
                    out.println("OK cocodrilo " + tipo + " creado en liana " + liana + " altura " + altura);
                    System.out.println("[ADMIN CMD] → ADMIN CROC " + tipo + " liana:" + liana + " altura:" + altura);

                } catch (NumberFormatException e) {
                    out.println("ERR parámetros numéricos inválidos");
                }
                return;
            }

            // ========================================
            // COMANDO: ADMIN FRUIT <TIPO> <LIANA> <ALTURA> <PUNTOS>
            // ========================================
            if ("FRUIT".equals(cmd)) {
                if (p.length < 6) {
                    out.println("ERR formato: ADMIN FRUIT <TIPO> <LIANA> <ALTURA> <PUNTOS>");
                    return;
                }

                try {
                    String tipo = p[2].toUpperCase();     // BANANA, NARANJA, CEREZA
                    int liana = Integer.parseInt(p[3]);   // 0-8 (del cliente)
                    int altura = Integer.parseInt(p[4]);  // 0-540
                    int puntos = Integer.parseInt(p[5]);  // 10-100

                    // Validar parámetros
                    if (liana < 0 || liana > 8) {
                        out.println("ERR liana debe estar entre 0-8");
                        return;
                    }

                    if (altura < 0 || altura > 540) {
                        out.println("ERR altura debe estar entre 0-540");
                        return;
                    }

                    if (puntos < 10 || puntos > 100) {
                        out.println("ERR puntos deben estar entre 10-100");
                        return;
                    }

                    if (!tipo.equals("BANANA") && !tipo.equals("NARANJA") && !tipo.equals("CEREZA")) {
                        out.println("ERR tipo debe ser BANANA, NARANJA o CEREZA");
                        return;
                    }

                    // Convertir índice 0-8 a 1-9 para GameManager
                    int lianaNum = liana + 1;

                    // Crear fruta
                    targetGame.crearFrutaAdmin(tipo, lianaNum, altura, puntos);
                    out.println("OK fruta " + tipo + " creada en liana " + liana +
                            " altura " + altura + " con " + puntos + " puntos");
                    System.out.println("[ADMIN CMD] → ADMIN FRUIT " + tipo + " liana:" + liana +
                            " " + altura + " " + puntos);

                } catch (NumberFormatException e) {
                    out.println("ERR parámetros numéricos inválidos");
                }
                return;
            }

            // ========================================
            // COMANDO: ADMIN DELFRUIT <LIANA> <ALTURA>
            // ========================================
            if ("DELFRUIT".equals(cmd)) {
                if (p.length < 4) {
                    out.println("ERR formato: ADMIN DELFRUIT <LIANA> <ALTURA>");
                    return;
                }

                try {
                    int liana = Integer.parseInt(p[2]);   // 0-8
                    int altura = Integer.parseInt(p[3]);  // 0-540

                    // Validar parámetros
                    if (liana < 0 || liana > 8) {
                        out.println("ERR liana debe estar entre 0-8");
                        return;
                    }

                    if (altura < 0 || altura > 540) {
                        out.println("ERR altura debe estar entre 0-540");
                        return;
                    }

                    // Eliminar fruta
                    boolean eliminada = targetGame.eliminarFrutaAdmin(liana, altura);

                    if (eliminada) {
                        out.println("OK fruta eliminada en liana " + liana + " altura " + altura);
                        System.out.println("[ADMIN CMD] → ADMIN DELFRUIT " + liana + " " + altura);
                    } else {
                        out.println("ERR no se encontró fruta en esa posición");
                    }

                } catch (NumberFormatException e) {
                    out.println("ERR parámetros numéricos inválidos");
                }
                return;
            }

            // Si llegamos aquí, comando desconocido
            out.println("ERR comando ADMIN desconocido: " + cmd);
            return;
        }

        /* ============================
       4) INPUT del jugador
       Formato:
       INPUT 0 LEFT/RIGHT/UP/DOWN/JUMP/STOP
        ============================ */
        if (line.startsWith("INPUT")) {

            // Los espectadores NO pueden enviar INPUT
            if (isSpectator) {
                out.println("ERR espectadores no pueden controlar el juego");
                return;
            }

            if (game != null) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String command = parts[2];
                    game.handleInput(command);
                }
            }
            return;
        }

        /* ============================
       3) Cliente ESPECTADOR (SPECTATE)
       Formato esperado:
       SPECTATE <playerId>
        ============================ */
        if (line.startsWith("SPECTATE")) {

            isGameClient = false;

            String[] p = line.split("\\s+");
            if (p.length < 2) {
                out.println("ERR comando SPECTATE incompleto");
                return;
            }

            try {
                Integer targetPlayerId = Integer.parseInt(p[1]);
                GameManager targetGame = PlayerRegistry.getGameManager(targetPlayerId);

                if (targetGame == null) {
                    out.println("ERR jugador no encontrado");
                    return;
                }

                // Verificar límite de 2 espectadores
                int spectatorCount = targetGame.getSpectatorCount();
                if (spectatorCount >= 2) {
                    out.println("ERR jugador ya tiene 2 espectadores");
                    return;
                }

                this.game = targetGame;
                targetGame.addObserver(this);
                this.isGameClient = true;
                this.isSpectator = true;

                System.out.println("[SERVER] Cliente ESPECTADOR de jugador " + targetPlayerId);
                out.println("OK observando jugador " + targetPlayerId);

            } catch (NumberFormatException e) {
                out.println("ERR playerId inválido");
            }

            return;
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


