# SonarQube Plan (buy-01 / safe-zone)

Goal: SonarQube runs on the Hetzner server (Docker). GitHub Actions scans every push/PR,
fails the pipeline on a bad quality gate, scans on a schedule, and blocks merge until the
check passes. CD only runs if Sonar passes.

## Steps

1. Add SonarQube + Postgres to Docker on the Hetzner server (new compose file in repo).
2. Start SonarQube, open it in the browser, log in, change the admin password.
3. Create a project for buy-01, get a project key, generate a token, pick a quality profile.
4. Add `SONAR_TOKEN` and `SONAR_HOST_URL` as GitHub secrets.
5. Add a `sonar` job to the CI workflow(s): Maven services use `sonar:sonar`, frontend uses
   the sonar-scanner CLI. Each gets `fetch-depth: 0` and waits for the quality gate.
6. Add coverage: JaCoCo for the 5 Spring Boot services, lcov (Karma) for Angular, so Sonar
   can see real coverage numbers instead of 0%.
7. Add a scheduled (cron) scan workflow.
8. Branch protection: require PR review + require the Sonar check to pass before merge.
   Add a PR template asking "Sonar issues fixed or justified?".
9. Wire CD: deploy job `needs` the sonar job, so a failed quality gate blocks deploy.
10. README: add a SonarQube badge + dashboard link.
11. Prove it works: open a PR with a bug/code smell on purpose, see it fail red, fix it,
    see it pass green.
12. (Bonus, if time) Slack/email notification, SonarLint in the IDE.

We go through these in order, one at a time.
