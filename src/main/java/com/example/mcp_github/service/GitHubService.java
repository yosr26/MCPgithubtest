package com.example.mcp_github.service;

import com.example.mcp_github.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

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

    public boolean hasAuthentication() {
        return hasToken;
    }
}
