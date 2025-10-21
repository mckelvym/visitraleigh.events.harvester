#!/bin/bash

# Script to generate markdown bullet list of git changes since a given tag
# Usage: ./git-changes.sh <tag-name>

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

# Generate markdown list in reverse chronological order
echo "# Changes since $TAG"
echo ""

git log "$TAG..HEAD" --reverse --pretty=format:"- %s"

echo ""
