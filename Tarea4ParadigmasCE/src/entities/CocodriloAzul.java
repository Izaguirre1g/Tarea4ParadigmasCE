package entities;

import model.Liana;
import patterns.strategy.BlueCrocStrategy;
import utils.TipoCocodrilo;

public class CocodriloAzul extends Cocodrilo {
    public CocodriloAzul(int id, Liana liana, double limiteInferior) {
        super(id, liana, TipoCocodrilo.AZUL, new BlueCrocStrategy(limiteInferior));
    }
}
