package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubCommit(
        String sha,
        Commit commit,
        @JsonProperty("html_url") String htmlUrl) {
    public record Commit(
            String message,
            Author author) {
    }

    public record Author(
            String name,
            String email,
            String date) {
    }
}