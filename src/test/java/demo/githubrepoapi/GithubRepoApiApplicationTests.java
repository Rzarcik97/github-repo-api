package demo.githubrepoapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock
@AutoConfigureRestTestClient
class GithubRepoApiApplicationTests {

    @InjectWireMock
    WireMockServer wireMock;

    @Autowired
    RestTestClient restTestClient;

    @Test
    void shouldReturnNonForkRepositoriesWithBranches() {
        wireMock.stubFor(get(urlPathEqualTo("/users/octocat/repos"))
                .willReturn(okJson("""
                        [
                          {
                            "name": "hello-world",
                            "fork": false,
                            "owner": { "login": "octocat" }
                          },
                          {
                            "name": "forked-repo",
                            "fork": true,
                            "owner": { "login": "octocat" }
                          }
                        ]
                        """)));

        wireMock.stubFor(get(urlPathEqualTo("/repos/octocat/hello-world/branches"))
                .willReturn(okJson("""
                        [
                          {
                            "name": "main",
                            "commit": { "sha": "abc123" }
                          },
                          {
                            "name": "dev",
                            "commit": { "sha": "def456" }
                          }
                        ]
                        """)));

        List<RepositoryResponse> repos = restTestClient.get()
                .uri("/api/users/octocat/repositories")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<RepositoryResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(repos).hasSize(1);
        assertThat(repos.getFirst().repositoryName()).isEqualTo("hello-world");
        assertThat(repos.getFirst().ownerLogin()).isEqualTo("octocat");
        assertThat(repos.getFirst().branches()).hasSize(2);
        assertThat(repos.getFirst().branches()
                .getFirst().name()).isEqualTo("main");
        assertThat(repos.getFirst().branches()
                .getFirst().lastCommitSha()).isEqualTo("abc123");
    }

    @Test
    void shouldReturnEmptyListWhenAllRepositoriesAreForks() {
        wireMock.stubFor(get(urlPathEqualTo("/users/forkmaster/repos"))
                .willReturn(okJson("""
                        [
                          {
                            "name": "forked-repo",
                            "fork": true,
                            "owner": { "login": "forkmaster" }
                          }
                        ]
                        """)));

        List<RepositoryResponse> repos = restTestClient.get()
                .uri("/api/users/forkmaster/repositories")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<RepositoryResponse>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(repos).isEmpty();
    }

    @Test
    void shouldReturn404WithErrorBodyWhenUserDoesNotExist() {
        wireMock.stubFor(get(urlPathEqualTo("/users/nonexistent/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "message": "Not Found" }
                                """)));

        ApiErrorResponse error = restTestClient.get()
                .uri("/api/users/nonexistent/repositories")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody(ApiErrorResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(error.status()).isEqualTo(404);
        assertThat(error.message()).contains("nonexistent");
    }
}