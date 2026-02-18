package com.example.mcp_github.tools.issue;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubIssue;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — Issue domain. Covers: listing and creating issues.
 */
@Component
public class GitHubIssueTools {

    private final GitHubService gitHubService;

    public GitHubIssueTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getRepositoryIssues",
            description = "List issues from a GitHub repository filtered by state.")
    public String getRepositoryIssues(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Filter by state: 'open', 'closed', or 'all' (default: open)") String state,
            @ToolParam(description = "Max results (default 10, max 100)") Integer limit) {
        try {
            String issueState = (state != null && !state.isBlank()) ? state : "open";
            List<GitHubIssue> issues = gitHubService.getRepositoryIssues(
                    username, repository, issueState, resolveLimit(limit));
            if (issues == null || issues.isEmpty()) {
                return "No %s issues found for repository: %s/%s".formatted(issueState, username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("%s issues for %s/%s:\n\n".formatted(issueState.toUpperCase(), username, repository));
            for (GitHubIssue i : issues) {
                sb.append("#%d — %s\n".formatted(i.number(), i.title()));
                sb.append("   State  : %s\n".formatted(i.state()));
                sb.append("   Author : %s\n".formatted(i.user().login()));
                sb.append("   Created: %s\n".formatted(i.createdAt()));
                sb.append("   URL    : %s\n\n".formatted(i.htmlUrl()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching issues for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "createIssue",
            description = "Create a new issue in a GitHub repository. Requires authentication.")
    public String createIssue(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Issue title") String title,
            @ToolParam(description = "Issue body/description (optional)") String body) {
        try {
            requireAuth();
            GitHubIssue issue = gitHubService.createIssue(
                    username, repository, title, body != null ? body : "");
            return """
                    ✅ Issue created successfully!

                    #%d — %s
                    Repository: %s/%s
                    State     : %s
                    URL       : %s
                    """.formatted(issue.number(), issue.title(), username, repository, issue.state(), issue.htmlUrl());
        } catch (Exception e) {
            return "Error creating issue in '%s/%s': %s".formatted(username, repository, e.getMessage());
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
