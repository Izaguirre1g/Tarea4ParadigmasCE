package patterns.strategy;

import entities.Cocodrilo;

/**
 * MovementStrategy
 * -----------------------------------------------------
 * Interfaz del patrón Strategy para el movimiento de cocodrilos.
 * Cada tipo de cocodrilo tiene su propia estrategia de movimiento.
 */
public interface MovementStrategy {
    /**
     * Define cómo se mueve el cocodrilo en cada tick.
     * @param cocodrilo El cocodrilo a mover
     */
    void move(Cocodrilo cocodrilo);
}