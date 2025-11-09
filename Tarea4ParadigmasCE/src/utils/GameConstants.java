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

    /* --- Dimensiones del mundo --- */
    public static final int WIN_WIDTH  = 960;
    public static final int WIN_HEIGHT = 540;

    /* --- Físicas y movimiento --- */
    public static final double GRAVITY              = 1.2;
    public static final double PLAYER_SPEED_X       = 3.0;   // desplazamiento horizontal
    public static final double PLAYER_SPEED_Y       = 1.5;   // desplazamiento vertical (subir/bajar liana)
    public static final double PLAYER_JUMP_VELOCITY = -GRAVITY*8.0;
    public static final double CROC_SPEED           = 2.0;   // velocidad de subida/bajada
    public static final double MAX_FALL_SPEED       = 12.0;

    /* --- Jugador --- */
    public static final int PLAYER_WIDTH  = 24;
    public static final int PLAYER_HEIGHT = 28;
    public static final int PLAYER_START_X = 100;
    public static final int PLAYER_START_Y = 400;
    public static final int PLAYER_START_LIVES = 3;

    /* --- Cocodrilos --- */
    public static final int CROC_WIDTH  = 28;
    public static final int CROC_HEIGHT = 18;
    public static final int CROC_MIN_Y  = 150;
    public static final int CROC_MAX_Y  = 400;

    /* --- Frutas --- */
    public static final int FRUIT_SIZE     = 12;
    public static final int FRUIT_POINTS_1 = 50;
    public static final int FRUIT_POINTS_2 = 70;
    public static final int FRUIT_POINTS_3 = 100;

    /* --- Plataformas y lianas --- */
    public static final double[][] PLATFORMS = {
        {100, 525, 760, 15},  // suelo
        {200, 360, 500, 10},  // segunda
        {150, 240, 300, 10}   // tercera
    };

    public static final double[][] LIANAS = {
        {300, 100, 300, 525},
        {480,  60, 480, 360},
        {660, 100, 660, 525}
    };

    /* --- Colisiones --- */
    public static final double COLLISION_TOLERANCE = 2.0; // margen de detección

    /* --- Sistema de puntuación --- */
    public static final int POINTS_FRUIT = 50;
    public static final int POINTS_SURVIVE = 1; // cada tick vivo
}
