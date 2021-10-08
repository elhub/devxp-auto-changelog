# 2. auto-generate-changelog

Date: 2021-09-13

## Status

Proposed

## Context

We want to include changelog information during releases of individual modules, but if it's going to be a manual process - it's going to be forgotten more often than not.

Since the changelog has to be updated during the release it makes sense to put this functionality in `auto-release` app. This will also allow us to reuse code that is used to get repo/commits information that we already have in this application.

## Implementation details

* We will use the [keepachangelog](https://keepachangelog.com/en/1.0.0/) template for the changelog files

* The changes are parsed for the commit range, which equals to all commits from the start until current release. But we will add a possibility to limit commits based on existing CHANGELOG.md file in the repository and latest information it contains.

* We do not want to create a "git log" in a changelog file, so only messages that contain jira links are going to be used. We will also include the links to jira issues.

* After some minor discussions we also decided that it is easier to ensure better quality of commit messages than jira issue titles, so we will use commit messages directly to generate a changelog. 

## Consequences

* Automatically updated changelogs for each new release
* Old changes will have to be put in the changelogs manually to limit the overall changelog generation
