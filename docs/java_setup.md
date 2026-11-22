# Java setup and the first successful run

Use **JDK 21** for this practice pack; it is the version used to verify the supplied programs. The code uses only Java's standard library. There is no Maven project, no Python environment, no database server and no API key. Use a full JDK, not a runtime-only installation. If your employer manages your computer, use its approved installation process. Your real work project may use another Java version; follow that project's requirements there.

## 1. Open the repository folder

Clone the repository with Git, or choose **Code > Download ZIP** on GitHub and extract it. Open the extracted or cloned `data-science-pratice` folder in your editor. The spelling is the repository's actual name.

The commands below must run in the folder containing `README.md`, `starter/` and `data/`. In IntelliJ, the **Terminal** tab is the easiest starting point. Windows PowerShell, macOS Terminal and a Linux shell use the same Java commands.

```text
java --version
```

Use major version 21 for the same setup as the verified pack. A newer full JDK should also run these basic programs, but has not been tested here. A message such as "java is not recognized" means Java is not on your PATH yet. Restart the terminal after installing a JDK. If source execution reports that `jdk.compiler` cannot be found, select a full JDK.

## 2. Run a tiny program

```text
java starter/StartHere.java
```

The program prints a project name, a department and a small document count. These are illustrative variables, not dataset results. Change `Einkauf` to `Personal` and `3` to `5`, save the file, and run the command again. This is task T00.

You do not need to type a separate `javac` command. Java's source-file mode compiles this single source file in memory and runs it. Keep the `.java` extension in the command.

## 3. Load twelve real practice rows

```text
java starter/Practice.java
```

Expected first-run behavior:

- The input path is `data/samples/documents_12.csv`.
- The loader reports 12 data rows, excluding the header.
- Three German document titles appear correctly.
- The last message says the exercise area is not implemented yet. That is intentional.

Open `starter/Practice.java` and find `runExercises`. This is your work area. The supplied CSV and chart helpers beneath it are plumbing; you can collapse them in the editor. Do not try to learn the whole helper implementation before starting T02.

## Optional bridge before T02

If lists, maps or loops are new, read `docs/java_basics.md` and run `java starter/CollectionsWarmup.java`. It uses a tiny book collection and three simple edits to introduce the syntax used in the exercise area. Return to T02 once you can explain the loop and one map lookup.

## 4. Use the full dataset when the task asks

```text
java starter/Practice.java data/raw/documents.csv
```

The loader reports 128 input rows. This is an ingestion-row count, not a count of unique documents. `data/raw/` is the unchanged source evidence. Write your results to `outputs/`.

## Supplied helper reference

`rows` in `runExercises` is a `List<Map<String, String>>`: a list of rows, each with column names mapped to their text values. For example, `rows.get(0).get("department")` reads the first row's department. All values arrive as strings; date and number validation are learner work. Raw strings are preserved, including deliberate spaces and empty values.

| Helper | Use |
| --- | --- |
| `Csv.read(Path.of("data/raw/matters.csv"))` | Read a semicolon CSV into a list of row maps. Use the same call for reviewers or document texts. |
| `Csv.writeReport("profile.csv", header, reportRows)` | Write `outputs/profile.csv`. `header` is a `List<String>` and `reportRows` is a `List<List<String>>`. Each row must match the header width. |
| `Charts.writeBarChart("departments.html", "Documents by department", counts)` | Write a self-contained HTML chart to `outputs/`. Supply your own `Map<String, Integer>` counts. Open the file in a browser. |

Use only a simple output filename, such as `profile.csv`; the helpers add `outputs/` themselves. An existing report with that name is overwritten. They reject paths such as `../data/raw/documents.csv`. The CSV writer quotes semicolons, quotation marks and newlines. The chart helper escapes category labels and contains no external scripts or network requests.

CSV is not safely parsed with `line.split(";")`: a title can contain a semicolon inside quotes, and a text field can span several physical lines. That is why a reader is supplied. The number of text lines in a CSV file is not always its number of data records.

## Check the supplied plumbing

```text
java starter/Practice.java --self-test
```

This checks quotation marks, German characters, multiline fields, malformed input, export round-tripping, HTML escaping and output-path restrictions. It does **not** check your completed assignments. A green support check is not evidence that your cleanup policy or analysis is correct.

## IntelliJ run configuration

1. Open the repository folder and set **Project SDK** to an installed JDK 21.
2. Run the commands above in the integrated Terminal first.
3. If you prefer the editor's Run button, open `Practice.java`, choose its `main` method and set the run configuration's **Working directory** to the repository folder. Leave program arguments empty for the sample; use `data/raw/documents.csv` for the full data.

If IntelliJ does not recognise the file as runnable Java, check the JDK and mark the `starter` folder as a **Sources Root** under **Mark Directory As**. You can always use the Terminal commands while configuring the editor.

These files have no `package` declaration and are self-contained. You do not need a framework or a multi-module Java project. This pack uses source-file mode with one source file at a time; keep your early helper methods inside `Practice`.

## Common problems

| Message or symptom | What to check |
| --- | --- |
| `File not found: data/samples/documents_12.csv` | The terminal or run configuration must start in the repository root, not in `starter/`. |
| Source-file mode or compiler-module error | Use a full JDK 21 and check which installation the terminal is using. |
| `cannot find symbol` after an edit | Check spelling, capitalization and whether you declared the variable before using it. The error includes a line number. |
| `';' expected` | Most Java statements end with a semicolon. Look at the reported line and the line above it. |
| German letters display incorrectly | Keep files in UTF-8; in IntelliJ check **File Encodings**. PowerShell 7 or Windows Terminal can help with terminal display. Do not replace umlauts in the raw data. |
| `NumberFormatException` for `0,92` | The dataset contains German number formatting. Decide and document a normalization rule before parsing; do not silently replace every error with zero. |
| Invalid dates are accepted unexpectedly | Java date parsing can be lenient/smart. When strict validation is required, use `ResolverStyle.STRICT` and patterns such as `uuuu-MM-dd` or `dd.MM.uuuu`; `yyyy` means year-of-era. |

Work in small steps: save, run, read the first error, change one thing, run again. When asking for help, paste the command and the exact error message together with the relevant few lines of code.

