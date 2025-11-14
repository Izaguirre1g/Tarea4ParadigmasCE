package patterns.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * GameObservable
 * -----------------------------------------------------
 * Implementación concreta del patrón Observer para el juego.
 * Mantiene una lista de observadores y los notifica de cambios.
 */
public class GameObservable implements Observable {

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void agregarObservador(Observer obs) {
        if (!observers.contains(obs)) {
            observers.add(obs);
            System.out.println("[Observable] Observador agregado: " + obs.getObserverId());
        }
    }

    @Override
    public void eliminarObservador(Observer obs) {
        if (observers.remove(obs)) {
            System.out.println("[Observable] Observador eliminado: " + obs.getObserverId());
        }
    }

    @Override
    public void notificarObservadores(Object mensaje) {
        for (Observer obs : observers) {
            obs.actualizar(mensaje);
        }
    }
}