package entities;

import model.Liana;
import model.Posicion;
import utils.TipoCocodrilo;

/**
 * Clase abstracta base para cocodrilos. Contiene los campos y setters
 * que usan las fábricas (setLianaActual, setAltura, setVelocidad, setTipo).
 * La lógica de mover() se implementará en subclases.
 */
public abstract class Cocodrilo {

    protected Integer id;
    protected Posicion posicion;          // posición aproximada
    protected Liana lianaActual;          // liana donde se mueve
    protected Double velocidad;           // unidades por tick (stub)
    protected TipoCocodrilo tipo;         // ROJO o AZUL
    protected Integer altura;             // altura sobre la base de la liana (stub)

    // --- Getters/Setters requeridos por las fábricas ---
    public void setLianaActual(Liana liana) { this.lianaActual = liana; }
    public void setAltura(Integer altura) { this.altura = altura; }
    public void setVelocidad(Double v) { this.velocidad = v; }
    public void setTipo(TipoCocodrilo t) { this.tipo = t; }

    public Liana getLianaActual() { return lianaActual; }
    public Integer getAltura() { return altura; }
    public Double getVelocidad() { return velocidad; }
    public TipoCocodrilo getTipo() { return tipo; }

    public Posicion getPosicion() { return posicion; }
    public void setPosicion(Posicion p) { this.posicion = p; }

    // --- Comportamiento a definir por cada subclase ---
    public abstract void mover();
}
