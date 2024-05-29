package csc435.app;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.logging.*;
import java.util.UUID;
import org.zeromq.ZMQException;
import org.zeromq.ZMQ.Socket;

public class ClientSideEngine {
    private ZContext context;
    private ZMQ.Socket socket;
    private String serverAddress;

    public ClientSideEngine() {
        this.context = new ZContext();
    }

    public void openConnection(String address, int port) {
        this.serverAddress = "tcp://" + address + ":" + port;
        this.socket = context.createSocket(ZMQ.REQ);
        socket.connect(serverAddress);       
        System.out.println("Connected to server at " + serverAddress);
    	String uniqueID = UUID.randomUUID().toString(); // Generate a unique ID
    	String connectMsg = "connect " + address + " " + uniqueID;  
    	socket.send(connectMsg.getBytes(ZMQ.CHARSET));    
     	System.out.println("Connect message sent to " + serverAddress);
    	String ack = socket.recvStr();
    	if (ack != null && ack.equals("ACK")) {
        	System.out.println("Connection acknowledged by server.");
    	} else {
        	System.out.println("Failed to receive connection acknowledgment from server.");
    	}
    }
    public void closeConnection() {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
            System.out.println("Disconnected from server.");
        }
    }

    public void indexFiles(String directoryPath) {
    if (this.socket == null) {
        System.out.println("No open connection. Please connect first.");
        return;
    }

    System.out.println("Preparing to send indexing request for directory: " + directoryPath);
    String message = "index " + directoryPath;

    try {
        System.out.println("Client sending index request...");
        this.socket.send(message.getBytes(ZMQ.CHARSET), 0);

        System.out.println("Message sent, waiting for reply...");

        byte[] reply = this.socket.recv(0);
        System.out.println("Acknowledgment from server: " + new String(reply, ZMQ.CHARSET));
        
        if(reply != null) {
            String replyMessage = new String(reply, ZMQ.CHARSET);
            System.out.println("Received reply: " + replyMessage);
        } else {
            System.out.println("No reply received from the server.");
        }
    } catch (ZMQException e) {
        System.out.println("An error occurred: " + e.getMessage());
        e.printStackTrace();
        if (e.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
            System.out.println("The context was terminated.");
        } else if (e.getErrorCode() == ZMQ.Error.ENOTSOCK.getCode()) {
            System.out.println("The socket was invalid.");
        }
    }
}
    
    public void searchFiles(String query) {
        if (this.socket == null) {
            System.out.println("No open connection. Please connect first.");
            return;
        }
        
        System.out.println("Sending search query to server: " + query);
    	String message = "search " + query;
    	this.socket.send(message.getBytes(ZMQ.CHARSET), 0);       
        byte[] reply = this.socket.recv(0);
    	String replyStr = new String(reply, ZMQ.CHARSET);
    	System.out.println("Search results: " + replyStr);
    }
}
