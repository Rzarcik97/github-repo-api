package demo.githubrepoapi;

public record BranchInfoDto(
        String name,
        String lastCommitSha
) {}
