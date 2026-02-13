package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubContent(
        String name,
        String path,
        String sha,
        long size,
        String type,
        @JsonProperty("download_url")
        String downloadUrl,
        @JsonProperty("html_url")
        String htmlUrl,
        String content,
        String encoding) {

}
