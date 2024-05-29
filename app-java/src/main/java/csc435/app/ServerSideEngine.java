package csc435.app;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.math.BigInteger;
import java.util.*;

public class ServerSideEngine {
    private final IndexStore store;
    private final int port;
    private final int numWorkers;
    private ZContext context;
    private ExecutorService workerPool;
    private Dispatcher dispatcher;
    private Map<String, String> clientAddresses;
    private static final Logger LOGGER = Logger.getLogger(ServerSideEngine.class.getName());
    private Set<Thread> workerThreads = new HashSet<>();
    private volatile boolean shutdownRequested = false;

    public ServerSideEngine(IndexStore store, int port, int numWorkers) {
        this.store = store;
        this.port = port;
        this.numWorkers = numWorkers;
        this.context = new ZContext();
        this.clientAddresses = new ConcurrentHashMap<>(); // Thread-safe set of client identities
    }
    
    
    public void initialize() {
        this.context = new ZContext();
        String inprocAddress = "inproc://workers";
        String outprocAddress = "inproc://workersToDispatcher"; 
        this.dispatcher = new Dispatcher(this, this.port, context, inprocAddress);
        Thread dispatcherThread = new Thread(this.dispatcher);
        dispatcherThread.start();

        // Initialize the worker threads
        for (int i = 0; i < this.numWorkers; i++) {
            Worker worker = new Worker(context, store, inprocAddress);
            Thread workerThread = new Thread(worker);
            workerThreads.add(workerThread);
            workerThread.start();
        }
    }
    public void shutdown() {
        shutdownRequested = true; 

        // Wait for all worker threads to complete
        for (Thread workerThread : workerThreads) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted while waiting for worker threads to finish.");
            }
        }

        if (this.context != null) {
            this.context.close();
        }

        System.out.println("Server shutdown completed.");
    }

    public void listActiveWorkers() {
        
    }
     public boolean isShutdownRequested() {
        return shutdownRequested;
    }


    public void listConnectedClients() {
    System.out.println("Listing connected clients:");
    int numberOfClients = clientAddresses.size();
    System.out.println("Number of connected clients: " + numberOfClients);
    if (numberOfClients > 0) {
        clientAddresses.forEach((key, value) -> System.out.println("IP Address: " + value));
    } else {
        System.out.println("No clients connected.");
    }
}



    public void addClient(String clientKey, String ipAddress) {
        clientAddresses.put(clientKey, ipAddress);
            System.out.println("Client connected: IP Address = " + ipAddress);
    }
    public void removeClient(String identity) {
        clientAddresses.remove(identity);
        LOGGER.info("Client removed: " + identity);
    }
}
