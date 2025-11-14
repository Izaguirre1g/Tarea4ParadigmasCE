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
        System.out.println("  crearcroc <tipo> <lianaId>  - Crea cocodrilo (tipo: ROJO/AZUL)");
        System.out.println("  eliminarcroc <id>           - Elimina cocodrilo por ID");
        System.out.println("  crearfruta <tipo> <lianaId> <altura> - Crea fruta (tipo: BANANA/NARANJA/CEREZA)");
        System.out.println("  eliminarfruta <lianaId> <altura>     - Elimina fruta por posición");
        System.out.println("  listar                      - Lista todas las entidades");
        System.out.println("  modo <TEXT/JSON>            - Cambia modo de comunicación");
        System.out.println("  exit                        - Salir");
        System.out.println("==============================\n");

        while (running) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "crearcroc":
                        handleCrearCroc(parts);
                        break;
                    case "eliminarcroc":
                        handleEliminarCroc(parts);
                        break;
                    case "crearfruta":
                        handleCrearFruta(parts);
                        break;
                    case "eliminarfruta":
                        handleEliminarFruta(parts);
                        break;
                    case "listar":
                        manager.listarEntidades();
                        break;
                    case "modo":
                        handleCambiarModo(parts);
                        break;
                    case "exit":
                        running = Boolean.FALSE;
                        System.out.println("Cerrando consola administrativa...");
                        break;
                    default:
                        System.out.println("Comando desconocido: " + command);
                }
            } catch (Exception e) {
                System.out.println("Error ejecutando comando: " + e.getMessage());
            }
        }
    }

    private void handleCrearCroc(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Uso: crearcroc <tipo> <lianaId>");
            return;
        }

        String tipoStr = parts[1].toUpperCase();
        Integer lianaId = Integer.parseInt(parts[2]);

        TipoCocodrilo tipo = null;
        if (tipoStr.equals("ROJO")) {
            tipo = TipoCocodrilo.ROJO;
        } else if (tipoStr.equals("AZUL")) {
            tipo = TipoCocodrilo.AZUL;
        } else {
            System.out.println("Tipo inválido. Use ROJO o AZUL");
            return;
        }

        Boolean resultado = manager.crearCocodrilo(tipo, lianaId);
        if (resultado) {
            System.out.println("✅ Cocodrilo creado exitosamente");
        } else {
            System.out.println("❌ Error al crear cocodrilo");
        }
    }

    private void handleEliminarCroc(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: eliminarcroc <id>");
            return;
        }

        Integer id = Integer.parseInt(parts[1]);
        Boolean resultado = manager.eliminarCocodrilo(id);

        if (resultado) {
            System.out.println("✅ Cocodrilo eliminado exitosamente");
        } else {
            System.out.println("❌ Error al eliminar cocodrilo");
        }
    }

    private void handleCrearFruta(String[] parts) {
        if (parts.length < 4) {
            System.out.println("Uso: crearfruta <tipo> <lianaId> <altura>");
            return;
        }

        String tipoStr = parts[1].toUpperCase();
        Integer lianaId = Integer.parseInt(parts[2]);
        Double altura = Double.parseDouble(parts[3]);

        TipoFruta tipo = null;
        if (tipoStr.equals("BANANA")) {
            tipo = TipoFruta.BANANA;
        } else if (tipoStr.equals("NARANJA")) {
            tipo = TipoFruta.NARANJA;
        } else if (tipoStr.equals("CEREZA")) {
            tipo = TipoFruta.CEREZA;
        } else {
            System.out.println("Tipo inválido. Use BANANA, NARANJA o CEREZA");
            return;
        }

        Boolean resultado = manager.crearFruta(tipo, lianaId, altura);
        if (resultado) {
            System.out.println("✅ Fruta creada exitosamente");
        } else {
            System.out.println("❌ Error al crear fruta");
        }
    }

    private void handleEliminarFruta(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Uso: eliminarfruta <lianaId> <altura>");
            return;
        }

        Integer lianaId = Integer.parseInt(parts[1]);
        Double altura = Double.parseDouble(parts[2]);

        Boolean resultado = manager.eliminarFruta(lianaId, altura);

        if (resultado) {
            System.out.println("✅ Fruta eliminada exitosamente");
        } else {
            System.out.println("❌ Error al eliminar fruta");
        }
    }

    private void handleCambiarModo(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Uso: modo <TEXT/JSON>");
            return;
        }

        String modoStr = parts[1].toUpperCase();
        if (modoStr.equals("TEXT")) {
            manager.setCommunicationMode(GameManager.CommunicationMode.TEXT);
            System.out.println("✅ Modo cambiado a TEXT");
        } else if (modoStr.equals("JSON")) {
            manager.setCommunicationMode(GameManager.CommunicationMode.JSON);
            System.out.println("✅ Modo cambiado a JSON");
        } else {
            System.out.println("Modo inválido. Use TEXT o JSON");
        }
    }
}