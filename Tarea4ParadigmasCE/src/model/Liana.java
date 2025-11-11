package model;

public class Liana {
    private Posicion inicio;
    private Posicion fin;

    public Liana(Posicion inicio, Posicion fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public Posicion getPosicionInicio() { return inicio; }
    public Posicion getPosicionFin() { return fin; }
}
