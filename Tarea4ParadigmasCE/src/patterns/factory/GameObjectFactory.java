package patterns.factory;

import entities.Cocodrilo;
import entities.Fruta;
import model.Liana;
import model.Posicion;
import utils.TipoCocodrilo;
import utils.TipoFruta;

/**
 * FabricaEntidades (Abstract Factory)
 * -----------------------------------------------------
 * Interfaz que define los métodos para crear entidades del juego.
 * Patrón Abstract Factory para la creación de objetos relacionados.
 */
public interface GameObjectFactory {

    /**
     * Crea un cocodrilo del tipo especificado.
     * @param tipo Tipo de cocodrilo (ROJO o AZUL)
     * @param posicion Posición inicial del cocodrilo
     * @return Cocodrilo creado
     */
    Cocodrilo crearCocodrilo(TipoCocodrilo tipo, Posicion posicion);

    /**
     * Crea un cocodrilo del tipo especificado en una liana.
     * @param tipo Tipo de cocodrilo (ROJO o AZUL)
     * @param liana Liana donde se colocará el cocodrilo
     * @param altura Altura en la liana (coordenada Y)
     * @return Cocodrilo creado
     */
    Cocodrilo crearCocodriloEnLiana(TipoCocodrilo tipo, Liana liana, Double altura);

    /**
     * Crea una fruta del tipo especificado.
     * @param tipo Tipo de fruta (BANANA, NARANJA, CEREZA)
     * @param posicion Posición de la fruta
     * @return Fruta creada
     */
    Fruta crearFruta(TipoFruta tipo, Posicion posicion);

    /**
     * Crea una fruta en una liana específica.
     * @param tipo Tipo de fruta
     * @param liana Liana donde se colocará
     * @param altura Altura en la liana
     * @return Fruta creada
     */
    Fruta crearFrutaEnLiana(TipoFruta tipo, Liana liana, Double altura);
}