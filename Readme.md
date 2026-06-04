# GitHub Repo API

A Spring Boot application that exposes a simplified endpoint for listing GitHub user repositories.

## Requirements

- Java 25
- Gradle
## Configuration

The application can be configured with the following environment variables:

| Variable                | Description                                                                       | Default  |
|-------------------------|-----------------------------------------------------------------------------------|----------|
| `GITHUB_TOKEN`          | GitHub personal access token (increases rate limit from 60 to 5000 requests/hour) | _(none)_ |
| `GITHUB_REPOS_PER_PAGE` | Number of repositories fetched per request (max 100)                              | `30`     |

## Running the application

Without token:
```bash
./gradlew bootRun
```

With token:
```bash
GITHUB_TOKEN=your_token ./gradlew bootRun
```

## Running tests

```bash
./gradlew test
```

## API

### List non-fork repositories for a GitHub user

**GET** `/api/users/{username}/repositories`

Returns all public repositories for the given GitHub username that are not forks. For each repository the response includes the repository name, owner login, and a list of branches with their latest commit SHA.

**Success response – 200 OK**

```json
[
  {
    "repositoryName": "hello-world",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123def456"
      }
    ]
  }
]
```

**User not found – 404 Not Found**

```json
{
  "status": 404,
  "message": "GitHub user 'nonexistent' not found"
}
```

> **Note:** Due to GitHub API pagination, the number of returned repositories is limited by `GITHUB_REPOS_PER_PAGE` (default 30).

## Architecture

The application follows a simple three-layer architecture:

- **Controller** – handles HTTP requests and responses
- **Service** – contains business logic (filtering forks, composing the response)
- **Client** – communicates with the GitHub API
  All classes reside in a single package.

## Backing API

GitHub REST API v3: https://developer.github.com/v3