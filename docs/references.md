# References and provenance

This is an independently authored, fictional practice project. Its organisation, stakeholders, operational rules, document excerpts, labels, and model responses were invented for teaching. It does not describe an IBM engagement, a real client, or a real legal requirement.

The dataset is deliberately small, patterned, and imperfect. It can support programming exercises and checks on this fixture; it cannot establish how German legal departments operate, how a production model performs, or what a real organisation must do.

## Technical references

Checked on 22 November 2026. These are optional reference pages, not prerequisite reading.

| Topic | Primary source | Why it is useful |
| --- | --- | --- |
| Run one Java file | [Dev.java: Launching Simple Source-Code Programs](https://dev.java/learn/first-steps/first-java-code/launch-simple-source-code-programs/) | Explains `java File.java`. The starter uses self-contained files, so it does not need the multi-file launcher introduced in JDK 22. |
| Java launcher | [Java 17 command reference](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html) | Documents source-file mode and command-line arguments. |
| Date parsing | [Java 17 DateTimeFormatter](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html) | Reference for date patterns and explicit resolver styles. Use `uuuu` with strict date parsing. |
| Invalid calendar dates | [Java 17 ResolverStyle](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/ResolverStyle.html) | Explains why strict parsing rejects impossible dates rather than adjusting them. |
| Text files and UTF-8 | [Java 17 Files](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html) | Reference for reading and writing local files. |
| IntelliJ source folders | [JetBrains: Content roots](https://www.jetbrains.com/help/idea/content-roots.html) | Describes marking a directory as a source folder when using the editor's Run button. |
| Retrieval and generation | [IBM: Retrieval augmented generation](https://www.ibm.com/think/topics/retrieval-augmented-generation) | Optional background on adding retrieved evidence to a model prompt. Retrieval does not guarantee a correct answer. |

## What is supplied and what is simulated

The CSV files are fictional system exports. The German document texts simulate already extracted text. The accompanying example PDFs are generated teaching facsimiles of selected excerpts, not scanned originals. The OCR scores and scanning errors are invented. No OCR engine, live DMS, legal database, or actual model was used to produce the teaching examples.

The optional LLM answers are authored fixtures. Their review results measure whether the learner recognises the planted examples; they are not an evaluation of any IBM product or other language model.

## Reproducibility

`tools/GenerateDataset.java` regenerates the dataset into a separate destination using fixed inputs. The raw files are preserved for repeatable exercises. Mentor files contain checkpoints and fixture checks. Editable Markdown sits alongside the PDF handouts; the PDF files are reading copies of those documents.
