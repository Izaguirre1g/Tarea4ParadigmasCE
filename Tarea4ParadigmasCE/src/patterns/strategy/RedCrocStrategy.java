package patterns.strategy;

import entities.Cocodrilo;
import model.Posicion;

/**
 * RedCrocStrategy
 * -----------------------------------------------------
 * Estrategia de movimiento para cocodrilos rojos.
 * Suben y bajan en un rango vertical limitado.
 */
public class RedCrocStrategy implements MovementStrategy {

    private static final Double MIN_Y = 150.0;
    private static final Double MAX_Y = 520.0;

    @Override
    public void move(Cocodrilo cocodrilo) {
        Posicion pos = cocodrilo.getPosicion();
        Double velocidad = cocodrilo.getVelocidad();
        Integer direccion = cocodrilo.getDireccion();

        // Movimiento vertical
        pos.y += velocidad * direccion;

        // Cambiar dirección en los límites
        if (pos.y <= MIN_Y) {
            pos.y = MIN_Y;
            cocodrilo.setDireccion(1); // cambiar a bajar
        } else if (pos.y >= MAX_Y) {
            pos.y = MAX_Y;
            cocodrilo.setDireccion(-1); // cambiar a subir
        }
    }
}