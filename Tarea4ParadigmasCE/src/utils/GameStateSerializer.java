package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.GameState;

/**
 * GameStateSerializer
 * -----------------------------------------------------
 * Utilidad para serializar/deserializar GameState usando Gson.
 * Convierte el estado del juego a JSON y viceversa.
 */
public class GameStateSerializer {

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()  // JSON formateado (opcional, para debugging)
            .create();

    /**
     * Serializa un GameState a JSON.
     * @param state El estado del juego
     * @return String en formato JSON
     */
    public static String toJson(GameState state) {
        return gson.toJson(state);
    }

    /**
     * Deserializa un JSON a GameState.
     * @param json String en formato JSON
     * @return GameState reconstruido
     */
    public static GameState fromJson(String json) {
        return gson.fromJson(json, GameState.class);
    }
}