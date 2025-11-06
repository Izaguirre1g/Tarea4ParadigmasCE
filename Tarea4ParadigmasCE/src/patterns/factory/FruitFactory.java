package patterns.factory;

import entities.Banana;
import entities.Fruta;
import entities.Naranja;
import model.Liana;
import utils.TipoFruta;

/**
 * Fábrica concreta para crear frutas.
 * NO crea cocodrilos (retorna null en ese método).
 */
public class FruitFactory extends GameObjectFactory {

    @Override
    public entities.Cocodrilo crearCocodrilo(utils.TipoCocodrilo tipo, Liana liana, Integer altura) {
        // Esta fábrica solo crea frutas.
        return null;
    }

    @Override
    public Fruta crearFruta(TipoFruta tipo, Liana liana, Integer altura, Integer puntos) {
        switch (tipo) {
            case BANANA: {
                Banana banana = new Banana();
                banana.setLiana(liana);
                banana.setAlturaEnLiana(altura);
                banana.setPuntos(puntos != null ? puntos : 100);
                banana.setTipo(TipoFruta.BANANA);
                return banana;
            }
            case NARANJA: {
                Naranja naranja = new Naranja();
                naranja.setLiana(liana);
                naranja.setAlturaEnLiana(altura);
                naranja.setPuntos(puntos != null ? puntos : 200);
                naranja.setTipo(TipoFruta.NARANJA);
                return naranja;
            }
            default:
                throw new IllegalArgumentException("Tipo de fruta no válido: " + tipo);
        }
    }
}
