package utils;

/**
 * GameConstants
 * -----------------------------------------------------
 * Contiene todas las constantes globales del juego.
 * Sirve para ajustar velocidades, físicas, dimensiones
 * y tiempos de actualización sin modificar la lógica.
 */
public final class GameConstants {

    private GameConstants() {} // Evita instanciar la clase

    /* --- Configuración general --- */
    public static final int TICK_RATE_MS = 16;     // ~60 FPS (1000/60 ≈ 16)
    public static final int SERVER_PORT  = 5000;   // Puerto del servidor
    public static final String SERVER_IP = "127.0.0.1";

    /* --- Ventana / Escenario --- */
    public static final int WIN_WIDTH  = 960;
    public static final int WIN_HEIGHT = 540;

    /* --- Jugador --- */
    public static final double PLAYER_START_X = 200;
    public static final double PLAYER_START_Y = 500;
    public static final int PLAYER_WIDTH  = 24;
    public static final int PLAYER_HEIGHT = 28;

    public static final double PLAYER_SPEED_X = 4.0;   // velocidad lateral
    public static final double PLAYER_SPEED_Y = 3.0;   // velocidad al subir/bajar liana
    public static final double PLAYER_JUMP_VELOCITY = 8.0;
    public static final int    PLAYER_START_LIVES = 3;

    /* --- Física --- */
    public static final double GRAVITY = 0.4;          // fuerza de gravedad por tick
    public static final double MAX_FALL_SPEED = 12.0;  // límite de velocidad en caída

    /* --- Cocodrilos --- */
    public static final double CROC_SPEED = 1.2;       // velocidad base
    public static final double CROC_MIN_Y = 150.0;     // límite superior de movimiento
    public static final double CROC_MAX_Y = 520.0;     // límite inferior de movimiento

    /* --- Frutas --- */
    public static final int FRUIT_SCORE_BANANA  = 70;
    public static final int FRUIT_SCORE_NARANJA = 100;
    public static final int FRUIT_SCORE_CEREZA  = 50;

    /* --- Lianas y plataformas --- */
    // Lianas: cada par {x1, y1, x2, y2}
    public static final double[][] LIANAS = {
        {160,120,160,525},
        {240,120,240,525},
        {400,220,400,525},
        {480,220,480,525},
        {640,120,640,525},
        {720,120,720,525}
    };

    // Plataformas: cada elemento {x, y, width, height}
    public static final double[][] PLATFORMS = {
        {100,525,760,15},  // suelo
        {150,420,600,10},  // media 1
        {250,320,400,10},  // media 2
        {100,220,500,10},  // media 3
        {0,120,300,10}     // superior
    };

    /* --- Límites de cámara (por seguridad) --- */
    public static final double MIN_X = 0;
    public static final double MAX_X = WIN_WIDTH - PLAYER_WIDTH;
    public static final double MIN_Y = 0;
    public static final double MAX_Y = WIN_HEIGHT - PLAYER_HEIGHT;

    /* --- Utilidades --- */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
