package entities;

import model.Liana;
import model.Posicion;
import utils.TipoCocodrilo;

/**
 * Clase abstracta base para cocodrilos.
 * ----------------------------------------------------
 * Contiene los atributos comunes y los setters usados por las fábricas.
 * La lógica de movimiento (mover) se implementará en las subclases
 * específicas (CocodriloRojo, CocodriloAzul).
 *
 * ✅ Cumple con:
 *    - Patrón Abstract Factory
 *    - Sin tipos primitivos (usa Integer, Double, Boolean)
 *    - Encapsulamiento total (getters y setters públicos)
 */
public abstract class Cocodrilo {

    /** Identificador único del cocodrilo */
    protected Integer id;

    /** Posición actual en el tablero */
    protected Posicion posicion;

    /** Liana en la que el cocodrilo se encuentra */
    protected Liana lianaActual;

    /** Velocidad de movimiento (unidades por tick) */
    protected Double velocidad;

    /** Tipo de cocodrilo (ROJO o AZUL) */
    protected TipoCocodrilo tipo;

    /** Altura en la liana (distancia desde la base) */
    protected Integer altura;

    // ==============================================================
    // Constructores
    // ==============================================================

    protected Cocodrilo() {
        this.id = null;
        this.posicion = null;
        this.lianaActual = null;
        this.velocidad = 0.0;
        this.tipo = null;
        this.altura = 0;
    }

    // ==============================================================
    // Getters y Setters (para uso de las fábricas y el GameManager)
    // ==============================================================

    /** Asigna el ID único del cocodrilo */
    public void setId(Integer id) {
        this.id = id;
    }

    /** Devuelve el ID del cocodrilo */
    public Integer getId() {
        return id;
    }

    /** Asigna la liana actual del cocodrilo */
    public void setLianaActual(Liana liana) {
        this.lianaActual = liana;
    }

    /** Asigna la altura en la liana */
    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    /** Asigna la velocidad de movimiento */
    public void setVelocidad(Double velocidad) {
        this.velocidad = velocidad;
    }

    /** Asigna el tipo de cocodrilo (ROJO o AZUL) */
    public void setTipo(TipoCocodrilo tipo) {
        this.tipo = tipo;
    }

    /** Devuelve la liana actual */
    public Liana getLianaActual() {
        return lianaActual;
    }

    /** Devuelve la altura actual */
    public Integer getAltura() {
        return altura;
    }

    /** Devuelve la velocidad actual */
    public Double getVelocidad() {
        return velocidad;
    }

    /** Devuelve el tipo de cocodrilo */
    public TipoCocodrilo getTipo() {
        return tipo;
    }

    /** Devuelve la posición actual */
    public Posicion getPosicion() {
        return posicion;
    }

    /** Asigna una nueva posición */
    public void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }

    // ==============================================================
    // Comportamiento abstracto (definido por subclases)
    // ==============================================================

    /**
     * Método abstracto que define cómo se mueve el cocodrilo.
     * Cada tipo de cocodrilo implementa su propio movimiento.
     */
    public abstract void mover();
}

