package entities;

import model.Liana;
import patterns.strategy.RedCrocStrategy;
import utils.TipoCocodrilo;

public class CocodriloRojo extends Cocodrilo {
    public CocodriloRojo(int id, Liana liana, double yMin, double yMax) {
        super(id, liana, TipoCocodrilo.ROJO, new RedCrocStrategy(yMin, yMax));
    }
}
