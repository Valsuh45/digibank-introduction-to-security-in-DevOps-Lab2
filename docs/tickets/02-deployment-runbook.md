# Ticket: Deployment Runbook

GitHub issue: https://github.com/Valsuh45/digibank-introduction-to-security-in-DevOps-Lab2/issues/13

## Purpose

Create a clear deployment guide for running DigiBank from the published GHCR image.

## Scope

- Document required environment variables.
- Explain PostgreSQL requirements.
- Provide a Compose example that uses the registry image instead of building locally.
- Explain health-check and rollback basics.
- Keep secrets out of the repository.

## Acceptance Criteria

- A teammate can deploy the app from GHCR using only the runbook.
- The runbook clearly separates local development from deployed runtime configuration.
- No real credentials or secret defaults are committed.
