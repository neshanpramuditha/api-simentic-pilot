# Freeze record

## Status: NOT FROZEN

**No freeze has taken place.** This file is the template defined by `operational-definitions.md` R18.1 (F-1). It contains **no freeze entries**. Every field marked `—` is empty on purpose.

When the freeze procedure (§3) is completed, this file becomes the **only authoritative record** of the following:
- which files are frozen;
- their identifiers;
- which amendments are adopted.

The protocol files are not edited at freeze. Their in-file status lines ("DRAFT, NOT FROZEN"), their empty freeze logs and their "NOT ADOPTED" labels become historical **only after** §2 below records a completed freeze.

## 1. Rules for this record

1. **No file records its own final hash.**
   - This file does not list itself.
   - The frozen files are not edited at freeze.
2. **Identifiers** (R17.2):

   | Identifier | Role |
   |---|---|
   | **Git blob id** | **Authoritative** for the version-controlled content |
   | **Working-tree SHA-256** | Integrity check of the exact bytes in the execution environment |

   Fresh-checkout line-ending behaviour is **untested**.
3. **Every value must come from an executed command.** The command and its output are recorded in §4. No value is entered by hand from memory.
4. **The freeze applies to one approved manifest version.** Any file whose blob id differs from that manifest **cannot** be frozen under it.
5. **Supporting artifacts** (the PM-S collection, the ST-S checks and adapter, the database probe) are frozen in **separate later entries** (§5), when built and before any run.

## 2. Freeze entry (to be completed only at freeze)

| Field | Value |
|---|---|
| Freeze ID | — |
| Freeze date and time (UTC) | — |
| Approved by | — |
| Approval statement (quote, or link to the record) | — |
| Approved manifest version | — (e.g. `proposed-freeze-manifest.md`, revision N) |
| Git blob id of the approved manifest file | — |
| Commit containing the frozen files (`git rev-parse HEAD` after commit) | — |
| Verification result (§4): all blob ids equal to the manifest | — |
| Verification result (§4): all working-tree SHA-256 values equal to the manifest | — |
| `baseline-hashes.sha256` 45/45 re-verified in this working copy | — |
| Freeze status | **NOT FROZEN** |

### 2.1 Frozen files (to be completed from the approved manifest at freeze)

| File | Role | Git blob id (authoritative) | Working-tree SHA-256 (integrity) | `git ls-tree HEAD` blob id matches? |
|---|---|---|---|---|
| — | — | — | — | — |

## 3. Freeze procedure

Each step needs the repository owner's explicit approval at the time.

1. Approve a specific `proposed-freeze-manifest.md` revision. Record the quote or link in §2.
2. Commit the documentation directory. The commit must contain only `docs/experiments/go-no-go-validation/`.
3. ~~Verify (§4) that, for every listed file:~~
   ~~- the `git ls-tree HEAD` blob id equals the manifest's blob id;~~
   ~~- the working-tree SHA-256 equals the manifest's value.~~

   *Amended by W-2 (2026-10-21, proposed, not adopted; `operational-definitions.md` R19). The struck-through text above is kept as history:*

   **3a. Blob id (authoritative).** For every listed file, the `git ls-tree HEAD` blob id **must equal** the manifest's blob id.
   - **Any blob-id mismatch stops the freeze**: record it in §4, and nothing is frozen.
   - No investigation can resolve a blob-id mismatch under this manifest revision; only a new, separately approved manifest revision can.

   **3b. Working-tree SHA-256 (integrity).** For every listed file, compare the SHA-256 of the current working-tree bytes with the manifest's value.
   - **Equal:** this check passed.
   - **Different:** the mismatch is **not** accepted silently and is **not** treated as harmless. It must be investigated with **executed commands**, recorded verbatim in §4. It counts as **resolved** only if all three of these are shown:
     1. the file's blob id passed 3a;
     2. `git diff --quiet HEAD -- <file>` exits with 0 (no content difference from HEAD after Git's normalisation);
     3. the SHA-256 of the current bytes with every CRLF replaced by LF equals the SHA-256 of `git cat-file blob <blob id>`. This shows that the difference is **solely line-ending conversion**.
   - A mismatch resolved in this way counts as a **passed** check for step 5, and is recorded in §2 as "SHA-256 mismatch, resolved: line endings only" together with the file names.
   - **Any other cause, or a mismatch that cannot be explained, counts as a failed check**: step 5 applies (record the failure and stop; nothing is frozen).
4. Re-verify the 45 entries in `baseline-hashes.sha256`.
5. Fill in §2, §2.1 and §6. Set the freeze status to FROZEN **only if every check passed**. If any check fails, record the failure and stop: **nothing is frozen**.
6. Commit this file.

## 4. Verification commands and outputs (to be completed at freeze)

```
git ls-tree HEAD -- docs/experiments/go-no-go-validation/
git hash-object docs/experiments/go-no-go-validation/<file>
Get-FileHash -Algorithm SHA256 docs/experiments/go-no-go-validation/<file>
```

| Command | Output (verbatim) | Run at (UTC) |
|---|---|---|
| — | — | — |

## 5. Later entries: supporting artifacts (each before any run)

| Artifact | Path | Git blob id | Working-tree SHA-256 | Frozen at (UTC) | Approved by |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

## 6. Amendment adoption status (to be confirmed at freeze)

"Proposed disposition" restates what the documents currently propose. **Nothing is adopted until the "Adopted at freeze?" column is completed in a completed freeze entry.**

### Experiment 1 (`experiment-1-rule-reuse-protocol.md`)

| Amendment | Proposed disposition | Adopted at freeze? |
|---|---|---|
| E1-A1 | Adopt | — |
| E1-A2 | Adopt (cap handling folded into E1-A8) | — |
| E1-A3 | Adopt | — |
| E1-A4 | Adopt | — |
| E1-A5 | Adopt | — |
| E1-A6 | Adopt | — |
| E1-A7 | Adopt | — |
| E1-A8 | Adopt | — |
| E1-A9 | Adopt | — |
| E1-A10 | Adopt | — |
| E1-A11 | Adopt | — |
| E1-A12 | Adopt the identifier rule; the freeze mechanics are superseded by E1-A13 | — |
| E1-A13 | Adopt | — |

### Experiment 2 (`experiment-2-evidence-completeness-protocol.md`)

| Amendment | Proposed disposition | Adopted at freeze? |
|---|---|---|
| E2-A1 | Superseded by E2-A11 (not adopted separately) | — |
| E2-A2 | **Withdrawn** | — |
| E2-A3 | Adopt the defect averaging only; the baseline definition is superseded by E2-A10 | — |
| E2-A4 | Adopt | — |
| E2-A5 | Adopt | — |
| E2-A6 | Adopt, as refined by E2-A12 | — |
| E2-A7 | Adopt | — |
| E2-A8 | Adopt | — |
| E2-A9 | Adopt | — |
| E2-A10 | Adopt | — |
| E2-A11 | Adopt | — |
| E2-A12 | Adopt, with the cap scope clarified by E2-A20 | — |
| E2-A13 | Adopt the provenance part only; the rule scope is replaced by E2-A17 | — |
| E2-A14 | Adopt; for the gate, the no-reviewer path is superseded by E2-A27 | — |
| E2-A15 | Adopt, with V7 added by E2-A27 | — |
| E2-A16 | Adopt | — |
| E2-A17 | Adopt | — |
| E2-A18 | Adopt | — |
| E2-A19 | Adopt | — |
| E2-A20 | Adopt | — |
| E2-A21 | Adopt | — |
| E2-A22 | Adopt | — |
| E2-A23 | Adopt | — |
| E2-A24 | Adopt; its open U-1 is resolved by E2-A27 | — |
| E2-A25 | Adopt | — |
| E2-A26 | Adopt | — |
| E2-A27 | Adopt | — |
| E2-A28 | Adopt | — |
| E2-A29 | Adopt | — |
| E2-A30 | Adopt the identifier rule; the freeze mechanics are superseded by E2-A31 | — |
| E2-A31 | Adopt | — |
| E2-A32 | Adopt | — |

### Operational definitions (`operational-definitions.md`)

| Part | Proposed disposition | Adopted at freeze? |
|---|---|---|
| §1–§9 | Adopt, except where marked superseded (§4 in part, §8, §9 A2–A5) | — |
| R0–R18 | Adopt, except where marked superseded (R5 rule scope, R15.2 interim U-1 paragraphs, the Δ̄_nat parts of R10 steps 2–3 as replaced by R18.2) | — |
| R19 (wording amendments W-1 to W-4) | Adopt (see the table below) | — |

### Dated wording amendments (2026-10-21; `operational-definitions.md` R19)

| Amendment | Affects | Proposed disposition | Adopted at freeze? |
|---|---|---|---|
| W-1 | `operational-definitions.md` R18.1: "byte-identical" becomes "content-identical by authoritative Git blob id" | Adopt | — |
| W-2 | This file, §3 step 3: 3a (a blob-id mismatch stops the freeze) and 3b (a SHA-256 mismatch is investigated, and counts as passed only if shown to be line endings only) | Adopt | — |
| W-3 | `operational-definitions.md` R17.2: the documentation is committed (`560d775`) and merged (`4f19c98`) | Adopt | — |
| W-4 | `operational-definitions.md` R16, row U-4: the observed CRLF state of the current working copy, distinguished from fresh-checkout behaviour, which is untested | Adopt | — |
