package patterns.observer;

/**
 * Observable
 * -----------------------------------------------------
 * Interfaz del patrón Observer para el sujeto observable.
 * Permite agregar, eliminar y notificar observadores.
 */
public interface Observable {
    /**
     * Agrega un observador a la lista.
     */
    void agregarObservador(Observer obs);

    /**
     * Elimina un observador de la lista.
     */
    void eliminarObservador(Observer obs);

    /**
     * Notifica a todos los observadores con un mensaje.
     */
    void notificarObservadores(Object mensaje);
}