# Aktenblick: junior assignment book

## How to use the tickets

Complete T00-T11 in order. T00 and T01 form the first 45-60 minute session. Most later tickets take several 30-90 minute blocks. The overall core is roughly 20-28 learning hours; the estimates are flexible and include reading, coding, and debugging.

Start with the twelve-row sample. Move to the full export only after you can explain what the sample program does. An imperfect but understood method is more useful than a large program you cannot explain.

The supplied files provide CSV loading, output writing, and a small chart helper. Your work goes in `runExercises(List<Map<String, String>> rows)` in `starter/Practice.java`, or small methods that you call from it. The supplied parser is infrastructure, not a reading assignment for your first day.

Run these commands from the repository root:

```text
java starter/StartHere.java
java starter/Practice.java
java starter/Practice.java data/raw/documents.csv
java starter/Practice.java --self-test
```

The last command checks the supplied CSV and output helpers. Passing it does **not** mean you have completed the assignments. See `docs/java_setup.md` for IntelliJ and command-line help.

For every ticket, record the command used, one result you checked manually, and one question or lesson in a copy of `templates/learning_journal.md`. Save your work in Git at useful stopping points. You may work on a personal branch when you are comfortable with branches; learning branches is not a prerequisite for the first session.

## T00 - Run a tiny Java program

**PM request:** "Bitte starte mit einem kleinen Beispiel und prüfe deine Entwicklungsumgebung."  
**Meaning:** Get a small program running before you touch the analysis.  
**Prerequisite:** None. **Time:** 20-30 minutes. **Difficulty:** 1/5.

**Input:** `starter/StartHere.java` and `docs/java_setup.md`.

**Work in a small block**

1. Open the repository folder, find the Java file, and run the first command above.
2. Read the few variables and the printed messages. Explain which values are text and which are numbers.
3. Make the small change requested in the file. Predict the new output before running it again.

**Deliverable:** Your changed warm-up file and three lines in the learning journal: command, prediction, observed result.

**Acceptance criteria**

- The Java program starts without an error.
- You can point to the line you changed and explain its effect.
- You know which folder is the repository root.

**Hint:** Run the program before changing anything. If it fails, copy the exact error into the journal; distinguish a setup problem from a coding problem.

**Checkpoint:** Stop here if setup used the entire session. That is still progress.

## T01 - Read the first twelve records

**PM request:** "Welche Informationen stehen pro Dokument im Export?"  
**Meaning:** Explain what one metadata record represents.  
**Prerequisite:** T00. **Time:** 25-30 minutes. **Difficulty:** 1/5.

**Input:** `data/samples/documents_12.csv`, `docs/data_dictionary.md`, and the supplied loader in `starter/Practice.java`.

**Work in a small block**

1. Run `java starter/Practice.java`. It loads the sample and shows a few titles.
2. Open the sample as text. Find `ingest_id`, `document_id`, `department`, and `status` in three records.
3. Read the definitions of `document_id`, `matter_id`, and `review_due_date`.
4. Write two questions you would ask the data owner. Do not repair cells yet.

**Deliverable:** A short note explaining three records, plus two entries in a copy of `templates/decision_log.csv`.

**Acceptance criteria**

- The loader reports twelve records; you have not counted the header as data.
- You explain the difference between a CSV record, a document, and a matter.
- You explain that the review target is an internal business date.
- You notice that a visually blank or unusual value needs investigation.

**Hint:** A quoted CSV value can contain a line break. Count records using the loader, not the number of lines in a text editor.

**Decision question:** Does an empty reviewer mean "not assigned," "unknown," or "not required"? The export alone does not answer that.

## T02 - Profile size and missing values

**PM request:** "Wie groß ist der Export, und wo fehlen Angaben?"  
**Meaning:** Count the export and identify missing fields.  
**Prerequisite:** T01. **Time:** 1.5-2 hours. **Difficulty:** 2/5.

**Input fields:** `ingest_id`, `document_id`, `title`, `department`, `received_date`, `reviewer_id`.

**Before you begin:** If a loop, `List`, or `Map` is unfamiliar, use `docs/java_basics.md` and run `java starter/CollectionsWarmup.java`. Its tiny book-list examples introduce the syntax without solving this analysis task. Give yourself an extra session or two if needed.

**Block A:** In the exercise area, print the row count and count blank or whitespace-only titles. Use a loop so you can explain what happens for each record.

**Block B:** Repeat the missing-value check for the selected fields. Add a percentage using the row count of the file you actually loaded.

**Block C:** Run the same code on the full export and write `outputs/profile.csv` with columns `metric`, `value`, and `denominator`.

**Acceptance criteria**

- The same code works for the sample and full export without a hardcoded record count.
- Blank means empty after trimming; `0` is not counted as blank.
- Every percentage names its denominator and handles an empty input safely.
- The original CSV is unchanged.
- You manually check one missing-value count against the sample.

**Hint:** First make a boolean question such as "is this title blank?" work for one row. Then place it in a loop. For output, see the supplied `Csv.writeReport(...)` helper.

**Decision question:** A nonblank title can still be wrong. What does this profile prove, and what does it not prove?

## T03 - Count categories without hiding messy values

**PM request:** "Zeige uns die Verteilung nach Abteilung, Status und Dokumentart."  
**Meaning:** Make frequency tables.  
**Prerequisite:** T02. **Time:** 1.5-2 hours. **Difficulty:** 2/5.

**Input fields:** `department`, `status`, `document_type`, `source`.

**Block A:** Count raw department values using a `Map<String, Integer>`. Display missing values as `(missing)` without changing the raw input.

**Block B:** Repeat for status, type, and source. Sort the displayed categories in a stable order, so two runs are easy to compare.

**Deliverable:** `outputs/raw_frequencies.csv` with `field`, `value`, and `count`.

**Acceptance criteria**

- Counts for each field add up to the number of loaded records.
- Alias values remain separate at this stage; the table shows why cleanup is needed.
- Missing values appear as a category and are included in the total.
- German characters display correctly.

**Hint:** You need only a loop, a map, and an increment. You do not need streams or a data-analysis framework.

**Decision question:** Could two labels with different spelling represent the same category? Record candidates; do not merge them based only on appearance.

## T04 - Normalize strings with explicit rules

**PM request:** "Bitte vereinheitliche bekannte Schreibweisen und dokumentiere jede Regel."  
**Meaning:** Apply agreed mappings; keep unknown values visible.  
**Prerequisite:** T03. **Time:** 2-3 hours. **Difficulty:** 2/5.

**Input fields:** Categorical fields and identifiers, using the exact mapping tables in `docs/data_dictionary.md`.

**Block A:** Write one small normalization method. Trim the edges of a value, apply an explicitly documented alias, and leave a canonical value unchanged.

**Block B:** Extend this to the specified categorical fields. Build new records; do not replace the values in the raw input file.

**Block C:** Compare raw and normalized frequency tables. Log unsupported values for review.

**Deliverable:** Normalization methods and `outputs/normalization_rules.csv` with `field`, `raw_value`, `normalized_value`, and `rule`.

**Acceptance criteria**

- Every replacement comes from an explicit rule in the dictionary.
- Applying the normalization method twice gives the same value as applying it once.
- IDs are trimmed but are not renumbered or invented.
- Unknown categories are not silently mapped to the most common category.
- Your methods do not change punctuation or whitespace inside document text or titles unless a rule explicitly permits it.

**Hint:** Keep a few before/after examples in your journal. An unfamiliar abbreviation is a question, not automatically a spelling error.

**Decision question:** If a value is unknown, should it remain in a separate category or be excluded from a particular KPI? State the chosen denominator later.

## T05 - Parse dates and numbers without inventing facts

**PM request:** "Welche Datums- und Zahlenwerte können wir zuverlässig verwenden?"  
**Meaning:** Separate valid, missing, and invalid values.  
**Prerequisite:** T04. **Time:** 2.5-4 hours. **Difficulty:** 3/5.

**Input fields:** The four date fields, `page_count`, and `ocr_confidence`. See the dictionary for the full validity rules.

**Block A:** Parse ISO dates and `dd.MM.uuuu` with strict validation. Convert valid dates to ISO `uuuu-MM-dd` in the candidate representation; `LocalDate.toString()` produces the intended format for these dates.

**Block B:** Parse positive whole-number page counts. For OCR, support the documented decimal comma, decimal point, and percent formats; store valid scores from 0 to 1.

**Block C:** Check chronology and consistency rules. Preserve the original offending value in a list of issues.

**Deliverable:** Small parsing methods and preliminary issue records with `ingest_id`, `document_id`, `field`, `raw_value`, `issue_code`, `message`.

**Acceptance criteria**

- Missing and invalid values are distinguishable in your issue log.
- An impossible date is rejected, not adjusted into the next month.
- `92,5%` becomes `0.925`; an unmarked `92.5` is outside the valid 0-1 range and is not guessed to be a percentage.
- Zero or negative page counts are flagged.
- Values that cannot be parsed, or fail a numeric range check, become blank in the candidate export, with their raw value retained in an issue record.
- A parseable date that violates chronology stays visible as an ISO date, with a relationship issue. Exclude it from calculations that require that relationship to be valid; do not invent a replacement date.
- A bad cell does not stop the entire file from being profiled.

**Hint:** Java strict date parsing uses `uuuu` for the year pattern in this exercise. Use `LocalDate` for day-level calculations; do not calculate durations by subtracting formatted strings.

**Decision question:** Is a missing OCR score an error for every source type? Use the dictionary's source-specific rule rather than assuming that email needs OCR.

## T06 - Distinguish repeated imports from conflicts

**PM request:** "Welche Einträge sind doppelt, und wo brauchen wir eine Entscheidung?"  
**Meaning:** Group by document ID and explain what can safely be consolidated.  
**Prerequisite:** T05. **Time:** 2-3 hours. **Difficulty:** 3/5.

**Input:** Metadata grouped by trimmed `document_id`.

**Block A:** Find IDs that appear more than once. Display the rows next to each other in a small diagnostic output.

**Block B:** Compare all business fields after the documented normalizations, excluding `ingest_id`. An invalid value must retain its original value in the comparison key; two different invalid values must not become equal just because both typed outputs are blank.

**Block C:** For agreeing rows, keep the smallest `ingest_id` and log every removed import. For conflicting rows, withhold the whole group from the candidate export and queue every source row for human review.

**Deliverable:** `outputs/duplicate_decisions.csv` with `document_id`, `ingest_id`, `decision`, `kept_ingest_id`, and `reason`.

**Acceptance criteria**

- You distinguish an extra ingestion row from an extra logical document.
- Changing the order of input rows does not change which agreeing row is kept.
- Conflicting groups are not resolved by CSV order, status popularity, or a guessed newest version.
- Every input row receives a traceable duplicate disposition.
- The supplied text row for a document is not mistaken for a duplicate metadata ingestion.

**Hint:** Begin by printing groups of size greater than one. Do not combine grouping, comparison, and output writing in one large method.

**Decision question:** Which source would a data owner need to inspect to resolve a conflict? Record the question and the affected IDs.

## T07 - Join the reference tables

**PM request:** "Gehören die Dokumente zu bekannten Akten und Bearbeitern?"  
**Meaning:** Validate references and enrich records safely.  
**Prerequisite:** T06. **Time:** 1.5-2.5 hours. **Difficulty:** 3/5.

**Input:** `matter_id`, `reviewer_id`, `data/raw/matters.csv`, and `data/raw/reviewers.csv`.

**Block A:** Load the reference files with the supplied `Csv.read(Path.of(...))` helper and build maps by their IDs.

**Block B:** Match candidate documents to matters and reviewers. Check the reviewer's `active` value using the status-dependent assignment rule in the dictionary. Add readable names in a separate enriched report if useful.

**Block C:** Identify missing and unknown references separately. Do not drop an unmatched document just because a lookup failed.

**Deliverable:** `outputs/reference_checks.csv` with `ingest_id`, `document_id`, `field`, `reference_value`, and `result`.

**Acceptance criteria**

- A missing reference and a nonblank unknown reference have different issue codes.
- A blank reviewer is allowed for `Neu`; it is not automatically an error. An existing inactive reviewer is distinguishable from an unknown ID.
- The join does not increase the number of candidate documents.
- Unmatched rows remain traceable.
- You do not assign a reviewer based only on a matching department name.

**Hint:** Think of a map as a small lookup table: an ID is the key, a reference record is the value.

**Decision question:** A reviewer can be associated with another department. Unless the dictionary makes this an error, do not invent a restriction.

## T08 - Produce a repeatable cleanup package

**PM request:** "Wir brauchen nachvollziehbare Dateien für den nächsten Review."  
**Meaning:** Turn the previous steps into one repeatable run.  
**Prerequisite:** T07. **Time:** 2-3 hours. **Difficulty:** 3/5.

**Input:** The full metadata export and both reference tables.

**Block A:** Connect the stages in an order you can explain: load, profile, normalize, validate, group duplicates, join, export.

**Block B:** Write `outputs/cleaned_documents.csv`. Keep the original metadata column names and order; unparseable or out-of-range typed values are blank. Parseable dates with chronology flags remain visible. This is a candidate inventory with separately recorded issues, not a guarantee that all rows are ready for migration.

**Block C:** Write `outputs/review_queue.csv` and `outputs/reconciliation.csv`. Reuse issue records from prior tickets rather than silently losing them at export time.

**Required review-queue columns:** `ingest_id`, `document_id`, `field`, `raw_value`, `issue_code`, `message`. One record describes one issue. A source row may have several issues.

**Acceptance criteria**

- All output files are under `outputs/`; no raw input is overwritten.
- The cleanup can be rerun without manual edits between stages.
- The candidate file contains at most one row per `document_id`.
- Reconciliation shows raw rows = kept candidate rows + removed agreeing imports + withheld conflicting source rows.
- A second reconciliation shows unique IDs = candidate IDs + conflicting IDs.
- The issue count is reported separately; it is not subtracted from a row count, because one row can have multiple issues.
- Reading the written CSV with the supplied loader preserves German text and quoted content.

**Hint:** A useful final printout names each output path and its record count. A person reviewing the run should not have to search for its results.

**Decision question:** Which candidate cells still need the business team's input? Make the limitations visible even if the program ran successfully.

## T09 - Answer the PM's operational questions

**PM request:** "Wie groß ist der offene Arbeitsvorrat, und wo sollten wir nachsehen?"  
**Meaning:** Compute a small set of well-defined KPIs.  
**Prerequisite:** T08. **Time:** 2-3 hours. **Difficulty:** 3/5.

**Input:** Candidate unique documents plus their quality flags. Use the fixed snapshot `2026-11-20`.

Calculate these four measures before adding anything else:

1. **Candidate documents by department and status.** Include missing/unknown categories so the table reconciles.
2. **Open review workload.** Open means canonical status `Neu`, `In Prüfung`, or `Rückfrage`. Unknown status is excluded and reported separately.
3. **Past-target open reviews.** First select open candidates with a valid `received_date` on or before the snapshot and a valid `review_due_date` on or after receipt. Among that eligible group, past target means the target is strictly earlier than the snapshot. A target on the snapshot is due today, not past target. Report open documents excluded for unusable receipt/target dates or invalid chronology separately. Unrelated quality flags do not automatically exclude a document from this measure.
4. **Low OCR among usable scan scores.** Count canonical `Scan` records with valid `ocr_confidence` strictly below `0.80`. State the valid scan-score denominator; blank or invalid scores are excluded and counted separately.

**Optional extension:** Calculate review duration in calendar days for `Freigegeben` documents with valid received and reviewed dates, both on or before the snapshot, and a nonnegative difference. Show the number included and excluded. Do not treat missing completion dates as zero days.

**Deliverable:** `outputs/kpis.csv` and a draft one-page report using `templates/report_template.md`.

**Acceptance criteria**

- Every measure names its unit, population, date, and exclusions.
- A zero denominator produces a clear unavailable result, not a misleading percentage or a crash.
- Conflicting duplicate IDs are not quietly included in candidate-level KPIs.
- A high number of late reviews is described as an observation, not proof of poor staff performance.
- You verify one KPI manually on a few chosen records.

**Hint:** Compute the denominator first, then the numerator. Keep them as separate variables with descriptive names.

**Decision question:** Does the dataset tell you how long a person actually worked on a document? Explain why calendar delay is not the same as effort.

## T10 - Make one useful chart

**PM request:** "Kannst du die Verteilung übersichtlich zeigen?"  
**Meaning:** Make the main result easy to inspect.  
**Prerequisite:** T09. **Time:** 1.5-2 hours. **Difficulty:** 2/5.

**Input:** One frequency table from the candidate dataset. Start with documents by department.

**Block A:** Pass your calculated counts to `Charts.writeBarChart("departments.html", "Candidate documents by department", counts)`.

**Block B:** Open `outputs/departments.html` in a browser. Compare every bar label and value with the underlying frequency table.

**Deliverable:** One self-contained HTML bar chart and a two-sentence caption. A second chart for open statuses is optional.

**Acceptance criteria**

- The title identifies the population; axes or labels make the units clear.
- The category values and totals match the table.
- Missing or unknown categories are either shown or explicitly described in the caption.
- The chart is readable without color being the only way to distinguish categories.
- You explain one finding and one limitation; you do not claim a trend from one snapshot.

**Hint:** The helper supplies rendering. Your task is to select valid data and communicate what it means, not to learn HTML and CSS at the same time.

**Decision question:** Would a different denominator change the story? Make the chosen one visible.

## T11 - Hand over the project in five minutes

**PM request:** "Bitte fasse Ergebnis, offene Fragen und nächste Schritte zusammen."  
**Meaning:** Explain what you built and what the team can safely conclude.  
**Prerequisite:** T10. **Time:** 1.5-2 hours. **Difficulty:** 2/5.

**Input:** Your program, outputs, chart, and decision log.

**Block A:** Finish the report. Include three findings, the main quality limitations, and two useful next actions.

**Block B:** Add run instructions, the fixed snapshot, the input files, and the expected output filenames. Explain one normalization rule and one deliberately unresolved case.

**Block C:** Run the entire workflow from the repository root. Give a five-minute demonstration to a mentor, or record yourself explaining it.

**Deliverable:** `outputs/project_report.md`, the final Java work, and your completed decision log and journal.

**Acceptance criteria**

- Another person can reproduce your outputs from the documented command.
- The report distinguishes raw ingestion records, unique documents, and candidate documents.
- Every reported number is produced by your code or explicitly labeled as a manual observation.
- You can explain the main loop and one helper method without reading a generated explanation.
- Unresolved data issues and the limits of synthetic data are stated.
- No complete task solution is required from an AI tutor to explain your work.

**Hint:** Prepare a demonstration of one good row, one invalid value, and one conflict. This makes your decisions concrete.

**Next step:** If the core feels comfortable, choose the optional search and mock-answer exercises in `docs/llm_lab.md`. You do not need to finish them to complete this project.

## A practical help ladder

When stuck, describe the expected result, the observed result, and the smallest relevant input. Then ask for help at one of these levels:

1. Explain the concept or error message.
2. Give one hint or a tiny unrelated example.
3. Review my attempted method and point out one problem.
4. Help me correct a small piece while I explain the change.

Ask for a complete solution only if you deliberately choose to stop treating that part as an exercise. Keep the mentor directory closed until you have attempted the relevant task.
