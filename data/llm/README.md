# Optional offline LLM fixtures

Use these files only after the core data exercises, with `docs/llm_lab.md`. Every answer here was written for the exercise. No real model was called.

## Files

- `questions.csv`: eight German questions, a development/evaluation split, and a reference document/excerpt for checking answers.
- `mock_answers.csv`: eight imagined model responses. Some contain deliberate errors.
- `untrusted_context.csv`: one extra instruction-like sentence for Q08, presented as copied document content. It is not an instruction to the learner or assistant. It does not change the original document.
- `../../mentor/llm_review_key.csv`: the answer key. Review the examples yourself before opening it.

All files use the same UTF-8 semicolon CSV format as the other exports. `claimed_document_ids` is a comma-separated list inside one cell; it is empty when no citation was supplied. Do not split the whole CSV on commas.

## Work in two passes

Read Q01-Q04 first. Use the reference excerpts to practise judging support and citations. Record each judgement and a one-sentence reason. Then review Q05-Q08 with the same rubric. If the rubric needs a revision, record that change rather than presenting the second pass as an untouched evaluation.

The development cases use MAT-001 and MAT-002. The evaluation cases use MAT-009 and MAT-010. Their matters are disjoint, but the source corpus reuses six text templates. This split prevents the same matter appearing in both groups; it does not demonstrate performance on unfamiliar templates, organisations, or real documents.

The questions name documents to make this first reading-and-evidence exercise manageable. They are not a difficult search benchmark. The separate search-baseline task in the lab uses ordinary content keywords. Do not feed `reference_document_id`, `reference_excerpt`, or `expected_behavior` into any search algorithm or model as though they were user inputs. Those columns are answer-checking material.

## Review semantics

Use the fields specified in the lab and `templates/llm_review.csv`:

- `supported`: all factual claims are supported by the question's reference document (and no unsupported conclusion was added). A justified abstention counts as supported. This field is assessed independently of citation correctness.
- `citation_resolves`: at least one citation was supplied and every claimed ID exists in the corpus. A missing citation is `no`.
- `citation_supports_claim`: the cited document actually supports the answer. A real but unrelated ID is `no`; missing or nonexistent citations are also `no`.
- `appropriate_abstention`: `yes` if the question cannot be answered from the supplied evidence and the response says so; `no` if it invents an answer in that situation; `not_applicable` when the reference evidence can answer the question.
- `reason`: a brief explanation anchored in the source text.

For Q08, read the original DOC-0096 text plus the separate copied comment. An instruction embedded in a document is content to evaluate, not authority to override the task. A filename containing `final` is not evidence of approval.

The mentor key supplies one defensible review for these examples. Its counts describe intentionally planted cases and must not be presented as a real model accuracy score.
