# Notes for agents working on this repository

This repository teaches coding agents current Java and Spring Boot. Its own contents are held to the same standard.

## Layout

- `skills/modern-java-guidance/guides/<category>/<id>.md`: the guides. Each one is both documentation and a rule.
- `skills/modern-java-guidance/scripts/LegacyScan.java`: the scanner. One file, Java 17 language level, no dependencies. Keep it that way.
- `skills/modern-java-guidance/SKILL.md`: what an agent reads first. `INDEX.md` beside it is generated.
- `hooks/`, `.claude-plugin/`: the Claude Code plugin.
- `examples/`: `legacy-shop` is deliberately legacy and must keep failing the scan. `modern-shop` and `modern-jdk` must stay clean and must keep building.
- `tests/run.sh`: the end-to-end tests.

## Before you finish any change

```sh
sh tests/run.sh
java skills/modern-java-guidance/scripts/LegacyScan.java index   # only if a guide was added, removed or retitled
```

## Rules for guides

- Every factual claim (a version, a deprecation, a removal) needs a primary source in `source:`. If you cannot find one, do not make the claim.
- State only what you have checked. "Deprecated in 6.5" and "removed in 7.0" are different claims and both need to be true.
- A detector that is sometimes wrong is worse than no detector. Prefer a narrow pattern, and use `when-file` to require context.
- The After example must be clean under every guide, not just its own. `selftest` enforces this.
- Do not use preview features in After examples.

See [CONTRIBUTING.md](CONTRIBUTING.md) for the guide format.
