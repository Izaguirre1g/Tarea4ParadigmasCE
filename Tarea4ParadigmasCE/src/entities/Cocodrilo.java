package entities;

import model.Posicion;
import patterns.strategy.MovementStrategy;

public class Cocodrilo {

    // Sistema de IDs únicos
    private static Integer nextId = 1;

    private Integer id;
    private Posicion posicion;
    private Double velocidad;
    private Integer direccion;
    private Boolean activo;
    private MovementStrategy strategy;

    public Cocodrilo(Posicion posicion, Double velocidad, MovementStrategy strategy) {
        this.id = nextId++;
        this.posicion = posicion;
        this.velocidad = velocidad;
        this.strategy = strategy;
        this.direccion = 1;
        this.activo = Boolean.TRUE;
    }

    public void update() {
        if (strategy != null && activo) {
            strategy.move(this);
        }
    }

    // Getters y Setters
    public Integer getId() { return id; }

    public Posicion getPosicion() { return posicion; }
    public void setPosicion(Posicion posicion) { this.posicion = posicion; }

    public Double getVelocidad() { return velocidad; }
    public void setVelocidad(Double velocidad) { this.velocidad = velocidad; }

    public Integer getDireccion() { return direccion; }
    public void setDireccion(Integer direccion) { this.direccion = direccion; }

    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public MovementStrategy getStrategy() { return strategy; }
    public void setStrategy(MovementStrategy strategy) { this.strategy = strategy; }

    public String toNetworkString() {
        String tipo = (strategy instanceof patterns.strategy.RedCrocStrategy) ? "RED" : "BLUE";
        return String.format("CROC %d type=%s x=%.0f y=%.0f alive=%d",
                id, tipo, posicion.x, posicion.y, activo ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.format("Cocodrilo{id=%d, pos=%s, velocidad=%.1f, activo=%b}",
                id, posicion, velocidad, activo);
    }
}