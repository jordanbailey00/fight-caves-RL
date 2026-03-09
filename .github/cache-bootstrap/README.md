# Hosted Cache Bootstrap

This branch exists only to host a temporary cache bootstrap artifact for hosted
native-Linux Phase 0 benchmark runs.

- Source: validated local `data/cache/` from the main workspace clone
- Consumer: `fight-caves-RL` hosted Phase 0 workflow
- Scope: benchmark/bootstrap infrastructure only
- Not for simulator/runtime source-of-truth content

The workflow should verify part checksums from `manifest.json`, reconstruct the
zip archive, verify the full archive checksum, and then extract into
`data/cache/`.
