package entities;

import model.Posicion;
import patterns.strategy.RedCrocStrategy;

public class CocodriloRojo extends Cocodrilo {

    public CocodriloRojo(Posicion posicion) {
        super(posicion, 1.2, new RedCrocStrategy());
    }
}
