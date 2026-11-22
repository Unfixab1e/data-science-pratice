# Aktenblick synthetic export

This is a small, deliberately imperfect training export from an invented document-management system. Rheinblick Service GmbH, its counterparties, people, records, identifiers and document excerpts are fictional. The material does not describe IBM systems, a real client, real litigation or actual German legal requirements.

## Start small

Begin with `samples/documents_12.csv`. It contains the first 12 records from the full metadata export without any corrections. Move to `raw/documents.csv` once your program can load the sample reliably. The metadata export has 128 ingestion records; an ingestion record is not necessarily a different document.

All CSV files use UTF-8 without a byte-order mark, semicolons between fields, and standard double-quote escaping. Some fields contain semicolons, quotation marks or newlines. Use the supplied CSV reader; `split(";")` and line counting are not valid parsers for these files. Blank is the only missing-value marker. Do not interpret an empty OCR score as zero.

| File | What one record means | Intended use |
| --- | --- | --- |
| `raw/documents.csv` | One document ingestion | Core analysis and cleanup |
| `raw/matters.csv` | One matter or business file | Join metadata to a named matter |
| `raw/reviewers.csv` | One reviewer | Validate assignment references |
| `raw/document_texts.csv` | One logical document excerpt | Optional text search and LLM evaluation |
| `samples/documents_12.csv` | One of the first twelve ingestions | First small exercises |
| `samples/csv_edge_cases.csv` | One CSV parser example | Five cases covering quotes, newlines and empty fields |
| `samples/text/` | One readable excerpt per example | Read six examples before working with text |
| `manifest.json` | Dataset metadata and hashes | Maintainer provenance and integrity |

The text table has one excerpt for each of the 120 logical document IDs. Join on a trimmed `document_id`, not the ingestion ID. A duplicate ingestion does not need an additional text record. The reference tables contain 12 matters and 6 reviewers. The data dictionary in `docs/data_dictionary.md` defines every field and the classroom cleaning contract.

## Reporting date and interpretation

Use **2026-11-20** as the fixed snapshot date. Use `LocalDate.of(2026, 11, 20)` in reproducible analysis. Do not use the day on which you happen to run the program.

`review_due_date` is a made-up **internal operational review target**. It is never a statutory deadline, retention rule or legal recommendation. `Freigegeben` means the training document workflow is marked reviewed; it does not prove that a legal conclusion, contract or payment has been approved.

Raw values intentionally include data-quality defects. Keep the raw files unchanged and write your outputs under `outputs/`. A cleaning program should record uncertainty and unresolved conflicts instead of inventing values. Details of planted anomalies and selected check totals are kept separately under `mentor/`.

## Synthetic-data limitations

The 120 text excerpts were generated from six template families and varied with invented IDs, dates, counterparties, locations and amounts. The six document categories are balanced in the underlying fixture. This is useful for a first programming exercise but is not representative of the distribution, difficulty or linguistic variety of production legal documents. Do not interpret classifier scores on this tiny set as expected production performance.

The export is already text-based. Reading the provided text is the core task. OCR, PDF extraction, legal interpretation, access-control deployment and connecting a live LLM service are outside the required exercises. Optional PDF examples are facsimiles of six text excerpts, not the source of truth for the entire corpus.

## Reproduce the fixture (maintainers only)

The fixture factory uses standard Java libraries, is deterministic, and needs no API or model. JDK 21 is recommended and tested. From the repository root:

```text
java tools/GenerateDataset.java ../aktenblick-regenerated
```

The target must be a new or empty directory. The generator refuses a non-empty destination and does not overwrite this checkout. It creates `data/` and `mentor/data/` below that destination. Compare the generated hashes with the committed manifest before replacing a fixture. Do not run this step as an exercise prerequisite; the dataset is already included.

The manifest covers the CSV files, six sample TXT excerpts and mentor data produced by the generator. Handwritten instructions, exercises and PDF facsimiles are outside its hash scope.
