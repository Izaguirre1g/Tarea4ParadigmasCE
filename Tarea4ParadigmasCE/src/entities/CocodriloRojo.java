package entities;

import model.Posicion;
import patterns.strategy.RedCrocStrategy;

/**
 * CocodriloRojo
 * -----------------------------------------------------
 * Cocodrilo que sube y baja en una única liana sin caerse.
 */
public class CocodriloRojo extends Cocodrilo {

    public CocodriloRojo(Posicion posicion) {
        super(posicion, 1.2, new RedCrocStrategy());
    }
}