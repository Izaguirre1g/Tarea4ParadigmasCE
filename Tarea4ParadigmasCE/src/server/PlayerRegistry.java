package server;

import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PlayerRegistry
 * -----------------------------------------------------
 * Registro global de jugadores conectados.
 * Guarda PlayerSession y permite:
 *   - crear sesiones
 *   - eliminarlas
 *   - obtener JSON con la lista de jugadores
 *   - obtener el GameManager de un jugador
 */
public class PlayerRegistry {

    private static final Map<Integer, PlayerSession> sessions =
            new ConcurrentHashMap<>();

    private static final AtomicInteger nextId = new AtomicInteger(0);

    /** Crea una nueva sesión (partida nueva) para un jugador. */
    public static PlayerSession createSession(Socket socket, String playerName) {
        int id = nextId.getAndIncrement();
        PlayerSession session = new PlayerSession(id, socket);
        if (playerName != null && !playerName.isBlank()) {
            session.name = playerName;
        }
        sessions.put(id, session);
        System.out.println("[REGISTRY] Nuevo jugador id=" + id +
                ", name=" + session.name);
        return session;
    }

    /** Remueve la sesión cuando un jugador se desconecta. */
    public static void removeSession(int id) {
        PlayerSession s = sessions.remove(id);
        if (s != null) {
            System.out.println("[REGISTRY] Jugador desconectado id=" + id);
        }
    }

    /** Devuelve todas las sesiones activas. */
    public static Collection<PlayerSession> getSessions() {
        return sessions.values();
    }

    /** Obtiene el GameManager (partida) de un jugador específico. */
    public static GameManager getGameManager(int playerId) {
        PlayerSession s = sessions.get(playerId);
        return (s != null) ? s.game : null;
    }

    /** Construye un JSON simple con la lista de jugadores. */
    public static String getPlayersJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;
        for (PlayerSession s : sessions.values()) {
            if (!first) sb.append(",");
            first = false;

            sb.append("{\"id\":")
                    .append(s.id)
                    .append(",\"name\":\"")
                    .append(escapeJson(s.name))
                    .append("\"}");
        }

        sb.append("]");
        return sb.toString();
    }

    // Escapa comillas en nombres de jugador
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
