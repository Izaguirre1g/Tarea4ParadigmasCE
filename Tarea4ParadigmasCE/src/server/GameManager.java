package server;

import entities.Cocodrilo;
import entities.Fruta;
import model.GameState;
import model.Liana;
import model.Posicion;
import patterns.factory.GameObjectFactory;
import patterns.factory.GameObjectFactoryImpl;
import patterns.observer.GameObservable;
import utils.GameStateSerializer;
import utils.TipoFruta;

import java.util.Map;
import java.util.concurrent.*;

import static utils.GameConstants.*;

/**
 * GameManager
 * -----------------------------------------------------
 * Gestiona toda la lógica del juego usando GameState encapsulado
 * (un solo estado global por ahora).
 *
 * Además expone métodos administrativos para:
 *  - crear / eliminar cocodrilos
 *  - crear / eliminar frutas
 *  - listar entidades
 *  - cambiar modo de comunicación (TEXT / JSON)
 *  - registrar jugadores conectados (para la UI admin)
 */
public class GameManager {

    // Estado del juego encapsulado
    private final GameState state = new GameState();

    // Ejecuta el loop del juego cada TICK_RATE_MS
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    // Factory para crear entidades
    private final GameObjectFactory factory = new GameObjectFactoryImpl();

    // Observable para notificar a los ClientHandler
    private final GameObservable observable = new GameObservable();

    // Jugadores conectados (para la lista en el panel admin)
    // idCliente -> nombre
    private final Map<Integer, String> connectedPlayers = new ConcurrentHashMap<>();

    // Modo de comunicación con los clientes
    public enum CommunicationMode {
        TEXT,   // Protocolo de texto actual
        JSON    // Protocolo JSON
    }

    private CommunicationMode mode = CommunicationMode.TEXT;

    public GameManager() {
        initLevel();
        executor.scheduleAtFixedRate(this::tick, 0,
                TICK_RATE_MS.longValue(), TimeUnit.MILLISECONDS);
    }

    /* =========================================================
       INICIALIZACIÓN
       ========================================================= */

    private void initLevel() {
        state.getLianas().clear();
        state.getFrutas().clear();
        state.getCocodrilos().clear();

        // Crear lianas
        for (int i = 0; i < LIANAS.length; i++) {
            Double[] l = LIANAS[i];
            state.getLianas().add(
                    new Liana(i,
                            new Posicion(l[0], l[1]),
                            new Posicion(l[2], l[3])));
        }

        // Cocodrilos iniciales de ejemplo
        state.getCocodrilos().add(
                factory.crearCocodrilo(utils.TipoCocodrilo.ROJO,
                        new Posicion(160.0, 300.0)));
        state.getCocodrilos().add(
                factory.crearCocodrilo(utils.TipoCocodrilo.AZUL,
                        new Posicion(400.0, 200.0)));

        // Frutas iniciales
        state.getFrutas().add(
                factory.crearFruta(TipoFruta.BANANA,
                        new Posicion(240.0, 250.0)));
        state.getFrutas().add(
                factory.crearFruta(TipoFruta.CEREZA,
                        new Posicion(640.0, 350.0)));

        // Jugador
        state.setPlayerX(PLAYER_START_X);
        state.setPlayerY(PLAYER_START_Y);
        state.setVelocityX(0.0);
        state.setVelocityY(0.0);
        state.setLives(PLAYER_START_LIVES);
        state.setScore(0);
        state.setJumping(false);
        state.setOnLiana(false);
    }

    /* =========================================================
       LOOP PRINCIPAL DEL JUEGO
       ========================================================= */

    private void tick() {
        updatePlayer();
        updateCrocs();
        checkCollisions();
        broadcast();
    }

    /* =========================================================
       ENTRADA DEL JUGADOR (desde ClientHandler)
       ========================================================= */

    public void handleInput(String input) {
        switch (input.toUpperCase()) {
            case "LEFT"  -> state.setVelocityX(-PLAYER_SPEED_X);
            case "RIGHT" -> state.setVelocityX(PLAYER_SPEED_X);
            case "UP" -> {
                if (state.isOnLiana()) state.setVelocityY(-PLAYER_SPEED_Y);
            }
            case "DOWN" -> {
                if (state.isOnLiana()) state.setVelocityY(PLAYER_SPEED_Y);
            }
            case "JUMP" -> {
                if (!state.isOnLiana() && !state.isJumping()) {
                    state.setJumping(true);
                    state.setVelocityY(-PLAYER_JUMP_VELOCITY);
                }
            }
            case "STOP" -> {
                state.setVelocityX(0.0);
                if (state.isOnLiana()) state.setVelocityY(0.0);
            }
        }
    }

    /* =========================================================
       ACTUALIZACIÓN DEL JUGADOR
       ========================================================= */

    private void updatePlayer() {

        // --- Movimiento horizontal ---
        double newX = state.getPlayerX() + state.getVelocityX();

        // Suavizado cuando está en liana
        if (state.isOnLiana()) {
            newX = state.getPlayerX() + (state.getVelocityX() * 0.35);
        }

        state.setPlayerX(clamp(newX, MIN_X, MAX_X));

        // --- Detectar si está en una liana ---
        state.setOnLiana(false);

        for (Liana liana : state.getLianas()) {
            double lx = liana.getPosicionInicio().x;
            double dx = Math.abs(lx - (state.getPlayerX() + PLAYER_WIDTH / 2.0));

            if (dx < 15.0 &&
                    state.getPlayerY() + PLAYER_HEIGHT > liana.getPosicionInicio().y &&
                    state.getPlayerY() < liana.getPosicionFin().y) {
                state.setOnLiana(true);
                break;
            }
        }

        // --- Movimiento vertical ---
        if (state.isOnLiana()) {
            // En liana → sin salto
            state.setJumping(false);
            state.setVelocityY(state.getVelocityY() * 0.7);

            double newY = state.getPlayerY() + state.getVelocityY();
            state.setPlayerY(clamp(newY, MIN_Y, MAX_Y));
            return;
        }

        // Sobre plataforma
        if (isOnPlatform()) {
            state.setVelocityY(0.0);
            state.setJumping(false);
            return;
        }

        // En el aire: gravedad + anti-tunneling
        double vy = state.getVelocityY() + GRAVITY;
        if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;

        double nextY = state.getPlayerY() + vy;

        Double landingY = getPlatformLandingY(
                state.getPlayerX(),
                state.getPlayerY(),
                nextY);

        if (landingY != null) {
            state.setPlayerY(landingY - PLAYER_HEIGHT);
            state.setVelocityY(0.0);
            state.setJumping(false);
        } else {
            state.setPlayerY(nextY);
            state.setVelocityY(vy);
        }

        state.setPlayerY(clamp(state.getPlayerY(), MIN_Y, MAX_Y));
    }

    private Double getPlatformLandingY(double x, double yActual, double yNext) {
        for (Double[] plat : PLATFORMS) {
            double px = plat[0];
            double py = plat[1];
            double pw = plat[2];

            boolean dentroX = x + PLAYER_WIDTH > px && x < px + pw;
            if (!dentroX) continue;

            boolean caeSobre =
                    yActual + PLAYER_HEIGHT <= py &&
                            yNext + PLAYER_HEIGHT >= py;

            if (caeSobre) return py;
        }
        return null;
    }

    // ¿Está el jugador sobre alguna plataforma?
    private boolean isOnPlatform() {

        double nextY = state.getPlayerY() + state.getVelocityY();
        double playerBottomNext = nextY + PLAYER_HEIGHT;
        double currentBottom = state.getPlayerY() + PLAYER_HEIGHT;

        Double closestPlatformY = null;

        for (Double[] plat : PLATFORMS) {
            double px = plat[0];
            double py = plat[1];
            double pw = plat[2];

            boolean insideX =
                    (state.getPlayerX() + PLAYER_WIDTH > px) &&
                            (state.getPlayerX() < px + pw);

            if (!insideX) continue;

            if (py >= currentBottom) {
                if (closestPlatformY == null || py < closestPlatformY) {
                    closestPlatformY = py;
                }
            }
        }

        if (closestPlatformY == null) return false;

        if (state.getVelocityY() >= 0 &&
                currentBottom <= closestPlatformY &&
                playerBottomNext >= closestPlatformY) {
            state.setPlayerY(closestPlatformY - PLAYER_HEIGHT);
            return true;
        }

        return false;
    }

    /* =========================================================
       COCODRILOS / COLISIONES
       ========================================================= */

    private void updateCrocs() {
        for (Cocodrilo c : state.getCocodrilos()) {
            c.update();
        }
    }

    private void checkCollisions() {
        checkFruits();
        checkCrocs();
    }

    private void checkFruits() {
        for (Fruta f : state.getFrutas()) {
            if (!f.isActiva()) continue;

            double dx = Math.abs(f.getPosicion().x - state.getPlayerX());
            double dy = Math.abs(f.getPosicion().y - state.getPlayerY());

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                f.setActiva(false);
                state.setScore(state.getScore() + f.getPuntos());
                System.out.println("🍒 Fruta " + f.getTipo().getNombre()
                        + " recogida! +" + f.getPuntos() + " pts");
            }
        }
    }

    private void checkCrocs() {
        for (Cocodrilo c : state.getCocodrilos()) {
            if (!c.isActivo()) continue;

            double dx = Math.abs(c.getPosicion().x - state.getPlayerX());
            double dy = Math.abs(c.getPosicion().y - state.getPlayerY());

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                System.out.println("🐊 ¡Cocodrilo te atrapó!");
                playerDeath();
                return;
            }
        }
    }

    private void playerDeath() {
        state.setLives(state.getLives() - 1);
        if (state.getLives() <= 0) {
            state.setLives(PLAYER_START_LIVES);
            state.setScore(0);
            initLevel();
        }
        state.setPlayerX(PLAYER_START_X);
        state.setPlayerY(PLAYER_START_Y);
        state.setVelocityX(0.0);
        state.setVelocityY(0.0);
        state.setJumping(false);
    }

    /* =========================================================
       SERIALIZACIÓN / BROADCAST
       ========================================================= */

    private String buildGameState() {
        if (mode == CommunicationMode.JSON) {
            return GameStateSerializer.toJson(state);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                    "PLAYER 0 x=%.0f y=%.0f lives=%d score=%d\n",
                    state.getPlayerX(), state.getPlayerY(),
                    state.getLives(), state.getScore()));

            for (Cocodrilo c : state.getCocodrilos())
                sb.append(c.toNetworkString()).append("\n");

            for (Fruta f : state.getFrutas())
                if (f.isActiva())
                    sb.append(f.toNetworkString()).append("\n");

            return sb.toString();
        }
    }

    public void setCommunicationMode(CommunicationMode mode) {
        this.mode = mode;
        System.out.println("[GameManager] Modo de comunicación cambiado a: " + mode);
    }

    public void addObserver(patterns.observer.Observer obs) {
        observable.agregarObservador(obs);
    }

    public void removeObserver(patterns.observer.Observer obs) {
        observable.eliminarObservador(obs);
    }

    private void broadcast() {
        observable.notificarObservadores(buildGameState());
    }

    /* =========================================================
       MÉTODOS ADMIN (crear / eliminar entidades)
       ========================================================= */

    // Versión sencilla: usa el centro de la liana
    public Boolean crearCocodrilo(utils.TipoCocodrilo tipo, Integer lianaId) {
        Liana lianaSeleccionada = null;
        for (Liana l : state.getLianas()) {
            if (l.getId().equals(lianaId)) {
                lianaSeleccionada = l;
                break;
            }
        }

        if (lianaSeleccionada == null) {
            System.out.println("[Error] Liana no encontrada con ID: " + lianaId);
            return false;
        }

        double y = (lianaSeleccionada.getPosicionInicio().y +
                lianaSeleccionada.getPosicionFin().y) / 2.0;

        return crearCocodrilo(tipo, lianaId, y);
    }

    // Versión completa: tipo + liana + altura específica (para ADMIN C)
    public Boolean crearCocodrilo(utils.TipoCocodrilo tipo,
                                  Integer lianaId,
                                  Double altura) {

        Liana lianaSeleccionada = null;
        for (Liana l : state.getLianas()) {
            if (l.getId().equals(lianaId)) {
                lianaSeleccionada = l;
                break;
            }
        }

        if (lianaSeleccionada == null) {
            System.out.println("[Error] Liana no encontrada con ID: " + lianaId);
            return false;
        }

        try {
            Posicion pos = new Posicion(
                    lianaSeleccionada.getPosicionInicio().x,
                    altura
            );

            Cocodrilo nuevo = factory.crearCocodrilo(tipo, pos);
            state.getCocodrilos().add(nuevo);

            System.out.println("[GameManager] Cocodrilo creado → ID: "
                    + nuevo.getId() + ", Tipo: " + tipo +
                    ", Liana: " + lianaId + ", Altura: " + altura);

            return true;

        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
            return false;
        }
    }

    public Boolean eliminarCocodrilo(Integer cocodriloId) {
        Cocodrilo crocAEliminar = null;
        for (Cocodrilo c : state.getCocodrilos()) {
            if (c.getId().equals(cocodriloId)) {
                crocAEliminar = c;
                break;
            }
        }

        if (crocAEliminar == null) {
            System.out.println("[Error] Cocodrilo no encontrado con ID: " + cocodriloId);
            return false;
        }

        state.getCocodrilos().remove(crocAEliminar);
        System.out.println("[GameManager] Cocodrilo eliminado → ID: " + cocodriloId);
        return true;
    }

    public Boolean crearFruta(TipoFruta tipo, Integer lianaId, Double altura) {
        Liana lianaSeleccionada = null;
        for (Liana l : state.getLianas()) {
            if (l.getId().equals(lianaId)) {
                lianaSeleccionada = l;
                break;
            }
        }

        if (lianaSeleccionada == null) {
            System.out.println("[Error] Liana no encontrada con ID: " + lianaId);
            return false;
        }

        try {
            Fruta nueva = factory.crearFrutaEnLiana(tipo, lianaSeleccionada, altura);
            state.getFrutas().add(nueva);
            System.out.println("[GameManager] Fruta creada → ID: " + nueva.getId() +
                    ", Tipo: " + tipo + ", Liana: " + lianaId +
                    ", Altura: " + altura);
            return true;
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
            return false;
        }
    }

    public Boolean eliminarFruta(Integer lianaId, Double altura) {
        Fruta frutaAEliminar = null;

        for (Fruta f : state.getFrutas()) {
            if (f.getLiana() != null &&
                    f.getLiana().getId().equals(lianaId) &&
                    Math.abs(f.getPosicion().y - altura) < 10.0) {
                frutaAEliminar = f;
                break;
            }
        }

        if (frutaAEliminar == null) {
            System.out.println("[Error] Fruta no encontrada en Liana: " +
                    lianaId + ", Altura: " + altura);
            return false;
        }

        state.getFrutas().remove(frutaAEliminar);
        System.out.println("[GameManager] Fruta eliminada → ID: " + frutaAEliminar.getId());
        return true;
    }

    public Boolean eliminarFrutaPorId(Integer frutaId) {
        Fruta frutaAEliminar = null;

        for (Fruta f : state.getFrutas()) {
            if (f.getId().equals(frutaId)) {
                frutaAEliminar = f;
                break;
            }
        }

        if (frutaAEliminar == null) {
            System.out.println("[Error] Fruta no encontrada con ID: " + frutaId);
            return false;
        }

        state.getFrutas().remove(frutaAEliminar);
        System.out.println("[GameManager] Fruta eliminada → ID: " + frutaId);
        return true;
    }

    public void listarEntidades() {
        System.out.println("\n=== ENTIDADES ACTIVAS ===");

        System.out.println("-- Cocodrilos (" + state.getCocodrilos().size() + ") --");
        for (Cocodrilo c : state.getCocodrilos()) {
            System.out.println("  " + c);
        }

        System.out.println("-- Frutas (" + state.getFrutas().size() + ") --");
        for (Fruta f : state.getFrutas()) {
            if (f.isActiva()) {
                System.out.println("  " + f);
            }
        }

        System.out.println("-- Lianas (" + state.getLianas().size() + ") --");
        for (Liana l : state.getLianas()) {
            System.out.println("  " + l);
        }

        System.out.println("=========================\n");
    }

    public GameState getState() {
        return state;
    }

    /* =========================================================
       COMANDOS ADMIN (desde cliente C)
       ========================================================= */

    public String procesarComandoAdmin(String cmd) {
        System.out.println("[ADMIN CMD] → " + cmd);

        String[] p = cmd.split("\\s+");
        if (p.length == 0) return "ERR comando vacío";

        if (!p[0].equalsIgnoreCase("ADMIN"))
            return "ERR comando desconocido";

        try {
            // ADMIN CROC ROJO <lianaId> <altura>
            if (p[1].equalsIgnoreCase("CROC")) {
                utils.TipoCocodrilo tipo =
                        p[2].equalsIgnoreCase("ROJO") ? utils.TipoCocodrilo.ROJO :
                                p[2].equalsIgnoreCase("AZUL") ? utils.TipoCocodrilo.AZUL : null;

                if (tipo == null) return "ERR tipo cocodrilo inválido";

                Integer liana = Integer.parseInt(p[3]);
                Double altura = Double.parseDouble(p[4]);

                Boolean ok = crearCocodrilo(tipo, liana, altura);
                return ok ? "OK cocodrilo creado" : "ERR no se pudo crear";
            }

            // ADMIN FRUTA BANANA <lianaId> <altura>
            if (p[1].equalsIgnoreCase("FRUTA")) {
                TipoFruta tipo =
                        p[2].equalsIgnoreCase("BANANA") ? TipoFruta.BANANA :
                                p[2].equalsIgnoreCase("NARANJA") ? TipoFruta.NARANJA :
                                        p[2].equalsIgnoreCase("CEREZA") ? TipoFruta.CEREZA : null;

                if (tipo == null) return "ERR tipo fruta inválida";

                Integer liana = Integer.parseInt(p[3]);
                Double altura = Double.parseDouble(p[4]);

                Boolean ok = crearFruta(tipo, liana, altura);
                return ok ? "OK fruta creada" : "ERR no se pudo crear";
            }

            // ADMIN DEL_ID <id>
            if (p[1].equalsIgnoreCase("DEL_ID")) {
                Integer id = Integer.parseInt(p[2]);

                boolean eliminado = eliminarCocodrilo(id);
                if (!eliminado) eliminado = eliminarFrutaPorId(id);

                return eliminado ? "OK eliminado" : "ERR id no encontrado";
            }

            // ADMIN LIST
            if (p[1].equalsIgnoreCase("LIST")) {
                listarEntidades();
                return "OK listado en consola";
            }

            // ADMIN PLAYERS se maneja en ClientHandler llamando a getConnectedPlayersJson()

            return "ERR comando ADMIN desconocido";

        } catch (Exception e) {
            return "ERR excepción: " + e.getMessage();
        }
    }

    /* =========================================================
       REGISTRO DE JUGADORES (para la UI admin)
       ========================================================= */

    public void registerPlayer(int clientId, String name) {
        connectedPlayers.put(clientId, name);
    }

    public void unregisterPlayer(int clientId) {
        connectedPlayers.remove(clientId);
    }

    /**
     * Devuelve la lista de jugadores conectados en formato JSON muy simple:
     *   [ {"id":0,"name":"DKJr"}, {"id":1,"name":"Otro"} ]
     */
    public String getConnectedPlayersJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Map.Entry<Integer, String> e : connectedPlayers.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"id\":").append(e.getKey())
                    .append(",\"name\":\"").append(e.getValue()).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Cuenta cuántos espectadores están observando esta partida.
     * Un espectador es un Observer que NO es el jugador dueño de la partida.
     *
     * @return Número de espectadores (máximo 2 permitido)
     */
    public Integer getSpectatorCount() {
        int totalObservers = observable.getObserverCount();
        return Math.max(0, totalObservers - 1);
    }
}

