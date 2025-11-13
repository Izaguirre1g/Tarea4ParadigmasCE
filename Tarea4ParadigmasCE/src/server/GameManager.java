package server;

import entities.Cocodrilo;
import entities.Fruta;
import model.GameState;
import model.Liana;
import model.Tablero;
import patterns.factory.CrocodileFactory;
import patterns.factory.FruitFactory;
import patterns.observer.GameObservable;
import utils.TipoCocodrilo;
import utils.TipoFruta;

import java.util.*;

/**
 * Clase GameManager
 * -------------------------------
 * Controla la lógica principal del juego:
 * - Creación de entidades (cocodrilos, frutas)
 * - Eliminación dinámica
 * - Actualización de partidas
 * - Notificación a los observadores (clientes)
 *
 * Implementa el patrón Abstract Factory para crear entidades.
 */
public class GameManager {

    /** Lista de partidas activas */
    private final Map<Integer, GameState> partidas = new HashMap<>();

    /** Fábricas de objetos del juego */
    private final CrocodileFactory crocodileFactory = new CrocodileFactory();
    private final FruitFactory fruitFactory = new FruitFactory();

    /** Observables (uno por partida) */
    private final Map<Integer, GameObservable> observables = new HashMap<>();

    /** Contador global de entidades creadas */
    private Integer nextEntityId = 1;

    // ==========================================================
    // MÉTODOS DE CREACIÓN
    // ==========================================================

    /**
     * Crea y agrega un nuevo cocodrilo a la partida especificada.
     */
    public Boolean agregarCocodrilo(Integer partidaId, TipoCocodrilo tipo, Integer lianaId, Integer altura) {
        GameState estado = partidas.get(partidaId);
        if (estado == null) {
            System.out.println("[Error] Partida no encontrada: " + partidaId);
            return false;
        }

        Tablero tablero = estado.getTablero();
        Liana liana = tablero.getLiana(lianaId);
        if (liana == null) {
            System.out.println("[Error] Liana no encontrada: " + lianaId);
            return false;
        }

        Cocodrilo cocodrilo = crocodileFactory.crearCocodrilo(tipo, liana, altura);
        if (cocodrilo == null) {
            System.out.println("[Error] No se pudo crear cocodrilo de tipo " + tipo);
            return false;
        }

        cocodrilo.setId(nextEntityId++); // ✅ Usa setter público
        estado.getCocodrilos().add(cocodrilo);

        System.out.println("[GameManager] Cocodrilo agregado → ID: " + cocodrilo.getId() +
                " | Tipo: " + cocodrilo.getTipo() +
                " | Liana: " + lianaId +
                " | Altura: " + altura);

        notificarCambio(partidaId, estado);
        return true;
    }

    /**
     * Crea y agrega una nueva fruta a la partida especificada.
     */
    public Boolean agregarFruta(Integer partidaId, TipoFruta tipo, Integer lianaId, Integer altura, Integer puntos) {
        GameState estado = partidas.get(partidaId);
        if (estado == null) {
            System.out.println("[Error] Partida no encontrada: " + partidaId);
            return false;
        }

        Tablero tablero = estado.getTablero();
        Liana liana = tablero.getLiana(lianaId);
        if (liana == null) {
            System.out.println("[Error] Liana no encontrada: " + lianaId);
            return false;
        }

        Fruta fruta = fruitFactory.crearFruta(tipo, liana, altura, puntos);
        if (fruta == null) {
            System.out.println("[Error] No se pudo crear fruta de tipo " + tipo);
            return false;
        }

        fruta.setId(nextEntityId++); // ✅ Usa setter público
        estado.getFrutas().add(fruta);

        System.out.println("[GameManager] Fruta agregada → ID: " + fruta.getId() +
                " | Tipo: " + fruta.getTipo() +
                " | Liana: " + lianaId +
                " | Altura: " + altura);

        notificarCambio(partidaId, estado);
        return true;
    }

    // ==========================================================
    // MÉTODOS DE ELIMINACIÓN
    // ==========================================================

    /**
     * Elimina una fruta de la partida por su ID.
     */
    public Boolean eliminarFruta(Integer partidaId, Integer frutaId) {
        GameState estado = partidas.get(partidaId);
        if (estado == null) {
            System.out.println("[Error] Partida no encontrada: " + partidaId);
            return false;
        }

        Fruta frutaAEliminar = null;
        for (Fruta f : estado.getFrutas()) {
            if (f.getId().equals(frutaId)) {
                frutaAEliminar = f;
                break;
            }
        }

        if (frutaAEliminar == null) {
            System.out.println("[Error] Fruta no encontrada con ID: " + frutaId);
            return false;
        }

        estado.getFrutas().remove(frutaAEliminar);
        System.out.println("[GameManager] Fruta eliminada → ID: " + frutaId);

        notificarCambio(partidaId, estado);
        return true;
    }

    /**
     * Elimina un cocodrilo de la partida por su ID.
     */
    public Boolean eliminarCocodrilo(Integer partidaId, Integer cocodriloId) {
        GameState estado = partidas.get(partidaId);
        if (estado == null) {
            System.out.println("[Error] Partida no encontrada: " + partidaId);
            return false;
        }

        Cocodrilo crocAEliminar = null;
        for (Cocodrilo c : estado.getCocodrilos()) {
            if (c.getId().equals(cocodriloId)) {
                crocAEliminar = c;
                break;
            }
        }

        if (crocAEliminar == null) {
            System.out.println("[Error] Cocodrilo no encontrado con ID: " + cocodriloId);
            return false;
        }

        estado.getCocodrilos().remove(crocAEliminar);
        System.out.println("[GameManager] Cocodrilo eliminado → ID: " + cocodriloId);

        notificarCambio(partidaId, estado);
        return true;
    }

    // ==========================================================
    // UTILIDADES
    // ==========================================================

    /**
     * Muestra en consola todas las frutas y cocodrilos activos.
     */
    public void listarEntidades(Integer partidaId) {
        GameState estado = partidas.get(partidaId);
        if (estado == null) {
            System.out.println("[Error] Partida no encontrada: " + partidaId);
            return;
        }

        System.out.println("\n=== ENTIDADES ACTIVAS (Partida " + partidaId + ") ===");

        System.out.println("-- Cocodrilos --");
        for (Cocodrilo c : estado.getCocodrilos()) {
            System.out.println("ID: " + c.getId() + " | Tipo: " + c.getTipo() +
                    " | Liana: " + (c.getLianaActual() != null ? c.getLianaActual().getId() : "-") +
                    " | Altura: " + c.getAltura());
        }

        System.out.println("-- Frutas --");
        for (Fruta f : estado.getFrutas()) {
            System.out.println("ID: " + f.getId() + " | Tipo: " + f.getTipo() +
                    " | Liana: " + (f.getLiana() != null ? f.getLiana().getId() : "-") +
                    " | Altura: " + f.getAlturaEnLiana());
        }
    }

    /**
     * Notifica a los observadores (clientes) el cambio de estado.
     */
    private void notificarCambio(Integer partidaId, GameState estado) {
        GameObservable obs = observables.get(partidaId);
        if (obs != null) {
            obs.notificarObservadores(estado);
        }
    }
}


