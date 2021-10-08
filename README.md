# auto-changelog

<!-- PROJECT SHIELDS -->
![TeamCity Build](https://teamcity.elhub.cloud/app/rest/builds/buildType:(id:Tools_DevToolsAutoChangelog_AutoChangelog)/statusIcon)
[![Quality Gate Status](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-changelog&metric=alert_status)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-changelog)
[![Lines of Code](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-changelog&metric=ncloc)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-changelog)

[![Vulnerabilities](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-changelog&metric=vulnerabilities)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-changelog)
[![Bugs](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-changelog&metric=bugs)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-changelog)
[![Code Smells](https://sonar.elhub.cloud/api/project_badges/measure?project=no.elhub.tools%3Adev-tools-auto-changelog&metric=code_smells)](https://sonar.elhub.cloud/dashboard?id=no.elhub.tools%3Adev-tools-auto-changelog)

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

The latest version can be downloaded from Elhub's internal artifactory under _elhub-bin/auto-changelog/_.

To build the current version, run:

```sh
./gradlew assemble
```

To publish the executable jar to artifactory, run:

```sh
./gradlew publish
```

## Usage

To run the project on the existing repository for a gradle project, use:

```sh
java -jar auto-changelog.jar
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
