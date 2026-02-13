package com.example.mcp_github.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubSearchResult(
        @JsonProperty("total_count")
        int totalCount,
        @JsonProperty("incomplete_results")
        boolean incompleteResults,
        List<GitHubRepository> items) {

}
