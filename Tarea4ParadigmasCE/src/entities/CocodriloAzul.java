package entities;

import model.Posicion;
import patterns.strategy.BlueCrocStrategy;

/**
 * CocodriloAzul
 * -----------------------------------------------------
 * Cocodrilo que desciende verticalmente y cae.
 */
public class CocodriloAzul extends Cocodrilo {

    public CocodriloAzul(Posicion posicion) {
        super(posicion, 1.5, new BlueCrocStrategy());
    }
}