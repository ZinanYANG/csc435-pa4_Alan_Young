package csc435.app;

import java.util.Set;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
public class Worker implements Runnable {
    private ZMQ.Socket receiver;
    private IndexStore store;
    public Worker(ZContext context, IndexStore store, String inprocAddress) {
        this.store = store;
        this.receiver = context.createSocket(ZMQ.PULL);
        this.receiver.connect(inprocAddress);
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            String command = receiver.recvStr();
            if (command != null) {
            		System.out.println("Worker received command: " + command);
            		processCommand(command);
        	}
          
        }
    }
    private void processCommand(String command) {
    	if (command.startsWith("search ")) {
        	String[] parts = command.split(" ", 2);
        	if (parts.length == 2) {
            		searchFiles(parts[1]); 
        	}
    	} 
        else if (command.startsWith("index")) {
            String[] parts = command.split(" ", 2);
            if (parts.length == 2) {
                indexDirectory(parts[1]); 
            }
        } else if (command.startsWith("search")) {
            String[] parts = command.split(" ", 2);
            if (parts.length == 2) {
                searchFiles(parts[1]);
            }
        }
    }
    private void indexDirectory(String directoryPath) {
    	long startTime = System.currentTimeMillis();
    	System.out.println("Worker starting to index directory: " + directoryPath);
        try {
            Files.walk(Paths.get(directoryPath))
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    try {
                        Stream<String> lines = Files.lines(filePath);
                        lines.flatMap(line -> Arrays.stream(line.trim().split("\\s+")))
                             .forEach(word -> store.insertIndex(word, filePath.toString()));
                        lines.close();
                        System.out.println("Indexed file: " + filePath);
                    } catch (IOException e) {
                        System.out.println("Failed to read file: " + filePath);
                        e.printStackTrace();
                    }
                });
        } catch (IOException e) {
            System.out.println("Failed to walk the directory: " + directoryPath);
            e.printStackTrace();
        }
       
         long endTime = System.currentTimeMillis(); // End timing
    	System.out.println("Indexing completed in " + (endTime - startTime) / 1000.0 + " seconds");
    }

	
	private String[] words;
	private Map<String, Integer> results;
	private Map<String, Integer> firstWordResults;
	private Map<String, Integer> secondWordResults;
	private  List<Map.Entry<String, Integer>> sortedResults;
	int totalFreq;
	
	private void searchFiles(String query) {
    long startTime = System.currentTimeMillis(); 

    results = new HashMap<>();

    words = query.split(" AND ");
     System.out.println("Processing query: " + query);
    
    if (words.length > 1) { 
    	System.out.println("AND query detected. Words: " + Arrays.toString(words));
    	

        firstWordResults = store.lookupIndex(words[0]);
        secondWordResults = store.lookupIndex(words[1]);
        
        System.out.println("First word results size: " + firstWordResults.size());
        System.out.println("Second word results size: " + secondWordResults.size());
        firstWordResults.keySet().retainAll(secondWordResults.keySet());
        firstWordResults.forEach((doc, freq) -> {
            if (secondWordResults.containsKey(doc)) {
                totalFreq = freq + secondWordResults.get(doc);
                results.put(doc, totalFreq);
                System.out.println("Doc: " + doc + ", Total Freq: " + totalFreq);
            }
        });

    } else { 
    	System.out.println("Single word query: " + words[0]);
        results = store.lookupIndex(words[0]);
    }

    outputSearchResults(results, startTime);
}

private void outputSearchResults(Map<String, Integer> results, long startTime) {
    if (results.isEmpty()) {
        System.out.println("No results found for query.");
    } else {
        // Sort by frequency and limit to top 10
        List<Map.Entry<String, Integer>> sortedResults = results.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());

        System.out.println("Search completed in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
        System.out.println("Search results (top 10):");
        sortedResults.forEach(entry -> System.out.println("* " + entry.getKey() + " " + entry.getValue()));
    }
}
	
}
