package patterns.strategy;

import entities.Cocodrilo;
import model.Liana;
import model.Posicion;

/**
 * RedCrocStrategy
 * -----------------------------------------------------
 * Estrategia de movimiento para cocodrilos rojos.
 * Se mueven VERTICALMENTE (arriba y abajo) dentro de los límites de su liana asignada.
 * Si no tienen liana asignada, usan límites por defecto.
 */
public class RedCrocStrategy implements MovementStrategy {

    private static final Double VERTICAL_SPEED = 1.2;  // Velocidad vertical
    private static final Double ADJUSTMENT_SPEED = 2.0;  // Velocidad para ajuste inicial

    @Override
    public void move(Cocodrilo cocodrilo) {
        Posicion pos = cocodrilo.getPosicion();
        Liana liana = cocodrilo.getLiana();

        // Si no tiene liana asignada, usar movimiento vertical por defecto
        if (liana == null) {
            moveVerticalDefault(cocodrilo);
            return;
        }

        // Obtener límites verticales de la liana
        double lianaMinY = Math.min(liana.getPosicionInicio().y, liana.getPosicionFin().y);
        double lianaMaxY = Math.max(liana.getPosicionInicio().y, liana.getPosicionFin().y);
        double lianaMinX = Math.min(liana.getPosicionInicio().x, liana.getPosicionFin().x);
        double lianaMaxX = Math.max(liana.getPosicionInicio().x, liana.getPosicionFin().x);

        // Tamaño del sprite del cocodrilo
        final double CROC_HEIGHT = 30.0;

        // Ajustar límites verticales con márgenes
        double minY = lianaMinY + 15;  // Margen superior
        double maxY = lianaMaxY - CROC_HEIGHT - 15;  // Margen inferior reducido

        // FASE 1: Ajustar posición X si está fuera de la liana
        if (!cocodrilo.getIsAligned()) {
            double lianaCenterX = (lianaMinX + lianaMaxX) / 2.0;

            // Si está muy lejos horizontalmente, mover hacia el centro
            if (Math.abs(pos.x - lianaCenterX) > 5.0) {
                if (pos.x < lianaCenterX) {
                    pos.x += ADJUSTMENT_SPEED;
                } else {
                    pos.x -= ADJUSTMENT_SPEED;
                }
            } else {
                pos.x = lianaCenterX;
                cocodrilo.setIsAligned(true);

                // Inicializar dirección vertical si no está establecida
                if (cocodrilo.getDireccion() == 0) {
                    cocodrilo.setDireccion(1); // Empezar bajando
                }
            }
        }

        // FASE 2: Movimiento vertical con rebote
        if (cocodrilo.getIsAligned()) {
            Integer direccion = cocodrilo.getDireccion();

            // Si la dirección es 0, establecer una dirección inicial
            if (direccion == 0) {
                direccion = 1;
                cocodrilo.setDireccion(direccion);
            }

            // Movimiento vertical
            pos.y += VERTICAL_SPEED * direccion;

            // Rebote en los límites verticales
            if (pos.y <= minY) {
                pos.y = minY;
                cocodrilo.setDireccion(1); // Cambiar a bajar
            } else if (pos.y >= maxY) {
                pos.y = maxY;
                cocodrilo.setDireccion(-1); // Cambiar a subir
            }
        }
    }

    /**
     * Movimiento vertical por defecto cuando no hay liana asignada
     */
    private void moveVerticalDefault(Cocodrilo cocodrilo) {
        Posicion pos = cocodrilo.getPosicion();
        Integer direccion = cocodrilo.getDireccion();

        final Double MIN_Y = 150.0;
        final Double MAX_Y = 520.0;

        // Si la dirección es 0, establecer una dirección inicial
        if (direccion == 0) {
            direccion = 1;
            cocodrilo.setDireccion(direccion);
        }

        // Movimiento vertical
        pos.y += VERTICAL_SPEED * direccion;

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