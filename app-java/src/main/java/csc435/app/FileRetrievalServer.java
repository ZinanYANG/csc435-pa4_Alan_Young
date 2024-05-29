package csc435.app;

public class FileRetrievalServer {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java FileRetrievalServer <IP address> <port> <num workers>");
            System.exit(1);
        }
        String ipAddress = args[0];
        int port = Integer.parseInt(args[1]);
        int numWorkers = Integer.parseInt(args[2]);
        IndexStore store = new IndexStore();
        ServerSideEngine engine = new ServerSideEngine(store, port, numWorkers);
        ServerAppInterface appInterface = new ServerAppInterface(engine);
        engine.initialize();
        appInterface.readCommands();
    }
}
