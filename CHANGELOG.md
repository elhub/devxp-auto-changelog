# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [UNRELEASED]

### Changed

- Refactor to use FunSpec

## [0.5.0] - 2025-03-24

### Added

- Add support for generating changelog in json format [minor] (#23)

## [0.4.0] - 2025-03-17

### Added

- Add option to generate changelog based on url [minor]
- Add changelog file to make docs generate changelog (for testing)

### Changed

- Update to use shadowJar
- Update README
- Update to match new jira

### Fixed

- Fix errors in publishing configuration

### Unknown

- Merge pull request #19 from elhub/feat/add-renovate-config
- Enable Renovate scanning for this repo
- Move .changelog to .devxp folder
- Bump devxp-build-config version for tc upgrade

## [0.3.6] - 2025-01-29

### Changed

- Update build-config version
- Refactor multi-module setup again [patch]

## [0.3.5] - 2025-01-24

### Changed

- Remove artifactoryRepository param from tc pipeline [patch]

## [0.3.4] - 2025-01-24

### Changed

- Refactor the gradle structure again [patch]

## [0.3.3] - 2025-01-24

### Unknown

- Make root project a library to allow jfrog publishing [patch]

## [0.3.2] - 2025-01-24

### Changed

- Update gradle versions and clean up structure [patch]
- Update title keywords to support conventional commits
- Remove pausing
- Update TC settings for teamcity1

### Fixed

- Fix the default project files and clean out phabricator legacy (#1)
- Fix breaking TC settings
- Fix ID in TC settings

### Unknown

- chore: bump build config version
- Merge branch 'main' of github.com:elhub/devxp-auto-changelog
- TeamCity change in 'DevXP' project: bulk pause/activate with comment: Paused due to migration to teamcity1.

## [0.3.1] - 2023-06-12

### Fixed

- Fix missing entries in ansible projects that use devxp-auto-release

## [0.3.0] - 2023-06-09

### Added

- Add dependency check report.
- Add new elhub gradle plugins
- Add tests to cli of AutoChangelog
- Add sonar tests
- Add VCS trigger

### Changed

- Update libraries with dependency catalogs + JDK17
- Update kotlin-core to 0.1.0.
- Update pom.xml to 0.50.2.
- Update no.elhub.devxp.kotlin-core to 0.0.15.
- Update dependencies.
- Update of settings by devxp-buildbot
- Update of settings by devxp-buildbot
- Update of settings by devxp-buildbot
- Update TC build config
- Remove empty docs folder
- Updated elhub-gradle and use modules
- Remove allure report in TC
- Remove TC patch
- Update gradle to use elhub-gradle
- Update TeamCity settings to version 0.41.0

### Fixed

- Fix gradle publishing config
- Fix breaking tests
- Fix common references
- Fix broken build

### Unknown

- Set java target to 1.8
- Upgrade devxp-gradle-plugin to 0.1.2.
- Upgrade to OpenJDK 17.
- Generate compare url for latest release
- Set up sonar for multiple modules
- Merge branch 'main' of github.com:elhub/devxp-auto-changelog
- TeamCity change in 'DevXP / devxp-auto-changelog' project: Project editing is enabled
- Merge branch 'main' of github.com:elhub/devxp-auto-changelog

## [0.2.1] - 2022-07-27

### Fixed

- Fix for repos containing non-semver compliant tags

## [0.2.0] - 2022-07-26

### Added

- Add missing license

### Changed

- Change project structure to multi-module
- Remove TC-generated patches to build configs
- Update elhub-gradle and kotlin versions
- Remove commit trigger
- Change 'unknown' title keyword to 'other'
- Update .arcconfig

### Fixed

- Fix published artifact ids
- Fix artifacts names and publishing config
- Fix failing AutoRelease build
- Fix PublishDocs dependency in teamcity settings
- Fix PublishDocs initialization in teamcity settings
- Fix import in teamcity settings

### Unknown

- Disable allure
- Chore - update dependencies
- TeamCity change in 'DevXP / devxp-auto-changelog' project: general settings of 'Publish Docs' build configuration were updated
- Rename to devxp-auto-changelog

## [0.1.4] - 2021-10-25

### Fixed

- Fix handling of non-existent output dir/file

## [0.1.3] - 2021-10-25

### Added

- Add cli option for custom input filename
- Add possibility to append to existing changelog

### Unknown

- Use only jira-linked commits

## [0.1.2] - 2021-10-25

### Added

- Add links for jira issues

## [0.1.1] - 2021-10-25

### Added

- Add dir/file paths as cli options

### Fixed

- Fix duplicate changelog entry

### Unknown

- Revert "Release with github actions"

## [0.1.0] - 2021-10-24

### Added

- Add vcs trigger to AutoRelease build config
- Add teamcity configuration
- Add jira issue IDs to change entries
- Add basic commandline interface implementation
- Create Changelist with correct change categories
- Implement writing different change categories from the Changelist
- Add extension to get lines from file until predicate
- Create a changelist from git log
- Add Unreleased version object
- Add optional predicate arg to GitRepo#constructLog fun
- Add extensions for git commits and collections
- Add test for ChangelogReader
- Add explicit exception for non-matched Version
- Add basic ChangelogWriter
- Add ChangelogReader that gets latest version from changelog

### Changed

- Refactor ChangelogWrite#writeToString fun to use Changelist

### Fixed

- Fix order in the generated Changelist
- Fix order of changelist entries

### Unknown

- Construct GitLog from a git repo
- Get file contents after certain line
- Optimize ChangelogReader#read function
- Read changelog lines into Changelog instance
- Prepend new content before existing release in changelog
- Extract git refs with GitRepo class
- Handle incomplete and empty changelogs
- Initial commit