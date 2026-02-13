package com.example.mcp_github.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubCollaborator(
        String login,
        @JsonProperty("avatar_url")
        String avatarUrl,
        @JsonProperty("html_url")
        String htmlUrl,
        String type,
        @JsonProperty("site_admin")
        boolean siteAdmin,
        GitHubPermissions permissions) {

    public record GitHubPermissions(
            boolean admin,
            boolean maintain,
            boolean push,
            boolean triage,
            boolean pull) {

    }
}
