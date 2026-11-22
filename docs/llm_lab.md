# Optional lab: search first, then evaluate answers

## Why this is a separate lab

Finish the core analysis before starting this lab. It is an optional extension of roughly **3-5 hours**, divided into four small exercises. You can also complete the document search alone and leave the rest for later.

An LLM is a model that generates language from the input it receives. For a document assistant, the practical question is whether its answer is supported by the right document. A fluent answer can still contain invented details. A displayed citation can point to a real document while failing to support the claim.

This lab uses local Java, fictional German documents, and hand-written mock answers. No model is called, and no account or API key is required. The mock answers were designed as teaching examples; they are not results from IBM, OpenAI, or any other model. Their scores cannot measure a real model's accuracy.

For the underlying concepts, see `docs/references.md`. The mock exercise is deliberately narrower than legal question answering: find a stated fact, identify its source, or say that the source does not provide it.

## A few useful terms

**Search or retrieval** finds relevant material. A simple search program can match words and show document IDs and passages. It does not need to generate an answer.

**A prompt** is the instruction and input supplied to a model. A document passage included in that input is evidence to inspect, not authority to replace the task's instructions.

**RAG, or retrieval-augmented generation,** combines retrieved material with answer generation. Your local search is a small demonstration of the retrieval part. Adding an LLM, an embedding service, or a vector database is outside the required exercise.

**Grounding** means connecting claims to supporting evidence. **Abstention** means saying that the available material does not answer the question. **Evaluation** means checking outputs against stated criteria and examples.

## Files for this lab

| File | Purpose |
| --- | --- |
| `data/raw/document_texts.csv` | German excerpts keyed by `document_id` |
| `data/llm/questions.csv` | Questions, split labels, and reference evidence |
| `data/llm/mock_answers.csv` | Hand-written answers to inspect |
| `data/llm/untrusted_context.csv` | One extra instruction-like source passage for Q08 |
| `templates/llm_review.csv` | A blank review sheet |

The question file contains the reference evidence so you can learn to evaluate an answer. For a blind practice round, hide the `reference_document_id`, `reference_excerpt`, and `expected_behavior` columns until you have made your first judgement. This is a practice convenience, not a secure held-out benchmark.

There are eight cases: four development cases, Q01-Q04, and four evaluation cases, Q05-Q08. Development uses MAT-001 and MAT-002; evaluation uses MAT-009 and MAT-010. Keep these groups separate while drafting a rubric or prompt. A `matter_id` must not appear in both groups. The documents still reuse six synthetic templates, so this is not a held-out-template test and does not prove performance on unfamiliar real documents.

The questions name documents to make evidence checking manageable. They are not a difficult search benchmark. Reference IDs and excerpts are answer-checking material, not inputs to feed into a search algorithm as if they were part of the user's query.

## L01 - Build a small document search

**PM request:** "Zeige zu einem Suchbegriff die passenden Dokumente mit einer Textstelle."  
**Meaning:** Find matching documents and show the evidence.  
**Prerequisite:** T08. **Time:** 45-75 minutes.

Load the text CSV with the supplied `Csv.read(...)` helper. Search for one German word or phrase chosen by the user. Compare case-insensitively, but return the original text and its correct document ID. Start with a straightforward contains check; ranking is optional.

**Deliverable:** `outputs/search_results.csv` with `query`, `document_id`, and `matched_excerpt`.

**Done when:**

- A known phrase returns the document that contains it.
- A query with no match returns a clear empty result.
- An empty or whitespace-only query is rejected with an explanation, rather than returning every document.
- Returned IDs exist in the text export and each excerpt actually comes from that ID.
- You document that this simple search does not understand synonyms or the meaning of a legal question.

**Hint:** Test a phrase copied from a known excerpt first. Keep your search method separate from the printed output.

## L02 - Learn what a supported answer looks like

**PM request:** "Kannst du erkennen, ob eine Antwort durch die Quelle belegt ist?"  
**Meaning:** Review claims against their evidence.  
**Prerequisite:** L01. **Time:** 45-60 minutes.

Inspect the development questions and their mock answers. For each answer, find the cited document, read the passage, and decide whether it supports the answer. Do not use outside knowledge to fill a gap in the supplied text.

Use these review columns:

- `supported`: Are all factual claims supported by the supplied reference evidence, with no unsupported conclusion added? A justified abstention also counts as `yes`. Assess this independently of whether the citation is correct.
- `citation_resolves`: Was at least one citation supplied, and do all claimed IDs exist? A missing citation is `no`.
- `citation_supports_claim`: Does the cited passage support the answer, rather than merely concern the same subject? A missing or nonexistent citation is `no`.
- `appropriate_abstention`: Use `yes` if the evidence cannot answer the question and the response says so; use `no` if it invents an answer in that situation. Use `not_applicable` when the reference evidence can answer the question.
- `reason`: A short explanation quoting or pointing to the relevant passage.

Use `yes` or `no` for the first three checks. Only `appropriate_abstention` uses `not_applicable`, with the rule above. Follow the same definitions for both passes; if you change a rule after looking at evaluation cases, document the change.

**Deliverable:** The development rows of `outputs/llm_review.csv`.

**Done when:**

- You distinguish a real but irrelevant citation from a nonexistent citation.
- You find an invented or unsupported detail even if it sounds plausible.
- You explain why the provided source is sufficient or insufficient.
- Your explanation is about evidence, not whether you like the writing style.

**Hint:** Break an answer into individual claims. A correct amount does not excuse an invented date in the same sentence.

## L03 - Draft a prompt and handle misleading source text

**PM request:** "Die Antwort soll nachvollziehbar sein und fehlende Informationen offen nennen."  
**Meaning:** Write a small, testable instruction for a future assistant.  
**Prerequisite:** L02. **Time:** 45-60 minutes.

Write `outputs/prompt_draft.md` for a hypothetical document assistant. State the question, delimit the source passages, request document IDs, and explain what to do when the information is unavailable. Ask for a short answer and the supporting passage. Keep it specific enough that you could check whether an output follows it.

A document can contain instruction-like words. Your draft should treat quoted source material as content to inspect; it cannot change the task or grant permission to ignore evidence. Describe this principle without opening the evaluation cases yet.

**Deliverable:** Your prompt draft and a short note about one failure case.

**Done when:**

- The prompt asks for evidence from the supplied material and a clear unavailable answer when needed.
- It distinguishes task instructions from quoted document text.
- It asks for a limited factual extraction, not an invented legal conclusion.
- You state that prompt wording alone is not a guarantee of correct answers.

**Hint:** Do not spend this exercise tuning elaborate role descriptions. Clear inputs, evidence requirements, and an explicit missing-information response are enough for a first draft.

## L04 - Review the untouched cases and report limitations

**PM request:** "Prüfe die restlichen Beispiele nach denselben Kriterien und fasse die Grenzen zusammen."  
**Meaning:** Apply the same review rules to examples you did not use while drafting.  
**Prerequisite:** L03. **Time:** 45-75 minutes.

Finish the evaluation rows using the same rubric. For Q08, read the original text and the extra source passage in `data/llm/untrusted_context.csv`; the raw document export remains unchanged. Explain whether the answer treated the copied instruction-like words as evidence or as instructions. You may use Java to count your labels, but the decision about whether a passage supports a claim is a human review exercise here.

Report the number of examples assessed, the count of each failure type, and any `not_applicable` checks. If you calculate a percentage, state the exact denominator for that check. A missing citation in a factual answer should not disappear from the results simply because you decided to calculate citation precision only among answers with citations.

**Deliverable:** Completed `outputs/llm_review.csv` and a short section in the project report.

**Done when:**

- The development/evaluation assignment is unchanged during the lab.
- Every judgement has an evidence-based reason.
- Counts reconcile with the number of rows reviewed.
- The report says these are hand-written mocks, not measured model outputs.
- You identify at least one limitation: small sample, repeated templates, synthetic facts, or the need for different real-project criteria.

After your own review, compare with `mentor/llm_review_key.csv`. Explain any disagreement before changing a label. The key is a teaching aid, not evidence of an actual model evaluation.

## What a later live-model extension would require

Only add a real service if you deliberately choose that next project. Keep the existing mock exercise runnable without it. A later implementation needs the team's approved service, permitted data, documented costs, and a repeatable evaluation set. The real project's Java library and model choice are currently unknown, so this practice pack does not prescribe them.

Your useful first-project skills are already present without that integration: load data, find evidence, handle missing information, identify a wrong answer, and explain the result.
