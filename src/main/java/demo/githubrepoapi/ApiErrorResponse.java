package demo.githubrepoapi;

public record ApiErrorResponse(
        int status, String message
) {}
