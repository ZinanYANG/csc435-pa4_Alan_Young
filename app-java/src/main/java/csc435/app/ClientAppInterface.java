package csc435.app;

import java.lang.System;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ClientAppInterface {
    private ClientSideEngine engine;
    private static final Logger LOGGER = Logger.getLogger(ClientAppInterface.class.getName());

    public ClientAppInterface(ClientSideEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> ");
            command = sc.nextLine();
            System.out.println("Received command: " + command); // Added for debugging

            try {
            	 if (command.equals("quit")) {
                	System.out.println("Quitting application on user command."); // Added for debugging
                	engine.closeConnection();
                	break;
            	}
                if (command.startsWith("connect ")) {
                    String[] parts = command.split("\\s+");
                    if (parts.length == 3) {
                        String address = parts[1];
                        int port = Integer.parseInt(parts[2]);
                        engine.openConnection(address, port); 
                    }
                } else if (command.startsWith("index ")) {
                    	String[] parts = command.split("\\s+", 2);
            		if (parts.length == 2) {
                		String directoryPath = parts[1];
                		System.out.println("Attempting to index directory: " + directoryPath); 
                		engine.indexFiles(directoryPath);
            		} else {
                		LOGGER.warning("Invalid index command format. Usage: index <directoryPath>");
            		}
            
            
                } else if (command.startsWith("search ")) {
    		    String searchQuery = command.substring("search ".length());
    			if (!searchQuery.trim().isEmpty()) {
        			System.out.println("Searching for: " + searchQuery);
        			engine.searchFiles(searchQuery);
    			} else {
        			System.out.println("Search query cannot be empty.");
    			}
                } else {
                    System.out.println("Unrecognized command!");
                }
            } catch (Exception e) {
                System.out.println("Error processing command: " + command); 
            	e.printStackTrace(); 
            }
        } 

        sc.close();
        LOGGER.info("Scanner closed and application terminated.");
    } 
} 

