package entities;

import model.Liana;
import model.Posicion;
import patterns.strategy.MovementStrategy;
import utils.TipoCocodrilo;

public abstract class Cocodrilo {

    protected int id;
    protected Liana liana;
    protected Posicion posicion;
    protected double velocidad;
    protected boolean vivo;
    protected TipoCocodrilo tipo;
    protected MovementStrategy strategy;

    public Cocodrilo(int id, Liana liana, TipoCocodrilo tipo, MovementStrategy strategy) {
        this.id = id;
        this.liana = liana;
        this.tipo = tipo;
        this.strategy = strategy;
        this.posicion = new Posicion(liana.getX(), 0);
        this.velocidad = 0.1; // velocidad base
        this.vivo = true;
    }

    public void update(double delta) {
        if (!vivo) return;
        posicion = strategy.nextPosition(posicion, delta, velocidad);

        // En caso de ser un cocodrilo azul que "muere" al caer
        if (strategy instanceof patterns.strategy.BlueCrocStrategy blue) {
            this.vivo = blue.isAlive();
        }
    }

    // Getters
    public Posicion getPosicion() { return posicion; }
    public Liana getLiana() { return liana; }
    public int getId() { return id; }
    public boolean isVivo() { return vivo; }
    public TipoCocodrilo getTipo() { return tipo; }
}
