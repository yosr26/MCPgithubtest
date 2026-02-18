package com.example.mcp_github.tools.release;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubRelease;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — Release domain. Covers: listing releases and fetching the latest
 * release.
 */
@Component
public class GitHubReleaseTools {

    private final GitHubService gitHubService;

    public GitHubReleaseTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getRepositoryReleases",
            description = "List releases for a GitHub repository, including drafts and pre-releases.")
    public String getRepositoryReleases(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository,
            @ToolParam(description = "Max results (default 10, max 100)") Integer limit) {
        try {
            List<GitHubRelease> releases = gitHubService.getRepositoryReleases(username, repository, resolveLimit(limit));
            if (releases == null || releases.isEmpty()) {
                return "No releases found for repository: %s/%s".formatted(username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Releases for %s/%s:\n\n".formatted(username, repository));
            for (GitHubRelease r : releases) {
                String type = r.prerelease() ? "🚧 Pre-release" : r.draft() ? "📝 Draft" : "✅ Release";
                sb.append("%s %s (%s)\n".formatted(type, r.name(), r.tagName()));
                sb.append("   Published: %s\n".formatted(r.publishedAt()));
                appendAssets(sb, r);
                sb.append("   URL: %s\n\n".formatted(r.htmlUrl()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching releases for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    @Tool(name = "getLatestRelease",
            description = "Get the latest stable release for a GitHub repository.")
    public String getLatestRelease(
            @ToolParam(description = "Repository owner username") String username,
            @ToolParam(description = "Repository name") String repository) {
        try {
            GitHubRelease r = gitHubService.getLatestRelease(username, repository);
            if (r == null) {
                return "No releases found for repository: %s/%s".formatted(username, repository);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Latest release for %s/%s:\n\n".formatted(username, repository));
            sb.append("🏷️ %s (%s)\n".formatted(r.name(), r.tagName()));
            sb.append("📅 Published: %s\n".formatted(r.publishedAt()));
            if (r.body() != null && !r.body().isBlank()) {
                sb.append("\n📝 Release Notes:\n%s\n".formatted(r.body()));
            }
            appendAssets(sb, r);
            sb.append("\n🔗 URL: %s\n".formatted(r.htmlUrl()));
            return sb.toString();
        } catch (Exception e) {
            return "Error fetching latest release for '%s/%s': %s".formatted(username, repository, e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private void appendAssets(StringBuilder sb, GitHubRelease r) {
        if (r.assets() != null && !r.assets().isEmpty()) {
            sb.append("   📦 Assets (%d):\n".formatted(r.assets().size()));
            r.assets().forEach(a -> sb.append("      • %s (%.2f MB — %d downloads)\n"
                    .formatted(a.name(), a.size() / 1024.0 / 1024.0, a.downloadCount())));
        }
    }

    private int resolveLimit(Integer limit) {
        return (limit != null && limit > 0) ? Math.min(limit, 100) : 10;
    }
}
