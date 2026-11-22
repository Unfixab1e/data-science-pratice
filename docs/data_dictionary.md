# Data dictionary and cleaning contract

**Project:** Aktenblick. **Dataset version:** 1.0. **Fixed snapshot:** 2026-11-20.

These are rules for the fictional exercise, not statements of German law or actual IBM system requirements. Start with the columns needed by your current task; you do not have to implement every validation rule on the first day.

## 1. File format

Every CSV is UTF-8, has one header record, and uses `;` as its delimiter. A quoted field may contain `;`, `"` or a newline. Embedded quotes are doubled. An empty field is missing; there is no `NULL`, `NA` or zero sentinel. Read CSV records with the supplied reader. The number of physical lines is not the number of records.

Trim whitespace at the beginning and end of fields for comparison, while keeping the original value for an audit log. Preserve internal spaces, punctuation and newlines in titles and text. Preserve umlauts. Never strip all non-ASCII characters.

IDs are strings, not numbers. Keep their leading zeroes. All learner output CSVs should use the same encoding and delimiter, with proper quoting. Use a dot in normalized decimal numbers and ISO `uuuu-MM-dd` dates.

## 2. Document metadata

One row of `data/raw/documents.csv` is an **ingestion record**. Different ingestion IDs can refer to the same business document. The full export has 128 ingestion records; the sample contains its first 12 records.

| Field | Meaning and classroom rule |
| --- | --- |
| `ingest_id` | Unique import-row identifier, such as `ROW-0001`. Required. Keep it in logs and duplicate decisions. |
| `document_id` | Business document identifier, such as `DOC-0001`. Required. Trim before grouping or joining; duplicate IDs need investigation. |
| `matter_id` | Foreign key to `matters.csv`. Required. A nonblank ID absent from that file is an unresolved reference. |
| `title` | Human-readable title. Required. Missing titles remain missing and go to the review queue. |
| `document_type` | One of six agreed document categories listed below. |
| `department` | Responsible organizational area. One of four values listed below. |
| `received_date` | Calendar date when the document arrived. Required; invalid, missing or future receipt dates are flagged. |
| `document_date` | Date printed on the document. Required. Its ordering relative to receipt is checked separately. |
| `status` | Internal processing state. It is not a legal conclusion or permission to execute payment. |
| `page_count` | Positive whole number of pages. Zero, negative and nonnumeric values are invalid. |
| `ocr_confidence` | Optional quality score from 0 to 1, inclusive. Required for `Scan`; blank is allowed for `E-Mail` or `Upload`. |
| `source` | Ingestion channel: `E-Mail`, `Scan` or `Upload`. |
| `reviewed_date` | Date of completed internal review. Required for `Freigegeben`; blank is allowed in the other states. |
| `reviewer_id` | Assigned reviewer. Blank is allowed for `Neu`; otherwise require an existing active reviewer. |
| `confidentiality` | Internal classification: `Intern`, `Vertraulich` or `Streng vertraulich`. Required; never default a blank to the least restrictive value. |
| `language` | Language code. This small training export supports only `de`. Other values are flagged. |
| `review_due_date` | Fictional internal target date for review. Required. It is not a statutory deadline and may fall after the snapshot date. |

### Controlled vocabulary and aliases

Recognize canonical values without regard to letter case, after trimming outside spaces. Use the following explicitly allowed aliases as well. Keep the agreed capitalization in normalized output. Do not use fuzzy matching to guess an unknown category.

| Field | Canonical values | Additional allowed aliases |
| --- | --- | --- |
| `document_type` | `Vertrag`, `Nachtrag`, `Kündigung`, `Rechnung`, `Datenschutzanfrage`, `Schriftverkehr` | `Kuendigung` to `Kündigung`; `Datenschutz-Anfrage` to `Datenschutzanfrage` |
| `department` | `Einkauf`, `Personal`, `Vertrieb`, `Datenschutz` | None beyond case and outside spaces |
| `status` | `Neu`, `In Prüfung`, `Freigegeben`, `Rückfrage` | None beyond case and outside spaces |
| `source` | `E-Mail`, `Scan`, `Upload` | `email` to `E-Mail` |
| `confidentiality` | `Intern`, `Vertraulich`, `Streng vertraulich` | None beyond case and outside spaces |

`Recht` is not an agreed department in this export. An inactive reviewer is an existing reference requiring reassignment, not an unknown reference. Reviewers may help another department; a cross-department assignment is not automatically an error in this exercise.

### Dates, numbers and missing values

Accept strict ISO `uuuu-MM-dd` and German `dd.MM.uuuu` dates, after trimming. In Java, use `DateTimeFormatter` with `ResolverStyle.STRICT`. `31.02.2026` and `2026-04-31` must fail instead of becoming another day. A missing or invalid date must not become today's date.

OCR examples: `0.82`, `0,82` and `82%` all mean 0.82. A percentage is divided by 100. Check the resulting number is finite and inside 0..1. An empty value is not 0. For optional low-quality reporting, use `< 0.80` as a declared classroom threshold; it is not a calibrated OCR guarantee.

Use `Integer.parseInt` for positive page counts after trimming. Do not silently translate words into numbers or round decimal page counts. Preserve an invalid raw value in the review queue. A normalized typed field can be blank with a matching quality issue.

In the candidate export, a value that cannot be parsed or fails its numeric range check becomes blank, with the original value retained in the review queue. A parseable calendar date that fails a chronology or snapshot rule stays visible as its normalized ISO date. Flag the relationship and exclude it from calculations that require that relationship to be valid. Do not erase a validly parsed date merely because its relationship is unresolved.

### Chronology and workflow checks

Apply comparisons only when both dates parsed successfully. Report each problem with its ingestion ID and the source values. Do not invent an ordering when a date is absent or invalid.

- `document_date` after `received_date` requires review.
- `reviewed_date` before `received_date` requires review.
- Receipt, document and completed-review dates after 2026-11-20 require review because this is a snapshot export.
- `review_due_date` before `received_date` requires review. A target after the snapshot is allowed.
- `Freigegeben` with no review date requires review.
- An unassigned, unknown or inactive reviewer on a non-`Neu` record requires review.

For a later backlog report, overdue means: a nonconflicting document has a canonical open status (`Neu`, `In Prüfung` or `Rückfrage`), a valid receipt date on or before 2026-11-20, and a valid internal target date that is on or after receipt but strictly earlier than 2026-11-20. Exclude documents with unusable receipt/target dates or conflicting duplicates from that calculation. Other unrelated quality flags may remain. Targets on 2026-11-20 are **due today**, not overdue. Report exclusions and the denominator explicitly.

## 3. Duplicate policy

Group on trimmed `document_id`. Compare business fields after the documented harmless normalization; exclude `ingest_id` from that comparison. Keep invalid raw tokens when comparing instead of collapsing every invalid value to the same null. Otherwise two different errors could be incorrectly treated as equal.

For a group whose business fields agree, retain the smallest `ingest_id` and record the discarded IDs and reason. For a group with conflicting business values, withhold **all** rows of that group from the one-row-per-document analytical output and list them in the conflict queue. The later row is not automatically more trustworthy.

After this policy, the remaining rows are **candidates for analysis**, not a claim that every field is valid. Keep other unresolved data-quality flags and exclude unusable fields from the relevant calculations. Do not delete a whole document merely because one optional analytical field is missing. More than one issue can apply to the same row.

## 4. Reference and text tables

| File and field | Meaning |
| --- | --- |
| `matters.csv: matter_id` | Unique matter ID, such as `MAT-001`; join to trimmed document `matter_id`. |
| `matter_name` | Fictional descriptive matter name. |
| `department` | Matter's coordinating department; uses the same four canonical values. |
| `opened_date` | ISO date of the fictional matter's opening. |
| `matter_status` | `Offen` or `Abgeschlossen`; use for context. No automated document-status rule is inferred from it. |
| `reviewers.csv: reviewer_id` | Unique reviewer ID, such as `REV-001`; blank assignment is distinct from a failed join. |
| `display_name` | Invented name. No real person is represented. |
| `department` | Reviewer's home department; helping elsewhere is permitted. |
| `active` | Boolean text `true` or `false`; inactive IDs remain valid references. |
| `document_texts.csv: document_id` | Unique business ID for one of the 120 logical documents. |
| `text` | German synthetic excerpt, including intentional paragraph breaks; may contain semicolons and quotes. |

Text and metadata are separate exports. An excerpt's date can provide evidence for a **human-reviewed** correction, but reading a different value is not permission for the first cleanup script to silently overwrite metadata. Optional PDF samples show the first six excerpts and add no new source facts.

## 5. Evidence and output

Keep `data/raw/` unchanged. Record transformations and unresolved cases separately. A review-queue row contains `ingest_id`, `document_id`, `field`, `raw_value`, `issue_code` and `message`; put a short explanation or suggested next step in `message`. A duplicate queue may also list the whole group. Distinguish a harmless formatting transformation from an unresolved defect in your report.

Use `outputs/` for normalized CSVs, charts and findings. Keep the row count before and after every filtering or deduplication step. Every percentage needs a named denominator and missing-value policy. Aggregate review duration only where both receipt and review dates are valid and ordered; state how many documents were excluded.

The maintainer factory is `tools/GenerateDataset.java`; the provenance manifest is `data/manifest.json`. Selected answer checks and the deliberate injection ledger are in `mentor/data/`. They describe this fixed synthetic fixture and do not establish real-world quality rates.
