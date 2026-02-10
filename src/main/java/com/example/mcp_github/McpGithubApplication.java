package com.example.mcp_github;

import org.springframework.boot.SpringApplication;
import com.example.mcp_github.service.GitHubTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class McpGithubApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpGithubApplication.class, args);
	}

	@Bean
	public List<ToolCallback> tools(GitHubTools gitHubTools) {
		return List.of(ToolCallbacks.from(gitHubTools));
	}

}
