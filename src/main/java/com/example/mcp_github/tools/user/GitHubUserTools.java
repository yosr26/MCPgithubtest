package com.example.mcp_github.tools.user;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.example.mcp_github.model.GitHubUser;
import com.example.mcp_github.service.GitHubService;

/**
 * MCP Tools — User domain. Covers: viewing any user's public profile or the
 * authenticated user's profile.
 */
@Component
public class GitHubUserTools {

    private final GitHubService gitHubService;

    public GitHubUserTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getUserProfile",
            description = "Get detailed public profile information for any GitHub user.")
    public String getUserProfile(
            @ToolParam(description = "The GitHub username to look up") String username) {
        try {
            GitHubUser user = gitHubService.getUserProfile(username);
            if (user == null) {
                return "User not found: " + username;
            }
            return formatProfile("👤 GitHub Profile: " + user.login(), user);
        } catch (Exception e) {
            return "Error fetching profile for user '%s': %s".formatted(username, e.getMessage());
        }
    }

    @Tool(name = "getMyProfile",
            description = "Get the authenticated user's own GitHub profile. Requires authentication.")
    public String getMyProfile() {
        try {
            requireAuth();
            GitHubUser user = gitHubService.getAuthenticatedUserProfile();
            return formatProfile("👤 Your GitHub Profile", user);
        } catch (Exception e) {
            return "Error fetching your profile: " + e.getMessage();
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private void requireAuth() {
        if (!gitHubService.hasAuthentication()) {
            throw new IllegalStateException("GitHub token is not configured.");
        }
    }

    private String formatProfile(String header, GitHubUser u) {
        return """
                %s

                Username : %s
                Name     : %s
                Bio      : %s
                Company  : %s
                Location : %s
                Blog     : %s
                Email    : %s

                📊 Stats:
                   Public Repos : %d
                   Public Gists : %d
                   Followers    : %d
                   Following    : %d

                🔗 Profile URL: %s
                📅 Joined     : %s
                """.formatted(
                header,
                u.login(),
                nullOr(u.name()),
                nullOr(u.bio()),
                nullOr(u.company()),
                nullOr(u.location()),
                nullOr(u.blog()),
                nullOr(u.email()),
                u.publicRepos(),
                u.publicGists(),
                u.followers(),
                u.following(),
                u.htmlUrl(),
                u.createdAt());
    }

    private String nullOr(String value) {
        return (value != null && !value.isBlank()) ? value : "N/A";
    }
}
