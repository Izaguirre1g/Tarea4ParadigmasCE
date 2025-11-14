package patterns.observer;

/**
 * Observer
 * -----------------------------------------------------
 * Interfaz del patrón Observer.
 * Los observadores son notificados cuando hay cambios en el estado.
 */
public interface Observer {
    /**
     * Método llamado cuando el observable notifica un cambio.
     * @param mensaje El mensaje o dato enviado por el observable
     */
    void actualizar(Object mensaje);

    /**
     * Obtiene el ID único del observador.
     * @return ID del observador
     */
    Integer getObserverId();
}