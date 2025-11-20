package utils;

/**
 * GameConstants - 9 LIANAS ESTILO ARCADE
 * -----------------------------------------------------
 * Layout completo con todas las lianas y plataformas
 */
public final class GameConstants {

    private GameConstants() {}

    /* --- Configuración general --- */
    public static final Integer TICK_RATE_MS = 16;
    public static final Integer SERVER_PORT  = 5000;
    public static final String SERVER_IP = "127.0.0.1";

    /* --- Ventana / Escenario --- */
    public static final Integer WIN_WIDTH  = 960;
    public static final Integer WIN_HEIGHT = 540;

    /* --- Jugador --- */
    public static final Double PLAYER_START_X = 150.0;
    public static final Double PLAYER_START_Y = 490.0;
    public static final Integer PLAYER_WIDTH  = 24;
    public static final Integer PLAYER_HEIGHT = 28;

    public static final Double PLAYER_SPEED_X = 4.0;
    public static final Double PLAYER_SPEED_Y = 3.0;
    public static final Double PLAYER_JUMP_VELOCITY = 5.0;
    public static final Integer PLAYER_START_LIVES = 3;
    public static final Integer PLAYER_MAX_LIVES = 9;

    /* --- Física --- */
    public static final Double GRAVITY = 0.25;
    public static final Double MAX_FALL_SPEED = 8.0;

    /* --- Cocodrilos --- */
    public static final Double CROC_SPEED = 1.2;
    public static final Double CROC_MIN_Y = 150.0;
    public static final Double CROC_MAX_Y = 500.0;

    /* --- Frutas --- */
    public static final Integer FRUIT_SCORE_BANANA  = 70;
    public static final Integer FRUIT_SCORE_NARANJA = 100;
    public static final Integer FRUIT_SCORE_CEREZA  = 50;

    /* --- Jaula de DK --- */
    public static final Double CAGE_X = 20.0;
    public static final Double CAGE_Y = 85.0;
    public static final Integer CAGE_WIDTH = 70;
    public static final Integer CAGE_HEIGHT = 45;
    public static final Integer WIN_SCORE_BONUS = 1000;

    /* --- Lianas y plataformas --- */

    //LIANAS - 9 en total
    // ════════════════════════════════════════════════════════════════
    // Formato: {X_inicio, Y_inicio, X_fin, Y_fin}
    // ════════════════════════════════════════════════════════════════
    public static final Double[][] LIANAS = {
            // ────────────────────────────────────────────────────────
            // LADO IZQUIERDO (2 lianas)
            // ────────────────────────────────────────────────────────

            // LIANA 1: Desde plataforma superior (Y=130) hasta abajo
            // X=150 (cerca del borde izquierdo)
            {20.0, 130.0, 150.0, 515.0},

            // LIANA 2: Desde primera plataforma escalonada (Y=280) hasta abajo
            // X=250 (sobre la plataforma escalonada)
            {100.0, 130.0, 150.0, 490.0},

            // ────────────────────────────────────────────────────────
            // CENTRO (3 lianas)
            // ────────────────────────────────────────────────────────

            // LIANA 3: Desde plataforma superior (Y=130) hasta abajo
            // X=370 (cerca del borde derecho de plataforma superior izq)
            {220.0, 280.0, 220.0, 500.0},

            // LIANA 4: Entre plataforma superior y nueva plataforma
            // X=440 (en el espacio entre las dos plataformas superiores)
            // Solo conecta de Y=130 a Y=160 (corta)
            {370.0, 130.0, 370.0, 470.0},

            // LIANA 5: Desde nueva plataforma (Y=160) hasta abajo
            // X=480 (sobre la nueva plataforma superior derecha)
            {480.0, 160.0, 480.0, 400.0},

            // ────────────────────────────────────────────────────────
            // LADO DERECHO (4 lianas)
            // ────────────────────────────────────────────────────────

            // LIANA 6: Desde nueva plataforma (Y=160), pegada al inicio
            // X=670 (cerca del borde derecho de la plataforma)
            {570.0, 160.0, 670.0, 430.0},

            // LIANA 7: Desde nueva plataforma (Y=160), separada
            // X=730 (separada ~60px de la liana 6)
            {660.0, 160.0, 730.0, 410.0},

            // LIANA 8: Liana larga desde ARRIBA (sin conectar a plataforma)
            // X=810, empieza desde Y=50 (arriba de todo)
            {760.0, 50.0, 810.0, 430.0},

            // LIANA 9: Otra liana larga desde ARRIBA
            // X=880, empieza desde Y=50 (arriba de todo)
            {890.0, 50.0, 880.0, 430.0}
    };

    /**
     * Coordenadas X de cada liana para colocar cocodrilos y frutas correctamente.
     *
     * IMPORTANTE: Basadas en las coordenadas reales de las lianas diagonales/verticales.
     * Para lianas diagonales se usa el promedio de X_inicio y X_fin.
     * Para lianas verticales se usa directamente la X.
     *
     * Índice 0 = Liana 1, Índice 1 = Liana 2, ... Índice 8 = Liana 9
     */
    public static final Double[] LIANA_X_POSITIONS = {
            85.0,   // Liana 1: promedio de (20 + 150) / 2 = 85
            125.0,  // Liana 2: promedio de (100 + 150) / 2 = 125
            220.0,  // Liana 3: vertical en X=220
            370.0,  // Liana 4: vertical en X=370
            480.0,  // Liana 5: vertical en X=480
            620.0,  // Liana 6: promedio de (570 + 670) / 2 = 620
            695.0,  // Liana 7: promedio de (660 + 730) / 2 = 695
            785.0,  // Liana 8: promedio de (760 + 810) / 2 = 785
            885.0   // Liana 9: promedio de (890 + 880) / 2 = 885
    };

    /**
     * Obtener la coordenada X de una liana específica.
     *
     * @param lianaNumber Número de liana (1-9)
     * @return Coordenada X donde se debe colocar el objeto
     * @throws IllegalArgumentException si el número de liana es inválido
     */
    public static Double getLianaX(int lianaNumber) {
        if (lianaNumber < 1 || lianaNumber > 9) {
            throw new IllegalArgumentException(
                    "Número de liana debe estar entre 1 y 9, recibido: " + lianaNumber
            );
        }
        return LIANA_X_POSITIONS[lianaNumber - 1];
    }

    /**
     * Validar si un número de liana es válido.
     *
     * @param lianaNumber Número a validar
     * @return true si es válido (1-9), false en caso contrario
     */
    public static boolean isValidLianaNumber(int lianaNumber) {
        return lianaNumber >= 1 && lianaNumber <= 9;
    }

    // ════════════════════════════════════════════════════════════════
    // GUÍA PARA AJUSTAR LIANAS:
    // ════════════════════════════════════════════════════════════════
    // Cada liana: {X_inicio, Y_inicio, X_fin, Y_fin}
    //
    // Mover HORIZONTALMENTE: cambiar primer y tercer valor
    // Cambiar ALTURA INICIO: cambiar segundo valor (Y_inicio)
    // Cambiar ALTURA FIN: cambiar cuarto valor (Y_fin)
    //
    // ALTURAS DE REFERENCIA:
    // Y=50  → Por encima de todo
    // Y=130 → Plataforma superior izquierda
    // Y=160 → Plataforma superior derecha
    // Y=230 → Plataforma derecha media
    // Y=280 → Primera escalonada
    // Y=380 → Segunda escalonada
    // Y=470 → Islitas
    // Y=515 → Cerca del suelo
    // ════════════════════════════════════════════════════════════════

    //PLATAFORMAS - 12 en total
    public static final Double[][] PLATFORMS = {
            {0.0,   130.0, 440.0, 12.0},  // Superior izquierda
            {420.0, 160.0, 240.0, 12.0},  // Superior derecha
            {150.0, 280.0, 150.0, 12.0},  // Media arriba (izq)
            {200.0, 380.0, 150.0, 12.0},  // Media abajo (izq)
            {680.0, 330.0, 280.0, 12.0},  // Derecha
            {50.0,  520.0, 80.0,  12.0},  // Base izq 1
            {140.0, 520.0, 80.0,  12.0},  // Base izq 2
            {230.0, 520.0, 80.0,  12.0},  // Base izq 3
            {500.0, 470.0, 70.0,  12.0},  // Islita 1
            {600.0, 470.0, 70.0,  12.0},  // Islita 2
            {700.0, 470.0, 70.0,  12.0},  // Islita 3
            {800.0, 470.0, 70.0,  12.0}   // Islita 4
    };

    /* --- Límites de cámara --- */
    public static final Double MIN_X = 0.0;
    public static final Double MAX_X = WIN_WIDTH - PLAYER_WIDTH.doubleValue();
    public static final Double MIN_Y = 0.0;
    public static final Double MAX_Y = WIN_HEIGHT - PLAYER_HEIGHT.doubleValue();

    /* --- Utilidades --- */
    public static Double clamp(Double value, Double min, Double max) {
        return Math.max(min, Math.min(max, value));
    }
}