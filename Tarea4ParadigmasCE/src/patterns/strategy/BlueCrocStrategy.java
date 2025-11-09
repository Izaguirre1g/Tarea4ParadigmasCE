package patterns.strategy;

import model.Posicion;

/**
 * Estrategia de movimiento para cocodrilos azules:
 * descienden verticalmente hasta "caer" (fuera de pantalla).
 */
public class BlueCrocStrategy implements MovementStrategy {

    private final double limiteInferior;
    private boolean alive = true;

    public BlueCrocStrategy(double limiteInferior) {
        this.limiteInferior = limiteInferior;
    }

    @Override
    public Posicion nextPosition(Posicion actual, double delta, double velocidad) {
        if (!alive) return actual;

        double nuevaY = actual.getY() + velocidad * delta;
        if (nuevaY >= limiteInferior) {
            alive = false; // Se cae
        }
        return new Posicion(actual.getX(), nuevaY);
    }

    public boolean isAlive() {
        return alive;
    }
}
