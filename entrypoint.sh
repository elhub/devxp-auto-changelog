#!/bin/bash
set -e

generate_jwt() {
  # Current timestamp and expiration (10 minutes from now)
  NOW=$(date +%s)
  EXPIRATION=$((NOW + 600))

  # Create JWT header
  HEADER='{"alg":"RS256","typ":"JWT"}'

  # Create JWT payload
  PAYLOAD="{\"iat\":${NOW},\"exp\":${EXPIRATION},\"iss\":\"$APP_ID\"}"

  # Base64 encode header and payload
  base64_header=$(echo -n "${HEADER}" | openssl base64 -e -A | tr '+/' '-_' | tr -d '=')
  base64_payload=$(echo -n "${PAYLOAD}" | openssl base64 -e -A | tr '+/' '-_' | tr -d '=')

  # Add content of PRIVATE_KEY to private-key.pem
  echo "$PRIVATE_KEY" > /tmp/private-key.pem

  # Create signature
  signature=$(echo -n "${base64_header}.${base64_payload}" | \
  openssl dgst -sha256 -sign "/tmp/private-key.pem" | \
  openssl base64 -e -A | \
  tr '+/' '-_' | \
  tr -d '=')

  rm /tmp/private-key.pem

  # Combine all parts to create JWT
  echo "${base64_header}.${base64_payload}.${signature}"
}

# Get installation access token using JWT
get_access_token() {
  local jwt
  jwt=$(generate_jwt)

  curl -X POST \
  -H "Authorization: Bearer ${jwt}" \
  -H "Accept: application/vnd.github.v3+json" \
  --proxy http://proxy.elhub.cloud:3128 \
  "https://api.github.com/app/installations/$INSTALLATION_ID/access_tokens"
}

# Execute and get token
GITHUB_ACCESS_TOKEN=$(get_access_token | grep -o '"token": "[^"]*' | sed 's/"token": "//')

if [ -z "$GITHUB_ACCESS_TOKEN" ]; then
  echo "Failed to get GitHub access token. This is must for Renovate to work."
  exit 1
fi

echo "export RENOVATE_TOKEN='$GITHUB_ACCESS_TOKEN'" >> /tmp/secret.env

SOURCE_REPO="devxp-auto-release"
TARGET_REPO="devxp-system-release"

# Pass gh credentials to normal git commands for use in changelog generation
gh auth setup-git

# Generate changelog using the CLI tool
echo "Generating changelog..."
java -jar devxp-auto-changelog.jar -j -r https://github.com/elhub/$SOURCE_REPO -d $SOURCE_REPO

# Clone the target repository (where PR will be created)
echo "Cloning target repository..."
gh repo clone elhub/$TARGET_REPO
cd $TARGET_REPO

# Create a new branch
BRANCH_NAME="changelog-update-$(date +%Y%m%d-%H%M%S)"
git checkout -b "$BRANCH_NAME"

# Copy the generated changelog to the target repository
echo "Copying changelog to target repository..."
cp ../CHANGELOG.json ./CHANGELOG.json

# Commit and push changes
git config --global user.email "changelog-bot@notarealemail.com"
git config --global user.name "changelog-bot"
git add CHANGELOG.json
git commit -m "Update changelog"
git push origin "$BRANCH_NAME"

# Create pull request
echo "Creating pull request..."
gh pr create --repo elhub/$TARGET_REPO \
  --base main \
  --head "$BRANCH_NAME" \
  --title "Update Changelog" \
  --body "Automated changelog update generated by CI/CD pipeline."

echo "Pull request created successfully!"
