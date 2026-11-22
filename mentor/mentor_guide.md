# Mentor guide - spoilers

## Read this after the learner has tried the ticket

This directory contains generation truth and checkpoints. It is intentionally separate from the learner's first steps. Do not have the learner copy expected numbers into the program. The goal is to reproduce a result from the input and explain the reasoning.

This is an independent fictional practice project. It does not specify any employer's tools, production architecture, or actual onboarding expectations.

## What the learner should gain

By the end of the core work, a beginner should be able to run a small Java program, inspect a CSV record, use a loop and a map, write small methods, handle invalid data explicitly, and explain a basic analysis. A working production application, advanced statistics, a model integration, and complete independence are not expected.

The core tickets are approximately 20-28 learning hours. Use short sessions. If a ticket becomes frustrating, reduce its scope to one field or one row. Completing a smaller analysis with understanding is better than completing every output through copied code.

## How to help without taking over

Ask the learner to predict one result before running the code. If the result is wrong, inspect one small example together. Give a conceptual explanation, a single hint, or a tiny unrelated example before offering a correction.

Useful questions include:

- Which input record should make this condition true?
- What type does this value have right now: text, number, or date?
- Is this a row count, a document count, or an issue count?
- What should happen if the cell is blank?
- Can you explain why this duplicate decision is safe?
- Which records are excluded from this percentage?

Avoid asking the learner to understand the supplied CSV parser on day one. It is scaffolding. Review the learner's own methods and the business decisions first.

## First-session checkpoint

T00 and T01 should take about 45-60 minutes if Java is already available. Setup may take longer. A successful first session is simply: the warm-up runs, the twelve-row sample loads, the learner explains a few fields, and records a question about an ambiguous value.

Do not immediately add Maven, Spring, databases, a cloud model, or a frontend. They would introduce several independent learning problems before the first data task is understood.

## Ground truth and useful invariants

The dataset generator creates a deliberately fixed cohort. `data/manifest.json` records provenance and hashes. `mentor/data/expected_metrics.csv` contains selected answer checks, `mentor/data/anomalies.csv` records deliberate injections, and `mentor/data/duplicate_groups.csv` records duplicate truth. These files permit targeted checks of a learner's implementation.

The metadata invariants are:

| Quantity | Expected value |
| --- | --- |
| Main ingestion records | 128 |
| Sample ingestion records | 12 |
| Distinct trimmed document IDs | 120 |
| Matters | 12 |
| Reviewers | 6 |
| Text records | 120 |
| Extra agreeing imports removed | 5 |
| Conflicting document IDs | 3 |
| Source rows in conflicting groups | 6 |
| Candidate documents after duplicate policy | 117 |

Row reconciliation: **128 = 117 + 5 + 6**. Document reconciliation: **120 = 117 + 3**.

Agreeing duplicate IDs are `DOC-0051`, `DOC-0062`, `DOC-0073`, `DOC-0084`, and `DOC-0095`. Conflicting IDs are `DOC-0106`, `DOC-0107`, and `DOC-0108`.

The candidate count does not mean that 117 records are fully valid. Some candidates retain blank typed cells or unresolved references with issue records. That distinction is an important review point.

Do not turn the anomaly injection count into the only required issue count. Some rules can produce more than one issue from one cell or one record. A consistent learner rule may flag an additional consequence. Review the explanation before treating a count mismatch as a coding error.

## Review by milestone

### After T03: does the profile describe the input?

Check raw record counts, a missing-title example, and category totals. Every frequency table should sum to the number of loaded records when missing values are included. Make sure percentages use a decimal calculation rather than accidental integer division.

Ask why `Freigegeben` and its raw lowercase form `freigegeben` initially appear separately. An initial profile should not silently hide dirty inputs.

### After T05: are corrections defensible?

Check one alias, one German-format date, one impossible date, one missing value, one zero/negative page count, and one percentage OCR value. The dictionary defines what is allowed. A parser that adjusts an impossible date has not passed.

There is no `N/A` sentinel in this dataset. Missing means blank after trimming. Zero is a value and fails the page-count rule; it is not equivalent to missing.

For each unparseable or out-of-range typed value, the candidate representation is blank, but the issue queue must retain the source value. Keep parseable calendar dates that violate chronology visible, with relationship flags; exclude them from the relevant calculations. Check that the learner can distinguish no information from information that failed validation.

### After T08: is the cleanup traceable?

Check both reconciliations above, raw-file preservation, deterministic agreeing-duplicate choices, and withholding of all conflicting source rows. A source row may have several issue records. Issues must not be subtracted from document counts.

Review one orphan matter and one unknown reviewer. The learner should not lose the document in an inner join or invent a substitute reviewer. A cross-department reviewer is not automatically an error unless the declared fictional rules say so.

### After T10: do the claims match the calculation?

Use the fixed snapshot, 2026-11-20. Open statuses are `Neu`, `In Prüfung`, and `Rückfrage`. For past-target reporting, first require a valid receipt date on or before the snapshot and a valid target on or after receipt. Past-target means strictly earlier, not equal to, the snapshot. A future receipt or target-before-receipt relationship is excluded; unrelated flags do not automatically remove the record. Scores below `0.80` are low only among valid scan scores for the stated KPI.

Ask the learner to explain exclusions. A denominator consisting only of usable scan scores should not be labeled "all documents." Missing completion dates are not zero-day reviews. A one-time snapshot does not establish a trend or staff productivity.

### After T11: can another person understand the result?

Ask for a five-minute demonstration using one good record, one invalid cell, and one duplicate conflict. Re-run the documented command. The report should name its populations, unresolved questions, and reproducible outputs without implying that the synthetic statistics represent a real company.

## A compact assessment rubric

Use the rubric for feedback rather than a high-stakes grade. Score each area 0, 1, or 2: 0 = not yet demonstrated, 1 = works with help or partial explanation, 2 = works and can be explained.

| Area | Evidence to look for |
| --- | --- |
| Java understanding | Explains a loop, a map lookup, and a small method |
| Data interpretation | Distinguishes ingestion rows, logical documents, matters, and issues |
| Validation | Preserves raw values and separates missing from invalid |
| Duplicate handling | Keeps agreeing rows deterministically and withholds conflicts |
| Reporting | Defines denominators and limitations accurately |
| Reproducibility | Runs the workflow and locates its outputs |
| Communication | Asks a precise question and explains an unresolved case |

Look at the pattern of strengths and gaps rather than the total. A learner who still needs help with syntax but correctly identifies a data ambiguity is making useful progress.

## Optional search and LLM lab

The lab uses eight hand-written mock answers, not a live model: four development and four evaluation examples. Its aim is to distinguish supported factual extraction from fluent invention and to check whether citations actually support a claim. A low score is a property of the intentionally mixed mock examples, not evidence about any vendor. `mentor/llm_review_key.csv` supplies review labels and reasons; consult `data/llm/README.md` for the exact field definitions before comparing labels.

Keep development and evaluation matters separate. Because the corpus is synthetic and reuses templates, even a clean matter split is not a strong test of real-world generalization. The reference answers are visible to support learning; this is not a secure benchmark.

The misleading instruction-like text is an exercise in treating quoted source content as data. Have the learner explain why following that text would contradict the task. Do not present a draft prompt as a complete defense against every failure.

If the learner is still spending substantial effort on loops or methods, postpone this lab. The core analysis alone is a complete project.

## If the learner wants to extend the project

Choose one small next step after the core demonstration: add a useful filter, compare two declared cohorts, improve a search result excerpt, or add one carefully chosen validation check. Let the actual work assignment determine whether a database, Java framework, or model integration becomes relevant. No such technology is required to make this practice project worthwhile.
