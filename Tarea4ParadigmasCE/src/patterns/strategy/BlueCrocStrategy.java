package patterns.strategy;

import entities.Cocodrilo;
import utils.GameConstants;

public class BlueCrocStrategy implements MovementStrategy {
    private double velocidad = GameConstants.CROC_SPEED * 0.8; // un poco más lento

    @Override
    public void move(Cocodrilo c) {
        c.getPosicion().y += velocidad;

        // si sale de la pantalla, reaparece arriba
        if (c.getPosicion().y > GameConstants.CROC_MAX_Y + 20) {
            c.getPosicion().y = GameConstants.CROC_MIN_Y;
        }
    }
}
