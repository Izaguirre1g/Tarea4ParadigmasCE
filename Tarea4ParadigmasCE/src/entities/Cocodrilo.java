package entities;

import model.Liana;
import model.Posicion;
import utils.TipoCocodrilo;

/**
 * Clase abstracta base para los cocodrilos del juego.
 * ----------------------------------------------
 * Representa la estructura común de cualquier cocodrilo (rojo o azul).
 * Contiene los atributos y métodos base que utilizan las fábricas para
 * configurar las instancias (Abstract Factory Pattern).
 *
 * ✅ Cumple con el requerimiento del proyecto:
 *    - No usa tipos primitivos (int, double, boolean)
 *    - Usa las clases equivalentes (Integer, Double, Boolean)
 *    - Preparada para ser extendida por subclases concretas
 */
public abstract class Cocodrilo {

    /** Identificador único del cocodrilo */
    protected Integer id;

    /** Posición actual del cocodrilo en el tablero */
    protected Posicion posicion;

    /** Liana donde el cocodrilo se encuentra actualmente */
    protected Liana lianaActual;

    /** Velocidad del cocodrilo en unidades por tick */
    protected Double velocidad;

    /** Tipo de cocodrilo (ROJO o AZUL) */
    protected TipoCocodrilo tipo;

    /** Altura actual sobre la base de la liana */
    protected Integer altura;

    /**
     * Constructor protegido (opcional): puede usarse en subclases
     * para inicializar atributos comunes.
     */
    protected Cocodrilo() {
        this.id = null;
        this.posicion = null;
        this.lianaActual = null;
        this.velocidad = 0.0;
        this.tipo = null;
        this.altura = 0;
    }

    // --- Getters y Setters requeridos por las fábricas ---

    public void setLianaActual(Liana liana) {
        this.lianaActual = liana;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public void setVelocidad(Double velocidad) {
        this.velocidad = velocidad;
    }

    public void setTipo(TipoCocodrilo tipo) {
        this.tipo = tipo;
    }

    public Liana getLianaActual() {
        return lianaActual;
    }

    public Integer getAltura() {
        return altura;
    }

    public Double getVelocidad() {
        return velocidad;
    }

    public TipoCocodrilo getTipo() {
        return tipo;
    }

    public Posicion getPosicion() {
        return posicion;
    }

    public void setPosicion(Posicion posicion) {
        this.posicion = posicion;
    }

    /**
     * Método abstracto que define el movimiento del cocodrilo.
     * Cada subclase (rojo o azul) debe implementar su propia lógica.
     */
    public abstract void mover();
}

