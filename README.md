# auto-changelog

[<img src="https://img.shields.io/badge/repo-github-blue" alt="">](https://github.com/elhub/devxp-auto-changelog)
[<img src="https://img.shields.io/badge/issues-jira-orange" alt="">](https://jira.elhub.cloud/issues/?jql=project%20%3D%20%22Team%20Dev%22%20AND%20component%20%3D%20devxp-auto-changelog%20AND%20status%20!%3D%20Done)
[<img src="https://teamcity.elhub.cloud/app/rest/builds/buildType:(id:DevXp_DevXpAutoChangelog_PublishDocs)/statusIcon" alt="">](https://teamcity.elhub.cloud/project/DevXp_DevXpAutoChangelog?mode=builds#all-projects)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-changelog&metric=alert_status" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-changelog)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-changelog&metric=ncloc" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-changelog)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-changelog&metric=bugs" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-changelog)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-changelog&metric=vulnerabilities" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-changelog)
[<img src="https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.devxp%3Adevxp-auto-changelog&metric=coverage" alt="">](https://sonar.elhub.cloud/dashboard?id=no.elhub.devxp%3Adevxp-auto-changelog)

## Table of Contents

* [About](#about)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
* [Usage](#usage)
  * [Gradle](#gradle)
  * [Maven](#maven)
  * [Multi-module Maven](#multi-module-maven)
* [Testing](#testing)
* [Roadmap](#roadmap)
* [Contributing](#contributing)
* [Owners](#owners)
* [License](#license)

## About

**auto-changelog** is a small application that automates creation of changelogs for software projects based on git commits. It:

* Determines the version number based on the git tags in the repository
* Parses the commit log from the last version to determine the commit/release range to generate change logs for
* Generates the changelog from those commits

## Getting Started

### Prerequisites

This application requires Java 1.8 or later. In addition, auto-changelog must be run in a directory with an initialized git repository.

### Installation

#### CLI Application

The latest version can be downloaded from Elhub's internal artifactory under _elhub-bin/auto-changelog-cli/_.

To build the current version, run:

```sh
./gradlew assemble
```

To publish the executable jar to artifactory, run:

```sh
./gradlew publish
```

#### API

Add the library to dependencies.

Gradle:
```kotlin
implementation("no.elhub.devxp:auto-changelog-core:$version")
```

Maven:
```xml
<dependency>
  <groupId>no.elhub.devxp</groupId>
  <artifactId>auto-changelog-core</artifactId>
  <version>${version}</version>
</dependency>
```

## Usage

### CLI

```sh
java -jar auto-changelog-cli.jar --help
```

## Testing

The full suite of tests can be run using:

```sh
./gradlew test
```

## Roadmap

See the [open issues](https://github.com/elhub/dev-tools-auto-changelog/issues) for a list of proposed features (and known issues).

## Contributing

Contributing, issues and feature requests are welcome. See the
[Contributing](https://github.com/elhub/dev-tools-auto-changelog/blob/main/CONTRIBUTING.md) file.

## Owners

This project is developed by [Elhub](https://github.com/elhub). For the specific development group responsible for this
code, see the [Codeowners](https://github.com/elhub/dev-tools-auto-changelog/blob/main/CODEOWNERS) file.

## License

This project is [MIT](https://github.com/elhub/dev-tools-auto-changelog/blob/main/LICENSE.md) licensed.
