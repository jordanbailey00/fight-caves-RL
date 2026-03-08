# changelog.md

## 2026-03-08

- Tightened the root `FCspec.md` episode-start contract so the primary module spec now matches the already-documented headless reset contract:
  - fixed run toggle wording to unconditional `ON`
  - added explicit reset input defaults for `startWave`, `ammo`, `prayerPotions`, and `sharks`
  - added the missing `all other skills: 1` line
  - clarified that no neck item is part of the canonical loadout
- Added an explicit root-spec note that the current `create_release.yml` remains inherited from the upstream Void fork and is not the source of truth for headless artifact naming or packaging contracts.
- Updated the root README and end-to-end checklist to use the canonical headless artifact wording:
  - default task `:game:headlessDistZip`
  - fallback/validation task `:game:packageHeadless`

## 2026-03-07

- Adopted canonical Fight Caves module root filenames:
  - `FCspec.md`
  - `FCplan.md`
- Renamed the prior root files from `spec.md` and `plan.md`.
- Updated active functional references so the rename does not break:
  - repo-root detection in Kotlin sources/tests
  - headless manifest references
  - current README guidance
  - current end-to-end checklist docs
- Existing detailed historical execution notes remain in `docs/changelog.md`.
