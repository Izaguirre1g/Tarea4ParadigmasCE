package patterns.strategy;

import model.Posicion;

/**
 * Estrategia de movimiento para cocodrilos rojos:
 * suben y bajan en una sola liana, sin caerse.
 */
public class RedCrocStrategy implements MovementStrategy {

    private double direction = 1; // 1 = bajando, -1 = subiendo
    private final double yMin;
    private final double yMax;

    public RedCrocStrategy(double yMin, double yMax) {
        this.yMin = yMin;
        this.yMax = yMax;
    }

    @Override
    public Posicion nextPosition(Posicion actual, double delta, double velocidad) {
        double nuevaY = actual.getY() + direction * velocidad * delta;

        // Cambiar dirección si llega a extremos
        if (nuevaY >= yMax) {
            nuevaY = yMax;
            direction = -1;
        } else if (nuevaY <= yMin) {
            nuevaY = yMin;
            direction = 1;
        }

        return new Posicion(actual.getX(), nuevaY);
    }
}
