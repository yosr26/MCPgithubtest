package com.example.mcp_github.tools.commit;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubCommit;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — Commit domain. Covers: listing commits and retrieving the latest
 * commit.
 */
@Component
public class GitHubCommitTools {

    private final GitHubService gitHubService;

    public GitHubCommitTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getRepositoryCommits",
            description = "List recent commits from a GitHub repository, ordered by date descending.")
    public String getRepositoryCommits(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Number of commits to retrieve (default 10, max 100)") Integer limit) {
        try {
            List<GitHubCommit> commits = gitHubService.getRepositoryCommits(username, repository, resolveLimit(limit));
            if (commits == null || commits.isEmpty()) {
                return "No commits found for repository: %s/%s".formatted(username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Recent commits for %s/%s:\n\n".formatted(username, repository));
            for (int i = 0; i < commits.size(); i++) {
                GitHubCommit c = commits.get(i);
                String firstLine = c.commit().message().split("\n")[0];
                sb.append("%d. %s\n".formatted(i + 1, firstLine));
                sb.append("   SHA   : %s\n".formatted(c.sha().substring(0, 7)));
                sb.append("   Author: %s <%s>\n".formatted(c.commit().author().name(), c.commit().author().email()));
                sb.append("   Date  : %s\n".formatted(c.commit().author().date()));
                sb.append("   URL   : %s\n\n".formatted(c.htmlUrl()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching commits for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "getLastCommit",
            description = "Get the single most recent commit from a GitHub repository.")
    public String getLastCommit(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            GitHubCommit c = gitHubService.getLastCommit(username, repository);
            if (c == null) {
                return "No commits found for repository: %s/%s".formatted(username, repository);
            }
            return """
                    Latest commit in %s/%s:

                    📝 Message : %s
                    👤 Author  : %s <%s>
                    📅 Date    : %s
                    🔗 SHA     : %s
                    🌐 URL     : %s
                    """.formatted(
                    username, repository,
                    c.commit().message(),
                    c.commit().author().name(), c.commit().author().email(),
                    c.commit().author().date(),
                    c.sha().substring(0, 7),
                    c.htmlUrl());
        } catch (Exception e) {
            return "Error fetching last commit for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private int resolveLimit(Integer limit) {
        return (limit != null && limit > 0) ? Math.min(limit, 100) : 10;
    }
}
