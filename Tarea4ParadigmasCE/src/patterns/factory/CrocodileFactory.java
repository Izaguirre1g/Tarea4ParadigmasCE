package patterns.factory;

import entities.Cocodrilo;
import entities.CocodriloRojo;
import entities.CocodriloAzul;
import model.Liana;
import utils.TipoCocodrilo;

public class CrocodileFactory {

    public static Cocodrilo createCrocodile(int id, TipoCocodrilo tipo, Liana liana) {
        switch (tipo) {
            case ROJO:
                // Suben y bajan entre y=100 y y=400
                return new CocodriloRojo(id, liana, 100, 400);

            case AZUL:
                // Caen hasta y=500
                return new CocodriloAzul(id, liana, 500);

            default:
                throw new IllegalArgumentException("Tipo de cocodrilo desconocido: " + tipo);
        }
    }
}
