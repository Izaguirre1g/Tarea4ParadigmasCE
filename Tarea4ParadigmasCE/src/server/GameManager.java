package server;

import entities.*;
import model.GameState;
import model.Liana;
import model.Posicion;
import patterns.factory.GameObjectFactory;
import patterns.observer.GameObservable;
import utils.GameStateSerializer;
import utils.TipoFruta;

import java.util.concurrent.*;

import static utils.GameConstants.*;

/**
 * GameManager
 * -----------------------------------------------------
 * Gestiona toda la lógica del juego usando GameState encapsulado.
 */
public class GameManager {

    // Estado del juego encapsulado
    private final GameState state = new GameState();

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();
    private final GameObjectFactory factory = new GameObjectFactory();
    private final GameObservable observable = new GameObservable();

    // Modo de comunicación
    public enum CommunicationMode {
        TEXT,   // Protocolo de texto actual
        JSON    // Protocolo JSON
    }

    private CommunicationMode mode = CommunicationMode.JSON; // Por defecto usar JSON

    public GameManager() {
        initLevel();
        executor.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS.longValue(), TimeUnit.MILLISECONDS);
    }

    /* --- Inicialización del nivel --- */
    private void initLevel() {
        state.getLianas().clear();
        state.getFrutas().clear();
        state.getCocodrilos().clear();

        // Crear lianas
        for (Integer i = 0; i < LIANAS.length; i++) {
            Double[] l = LIANAS[i];
            state.getLianas().add(new Liana(i, new Posicion(l[0], l[1]), new Posicion(l[2], l[3])));
        }

        // Crear algunos cocodrilos de ejemplo
        state.getCocodrilos().add(new CocodriloRojo(new Posicion(160.0, 300.0)));
        state.getCocodrilos().add(new CocodriloAzul(new Posicion(400.0, 200.0)));

        // Crear frutas
        state.getFrutas().add(factory.crearFruta(TipoFruta.BANANA, new Posicion(240.0, 250.0)));
        state.getFrutas().add(factory.crearFruta(TipoFruta.CEREZA, new Posicion(640.0, 350.0)));

        // Inicializar posición del jugador
        state.setPlayerX(PLAYER_START_X);
        state.setPlayerY(PLAYER_START_Y);
        state.setVelocityX(0.0);
        state.setVelocityY(0.0);
        state.setLives(PLAYER_START_LIVES);
        state.setScore(0);
        state.setJumping(Boolean.FALSE);
        state.setOnLiana(Boolean.FALSE);
    }

    /* --- Loop principal del juego --- */
    private void tick() {
        updatePlayer();
        updateCrocs();
        checkCollisions();
        broadcast();
    }

    /* --- Entrada del jugador --- */
    public void handleInput(String input) {
        switch (input.toUpperCase()) {
            case "LEFT":
                state.setVelocityX(-PLAYER_SPEED_X);
                break;
            case "RIGHT":
                state.setVelocityX(PLAYER_SPEED_X);
                break;
            case "UP":
                if (state.isOnLiana()) state.setVelocityY(-PLAYER_SPEED_Y);
                break;
            case "DOWN":
                if (state.isOnLiana()) state.setVelocityY(PLAYER_SPEED_Y);
                break;
            case "JUMP":
                if (!state.isJumping() && state.isOnLiana()) {
                    state.setJumping(Boolean.TRUE);
                    state.setVelocityY(-PLAYER_JUMP_VELOCITY);
                }
                break;
            case "STOP":
                state.setVelocityX(0.0);
                if (state.isOnLiana()) state.setVelocityY(0.0);
                break;
        }
    }

    /* --- Actualización del jugador --- */
    private void updatePlayer() {
        Boolean estabaEnLiana = state.isOnLiana();

        // Aplicar movimiento horizontal
        state.setPlayerX(state.getPlayerX() + state.getVelocityX());
        state.setPlayerX(clamp(state.getPlayerX(), MIN_X, MAX_X));

        // Verificar si está en una liana
        state.setOnLiana(Boolean.FALSE);
        for (Liana liana : state.getLianas()) {
            Double lianaX = liana.getPosicionInicio().x;
            Double dx = Math.abs(lianaX - (state.getPlayerX() + PLAYER_WIDTH / 2.0));

            if (dx < 15.0 &&
                    state.getPlayerY() + PLAYER_HEIGHT > liana.getPosicionInicio().y &&
                    state.getPlayerY() < liana.getPosicionFin().y) {
                state.setOnLiana(Boolean.TRUE);
                break;
            }
        }

        // Física vertical
        if (state.isOnLiana()) {
            // En liana: control manual
            state.setPlayerY(state.getPlayerY() + state.getVelocityY());
            state.setJumping(Boolean.FALSE);
        } else {
            // En el aire: gravedad
            if (state.isJumping() || !isOnPlatform()) {
                state.setVelocityY(state.getVelocityY() + GRAVITY);
                if (state.getVelocityY() > MAX_FALL_SPEED) {
                    state.setVelocityY(MAX_FALL_SPEED);
                }
                state.setPlayerY(state.getPlayerY() + state.getVelocityY());
            } else {
                state.setVelocityY(0.0);
                state.setJumping(Boolean.FALSE);
            }
        }

        // Limitar Y
        state.setPlayerY(clamp(state.getPlayerY(), MIN_Y, MAX_Y));

        // Ajustar velocidad en liana
        if (state.isOnLiana()) {
            state.setVelocityX(state.getVelocityX() * 0.5);
        }
        if (!state.isOnLiana() && estabaEnLiana) {
            state.setVelocityX(0.0);
        }
    }

    /* --- Verificar si está sobre una plataforma --- */
    private Boolean isOnPlatform() {
        for (Double[] plat : PLATFORMS) {
            Double px = plat[0];
            Double py = plat[1];
            Double pw = plat[2];
            Double ph = plat[3];

            if (state.getPlayerX() + PLAYER_WIDTH > px &&
                    state.getPlayerX() < px + pw &&
                    Math.abs((state.getPlayerY() + PLAYER_HEIGHT) - py) < 5.0) {
                state.setPlayerY(py - PLAYER_HEIGHT);
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /* --- Actualizar cocodrilos --- */
    private void updateCrocs() {
        for (Cocodrilo c : state.getCocodrilos()) {
            c.update();
        }
    }

    /* --- Verificar colisiones --- */
    private void checkCollisions() {
        checkFruits();
        checkCrocs();
    }

    /* --- Colisión con frutas --- */
    private void checkFruits() {
        for (Fruta f : state.getFrutas()) {
            if (!f.isActiva()) continue;

            Double dx = Math.abs(f.getPosicion().x - state.getPlayerX());
            Double dy = Math.abs(f.getPosicion().y - state.getPlayerY());

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                f.setActiva(Boolean.FALSE);
                state.setScore(state.getScore() + f.getPuntos());
                System.out.println("🍒 Fruta " + f.getTipo().getNombre() + " recogida! +" + f.getPuntos() + " pts");
            }
        }
    }

    /* --- Colisión con cocodrilos --- */
    private void checkCrocs() {
        for (Cocodrilo c : state.getCocodrilos()) {
            if (!c.isActivo()) continue;

            Double dx = Math.abs(c.getPosicion().x - state.getPlayerX());
            Double dy = Math.abs(c.getPosicion().y - state.getPlayerY());

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                System.out.println("🐊 ¡Cocodrilo te atrapó!");
                playerDeath();
                return;
            }
        }
    }

    /* --- Reinicio al morir --- */
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
        state.setJumping(Boolean.FALSE);
    }

    /* --- Compilar estado para los clientes --- */
    private String buildGameState() {
        if (mode == CommunicationMode.JSON) {
            return buildGameStateJSON();
        } else {
            return buildGameStateText();
        }
    }

    /**
     * Genera el estado del juego en formato JSON.
     */
    private String buildGameStateJSON() {
        return GameStateSerializer.toJson(state);
    }

    /**
     * Genera el estado del juego en formato de texto (protocolo original).
     */
    private String buildGameStateText() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("PLAYER 0 x=%.0f y=%.0f lives=%d score=%d\n",
                state.getPlayerX(), state.getPlayerY(), state.getLives(), state.getScore()));

        for (Cocodrilo c : state.getCocodrilos())
            sb.append(c.toNetworkString()).append("\n");

        for (Fruta f : state.getFrutas())
            if (f.isActiva()) sb.append(f.toNetworkString()).append("\n");

        return sb.toString();
    }

    /**
     * Cambia el modo de comunicación.
     */
    public void setCommunicationMode(CommunicationMode mode) {
        this.mode = mode;
        System.out.println("[GameManager] Modo de comunicación cambiado a: " + mode);
    }

    /* --- Broadcast a observadores --- */
    public void addObserver(patterns.observer.Observer obs) {
        observable.agregarObservador(obs);
    }

    public void removeObserver(patterns.observer.Observer obs) {
        observable.eliminarObservador(obs);
    }

    private void broadcast() {
        observable.notificarObservadores(buildGameState());
    }

/* ================================================================
       MÉTODOS DE GESTIÓN ADMINISTRATIVA (Crear/Eliminar Entidades)
       ================================================================ */

    /**
     * Crea un cocodrilo del tipo especificado en una liana.
     */
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
            return Boolean.FALSE;
        }

        Double x = lianaSeleccionada.getPosicionInicio().x;
        Double y = (lianaSeleccionada.getPosicionInicio().y + lianaSeleccionada.getPosicionFin().y) / 2.0;
        Posicion pos = new Posicion(x, y);

        Cocodrilo nuevo = null;
        if (tipo == utils.TipoCocodrilo.ROJO) {
            nuevo = new CocodriloRojo(pos);
        } else if (tipo == utils.TipoCocodrilo.AZUL) {
            nuevo = new CocodriloAzul(pos);
        }

        if (nuevo != null) {
            state.getCocodrilos().add(nuevo);
            System.out.println("[GameManager] Cocodrilo creado → ID: " + nuevo.getId() +
                    ", Tipo: " + tipo + ", Liana: " + lianaId);
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * Elimina un cocodrilo por su ID.
     */
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
            return Boolean.FALSE;
        }

        state.getCocodrilos().remove(crocAEliminar);
        System.out.println("[GameManager] Cocodrilo eliminado → ID: " + cocodriloId);
        return Boolean.TRUE;
    }

    /**
     * Crea una fruta del tipo especificado en una liana a cierta altura.
     */
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
            return Boolean.FALSE;
        }

        Double minY = lianaSeleccionada.getPosicionInicio().y;
        Double maxY = lianaSeleccionada.getPosicionFin().y;

        if (altura < minY || altura > maxY) {
            System.out.println("[Error] Altura fuera del rango de la liana. Rango: [" + minY + ", " + maxY + "]");
            return Boolean.FALSE;
        }

        Double x = lianaSeleccionada.getPosicionInicio().x;
        Posicion pos = new Posicion(x, altura);

        Fruta nueva = factory.crearFruta(tipo, pos);
        nueva.setLiana(lianaSeleccionada);

        state.getFrutas().add(nueva);
        System.out.println("[GameManager] Fruta creada → ID: " + nueva.getId() +
                ", Tipo: " + tipo + ", Liana: " + lianaId + ", Altura: " + altura);
        return Boolean.TRUE;
    }

    /**
     * Elimina una fruta por su posición (liana + altura).
     */
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
            System.out.println("[Error] Fruta no encontrada en Liana: " + lianaId + ", Altura: " + altura);
            return Boolean.FALSE;
        }

        state.getFrutas().remove(frutaAEliminar);
        System.out.println("[GameManager] Fruta eliminada → ID: " + frutaAEliminar.getId());
        return Boolean.TRUE;
    }

    /**
     * Elimina una fruta directamente por su ID.
     */
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
            return Boolean.FALSE;
        }

        state.getFrutas().remove(frutaAEliminar);
        System.out.println("[GameManager] Fruta eliminada → ID: " + frutaId);
        return Boolean.TRUE;
    }

    /**
     * Lista todas las entidades activas en el juego.
     */
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

    /**
     * Obtiene el estado actual del juego (para serialización futura).
     */
    public GameState getState() {
        return state;
    }
}
