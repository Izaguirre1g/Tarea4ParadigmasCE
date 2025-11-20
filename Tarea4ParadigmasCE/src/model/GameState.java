package model;


import entities.Cocodrilo;
import entities.Fruta;

import java.util.ArrayList;
import java.util.List;

/**
 * GameState
 * -----------------------------------------------------
 * Encapsula todo el estado del juego en una sola clase.
 * Facilita la serialización y el manejo del estado completo.
 */
public class GameState {

    // Jugador
    private Double playerX;
    private Double playerY;
    private Double velocityX;
    private Double velocityY;
    private Integer lives;
    private Integer score;
    private Boolean jumping;
    private Boolean onLiana;
    private Boolean hasWon;  // indica si el jugador ganó el nivel
    private Boolean justGainedLife = false;


    // Entidades del juego
    private List<Cocodrilo> cocodrilos;
    private List<Fruta> frutas;
    private List<Liana> lianas;

    // Constructor
    public GameState() {
        this.playerX = 0.0;
        this.playerY = 0.0;
        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.lives = 3;
        this.score = 0;
        this.jumping = Boolean.FALSE;
        this.onLiana = Boolean.FALSE;
        this.hasWon = Boolean.FALSE;
        this.cocodrilos = new ArrayList<>();
        this.frutas = new ArrayList<>();
        this.lianas = new ArrayList<>();
    }

    // Getters y Setters - Jugador

    public Double getPlayerX() { return playerX; }
    public void setPlayerX(Double playerX) { this.playerX = playerX; }

    public Double getPlayerY() { return playerY; }
    public void setPlayerY(Double playerY) { this.playerY = playerY; }

    public Double getVelocityX() { return velocityX; }
    public void setVelocityX(Double velocityX) { this.velocityX = velocityX; }

    public Double getVelocityY() { return velocityY; }
    public void setVelocityY(Double velocityY) { this.velocityY = velocityY; }

    public Integer getLives() { return lives; }
    public void setLives(Integer lives) { this.lives = lives; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Boolean isJumping() { return jumping; }
    public void setJumping(Boolean jumping) { this.jumping = jumping; }

    public Boolean isOnLiana() { return onLiana; }
    public void setOnLiana(Boolean onLiana) { this.onLiana = onLiana; }

    public Boolean hasWon() { return hasWon; }
    public void setHasWon(Boolean hasWon) { this.hasWon = hasWon; }

    public Boolean getJustGainedLife() {
        return justGainedLife;
    }

    public void setJustGainedLife(Boolean justGainedLife) {
        this.justGainedLife = justGainedLife;
    }

    // Getters y Setters - Entidades

    public List<Cocodrilo> getCocodrilos() { return cocodrilos; }
    public void setCocodrilos(List<Cocodrilo> cocodrilos) { this.cocodrilos = cocodrilos; }

    public List<Fruta> getFrutas() { return frutas; }
    public void setFrutas(List<Fruta> frutas) { this.frutas = frutas; }

    public List<Liana> getLianas() { return lianas; }
    public void setLianas(List<Liana> lianas) { this.lianas = lianas; }

    @Override
    public String toString() {
        return String.format("GameState{player=(%.0f,%.0f), lives=%d, score=%d, cocodrilos=%d, frutas=%d}",
                playerX, playerY, lives, score, cocodrilos.size(), frutas.size());
    }
}