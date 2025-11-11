package patterns.factory;

import entities.Cocodrilo;
import entities.CocodriloAzul;
import entities.CocodriloRojo;
import model.Posicion;
import utils.TipoCocodrilo;
import utils.GameConstants;

/**
 * Factory que crea cocodrilos rojos o azules
 * usando el patrón Factory Method.
 */
public class CrocodileFactory {

    /**
     * Crea un cocodrilo según el tipo especificado.
     * @param tipo TipoCocodrilo (ROJO o AZUL)
     * @param pos  Posición inicial del cocodrilo
     * @return instancia de Cocodrilo con estrategia asignada
     */
    public Cocodrilo createCrocodile(TipoCocodrilo tipo, Posicion pos) {
        Cocodrilo c = null;

        switch (tipo) {
            case ROJO:
                c = new CocodriloRojo(pos);
                break;

            case AZUL:
                c = new CocodriloAzul(pos);
                break;
        }

        // Seguridad: velocidad y límites por defecto
        if (c != null) {
            if (c.getVelocidad() <= 0) c.setVelocidad(GameConstants.CROC_SPEED);
            if (c.getPosicion().y < GameConstants.CROC_MIN_Y)
                c.getPosicion().y = GameConstants.CROC_MIN_Y + 50;
        }

        return c;
    }
}
