package server;

import utils.TipoCocodrilo;
import utils.TipoFruta;

import java.util.Scanner;

/**
 * AdminConsole
 * -----------------------------------------------------
 * Consola administrativa para gestionar entidades del juego.
 * Permite crear/eliminar cocodrilos y frutas en tiempo real.
 */
public class AdminConsole implements Runnable {

    private final GameManager manager;
    private final Scanner scanner;
    private Boolean running;

    public AdminConsole(GameManager manager) {
        this.manager = manager;
        this.scanner = new Scanner(System.in);
        this.running = Boolean.TRUE;
    }

    @Override
    public void run() {

        System.out.println("\n=== CONSOLA ADMINISTRATIVA ===");
        System.out.println("Comandos disponibles:");
        System.out.println("  crearcroc <tipo> <lianaId> <altura>");
        System.out.println("  eliminarcroc <id>");
        System.out.println("  crearfruta <tipo> <lianaId> <altura>");
        System.out.println("  eliminarfruta <lianaId> <altura>");
        System.out.println("  listar");
        System.out.println("  modo <TEXT/JSON>");
        System.out.println("  exit");
        System.out.println("================================\n");

        while (running) {

            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "crearcroc":        handleCrearCroc(parts); break;
                    case "eliminarcroc":     handleEliminarCroc(parts); break;
                    case "crearfruta":       handleCrearFruta(parts); break;
                    case "eliminarfruta":    handleEliminarFruta(parts); break;
                    case "listar":           manager.listarEntidades(); break;
                    case "modo":             handleCambiarModo(parts); break;

                    case "exit":
                        running = Boolean.FALSE;
                        System.out.println("Cerrando consola administrativa...");
                        break;

                    default:
                        System.out.println("Comando desconocido: " + command);
                }
            }
            catch (Exception e) {
                System.out.println("Error ejecutando comando: " + e.getMessage());
            }
        }
    }

    /**
     * crearcroc <tipo> <lianaId> <altura>
     */
    private void handleCrearCroc(String[] parts) {

        if (parts.length < 4) {
            System.out.println("Uso correcto:");
            System.out.println("  crearcroc <ROJO/AZUL> <lianaId> <altura>");
            return;
        }

        String tipoStr = parts[1].toUpperCase();
        Integer lianaId = Integer.parseInt(parts[2]);
        Double altura = Double.parseDouble(parts[3]);

        TipoCocodrilo tipo;
        switch (tipoStr) {
            case "ROJO": tipo = TipoCocodrilo.ROJO; break;
            case "AZUL": tipo = TipoCocodrilo.AZUL; break;
            default:
                System.out.println("Tipo inválido. Use ROJO o AZUL");
                return;
        }

        Boolean resultado = manager.crearCocodrilo(tipo, lianaId, altura);

        if (resultado)
            System.out.println("✅ Cocodrilo creado exitosamente");
        else
            System.out.println("❌ Error al crear cocodrilo");
    }

    /**
     * eliminarcroc <id>
     */
    private void handleEliminarCroc(String[] parts) {

        if (parts.length < 2) {
            System.out.println("Uso: eliminarcroc <id>");
            return;
        }

        Integer id = Integer.parseInt(parts[1]);
        Boolean resultado = manager.eliminarCocodrilo(id);

        if (resultado)
            System.out.println("✅ Cocodrilo eliminado");
        else
            System.out.println("❌ Error: ID no encontrado");
    }

    /**
     * crearfruta <tipo> <lianaId> <altura>
     */
    private void handleCrearFruta(String[] parts) {

        if (parts.length < 4) {
            System.out.println("Uso: crearfruta <BANANA/NARANJA/CEREZA> <lianaId> <altura>");
            return;
        }

        String tipoStr = parts[1].toUpperCase();
        Integer lianaId = Integer.parseInt(parts[2]);
        Double altura = Double.parseDouble(parts[3]);

        TipoFruta tipo;

        switch (tipoStr) {
            case "BANANA":  tipo = TipoFruta.BANANA; break;
            case "NARANJA": tipo = TipoFruta.NARANJA; break;
            case "CEREZA":  tipo = TipoFruta.CEREZA; break;
            default:
                System.out.println("Tipo inválido. Use BANANA, NARANJA o CEREZA");
                return;
        }

        Boolean resultado = manager.crearFruta(tipo, lianaId, altura);

        if (resultado)
            System.out.println("✅ Fruta creada");
        else
            System.out.println("❌ Error al crear fruta");
    }

    /**
     * eliminarfruta <lianaId> <altura>
     */
    private void handleEliminarFruta(String[] parts) {

        if (parts.length < 3) {
            System.out.println("Uso: eliminarfruta <lianaId> <altura>");
            return;
        }

        Integer lianaId = Integer.parseInt(parts[1]);
        Double altura = Double.parseDouble(parts[2]);

        Boolean resultado = manager.eliminarFruta(lianaId, altura);

        if (resultado)
            System.out.println("✅ Fruta eliminada");
        else
            System.out.println("❌ Error al eliminar fruta");
    }

    /**
     * modo <TEXT/JSON>
     */
    private void handleCambiarModo(String[] parts) {

        if (parts.length < 2) {
            System.out.println("Uso: modo <TEXT/JSON>");
            return;
        }

        String modoStr = parts[1].toUpperCase();

        try {
            GameManager.CommunicationMode newMode =
                    GameManager.CommunicationMode.valueOf(modoStr);

            manager.setCommunicationMode(newMode);
            System.out.println("Modo cambiado a " + newMode);

        } catch (Exception e) {
            System.out.println("Modo inválido. Use TEXT o JSON");
        }
    }
}


