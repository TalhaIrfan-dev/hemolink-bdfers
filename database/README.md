# HemoLink database scripts

Run the scripts in this order. Each script is **non-destructive**: it never drops tables or deletes data.

| Order | Script | What it does | When |
|---|---|---|---|
| 1 | `database_schema.sql` | Creates `hemolink_db` and the original tables | Fresh install only (it fails if the database already exists) |
| 2 | `003_guidance_and_health_checks.sql` | Adds donation guidance settings, their change history, and donor health checks (Module 2.3) | Once, on every database (safe to re-run) |
| 3 | `004_request_review_tracking.sql` | Adds who reviewed or closed a request, when, and the close reason (Module 2.4) | Once, on every database (safe to re-run) |
| 4 | `005_donor_status_history.sql` | Adds the donor deactivation/reactivation history (Module 2.5) | Once, on every database (safe to re-run) |
| 5 | `006_notification_type.sql` | Adds a type to notifications (Module 2.6) | Once, on every database (safe to re-run) |
| 6 | `007_query_indexes.sql` | Adds three indexes for dashboard trends and the disclosure log (Phase 4) | Optional, recommended (safe to re-run) |
| 7 | `seed_dummy_data.sql` | Fictional admin, requesters, donors and requests for testing and demos | Optional (safe to re-run) |
| 8 | `seed_demo_activity.sql` | Fictional matches, health checks, a donation, a deactivation history and notifications on top of step 7 | Optional, after step 7 (safe to re-run) |

There is no `002` script: Module 2.1 and Module 2.2 needed no schema changes.

```bash
mysql -u root -p < database/database_schema.sql                                  # fresh install only
mysql -u root -p hemolink_db < database/003_guidance_and_health_checks.sql
mysql -u root -p hemolink_db < database/004_request_review_tracking.sql
mysql -u root -p hemolink_db < database/005_donor_status_history.sql
mysql -u root -p hemolink_db < database/006_notification_type.sql
mysql -u root -p hemolink_db < database/007_query_indexes.sql                     # optional, recommended
mysql -u root -p hemolink_db < database/seed_dummy_data.sql                      # optional
mysql -u root -p hemolink_db < database/seed_demo_activity.sql                   # optional, after the seed above
```

The application uses `spring.jpa.hibernate.ddl-auto=validate`, so it **will not start** until every script it needs has been applied.

Seed login for every seeded account: password `password` (see the top of `seed_dummy_data.sql`).

## Columns kept but no longer used
They are kept so that existing data is not lost:
- `donor_profiles.eligibility_status`, `eligibility_reason`, `eligibility_reviewed_by`, `eligibility_reviewed_at`: eligibility is advisory (D-01).
- `donor_matches.admin_approval`, `approved_by`, `approval_at`: disclosure is controlled by the donor (D-16).

## Requirements and review

- MySQL **8.0.16 or later** (CHECK constraints are enforced from that version).
- Run MySQL and the application in the same time zone (see `docs/DATABASE_REVIEW.md`, DB-05).
- Full review, index map and the proposed (not applied) clean-up of unused columns: `docs/DATABASE_REVIEW.md`.
