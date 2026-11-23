#!/usr/bin/env bash
# Build Warp in a scratch friendly way; the jar ends up in target/.
set -euo pipefail
cd "$(dirname "$0")"

mvn -B verify
tools/verify-jar.sh
printf 'built %s\n' "$(pwd)/target/warp-26.3.1.jar"
