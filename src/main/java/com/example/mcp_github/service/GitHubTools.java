package com.example.mcp_github.service;

import com.example.mcp_github.model.*;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GitHubTools {

    private final GitHubService gitHubService;

    public GitHubTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Tool(name = "getUserRepositories", description = "Get all public repositories for a GitHub user. Returns repository name, description, language, stars, and other metadata. Specify the GitHub username.")
    public String getUserRepositories(String username) {
        try {
            List<GitHubRepository> repos = gitHubService.getUserRepositories(username);

            if (repos == null || repos.isEmpty()) {
                return "No repositories found for user: " + username;
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d repositories for user '%s':\n\n",
                    repos.size(), username));

            for (GitHubRepository repo : repos) {
                result.append(String.format("📦 %s\n", repo.name()));
                result.append(String.format("   Description: %s\n",
                        repo.description() != null ? repo.description() : "No description"));
                result.append(String.format("   Language: %s\n",
                        repo.language() != null ? repo.language() : "Unknown"));
                result.append(String.format("   ⭐ Stars: %d | 🍴 Forks: %d\n",
                        repo.stars(), repo.forks()));
                result.append(String.format("   URL: %s\n", repo.htmlUrl()));
                result.append(String.format("   Last updated: %s\n\n", repo.updatedAt()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching repositories for user '" + username + "': " + e.getMessage();
        }
    }

    @Tool(name = "getRepositoryCommits", description = "Get recent commits from a specific GitHub repository. Specify the username, repository name, and optionally the number of commits to retrieve (default: 10, max: 100).")
    public String getRepositoryCommits(String username, String repository, Integer limit) {
        try {
            int actualLimit = (limit != null && limit > 0) ? limit : 10;
            List<GitHubCommit> commits = gitHubService.getRepositoryCommits(
                    username, repository, actualLimit);

            if (commits == null || commits.isEmpty()) {
                return String.format("No commits found for repository: %s/%s",
                        username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Recent commits for %s/%s:\n\n",
                    username, repository));

            for (int i = 0; i < commits.size(); i++) {
                GitHubCommit commit = commits.get(i);
                String firstLine = commit.commit().message().split("\n")[0];
                result.append(String.format("%d. %s\n", i + 1, firstLine));
                result.append(String.format("   SHA: %s\n", commit.sha().substring(0, 7)));
                result.append(String.format("   Author: %s (%s)\n",
                        commit.commit().author().name(),
                        commit.commit().author().email()));
                result.append(String.format("   Date: %s\n", commit.commit().author().date()));
                result.append(String.format("   URL: %s\n\n", commit.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching commits for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    @Tool(name = "getLastCommit", description = "Get the most recent commit from a GitHub repository. Specify the username and repository name.")
    public String getLastCommit(String username, String repository) {
        try {
            GitHubCommit commit = gitHubService.getLastCommit(username, repository);

            if (commit == null) {
                return String.format("No commits found for repository: %s/%s",
                        username, repository);
            }

            return String.format("""
                    Latest commit in %s/%s:

                    📝 Message: %s
                    👤 Author: %s (%s)
                    📅 Date: %s
                    🔗 SHA: %s
                    🌐 URL: %s
                    """,
                    username, repository,
                    commit.commit().message(),
                    commit.commit().author().name(),
                    commit.commit().author().email(),
                    commit.commit().author().date(),
                    commit.sha().substring(0, 7),
                    commit.htmlUrl());
        } catch (Exception e) {
            return "Error fetching last commit for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    @Tool(name = "getMyRepositories", description = "Get all repositories (including private ones) for the authenticated GitHub user. This only works if a GitHub token is configured. Returns both public and private repositories with their details.")
    public String getMyRepositories() {
        try {
            if (!gitHubService.hasAuthentication()) {
                return "Error: Cannot access private repositories. GitHub token is not configured in the MCP server.";
            }

            List<GitHubRepository> repos = gitHubService.getAuthenticatedUserRepositories();

            if (repos == null || repos.isEmpty()) {
                return "No repositories found for the authenticated user.";
            }

            // Count private vs public
            long privateCount = repos.stream().filter(GitHubRepository::isPrivate).count();
            long publicCount = repos.size() - privateCount;

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d repositories for authenticated user:\n", repos.size()));
            result.append(String.format("📊 %d public, %d private\n\n", publicCount, privateCount));

            for (GitHubRepository repo : repos) {
                String visibility = repo.isPrivate() ? "🔒 PRIVATE" : "🌐 PUBLIC";
                result.append(String.format("%s %s\n", visibility, repo.name()));
                result.append(String.format("   Description: %s\n",
                        repo.description() != null ? repo.description() : "No description"));
                result.append(String.format("   Language: %s\n",
                        repo.language() != null ? repo.language() : "Unknown"));
                result.append(String.format("   ⭐ Stars: %d | 🍴 Forks: %d\n",
                        repo.stars(), repo.forks()));
                result.append(String.format("   URL: %s\n", repo.htmlUrl()));
                result.append(String.format("   Last updated: %s\n\n", repo.updatedAt()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching repositories: " + e.getMessage();
        }
    }
}