# auto-changelog

## About

**auto-changelog** is a small application that automates creation of changelogs for software projects based on git commits. It:

* Determines the version number based on the git tags in the repository
* Parses the commit log from the last version to determine the commit/release range to generate change logs for
* Generates the changelog from those commits

## Getting Started

### Prerequisites

This application requires Java 17 or later. In addition, auto-changelog must be run in a directory with an initialized git repository.

### Installation

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

## Contributing

Contributing, issues and feature requests are welcome. See the [Contributing](https://github.com/elhub/devx-auto-changelog/blob/main/.github/CONTRIBUTING) file.

## Owners

This project is developed by [Elhub](https://www.elhub.no). For the specific development group responsible for this
code, see the [Codeowners](https://github.com/elhub/devxp-auto-changelog/blob/main/.github/CODEOWNERS) file.
