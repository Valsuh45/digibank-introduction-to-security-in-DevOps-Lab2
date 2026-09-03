# Ticket: Supply Chain Evidence

GitHub issue: https://github.com/Valsuh45/digibank-introduction-to-security-in-DevOps-Lab2/issues/14

## Purpose

Strengthen the DevSecOps evidence by attaching machine-readable build artifacts to releases or workflow runs.

## Scope

- Evaluate SBOM generation for the Maven build or Docker image.
- Evaluate artifact attestation or image signing if required by the course.
- Store generated evidence as GitHub Actions artifacts or release assets.
- Document how reviewers can verify the evidence.

## Acceptance Criteria

- CI produces at least one useful supply-chain evidence artifact.
- The artifact is tied to a commit SHA.
- Documentation explains what the artifact proves and what it does not prove.
