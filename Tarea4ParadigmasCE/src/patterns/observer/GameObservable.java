package patterns.observer;

import java.util.concurrent.CopyOnWriteArrayList;

public class GameObservable {
    private final CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<>();

    public void addObserver(Observer o) { observers.add(o); }
    public void removeObserver(Observer o) { observers.remove(o); }

    public void notifyObservers(String gameState) {
        for (Observer o : observers) o.update(gameState);
    }
}
