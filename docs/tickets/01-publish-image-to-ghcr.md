# Ticket: Publish Docker Image to GHCR

GitHub issue: https://github.com/Valsuh45/digibank-introduction-to-security-in-DevOps-Lab2/issues/12

## Purpose

Publish the DigiBank Docker image to GitHub Container Registry so the team has a reusable deployment artifact instead of rebuilding from source every time.

## Scope

- Add a GitHub Actions job that builds the application image after tests pass.
- Log in to `ghcr.io` with `GITHUB_TOKEN`.
- Tag the image with the branch SHA and a stable tag for `main`.
- Push only after the image passes the existing container security scan.
- Document how to pull and run the image.

## Acceptance Criteria

- A successful `main` workflow publishes an image under `ghcr.io/<owner>/<repo>`.
- Pull instructions are documented.
- The workflow does not publish images from untrusted pull requests.
- The image scan remains required before publish.
