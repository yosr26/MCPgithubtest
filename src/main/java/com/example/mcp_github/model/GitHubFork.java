package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubFork(
        String name,
        @JsonProperty("full_name")
        String fullName,
        @JsonProperty("html_url")
        String htmlUrl,
        GitHubOwner owner,
        @JsonProperty("created_at")
        String createdAt) {

    public record GitHubOwner(
            String login,
            @JsonProperty("html_url")
            String htmlUrl) {

    }
}
