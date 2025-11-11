package patterns.strategy;

import entities.Cocodrilo;
import utils.GameConstants;

public class RedCrocStrategy implements MovementStrategy {
    private double minY = GameConstants.CROC_MIN_Y;
    private double maxY = GameConstants.CROC_MAX_Y;

    @Override
    public void move(Cocodrilo c) {
        // movimiento controlado
        c.getPosicion().y += c.getVelocidad() * c.getDireccion();

        // invertir dirección al llegar a los límites
        if (c.getPosicion().y >= maxY) {
            c.getPosicion().y = maxY;
            c.setDireccion(-1);
        } else if (c.getPosicion().y <= minY) {
            c.getPosicion().y = minY;
            c.setDireccion(1);
        }
    }
}
