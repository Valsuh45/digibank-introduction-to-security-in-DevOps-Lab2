# Independent Workshop 1 review

Reviewed against the Workshop 1 PDF objectives A1.1–A1.8 and sections 7.14/8.1, plus the agreed twelve-item closeout checklist. Three implementation agents handled domain security, HTTP/integration tests, and documentation. A separate senior reviewer inspected the combined changes; the coordinating agent executed the checks.

## Findings and resolution

- P2: account-list OpenAPI incorrectly advertised 404 for an unknown customer. Corrected to 200 with an empty list; the real HTTP campaign now asserts this behavior.
- P3: Python bytecode was not excluded from Git. Added cache and bytecode exclusions.
- The subsequent read-only review confirmed both fixes and found no new actionable code defects.

The coordinator's PMD run additionally identified excessive complexity in transfer execution. Extracting account-state validation preserved behavior and restored the gate. The reviewer inspected that refactor. PostgreSQL HTTP verification also required timestamp comparisons to allow microsecond storage rounding, while all other record fields remain checked.

## Verified scope

See [the evidence index](README.md) for 85 passing tests, PMD/SpotBugs results, Docker/PostgreSQL execution, 57 HTTP requests, restart persistence, and screenshots. The rollback integration test flushes both balance updates and the audit insertion before injecting failure; subsequent reads verify rollback outside a test-managed transaction.

The reviewer inspected the implementation supporting these results but did not independently rerun Maven or Docker. Review approval is not a claim of exhaustive security assurance. Authentication, ownership enforcement, concurrency locking, and the later SAST/DAST campaigns remain outside this Workshop 1 closeout.

## Submission boundary

The final tree is local and uncommitted. The existing remote CI run covers only its base commit. Capture the final IDE project tree and attach a CI run for the eventual committed revision before claiming a complete final submission package.
