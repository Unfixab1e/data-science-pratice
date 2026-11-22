# Aktenblick: project brief

## A small client project for your first Java analysis

**Training client:** Rheinblick Service GmbH, an invented company.  
**Team:** Legal Operations and Document Services, also invented.  
**Snapshot date:** 20 November 2026.  
**Your role:** Junior developer and data analyst.  
**Main language:** Java. The documents and business terms are German; explanations are in plain English.

This is a fictional practice assignment, created to help you prepare for a document-management project. It is not an IBM project description, an IBM training course, or a prediction of your first assignment. The dataset contains no client records. Its companies, people, matters, documents, amounts, and operational rules were invented for practice.

## The client's problem

Rheinblick has accumulated documents in shared folders, email attachments, and scanned paper files. The Legal Operations team wants to move the inventory into one document-management system. A pilot export has arrived, but different teams entered dates, department names, and status values differently. Some documents were imported twice. A few records refer to unknown matters or reviewers.

The project manager does not yet want an automatic legal assistant. She needs a reliable answer to four practical questions:

1. What is in this export?
2. Which records need repair or a human decision before migration?
3. How much internal review work is waiting, and where?
4. Could employees later find relevant passages without reading every document?

Your first deliverable is a small, explainable Java analysis. The final core deliverable is a repeatable cleanup and an operational report. A search and LLM exercise is available after the core work.

### The PM's first message

> Guten Morgen! Bitte prüfe zuerst, welche Dokumente im Export enthalten sind. Wir brauchen eine Übersicht nach Abteilung und Status sowie eine Liste der Datenprobleme. Bitte ändere die Originaldateien nicht. Wenn Informationen fehlen oder widersprüchlich sind, notiere die Frage für das Fachteam.

In English: first describe the documents, count them by department and status, list data problems, preserve the source files, and ask the business team about missing or conflicting information.

## Your fictional project team

| Role | What this person decides |
| --- | --- |
| PM, Mira Keller | Priorities, the report audience, and what fits in the pilot |
| Legal Operations lead, Jonas Weber | The meaning of statuses and which conflicts need review |
| Data owner, Lea Winter | Correct source values and ownership of missing information |
| Senior developer, Sam Adler | Technical review, Java questions, and maintainability |
| You, junior developer | Reproducible analysis, transparent cleanup, and clear questions |

These names are fictional role-play characters. You do not need to contact anyone. Copy `templates/decision_log.csv` to `outputs/decision_log.csv` and record questions there; a mentor can answer as the fictional data owner.

## The files you receive

The main export contains **128 ingestion records for 120 logical documents**. An ingestion record is one row received from an import. A logical document is identified by `document_id`; several ingestion rows can refer to it.

| File | What it represents |
| --- | --- |
| `data/samples/documents_12.csv` | A small first exercise, taken from the main export |
| `data/raw/documents.csv` | The unmodified metadata export |
| `data/raw/matters.csv` | Twelve matters, or business files, to which documents belong |
| `data/raw/reviewers.csv` | Six fictional staff records |
| `data/raw/document_texts.csv` | German text excerpts, one for each logical document |

Read `docs/data_dictionary.md` when a field is unfamiliar. The data uses UTF-8 and a semicolon separator. Quoted values can contain semicolons, quotation marks, and line breaks. The supplied Java loader handles this formatting, so your first task is not to build a CSV parser.

The metadata has useful imperfections on purpose. You are expected to discover them, not to repair the source files by hand. Missing titles, invalid dates, and unrecognized identifiers are examples of information that code must not invent.

## Three connected mini projects

### 1. Document inventory: understand before changing

Work on twelve rows first. Read several German titles, print a few values, and count records. Then calculate missing values and simple frequency tables on the full export.

The result is a short profile: the size of the export, the categories it contains, and questions that prevent a confident interpretation. You practise variables, loops, strings, lists, maps, and explaining a number.

### 2. Migration preparation: cleanup and reporting

Apply documented mappings, parse dates and numbers, distinguish repeated imports from conflicting versions, and check links to matters and reviewers. Write new outputs and a list of issues; keep the originals unchanged.

The result is a candidate inventory, a review queue, a reconciliation of row counts, and a one-page report with a chart. You practise methods, validation, joins, error handling, and reproducibility. A candidate record may still have flagged cells; the file is not a declaration that every document is ready for production migration.

### 3. Search and LLM evaluation: optional

Search the supplied document text and show the passage that matched. Then inspect hand-written mock answers to a small set of questions. Decide whether each claim is supported by its cited passage and whether the answer should instead admit that the information is unavailable.

No model account, API key, paid service, or external upload is needed. This exercise teaches evidence checking before adding a live model. It is deliberately separate from the core Java work.

## The agreed pilot rules

These are fictional operational requirements for this exercise. They are not legal rules.

- Use **2026-11-20** as the analysis date every time, even if you complete the exercise later.
- Keep source files unchanged. Write learner results under `outputs/`.
- A blank or whitespace-only source cell means missing. Zero is not missing.
- Normalize only with explicit mappings from the data dictionary. Unknown values stay visible as issues.
- Use strict date parsing. An impossible date is an issue, not permission to move it to another day.
- Keep the smallest `ingest_id` when repeated imports of the same `document_id` agree in all business fields. Keep an audit entry for the removed import.
- Send every row of a conflicting `document_id` group for manual review. Do not assume that the last CSV row is the newest or correct version.
- Missing values remain blank. Values that fail parsing or their numeric range check become blank in the candidate export, with the original value and explanation preserved in the issue log. Keep parseable calendar dates that fail a chronology rule, flag the relationship, and exclude them from calculations that depend on that relationship. Never guess a date, page count, owner, or title.
- `review_due_date` is an internal review target supplied by the fictional business team. Do not calculate or infer legal deadlines.
- Human reviewers decide what a document means. The exercises do not determine legal validity, liability, rights, or retention duties.

## What counts as success

The PM must be able to trace a reported number to its input and definition. A useful answer looks like this:

> Among the eligible unique candidate documents, X have an open status. Y of those have a valid internal target date earlier than 20 November. Z open documents have no valid target date, so they are excluded from the overdue calculation.

That is stronger than a large percentage without a denominator. If a record is unresolved, saying so is a valid result. The goal is a small program whose choices you can explain.

The core delivery contains:

1. Java code that you can rerun from the repository root.
2. A profile of the original export.
3. Candidate records, issue records, duplicate decisions, and a row reconciliation.
4. A short operational report with one or two readable charts.
5. A decision log and a short description of limitations.

There is no requirement to create a website, database, Spring application, vector database, or trained model. There is also no requirement to solve every data problem automatically.

## A realistic working rhythm

The twelve core tickets are intended for approximately **20-28 hours of learning**, spread across short sessions. These are planning estimates, not a performance target. Setup, unfamiliar Java concepts, and debugging may take longer. Work in 30-90 minute blocks and stop at a checkpoint rather than rushing to finish a ticket.

For the first 45-60 minutes, run the tiny Java example, run the sample loader, and write down what three records mean. Do not start the full cleanup on day one. The exact steps are T00 and T01 in the assignment book.

## Questions to ask on the real assignment

Once the actual project is explained, these questions are more useful than guessing its architecture:

- What does the team mean by "legal documents": contracts, correspondence, court material, policy documents, or something else?
- What is the first deliverable expected from me, and how will someone check it?
- What Java version, IDE, build tool, and existing application should I use?
- Will I work with metadata, document text, PDFs, OCR output, or all of them?
- Which identifiers are stable, and what does the team call a duplicate or a new version?
- Who decides how missing, inconsistent, or confidential information is handled?
- Is an LLM actually part of my first task? If so, what approved service and evaluation process already exist?
- Which sample data and development environment am I allowed to use?

This practice pack gives you vocabulary and small coding habits to use in that conversation. Adapt to the real team's definitions when you receive them.
