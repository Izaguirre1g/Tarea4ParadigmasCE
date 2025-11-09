package server;

import entities.Cocodrilo;
import entities.CocodriloRojo;
import model.Liana;
import network.ClientHandler;
import utils.TipoCocodrilo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GameManager {
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<Cocodrilo> crocs = new ArrayList<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public GameManager() {
        // Crear una liana de ejemplo (posición x=100)
        Liana l = new Liana(100);
        // Crear un cocodrilo rojo que se mueve entre y=100 y y=400
        CocodriloRojo c = new CocodriloRojo(0, l, 100, 400);
        crocs.add(c);

        executor.scheduleAtFixedRate(this::tick, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void addClient(ClientHandler c) {
        clients.add(c);
    }

    public void removeClient(ClientHandler c) {
        clients.remove(c);
    }

    private void tick() {
        double delta = 0.1;
        for (Cocodrilo c : crocs) {
            c.update(delta);
        }

        broadcastState();
    }

    private void broadcastState() {
        for (ClientHandler ch : clients) {
            for (Cocodrilo c : crocs) {
                String msg = String.format(
                        "CROC %d type=%s liana=%d x=%.0f y=%.0f alive=%d",
                        c.getId(), c.getTipo(), c.getLiana().getId(),
                        c.getPosicion().getX(), c.getPosicion().getY(),
                        c.isVivo() ? 1 : 0
                );
                ch.sendLine(msg);
            }
        }
    }
}
