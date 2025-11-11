package utils;

import patterns.strategy.MovementStrategy;
import patterns.strategy.RedCrocStrategy;
import patterns.strategy.BlueCrocStrategy;
import static utils.GameConstants.*;

public enum TipoCocodrilo {
    ROJO("RED", 2.0, new RedCrocStrategy()),
    AZUL("BLUE", 2.5, new BlueCrocStrategy());


    private final String nombre;
    private final double velocidadBase;
    private final MovementStrategy strategy;

    TipoCocodrilo(String nombre, double velocidadBase, MovementStrategy strategy) {
        this.nombre = nombre;
        this.velocidadBase = velocidadBase;
        this.strategy = strategy;
    }

    public String getNombre() { return nombre; }
    public double getVelocidadBase() { return velocidadBase; }
    public MovementStrategy getStrategy() { return strategy; }

    @Override
    public String toString() {
        return nombre;
    }
}
