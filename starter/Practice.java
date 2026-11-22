import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aktenblick practice workspace. Use JDK 21 or newer; no libraries.
 * Run from the repository folder:
 *   java starter/Practice.java
 *   java starter/Practice.java data/raw/documents.csv
 *   java starter/Practice.java --self-test
 *
 * Start in runExercises(). The Csv and Charts classes are supplied tools.
 * You do not need to understand or change their internals to begin.
 */
public class Practice {
    public static void main(String[] args) {
        try {
            if (args.length == 1 && args[0].equals("--self-test")) {
                supportSelfTest();
                return;
            }
            if (args.length > 1 || (args.length == 1 && args[0].startsWith("--"))) {
                throw new IllegalArgumentException(
                    "Usage: java starter/Practice.java [CSV path | --self-test]");
            }
            Path input = Path.of(args.length == 0
                ? "data/samples/documents_12.csv" : args[0]);
            List<Map<String, String>> rows = Csv.read(input);
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("The input has a header but no data rows.");
            }
            if (!rows.get(0).containsKey("document_id") || !rows.get(0).containsKey("title")) {
                throw new IllegalArgumentException("Choose documents.csv or documents_12.csv.");
            }

            System.out.println("Aktenblick | supplied loader");
            System.out.println("Input: " + input);
            System.out.println("Loaded rows (header excluded): " + rows.size());
            System.out.println("First three titles:");
            for (int i = 0; i < Math.min(3, rows.size()); i++) {
                System.out.println("  " + rows.get(i).get("title"));
            }
            System.out.println();
            runExercises(rows);
        } catch (IOException | IllegalArgumentException error) {
            System.err.println("Could not run: " + error.getMessage());
            System.err.println("Run from the folder containing README.md, starter/ and data/.");
            System.exit(1);
        }
    }

    /** Your work starts here. Implement one task at a time. */
    static void runExercises(List<Map<String, String>> rows) throws IOException {
        // A row is a Map: column name -> text value.
        // Example lookup: rows.get(0).get("department")
        // Every imported value is text, even a date or a page count.

        // T02: Count nonblank and blank values for the requested columns.
        // T03: Count categories. Start with one column and a normal for loop.
        // T04: Create normalized COPIES; preserve the raw input.
        // T05: Parse/validate dates and numbers; record problems explicitly.
        // T06: Identify exact duplicates and conflicts; do not guess a winner.
        // T07: Join matters and reviewers by their IDs.
        // T08: Write the agreed outputs with Csv.writeReport(...).
        // T09: Calculate the agreed operational KPIs and explain denominators.
        // T10: Pass your own counts to Charts.writeBarChart(...).
        // T11: Write your findings and limitations in your handover.

        System.out.println("Exercise area: not implemented yet. Start with T02.");
        System.out.println("This successful load is not a completed analysis.");
    }

    // ---------------------------------------------------------------------
    // SUPPLIED SUPPORT. You can collapse everything below this line in an IDE.
    // This is infrastructure, not a solution to the analysis assignments.
    // ---------------------------------------------------------------------

    /** UTF-8 semicolon CSV with quotes, escaped quotes and multiline fields. */
    static class Csv {
        static List<Map<String, String>> read(Path path) throws IOException {
            if (!Files.isRegularFile(path)) {
                throw new IOException("File not found: " + path);
            }
            List<List<String>> table;
            try {
                table = parse(Files.readString(path, StandardCharsets.UTF_8));
            } catch (IllegalArgumentException error) {
                throw new IOException(path + ": " + error.getMessage(), error);
            }
            if (table.isEmpty()) {
                throw new IOException(path + ": missing CSV header");
            }
            List<String> header = table.get(0);
            for (int i = 0; i < header.size(); i++) {
                if (header.get(i).isBlank() || header.subList(0, i).contains(header.get(i))) {
                    throw new IOException(path + ": blank or duplicate header: " + header.get(i));
                }
            }
            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i < table.size(); i++) {
                List<String> values = table.get(i);
                if (values.size() != header.size()) {
                    throw new IOException(path + ": record " + (i + 1) + " has "
                        + values.size() + " fields; expected " + header.size());
                }
                Map<String, String> row = new LinkedHashMap<>();
                for (int j = 0; j < header.size(); j++) {
                    row.put(header.get(j), values.get(j));
                }
                rows.add(row);
            }
            return rows;
        }

        static List<List<String>> parse(String source) {
            String text = source.startsWith("\uFEFF") ? source.substring(1) : source;
            List<List<String>> table = new ArrayList<>();
            List<String> row = new ArrayList<>();
            StringBuilder value = new StringBuilder();
            boolean inQuotes = false;
            boolean afterQuote = false;
            boolean recordStarted = false;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (inQuotes) {
                    if (c == '"') {
                        if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                            value.append('"');
                            i++;
                        } else {
                            inQuotes = false;
                            afterQuote = true;
                        }
                    } else {
                        value.append(c);
                    }
                } else if (c == ';') {
                    row.add(value.toString());
                    value.setLength(0);
                    afterQuote = false;
                    recordStarted = true;
                } else if (c == '\n' || c == '\r') {
                    row.add(value.toString());
                    table.add(row);
                    row = new ArrayList<>();
                    value.setLength(0);
                    afterQuote = false;
                    recordStarted = false;
                    if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else if (c == '"' && value.length() == 0 && !afterQuote) {
                    inQuotes = true;
                    recordStarted = true;
                } else {
                    if (afterQuote || c == '"') {
                        throw new IllegalArgumentException("Unexpected quote/text in CSV near character " + i);
                    }
                    value.append(c);
                    recordStarted = true;
                }
            }
            if (inQuotes) {
                throw new IllegalArgumentException("Unclosed quoted CSV field");
            }
            if (recordStarted || !row.isEmpty() || value.length() > 0 || afterQuote) {
                row.add(value.toString());
                table.add(row);
            }
            return table;
        }

        /** filename is a simple name, e.g. profile.csv. Writes only in outputs/. */
        static void writeReport(String filename, List<String> header,
                                List<List<String>> rows) throws IOException {
            if (header.isEmpty()) throw new IllegalArgumentException("Output needs column names");
            for (int i = 0; i < header.size(); i++) {
                if (header.get(i) == null || header.get(i).isBlank()
                        || header.subList(0, i).contains(header.get(i))) {
                    throw new IllegalArgumentException("Output column names must be nonblank and unique");
                }
            }
            List<List<String>> table = new ArrayList<>();
            table.add(header);
            for (List<String> row : rows) {
                if (row.size() != header.size()) {
                    throw new IllegalArgumentException("Output row width differs from header width");
                }
                table.add(row);
            }
            String content = encode(table);
            Files.writeString(outputPath(filename), content, StandardCharsets.UTF_8);
        }

        static String encode(List<List<String>> table) {
            StringBuilder result = new StringBuilder();
            for (List<String> row : table) {
                for (int i = 0; i < row.size(); i++) {
                    if (i > 0) result.append(';');
                    String value = row.get(i);
                    if (value == null) {
                        throw new IllegalArgumentException("Use an empty string for a missing output value");
                    }
                    if (value.contains(";") || value.contains("\"")
                            || value.contains("\n") || value.contains("\r")) {
                        result.append('"').append(value.replace("\"", "\"\"")).append('"');
                    } else {
                        result.append(value);
                    }
                }
                result.append('\n');
            }
            return result.toString();
        }
    }

    /** A small chart helper; the learner must calculate the counts first. */
    static class Charts {
        static void writeBarChart(String filename, String title,
                                  Map<String, Integer> counts) throws IOException {
            if (counts.isEmpty()) {
                throw new IllegalArgumentException("A chart needs at least one category");
            }
            int maximum = 1;
            for (Map.Entry<String, Integer> item : counts.entrySet()) {
                if (item.getKey() == null || item.getValue() == null || item.getValue() < 0) {
                    throw new IllegalArgumentException("Chart labels and nonnegative counts are required");
                }
                maximum = Math.max(maximum, item.getValue());
            }
            StringBuilder html = new StringBuilder("<!doctype html><html lang=\"en\"><meta charset=\"utf-8\">");
            html.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>")
                .append(escape(title)).append("</title><style>")
                .append("body{font:17px system-ui,sans-serif;max-width:850px;margin:40px auto;padding:0 24px;color:#173140;background:#f7f9fb}")
                .append("h1{font-size:28px}.row{display:grid;grid-template-columns:170px 1fr 70px;gap:12px;align-items:center;margin:18px 0}")
                .append(".bar{height:24px;background:#dce8ef;border-radius:3px;overflow:hidden}.fill{height:100%;background:#147d92}")
                .append(".count{text-align:right}.label{overflow-wrap:anywhere}@media(max-width:550px){.row{grid-template-columns:100px 1fr 45px;font-size:14px}}")
                .append("</style><main><h1>").append(escape(title)).append("</h1>")
                .append("<p>Synthetic Aktenblick data. Each bar starts at zero; the largest count sets the scale.</p>");
            for (Map.Entry<String, Integer> item : counts.entrySet()) {
                double width = 100.0 * item.getValue() / maximum;
                html.append("<div class=\"row\"><span class=\"label\">")
                    .append(escape(item.getKey())).append("</span><div class=\"bar\" aria-hidden=\"true\">")
                    .append("<div class=\"fill\" style=\"width:").append(width)
                    .append("%\"></div></div><span class=\"count\">")
                    .append(item.getValue()).append("</span></div>");
            }
            html.append("<p>Counts supplied by the learner. State filters and denominator in your report.</p></main></html>");
            Files.writeString(outputPath(filename), html.toString(), StandardCharsets.UTF_8);
        }

        static String escape(String value) {
            if (value == null) throw new IllegalArgumentException("Text must not be null");
            return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
        }
    }

    static Path outputPath(String filename) throws IOException {
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\")
                || filename.equals(".") || filename.equals("..") || Path.of(filename).isAbsolute()) {
            throw new IllegalArgumentException("Use a simple output filename, e.g. profile.csv");
        }
        Path directory = Path.of("outputs");
        if (Files.isSymbolicLink(directory)) {
            throw new IOException("outputs/ must be a normal directory, not a symbolic link");
        }
        Files.createDirectories(directory);
        Path result = directory.resolve(filename);
        if (Files.isSymbolicLink(result)) {
            throw new IOException("Output must be a normal file, not a symbolic link");
        }
        return result;
    }

    static void supportSelfTest() throws IOException {
        String fixture = "\uFEFFid;title;note\r\n"
            + "1;\"Prüfung; ÄÖÜ\";\"Zeile 1\nZeile \"\"2\"\"\"\r\n"
            + "2;Nachtrag;\r\n";
        List<List<String>> parsed = Csv.parse(fixture);
        check(parsed.size() == 3, "logical CSV records, including header");
        check(parsed.get(0).get(0).equals("id"), "BOM removal");
        check(parsed.get(1).get(1).equals("Prüfung; ÄÖÜ"), "quoted delimiter and Unicode");
        check(parsed.get(1).get(2).equals("Zeile 1\nZeile \"2\""), "multiline and escaped quotes");
        check(parsed.get(2).get(2).isEmpty(), "trailing empty field");
        check(Csv.parse("a;b").get(0).size() == 2, "last record without newline");
        check(Csv.parse(Csv.encode(parsed)).equals(parsed), "CSV write/read round trip");
        expectInvalid(() -> Csv.parse("a;\"unfinished"), "reject unclosed quote");
        expectInvalid(() -> Csv.parse("a;\"b\"junk"), "reject text after closing quote");
        expectInvalid(() -> Csv.parse("a;b\"c"), "reject quote inside bare field");
        check(Charts.escape("<script>&\"'").equals("&lt;script&gt;&amp;&quot;&#39;"), "HTML escaping");
        expectInvalid(() -> outputPath("../data/raw/documents.csv"), "reject parent traversal");
        expectInvalid(() -> outputPath("data\\raw.csv"), "reject Windows separator");

        Path temporary = Files.createTempFile("aktenblick-support-", ".csv");
        try {
            Files.writeString(temporary, "a;a\n1;2\n", StandardCharsets.UTF_8);
            expectInvalid(() -> Csv.read(temporary), "reject duplicate headers");
            Files.writeString(temporary, "a;b\n1\n", StandardCharsets.UTF_8);
            expectInvalid(() -> Csv.read(temporary), "reject uneven row widths");
            Files.writeString(temporary, fixture, StandardCharsets.UTF_8);
            check(Csv.read(temporary).get(1).get("title").equals("Nachtrag"), "UTF-8 file reader");
        } finally {
            Files.deleteIfExists(temporary);
        }
        String token = Long.toUnsignedString(System.nanoTime());
        String csvName = "_support-check-" + token + ".csv";
        String htmlName = "_support-check-" + token + ".html";
        Path csvOutput = outputPath(csvName);
        Path htmlOutput = outputPath(htmlName);
        try {
            Csv.writeReport(csvName, List.of("label", "count"),
                List.of(List.of("Prüfung; \"2\"", "10")));
            check(Csv.read(csvOutput).get(0).get("label").equals("Prüfung; \"2\""), "report file round trip");
            Map<String, Integer> counts = new LinkedHashMap<>();
            counts.put("<script> & Prüfung", 2);
            counts.put("No documents", 0);
            Charts.writeBarChart(htmlName, "Support check", counts);
            String chart = Files.readString(htmlOutput, StandardCharsets.UTF_8);
            check(chart.contains("&lt;script&gt; &amp; Prüfung") && !chart.contains("<script>"), "escaped chart file");
            check(chart.contains("width:100.0%") && chart.contains("width:0.0%"), "zero-based bar widths");
        } finally {
            Files.deleteIfExists(csvOutput);
            Files.deleteIfExists(htmlOutput);
        }
        System.out.println("PASS: supplied CSV/export/chart support checks.");
        System.out.println("These checks do not validate or complete T02-T11.");
    }

    interface CheckedAction { void run() throws IOException; }

    static void expectInvalid(CheckedAction action, String label) throws IOException {
        try {
            action.run();
        } catch (IllegalArgumentException | IOException expected) {
            return;
        }
        throw new IllegalArgumentException("Support self-test failed: " + label);
    }

    static void check(boolean condition, String label) {
        if (!condition) throw new IllegalArgumentException("Support self-test failed: " + label);
    }
}
