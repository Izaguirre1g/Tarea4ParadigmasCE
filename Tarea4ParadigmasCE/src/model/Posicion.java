package model;

/**
 * Representa una posición en el plano del juego.
 *
 * Inmutable: al modificar (mover, sumar) devuelve una nueva Posicion.
 */
public class Posicion {
    private final double x;
    private final double y;

    public Posicion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * Devuelve una nueva posición sumando un desplazamiento.
     */
    public Posicion add(double dx, double dy) {
        return new Posicion(this.x + dx, this.y + dy);
    }

    /**
     * Distancia euclidiana a otra posición (por si luego se usa en colisiones).
     */
    public double distanceTo(Posicion other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Compara posiciones con una pequeña tolerancia.
     */
    public boolean equals(Posicion other) {
        if (other == null) return false;
        double eps = 0.0001;
        return Math.abs(this.x - other.x) < eps && Math.abs(this.y - other.y) < eps;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
