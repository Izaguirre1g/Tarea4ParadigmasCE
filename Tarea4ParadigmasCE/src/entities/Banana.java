package entities;

import model.Posicion;

public class Banana extends Fruta {

    public Banana(Posicion pos, int puntos) {
        super();
        this.setPuntos(puntos);  // opcional, si ya lo define el enum puedes omitirlo
    }
}
