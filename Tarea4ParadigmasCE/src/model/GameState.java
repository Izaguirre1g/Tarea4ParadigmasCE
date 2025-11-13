package model;

import entities.Cocodrilo;
import entities.Fruta;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el estado completo de una partida del juego.
 * Contiene el tablero, los cocodrilos y las frutas activas.
 */
public class GameState {

    private Integer partidaId;
    private Tablero tablero;
    private List<Cocodrilo> cocodrilos;
    private List<Fruta> frutas;

    public GameState() {
        this.partidaId = 0;
        this.tablero = new Tablero();
        this.cocodrilos = new ArrayList<>();
        this.frutas = new ArrayList<>();
    }

    // --- GETTERS Y SETTERS ---

    public Integer getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(Integer partidaId) {
        this.partidaId = partidaId;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public List<Cocodrilo> getCocodrilos() {
        return cocodrilos;
    }

    public List<Fruta> getFrutas() {
        return frutas;
    }
}

