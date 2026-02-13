package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubBranch(
        String name,
        GitHubCommitRef commit,
        @JsonProperty("protected")
        boolean protectedBranch) {

    public record GitHubCommitRef(
            String sha,
            String url) {

    }
}
