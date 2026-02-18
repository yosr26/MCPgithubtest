package com.example.mcp_github.tools.pullrequest;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubPullRequest;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — Pull Request domain. Covers: listing and creating pull requests.
 */
@Component
public class GitHubPullRequestTools {

    private final GitHubService gitHubService;

    public GitHubPullRequestTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getRepositoryPullRequests",
            description = "List pull requests from a GitHub repository filtered by state.")
    public String getRepositoryPullRequests(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Filter by state: 'open', 'closed', or 'all' (default: open)") String state,
            @ToolParam(description = "Max results (default 10, max 100)") Integer limit) {
        try {
            String prState = (state != null && !state.isBlank()) ? state : "open";
            List<GitHubPullRequest> prs = gitHubService.getRepositoryPullRequests(
                    username, repository, prState, resolveLimit(limit));
            if (prs == null || prs.isEmpty()) {
                return "No %s pull requests found for repository: %s/%s".formatted(prState, username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("%s pull requests for %s/%s:\n\n".formatted(prState.toUpperCase(), username, repository));
            for (GitHubPullRequest pr : prs) {
                sb.append("#%d — %s%s\n".formatted(pr.number(), pr.title(), pr.draft() ? "  🚧 DRAFT" : ""));
                sb.append("   State  : %s\n".formatted(pr.state()));
                sb.append("   Author : %s\n".formatted(pr.user().login()));
                sb.append("   Created: %s\n".formatted(pr.createdAt()));
                if (pr.mergedAt() != null) {
                    sb.append("   Merged : %s\n".formatted(pr.mergedAt()));
                }
                sb.append("   URL    : %s\n\n".formatted(pr.htmlUrl()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching pull requests for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "createPullRequest",
            description = "Create a new pull request in a GitHub repository. Requires authentication.")
    public String createPullRequest(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Pull request title") String title,
            @ToolParam(description = "Source branch (head)") String head,
            @ToolParam(description = "Target branch (base)") String base,
            @ToolParam(description = "Pull request body/description (optional)") String body) {
        try {
            requireAuth();
            GitHubPullRequest pr = gitHubService.createPullRequest(
                    username, repository, title, head, base, body != null ? body : "");
            return """
                    ✅ Pull request created successfully!

                    #%d — %s
                    Repository: %s/%s
                    %s → %s
                    State     : %s
                    URL       : %s
                    """.formatted(pr.number(), pr.title(), username, repository, head, base, pr.state(), pr.htmlUrl());
        } catch (Exception e) {
            return "Error creating pull request in '%s/%s': %s".formatted(username, repository, e.getMessage());
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
