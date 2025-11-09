package model;

public class Liana {
    private Integer id;
    private Posicion posicion;

    public Liana(Integer id, Posicion posicion) {
        this.id = id;
        this.posicion = posicion;
    }

    // 👇 Nuevo constructor opcional:
    public Liana(int x) {
        this.id = 0;
        this.posicion = new Posicion(x, 0);
    }

    public Integer getId() { return id; }
    public Posicion getPosicion() { return posicion; }

    public double getX() { return posicion.getX(); }
}
