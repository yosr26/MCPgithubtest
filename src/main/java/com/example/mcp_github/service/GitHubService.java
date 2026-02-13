package com.example.mcp_github.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.mcp_github.model.GitHubBranch;
import com.example.mcp_github.model.GitHubCollaborator;
import com.example.mcp_github.model.GitHubCommit;
import com.example.mcp_github.model.GitHubContent;
import com.example.mcp_github.model.GitHubFork;
import com.example.mcp_github.model.GitHubIssue;
import com.example.mcp_github.model.GitHubPullRequest;
import com.example.mcp_github.model.GitHubRelease;
import com.example.mcp_github.model.GitHubRepository;
import com.example.mcp_github.model.GitHubSearchResult;
import com.example.mcp_github.model.GitHubUser;
import com.example.mcp_github.model.GitHubWorkflowRun;
import com.example.mcp_github.model.GitHubWorkflowRunsResponse;

@Service
public class GitHubService {

    private final WebClient webClient;
    private final boolean hasToken;

    public GitHubService(
            @Value("${github.api.base-url}") String baseUrl,
            @Value("${github.api.token:}") String token) {

        this.hasToken = token != null && !token.isEmpty();

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }

        this.webClient = builder.build();
    }

    // ==================== REPOSITORIES ====================
    // For any user's PUBLIC repos
    public List<GitHubRepository> getUserRepositories(String username) {
        return webClient.get()
                .uri("/users/{username}/repos?sort=updated&per_page=100", username)
                .retrieve()
                .bodyToFlux(GitHubRepository.class)
                .collectList()
                .block();
    }

    // For authenticated user's ALL repos (public + private)
    public List<GitHubRepository> getAuthenticatedUserRepositories() {
        if (!hasToken) {
            throw new IllegalStateException("GitHub token required");
        }

        return webClient.get()
                .uri("/user/repos?per_page=100&type=all")
                .retrieve()
                .bodyToFlux(GitHubRepository.class)
                .collectList()
                .block();
    }

    // ==================== COMMITS ====================
    public List<GitHubCommit> getRepositoryCommits(String username, String repo, int limit) {
        int perPage = Math.min(limit, 100);

        return webClient.get()
                .uri("/repos/{username}/{repo}/commits?per_page={perPage}",
                        username, repo, perPage)
                .retrieve()
                .bodyToFlux(GitHubCommit.class)
                .collectList()
                .block();
    }

    public GitHubCommit getLastCommit(String username, String repo) {
        List<GitHubCommit> commits = webClient.get()
                .uri("/repos/{username}/{repo}/commits?per_page=1", username, repo)
                .retrieve()
                .bodyToFlux(GitHubCommit.class)
                .collectList()
                .block();

        return commits != null && !commits.isEmpty() ? commits.get(0) : null;
    }

    // ==================== COLLABORATORS ====================
    public List<GitHubCollaborator> getRepositoryCollaborators(String username, String repo) {
        return webClient.get()
                .uri("/repos/{username}/{repo}/collaborators", username, repo)
                .retrieve()
                .bodyToFlux(GitHubCollaborator.class)
                .collectList()
                .block();
    }

    // ==================== ISSUES ====================
    public List<GitHubIssue> getRepositoryIssues(String username, String repo, String state, int limit) {
        int perPage = Math.min(limit, 100);

        return webClient.get()
                .uri("/repos/{username}/{repo}/issues?state={state}&per_page={perPage}",
                        username, repo, state, perPage)
                .retrieve()
                .bodyToFlux(GitHubIssue.class)
                .collectList()
                .block();
    }

    public GitHubIssue createIssue(String username, String repo, String title, String body) {
        if (!hasToken) {
            throw new IllegalStateException("GitHub token required to create issues");
        }

        record IssueRequest(String title, String body) {

        }

        var requestBody = new IssueRequest(title, body);

        return webClient.post()
                .uri("/repos/{username}/{repo}/issues", username, repo)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GitHubIssue.class)
                .block();
    }

    // ==================== PULL REQUESTS ====================
    public List<GitHubPullRequest> getRepositoryPullRequests(String username, String repo, String state, int limit) {
        int perPage = Math.min(limit, 100);

        return webClient.get()
                .uri("/repos/{username}/{repo}/pulls?state={state}&per_page={perPage}",
                        username, repo, state, perPage)
                .retrieve()
                .bodyToFlux(GitHubPullRequest.class)
                .collectList()
                .block();
    }

    // ==================== BRANCHES ====================
    public List<GitHubBranch> getRepositoryBranches(String username, String repo) {
        return webClient.get()
                .uri("/repos/{username}/{repo}/branches", username, repo)
                .retrieve()
                .bodyToFlux(GitHubBranch.class)
                .collectList()
                .block();
    }

    // ==================== USER PROFILE ====================
    public GitHubUser getUserProfile(String username) {
        return webClient.get()
                .uri("/users/{username}", username)
                .retrieve()
                .bodyToMono(GitHubUser.class)
                .block();
    }

    public GitHubUser getAuthenticatedUserProfile() {
        if (!hasToken) {
            throw new IllegalStateException("GitHub token required");
        }

        return webClient.get()
                .uri("/user")
                .retrieve()
                .bodyToMono(GitHubUser.class)
                .block();
    }

    // ==================== RELEASES ====================
    public List<GitHubRelease> getRepositoryReleases(String username, String repo, int limit) {
        int perPage = Math.min(limit, 100);

        return webClient.get()
                .uri("/repos/{username}/{repo}/releases?per_page={perPage}",
                        username, repo, perPage)
                .retrieve()
                .bodyToFlux(GitHubRelease.class)
                .collectList()
                .block();
    }

    public GitHubRelease getLatestRelease(String username, String repo) {
        return webClient.get()
                .uri("/repos/{username}/{repo}/releases/latest", username, repo)
                .retrieve()
                .bodyToMono(GitHubRelease.class)
                .block();
    }

    // ==================== GITHUB ACTIONS ====================
    public List<GitHubWorkflowRun> getWorkflowRuns(String username, String repo, int limit) {
        int perPage = Math.min(limit, 100);

        GitHubWorkflowRunsResponse response = webClient.get()
                .uri("/repos/{username}/{repo}/actions/runs?per_page={perPage}",
                        username, repo, perPage)
                .retrieve()
                .bodyToMono(GitHubWorkflowRunsResponse.class)
                .block();

        return response != null ? response.workflowRuns() : List.of();
    }

    // ==================== FILE CONTENT ====================
    public GitHubContent getFileContent(String username, String repo, String path) {
        return webClient.get()
                .uri("/repos/{username}/{repo}/contents/{path}", username, repo, path)
                .retrieve()
                .bodyToMono(GitHubContent.class)
                .block();
    }

    // ==================== SEARCH ====================
    public List<GitHubRepository> searchRepositories(String query, int limit) {
        int perPage = Math.min(limit, 100);

        GitHubSearchResult result = webClient.get()
                .uri("/search/repositories?q={query}&per_page={perPage}&sort=stars&order=desc",
                        query, perPage)
                .retrieve()
                .bodyToMono(GitHubSearchResult.class)
                .block();

        return result != null ? result.items() : List.of();
    }

    // ==================== FORKS ====================
    public List<GitHubFork> getRepositoryForks(String username, String repo, int limit) {
        int perPage = Math.min(limit, 100);

        return webClient.get()
                .uri("/repos/{username}/{repo}/forks?per_page={perPage}&sort=newest",
                        username, repo, perPage)
                .retrieve()
                .bodyToFlux(GitHubFork.class)
                .collectList()
                .block();
    }

    public GitHubRepository forkRepository(String username, String repo) {
        if (!hasToken) {
            throw new IllegalStateException("GitHub token required to fork repositories");
        }

        return webClient.post()
                .uri("/repos/{username}/{repo}/forks", username, repo)
                .retrieve()
                .bodyToMono(GitHubRepository.class)
                .block();
    }

    // ==================== STARRING ====================
    public void starRepository(String username, String repo) {
        if (!hasToken) {
            throw new IllegalStateException("GitHub token required to star repositories");
        }

        webClient.put()
                .uri("/user/starred/{username}/{repo}", username, repo)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void unstarRepository(String username, String repo) {
        if (!hasToken) {
            throw new IllegalStateException("GitHub token required to unstar repositories");
        }

        webClient.delete()
                .uri("/user/starred/{username}/{repo}", username, repo)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public boolean isRepositoryStarred(String username, String repo) {
        if (!hasToken) {
            return false;
        }

        try {
            webClient.get()
                    .uri("/user/starred/{username}/{repo}", username, repo)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== HELPER ====================
    public boolean hasAuthentication() {
        return hasToken;
    }
}
