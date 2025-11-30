package model;

public class Liana {
    private Integer id;
    private Posicion inicio;
    private Posicion fin;

    public Liana(Integer id, Posicion inicio, Posicion fin) {
        this.id = id;
        this.inicio = inicio;
        this.fin = fin;
    }

    public Integer getId() { return id; }
    public Posicion getPosicionInicio() { return inicio; }
    public Posicion getPosicionFin() { return fin; }

    @Override
    public String toString() {
        return "Liana{id=" + id + ", inicio=" + inicio + ", fin=" + fin + "}";
    }
}