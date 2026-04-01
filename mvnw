#!/bin/sh

set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MVN_DIR="$BASE_DIR/.mvn"
MVN_VERSION="3.9.9"
DIST_NAME="apache-maven-${MVN_VERSION}"
DIST_DIR="$MVN_DIR/${DIST_NAME}"
ARCHIVE="$MVN_DIR/${DIST_NAME}-bin.zip"
DOWNLOAD_URL="https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/${DIST_NAME}-bin.zip"

if [ ! -x "$DIST_DIR/bin/mvn" ]; then
  mkdir -p "$MVN_DIR"
  if [ ! -f "$ARCHIVE" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$DOWNLOAD_URL" -o "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "$DOWNLOAD_URL" -O "$ARCHIVE"
    else
      echo "Neither curl nor wget is available. Please install one of them." >&2
      exit 1
    fi
  fi

  if command -v unzip >/dev/null 2>&1; then
    unzip -q -o "$ARCHIVE" -d "$MVN_DIR"
  else
    echo "unzip is required to extract Maven." >&2
    exit 1
  fi
fi

exec "$DIST_DIR/bin/mvn" "$@"
