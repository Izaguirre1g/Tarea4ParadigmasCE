package entities;

import model.Liana;
import utils.TipoFruta;

/**
 * Clase abstracta base para las frutas del juego.
 * -------------------------------------------------
 * Representa el comportamiento y atributos comunes de todas las frutas.
 * Las fábricas (FruitFactory) utilizan estos métodos para construir
 * instancias concretas como Banana o Naranja.
 */
public abstract class Fruta {

    /** Identificador único de la fruta */
    protected Integer id;

    /** Liana donde cuelga la fruta */
    protected Liana liana;

    /** Altura vertical de la fruta en la liana */
    protected Integer alturaEnLiana;

    /** Puntaje que otorga al ser recolectada */
    protected Integer puntos;

    /** Tipo de fruta (BANANA, NARANJA, etc.) */
    protected TipoFruta tipo;

    /** Estado de recolección: TRUE si ya fue recogida */
    protected Boolean recolectada = Boolean.FALSE;

    // --- Setters usados por la fábrica (Abstract Factory Pattern) ---

    /**
     * Asigna la liana a la que pertenece la fruta.
     * @param l objeto Liana
     */
    public void setLiana(Liana l) {
        this.liana = l;
    }

    /**
     * Asigna la altura vertical en la liana.
     * @param a altura representada como Integer
     */
    public void setAlturaEnLiana(Integer a) {
        this.alturaEnLiana = a;
    }

    /**
     * Define los puntos que otorga la fruta.
     * @param p puntos en formato Integer
     */
    public void setPuntos(Integer p) {
        this.puntos = p;
    }

    /**
     * Define el tipo de fruta (BANANA/NARANJA).
     * @param t tipo de fruta
     */
    public void setTipo(TipoFruta t) {
        this.tipo = t;
    }

    /**
     * Devuelve los puntos que otorga esta fruta.
     * @return cantidad de puntos (Integer)
     */
    public Integer obtenerPuntos() {
        return puntos;
    }

    /**
     * Cambia el estado de la fruta a "recolectada".
     */
    public void marcarComoRecolectada() {
        this.recolectada = Boolean.TRUE;
    }

    /**
     * Verifica si la fruta ya fue recolectada.
     * @return TRUE si la fruta ya fue recogida, FALSE si no.
     */
    public Boolean estaRecolectada() {
        return recolectada;
    }
}

