package csc435.app;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.logging.*;
import java.nio.charset.StandardCharsets;

public class Dispatcher implements Runnable {
    private ServerSideEngine engine;
    private ZContext context;
    private ZMQ.Socket routerSocket;
    private int port;
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
    private ZMQ.Socket sender;

    public Dispatcher(ServerSideEngine engine, int port, ZContext context, String inprocAddress) {
        this.engine = engine;
        this.port = port;
        this.context = new ZContext();
        this.sender = context.createSocket(ZMQ.PUSH);
        this.sender.bind(inprocAddress);
    }

    @Override
    public void run() {
        try {
            routerSocket = context.createSocket(ZMQ.ROUTER);
            routerSocket.bind("tcp://*:" + port);
            LOGGER.info("Dispatcher listening on port: " + port);
            System.out.println("Dispatcher is now listening on port: " + port); 

            while (!Thread.currentThread().isInterrupted()) {
                ZMQ.Poller poller = context.createPoller(1);
                poller.register(routerSocket, ZMQ.Poller.POLLIN);

                if (poller.poll(1000) == -1) break; 

                if (poller.pollin(0)) {
                    handleClientRequest();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception in Dispatcher", e);
        } finally {
            closeResources();
        }
    }

    private void handleClientRequest() {
    byte[] clientIdentity = routerSocket.recv(0);
    routerSocket.recv(0); 
    byte[] message = routerSocket.recv(0);
    
    String content = new String(message, StandardCharsets.UTF_8);
    System.out.println("Dispatcher received message: " + content);
    
    
    if (content.startsWith("search ")) {
        System.out.println("Dispatcher forwarding search request to workers: " + content);
        sender.send(content);
        routerSocket.sendMore(clientIdentity);
        routerSocket.sendMore("");
        routerSocket.send("Search request received and is being processed.".getBytes(StandardCharsets.UTF_8));
    } 
    else if (content.startsWith("index ")) {
            	System.out.println("Dispatcher forwarding index request to workers: " + content);
            	sender.send(content);         
        	routerSocket.sendMore(clientIdentity);
        	routerSocket.sendMore("");
        	routerSocket.send("Index request received and is being processed.".getBytes(StandardCharsets.UTF_8));
        	System.out.println("Acknowledgement sent to client for index request.");
        }
    
     else if (content.startsWith("connect ")) {
        String[] parts = content.split(" "); 
        if (parts.length > 2) {
            String clientIP = parts[1];
            String uniqueID = parts[2]; 
            String clientKey = clientIP + "-" + uniqueID; 
            System.out.println("Processing connect request from IP: " + clientIP);
            routerSocket.sendMore(clientIdentity);
            routerSocket.sendMore("");
            routerSocket.send("ACK".getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent ACK to IP: " + clientIP);
            engine.addClient(clientKey, clientIP);
        } else {
            System.out.println("Connect command format is incorrect. Expected format: connect <IP> <UID>");
        }
    } else {
        System.out.println("Dispatcher received unrecognized command: " + content);
    }
   
}

    private void closeResources() {
        if (routerSocket != null) {
            routerSocket.close();
        }
        if (context != null) {
            context.close();
        }
    }
}
