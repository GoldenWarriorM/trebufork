#!/usr/bin/env bash
set -euo pipefail

# Bumps the Magisk module version, commits it, tags it and pushes both. The tag
# push triggers the GitHub Actions release workflow (.github/workflows/release.yml)
# which builds the APK, signs it with the vendored platform key, packages the
# module, publishes the GitHub Release and refreshes magisk/update.json.
#
# Short release notes for Magisk's update dialog are taken from
# magisk/changelog.md (plain Markdown, English); edit that file before running
# this script. update.json points its "changelog" field at the raw file so
# Magisk shows the notes in the update window.
#
# versionCode convention (matches existing 0.5.0 -> 500, 0.5.1 -> 501):
#   major * 1000 + minor * 100 + patch

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

VERSION="${1:-}"
if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>    e.g. $0 0.6.0" >&2
    exit 1
fi
if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Version must be in X.Y.Z form, got: $VERSION" >&2
    exit 1
fi

IFS=. read -r MAJOR MINOR PATCH <<< "$VERSION"
CODE=$((MAJOR * 1000 + MINOR * 100 + PATCH))
TAG="v$VERSION"

# Sanity: never go backwards.
CUR_VER="$(grep -m1 '^version=' magisk-template/module.prop | cut -d= -f2 | sed 's/^v//')"
if [ -n "$CUR_VER" ] && [ "$VERSION" = "$CUR_VER" ]; then
    echo "Version $VERSION is already set in module.prop; nothing to bump." >&2
    exit 1
fi

echo "Bumping module to $TAG (versionCode=$CODE)..."

sed -i "s/^version=v.*/version=$TAG/" magisk-template/module.prop
sed -i "s/^versionCode=.*/versionCode=$CODE/" magisk-template/module.prop

cat > magisk/update.json <<EOF
{
  "version": "$TAG",
  "versionCode": $CODE,
  "zipUrl": "https://github.com/GoldenWarriorM/trebufork/releases/download/${TAG}/Trebufork-magisk-${TAG}.zip",
  "changelog": "https://raw.githubusercontent.com/GoldenWarriorM/trebufork/main/magisk/changelog.md"
}
EOF

git add magisk-template/module.prop magisk/update.json magisk/changelog.md
git commit -m "Bump module version to $TAG"
git tag "$TAG"
# Release into the public module repo (GoldenWarriorM/trebufork), not the
# private history archive. Local dev branch publish mirrors public/main.
REMOTE="${RELEASE_REMOTE:-public}"
git push "$REMOTE" HEAD:main
git push "$REMOTE" "$TAG"

echo "Released $TAG. GitHub Actions is building and publishing the module;"
echo "watch it at https://github.com/GoldenWarriorM/trebufork/actions"
