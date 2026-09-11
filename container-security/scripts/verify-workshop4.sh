#!/usr/bin/env bash
#
# DigiBank Workshop 4 - local verification sequence.
#
# Replays the main dependency and container security checks locally so the same controls can
# be transposed to GitHub Actions. Run from the repository root.
#
# Usage:
#   ./container-security/scripts/verify-workshop4.sh
#
set -euo pipefail

echo "==> 1/6 Clean Maven build and tests"
mvn clean verify

echo "==> 2/6 Export dependency tree"
mvn dependency:tree > dependency-tree.txt

echo "==> 3/6 OWASP Dependency-Check (fails on CVSS >= 7)"
mvn org.owasp:dependency-check-maven:check

echo "==> 4/6 Build Docker image"
docker build -t digibank:local .

echo "==> 5/6 Export image metadata and history"
docker image inspect digibank:local > docker-image-inspect.json
docker history digibank:local --no-trunc > docker-image-history.txt

echo "==> 6/6 Trivy image scan (HIGH/CRITICAL, exit 1 on findings)"
trivy image --severity HIGH,CRITICAL --exit-code 1 digibank:local

echo "Workshop 4 local verification completed successfully."
