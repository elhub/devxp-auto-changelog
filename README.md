# 🪵 devxp-auto-changelog 🪵

**devxp-auto-changelog** is a small application that automates creation of changelogs for software projects based on git commits and JIRA issues. It:

* Determines the version number based on the git tags in the repository
* Parses the commit log from the last version to determine the commit/release range to generate change logs for
* Generates the changelog from those commits

By default, auto-changelog groups the changelog by JIRA issue, listing all commits associated with each issue.
Optionally, it can also generate a more traditional changelog grouped by commit/tag.

## 🤖 Automated usage

### 🏙️ Publish in docs.elhub.cloud

This application integrates with the `docs`. To enable changelog generation, place a .changelog file in the .devxp directory
(`repo/.devxp/.changelog`). Changelogs will be included and updated in the docs page the next time the Publish Docs job
runs (~ every 3 hours).

## ✍️ Manual usage

### 📋 Prerequisites

This application requires Java 17 or later. In addition, auto-changelog must be run in a directory with an initialized git repository.

#### 🏗️ Building and publishing

The latest version can be downloaded from Elhub's internal artifactory under _elhub-bin/auto-changelog/_.

To build a local version, run:

```sh
./gradlew shadowJar
```

#### ⌨️ CLI

```sh
java -jar devxp-auto-changelog.jar --help
```

The following options are available for the CLI:

<!-- editorconfig-checker-disable -->
| Option                            | Description                                                                                            | Default             |
|-----------------------------------|--------------------------------------------------------------------------------------------------------|---------------------|
| `-j, --json`                      | Whether to write the changelog as JSON.                                                                | `false`             |
| `--from-tag=<tag>`                | Include commits after the specified tag (excluding the tag itself).                                    | `null` (from start) |
| `--to-tag=<tag>`                  | Include commits up to and including the specified tag.                                                 | `null` (to end)     |
| `--group-by-commit`               | Whether to create a more traditional changelog grouped by commit.                                      | `false`             |
| `--include-pr-description-issues` | Whether to include JIRA issues found in PR descriptions in addition to those found in commit messages. | `false`             |
| `-h, --help`                      | Print usage information and exit.                                                                      |                     |
<!-- editorconfig-checker-enable -->

## 🧪 Testing

The full suite of tests can be run using:

```sh
./gradlew test
```

## 🤝 Contributing

Contributing, issues, and feature requests are welcome. See the [Contributing](
https://github.com/elhub/devxp-auto-changelog/blob/main/.github/CONTRIBUTING) file.

## 🎩 Owners

This project is developed by [Elhub](https://www.elhub.no). For the specific development group responsible for this
code, see the [Codeowners](
https://github.com/elhub/devxp-auto-changelog/blob/main/.github/CODEOWNERS) file.
