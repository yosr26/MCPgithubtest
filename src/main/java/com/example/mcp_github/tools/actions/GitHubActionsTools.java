package com.example.mcp_github.tools.actions;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubWorkflowRun;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — GitHub Actions domain. Covers: listing workflow runs with their
 * status and conclusion.
 */
@Component
public class GitHubActionsTools {

    private final GitHubService gitHubService;

    public GitHubActionsTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getWorkflowRuns",
            description = "List GitHub Actions workflow runs for a repository with their status and conclusion.")
    public String getWorkflowRuns(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Max results (default 10, max 100)") Integer limit) {
        try {
            List<GitHubWorkflowRun> runs = gitHubService.getWorkflowRuns(username, repository, resolveLimit(limit));
            if (runs == null || runs.isEmpty()) {
                return "No workflow runs found for repository: %s/%s".formatted(username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("GitHub Actions runs for %s/%s:\n\n".formatted(username, repository));
            for (GitHubWorkflowRun run : runs) {
                sb.append("%s %s\n".formatted(statusEmoji(run), run.name()));
                sb.append("   Branch : %s\n".formatted(run.headBranch()));
                sb.append("   Status : %s".formatted(run.status()));
                if (run.conclusion() != null) {
                    sb.append(" (%s)".formatted(run.conclusion()));
                }
                sb.append("\n");
                sb.append("   Started: %s\n".formatted(run.createdAt()));
                sb.append("   URL    : %s\n\n".formatted(run.htmlUrl()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching workflow runs for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private String statusEmoji(GitHubWorkflowRun run) {
        String key = run.conclusion() != null ? run.conclusion() : run.status();
        return switch (key) {
            case "success" ->
                "✅";
            case "failure" ->
                "❌";
            case "cancelled" ->
                "🚫";
            case "in_progress" ->
                "🔄";
            default ->
                "⏸️";
        };
    }

    private int resolveLimit(Integer limit) {
        return (limit != null && limit > 0) ? Math.min(limit, 100) : 10;
    }
}
