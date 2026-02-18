package com.example.mcp_github.tools.memory;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.service.MemoryService;

/**
 * MCP Tools — Memory domain. Covers: saving, reading and deleting persistent
 * context via JSON file.
 */
@Component
public class MemoryTools {

    private final MemoryService memoryService;

    public MemoryTools(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @Tool(name = "rememberContext",
            description = "Save a key-value pair to persistent memory. Use this to remember project context, branch names, usernames, etc.")
    public String rememberContext(
            @ToolParam(description = "Key to save (e.g. 'current_project', 'current_branch')") String key,
            @ToolParam(description = "Value to associate with the key") String value) {
        try {
            memoryService.remember(key, value);
            return "✅ Remembered: %s = %s".formatted(key, value);
        } catch (Exception e) {
            return "Error saving memory: " + e.getMessage();
        }
    }

    @Tool(name = "recallContext",
            description = "Retrieve a previously saved value from memory by its key.")
    public String recallContext(
            @ToolParam(description = "Key to retrieve (e.g. 'current_project')") String key) {
        try {
            String value = memoryService.recall(key);
            if (value == null) {
                return "No memory found for key: " + key;
            }
            return "🧠 %s = %s".formatted(key, value);
        } catch (Exception e) {
            return "Error reading memory: " + e.getMessage();
        }
    }

    @Tool(name = "recallAllContext",
            description = "Retrieve all saved memory entries.")
    public String recallAllContext() {
        try {
            Map<String, String> memory = memoryService.recallAll();
            if (memory.isEmpty()) {
                return "Memory is empty.";
            }
            StringBuilder sb = new StringBuilder("🧠 All memory:\n\n");
            memory.forEach((k, v) -> sb.append("   %s = %s\n".formatted(k, v)));
            return sb.toString();
        } catch (Exception e) {
            return "Error reading memory: " + e.getMessage();
        }
    }

    @Tool(name = "forgetContext",
            description = "Delete a specific key from memory.")
    public String forgetContext(
            @ToolParam(description = "Key to delete") String key) {
        try {
            memoryService.forget(key);
            return "🗑️ Forgot: " + key;
        } catch (Exception e) {
            return "Error deleting memory: " + e.getMessage();
        }
    }

    @Tool(name = "forgetAllContext",
            description = "Clear all saved memory. ⚠️ Irreversible.")
    public String forgetAllContext() {
        try {
            memoryService.forgetAll();
            return "🗑️ All memory cleared.";
        } catch (Exception e) {
            return "Error clearing memory: " + e.getMessage();
        }
    }
}
