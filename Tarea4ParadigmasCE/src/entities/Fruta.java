package entities;

import model.Liana;
import utils.TipoFruta;

/**
 * Clase abstracta base para frutas. Las fábricas usan estos setters:
 * setLiana, setAlturaEnLiana, setPuntos, setTipo.
 */
public abstract class Fruta {

    protected Integer id;
    protected Liana liana;            // liana donde cuelga la fruta
    protected Integer alturaEnLiana;  // posición vertical
    protected Integer puntos;         // puntaje al recolectar
    protected TipoFruta tipo;         // BANANA/NARANJA
    protected Boolean recolectada = false;

    // --- Setters usados por la fábrica ---
    public void setLiana(Liana l) { this.liana = l; }
    public void setAlturaEnLiana(Integer a) { this.alturaEnLiana = a; }
    public void setPuntos(Integer p) { this.puntos = p; }
    public void setTipo(TipoFruta t) { this.tipo = t; }

    public Integer obtenerPuntos() { return puntos; }
}
