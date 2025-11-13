package patterns.factory;

import entities.Cocodrilo;
import entities.Fruta;
import model.Liana;
import utils.TipoCocodrilo;
import utils.TipoFruta;

/**
 * Clase abstracta base para las fábricas de objetos del juego.
 * ------------------------------------------------------------
 * Define los métodos de creación genéricos usados por las fábricas concretas.
 *
 * ✅ Cumple con:
 *    - Patrón Abstract Factory
 *    - Sin tipos primitivos (usa Integer, Double)
 */
public abstract class GameObjectFactory {

    /** Crea un cocodrilo de un tipo específico. */
    public abstract Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Liana liana, Integer altura);

    /** Crea una fruta de un tipo específico. */
    public abstract Fruta crearFruta(TipoFruta tipo, Liana liana, Integer altura, Integer puntos);
}
