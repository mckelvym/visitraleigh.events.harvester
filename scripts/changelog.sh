#!/bin/bash

# Script to prepend markdown bullet list of git changes since a given tag to CHANGELOG.md
# Usage: ./changelog.sh <tag-name>

CHANGELOG_FILE="CHANGELOG.md"

if [ $# -eq 0 ]; then
    echo "Usage: $0 <tag-name>"
    echo "Example: $0 1.0.0"
    exit 1
fi

TAG=$1

# Check if tag exists
if ! git rev-parse "$TAG" >/dev/null 2>&1; then
    echo "Error: Tag '$TAG' not found"
    exit 1
fi

# Create a temporary file
TEMP_FILE=$(mktemp)

# 1. Write the new changes to the temp file
echo "# Changes since $TAG" > "$TEMP_FILE"
echo "" >> "$TEMP_FILE"

# --reverse lists oldest commits first within the block
git log "$TAG..HEAD" --reverse --pretty=format:"- %s" >> "$TEMP_FILE"

echo "" >> "$TEMP_FILE"
echo "" >> "$TEMP_FILE"

# 2. Append the existing changelog content to the temp file
if [ -f "$CHANGELOG_FILE" ]; then
    cat "$CHANGELOG_FILE" >> "$TEMP_FILE"
fi

# 3. Overwrite the original file with the new content
mv "$TEMP_FILE" "$CHANGELOG_FILE"

cat "$CHANGELOG_FILE"
