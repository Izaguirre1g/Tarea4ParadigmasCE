package entities;

import model.Posicion;
import patterns.strategy.MovementStrategy;

public class Cocodrilo {

    private Posicion posicion;
    private double velocidad;
    private int direccion; // 1 = abajo, -1 = arriba
    private boolean activo = true;
    private MovementStrategy strategy;

    public Cocodrilo(Posicion posicion, double velocidad, MovementStrategy strategy) {
        this.posicion = posicion;
        this.velocidad = velocidad;
        this.strategy = strategy;
        this.direccion = 1; // por defecto bajando
    }

    public void update() {
        if (strategy != null) strategy.move(this);
    }

    // --- Getters y setters ---
    public Posicion getPosicion() { return posicion; }
    public double getVelocidad() { return velocidad; }

    public void setVelocidad(double velocidad) { this.velocidad = velocidad; }

    public int getDireccion() { return direccion; }
    public void setDireccion(int direccion) { this.direccion = direccion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public MovementStrategy getStrategy() { return strategy; }
    public void setStrategy(MovementStrategy strategy) { this.strategy = strategy; }

    // --- Serialización para enviar al cliente ---
    public String toNetworkString() {
        String tipo = this.getClass().getSimpleName().contains("Rojo") ? "RED" : "BLUE";
        return String.format("CROC id=%d type=%s x=%.0f y=%.0f alive=%d",
                this.hashCode(), tipo, posicion.x, posicion.y, activo ? 1 : 0);
    }
}
