package entities;

import model.Posicion;
import utils.TipoFruta;

public class Fruta {
    protected TipoFruta tipo;
    protected Posicion posicion;
    protected int puntos;
    protected boolean activa = true;

    public Fruta(TipoFruta tipo, Posicion pos) {
        this.tipo = tipo;
        this.posicion = pos;
        this.puntos = tipo.getPuntos();
    }

    public boolean checkCollision(Posicion jugador, int w, int h) {
        double dx = Math.abs(jugador.x - posicion.x);
        double dy = Math.abs(jugador.y - posicion.y);
        return dx < w && dy < h;
    }

    public String toNetworkString() {
        return String.format("FRUIT id=%d type=%s x=%.0f y=%.0f points=%d active=%d",
            this.hashCode(),
            tipo.name(),
            posicion.x,
            posicion.y,
            puntos,
            activa ? 1 : 0);
    }


    // ✅ Getters y setters
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    public int getPuntos() { return puntos; }
    public TipoFruta getTipo() { return tipo; }

    // 👇 Agrega este método
    public Posicion getPosicion() {
        return posicion;
    }
}
