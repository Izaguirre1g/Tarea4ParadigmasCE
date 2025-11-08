package patterns.factory;

import entities.Cocodrilo;
import entities.CocodriloAzul;
import entities.CocodriloRojo;
import model.Liana;
import utils.TipoCocodrilo;

/**
 * Fábrica concreta para crear cocodrilos.
 * NO crea frutas (retorna null en ese método).
 */
public class CrocodileFactory extends GameObjectFactory {

    @Override
    public Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Liana liana, Integer altura) {
        switch (tipo) {
            case ROJO: {
                CocodriloRojo rojo = new CocodriloRojo();
                rojo.setLianaActual(liana);
                rojo.setAltura(altura);
                rojo.setVelocidad(1.0);     // valor por defecto
                rojo.setTipo(TipoCocodrilo.ROJO);
                return rojo;
            }
            case AZUL: {
                CocodriloAzul azul = new CocodriloAzul();
                azul.setLianaActual(liana);
                azul.setAltura(altura);
                azul.setVelocidad(1.2);     // valor por defecto
                azul.setTipo(TipoCocodrilo.AZUL);
                return azul;
            }
            default:
                throw new IllegalArgumentException("Tipo de cocodrilo no válido: " + tipo);
        }
    }

    @Override
    public entities.Fruta crearFruta(utils.TipoFruta tipo, Liana liana, Integer altura, Integer puntos) {
        // Esta fábrica solo crea cocodrilos.
        return null;
    }
}

