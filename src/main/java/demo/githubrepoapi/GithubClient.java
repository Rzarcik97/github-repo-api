package demo.githubrepoapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.util.List;

@Component
public class GithubClient {

    private final RestClient restClient;

    @Value("${github.api.repos-per-page}")
    private int reposPerPage;

    GithubClient(@Value("${github.api.base-url}") String baseUrl,
                 @Value("${github.api.token:}") String token) {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10");

        if (!token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }

        this.restClient = builder.build();
    }

    List<GithubRepoDto> fetchRepositories(String username) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{username}/repos")
                            .queryParam("per_page", reposPerPage)
                            .build(username))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException(username);
            }
            throw e;
        }
    }

    List<GithubBranchDto> fetchBranches(String username, String repoName) {
        return restClient.get()
                .uri("/repos/{username}/{repoName}/branches", username, repoName)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
