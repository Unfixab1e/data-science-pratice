# Maintainer tools

Learners can ignore this folder during the core exercises.

## Check the supplied practice pack

From the repository root, with JDK 21:

```text
java tools/VerifyPack.java
java starter/Practice.java --self-test
```

The first command checks the authored fixtures and intentionally reads mentor checkpoints. It verifies the pack, not a learner's analysis. The second command checks only the supplied file and chart helpers.

## Regenerate the metadata and document corpus

```text
java tools/GenerateDataset.java ../aktenblick-regenerated
```

Use a nonexistent or empty destination directory, as required by the generator. Compare its generated files with this repository before replacing anything. Do not regenerate directly into your working repository or overwrite a learner's notes.

The generator uses fixed inputs and requires no network connection. It generates the raw/small datasets, readable text samples, the data manifest, and mentor dataset checkpoints. It does not generate the hand-authored optional LLM fixtures, Markdown handbooks, or PDF reading copies.

## Documentation

The Markdown files are the editable source for the PDF handouts. If you change teaching content, update the corresponding PDF reading copy and check that long tables, German characters, and page breaks still render correctly. No Python installation, PDF library, or documentation build system is needed to use this practice pack.
