package utils;

public enum TipoFruta {
    BANANA("Banana", 70, "banana.png"),
    NARANJA("Naranja", 100, "naranja.png"),
    CEREZA("Cereza", 50, "cereza.png");

    private final String nombre;
    private final Integer puntos;
    private final String sprite;

    TipoFruta(String nombre, Integer puntos, String sprite) {
        this.nombre = nombre;
        this.puntos = puntos;
        this.sprite = sprite;
    }

    public String getNombre() { return nombre; }
    public Integer getPuntos() { return puntos; }
    public String getSprite() { return sprite; }

    @Override
    public String toString() {
        return nombre;
    }
}