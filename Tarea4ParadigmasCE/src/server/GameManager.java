package server;

import entities.*;
import model.*;
import network.ClientHandler;
import patterns.factory.GameObjectFactory;
import patterns.observer.GameObservable;
import utils.GameConstants;
import utils.TipoCocodrilo;
import utils.TipoFruta;

import java.util.*;
import java.util.concurrent.*;

import static utils.GameConstants.*;

/**
 * GameManager
 * -----------------------------------------------------
 * Coordina la lógica del juego DonCEy Kong Jr.
 * Usa los patrones Factory, Strategy y Observer.
 */
public class GameManager extends GameObservable {

    private final List<Cocodrilo> cocodrilos = new CopyOnWriteArrayList<>();
    private final List<Fruta> frutas = new CopyOnWriteArrayList<>();
    private final List<Liana> lianas = new ArrayList<>();

    private double playerX = PLAYER_START_X;
    private double playerY = PLAYER_START_Y;
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean jumping = false;
    private boolean onLiana = false;

    private int score = 0;
    private int lives = PLAYER_START_LIVES;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();
    private final GameObjectFactory factory = new GameObjectFactory();

    public GameManager() {
        initLevel();
        executor.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
    }

    /* --- Inicialización del nivel --- */
    private void initLevel() {
        lianas.clear();
        frutas.clear();
        cocodrilos.clear();

        // Crear lianas
        lianas.add(new Liana(new Posicion(160,120), new Posicion(160,525)));
        lianas.add(new Liana(new Posicion(240,120), new Posicion(240,525)));
        lianas.add(new Liana(new Posicion(400,220), new Posicion(400,525)));
        lianas.add(new Liana(new Posicion(480,220), new Posicion(480,525)));
        lianas.add(new Liana(new Posicion(640,120), new Posicion(640,525)));
        lianas.add(new Liana(new Posicion(720,120), new Posicion(720,525)));

        // Crear frutas
        frutas.add((Fruta) factory.create("fruit", TipoFruta.BANANA.name(), new Posicion(420,340)));
        frutas.add((Fruta) factory.create("fruit", TipoFruta.NARANJA.name(), new Posicion(660,240)));
        frutas.add((Fruta) factory.create("fruit", TipoFruta.CEREZA.name(), new Posicion(220,440)));

        // Crear cocodrilos
        cocodrilos.add((Cocodrilo) factory.create("crocodile", TipoCocodrilo.ROJO.name(), new Posicion(240,400)));
        cocodrilos.add((Cocodrilo) factory.create("crocodile", TipoCocodrilo.ROJO.name(), new Posicion(640,300)));
        cocodrilos.add((Cocodrilo) factory.create("crocodile", TipoCocodrilo.AZUL.name(), new Posicion(400,200)));

        System.out.println("Nivel inicializado con " + frutas.size() + " frutas y " + cocodrilos.size() + " cocodrilos.");
    }

    /* --- Procesamiento de entrada del jugador --- */
    public synchronized void processInput(String line) {
        if (line.contains("LEFT")) velocityX = -PLAYER_SPEED_X;
        else if (line.contains("RIGHT")) velocityX = PLAYER_SPEED_X;
        else if (line.contains("UP") && onLiana) velocityY = -PLAYER_SPEED_Y;
        else if (line.contains("DOWN") && onLiana) velocityY = PLAYER_SPEED_Y;
        else if (line.contains("STOP")) {
            velocityX = 0;
            if (onLiana) velocityY = 0;
        }
        if (line.contains("JUMP") && !jumping && !onLiana) {
            jumping = true;
            velocityY = -Math.abs(PLAYER_JUMP_VELOCITY);
        }
    }

    /* --- Bucle principal del juego --- */
    private synchronized void tick() {
        // Aplicar movimiento y gravedad
        playerX += velocityX;
        if (!onLiana) velocityY += GRAVITY;
        playerY += velocityY;

        // Colisión con suelo
        if (playerY + PLAYER_HEIGHT >= WIN_HEIGHT) {
            playerY = WIN_HEIGHT - PLAYER_HEIGHT;
            velocityY = 0;
            jumping = false;
        }

        // Colisión con plataformas
        for (double[] p : PLATFORMS) {
            double px = p[0], py = p[1], pw = p[2], ph = p[3];
            boolean dentroX = playerX + PLAYER_WIDTH > px && playerX < px + pw;
            boolean tocandoY = playerY + PLAYER_HEIGHT >= py && playerY + PLAYER_HEIGHT <= py + 10;

            if (dentroX && tocandoY && velocityY > 0) {
                playerY = py - PLAYER_HEIGHT;
                velocityY = 0;
                jumping = false;
                break;
            }
        }

        // Movimiento de cocodrilos
        for (Cocodrilo c : cocodrilos) c.update();

        // Verificar lianas, frutas y cocodrilos
        checkLianas();
        checkFruits();
        checkCrocs();

        // Condición de victoria (sube hasta arriba)
        if (playerY < 120 && velocityY < 0 && !jumping) {
            lives++;
            score += 500;
            System.out.println("🏁 Nivel superado! Reiniciando...");
            initLevel();
        }
        /*for (Cocodrilo c : cocodrilos)
            System.out.printf("[CROC] %s y=%.1f dir=%d vel=%.1f%n",
                c.getClass().getSimpleName(),
                c.getPosicion().y,
                c.getDireccion(),
                c.getVelocidad());*/
        // Notificar estado del juego
        notifyObservers(buildGameState());
    }

    /* --- Colisión con lianas --- */
    private void checkLianas() {
        boolean estabaEnLiana = onLiana;
        onLiana = false;

        for (Liana l : lianas) {
            double dx = Math.abs(l.getPosicionInicio().x - (playerX + PLAYER_WIDTH / 2.0));
            if (dx < 15 && playerY + PLAYER_HEIGHT > l.getPosicionInicio().y && playerY < l.getPosicionFin().y) {
                onLiana = true;
                break;
            }
        }

        if (onLiana) velocityX *= 0.5;
        if (!onLiana && estabaEnLiana) velocityX = 0;
    }

    /* --- Colisión con frutas --- */
    private void checkFruits() {
        for (Fruta f : frutas) {
            if (!f.isActiva()) continue;

            double dx = Math.abs(f.getPosicion().x - playerX);
            double dy = Math.abs(f.getPosicion().y - playerY);

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                f.setActiva(false);
                score += f.getPuntos();
                System.out.println("🍒 Fruta " + f.getTipo().getNombre() + " recogida! +" + f.getPuntos() + " pts");
            }
        }
    }

    /* --- Colisión con cocodrilos --- */
    private void checkCrocs() {
        for (Cocodrilo c : cocodrilos) {
            if (!c.isActivo()) continue;
            double dx = Math.abs(c.getPosicion().x - playerX);
            double dy = Math.abs(c.getPosicion().y - playerY);

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                System.out.println("🐊 ¡Cocodrilo te atrapó!");
                playerDeath();
                return; // evita múltiples colisiones el mismo tick
            }
        }
    }

    /* --- Reinicio al morir --- */
    private void playerDeath() {
        lives--;
        if (lives <= 0) {
            lives = PLAYER_START_LIVES;
            score = 0;
            initLevel();
        }
        playerX = PLAYER_START_X;
        playerY = PLAYER_START_Y;
        velocityX = 0;
        velocityY = 0;
        jumping = false;
    }

    /* --- Compilar estado para los clientes --- */
    private String buildGameState() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("PLAYER 0 x=%.0f y=%.0f lives=%d score=%d\n",
                playerX, playerY, lives, score));

        for (Cocodrilo c : cocodrilos)
            sb.append(c.toNetworkString()).append("\n");

        for (Fruta f : frutas)
            if (f.isActiva()) sb.append(f.toNetworkString()).append("\n");

        return sb.toString();
    }
}
