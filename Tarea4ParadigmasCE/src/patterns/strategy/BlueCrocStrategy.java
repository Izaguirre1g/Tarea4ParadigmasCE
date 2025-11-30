package patterns.strategy;

import entities.Cocodrilo;
import model.Posicion;

/**
 * BlueCrocStrategy
 * -----------------------------------------------------
 * Estrategia de movimiento para cocodrilos azules.
 * Descienden verticalmente y se desactivan al salir del límite.
 */
public class BlueCrocStrategy implements MovementStrategy {

    private static final Double MAX_Y = 550.0;

    @Override
    public void move(Cocodrilo cocodrilo) {
        Posicion pos = cocodrilo.getPosicion();
        Double velocidad = cocodrilo.getVelocidad();

        // Siempre desciende
        pos.y += velocidad;

        // Si sale del límite, se desactiva
        if (pos.y >= MAX_Y) {
            cocodrilo.setActivo(Boolean.FALSE);
        }
    }
}