package entities;

import model.Liana;
import model.Posicion;
import utils.TipoFruta;

public class Fruta {

    // Sistema de IDs únicos
    private static Integer nextId = 1;

    private Integer id;
    private TipoFruta tipo;
    private Posicion posicion;
    private Liana liana;
    private Integer puntos;
    private Boolean activa;

    public Fruta() {
        this.id = nextId++;
        this.tipo = null;
        this.posicion = null;
        this.liana = null;
        this.puntos = 0;
        this.activa = Boolean.TRUE;
    }

    // Getters y Setters
    public Integer getId() { return id; }

    public TipoFruta getTipo() { return tipo; }
    public void setTipo(TipoFruta tipo) {
        this.tipo = tipo;
        if (tipo != null) {
            this.puntos = tipo.getPuntos();
        }
    }

    public Posicion getPosicion() { return posicion; }
    public void setPosicion(Posicion posicion) { this.posicion = posicion; }

    public Liana getLiana() { return liana; }
    public void setLiana(Liana liana) { this.liana = liana; }

    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }

    public Boolean isActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public String toNetworkString() {
        return String.format("FRUIT %d type=%s x=%.0f y=%.0f points=%d active=%d",
                id, tipo.getNombre(), posicion.x, posicion.y, puntos, activa ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.format("Fruta{id=%d, tipo=%s, pos=%s, puntos=%d, activa=%b}",
                id, tipo, posicion, puntos, activa);
    }
}