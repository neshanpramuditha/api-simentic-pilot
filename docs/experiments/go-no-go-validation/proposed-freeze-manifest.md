# Proposed freeze manifest, revision 3 (2026-10-21): PROPOSAL ONLY, NOT A FREEZE RECORD

**Status: PROPOSED. Nothing is frozen.** This file lists the identifiers the documents would be frozen at.
- It becomes binding only through a completed entry in `freeze-record.md` (R18.1), after your explicit approval of this revision.
- **Any edit to a listed file after generation invalidates its row.**

**Generation:**
- Generated on 2026-10-21, after the last documentation edit of this session, by executing `git hash-object` and `Get-FileHash -Algorithm SHA256` on each file.
- HEAD at generation: `33ed52ae7ff98d9c34995acc7170d244f73a023d`. Most listed files are **uncommitted** (blocker F-2).

**Not listed (R18.1):**
- this manifest itself;
- `freeze-record.md`, which is completed at freeze and so changes then.

## Identifier meaning (R17.2)

| Identifier | Meaning |
|---|---|
| **Git blob id** | **Authoritative** identifier of the version-controlled content. It is authoritative only once committed (F-2) |
| **Working-tree SHA-256** | Integrity check of the exact bytes in the current execution environment |

- **Fresh-checkout line-ending behaviour has not been tested.** `core.autocrlf=true` is set, no `.gitattributes` rule covers these files, and the working-tree files are LF-only.
- The **45/45** verification of `baseline-hashes.sha256` applies **only to the present working copy**.
- `.gitattributes`, the Git configuration and the frozen baseline were **not** changed.

## Files

| File | Role | Git blob id (authoritative) | Working-tree SHA-256 (integrity) | Git state | Changed since proposal 2? |
|---|---|---|---|---|---|
| `experiment-1-rule-reuse-protocol.md` | Protocol (freeze) | `e9c718a74410c2b116bfcf15595a1bd7b32cc7ee` | `e85fc58de255f92e577ab82077b734fdee3836ae500443ef62ac6f96a73f56d2` | tracked, modified (uncommitted) | **yes** |
| `experiment-2-evidence-completeness-protocol.md` | Protocol (freeze) | `57014136e89a3c061537ea28c19300b19a96f850` | `da42621a0d31e836982924fc4c95f85efa50187127ba66354d339c567040c0e3` | tracked, modified (uncommitted) | **yes** |
| `operational-definitions.md` | Binding definitions and decision rules (freeze) | `18283892b5ec5e06382df8ac96980d4e3d2146c9` | `85b370310f0441d4e89591a88456a33f15daa4494e77668cd4b9431825513c63` | untracked (uncommitted) | **yes** |
| `independent-api-selection-criteria.md` | Protocol supplement (freeze) | `c059ac8179ff71b8dd0c31462bd7049cb4da3574` | `3c7ec45e7b6dd3ee74890181b911ae4d98749dec8503b0d764517289bb34fa0a` | untracked (uncommitted) | no |
| `postman-baseline-checklist.md` | Baseline specification (freeze) | `40e47200ebd12b10ff642dc0b28cb45d802288cb` | `d24c9f0b1cf7cde512d4f871f80d94de2fda334e21825d0ca84bb41c4dc330e2` | untracked (uncommitted) | no |
| `schemathesis-baseline-audit.md` | Baseline specification (freeze) | `0e9c766fd0661c35be3f7e5e241be66cf0aaf04d` | `66ae8616af92046cfbf58cea9556e00358fcbe4147499d3b7721ba9a842d4277` | untracked (uncommitted) | no |
| `experiment-data-template.csv` | Template (freeze) | `34dd126a166eb4d4015a9abbf5589094f44de09e` | `7fc9c3681c19944e731f911f004f6d425a9436277a60f94b48ee99284479745b` | tracked, modified (uncommitted) | no |
| `evidence-matrix-template.csv` | Template (freeze) | `40431d23b05c130a85f5abaf0d149bfd5c600172` | `4e073f7eeca1d0d22f260707234a8109ee805255091bd73d1b73e17bbdb9c74f` | tracked, modified (uncommitted) | no |
| `go-no-go-feasibility-assessment.md` | Gate restatement and context (freeze as reference) | `1815a3440cb2f28bdaff51146a9c958950a22d85` | `8a7161380f9d31b1aae7e2d411b3eef6fac2d9980a7e0e1190ff53f3a6acdec2` | tracked, modified (uncommitted) | **yes** |
| `prototype-readiness-audit.md` | Audit, reference only (record) | `aac21cca70bdde6454a517deaf32dba448e5a9af` | `7cb759290d80a86668bfa252593946fd90e0d91f5b5caaf51ad606aa79d9dd69` | tracked, modified (uncommitted) | no |
| `README.md` | Index and data dictionary (record) | `1fdabc890ad3bf34100f7d866b0eff04261d6234` | `298ab40084e088e721814ea8300241a3f8ada5923abb86b929be60f488215e83` | tracked, modified (uncommitted) | **yes** |
| `baseline-hashes.sha256` | Frozen baseline manifest, already frozen (record; never edit) | `138aee5b2cf1e6ac7944c85f16e57bca716c6454` | `9b1e7435764a4431641d871e5f2a7447d8eff6b23e9b34a6adcf6c80163f0645` | tracked, unchanged | no |

## Blockers to freezing

| ID | Blocker | Status |
|---|---|---|
| F-1 | Freeze-record mechanics | **Resolved (proposed)**: `freeze-record.md`, R18.1 |
| F-2 | Listed files uncommitted | **Open**: your action (no commit is made by this session) |
| F-3 | Native-margin bound | **Resolved (proposed)**: R18.2 |
| F-4 | Explicit approval of this manifest revision | **Open** |

## Superseded history (kept; not valid for any changed file)

### Proposal 2 (2026-10-21)

| File | Blob id | Working-tree SHA-256 |
|---|---|---|
| `experiment-1-rule-reuse-protocol.md` | `8a5a079257c9216e146edf961cd4cedd1164ba25` | `031fb50889f9616a9607a938cebe49b3342fcecf643c9f5bf06c1cd1cf69074f` |
| `experiment-2-evidence-completeness-protocol.md` | `8e6a9b25c4afcf2c8b9f6a430ed96e189c5ac34a` | `98c48d87e819c00af523660157309cf82901e047fb4d446ef0776cd66c0a3b16` |
| `operational-definitions.md` | `5dbed2e9128fa09c523992d51550ea3a3f7e3f32` | `e8c51e96f3767a55e8eb2589d49feff6ebde0d55ba0f680466f4d753bba6ad47` |
| `independent-api-selection-criteria.md` | `c059ac8179ff71b8dd0c31462bd7049cb4da3574` | `3c7ec45e7b6dd3ee74890181b911ae4d98749dec8503b0d764517289bb34fa0a` |
| `postman-baseline-checklist.md` | `40e47200ebd12b10ff642dc0b28cb45d802288cb` | `d24c9f0b1cf7cde512d4f871f80d94de2fda334e21825d0ca84bb41c4dc330e2` |
| `schemathesis-baseline-audit.md` | `0e9c766fd0661c35be3f7e5e241be66cf0aaf04d` | `66ae8616af92046cfbf58cea9556e00358fcbe4147499d3b7721ba9a842d4277` |
| `experiment-data-template.csv` | `34dd126a166eb4d4015a9abbf5589094f44de09e` | `7fc9c3681c19944e731f911f004f6d425a9436277a60f94b48ee99284479745b` |
| `evidence-matrix-template.csv` | `40431d23b05c130a85f5abaf0d149bfd5c600172` | `4e073f7eeca1d0d22f260707234a8109ee805255091bd73d1b73e17bbdb9c74f` |
| `go-no-go-feasibility-assessment.md` | `c108aff44c4288c795d90b50d3363fd3c1621989` | `22e65e37e02df9025c8da7c7959b9608d6d560322ae857326bec30b75bd8a2e4` |
| `prototype-readiness-audit.md` | `aac21cca70bdde6454a517deaf32dba448e5a9af` | `7cb759290d80a86668bfa252593946fd90e0d91f5b5caaf51ad606aa79d9dd69` |
| `README.md` | `95b6f6b12f7001fa60f99546e5d3e8a5b353ff5e` | `c8a6bc5e63ac218492594d1a8f3acfbc3f44ded6de22062a27e83117733fdce2` |
| `baseline-hashes.sha256` | `138aee5b2cf1e6ac7944c85f16e57bca716c6454` | `9b1e7435764a4431641d871e5f2a7447d8eff6b23e9b34a6adcf6c80163f0645` |

### Proposal 1 (2026-10-21)

| File | Blob id |
|---|---|
| `experiment-1-rule-reuse-protocol.md` | `a38642ae37e84dced9b813892113b783b9902441` |
| `experiment-2-evidence-completeness-protocol.md` | `5dce9848b92578489f60efd2ad75fb877216e43a` |
| `operational-definitions.md` | `6d97f0650579167068546c17eca43128dacd27b6` |
| `independent-api-selection-criteria.md` | `c059ac8179ff71b8dd0c31462bd7049cb4da3574` |
| `postman-baseline-checklist.md` | `43f3a3e8b5e799e87de89a20d522e9511e80cd70` |
| `schemathesis-baseline-audit.md` | `317439e9fcf87e7c9c2d7d6eb459782dc96f4b08` |
| `experiment-data-template.csv` | `34dd126a166eb4d4015a9abbf5589094f44de09e` |
| `evidence-matrix-template.csv` | `40431d23b05c130a85f5abaf0d149bfd5c600172` |
| `go-no-go-feasibility-assessment.md` | `0c010119151734e6cfca136dfaf300d14f19e97c` |
| `prototype-readiness-audit.md` | `aac21cca70bdde6454a517deaf32dba448e5a9af` |
| `README.md` | `13e2c0a96c7e65223a8b0238811e5cd3f3b51557` |
| `baseline-hashes.sha256` | `138aee5b2cf1e6ac7944c85f16e57bca716c6454` |
