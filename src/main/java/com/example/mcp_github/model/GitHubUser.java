package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubUser(
        String login,
        String name,
        String email,
        String bio,
        String company,
        String location,
        String blog,
        @JsonProperty("avatar_url")
        String avatarUrl,
        @JsonProperty("html_url")
        String htmlUrl,
        @JsonProperty("public_repos")
        int publicRepos,
        @JsonProperty("public_gists")
        int publicGists,
        int followers,
        int following,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("updated_at")
        String updatedAt) {

}
