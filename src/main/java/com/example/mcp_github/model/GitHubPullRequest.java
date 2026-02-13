package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubPullRequest(
        int number,
        String title,
        String state,
        @JsonProperty("html_url")
        String htmlUrl,
        String body,
        GitHubUser user,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("updated_at")
        String updatedAt,
        @JsonProperty("merged_at")
        String mergedAt,
        boolean draft) {

    public record GitHubUser(
            String login,
            @JsonProperty("avatar_url")
            String avatarUrl) {

    }
}
