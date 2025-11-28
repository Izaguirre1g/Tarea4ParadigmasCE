package server;

import entities.Cocodrilo;
import entities.Fruta;
import entities.Mario;
import model.GameState;
import model.Liana;
import model.Posicion;
import patterns.factory.GameObjectFactory;
import patterns.factory.GameObjectFactoryImpl;
import patterns.observer.GameObservable;
import utils.GameConstants;
import utils.GameStateSerializer;
import utils.TipoFruta;
import utils.TipoCocodrilo;

import java.util.Map;
import java.util.concurrent.*;

import static utils.GameConstants.*;

/**
 * GameManager
 * -----------------------------------------------------
 * Gestiona toda la lógica del juego usando GameState encapsulado.
 * Cada jugador tiene su propio GameManager (partida independiente).
 */
public class GameManager {

    // Estado del juego encapsulado
    private final GameState state = new GameState();

    // Ejecuta el loop del juego cada TICK_RATE_MS
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    // Factory para crear entidades
    private final GameObjectFactory factory = new GameObjectFactoryImpl();

    // Observable para notificar a los ClientHandler
    private final GameObservable observable = new GameObservable();

    // Jugadores conectados (para la lista en el panel admin)
    private final Map<Integer, String> connectedPlayers = new ConcurrentHashMap<>();

    private Integer currentLevel = 1;           // Nivel actual (incrementa al ganar)
    private Double speedMultiplier = 1.0;       // Multiplicador de velocidad de cocodrilos

    // Estado de invencibilidad temporal
    private boolean isInvincible = false;
    private long invincibilityEndTime = 0;
    private static final long INVINCIBILITY_DURATION = 2000; // 2 segundos en milisegundos

    // Modo de comunicación con los clientes
    public enum CommunicationMode {
        TEXT,   // Protocolo de texto actual
        JSON    // Protocolo JSON
    }

    private CommunicationMode mode = CommunicationMode.TEXT;

    public GameManager() {
        initLevel();
        executor.scheduleAtFixedRate(this::tick, 0,
                TICK_RATE_MS.longValue(), TimeUnit.MILLISECONDS);
    }

    /* =========================================================
       INICIALIZACIÓN
       ========================================================= */

    private void initLevel() {
        state.getLianas().clear();
        state.getFrutas().clear();
        state.getCocodrilos().clear();

        // Crear lianas
        for (int i = 0; i < LIANAS.length; i++) {
            Double[] l = LIANAS[i];
            state.getLianas().add(
                    new Liana(i,
                            new Posicion(l[0], l[1]),
                            new Posicion(l[2], l[3])));
        }

        // CREAR MARIO
        Double marioSpeed = 1.5;  // Velocidad base
        state.setMario(new Mario(marioSpeed));
        System.out.println("[GameManager] Mario creado con velocidad: " + marioSpeed);

        // Cocodrilos iniciales de ejemplo
        Cocodrilo crocRojo = factory.crearCocodrilo(TipoCocodrilo.ROJO,
                new Posicion(GameConstants.getLianaX(1), 300.0)); // Liana 1, X=85
        crocRojo.setLiana(state.getLianas().get(0)); // Asignar Liana 1 (índice 0)
        state.getCocodrilos().add(crocRojo);

        Cocodrilo crocAzul = factory.crearCocodrilo(TipoCocodrilo.AZUL,
                new Posicion(GameConstants.getLianaX(5), 200.0)); // Liana 5, X=480
        crocAzul.setLiana(state.getLianas().get(4)); // Asignar Liana 5 (índice 4)
        state.getCocodrilos().add(crocAzul);

        // Frutas iniciales
        state.getFrutas().add(
                factory.crearFruta(TipoFruta.BANANA,
                        new Posicion(GameConstants.getLianaX(3), 250.0))); // Liana 3, X=220

        state.getFrutas().add(
                factory.crearFruta(TipoFruta.CEREZA,
                        new Posicion(GameConstants.getLianaX(7), 350.0))); // Liana 7, X=695

        // Jugador
        state.setPlayerX(PLAYER_START_X);
        state.setPlayerY(PLAYER_START_Y);
        state.setVelocityX(0.0);
        state.setVelocityY(0.0);
        state.setLives(PLAYER_START_LIVES);
        state.setScore(0);
        state.setJumping(false);
        state.setOnLiana(false);
        state.setHasWon(false);
    }

    /* =========================================================
       LOOP PRINCIPAL DEL JUEGO
       ========================================================= */

    private void tick() {
        updatePlayer();
        updateMario();
        updateCrocs();
        updateInvincibility();  //Actualizar invencibilidad
        checkCollisions();
        checkAbyssfall();
        broadcast();

        // Resetear flag de vida extra después de enviar
        if (state.getJustGainedLife()) {
            state.setJustGainedLife(false);
        }
    }

    /* =========================================================
       ENTRADA DEL JUGADOR (desde ClientHandler)
       ========================================================= */

    public void handleInput(String input) {
        switch (input.toUpperCase()) {
            case "LEFT"  -> state.setVelocityX(-PLAYER_SPEED_X);
            case "RIGHT" -> state.setVelocityX(PLAYER_SPEED_X);
            case "UP" -> {
                if (state.isOnLiana()) state.setVelocityY(-PLAYER_SPEED_Y);
            }
            case "DOWN" -> {
                if (state.isOnLiana()) state.setVelocityY(PLAYER_SPEED_Y);
            }
            case "JUMP" -> {
                if (!state.isOnLiana() && !state.isJumping()) {
                    state.setJumping(true);
                    state.setVelocityY(-PLAYER_JUMP_VELOCITY);
                }
            }
            case "STOP" -> {
                state.setVelocityX(0.0);
                if (state.isOnLiana()) state.setVelocityY(0.0);
            }
        }
    }

    /* =========================================================
       ACTUALIZACIÓN DEL JUGADOR
       ========================================================= */

    private void updatePlayer() {

        // --- Movimiento horizontal ---
        double newX = state.getPlayerX() + state.getVelocityX();

        // Suavizado cuando está en liana
        if (state.isOnLiana()) {
            newX = state.getPlayerX() + (state.getVelocityX() * 0.35);
        }

        state.setPlayerX(clamp(newX, MIN_X, MAX_X));

        // --- Detectar si está en una liana ---
        state.setOnLiana(false);

        for (Liana liana : state.getLianas()) {
            double lx = liana.getPosicionInicio().x;
            double dx = Math.abs(lx - (state.getPlayerX() + PLAYER_WIDTH / 2.0));

            if (dx < 15.0 &&
                    state.getPlayerY() + PLAYER_HEIGHT > liana.getPosicionInicio().y &&
                    state.getPlayerY() < liana.getPosicionFin().y) {
                state.setOnLiana(true);
                break;
            }
        }

        // Si el jugador cayó fuera de la pantalla (abajo)
        //if (state.getPlayerY() > 500) {  // Más cerca del fondo visible
        //    System.out.println("¡Caíste al abismo!");
        //    playerDeath();
        //    return;
        //}

        // --- Movimiento vertical ---
        if (state.isOnLiana()) {
            // En liana → sin salto
            state.setJumping(false);
            state.setVelocityY(state.getVelocityY() * 0.7);

            double newY = state.getPlayerY() + state.getVelocityY();
            state.setPlayerY(clamp(newY, MIN_Y, MAX_Y));
            return;
        }

        // Sobre plataforma
        if (isOnPlatform()) {
            state.setVelocityY(0.0);
            state.setJumping(false);
            return;
        }

        // En el aire: gravedad + anti-tunneling
        double vy = state.getVelocityY() + GRAVITY;
        if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;

        double nextY = state.getPlayerY() + vy;

        Double landingY = getPlatformLandingY(
                state.getPlayerX(),
                state.getPlayerY(),
                nextY);

        if (landingY != null) {
            state.setPlayerY(landingY - PLAYER_HEIGHT);
            state.setVelocityY(0.0);
            state.setJumping(false);
        } else {
            state.setPlayerY(nextY);
            state.setVelocityY(vy);
        }

        state.setPlayerY(clamp(state.getPlayerY(), MIN_Y, MAX_Y));
    }

    /**
     * Actualiza la posición de Mario (patrullaje en plataforma superior)
     */
    private void updateMario() {
        if (state.getMario() != null) {
            state.getMario().update();
        }
    }

    /**
     * Verifica si el jugador está cayendo al abismo sin plataforma debajo.
     * Solo se activa si el jugador está:
     * 1. NO es invencible
     * 2. Cayendo (velocidad Y positiva)
     * 3. En la zona baja (Y > 500)
     * 4. Sin plataforma debajo en los próximos 100 píxeles
     */
    private void checkAbyssfall() {
        // NO verificar si es invencible
        if (isInvincible) {
            return;  // ← NUEVO: Protección durante invencibilidad
        }

        // Solo verificar si está cayendo
        if (state.getVelocityY() <= 0) {
            return;  // Subiendo o quieto
        }

        // Solo verificar si está en zona baja
        if (state.getPlayerY() < 500) {
            return;  // Zona segura arriba
        }

        // Verificar si hay plataforma debajo
        boolean hasPlatformBelow = false;
        double playerBottomY = state.getPlayerY() + PLAYER_HEIGHT;
        double playerCenterX = state.getPlayerX() + PLAYER_WIDTH / 2.0;

        for (Double[] plat : PLATFORMS) {
            double px = plat[0];
            double py = plat[1];
            double pw = plat[2];

            // Plataforma horizontalmente alineada?
            boolean horizontalMatch =
                    (playerCenterX >= px) && (playerCenterX <= px + pw);

            // Plataforma debajo (dentro de 100px)?
            boolean verticalMatch =
                    (py >= playerBottomY) && (py <= playerBottomY + 100);

            if (horizontalMatch && verticalMatch) {
                hasPlatformBelow = true;
                break;
            }
        }

        // Cayendo sin plataforma = ABISMO
        if (!hasPlatformBelow) {
            System.out.println("¡Caíste al abismo!");
            playerDeath();
        }
    }

    private Double getPlatformLandingY(double x, double yActual, double yNext) {
        for (Double[] plat : PLATFORMS) {
            double px = plat[0];
            double py = plat[1];
            double pw = plat[2];

            boolean dentroX = x + PLAYER_WIDTH > px && x < px + pw;
            if (!dentroX) continue;

            boolean caeSobre =
                    yActual + PLAYER_HEIGHT <= py &&
                            yNext + PLAYER_HEIGHT >= py;

            if (caeSobre) return py;
        }
        return null;
    }

    private boolean isOnPlatform() {
        double nextY = state.getPlayerY() + state.getVelocityY();
        double playerBottomNext = nextY + PLAYER_HEIGHT;
        double currentBottom = state.getPlayerY() + PLAYER_HEIGHT;

        Double closestPlatformY = null;

        for (Double[] plat : PLATFORMS) {
            double px = plat[0];
            double py = plat[1];
            double pw = plat[2];

            boolean insideX =
                    (state.getPlayerX() + PLAYER_WIDTH > px) &&
                            (state.getPlayerX() < px + pw);

            if (!insideX) continue;

            if (py >= currentBottom) {
                if (closestPlatformY == null || py < closestPlatformY) {
                    closestPlatformY = py;
                }
            }
        }

        if (closestPlatformY == null) return false;

        if (state.getVelocityY() >= 0 &&
                currentBottom <= closestPlatformY &&
                playerBottomNext >= closestPlatformY) {
            state.setPlayerY(closestPlatformY - PLAYER_HEIGHT);
            return true;
        }

        return false;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /* =========================================================
       COCODRILOS / COLISIONES
       ========================================================= */

    private void updateCrocs() {
        for (Cocodrilo c : state.getCocodrilos()) {
            c.update();
        }
    }

    private void checkCollisions() {
        checkFruits();
        checkCrocs();
        checkMario();
        checkCage();
    }

    private void checkFruits() {
        for (Fruta f : state.getFrutas()) {
            if (!f.isActiva()) continue;

            double dx = Math.abs(f.getPosicion().x - state.getPlayerX());
            double dy = Math.abs(f.getPosicion().y - state.getPlayerY());

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                f.setActiva(false);
                state.setScore(state.getScore() + f.getPuntos());
                System.out.println("Fruta " + f.getTipo().getNombre()
                        + " recogida! +" + f.getPuntos() + " pts");
            }
        }
    }

    private void checkCrocs() {
        // Si es invencible, no verificar colisiones
        if (isInvincible) {
            return;  //Protección contra cocodrilos
        }

        for (Cocodrilo c : state.getCocodrilos()) {
            if (!c.isActivo()) continue;

            double dx = Math.abs(c.getPosicion().x - state.getPlayerX());
            double dy = Math.abs(c.getPosicion().y - state.getPlayerY());

            if (dx < PLAYER_WIDTH && dy < PLAYER_HEIGHT) {
                System.out.println("¡Cocodrilo te atrapó!");
                playerDeath();
                return;
            }
        }
    }

    private void checkCage() {
        if (state.hasWon()) return;

        double playerCenterX = state.getPlayerX() + PLAYER_WIDTH / 2.0;
        double playerCenterY = state.getPlayerY() + PLAYER_HEIGHT / 2.0;

        boolean inCageX = playerCenterX >= CAGE_X &&
                playerCenterX <= CAGE_X + CAGE_WIDTH;
        boolean inCageY = playerCenterY >= CAGE_Y &&
                playerCenterY <= CAGE_Y + CAGE_HEIGHT;

        if (inCageX && inCageY) {
            playerWin();
        }
    }

    /**
     * Verifica si el jugador colisiona con Mario
     */
    private void checkMario() {
        // Si es invencible, no verificar
        if (isInvincible) {
            return;
        }

        Mario mario = state.getMario();
        if (mario == null) {
            return;
        }

        // Verificar colisión
        if (mario.colisionaConJugador(
                state.getPlayerX(),
                state.getPlayerY(),
                PLAYER_WIDTH.doubleValue(),
                PLAYER_HEIGHT.doubleValue())) {

            System.out.println("¡Mario te atrapó!");
            playerDeath();
        }
    }

    private void playerWin() {
        state.setHasWon(true);

        // Agregar puntos de victoria
        state.setScore(state.getScore() + WIN_SCORE_BONUS);

        // OTORGAR VIDA EXTRA (con límite)
        Integer currentLives = state.getLives();

        if (currentLives < PLAYER_MAX_LIVES) {
            state.setLives(currentLives + 1);
            state.setJustGainedLife(true);  // ← NUEVO: Activar animación
            System.out.println("💚 ¡VIDA EXTRA OTORGADA! Vidas: " + state.getLives());
        } else {
            // Si ya tiene máximo de vidas, dar bonus de puntos
            Integer bonusPuntos = 500;
            state.setScore(state.getScore() + bonusPuntos);
            System.out.println("Máximo de vidas alcanzado! Bonus: +" + bonusPuntos + " pts");
        }

        System.out.println("¡VICTORIA! Has rescatado a Donkey Kong!");
        System.out.println("Bonus: +" + WIN_SCORE_BONUS + " puntos");
        System.out.println("Puntuación: " + state.getScore());

        // INCREMENTAR NIVEL Y VELOCIDAD
        currentLevel++;
        speedMultiplier += 0.15;

        System.out.println("¡Nivel " + currentLevel + "! Velocidad de enemigos: x" +
                String.format("%.2f", speedMultiplier));

        // Esperar un momento antes de reiniciar (2 segundos)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // REINICIAR NIVEL
        System.out.println("Reiniciando nivel...");
        restartLevel();
    }

    /**
     * Reinicia el nivel manteniendo puntuación, vidas y nivel actual
     * Los cocodrilos se mueven más rápido cada vez
     */
    private void restartLevel() {
        // Guardar valores importantes
        Integer savedLives = state.getLives();
        Integer savedScore = state.getScore();

        // Reiniciar el nivel (limpia entidades y recrear)
        initLevel();

        // Restaurar valores guardados
        state.setLives(savedLives);
        state.setScore(savedScore);
        state.setHasWon(false);

        // APLICAR MULTIPLICADOR DE VELOCIDAD A COCODRILOS
        for (Cocodrilo croc : state.getCocodrilos()) {
            Double velocidadActual = croc.getVelocidad();
            croc.setVelocidad(velocidadActual * speedMultiplier);
        }

        // APLICAR MULTIPLICADOR A MARIO
        if (state.getMario() != null) {
            Double velocidadMario = state.getMario().getVelocidad();
            state.getMario().setVelocidad(velocidadMario * speedMultiplier);
            System.out.println("[GameManager] Mario velocidad: " +
                    (velocidadMario * speedMultiplier));
        }

        System.out.println("Nivel reiniciado con velocidad x" +
                String.format("%.2f", speedMultiplier));
        System.out.println("Vidas: " + savedLives + " | Puntos: " + savedScore);
    }

    private void playerDeath() {
        state.setLives(state.getLives() - 1);
        System.out.println("Jugador perdió una vida. Vidas restantes: " + state.getLives());

        if (state.getLives() <= 0) {
            // GAME OVER COMPLETO
            System.out.println(" ╔════════════════════════════╗");
            System.out.println(" ║       GAME OVER            ║");
            System.out.println(" ╚════════════════════════════╝");
            System.out.println("Puntuación final: " + state.getScore());
            System.out.println("Nivel alcanzado: " + currentLevel);

            // REINICIAR TODO
            state.setLives(PLAYER_START_LIVES);
            state.setScore(0);
            state.setHasWon(false);
            currentLevel = 1;
            speedMultiplier = 1.0;

            initLevel();

            System.out.println("Juego reiniciado desde el nivel 1");

            // Desactivar invencibilidad en game over
            isInvincible = false;
            return;
        }

        // ACTIVAR INVENCIBILIDAD TEMPORAL
        isInvincible = true;
        invincibilityEndTime = System.currentTimeMillis() + INVINCIBILITY_DURATION;
        System.out.println("Invencibilidad activada por " +
                (INVINCIBILITY_DURATION / 1000) + " segundos");

        // REPOSICIONAR AL JUGADOR
        state.setPlayerX(PLAYER_START_X);
        state.setPlayerY(PLAYER_START_Y);
        state.setVelocityX(0.0);
        state.setVelocityY(0.0);
        state.setJumping(false);

        System.out.println("Jugador reposicionado en el inicio");
    }

    /**
     * Actualiza el estado de invencibilidad temporal.
     * Desactiva la invencibilidad cuando expira el tiempo.
     */
    private void updateInvincibility() {
        if (isInvincible) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= invincibilityEndTime) {
                isInvincible = false;
                System.out.println("Invencibilidad terminada");
            }
        }
    }

    /* =========================================================
       SERIALIZACIÓN / BROADCAST
       ========================================================= */

    private String buildGameState() {
        if (mode == CommunicationMode.JSON) {
            return GameStateSerializer.toJson(state);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                    "PLAYER 0 x=%.0f y=%.0f vx=%.2f vy=%.2f lives=%d score=%d jumping=%d onliana=%d won=%d gained_life=%d\n",
                    state.getPlayerX(), state.getPlayerY(),
                    state.getVelocityX(), state.getVelocityY(),
                    state.getLives(),
                    state.getScore(),
                    state.isJumping() ? 1 : 0,
                    state.isOnLiana() ? 1 : 0,
                    state.hasWon() ? 1 : 0,
                    state.getJustGainedLife() ? 1 : 0));

            sb.append(String.format("CAGE x=%.0f y=%.0f w=%d h=%d\n",
                    CAGE_X, CAGE_Y, CAGE_WIDTH, CAGE_HEIGHT));

            // ENVIAR POSICIÓN DE MARIO
            if (state.getMario() != null) {
                sb.append(state.getMario().toNetworkString()).append("\n");
            }

            for (Cocodrilo c : state.getCocodrilos())
                sb.append(c.toNetworkString()).append("\n");

            for (Fruta f : state.getFrutas())
                if (f.isActiva())
                    sb.append(f.toNetworkString()).append("\n");

            return sb.toString();
        }
    }

    public void setCommunicationMode(CommunicationMode mode) {
        this.mode = mode;
        System.out.println("[GameManager] Modo de comunicación cambiado a: " + mode);
    }

    public void addObserver(patterns.observer.Observer obs) {
        observable.agregarObservador(obs);
    }

    public void removeObserver(patterns.observer.Observer obs) {
        observable.eliminarObservador(obs);
    }

    private void broadcast() {
        observable.notificarObservadores(buildGameState());
    }

    /* =========================================================
       MÉTODOS ADMIN - CREAR/ELIMINAR ENTIDADES
       ========================================================= */

    /**
     * Crea un cocodrilo en una posición específica (comando ADMIN)
     * @param tipo "ROJO" o "AZUL"
     * @param lianaNum Número de liana (1-9) ← CAMBIO: antes era 1-6
     * @param altura Altura en píxeles (0-540)
     */
    public void crearCocodriloAdmin(String tipo, int lianaNum, int altura) {
        // Validar rango 1-9
        if (lianaNum < 1 || lianaNum > 9) {
            System.out.println("[GameManager] Error: liana inválida " + lianaNum);
            return;
        }

        double x = GameConstants.getLianaX(lianaNum);
        double y = (double) altura;

        Posicion pos = new Posicion(x, y);

        Cocodrilo croc;
        if ("ROJO".equals(tipo)) {
            croc = factory.crearCocodrilo(TipoCocodrilo.ROJO, pos);
        } else if ("AZUL".equals(tipo)) {
            croc = factory.crearCocodrilo(TipoCocodrilo.AZUL, pos);
        } else {
            System.out.println("[GameManager] Error: tipo de cocodrilo inválido " + tipo);
            return;
        }

        // Asignar liana
        if (lianaNum >= 1 && lianaNum <= state.getLianas().size()) {
            try {
                croc.setLiana(state.getLianas().get(lianaNum - 1));
            } catch (Exception e) {
                System.out.println("[GameManager] Advertencia: No se pudo asignar liana al cocodrilo");
            }
        }

        state.getCocodrilos().add(croc);

        System.out.println("[GameManager] Cocodrilo creado → ID: " + croc.getId() +
                ", Tipo: " + tipo + ", Liana: " + lianaNum +
                ", X: " + x + ", Altura: " + altura);
    }

    /**
     * Crea una fruta en una posición específica (comando ADMIN)
     * @param tipo "BANANA", "NARANJA" o "CEREZA"
     * @param lianaNum Número de liana (1-9) ← CAMBIO: antes era 1-6
     * @param altura Altura en píxeles (0-540)
     * @param puntos Puntos que otorga (10-100)
     */
    public void crearFrutaAdmin(String tipo, int lianaNum, int altura, int puntos) {

        // Validar rango 1-9
        if (lianaNum < 1 || lianaNum > 9) {
            System.out.println("[GameManager] Error: liana inválida " + lianaNum);
            return;
        }

        // USAR getLianaX()
        double x = GameConstants.getLianaX(lianaNum);
        double y = (double) altura;

        Posicion pos = new Posicion(x, y);

        TipoFruta tipoFruta;
        switch (tipo) {
            case "BANANA":
                tipoFruta = TipoFruta.BANANA;
                break;
            case "NARANJA":
                tipoFruta = TipoFruta.NARANJA;
                break;
            case "CEREZA":
                tipoFruta = TipoFruta.CEREZA;
                break;
            default:
                System.out.println("[GameManager] Error: tipo de fruta inválido " + tipo);
                return;
        }

        Fruta fruta = factory.crearFruta(tipoFruta, pos);
        fruta.setPuntos(puntos);

        // Asignar liana
        if (lianaNum >= 1 && lianaNum <= state.getLianas().size()) {
            try {
                fruta.setLiana(state.getLianas().get(lianaNum - 1));
            } catch (Exception e) {
                System.out.println("[GameManager] Advertencia: No se pudo asignar liana a la fruta");
            }
        }

        state.getFrutas().add(fruta);

        System.out.println("[GameManager] Fruta creada → ID: " + fruta.getId() +
                ", Tipo: " + tipo + ", Liana: " + lianaNum +
                ", X: " + x + ", Altura: " + altura + ", Puntos: " + puntos);
    }

    /**
     * Elimina una fruta en una posición específica (comando ADMIN)
     * @param lianaNum Número de liana (0-8)
     * @param altura Altura en píxeles (0-540)
     * @return true si se eliminó, false si no se encontró
     */
    public boolean eliminarFrutaAdmin(int lianaNum, int altura) {
        // Validar rango 0-8
        if (lianaNum < 0 || lianaNum > 8) {
            System.out.println("[GameManager] Error: liana inválida " + lianaNum);
            return false;
        }

        // Convertir índice 0-8 a 1-9 para getLianaX
        double targetX = GameConstants.getLianaX(lianaNum + 1);
        double targetY = (double) altura;
        double tolerance = 30.0;

        for (int i = 0; i < state.getFrutas().size(); i++) {
            Fruta fruta = state.getFrutas().get(i);
            Posicion pos = fruta.getPosicion();

            if (Math.abs(pos.x - targetX) < tolerance &&
                    Math.abs(pos.y - targetY) < tolerance) {

                Integer frutaId = fruta.getId();
                state.getFrutas().remove(i);

                System.out.println("[GameManager] Fruta eliminada → ID: " + frutaId +
                        ", Liana: " + lianaNum + ", X: " + targetX + ", Altura: " + altura);
                return true;
            }
        }

        System.out.println("[GameManager] No se encontró fruta en liana " + lianaNum +
                " altura " + altura);
        return false;
    }

    /**
     * Crea un cocodrilo (versión para AdminConsole con tipos enumerados)
     * @param tipo Tipo de cocodrilo (TipoCocodrilo.ROJO o TipoCocodrilo.AZUL)
     * @param lianaId Número de liana (1-6)
     * @param altura Altura en píxeles (0-540)
     * @return true si se creó exitosamente, false si hubo error
     */
    public Boolean crearCocodrilo(TipoCocodrilo tipo, Integer lianaId, Double altura) {
        //  Validar liana 1-9
        if (lianaId < 1 || lianaId > 9) {
            System.out.println("[GameManager] Error: liana inválida " + lianaId);
            return false;
        }

        // Validar altura
        if (altura < 0 || altura > 540) {
            System.out.println("[GameManager] Error: altura inválida " + altura);
            return false;
        }

        try {
            double x = GameConstants.getLianaX(lianaId);
            double y = altura;
            Posicion pos = new Posicion(x, y);

            // Crear cocodrilo usando el factory
            Cocodrilo croc = factory.crearCocodrilo(tipo, pos);

            // Asignar liana
            if (lianaId >= 1 && lianaId <= state.getLianas().size()) {
                croc.setLiana(state.getLianas().get(lianaId - 1));
            }

            state.getCocodrilos().add(croc);

            System.out.println("[GameManager] Cocodrilo creado → ID: " + croc.getId() +
                    ", Tipo: " + tipo + ", Liana: " + lianaId +
                    ", X: " + x + ", Altura: " + altura);

            return true;

        } catch (Exception e) {
            System.out.println("[GameManager] Error al crear cocodrilo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un cocodrilo por su ID
     * @param id ID del cocodrilo a eliminar
     * @return true si se eliminó, false si no se encontró
     */
    public Boolean eliminarCocodrilo(Integer id) {
        try {
            for (int i = 0; i < state.getCocodrilos().size(); i++) {
                Cocodrilo croc = state.getCocodrilos().get(i);
                if (croc.getId().equals(id)) {
                    state.getCocodrilos().remove(i);
                    System.out.println("[GameManager] Cocodrilo eliminado → ID: " + id);
                    return true;
                }
            }
            System.out.println("[GameManager] Error: Cocodrilo con ID " + id + " no encontrado");
            return false;

        } catch (Exception e) {
            System.out.println("[GameManager] Error al eliminar cocodrilo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea una fruta (versión para AdminConsole con tipos enumerados)
     * @param tipo Tipo de fruta (TipoFruta.BANANA, NARANJA, CEREZA)
     * @param lianaId Número de liana (1-6)
     * @param altura Altura en píxeles (0-540)
     * @return true si se creó exitosamente, false si hubo error
     */
    public Boolean crearFruta(TipoFruta tipo, Integer lianaId, Double altura) {
        // Validar liana 1-9
        if (lianaId < 1 || lianaId > 9) {
            System.out.println("[GameManager] Error: liana inválida " + lianaId);
            return false;
        }

        // Validar altura
        if (altura < 0 || altura > 540) {
            System.out.println("[GameManager] Error: altura inválida " + altura);
            return false;
        }

        try {
            double x = GameConstants.getLianaX(lianaId);
            double y = altura;
            Posicion pos = new Posicion(x, y);

            // Crear fruta usando el factory
            Fruta fruta = factory.crearFruta(tipo, pos);

            // Asignar liana
            if (lianaId >= 1 && lianaId <= state.getLianas().size()) {
                fruta.setLiana(state.getLianas().get(lianaId - 1));
            }

            state.getFrutas().add(fruta);

            System.out.println("[GameManager] Fruta creada → ID: " + fruta.getId() +
                    ", Tipo: " + tipo + ", Liana: " + lianaId +
                    ", X: " + x + ", Altura: " + altura + ", Puntos: " + fruta.getPuntos());

            return true;

        } catch (Exception e) {
            System.out.println("[GameManager] Error al crear fruta: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una fruta por posición (liana + altura)
     * @param lianaId Número de liana (1-6)
     * @param altura Altura en píxeles (0-540)
     * @return true si se eliminó, false si no se encontró
     */
    public Boolean eliminarFruta(Integer lianaId, Double altura) {
        double[] lianasX = {20.0, 100.0, 220.0, 370.0, 480.0, 570.0,660.0, 760.0, 890.0};

        // Validar liana
        if (lianaId < 1 || lianaId > 9) {
            System.out.println("[GameManager] Error: liana inválida " + lianaId);
            return false;
        }

        try {
            double targetX = lianasX[lianaId - 1];
            double targetY = altura;
            double tolerance = 30.0; // Tolerancia de 30 píxeles

            for (int i = 0; i < state.getFrutas().size(); i++) {
                Fruta fruta = state.getFrutas().get(i);
                Posicion pos = fruta.getPosicion();

                // Verificar si la fruta está cerca de la posición objetivo
                if (Math.abs(pos.x - targetX) < tolerance &&
                        Math.abs(pos.y - targetY) < tolerance) {

                    Integer frutaId = fruta.getId();
                    state.getFrutas().remove(i);

                    System.out.println("[GameManager] Fruta eliminada → ID: " + frutaId +
                            ", Liana: " + lianaId + ", Altura: " + altura);

                    return true;
                }
            }

            System.out.println("[GameManager] Error: No se encontró fruta en Liana " +
                    lianaId + ", Altura " + altura);
            return false;

        } catch (Exception e) {
            System.out.println("[GameManager] Error al eliminar fruta: " + e.getMessage());
            return false;
        }
    }

    /* =========================================================
       UTILIDADES
       ========================================================= */

    public void listarEntidades() {
        System.out.println("\n=== ENTIDADES ACTIVAS ===");
        System.out.println("-- Cocodrilos (" + state.getCocodrilos().size() + ") --");
        for (Cocodrilo c : state.getCocodrilos()) {
            System.out.println("  " + c);
        }
        System.out.println("-- Frutas (" + state.getFrutas().size() + ") --");
        for (Fruta f : state.getFrutas()) {
            if (f.isActiva()) {
                System.out.println("  " + f);
            }
        }
        System.out.println("-- Lianas (" + state.getLianas().size() + ") --");
        for (Liana l : state.getLianas()) {
            System.out.println("  " + l);
        }
        System.out.println("=========================\n");
    }

    public GameState getState() {
        return state;
    }

    public void registerPlayer(int clientId, String name) {
        connectedPlayers.put(clientId, name);
    }

    public void unregisterPlayer(int clientId) {
        connectedPlayers.remove(clientId);
    }

    public String getConnectedPlayersJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Map.Entry<Integer, String> e : connectedPlayers.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"id\":").append(e.getKey())
                    .append(",\"name\":\"").append(e.getValue()).append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    public Integer getSpectatorCount() {
        int totalObservers = observable.getObserverCount();
        return Math.max(0, totalObservers - 1);
    }
}

