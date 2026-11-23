#!/usr/bin/env bash
# Checks that the packaged jar is a usable plugin jar: the plugin descriptor is
# inside and the main class is present.
set -euo pipefail
cd "$(dirname "$0")/.."

jar="${1:-target/warp-26.3.1.jar}"
[ -f "$jar" ] || { printf 'error: %s not found, run mvn package first\n' "$jar" >&2; exit 1; }

if ! jar tf "$jar" | grep -q '^plugin.yml$'; then
    printf 'error: %s does not contain plugin.yml\n' "$jar" >&2
    exit 1
fi

if ! jar tf "$jar" | grep -q '^com/mdmuntakim/warp/WarpPlugin.class$'; then
    printf 'error: %s does not contain com.mdmuntakim.warp.WarpPlugin\n' "$jar" >&2
    exit 1
fi

printf 'plugin jar ok: %s\n' "$jar"
