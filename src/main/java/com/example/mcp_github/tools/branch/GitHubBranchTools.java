package com.example.mcp_github.tools.branch;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubBranch;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — Branch domain. Covers: listing, creating and deleting branches.
 */
@Component
public class GitHubBranchTools {

    private final GitHubService gitHubService;

    public GitHubBranchTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getRepositoryBranches",
            description = "List all branches in a GitHub repository with their protection status.")
    public String getRepositoryBranches(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            List<GitHubBranch> branches = gitHubService.getRepositoryBranches(username, repository);
            if (branches == null || branches.isEmpty()) {
                return "No branches found for repository: %s/%s".formatted(username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Branches for %s/%s:\n\n".formatted(username, repository));
            for (GitHubBranch b : branches) {
                sb.append("🌿 %s%s\n".formatted(b.name(), b.protectedBranch() ? "  🔒 Protected" : ""));
                sb.append("   Latest commit: %s\n\n".formatted(b.commit().sha().substring(0, 7)));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching branches for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "createBranch",
            description = "Create a new branch from an existing branch. Requires authentication.")
    public String createBranch(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Name for the new branch") String branchName,
            @ToolParam(description = "Source branch to create from (e.g. 'main')") String fromBranch) {
        try {
            requireAuth();
            GitHubBranch b = gitHubService.createBranch(username, repository, branchName, fromBranch);
            return """
                    ✅ Branch created successfully!

                    Branch      : %s
                    Repository  : %s/%s
                    Created from: %s
                    Latest SHA  : %s
                    """.formatted(b.name(), username, repository, fromBranch, b.commit().sha().substring(0, 7));
        } catch (Exception e) {
            return "Error creating branch '%s' in '%s/%s': %s"
                    .formatted(branchName, username, repository, e.getMessage());
        }
    }

    @Tool(name = "deleteBranch",
            description = "Delete a branch from a GitHub repository. Requires authentication. ⚠️ Irreversible.")
    public String deleteBranch(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Name of the branch to delete") String branchName) {
        try {
            requireAuth();
            gitHubService.deleteBranch(username, repository, branchName);
            return "🗑️ Branch '%s' deleted from %s/%s.".formatted(branchName, username, repository);
        } catch (Exception e) {
            return "Error deleting branch '%s' from '%s/%s': %s"
                    .formatted(branchName, username, repository, e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private void requireAuth() {
        if (!gitHubService.hasAuthentication()) {
            throw new IllegalStateException("GitHub token is not configured.");
        }
    }
}
