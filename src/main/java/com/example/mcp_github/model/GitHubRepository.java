package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRepository(
                String name,
                @JsonProperty("full_name") String fullName,
                String description,
                @JsonProperty("html_url") String htmlUrl,
                String language,
                @JsonProperty("stargazers_count") int stars,
                @JsonProperty("forks_count") int forks,
                @JsonProperty("created_at") String createdAt,
                @JsonProperty("updated_at") String updatedAt,
                @JsonProperty("private") boolean isPrivate) {
}