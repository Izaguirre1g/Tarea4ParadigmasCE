package server;

import network.ClientHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static utils.GameConstants.*;

public class GameManager {

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // Estado del jugador
    private double playerX = PLAYER_START_X;
    private double playerY = PLAYER_START_Y;
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean jumping = false;
    private boolean onLiana = false;
    private boolean grounded = false;
    private int score = 0;
    private int lives = PLAYER_START_LIVES;

    // Cocodrilo de prueba
    private double crocY = CROC_MIN_Y;
    private double crocDir = 1;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    public GameManager() {
        executor.scheduleAtFixedRate(this::tick, 0, TICK_RATE_MS, TimeUnit.MILLISECONDS);
    }

    public void addClient(ClientHandler c) { clients.add(c); }
    public void removeClient(ClientHandler c) { clients.remove(c); }

    public synchronized void processInput(String line) {
        if (line.contains("LEFT"))      velocityX = -PLAYER_SPEED_X;
        else if (line.contains("RIGHT")) velocityX =  PLAYER_SPEED_X;
        else if (line.contains("UP") && onLiana)    velocityY = -PLAYER_SPEED_Y;
        else if (line.contains("DOWN") && onLiana)  velocityY =  PLAYER_SPEED_Y;
        else if (line.contains("STOP")) {
            velocityX = 0;
            if (onLiana) velocityY = 0;
        }
        if (line.contains("JUMP") && !jumping && !onLiana && grounded) {
            jumping = true;
            velocityY = -Math.abs(PLAYER_JUMP_VELOCITY); // subir
            grounded = false;
        }
    }

    private synchronized void tick() {
        double prevX = playerX;
        double prevY = playerY;

        //Mover en X y resolver colisiones horizontales (paredes de plataformas)
        playerX += velocityX;
        resolveHorizontal(prevX);

        //Gravedad si no está en liana
        if (!onLiana) {
            velocityY += GRAVITY;
            if (velocityY > MAX_FALL_SPEED) velocityY = MAX_FALL_SPEED;
        }

        //Mover en Y
        prevY = playerY;
        playerY += velocityY;

        //Lianas
        checkLianas();

        //Resolver colisiones verticales con plataformas (aterrizaje / cabeza)
        grounded = resolveVertical(prevY);

        //Suelo/techo absolutos
        if (playerY + PLAYER_HEIGHT >= WIN_HEIGHT) {
            playerY = WIN_HEIGHT - PLAYER_HEIGHT;
            velocityY = 0;
            jumping = false;
            grounded = true;
        }
        if (playerY < 0) {
            playerY = 0;
            if (velocityY < 0) velocityY = 0;
        }

        //Limitar X al mundo
        if (playerX < 0) playerX = 0;
        if (playerX + PLAYER_WIDTH > WIN_WIDTH) playerX = WIN_WIDTH - PLAYER_WIDTH;



        //Cocodrilo
        crocY += crocDir * CROC_SPEED;
        if (crocY > CROC_MAX_Y) crocDir = -1;
        if (crocY < CROC_MIN_Y) crocDir = 1;

        //Colisiones con fruta/enemigo
        checkCollisions();

        //Broadcast
        broadcastState();
    }

    /** Colisión horizontal contra plataformas (lados) */
    private void resolveHorizontal(double prevX) {
        for (double[] p : PLATFORMS) {
            double px = p[0], py = p[1], pw = p[2], ph = p[3];

            boolean overlapY = (playerY + PLAYER_HEIGHT > py) && (playerY < py + ph);
            boolean overlapX = (playerX + PLAYER_WIDTH > px) && (playerX < px + pw);

            if (overlapX && overlapY) {
                // Veníamos desde la izquierda → pegarnos al lado izquierdo
                if (prevX + PLAYER_WIDTH <= px) {
                    playerX = px - PLAYER_WIDTH;
                }
                // Veníamos desde la derecha → pegarnos al lado derecho
                else if (prevX >= px + pw) {
                    playerX = px + pw;
                }
                // Si no se puede decidir, no tocamos X (dejar que Y lo resuelva)
            }
        }
    }

    /**
     * Colisiones verticales con plataformas.
     * Si está en una liana, se permite atravesar las plataformas.
     */
    private boolean resolveVertical(double prevY) {
        boolean landed = false;

        // Si está en liana → no bloquear movimiento vertical
        if (onLiana) {
            return false;
        }

        for (double[] p : PLATFORMS) {
            double px = p[0], py = p[1], pw = p[2], ph = p[3];

            boolean overlapX = (playerX + PLAYER_WIDTH > px) && (playerX < px + pw);
            boolean overlapY = (playerY + PLAYER_HEIGHT > py) && (playerY < py + ph);

            if (!overlapX) continue;

            if (overlapY) {
                // Aterrizaje (venía de arriba)
                boolean wasAbove = (prevY + PLAYER_HEIGHT) <= py + COLLISION_TOLERANCE;
                boolean nowBelowTop = (playerY + PLAYER_HEIGHT) >= py - COLLISION_TOLERANCE;

                if (wasAbove && nowBelowTop && velocityY >= 0) {
                    playerY = py - PLAYER_HEIGHT;
                    velocityY = 0;
                    jumping = false;
                    landed = true;
                    continue;
                }

                // Golpe de cabeza (venía desde abajo)
                boolean wasBelow = prevY >= (py + ph) - COLLISION_TOLERANCE;
                boolean nowAboveBottom = playerY <= (py + ph) + COLLISION_TOLERANCE;

                if (wasBelow && nowAboveBottom && velocityY < 0) {
                    playerY = py + ph;
                    velocityY = 0;
                }
            }
        }
        return landed;
    }


    private void checkLianas() {
        onLiana = false;

        for (double[] l : LIANAS) {
            double lx1 = l[0], ly1 = l[1], lx2 = l[2], ly2 = l[3];
            double cx = (lx1 + lx2) * 0.5;
            double playerCenterX = playerX + PLAYER_WIDTH * 0.5;

            double dx = Math.abs(cx - playerCenterX);
            boolean withinY = (playerY + PLAYER_HEIGHT > ly1) && (playerY < ly2);

            if (dx < 15 && withinY) {
                onLiana = true;
                break;
            }
        }

        // Si está en liana y no hay input vertical, detén la caída para "colgarse"
        if (onLiana && Math.abs(velocityY) < 0.1) {
            velocityY = 0;
        }
    }

    private void checkCollisions() {
        // Cocodrilo: bounding simple
        double dx = Math.abs((playerX + PLAYER_WIDTH/2.0) - (200 + CROC_WIDTH/2.0));
        double dy = Math.abs((playerY + PLAYER_HEIGHT/2.0) - (crocY + CROC_HEIGHT/2.0));
        if (dx < 20 && dy < 20) {
            lives--;
            if (lives <= 0) { lives = PLAYER_START_LIVES; score = 0; }
            resetPlayer();
        }

        // Fruta fija de ejemplo
        double fx = 350, fy = 180;
        if (Math.abs((playerX + PLAYER_WIDTH/2.0) - fx) < 18 &&
            Math.abs((playerY + PLAYER_HEIGHT/2.0) - fy) < 18) {
            System.out.println("🍎 ¡Fruta recogida!");
            score += POINTS_FRUIT;
        }
    }

    private void resetPlayer() {
        playerX = PLAYER_START_X;
        playerY = PLAYER_START_Y;
        velocityX = 0;
        velocityY = 0;
        jumping = false;
        grounded = false;
    }

    private void broadcastState() {
        for (ClientHandler ch : clients) {
            ch.sendLine(String.format(
                    "PLAYER 0 x=%.0f y=%.0f lives=%d score=%d",
                    playerX, playerY, lives, score));

            ch.sendLine(String.format(
                    "CROC 0 type=RED liana=1 x=200 y=%.0f alive=1",
                    crocY));

            ch.sendLine(String.format(
                    "FRUIT 0 liana=2 x=350 y=180 points=%d active=1",
                    POINTS_FRUIT));
        }
    }
}
