package com.example.mcp_github.tools.file;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubContent;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — File domain. Covers: reading, creating/updating and deleting
 * repository files.
 */
@Component
public class GitHubFileTools {

    private final GitHubService gitHubService;

    public GitHubFileTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getFileContent",
            description = "Read the content of a file from a GitHub repository.")
    public String getFileContent(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "File path within the repository (e.g. 'src/Main.java')") String path) {
        try {
            GitHubContent content = gitHubService.getFileContent(username, repository, path);
            if (content == null) {
                return "File not found: %s in %s/%s".formatted(path, username, repository);
            }
            if (!"file".equals(content.type())) {
                return "'%s' is not a file — it is a %s.".formatted(path, content.type());
            }
            String fileContent = decodeBase64Content(content);
            return """
                    📄 File: %s
                    Repository : %s/%s
                    Size       : %.2f KB

                    Content:
                    ```
                    %s
                    ```

                    🔗 URL: %s
                    """.formatted(
                    content.name(), username, repository,
                    content.size() / 1024.0,
                    fileContent,
                    content.htmlUrl());
        } catch (Exception e) {
            return "Error fetching file '%s' from '%s/%s': %s".formatted(path, username, repository, e.getMessage());
        }
    }

    @Tool(name = "pushFileContent",
            description = "Create or update a file in a GitHub repository with a commit. Requires authentication.")
    public String pushFileContent(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "File path within the repository (e.g. 'docs/README.md')") String path,
            @ToolParam(description = "Full text content to write into the file") String content,
            @ToolParam(description = "Commit message") String message,
            @ToolParam(description = "Target branch name") String branch) {
        try {
            requireAuth();
            String commitSha = gitHubService.pushFileContent(username, repository, path, content, message, branch);
            if (commitSha == null) {
                return "Error: File push failed — no commit SHA returned.";
            }
            return """
                    ✅ File pushed successfully!

                    File      : %s
                    Repository: %s/%s
                    Branch    : %s
                    Commit SHA: %s
                    Message   : %s
                    View      : https://github.com/%s/%s/blob/%s/%s
                    """.formatted(
                    path, username, repository,
                    branch, commitSha.substring(0, 7), message,
                    username, repository, branch, path);
        } catch (Exception e) {
            return "Error pushing file '%s' to '%s/%s': %s".formatted(path, username, repository, e.getMessage());
        }
    }

    @Tool(name = "deleteFile",
            description = "Delete a file from a GitHub repository with a commit. Requires authentication. ⚠️ Irreversible.")
    public String deleteFile(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "File path within the repository") String path,
            @ToolParam(description = "Commit message for the deletion") String message,
            @ToolParam(description = "Branch to delete the file from") String branch) {
        try {
            requireAuth();
            gitHubService.deleteFile(username, repository, path, message, branch);
            return "🗑️ File '%s' deleted from %s/%s (branch: %s).".formatted(path, username, repository, branch);
        } catch (Exception e) {
            return "Error deleting file '%s' from '%s/%s': %s".formatted(path, username, repository, e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private void requireAuth() {
        if (!gitHubService.hasAuthentication()) {
            throw new IllegalStateException("GitHub token is not configured.");
        }
    }

    private String decodeBase64Content(GitHubContent content) {
        if (content.content() != null && "base64".equals(content.encoding())) {
            return new String(java.util.Base64.getDecoder()
                    .decode(content.content().replace("\n", "")));
        }
        return "";
    }
}
