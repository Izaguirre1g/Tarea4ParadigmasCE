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
    public static final Integer TICK_RATE_MS = 16;     // ~60 FPS (1000/60 ≈ 16)
    public static final Integer SERVER_PORT  = 5000;   // Puerto del servidor
    public static final String SERVER_IP = "127.0.0.1";

    /* --- Ventana / Escenario --- */
    public static final Integer WIN_WIDTH  = 960;
    public static final Integer WIN_HEIGHT = 540;

    /* --- Jugador --- */
    public static final Double PLAYER_START_X = 200.0;
    public static final Double PLAYER_START_Y = 500.0;
    public static final Integer PLAYER_WIDTH  = 24;
    public static final Integer PLAYER_HEIGHT = 28;

    public static final Double PLAYER_SPEED_X = 4.0;   // velocidad lateral
    public static final Double PLAYER_SPEED_Y = 3.0;   // velocidad al subir/bajar liana
    public static final Double PLAYER_JUMP_VELOCITY = 5.0;
    public static final Integer PLAYER_START_LIVES = 3;

    /* --- Física --- */
    public static final Double GRAVITY = 0.25;          // fuerza de gravedad por tick
    public static final Double MAX_FALL_SPEED = 8.0;  // límite de velocidad en caída
    private static final double LIANA_HORIZONTAL_FACTOR = 0.40;

    /* --- Cocodrilos --- */
    public static final Double CROC_SPEED = 1.2;       // velocidad base
    public static final Double CROC_MIN_Y = 150.0;     // límite superior de movimiento
    public static final Double CROC_MAX_Y = 520.0;     // límite inferior de movimiento

    /* --- Frutas --- */
    public static final Integer FRUIT_SCORE_BANANA  = 70;
    public static final Integer FRUIT_SCORE_NARANJA = 100;
    public static final Integer FRUIT_SCORE_CEREZA  = 50;

    /* --- Jaula de DK (objetivo final) --- */
    public static final Double CAGE_X = 150.0;        // posición X de la jaula
    public static final Double CAGE_Y = 80.0;         // posición Y de la jaula
    public static final Integer CAGE_WIDTH = 60;      // ancho de la jaula
    public static final Integer CAGE_HEIGHT = 40;     // alto de la jaula
    public static final Integer WIN_SCORE_BONUS = 1000; // bonus por ganar

    /* --- Lianas y plataformas --- */
    // Lianas: cada par {x1, y1, x2, y2}
    public static final Double[][] LIANAS = {
            {160.0, 120.0, 160.0, 525.0},
            {240.0, 120.0, 240.0, 525.0},
            {400.0, 220.0, 400.0, 525.0},
            {480.0, 220.0, 480.0, 525.0},
            {640.0, 120.0, 640.0, 525.0},
            {720.0, 120.0, 720.0, 525.0}
    };

    // Plataformas: cada elemento {x, y, width, height}
    public static final Double[][] PLATFORMS = {
            {100.0, 525.0, 760.0, 15.0},  // suelo
            {150.0, 420.0, 600.0, 10.0},  // media 1
            {250.0, 320.0, 400.0, 10.0},  // media 2
            {100.0, 220.0, 500.0, 10.0},  // media 3
            {0.0, 120.0, 300.0, 10.0}     // superior
    };

    /* --- Límites de cámara (por seguridad) --- */
    public static final Double MIN_X = 0.0;
    public static final Double MAX_X = WIN_WIDTH - PLAYER_WIDTH.doubleValue();
    public static final Double MIN_Y = 0.0;
    public static final Double MAX_Y = WIN_HEIGHT - PLAYER_HEIGHT.doubleValue();

    /* --- Utilidades --- */
    public static Double clamp(Double value, Double min, Double max) {
        return Math.max(min, Math.min(max, value));
    }
}
