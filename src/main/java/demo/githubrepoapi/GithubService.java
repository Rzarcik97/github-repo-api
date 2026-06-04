package demo.githubrepoapi;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GithubService {

    private final GithubClient githubClient;

    GithubService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<RepositoryResponse> getNonForkRepositories(String username) {
        return githubClient.fetchRepositories(username).stream()
                .filter(repo -> !repo.fork())
                .map(repo -> new RepositoryResponse(
                        repo.name(),
                        repo.owner().login(),
                        fetchBranches(username, repo.name())
                ))
                .toList();
    }

    private List<BranchInfoDto> fetchBranches(String username, String repoName) {
        return githubClient.fetchBranches(username, repoName).stream()
                .map(branch -> new BranchInfoDto(
                        branch.name(),
                        branch.commit().sha()))
                .toList();
    }
}
