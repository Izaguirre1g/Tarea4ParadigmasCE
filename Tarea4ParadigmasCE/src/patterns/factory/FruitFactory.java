package patterns.factory;

import entities.*;
import model.Posicion;
import utils.TipoFruta;

/**
 * FruitFactory
 * -----------------------------------------------------
 * Crea instancias de frutas según su tipo (BANANA, NARANJA, CEREZA).
 */
public class FruitFactory {

    public Fruta createFruit(TipoFruta tipo, Posicion pos) {
        switch (tipo) {
            case BANANA:
                return new Banana(pos, tipo.getPuntos());
            case NARANJA:
                return new Naranja(pos, tipo.getPuntos());
            case CEREZA:
                return new Fruta(tipo, pos);
            default:
                throw new IllegalArgumentException("Tipo de fruta desconocido: " + tipo);
        }
    }
}
