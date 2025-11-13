package patterns.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase GameObservable
 * ---------------------
 * Implementación concreta del patrón Observer.
 * Contiene una lista de observadores (clientes)
 * y maneja la lógica para notificarles cuando algo cambia
 * (por ejemplo, un nuevo estado del juego o un mensaje del servidor).
 *
 */
public class GameObservable implements Observable {

    /** Lista de observadores suscritos (clientes activos) */
    private final List<Observer> observadores;

    /**
     * Constructor: inicializa la lista vacía de observadores.
     */
    public GameObservable() {
        observadores = new ArrayList<>();
    }

    /**
     * Agrega un nuevo observador (cliente) a la lista.
     * @param obs observador que se desea agregar
     */
    @Override
    public void agregarObservador(Observer obs) {
        observadores.add(obs);
        System.out.println("[Observer] Cliente #" + obs.getObserverId() + " se ha suscrito.");
    }

    /**
     * Elimina un observador (cliente) cuando se desconecta.
     * @param obs observador que se desea eliminar
     */
    @Override
    public void eliminarObservador(Observer obs) {
        observadores.remove(obs);
        System.out.println("[Observer] Cliente #" + obs.getObserverId() + " se ha dado de baja.");
    }

    /**
     * Envía un mensaje a todos los observadores suscritos.
     * @param data mensaje o estado a enviar
     */
    @Override
    public void notificarObservadores(Object data) {
        for (Observer obs : observadores) {
            obs.actualizar(data);
        }
    }

    /**
     * Devuelve la cantidad actual de observadores conectados.
     * Usa Integer en lugar de int para cumplir con la restricción.
     * @return cantidad de observadores conectados
     */
    public Integer contarObservadores() {
        // .size() devuelve int, pero se autoboxea a Integer automáticamente
        return observadores.size();
    }
}
