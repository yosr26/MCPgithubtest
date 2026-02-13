package com.example.mcp_github.service;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import com.example.mcp_github.model.GitHubBranch;
import com.example.mcp_github.model.GitHubCollaborator;
import com.example.mcp_github.model.GitHubCommit;
import com.example.mcp_github.model.GitHubContent;
import com.example.mcp_github.model.GitHubFork;
import com.example.mcp_github.model.GitHubIssue;
import com.example.mcp_github.model.GitHubPullRequest;
import com.example.mcp_github.model.GitHubRelease;
import com.example.mcp_github.model.GitHubRepository;
import com.example.mcp_github.model.GitHubUser;
import com.example.mcp_github.model.GitHubWorkflowRun;

@Service
public class GitHubTools {

    private final GitHubService gitHubService;

    public GitHubTools(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    // ==================== REPOSITORIES ====================
    @Tool(name = "getUserRepositories", description = "Get all public repositories for a GitHub user. Returns repository name, description, language, stars, and other metadata. Specify the GitHub username.")
    public String getUserRepositories(String username) {
        try {
            List<GitHubRepository> repos = gitHubService.getUserRepositories(username);

            if (repos == null || repos.isEmpty()) {
                return "No repositories found for user: " + username;
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d repositories for user '%s':\n\n",
                    repos.size(), username));

            for (GitHubRepository repo : repos) {
                result.append(String.format("📦 %s\n", repo.name()));
                result.append(String.format("   Description: %s\n",
                        repo.description() != null ? repo.description() : "No description"));
                result.append(String.format("   Language: %s\n",
                        repo.language() != null ? repo.language() : "Unknown"));
                result.append(String.format("   ⭐ Stars: %d | 🍴 Forks: %d\n",
                        repo.stars(), repo.forks()));
                result.append(String.format("   URL: %s\n", repo.htmlUrl()));
                result.append(String.format("   Last updated: %s\n\n", repo.updatedAt()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching repositories for user '" + username + "': " + e.getMessage();
        }
    }

    @Tool(name = "getMyRepositories", description = "Get all repositories (including private ones) for the authenticated GitHub user. This only works if a GitHub token is configured. Returns both public and private repositories with their details.")
    public String getMyRepositories() {
        try {
            if (!gitHubService.hasAuthentication()) {
                return "Error: Cannot access private repositories. GitHub token is not configured in the MCP server.";
            }

            List<GitHubRepository> repos = gitHubService.getAuthenticatedUserRepositories();

            if (repos == null || repos.isEmpty()) {
                return "No repositories found for the authenticated user.";
            }

            long privateCount = repos.stream().filter(GitHubRepository::isPrivate).count();
            long publicCount = repos.size() - privateCount;

            StringBuilder result = new StringBuilder();
            result.append(String.format("Found %d repositories for authenticated user:\n", repos.size()));
            result.append(String.format("📊 %d public, %d private\n\n", publicCount, privateCount));

            for (GitHubRepository repo : repos) {
                String visibility = repo.isPrivate() ? "🔒 PRIVATE" : "🌐 PUBLIC";
                result.append(String.format("%s %s\n", visibility, repo.name()));
                result.append(String.format("   Description: %s\n",
                        repo.description() != null ? repo.description() : "No description"));
                result.append(String.format("   Language: %s\n",
                        repo.language() != null ? repo.language() : "Unknown"));
                result.append(String.format("   ⭐ Stars: %d | 🍴 Forks: %d\n",
                        repo.stars(), repo.forks()));
                result.append(String.format("   URL: %s\n", repo.htmlUrl()));
                result.append(String.format("   Last updated: %s\n\n", repo.updatedAt()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching repositories: " + e.getMessage();
        }
    }

    // ==================== COMMITS ====================
    @Tool(name = "getRepositoryCommits", description = "Get recent commits from a specific GitHub repository. Specify the username, repository name, and optionally the number of commits to retrieve (default: 10, max: 100).")
    public String getRepositoryCommits(String username, String repository, Integer limit) {
        try {
            int actualLimit = (limit != null && limit > 0) ? limit : 10;
            List<GitHubCommit> commits = gitHubService.getRepositoryCommits(
                    username, repository, actualLimit);

            if (commits == null || commits.isEmpty()) {
                return String.format("No commits found for repository: %s/%s",
                        username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Recent commits for %s/%s:\n\n",
                    username, repository));

            for (int i = 0; i < commits.size(); i++) {
                GitHubCommit commit = commits.get(i);
                String firstLine = commit.commit().message().split("\n")[0];
                result.append(String.format("%d. %s\n", i + 1, firstLine));
                result.append(String.format("   SHA: %s\n", commit.sha().substring(0, 7)));
                result.append(String.format("   Author: %s (%s)\n",
                        commit.commit().author().name(),
                        commit.commit().author().email()));
                result.append(String.format("   Date: %s\n", commit.commit().author().date()));
                result.append(String.format("   URL: %s\n\n", commit.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching commits for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    @Tool(name = "getLastCommit", description = "Get the most recent commit from a GitHub repository. Specify the username and repository name.")
    public String getLastCommit(String username, String repository) {
        try {
            GitHubCommit commit = gitHubService.getLastCommit(username, repository);

            if (commit == null) {
                return String.format("No commits found for repository: %s/%s",
                        username, repository);
            }

            return String.format("""
                    Latest commit in %s/%s:

                    📝 Message: %s
                    👤 Author: %s (%s)
                    📅 Date: %s
                    🔗 SHA: %s
                    🌐 URL: %s
                    """,
                    username, repository,
                    commit.commit().message(),
                    commit.commit().author().name(),
                    commit.commit().author().email(),
                    commit.commit().author().date(),
                    commit.sha().substring(0, 7),
                    commit.htmlUrl());
        } catch (Exception e) {
            return "Error fetching last commit for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== COLLABORATORS ====================
    @Tool(name = "getRepositoryCollaborators", description = "Get all collaborators of a GitHub repository with their permissions. Specify the username and repository name.")
    public String getRepositoryCollaborators(String username, String repository) {
        try {
            List<GitHubCollaborator> collaborators = gitHubService.getRepositoryCollaborators(username, repository);

            if (collaborators == null || collaborators.isEmpty()) {
                return String.format("No collaborators found for repository: %s/%s", username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Collaborators for %s/%s:\n\n", username, repository));

            for (GitHubCollaborator collab : collaborators) {
                result.append(String.format("👤 %s (%s)\n", collab.login(), collab.type()));
                if (collab.permissions() != null) {
                    result.append("   Permissions: ");
                    if (collab.permissions().admin()) {
                        result.append("Admin ");
                    }
                    if (collab.permissions().maintain()) {
                        result.append("Maintain ");
                    }
                    if (collab.permissions().push()) {
                        result.append("Push ");
                    }
                    if (collab.permissions().pull()) {
                        result.append("Pull ");
                    }
                    result.append("\n");
                }
                result.append(String.format("   Profile: %s\n\n", collab.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching collaborators for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== ISSUES ====================
    @Tool(name = "getRepositoryIssues", description = "Get issues from a GitHub repository. Specify username, repository, state (open/closed/all), and optionally limit (default: 10, max: 100).")
    public String getRepositoryIssues(String username, String repository, String state, Integer limit) {
        try {
            String issueState = (state != null && !state.isEmpty()) ? state : "open";
            int actualLimit = (limit != null && limit > 0) ? limit : 10;

            List<GitHubIssue> issues = gitHubService.getRepositoryIssues(username, repository, issueState, actualLimit);

            if (issues == null || issues.isEmpty()) {
                return String.format("No %s issues found for repository: %s/%s", issueState, username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("%s issues for %s/%s:\n\n",
                    issueState.toUpperCase(), username, repository));

            for (GitHubIssue issue : issues) {
                result.append(String.format("#%d - %s\n", issue.number(), issue.title()));
                result.append(String.format("   State: %s\n", issue.state()));
                result.append(String.format("   Author: %s\n", issue.user().login()));
                result.append(String.format("   Created: %s\n", issue.createdAt()));
                result.append(String.format("   URL: %s\n\n", issue.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching issues for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    @Tool(name = "createIssue", description = "Create a new issue in a GitHub repository. Requires authentication. Specify username, repository, issue title, and optionally the issue body/description.")
    public String createIssue(String username, String repository, String title, String body) {
        try {
            if (!gitHubService.hasAuthentication()) {
                return "Error: Cannot create issue. GitHub token is not configured in the MCP server.";
            }

            String issueBody = (body != null && !body.isEmpty()) ? body : "";
            GitHubIssue issue = gitHubService.createIssue(username, repository, title, issueBody);

            return String.format("""
                    ✅ Issue created successfully!
                    
                    #%d - %s
                    Repository: %s/%s
                    State: %s
                    URL: %s
                    """,
                    issue.number(), issue.title(),
                    username, repository,
                    issue.state(),
                    issue.htmlUrl());
        } catch (Exception e) {
            return "Error creating issue in repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== PULL REQUESTS ====================
    @Tool(name = "getRepositoryPullRequests", description = "Get pull requests from a GitHub repository. Specify username, repository, state (open/closed/all), and optionally limit (default: 10, max: 100).")
    public String getRepositoryPullRequests(String username, String repository, String state, Integer limit) {
        try {
            String prState = (state != null && !state.isEmpty()) ? state : "open";
            int actualLimit = (limit != null && limit > 0) ? limit : 10;

            List<GitHubPullRequest> prs = gitHubService.getRepositoryPullRequests(username, repository, prState, actualLimit);

            if (prs == null || prs.isEmpty()) {
                return String.format("No %s pull requests found for repository: %s/%s", prState, username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("%s pull requests for %s/%s:\n\n",
                    prState.toUpperCase(), username, repository));

            for (GitHubPullRequest pr : prs) {
                String draftStatus = pr.draft() ? "🚧 DRAFT" : "";
                result.append(String.format("#%d - %s %s\n", pr.number(), pr.title(), draftStatus));
                result.append(String.format("   State: %s\n", pr.state()));
                result.append(String.format("   Author: %s\n", pr.user().login()));
                result.append(String.format("   Created: %s\n", pr.createdAt()));
                if (pr.mergedAt() != null) {
                    result.append(String.format("   Merged: %s\n", pr.mergedAt()));
                }
                result.append(String.format("   URL: %s\n\n", pr.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching pull requests for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== BRANCHES ====================
    @Tool(name = "getRepositoryBranches", description = "Get all branches from a GitHub repository. Specify the username and repository name.")
    public String getRepositoryBranches(String username, String repository) {
        try {
            List<GitHubBranch> branches = gitHubService.getRepositoryBranches(username, repository);

            if (branches == null || branches.isEmpty()) {
                return String.format("No branches found for repository: %s/%s", username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Branches for %s/%s:\n\n", username, repository));

            for (GitHubBranch branch : branches) {
                String protection = branch.protectedBranch() ? "🔒 Protected" : "";
                result.append(String.format("🌿 %s %s\n", branch.name(), protection));
                result.append(String.format("   Latest commit: %s\n\n",
                        branch.commit().sha().substring(0, 7)));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching branches for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== USER PROFILE ====================
    @Tool(name = "getUserProfile", description = "Get detailed profile information for any GitHub user. Specify the username.")
    public String getUserProfile(String username) {
        try {
            GitHubUser user = gitHubService.getUserProfile(username);

            if (user == null) {
                return "User not found: " + username;
            }

            return String.format("""
                    👤 GitHub Profile: %s
                    
                    Name: %s
                    Bio: %s
                    Company: %s
                    Location: %s
                    Blog: %s
                    Email: %s
                    
                    📊 Stats:
                       Public Repos: %d
                       Public Gists: %d
                       Followers: %d
                       Following: %d
                    
                    🔗 Profile URL: %s
                    📅 Joined: %s
                    """,
                    user.login(),
                    user.name() != null ? user.name() : "N/A",
                    user.bio() != null ? user.bio() : "N/A",
                    user.company() != null ? user.company() : "N/A",
                    user.location() != null ? user.location() : "N/A",
                    user.blog() != null ? user.blog() : "N/A",
                    user.email() != null ? user.email() : "N/A",
                    user.publicRepos(),
                    user.publicGists(),
                    user.followers(),
                    user.following(),
                    user.htmlUrl(),
                    user.createdAt());
        } catch (Exception e) {
            return "Error fetching profile for user '" + username + "': " + e.getMessage();
        }
    }

    @Tool(name = "getMyProfile", description = "Get the authenticated user's GitHub profile. Requires authentication.")
    public String getMyProfile() {
        try {
            if (!gitHubService.hasAuthentication()) {
                return "Error: Cannot access profile. GitHub token is not configured.";
            }

            GitHubUser user = gitHubService.getAuthenticatedUserProfile();

            return String.format("""
                    👤 Your GitHub Profile
                    
                    Username: %s
                    Name: %s
                    Bio: %s
                    Company: %s
                    Location: %s
                    Blog: %s
                    Email: %s
                    
                    📊 Stats:
                       Public Repos: %d
                       Public Gists: %d
                       Followers: %d
                       Following: %d
                    
                    🔗 Profile URL: %s
                    """,
                    user.login(),
                    user.name() != null ? user.name() : "N/A",
                    user.bio() != null ? user.bio() : "N/A",
                    user.company() != null ? user.company() : "N/A",
                    user.location() != null ? user.location() : "N/A",
                    user.blog() != null ? user.blog() : "N/A",
                    user.email() != null ? user.email() : "N/A",
                    user.publicRepos(),
                    user.publicGists(),
                    user.followers(),
                    user.following(),
                    user.htmlUrl());
        } catch (Exception e) {
            return "Error fetching your profile: " + e.getMessage();
        }
    }

    // ==================== RELEASES ====================
    @Tool(name = "getRepositoryReleases", description = "Get releases from a GitHub repository. Specify username, repository, and optionally limit (default: 10, max: 100).")
    public String getRepositoryReleases(String username, String repository, Integer limit) {
        try {
            int actualLimit = (limit != null && limit > 0) ? limit : 10;
            List<GitHubRelease> releases = gitHubService.getRepositoryReleases(username, repository, actualLimit);

            if (releases == null || releases.isEmpty()) {
                return String.format("No releases found for repository: %s/%s", username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Releases for %s/%s:\n\n", username, repository));

            for (GitHubRelease release : releases) {
                String releaseType = release.prerelease() ? "🚧 Pre-release"
                        : release.draft() ? "📝 Draft" : "✅ Release";
                result.append(String.format("%s %s (%s)\n",
                        releaseType, release.name(), release.tagName()));
                result.append(String.format("   Published: %s\n", release.publishedAt()));

                if (release.assets() != null && !release.assets().isEmpty()) {
                    result.append(String.format("   📦 Assets (%d):\n", release.assets().size()));
                    for (var asset : release.assets()) {
                        result.append(String.format("      - %s (%.2f MB, %d downloads)\n",
                                asset.name(),
                                asset.size() / 1024.0 / 1024.0,
                                asset.downloadCount()));
                    }
                }

                result.append(String.format("   URL: %s\n\n", release.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching releases for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    @Tool(name = "getLatestRelease", description = "Get the latest release from a GitHub repository. Specify the username and repository name.")
    public String getLatestRelease(String username, String repository) {
        try {
            GitHubRelease release = gitHubService.getLatestRelease(username, repository);

            if (release == null) {
                return String.format("No releases found for repository: %s/%s", username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Latest release for %s/%s:\n\n", username, repository));
            result.append(String.format("🏷️ %s (%s)\n", release.name(), release.tagName()));
            result.append(String.format("📅 Published: %s\n", release.publishedAt()));

            if (release.body() != null && !release.body().isEmpty()) {
                result.append(String.format("\n📝 Release Notes:\n%s\n", release.body()));
            }

            if (release.assets() != null && !release.assets().isEmpty()) {
                result.append(String.format("\n📦 Assets (%d):\n", release.assets().size()));
                for (var asset : release.assets()) {
                    result.append(String.format("   - %s (%.2f MB, %d downloads)\n",
                            asset.name(),
                            asset.size() / 1024.0 / 1024.0,
                            asset.downloadCount()));
                }
            }

            result.append(String.format("\n🔗 URL: %s\n", release.htmlUrl()));

            return result.toString();
        } catch (Exception e) {
            return "Error fetching latest release for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== GITHUB ACTIONS ====================
    @Tool(name = "getWorkflowRuns", description = "Get GitHub Actions workflow runs for a repository. Specify username, repository, and optionally limit (default: 10, max: 100).")
    public String getWorkflowRuns(String username, String repository, Integer limit) {
        try {
            int actualLimit = (limit != null && limit > 0) ? limit : 10;
            List<GitHubWorkflowRun> runs = gitHubService.getWorkflowRuns(username, repository, actualLimit);

            if (runs == null || runs.isEmpty()) {
                return String.format("No workflow runs found for repository: %s/%s", username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("GitHub Actions workflow runs for %s/%s:\n\n", username, repository));

            for (GitHubWorkflowRun run : runs) {
                String statusEmoji = switch (run.conclusion() != null ? run.conclusion() : run.status()) {
                    case "success" ->
                        "✅";
                    case "failure" ->
                        "❌";
                    case "cancelled" ->
                        "🚫";
                    case "in_progress" ->
                        "🔄";
                    default ->
                        "⏸️";
                };

                result.append(String.format("%s %s\n", statusEmoji, run.name()));
                result.append(String.format("   Branch: %s\n", run.headBranch()));
                result.append(String.format("   Status: %s", run.status()));
                if (run.conclusion() != null) {
                    result.append(String.format(" (%s)", run.conclusion()));
                }
                result.append("\n");
                result.append(String.format("   Created: %s\n", run.createdAt()));
                result.append(String.format("   URL: %s\n\n", run.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching workflow runs for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== FILE CONTENT ====================
    @Tool(name = "getFileContent", description = "Get the content of a file from a GitHub repository. Specify username, repository, and file path (e.g., 'README.md' or 'src/main.py').")
    public String getFileContent(String username, String repository, String path) {
        try {
            GitHubContent content = gitHubService.getFileContent(username, repository, path);

            if (content == null) {
                return String.format("File not found: %s in %s/%s", path, username, repository);
            }

            if (!"file".equals(content.type())) {
                return String.format("'%s' is not a file (it's a %s)", path, content.type());
            }

            // Decode base64 content
            String fileContent = "";
            if (content.content() != null && content.encoding() != null && "base64".equals(content.encoding())) {
                fileContent = new String(java.util.Base64.getDecoder().decode(content.content().replace("\n", "")));
            }

            return String.format("""
                    📄 File: %s
                    Repository: %s/%s
                    Size: %.2f KB
                    
                    Content:
```
                    %s
```
                    
                    🔗 URL: %s
                    """,
                    content.name(),
                    username, repository,
                    content.size() / 1024.0,
                    fileContent,
                    content.htmlUrl());
        } catch (Exception e) {
            return "Error fetching file '" + path + "' from repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== SEARCH ====================
    @Tool(name = "searchRepositories", description = "Search for GitHub repositories. Specify a search query (e.g., 'machine learning', 'language:python stars:>1000') and optionally limit (default: 10, max: 100).")
    public String searchRepositories(String query, Integer limit) {
        try {
            int actualLimit = (limit != null && limit > 0) ? limit : 10;
            List<GitHubRepository> repos = gitHubService.searchRepositories(query, actualLimit);

            if (repos == null || repos.isEmpty()) {
                return "No repositories found for query: " + query;
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Search results for '%s':\n\n", query));

            for (int i = 0; i < repos.size(); i++) {
                GitHubRepository repo = repos.get(i);
                result.append(String.format("%d. 📦 %s\n", i + 1, repo.fullName()));
                result.append(String.format("   Description: %s\n",
                        repo.description() != null ? repo.description() : "No description"));
                result.append(String.format("   Language: %s\n",
                        repo.language() != null ? repo.language() : "Unknown"));
                result.append(String.format("   ⭐ Stars: %d | 🍴 Forks: %d\n",
                        repo.stars(), repo.forks()));
                result.append(String.format("   URL: %s\n\n", repo.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error searching repositories: " + e.getMessage();
        }
    }

    // ==================== FORKS ====================
    @Tool(name = "getRepositoryForks", description = "Get forks of a GitHub repository. Specify username, repository, and optionally limit (default: 10, max: 100).")
    public String getRepositoryForks(String username, String repository, Integer limit) {
        try {
            int actualLimit = (limit != null && limit > 0) ? limit : 10;
            List<GitHubFork> forks = gitHubService.getRepositoryForks(username, repository, actualLimit);

            if (forks == null || forks.isEmpty()) {
                return String.format("No forks found for repository: %s/%s", username, repository);
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Forks of %s/%s:\n\n", username, repository));

            for (GitHubFork fork : forks) {
                result.append(String.format("🍴 %s\n", fork.fullName()));
                result.append(String.format("   Owner: %s\n", fork.owner().login()));
                result.append(String.format("   Created: %s\n", fork.createdAt()));
                result.append(String.format("   URL: %s\n\n", fork.htmlUrl()));
            }

            return result.toString();
        } catch (Exception e) {
            return "Error fetching forks for repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    @Tool(name = "forkRepository", description = "Fork a GitHub repository to your account. Requires authentication. Specify the username and repository name.")
    public String forkRepository(String username, String repository) {
        try {
            if (!gitHubService.hasAuthentication()) {
                return "Error: Cannot fork repository. GitHub token is not configured.";
            }

            GitHubRepository fork = gitHubService.forkRepository(username, repository);

            return String.format("""
                    🍴 Successfully forked repository!
                    
                    Original: %s/%s
                    Your fork: %s
                    URL: %s
                    """,
                    username, repository,
                    fork.fullName(),
                    fork.htmlUrl());
        } catch (Exception e) {
            return "Error forking repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    // ==================== STARRING ====================
    @Tool(name = "starRepository", description = "Star a GitHub repository. Requires authentication. Specify the username and repository name.")
    public String starRepository(String username, String repository) {
        try {
            if (!gitHubService.hasAuthentication()) {
                return "Error: Cannot star repository. GitHub token is not configured.";
            }

            gitHubService.starRepository(username, repository);
            return String.format("⭐ Successfully starred %s/%s", username, repository);
        } catch (Exception e) {
            return "Error starring repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }

    @Tool(name = "unstarRepository", description = "Unstar a GitHub repository. Requires authentication. Specify the username and repository name.")
    public String unstarRepository(String username, String repository) {
        try {
            if (!gitHubService.hasAuthentication()) {
                return "Error: Cannot unstar repository. GitHub token is not configured.";
            }

            gitHubService.unstarRepository(username, repository);
            return String.format("Removed star from %s/%s", username, repository);
        } catch (Exception e) {
            return "Error unstarring repository '" + username + "/" + repository + "': " + e.getMessage();
        }
    }
}
