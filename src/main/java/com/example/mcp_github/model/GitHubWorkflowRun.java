package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubWorkflowRun(
        long id,
        String name,
        @JsonProperty("head_branch")
        String headBranch,
        String status,
        String conclusion,
        @JsonProperty("html_url")
        String htmlUrl,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("updated_at")
        String updatedAt) {

}
