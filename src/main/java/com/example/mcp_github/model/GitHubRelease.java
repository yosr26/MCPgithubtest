package com.example.mcp_github.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubRelease(
        @JsonProperty("tag_name")
        String tagName,
        String name,
        String body,
        boolean draft,
        boolean prerelease,
        @JsonProperty("created_at")
        String createdAt,
        @JsonProperty("published_at")
        String publishedAt,
        @JsonProperty("html_url")
        String htmlUrl,
        List<GitHubAsset> assets) {

    public record GitHubAsset(
            String name,
            @JsonProperty("browser_download_url")
            String downloadUrl,
            int size,
            @JsonProperty("download_count")
            int downloadCount) {

    }
}
