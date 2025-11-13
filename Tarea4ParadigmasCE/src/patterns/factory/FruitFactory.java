package patterns.factory;

import entities.Cocodrilo;
import entities.Fruta;
import entities.Banana;
import entities.Naranja;
import model.Liana;
import model.Posicion;
import utils.TipoCocodrilo;
import utils.TipoFruta;

/**
 * Fábrica concreta encargada de crear frutas (power-ups)
 * ------------------------------------------------------
 * Implementa el método crearFruta() según el tipo de fruta (BANANA o NARANJA).
 * Aplica el patrón Abstract Factory.
 *
 * ✅ Sin tipos primitivos (usa Integer, Double, Boolean)
 * ✅ Cumple con las firmas de GameObjectFactory
 * ✅ Compatible con GameManager
 */
public class FruitFactory extends GameObjectFactory {

    /**
     * Esta fábrica no crea cocodrilos. Se implementa para cumplir con la clase base.
     */
    @Override
    public Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Liana liana, Integer altura) {
        // No aplica para frutas
        return null;
    }

    /**
     * Crea una fruta del tipo especificado.
     * @param tipo Tipo de fruta (BANANA, NARANJA)
     * @param liana Liana a la que pertenece
     * @param altura Altura sobre la base
     * @param puntos Puntaje que otorga
     * @return Nueva instancia de Fruta o null si el tipo es desconocido
     */
    @Override
    public Fruta crearFruta(TipoFruta tipo, Liana liana, Integer altura, Integer puntos) {
        if (tipo == null || liana == null || altura == null || puntos == null) {
            return null;
        }

        Fruta fruta;

        switch (tipo) {
            case BANANA:
                fruta = new Banana();
                fruta.setPuntos(puntos != null ? puntos : 100);
                break;

            case NARANJA:
                fruta = new Naranja();
                fruta.setPuntos(puntos != null ? puntos : 200);
                break;

            default:
                return null;
        }

        fruta.setTipo(tipo);
        fruta.setLiana(liana);
        fruta.setAlturaEnLiana(altura);
        fruta.setId(0); // Se puede asignar luego desde GameManager

        System.out.println("[Factory] Fruta creada: " + tipo + " en liana " + liana.getId());
        return fruta;
    }
}

