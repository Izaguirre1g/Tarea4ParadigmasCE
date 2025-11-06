package patterns.observer;

import java.util.List;

/**
 * Interface Observable
 * ---------------------
 * Representa el sujeto que será observado.
 * En este caso, el servidor (GameServer o GameManager)
 * implementará esta interfaz para permitir que los clientes
 * (ClientHandler) se registren como observadores.
 */
public interface Observable {

    /**
     * Agrega un nuevo observador a la lista.
     * @param obs el observador que desea suscribirse
     */
    void agregarObservador(Observer obs);

    /**
     * Elimina un observador de la lista.
     * @param obs el observador que desea darse de baja
     */
    void eliminarObservador(Observer obs);

    /**
     * Notifica a todos los observadores con un mensaje o estado.
     * @param data el objeto o mensaje que se enviará a todos los observadores
     */
    void notificarObservadores(Object data);
}
