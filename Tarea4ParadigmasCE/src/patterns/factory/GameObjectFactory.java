package patterns.factory;

import entities.*;
import model.Posicion;
import utils.TipoFruta;

/**
 * GameObjectFactory
 * -----------------------------------------------------
 * Fábrica para crear objetos del juego (patrón Factory).
 * Centraliza la creación de entidades.
 */
public class GameObjectFactory {

    /**
     * Crea una fruta del tipo especificado en la posición dada.
     */
    public Fruta crearFruta(TipoFruta tipo, Posicion posicion) {
        Fruta fruta = new Fruta();
        fruta.setTipo(tipo);
        fruta.setPosicion(posicion);
        fruta.setActiva(Boolean.TRUE);
        return fruta;
    }

    /**
     * Crea un cocodrilo rojo en la posición dada.
     */
    public CocodriloRojo crearCocodriloRojo(Posicion posicion) {
        return new CocodriloRojo(posicion);
    }

    /**
     * Crea un cocodrilo azul en la posición dada.
     */
    public CocodriloAzul crearCocodriloAzul(Posicion posicion) {
        return new CocodriloAzul(posicion);
    }
}