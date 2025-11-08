package model;

import java.util.ArrayList;
import java.util.List;
import entities.Cocodrilo;

/**
 * Representa una liana vertical. Stub mínimo para que las fábricas compilen.
 * Luego podrás añadir altura real, inicio/fin, etc.
 */
public class Liana {

    private Integer id;
    // Opcionalmente, podrías manejar posiciones de inicio/fin.
    private Posicion posicionBase; // por ejemplo, X fijo; Y base

    // Lista de cocodrilos en esta liana (para futura lógica).
    private final List<Cocodrilo> cocodrilos = new ArrayList<>();

    public Liana(Integer id, Posicion posicionBase) {
        this.id = id;
        this.posicionBase = posicionBase;
    }

    public Integer getId() { return id; }
    public Posicion getPosicionBase() { return posicionBase; }

    public void setId(Integer id) { this.id = id; }
    public void setPosicionBase(Posicion p) { this.posicionBase = p; }

    public List<Cocodrilo> getCocodrilos() { return cocodrilos; }

    public boolean agregarCocodrilo(Cocodrilo c) { return cocodrilos.add(c); }
    public boolean eliminarCocodrilo(Cocodrilo c) { return cocodrilos.remove(c); }
}
