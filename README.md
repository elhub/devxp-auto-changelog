# devxp-auto-changelog

[<img src="https://img.shields.io/badge/repo-github-blue" alt="">](https://github.com/elhub/devxp-auto-changelog)
[<img src="https://img.shields.io/badge/issues-jira-orange" alt="">](
https://jira.elhub.cloud/issues/?jql=project%20%3D%20%22Team%20Dev%22%20AND%20component%20%3D%20devxp-auto-changelog%20AND%20status%20!%3D%20Done)
[<img src="https://teamcity.elhub.cloud/app/rest/builds/buildType:(id:DevXp_DevXpAutoChangelog_PublishDocs)/statusIcon" alt="">](
https://teamcity.elhub.cloud/project/DevXp_DevXpAutoChangelog?mode=builds#all-projects)

## About

**devxp-auto-changelog** is a small application that automates creation of changelogs for software projects based on git commits. It:

* Determines the version number based on the git tags in the repository
* Parses the commit log from the last version to determine the commit/release range to generate change logs for
* Generates the changelog from those commitsdd

## Table of Contents

- [auto-changelog](#auto-changelog)
  - [About](#about)
  - [Table of Contents](#table-of-contents)
  - [Automated usage](#automated-usage)
    - [Running in TeamCity pipeline](#running-in-teamcity-pipeline)
  - [Manual usage](#manual-usage)
    - [Prerequisites](#prerequisites)
    - [CLI Application](#cli-application)
    - [CLI](#cli)
  - [Configuration](#configuration)
  - [Testing](#testing)
  - [Contributing](#contributing)
  - [Owners](#owners)

## Automated usage

### Running in TeamCity pipeline

This application integrates with the `docs`. To enable changelog generation, place a .changelog file in the .devxp directory
(`repo/.devxp/.changelog`). Changelogs will be included and updated in the docs page the next time the Publish Docs job
runs (~every 3 hours).

## Manual usage

### Prerequisites

This application requires Java 17 or later. In addition, auto-changelog must be run in a directory with an initialized git repository.

#### CLI Application

The latest version can be downloaded from Elhub's internal artifactory under _elhub-bin/auto-changelog/_.

To build the current version, run:

```sh
./gradlew assemble
```

To publish the executable jar to artifactory, run:

```sh
./gradlew publish
```

#### CLI

```sh
java -jar auto-changelog-cli.jar --help
```

The following options are available for the CLI:

| Option                        | Description                                                                 | Default          |
|-------------------------------|-----------------------------------------------------------------------------|------------------|
| `-r, --remote-path=<remotePath>` | URL to remote repository.                                                  | `.`              |
| `-j, --json`                  | Whether to write the changelog as JSON.                                     | `false`          |
| `-d, --dir-path=<repoPath>`   | Path to directory with git repository.                                      | `.`              |
| `-n, --changelog-name=<inputFileName>` | Input changelog file name.                                              | `CHANGELOG.md`   |
| `-o, --output-dir=<outputDir>`| Output directory path to which changelog file will be written.              | `.`              |
| `-f, --file-name=<outputFileName>` | Output file name.                                                         | `CHANGELOG.md`   |
| `--up-to=<upToTag>`           | Include commits up to and including the specified tag.                      |                  |
| `--after=<afterTag>`          | Include commits after the specified tag (excluding the tag itself).         |                  |
| `--for-tag=<tagName>`         | Generate changelog for the specified tag by comparing with its previous tag.| |
| `--jira`                      | Filter commits to include only those with Jira issues and fetch Jira details. |                  |
| `-h, --help`                  | Show this help message and exit.                                            |                  |
| `-V, --version`               | Print version information and exit.                                         |                  |

## Configuration

The application can be configured using a `config.yml` file placed in the root directory of the project. The following is an example configuration:

```yaml
version: 1.0
changelog:
  output: CHANGELOG.md
  template: default
```

## Testing

The full suite of tests can be run using:

```sh
./gradlew test
```

## Contributing

Contributing, issues, and feature requests are welcome. See the [Contributing](
https://github.com/elhub/devxp-auto-changelog/blob/main/.github/CONTRIBUTING) file.

## Owners

This project is developed by [Elhub](https://www.elhub.no). For the specific development group responsible for this
code, see the [Codeowners](
https://github.com/elhub/devxp-auto-changelog/blob/main/.github/CODEOWNERS) file.
