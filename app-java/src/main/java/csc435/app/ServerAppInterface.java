package csc435.app;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ServerAppInterface {
    private ServerSideEngine engine;
    private static final Logger LOGGER = Logger.getLogger(ServerAppInterface.class.getName());

    public ServerAppInterface(ServerSideEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine();

                if ("quit".equals(command)) {
                    System.out.println("Shutting down server on command.");
                    engine.shutdown();
                    break;
                } else if (command.startsWith("list")) {
            	    engine.listConnectedClients();

                } else {
                    System.out.println("Unrecognized command: " + command);
                }
            }
        } catch (Exception e) {
        	System.out.println("Error in ServerAppInterface: " + e.getMessage());
        }
    }
}
