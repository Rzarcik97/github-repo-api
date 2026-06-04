package demo.githubrepoapi;

record GithubRepoDto(
        String name,
        boolean fork,
        OwnerDto owner
) {
    record OwnerDto(
            String login
    ) {}
}
