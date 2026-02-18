package com.example.mcp_github.tools.social;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubFork;
import com.example.mcp_github.model.GitHubRepository;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — Social domain. Covers: forking repositories, starring/unstarring
 * and listing collaborators.
 */
@Component
public class GitHubSocialTools {

    private final GitHubService gitHubService;

    public GitHubSocialTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    // ─── Forks ───────────────────────────────────────────────────────────────────
    @Tool(name = "getRepositoryForks",
            description = "List forks of a GitHub repository, sorted by newest.")
    public String getRepositoryForks(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Max results (default 10, max 100)") Integer limit) {
        try {
            List<GitHubFork> forks = gitHubService.getRepositoryForks(username, repository, resolveLimit(limit));
            if (forks == null || forks.isEmpty()) {
                return "No forks found for repository: %s/%s".formatted(username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Forks of %s/%s:\n\n".formatted(username, repository));
            for (GitHubFork f : forks) {
                sb.append("🍴 %s\n".formatted(f.fullName()));
                sb.append("   Owner  : %s\n".formatted(f.owner().login()));
                sb.append("   Created: %s\n".formatted(f.createdAt()));
                sb.append("   URL    : %s\n\n".formatted(f.htmlUrl()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching forks for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "forkRepository",
            description = "Fork a GitHub repository to your authenticated account. Requires authentication.")
    public String forkRepository(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            requireAuth();
            GitHubRepository fork = gitHubService.forkRepository(username, repository);
            return """
                    🍴 Repository forked successfully!

                    Original : %s/%s
                    Your fork: %s
                    URL      : %s
                    """.formatted(username, repository, fork.fullName(), fork.htmlUrl());
        } catch (Exception e) {
            return "Error forking '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    // ─── Starring ────────────────────────────────────────────────────────────────
    @Tool(name = "starRepository",
            description = "Star a GitHub repository. Requires authentication.")
    public String starRepository(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            requireAuth();
            gitHubService.starRepository(username, repository);
            return "⭐ Starred %s/%s successfully.".formatted(username, repository);
        } catch (Exception e) {
            return "Error starring '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "unstarRepository",
            description = "Remove your star from a GitHub repository. Requires authentication.")
    public String unstarRepository(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            requireAuth();
            gitHubService.unstarRepository(username, repository);
            return "Removed star from %s/%s.".formatted(username, repository);
        } catch (Exception e) {
            return "Error unstarring '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "isRepositoryStarred",
            description = "Check whether the authenticated user has starred a repository. Requires authentication.")
    public String isRepositoryStarred(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            requireAuth();
            boolean starred = gitHubService.isRepositoryStarred(username, repository);
            return starred
                    ? "⭐ You have starred %s/%s.".formatted(username, repository)
                    : "You have not starred %s/%s.".formatted(username, repository);
        } catch (Exception e) {
            return "Error checking star for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    // ─── Collaborators ───────────────────────────────────────────────────────────
    @Tool(name = "getRepositoryCollaborators",
            description = "List all collaborators of a GitHub repository with their permission levels.")
    public String getRepositoryCollaborators(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            var collaborators = gitHubService.getRepositoryCollaborators(username, repository);
            if (collaborators == null || collaborators.isEmpty()) {
                return "No collaborators found for repository: %s/%s".formatted(username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Collaborators for %s/%s:\n\n".formatted(username, repository));
            for (var c : collaborators) {
                sb.append("👤 %s (%s)\n".formatted(c.login(), c.type()));
                if (c.permissions() != null) {
                    sb.append("   Permissions: ");
                    if (c.permissions().admin()) {
                        sb.append("Admin ");
                    }
                    if (c.permissions().maintain()) {
                        sb.append("Maintain ");
                    }
                    if (c.permissions().push()) {
                        sb.append("Push ");
                    }
                    if (c.permissions().pull()) {
                        sb.append("Pull ");
                    }
                    sb.append("\n");
                }
                sb.append("   Profile: %s\n\n".formatted(c.htmlUrl()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching collaborators for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private void requireAuth() {
        if (!gitHubService.hasAuthentication()) {
            throw new IllegalStateException("GitHub token is not configured.");
        }
    }

    private int resolveLimit(Integer limit) {
        return (limit != null && limit > 0) ? Math.min(limit, 100) : 10;
    }
}
