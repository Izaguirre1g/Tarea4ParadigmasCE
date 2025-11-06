package patterns.observer;

/**
 * Interface Observer
 * -------------------
 * Define el comportamiento que deben tener todos los observadores.
 * En este proyecto, cada cliente conectado (ClientHandler)
 * implementará esta interfaz para recibir actualizaciones del servidor.
 */
public interface Observer {

    /**
     * Método que se ejecuta cuando el observable (servidor)
     * notifica un cambio o un mensaje nuevo.
     * @param data el mensaje o estado que el servidor envía
     */
    void actualizar(Object data);

    /**
     * Devuelve el identificador único del observador.
     * Sirve para saber qué cliente está recibiendo las notificaciones.
     * @return el ID del observador
     */
    Integer getObserverId();
}
