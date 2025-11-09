package patterns.strategy;

import model.Posicion;

/**
 * Estrategia de movimiento genérica para entidades que se mueven.
 */
public interface MovementStrategy {
    /**
     * Calcula la siguiente posición de la entidad según su estado actual y tiempo transcurrido.
     * @param actual posición actual
     * @param delta tiempo (en milisegundos o ticks)
     * @param velocidad velocidad base de la entidad
     * @return nueva posición
     */
    Posicion nextPosition(Posicion actual, double delta, double velocidad);
}
