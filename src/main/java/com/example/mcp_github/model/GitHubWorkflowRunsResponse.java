package com.example.mcp_github.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubWorkflowRunsResponse(
        @JsonProperty("total_count")
        int totalCount,
        @JsonProperty("workflow_runs")
        List<GitHubWorkflowRun> workflowRuns) {

}
