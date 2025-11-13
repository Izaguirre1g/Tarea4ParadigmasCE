package patterns.factory;

import entities.Cocodrilo;
import entities.CocodriloRojo;
import entities.CocodriloAzul;
import entities.Fruta;
import model.Liana;
import model.Posicion;
import utils.TipoCocodrilo;
import utils.TipoFruta;

/**
 * Fábrica concreta encargada de crear instancias de cocodrilos.
 * -------------------------------------------------------------
 * Implementa el método de creación de enemigos según su tipo
 * (ROJO o AZUL). Aplica el patrón Abstract Factory.
 *
 * ✅ Sin tipos primitivos: usa Integer, Double, Boolean.
 * ✅ Cumple con las firmas de GameObjectFactory.
 */
public class CrocodileFactory extends GameObjectFactory {

    @Override
    public Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Liana liana, Integer altura) {
        if (tipo == null || liana == null || altura == null) {
            return null;
        }

        Cocodrilo cocodrilo;

        switch (tipo) {
            case ROJO:
                cocodrilo = new CocodriloRojo();
                cocodrilo.setVelocidad(1.2); // Double (autoboxing)
                break;

            case AZUL:
                cocodrilo = new CocodriloAzul();
                cocodrilo.setVelocidad(1.5);
                break;

            default:
                return null;
        }

        cocodrilo.setTipo(tipo);
        cocodrilo.setLianaActual(liana);
        cocodrilo.setAltura(altura);

        // Si Liana no tiene coordenadas, usa (0, altura)
        cocodrilo.setPosicion(new Posicion(0, altura));

        System.out.println("[Factory] Cocodrilo creado: " + tipo + " en liana " + liana.getId());
        return cocodrilo;
    }

    @Override
    public Fruta crearFruta(TipoFruta tipo, Liana liana, Integer altura, Integer puntos) {
        // Esta fábrica no crea frutas
        return null;
    }
}



