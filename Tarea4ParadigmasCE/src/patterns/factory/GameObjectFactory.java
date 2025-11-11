package patterns.factory;

import entities.*;
import model.Posicion;
import utils.TipoCocodrilo;
import utils.TipoFruta;

public class GameObjectFactory {

    private final CrocodileFactory crocFactory = new CrocodileFactory();
    private final FruitFactory fruitFactory = new FruitFactory();

    public Object create(String category, String type, Posicion pos) {
        if (category.equalsIgnoreCase("crocodile")) {
            return crocFactory.createCrocodile(TipoCocodrilo.valueOf(type.toUpperCase()), pos);
        } else if (category.equalsIgnoreCase("fruit")) {
            return fruitFactory.createFruit(TipoFruta.valueOf(type.toUpperCase()), pos);
        }
        throw new IllegalArgumentException("Tipo de objeto desconocido: " + category);
    }
}
