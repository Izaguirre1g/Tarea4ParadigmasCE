package model;

/**
 * Representa una liana individual dentro del tablero.
 */
public class Liana {

    private Integer id;
    private Integer x; // coordenada horizontal en el tablero

    public Liana(Integer id, Integer x) {
        this.id = id;
        this.x = x;
    }

    public Integer getId() {
        return id;
    }

    public Integer getX() {
        return x;
    }
}


