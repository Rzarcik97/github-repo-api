package demo.githubrepoapi;

record GithubBranchDto(
        String name,
        CommitDto commit
) {
    record CommitDto(
            String sha
    ) {}
}
