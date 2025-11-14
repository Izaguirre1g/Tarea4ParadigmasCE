package model;

public class Posicion {
    public Double x;
    public Double y;

    public Posicion(Double x, Double y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Posición no puede tener valores nulos");
        }
        this.x = x;
        this.y = y;
    }

    public Double getX() { return x; }
    public Double getY() { return y; }

    public void setX(Double x) { this.x = x; }
    public void setY(Double y) { this.y = y; }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", x, y);
    }
}