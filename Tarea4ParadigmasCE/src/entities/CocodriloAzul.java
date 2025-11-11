package entities;

import model.Posicion;
import patterns.strategy.BlueCrocStrategy;

public class CocodriloAzul extends Cocodrilo {

    public CocodriloAzul(Posicion posicion) {
        super(posicion, 1.2, new BlueCrocStrategy());
    }
}
