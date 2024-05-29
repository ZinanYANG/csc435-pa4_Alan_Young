package csc435.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;

public class IndexStore {
    private ConcurrentHashMap<String, Map<String, Integer>> index;

    public IndexStore() {
        this.index = new ConcurrentHashMap<>();
    }

    public void insertIndex(String word, String documentName) {
        this.index.compute(word, (key, value) -> {
            if (value == null) {
                value = new HashMap<>();
            }
             int currentCount = value.getOrDefault(documentName, 0);
            value.put(documentName, currentCount + 1);
            return value;
        });

    }


    
    public Map<String, Integer> lookupIndex(String word) {
    	Map<String, Integer> result = this.index.getOrDefault(word, new HashMap<>());
    	if(result.isEmpty()) {
        	System.out.println("No entries found for word: " + word);
    	} else {
        	// System.out.println("Lookup for word: " + word + ", found in documents: " + result);
    	}
   	return result;
    }

}
