package demo.githubrepoapi;

import java.util.List;

public record RepositoryResponse(
        String repositoryName,
        String ownerLogin,
        List<BranchInfoDto> branches
) {}
