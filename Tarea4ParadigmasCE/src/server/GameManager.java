package server;

import entities.*;
import model.*;
import patterns.factory.*;
import utils.*;

public class GameManager {

    private GameObjectFactory crocodileFactory;
    private GameObjectFactory fruitFactory;

    public GameManager() {
        crocodileFactory = new CrocodileFactory();
        fruitFactory = new FruitFactory();
    }

    public Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Liana liana, Integer altura) {
        return crocodileFactory.crearCocodrilo(tipo, liana, altura);
    }

    public Fruta crearFruta(TipoFruta tipo, Liana liana, Integer altura, Integer puntos) {
        return fruitFactory.crearFruta(tipo, liana, altura, puntos);
    }
}
/*Desde consola
* GameManager manager = new GameManager();
Liana liana = new Liana(1, new Posicion(10, 0), new Posicion(10, 100));

Cocodrilo nuevoCocodrilo = manager.crearCocodrilo(TipoCocodrilo.ROJO, liana, 30);
Fruta nuevaBanana = manager.crearFruta(TipoFruta.BANANA, liana, 40, 150);
*
* */
