package entities;

import model.Posicion;

/**
 * Mario - El antagonista que patrulla la plataforma superior
 * Se mueve de izquierda a derecha y es peligroso al contacto
 */
public class Mario {

    private static Integer idCounter = 0;

    private Integer id;
    private Posicion posicion;
    private Double velocidad;
    private Boolean moviendoDerecha;  // true = derecha, false = izquierda

    // Límites de movimiento en la plataforma superior
    private static final Double MIN_X = 140.0;  // Cerca de la jaula
    private static final Double MAX_X = 420.0;  // Final de plataforma
    private static final Double Y_FIXED = 107.0;  // Y fija (plataforma superior)

    // Tamaño para colisiones
    private static final Double WIDTH = 40.0;
    private static final Double HEIGHT = 40.0;

    /**
     * Constructor
     * @param velocidad Velocidad de movimiento (píxeles por tick)
     */
    public Mario(Double velocidad) {
        this.id = idCounter++;
        this.posicion = new Posicion(MIN_X, Y_FIXED);
        this.velocidad = velocidad;
        this.moviendoDerecha = true;
    }

    /**
     * Actualiza la posición de Mario (patrullaje)
     */
    public void update() {
        if (moviendoDerecha) {
            posicion.x += velocidad;

            // Si llegó al final de la plataforma, cambiar dirección
            if (posicion.x >= MAX_X) {
                posicion.x = MAX_X;
                moviendoDerecha = false;
            }
        } else {
            posicion.x -= velocidad;

            // Si llegó al inicio, cambiar dirección
            if (posicion.x <= MIN_X) {
                posicion.x = MIN_X;
                moviendoDerecha = true;
            }
        }
    }

    /**
     * Verifica si el jugador está colisionando con Mario
     * @param playerX Posición X del jugador
     * @param playerY Posición Y del jugador
     * @param playerWidth Ancho del jugador
     * @param playerHeight Alto del jugador
     * @return true si hay colisión
     */
    public boolean colisionaConJugador(Double playerX, Double playerY,
                                       Double playerWidth, Double playerHeight) {
        // Colisión AABB (Axis-Aligned Bounding Box)
        boolean colisionX = playerX + playerWidth > posicion.x &&
                playerX < posicion.x + WIDTH;

        boolean colisionY = playerY + playerHeight > posicion.y &&
                playerY < posicion.y + HEIGHT;

        return colisionX && colisionY;
    }

    /**
     * Serializa a string para protocolo de red
     * Formato: "MARIO id x y direction"
     */
    public String toNetworkString() {
        return String.format("MARIO %d x=%.0f y=%.0f dir=%s",
                id, posicion.x, posicion.y,
                moviendoDerecha ? "R" : "L");
    }

    // Getters
    public Integer getId() { return id; }
    public Posicion getPosicion() { return posicion; }
    public Double getVelocidad() { return velocidad; }
    public Boolean isMoviendoDerecha() { return moviendoDerecha; }

    // Setters
    public void setVelocidad(Double velocidad) { this.velocidad = velocidad; }

    @Override
    public String toString() {
        return String.format("Mario[id=%d, x=%.0f, y=%.0f, dir=%s, vel=%.1f]",
                id, posicion.x, posicion.y,
                moviendoDerecha ? "→" : "←", velocidad);
    }
}