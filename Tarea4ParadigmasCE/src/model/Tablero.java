package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero del juego Donkey Kong Jr.
 * Contiene la lista de lianas y permite acceder a ellas por índice.
 */
public class Tablero {

    private List<Liana> lianas;

    public Tablero() {
        this.lianas = new ArrayList<>();

        // Inicializar con 8 lianas base separadas horizontalmente
        for (int i = 0; i < 8; i++) {
            this.lianas.add(new Liana(i, i * 50)); // posición X separada
        }
    }

    public List<Liana> getLianas() {
        return lianas;
    }

    public Liana getLiana(Integer id) {
        if (id == null || id < 0 || id >= lianas.size()) {
            return null;
        }
        return lianas.get(id);
    }
}

