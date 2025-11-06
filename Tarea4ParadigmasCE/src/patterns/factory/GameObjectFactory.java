package patterns.factory;

import entities.Cocodrilo;
import entities.Fruta;
import model.Liana;
import utils.TipoCocodrilo;
import utils.TipoFruta;

/**
 * Clase abstracta de la Abstract Factory.
 * Define los métodos de creación para cocodrilos y frutas.
 */
public abstract class GameObjectFactory {

    /**
     * Crea un cocodrilo del tipo indicado, en la liana y altura dadas.
     */
    public abstract Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Liana liana, Integer altura);

    /**
     * Crea una fruta del tipo indicado, en la liana y altura dadas, con puntaje.
     */
    public abstract Fruta crearFruta(TipoFruta tipo, Liana liana, Integer altura, Integer puntos);
}
