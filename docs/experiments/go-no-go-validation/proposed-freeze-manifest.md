# Proposed freeze manifest, revision 5 (2026-10-21): PROPOSAL ONLY, NOT A FREEZE RECORD

**Status: PROPOSED. Nothing is frozen and nothing is approved.**
- It becomes binding only through a completed entry in `freeze-record.md` (R18.1), after your explicit approval of **this revision**.
- `freeze-record.md` reads **NOT FROZEN**. Its approval, adoption, freeze-date and verification fields are empty.
- **Any edit to a listed file after generation invalidates its row.**

**Generation:**
- Generated on 2026-10-21 on branch `master`, HEAD `4f19c98fc470befe493006e128e24d650dd8fc57`, by executing `git ls-tree HEAD`, `git hash-object` and `Get-FileHash -Algorithm SHA256` on each file's current bytes.

**Why revision 5.**
- The dated wording amendments W-1 to W-4 (`operational-definitions.md` R19) changed `operational-definitions.md`, which is listed here, and `freeze-record.md`, which is not listed.
- **Only `operational-definitions.md` changes blob id.** Its new blob id exists only in the working tree until committed (blocker F-2 is reopened for this one file).

**Not listed (R18.1):**
- this manifest itself;
- `freeze-record.md`.

## Identifier meaning (R17.2, as worded by W-1 and W-4)

| Identifier | Meaning |
|---|---|
| **Git blob id** | **Authoritative** identifier of the version-controlled content. Frozen files are **content-identical by blob id** (W-1) |
| **Working-tree SHA-256** | Integrity check of the **current working-tree bytes in this environment only**. It **can differ across environments and checkouts because of line endings**, even with an identical blob id |

At freeze, a blob-id mismatch stops the freeze. A SHA-256 mismatch must be investigated and documented, and counts as passed only if shown to be line endings only (W-2, `freeze-record.md` §3 step 3).

- **Observed** in this working copy (executed): 11 listed files are CRLF; `baseline-hashes.sha256` is LF.
- **Untested:** fresh-checkout behaviour in any other environment.
- The **45/45** baseline verification applies **only to the present working copy**.
- `.gitattributes`, the Git configuration and the frozen baseline were **not** changed.

## Commit history of the listed files

| Commit | Description |
|---|---|
| `33ed52a` | "Define baseline". Contains `baseline-hashes.sha256` |
| `560d775` | "docs: go/no-go validation protocols and proposed freeze manifest (not frozen)". Only `docs/experiments/go-no-go-validation/` |
| `4f19c98` | "Merge pull request #1 from neshanpramuditha/go-no-go-protocols". The current HEAD |
| (none yet) | W-1 to W-4 edits to `operational-definitions.md` and `freeze-record.md`, and this manifest: **uncommitted** |

## Files

| File | Role | Git blob id (authoritative) | Working-tree SHA-256 (current bytes, this environment) | Line endings | Git state | Blob changed since rev 4? | SHA-256 changed since rev 4? |
|---|---|---|---|---|---|---|---|
| `experiment-1-rule-reuse-protocol.md` | Protocol (freeze) | `e9c718a74410c2b116bfcf15595a1bd7b32cc7ee` | `04c3a4e2fb73af3033742e571d181bbfc39c6b9bc1fc7f0344958e69c3aedf81` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `experiment-2-evidence-completeness-protocol.md` | Protocol (freeze) | `57014136e89a3c061537ea28c19300b19a96f850` | `034955526ee97c16112e6ac87c4b9cc22bebf239df6db6d4f988c5b516ffadad` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `operational-definitions.md` | Binding definitions and decision rules (freeze) | `729d9a35e215b401b07cf5a05c1cb70fe1449554` | `fb11596d9926227e5ca7a8056d02eea94f708cbbcd4d640b9764a7886553216e` | CRLF | **modified in working tree (W-1, W-3, W-4); not yet committed**. HEAD blob `18283892b5ec5e06382df8ac96980d4e3d2146c9` | **yes** | **yes** |
| `independent-api-selection-criteria.md` | Protocol supplement (freeze) | `c059ac8179ff71b8dd0c31462bd7049cb4da3574` | `cc52ebbe3897c5a9be5560f0fdcde11da0f24637db50282be94a6d6a1f779fc6` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `postman-baseline-checklist.md` | Baseline specification (freeze) | `40e47200ebd12b10ff642dc0b28cb45d802288cb` | `6026c1b9e7b0e90740738755eb45de115164eafe06e00034b352bf6c443a3a28` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `schemathesis-baseline-audit.md` | Baseline specification (freeze) | `0e9c766fd0661c35be3f7e5e241be66cf0aaf04d` | `13f6c123674271d38b810ef11fc359677c7f8e2989b542480894507271ac4a17` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `experiment-data-template.csv` | Template (freeze) | `34dd126a166eb4d4015a9abbf5589094f44de09e` | `fcc62790ab59e686253bb01f53bf272112e45c2fe365959355dd10ab83bf6007` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `evidence-matrix-template.csv` | Template (freeze) | `40431d23b05c130a85f5abaf0d149bfd5c600172` | `441f6c73155c3f23d83f8fbd31970370b0feb41d8ca6ddb0ba8edc92895577d2` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `go-no-go-feasibility-assessment.md` | Gate restatement and context (freeze as reference) | `1815a3440cb2f28bdaff51146a9c958950a22d85` | `4f9e4ebfb9f1447fce5a259ba7af396955357af41f49743b34f72650b1f7cc07` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `prototype-readiness-audit.md` | Audit, reference only (record) | `aac21cca70bdde6454a517deaf32dba448e5a9af` | `2bd797fb04d6fa473aa1cebd5667a4155b5b188c27ccc6cbb9a93dfc11d14f60` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `README.md` | Index and data dictionary (record) | `1fdabc890ad3bf34100f7d866b0eff04261d6234` | `588c9b6f4902e043193ae3a95cb42048837bac51a85579a57fe1d30743cbd749` | CRLF | committed in `560d775`, merged by `4f19c98`; unchanged since | no | no |
| `baseline-hashes.sha256` | Frozen baseline manifest, already frozen (record; never edit) | `138aee5b2cf1e6ac7944c85f16e57bca716c6454` | `9b1e7435764a4431641d871e5f2a7447d8eff6b23e9b34a6adcf6c80163f0645` | LF | committed in `33ed52a`; not changed by `560d775`/`4f19c98` | no | no |

## Blockers to freezing

| ID | Blocker | Status |
|---|---|---|
| F-1 | Freeze-record mechanics | **Resolved (proposed)**: R18.1, with W-1 and W-2 |
| F-2 | Listed content committed | **Open for `operational-definitions.md` only.** Its revision-5 blob id is not yet in HEAD. The other 11 files are committed (`560d775`, `4f19c98`) |
| F-3 | Native-margin bound | **Resolved (proposed)**: R18.2 |
| F-4 | Explicit approval of **this** manifest revision | **Open** |

## Superseded history (kept)

### Revision 4 (2026-10-21, uncommitted working-tree proposal)

| File | Blob id | Working-tree SHA-256 | Line endings |
|---|---|---|---|
| `experiment-1-rule-reuse-protocol.md` | `e9c718a74410c2b116bfcf15595a1bd7b32cc7ee` | `04c3a4e2fb73af3033742e571d181bbfc39c6b9bc1fc7f0344958e69c3aedf81` | CRLF |
| `experiment-2-evidence-completeness-protocol.md` | `57014136e89a3c061537ea28c19300b19a96f850` | `034955526ee97c16112e6ac87c4b9cc22bebf239df6db6d4f988c5b516ffadad` | CRLF |
| `operational-definitions.md` | `18283892b5ec5e06382df8ac96980d4e3d2146c9` | `948c4a17e369deb37f32f87a2904debc89c1feac25f033a6894f409f4578bb17` | CRLF |
| `independent-api-selection-criteria.md` | `c059ac8179ff71b8dd0c31462bd7049cb4da3574` | `cc52ebbe3897c5a9be5560f0fdcde11da0f24637db50282be94a6d6a1f779fc6` | CRLF |
| `postman-baseline-checklist.md` | `40e47200ebd12b10ff642dc0b28cb45d802288cb` | `6026c1b9e7b0e90740738755eb45de115164eafe06e00034b352bf6c443a3a28` | CRLF |
| `schemathesis-baseline-audit.md` | `0e9c766fd0661c35be3f7e5e241be66cf0aaf04d` | `13f6c123674271d38b810ef11fc359677c7f8e2989b542480894507271ac4a17` | CRLF |
| `experiment-data-template.csv` | `34dd126a166eb4d4015a9abbf5589094f44de09e` | `fcc62790ab59e686253bb01f53bf272112e45c2fe365959355dd10ab83bf6007` | CRLF |
| `evidence-matrix-template.csv` | `40431d23b05c130a85f5abaf0d149bfd5c600172` | `441f6c73155c3f23d83f8fbd31970370b0feb41d8ca6ddb0ba8edc92895577d2` | CRLF |
| `go-no-go-feasibility-assessment.md` | `1815a3440cb2f28bdaff51146a9c958950a22d85` | `4f9e4ebfb9f1447fce5a259ba7af396955357af41f49743b34f72650b1f7cc07` | CRLF |
| `prototype-readiness-audit.md` | `aac21cca70bdde6454a517deaf32dba448e5a9af` | `2bd797fb04d6fa473aa1cebd5667a4155b5b188c27ccc6cbb9a93dfc11d14f60` | CRLF |
| `README.md` | `1fdabc890ad3bf34100f7d866b0eff04261d6234` | `588c9b6f4902e043193ae3a95cb42048837bac51a85579a57fe1d30743cbd749` | CRLF |
| `baseline-hashes.sha256` | `138aee5b2cf1e6ac7944c85f16e57bca716c6454` | `9b1e7435764a4431641d871e5f2a7447d8eff6b23e9b34a6adcf6c80163f0645` | LF |

### Revision 3 (2026-10-21, committed in `560d775`)

The blob ids are unchanged in revision 4. The SHA-256 values are the earlier LF bytes.

| File | Blob id | Working-tree SHA-256 (LF bytes at revision 3) |
|---|---|---|
| `experiment-1-rule-reuse-protocol.md` | `e9c718a74410c2b116bfcf15595a1bd7b32cc7ee` | `e85fc58de255f92e577ab82077b734fdee3836ae500443ef62ac6f96a73f56d2` |
| `experiment-2-evidence-completeness-protocol.md` | `57014136e89a3c061537ea28c19300b19a96f850` | `da42621a0d31e836982924fc4c95f85efa50187127ba66354d339c567040c0e3` |
| `operational-definitions.md` | `18283892b5ec5e06382df8ac96980d4e3d2146c9` | `85b370310f0441d4e89591a88456a33f15daa4494e77668cd4b9431825513c63` |
| `independent-api-selection-criteria.md` | `c059ac8179ff71b8dd0c31462bd7049cb4da3574` | `3c7ec45e7b6dd3ee74890181b911ae4d98749dec8503b0d764517289bb34fa0a` |
| `postman-baseline-checklist.md` | `40e47200ebd12b10ff642dc0b28cb45d802288cb` | `d24c9f0b1cf7cde512d4f871f80d94de2fda334e21825d0ca84bb41c4dc330e2` |
| `schemathesis-baseline-audit.md` | `0e9c766fd0661c35be3f7e5e241be66cf0aaf04d` | `66ae8616af92046cfbf58cea9556e00358fcbe4147499d3b7721ba9a842d4277` |
| `experiment-data-template.csv` | `34dd126a166eb4d4015a9abbf5589094f44de09e` | `7fc9c3681c19944e731f911f004f6d425a9436277a60f94b48ee99284479745b` |
| `evidence-matrix-template.csv` | `40431d23b05c130a85f5abaf0d149bfd5c600172` | `4e073f7eeca1d0d22f260707234a8109ee805255091bd73d1b73e17bbdb9c74f` |
| `go-no-go-feasibility-assessment.md` | `1815a3440cb2f28bdaff51146a9c958950a22d85` | `8a7161380f9d31b1aae7e2d411b3eef6fac2d9980a7e0e1190ff53f3a6acdec2` |
| `prototype-readiness-audit.md` | `aac21cca70bdde6454a517deaf32dba448e5a9af` | `7cb759290d80a86668bfa252593946fd90e0d91f5b5caaf51ad606aa79d9dd69` |
| `README.md` | `1fdabc890ad3bf34100f7d866b0eff04261d6234` | `298ab40084e088e721814ea8300241a3f8ada5923abb86b929be60f488215e83` |
| `baseline-hashes.sha256` | `138aee5b2cf1e6ac7944c85f16e57bca716c6454` | `9b1e7435764a4431641d871e5f2a7447d8eff6b23e9b34a6adcf6c80163f0645` |

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
