package entities;

import model.Posicion;
import utils.TipoFruta;

public class Banana extends Fruta {

    public Banana(Posicion pos, int puntos) {
        super(TipoFruta.BANANA, pos);
        this.puntos = puntos;  // opcional, si ya lo define el enum puedes omitirlo
    }
}
