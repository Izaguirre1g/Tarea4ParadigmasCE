package entities;

import model.Posicion;
import utils.TipoFruta;

public class Naranja extends Fruta {

    public Naranja(Posicion pos, int puntos) {
        super(TipoFruta.NARANJA, pos);
        this.puntos = puntos;
    }
}
