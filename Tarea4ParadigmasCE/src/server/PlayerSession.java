package server;

import java.net.Socket;

/**
 * PlayerSession
 * -----------------------------------------------------
 * Representa la sesión de un jugador conectado al servidor.
 * Cada sesión tiene SU PROPIO GameManager (su propia partida).
 */
public class PlayerSession {

    public final int id;          // ID numérico del jugador (0,1,2,...)
    public final Socket socket;   // socket asociado a ese jugador
    public String name;           // nombre enviado en el JOIN
    public GameManager game;      // partida individual de este jugador

    public PlayerSession(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
        this.name = "Jugador" + id;   // nombre por defecto
        this.game = new GameManager(); // cada jugador tiene su GameManager
    }
}

