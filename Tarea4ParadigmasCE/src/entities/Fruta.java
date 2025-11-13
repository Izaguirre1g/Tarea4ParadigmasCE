package entities;

import model.Liana;
import utils.TipoFruta;

/**
 * Clase abstracta base para frutas.
 * ----------------------------------------------------
 * Las fábricas usan estos setters: setLiana, setAlturaEnLiana,
 * setPuntos, setTipo, setId.
 *
 * ✅ Cumple con:
 *    - Patrón Abstract Factory
 *    - Sin tipos primitivos (usa Integer, Boolean)
 *    - Encapsulamiento total con getters y setters
 */
public abstract class Fruta {

    /** Identificador único de la fruta */
    protected Integer id;

    /** Liana donde cuelga la fruta */
    protected Liana liana;

    /** Altura en la liana */
    protected Integer alturaEnLiana;

    /** Puntos que otorga al ser recolectada */
    protected Integer puntos;

    /** Tipo de fruta (BANANA / NARANJA / etc.) */
    protected TipoFruta tipo;

    /** Indica si la fruta ya fue recolectada */
    protected Boolean recolectada = Boolean.FALSE;

    // ==============================================================
    // Getters y Setters (usados por fábricas y GameManager)
    // ==============================================================

    /** Asigna el ID único de la fruta */
    public void setId(Integer id) {
        this.id = id;
    }

    /** Devuelve el ID de la fruta */
    public Integer getId() {
        return id;
    }

    /** Asigna la liana de la fruta */
    public void setLiana(Liana l) {
        this.liana = l;
    }

    /** Asigna la altura en la liana */
    public void setAlturaEnLiana(Integer a) {
        this.alturaEnLiana = a;
    }

    /** Asigna los puntos de la fruta */
    public void setPuntos(Integer p) {
        this.puntos = p;
    }

    /** Asigna el tipo de fruta */
    public void setTipo(TipoFruta t) {
        this.tipo = t;
    }

    /** Devuelve la cantidad de puntos que otorga */
    public Integer obtenerPuntos() {
        return puntos;
    }

    /** Devuelve el tipo de fruta */
    public TipoFruta getTipo() {
        return tipo;
    }

    /** Marca la fruta como recolectada */
    public void marcarComoRecolectada() {
        this.recolectada = Boolean.TRUE;
    }

    /** Verifica si la fruta ya fue recolectada */
    public Boolean estaRecolectada() {
        return recolectada;
    }

    public Liana getLiana() {
        return liana;
    }

    public Integer getAlturaEnLiana() {
        return alturaEnLiana;
    }

}
