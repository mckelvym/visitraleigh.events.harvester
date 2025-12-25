#!/bin/bash

# Script to prepend markdown bullet list of git changes since a given tag to CHANGELOG.md
# Usage: ./changelog.sh <from-tag> [<to-tag>]
# Examples:
#   ./changelog.sh 1.3.0          # Append changes since 1.3.0 to existing section or create new
#   ./changelog.sh 1.2.0 1.3.0    # Create "# 1.3.0" with changes since 1.2.0

CHANGELOG_FILE="CHANGELOG.md"

if [ $# -eq 0 ] || [ $# -gt 2 ]; then
    echo "Usage: $0 <from-tag> [<to-tag>]"
    echo "Example: $0 1.3.0          # Append to existing 'Changes since 1.3.0' or create new"
    echo "Example: $0 1.2.0 1.3.0    # Create '# 1.3.0' with changes since 1.2.0"
    exit 1
fi

if [ $# -eq 1 ]; then
    # One tag: append to existing section or create new
    FROM_TAG=$1
    TO_REF="HEAD"
    HEADING="Changes since $FROM_TAG"

    # Check if tag exists
    if ! git rev-parse "$FROM_TAG" >/dev/null 2>&1; then
        echo "Error: Tag '$FROM_TAG' not found"
        exit 1
    fi

    # Check if heading already exists
    if [ -f "$CHANGELOG_FILE" ] && grep -q "^# $HEADING$" "$CHANGELOG_FILE"; then
        # Append to existing section
        TEMP_FILE=$(mktemp)
        CHANGES_FILE=$(mktemp)

        # Get new changes and write to temp file
        git log "$FROM_TAG..$TO_REF" --reverse --pretty=format:"- %s" > "$CHANGES_FILE"

        if [ ! -s "$CHANGES_FILE" ]; then
            echo "No new changes to add"
            rm "$CHANGES_FILE"
            exit 0
        fi

        # Use awk to insert new items after the heading
        awk -v heading="# $HEADING" -v changesfile="$CHANGES_FILE" '
            BEGIN { inserted = 0 }
            {
                print $0
                if (!inserted && $0 == heading) {
                    # Skip the blank line after heading
                    getline
                    print $0
                    # Insert new changes from file
                    while ((getline line < changesfile) > 0) {
                        print line
                    }
                    close(changesfile)
                    inserted = 1
                }
            }
        ' "$CHANGELOG_FILE" > "$TEMP_FILE"

        mv "$TEMP_FILE" "$CHANGELOG_FILE"
        rm "$CHANGES_FILE"
        echo "Appended changes to existing section: $HEADING"
    else
        # Create new section (original behavior)
        TEMP_FILE=$(mktemp)

        echo "# $HEADING" > "$TEMP_FILE"
        echo "" >> "$TEMP_FILE"
        git log "$FROM_TAG..$TO_REF" --reverse --pretty=format:"- %s" >> "$TEMP_FILE"
        echo "" >> "$TEMP_FILE"
        echo "" >> "$TEMP_FILE"

        if [ -f "$CHANGELOG_FILE" ]; then
            cat "$CHANGELOG_FILE" >> "$TEMP_FILE"
        fi

        mv "$TEMP_FILE" "$CHANGELOG_FILE"
        echo "Created new section: $HEADING"
    fi
else
    # Two tags: create section with heading as second tag, changes from first tag to HEAD
    FROM_TAG=$1
    TO_TAG=$2
    HEADING="$TO_TAG"

    # Check if first tag exists
    if ! git rev-parse "$FROM_TAG" >/dev/null 2>&1; then
        echo "Error: Tag '$FROM_TAG' not found"
        exit 1
    fi

    TEMP_FILE=$(mktemp)

    echo "# $HEADING" > "$TEMP_FILE"
    echo "" >> "$TEMP_FILE"
    git log "$FROM_TAG..HEAD" --reverse --pretty=format:"- %s" >> "$TEMP_FILE"
    echo "" >> "$TEMP_FILE"
    echo "" >> "$TEMP_FILE"

    if [ -f "$CHANGELOG_FILE" ]; then
        cat "$CHANGELOG_FILE" >> "$TEMP_FILE"
    fi

    mv "$TEMP_FILE" "$CHANGELOG_FILE"
    echo "Created section: # $HEADING (changes since $FROM_TAG)"
fi

cat "$CHANGELOG_FILE"
