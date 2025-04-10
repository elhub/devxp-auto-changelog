# Start with a base image that includes Java
FROM docker.jfrog.elhub.cloud/eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Install necessary dependencies
RUN apt-get update && apt-get install -y \
    curl \
    jq \
    git \
    ca-certificates \
    gnupg \
    software-properties-common \
    && rm -rf /var/lib/apt/lists/*

# Install GitHub CLI for PR creation
RUN curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \
    && chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
    && apt-get update \
    && apt-get install -y gh \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables for JFrog access
ENV JFROG_URL="https://jfrog.elhub.cloud/artifactory"
ENV CLI_TOOL_PATH="elhub-mvn-release-local/no/elhub/devxp/devxp-auto-changelog/0.5.0/devxp-auto-changelog-0.5.0.jar"

# Download your CLI tool from JFrog Artifactory
RUN curl -L ${JFROG_URL}/${CLI_TOOL_PATH} -o devxp-auto-changelog.jar

# Make the CLI tool executable
RUN chmod +x devxp-auto-changelog.jar

# Add the CLI tool to the PATH
ENV PATH="/app:${PATH}"

# Add a script to handle the changelog generation and PR creation
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
