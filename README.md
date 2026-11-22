# Aktenblick: Java data practice

A fictional German document-management project for a beginner learning to code and analyse data.

Your client is the invented **Rheinblick Service GmbH**. Its team wants to prepare a messy document export for an internal migration review. You will inspect the export, clean values without inventing facts, explain unresolved problems, and produce a small operational report. An optional later lab introduces document search and evidence checks for LLM answers.

**Start with T00 and T01. You do not need to understand this whole repository on day one.**

## Your first 45-60 minutes

1. Read the first part of the [project brief](docs/pdf/01_project_brief.pdf).
2. Follow [Java setup](docs/java_setup.md). The practice pack uses **JDK 21**, chosen for this exercise; your actual workplace project may specify a different version.
3. Run the tiny warm-up and change the one value requested in its comments.
4. Run the supplied CSV loader on the twelve-row sample.
5. Write down what three fields mean and two questions you would ask the data owner. Stop there for the first session.

From a terminal:

```text
git clone --branch master https://github.com/Unfixab1e/data-science-pratice.git
cd data-science-pratice
java --version
java starter/StartHere.java
java starter/Practice.java
```

If the repository is already on your computer, open a terminal in that folder and start at `java --version`. IntelliJ instructions and common errors are in the setup guide.

The first program is a small variables-and-printing exercise. The second loads **12 records** and prints three titles. Its message says the analysis exercises are not implemented yet. That is intentional: you write the analysis in the clearly marked `runExercises` section.

## Three connected projects

| Project | What you build | Tasks | Approximate learning time |
| --- | --- | --- | --- |
| 1. Understand the export | A data profile and category frequency tables | T00-T03 | 4-5 hours |
| 2. Prepare a reviewable inventory | Normalized candidate data, issue queue, duplicate decisions and reference checks | T04-T08 | 10-16 hours |
| 3. Explain the findings | Operational KPIs, one chart and a short PM handover | T09-T11 | 5-7 hours |
| Optional: search and LLM evidence | A keyword search, a draft prompt and reviews of eight mock answers | L01-L04 | 3-5 hours |

Plan roughly **20-28 hours for the core**, split into small sessions. These are learning estimates, not deadlines. Debugging may take longer, and stopping after each useful step is fine.

The early work uses a few columns, ordinary loops, strings, methods, lists and maps. CSV parsing and chart rendering are supplied so you can focus on the data. No Python, Maven, database, paid API, cloud account or model download is required.

If loops, lists or maps are new, pause before T02 for the [small Java bridge](docs/java_basics.md). Run `java starter/CollectionsWarmup.java` and make its three tiny book-list changes. Allow an extra session or two; this is preparation, not a test you need to pass quickly.

## Read the handouts in this order

| Handout | PDF | Editable source |
| --- | --- | --- |
| Project brief: client, scope and success criteria | [01_project_brief.pdf](docs/pdf/01_project_brief.pdf) | [project_brief.md](docs/project_brief.md) |
| Twelve task assignments with acceptance criteria and hints | [02_assignments.pdf](docs/pdf/02_assignments.pdf) | [assignments.md](docs/assignments.md) |
| Data meanings, mappings and cleaning rules | [03_data_dictionary.pdf](docs/pdf/03_data_dictionary.pdf) | [data_dictionary.md](docs/data_dictionary.md) |
| Java setup, helper reference and troubleshooting | [04_java_setup.pdf](docs/pdf/04_java_setup.pdf) | [java_setup.md](docs/java_setup.md) |
| Optional search and mock LLM evaluation | [05_optional_llm_lab.pdf](docs/pdf/05_optional_llm_lab.pdf) | [llm_lab.md](docs/llm_lab.md) |
| German-English project vocabulary | [06_glossary.pdf](docs/pdf/06_glossary.pdf) | [glossary.md](docs/glossary.md) |
| Optional bridge: lists, maps, loops and methods | [07_java_basics.pdf](docs/pdf/07_java_basics.pdf) | [java_basics.md](docs/java_basics.md) |

Use the dictionary as a reference for your current ticket. It is not another document to memorise before starting.

## What is in the dataset?

| File | Contents |
| --- | --- |
| [documents_12.csv](data/samples/documents_12.csv) | A small starting sample, unchanged from the first twelve import records |
| [documents.csv](data/raw/documents.csv) | 128 import records representing 120 logical documents, with 17 metadata fields |
| [matters.csv](data/raw/matters.csv) | 12 fictional matters/cases for join practice |
| [reviewers.csv](data/raw/reviewers.csv) | 6 fictional reviewers for reference checks |
| [document_texts.csv](data/raw/document_texts.csv) | 120 German text excerpts covering six document types |
| [PDF document samples](data/samples/pdf/) | Six one-page reading facsimiles, one for each document type |
| [Optional LLM fixtures](data/llm/) | Eight questions, imagined answers and a small untrusted-text exercise |

All CSVs use **UTF-8, semicolons and standard CSV quoting**. Some cells contain quotes, semicolons or line breaks. Use the supplied reader; `split(";")` is not sufficient. If inspecting in Excel or another spreadsheet application, import with UTF-8 and semicolon separation and keep IDs as text. Do not save over the original export.

The data deliberately includes inconsistent spelling, missing values, mixed date/number formats, invalid values, duplicate imports, conflicting metadata and broken references. Some unusual values are allowed; the dictionary explains which ones. A formatting variation is not automatically a defect.

The fixed snapshot is **20 November 2026**. Use it for the exercises even if you work on a later date. `review_due_date` is a fictional **internal review target**, never a legal deadline.

## Where to write your work

- Edit the small exercise in `starter/StartHere.java`, then the learner area in `starter/Practice.java`.
- Put generated CSVs, charts and the final report in `outputs/`.
- Put your notes in `work/`; copy the [journal, decision log and report templates](templates/) when needed.
- Preserve `data/raw/` and the supplied samples so results remain repeatable.
- Use small Git commits at useful stopping points. A personal practice branch is optional.

When you are ready for the full export:

```text
java starter/Practice.java data/raw/documents.csv
```

The helper check below verifies the supplied parser and output tools. It does not grade your analysis:

```text
java starter/Practice.java --self-test
```

## For the mentor

The [mentor guide](mentor/mentor_guide.pdf) contains hints, review criteria and checkpoint information. [Machine-readable checkpoints](mentor/data/) identify the deliberately planted cases. The [LLM review key](mentor/llm_review_key.csv) explains the eight mock answers. Keep these separate from the learner's first attempts.

There is no completed end-to-end solution hidden in the starter. [AGENTS.md](AGENTS.md) asks coding assistants to teach one small step at a time. An [optional coaching prompt](mentor/coaching_prompt.md) makes that approach easy to reuse.

Maintainers can run the integrity check or regenerate the fixed data into a separate empty directory. See [tools/README.md](tools/README.md).

## Boundaries of the exercise

This is an independent fictional training pack, not an IBM or client project specification. No production architecture, legal requirement, or real-model performance is inferred from it. The corpus is small and reuses six template families; it is suitable for practice, not for establishing a model's real-world accuracy.

The PDF samples are generated facsimiles of excerpts, not scanned originals. OCR scores and source labels are simulated. Extracted text is already supplied, so PDF extraction and OCR are not prerequisites. The optional model responses are hand-authored examples and run entirely offline.

See [data provenance](data/README.md) and [technical references](docs/references.md) for details.
