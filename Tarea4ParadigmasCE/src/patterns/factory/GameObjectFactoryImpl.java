package patterns.factory;

import entities.*;
import model.Liana;
import model.Posicion;
import utils.TipoCocodrilo;
import utils.TipoFruta;

/**
 * FabricaEntidadesImpl
 * -----------------------------------------------------
 * Implementación concreta del patrón Abstract Factory.
 * Crea todas las entidades del juego (cocodrilos y frutas).
 */
public class GameObjectFactoryImpl implements GameObjectFactory {

    @Override
    public Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Posicion posicion) {
        Cocodrilo cocodrilo = null;

        switch (tipo) {
            case ROJO:
                cocodrilo = new CocodriloRojo(posicion);
                break;
            case AZUL:
                cocodrilo = new CocodriloAzul(posicion);
                break;
        }

        if (cocodrilo != null) {
            System.out.println("[Factory] Cocodrilo creado: " + tipo + " en posición " + posicion);
        }

        return cocodrilo;
    }

    @Override
    public Cocodrilo crearCocodriloEnLiana(TipoCocodrilo tipo, Liana liana, Double altura) {
        if (liana == null) {
            throw new IllegalArgumentException("La liana no puede ser nula");
        }

        // Validar que la altura esté dentro del rango de la liana
        Double minY = liana.getPosicionInicio().y;
        Double maxY = liana.getPosicionFin().y;

        if (altura < minY || altura > maxY) {
            throw new IllegalArgumentException(
                    String.format("Altura %.0f fuera del rango de la liana [%.0f, %.0f]",
                            altura, minY, maxY)
            );
        }

        // Crear posición en la liana
        Double x = liana.getPosicionInicio().x;
        Posicion posicion = new Posicion(x, altura);

        return crearCocodrilo(tipo, posicion);
    }

    @Override
    public Fruta crearFruta(TipoFruta tipo, Posicion posicion) {
        Fruta fruta = new Fruta();
        fruta.setTipo(tipo);
        fruta.setPosicion(posicion);
        fruta.setActiva(Boolean.TRUE);

        System.out.println("[Factory] Fruta creada: " + tipo + " en posición " + posicion);

        return fruta;
    }

    @Override
    public Fruta crearFrutaEnLiana(TipoFruta tipo, Liana liana, Double altura) {
        if (liana == null) {
            throw new IllegalArgumentException("La liana no puede ser nula");
        }

        // Validar que la altura esté dentro del rango de la liana
        Double minY = liana.getPosicionInicio().y;
        Double maxY = liana.getPosicionFin().y;

        if (altura < minY || altura > maxY) {
            throw new IllegalArgumentException(
                    String.format("Altura %.0f fuera del rango de la liana [%.0f, %.0f]",
                            altura, minY, maxY)
            );
        }

        // Crear posición en la liana
        Double x = liana.getPosicionInicio().x;
        Posicion posicion = new Posicion(x, altura);

        Fruta fruta = crearFruta(tipo, posicion);
        fruta.setLiana(liana);

        return fruta;
    }
}