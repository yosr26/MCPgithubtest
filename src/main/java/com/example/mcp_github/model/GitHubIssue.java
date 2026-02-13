package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubIssue(
        int number,
        String title,
        String state,
        @JsonProperty("html_url")
        String htmlUrl,
        String body,
        IssueUser user,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("updated_at")
        String updatedAt,
        @JsonProperty("closed_at")
        String closedAt
        ) {

    public static record IssueUser(
            String login,
            @JsonProperty("avatar_url")
            String avatarUrl
            ) {

    }
}
