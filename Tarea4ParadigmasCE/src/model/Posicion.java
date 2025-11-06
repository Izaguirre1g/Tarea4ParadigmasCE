package model;

/**
 * Posición 2D simple (x,y) en el tablero.
 * Stub mínimo para compilar las entidades.
 */
public class Posicion {

    // Usamos wrappers (Integer) para alinearnos con el PDF (no primitivos).
    private Integer x;
    private Integer y;

    public Posicion(Integer x, Integer y) {
        // Validaciones básicas (no nulos, no negativos).
        if (x == null || y == null || x < 0 || y < 0) {
            throw new IllegalArgumentException("Posición inválida");
        }
        this.x = x;
        this.y = y;
    }

    public Integer getX() { return x; }
    public Integer getY() { return y; }

    public void setX(Integer x) { this.x = x; }
    public void setY(Integer y) { this.y = y; }

    @Override
    public String toString() { return "(" + x + "," + y + ")"; }
}

