package com.example.mcp_github.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for persistent memory management using a local JSON file.
 */
@Service
public class MemoryService {

    private static final String MEMORY_FILE
            = "C:\\Users\\user\\Desktop\\MCPgithubtest\\memory.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void remember(String key, String value) {
        try {
            Map<String, String> memory = loadMemory();
            memory.put(key, value);
            objectMapper.writeValue(new File(MEMORY_FILE), memory);
        } catch (Exception e) {
            throw new RuntimeException("Error saving memory: " + e.getMessage());
        }
    }

    public String recall(String key) {
        try {
            return loadMemory().getOrDefault(key, null);
        } catch (Exception e) {
            throw new RuntimeException("Error reading memory: " + e.getMessage());
        }
    }

    public Map<String, String> recallAll() {
        try {
            return loadMemory();
        } catch (Exception e) {
            throw new RuntimeException("Error reading memory: " + e.getMessage());
        }
    }

    public void forget(String key) {
        try {
            Map<String, String> memory = loadMemory();
            memory.remove(key);
            objectMapper.writeValue(new File(MEMORY_FILE), memory);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting memory: " + e.getMessage());
        }
    }

    public void forgetAll() {
        try {
            objectMapper.writeValue(new File(MEMORY_FILE), new HashMap<>());
        } catch (Exception e) {
            throw new RuntimeException("Error clearing memory: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadMemory() throws Exception {
        File file = new File(MEMORY_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(file, Map.class);
    }
}
