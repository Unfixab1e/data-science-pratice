import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maintainer integrity check, not a learner solution or task grader.
 * Reads mentor/data/ explicitly. Learners should use Practice --self-test instead.
 * Run from the repository root: java tools/VerifyPack.java
 * Optional argument: another pack root, e.g. a newly generated fixture folder.
 * Explicit fixture-only roots may omit data/llm/; the default checkout must have it.
 */
public class VerifyPack {
    static final LocalDate SNAPSHOT = LocalDate.of(2026, 11, 20);
    static final List<String> DOCUMENT_COLUMNS = List.of("ingest_id", "document_id", "matter_id",
        "title", "document_type", "department", "received_date", "document_date", "status",
        "page_count", "ocr_confidence", "source", "reviewed_date", "reviewer_id",
        "confidentiality", "language", "review_due_date");

    public static void main(String[] args) throws Exception {
        if (args.length > 1) throw new IllegalArgumentException("Usage: java tools/VerifyPack.java [pack-root]");
        Path root = Path.of(args.length == 0 ? "." : args[0]).toAbsolutePath().normalize();
        List<Map<String, String>> docs = read(root.resolve("data/raw/documents.csv"));
        List<Map<String, String>> sample = read(root.resolve("data/samples/documents_12.csv"));
        List<Map<String, String>> texts = read(root.resolve("data/raw/document_texts.csv"));
        List<Map<String, String>> matters = read(root.resolve("data/raw/matters.csv"));
        List<Map<String, String>> reviewers = read(root.resolve("data/raw/reviewers.csv"));
        List<Map<String, String>> anomalies = read(root.resolve("mentor/data/anomalies.csv"));
        List<Map<String, String>> expected = read(root.resolve("mentor/data/expected_metrics.csv"));
        check(docs.size() == 128, "128 metadata rows");
        check(new ArrayList<>(docs.get(0).keySet()).equals(DOCUMENT_COLUMNS), "metadata schema/order");
        check(sample.size() == 12 && docs.subList(0, 12).equals(sample), "sample preserves first 12 rows");
        check(texts.size() == 120 && matters.size() == 12 && reviewers.size() == 6, "reference/text sizes");
        Set<String> matterIds = uniqueIds(matters, "matter_id");
        Set<String> reviewerIds = uniqueIds(reviewers, "reviewer_id");
        Set<String> textIds = uniqueIds(texts, "document_id");
        uniqueIds(docs, "ingest_id");
        Map<String, Map<String, String>> byIngest = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> groups = new LinkedHashMap<>();
        for (Map<String, String> row : docs) {
            byIngest.put(row.get("ingest_id"), row);
            groups.computeIfAbsent(row.get("document_id").trim(), key -> new ArrayList<>()).add(row);
        }
        check(groups.size() == 120 && groups.keySet().equals(textIds), "one text per logical document");
        for (Map<String, String> text : texts) {
            check(text.get("text").contains("SYNTHETISCHER ÜBUNGSTEXT"), "synthetic text marking");
            check(text.get("text").contains(text.get("document_id")), "text identifies its document");
        }

        int exact = 0;
        int conflicts = 0;
        for (List<Map<String, String>> group : groups.values()) {
            check(group.size() <= 2, "fixture has pairs, not larger duplicate groups");
            if (group.size() == 2) {
                Map<String, String> left = new LinkedHashMap<>(group.get(0));
                Map<String, String> right = new LinkedHashMap<>(group.get(1));
                left.remove("ingest_id");
                right.remove("ingest_id");
                if (left.equals(right)) exact++; else conflicts++;
            }
        }
        check(exact == 5 && conflicts == 3, "five exact pairs and three conflicting pairs");

        Set<String> inactive = new HashSet<>();
        for (Map<String, String> reviewer : reviewers) {
            check(Set.of("true", "false").contains(reviewer.get("active")), "reviewer active boolean");
            if (reviewer.get("active").equals("false")) inactive.add(reviewer.get("reviewer_id"));
        }
        int unknownMatters = 0, unknownReviewers = 0, inactiveAssignments = 0;
        int missingTitles = 0, missingReceived = 0, invalidPages = 0, impossibleDates = 0;
        int unsupportedLanguage = 0, futureReceived = 0;
        for (Map<String, String> row : docs) {
            String matter = row.get("matter_id").trim();
            String reviewer = row.get("reviewer_id").trim();
            if (!matter.isEmpty() && !matterIds.contains(matter)) unknownMatters++;
            if (!reviewer.isEmpty() && !reviewerIds.contains(reviewer)) unknownReviewers++;
            if (inactive.contains(reviewer)) inactiveAssignments++;
            if (row.get("title").isBlank()) missingTitles++;
            if (row.get("received_date").isBlank()) missingReceived++;
            if (!row.get("language").equals("de")) unsupportedLanguage++;
            try {
                if (Integer.parseInt(row.get("page_count").trim()) < 1) invalidPages++;
            } catch (NumberFormatException error) { invalidPages++; }
            for (String field : List.of("received_date", "document_date", "reviewed_date", "review_due_date")) {
                if (row.get(field).isBlank()) continue;
                try {
                    LocalDate date = strictDate(row.get(field));
                    if (field.equals("received_date") && date.isAfter(SNAPSHOT)) futureReceived++;
                } catch (DateTimeParseException error) { impossibleDates++; }
            }
        }
        for (Map<String, String> anomaly : anomalies) {
            Map<String, String> row = byIngest.get(anomaly.get("ingest_id"));
            check(row != null, "anomaly ingestion exists");
            check(row.get("document_id").trim().equals(anomaly.get("document_id")), "anomaly document matches");
            if (!anomaly.get("field").equals("*")) {
                check(anomaly.get("after").equals(row.get(anomaly.get("field"))), "anomaly after-value matches raw data");
            }
        }

        Map<String, String> actual = new LinkedHashMap<>();
        actual.put("snapshot_date", SNAPSHOT.toString());
        metric(actual, "raw_document_rows", docs.size());
        metric(actual, "distinct_document_ids_after_trim", groups.size());
        metric(actual, "sample_document_rows", sample.size());
        metric(actual, "matter_rows", matters.size());
        metric(actual, "reviewer_rows", reviewers.size());
        metric(actual, "document_text_rows", texts.size());
        metric(actual, "duplicate_business_ids", exact + conflicts);
        metric(actual, "exact_duplicate_groups", exact);
        metric(actual, "exact_duplicate_excess_rows", exact);
        metric(actual, "conflicting_duplicate_groups", conflicts);
        metric(actual, "conflicting_duplicate_rows", conflicts * 2);
        metric(actual, "rows_after_exact_dedup_only", docs.size() - exact);
        metric(actual, "nonconflicting_unique_candidate_documents", docs.size() - exact - conflicts * 2);
        metric(actual, "missing_title_rows", missingTitles);
        metric(actual, "missing_received_date_rows", missingReceived);
        metric(actual, "invalid_page_count_rows", invalidPages);
        metric(actual, "impossible_calendar_date_cells", impossibleDates);
        metric(actual, "unknown_matter_reference_rows", unknownMatters);
        metric(actual, "unknown_reviewer_reference_rows", unknownReviewers);
        metric(actual, "inactive_reviewer_assignment_rows", inactiveAssignments);
        metric(actual, "unsupported_language_rows", unsupportedLanguage);
        metric(actual, "future_received_date_rows", futureReceived);
        metric(actual, "injection_log_records", anomalies.size());
        int checkedMetrics = 0;
        for (Map<String, String> expectedRow : expected) {
            String name = expectedRow.get("metric");
            if (actual.containsKey(name)) {
                check(actual.get(name).equals(expectedRow.get("value")), "metric " + name);
                checkedMetrics++;
            }
        }
        check(checkedMetrics == actual.size(), "every independently derived metric exists in mentor file");

        String manifest = Files.readString(root.resolve("data/manifest.json"), StandardCharsets.UTF_8);
        Matcher hashes = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([0-9a-f]{64})\"").matcher(manifest);
        int checkedHashes = 0;
        Set<String> hashedPaths = new HashSet<>();
        while (hashes.find()) {
            String name = hashes.group(1);
            Path file = root.resolve(name).normalize();
            check(file.startsWith(root), "manifest paths stay inside pack root");
            String actualHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
            check(actualHash.equals(hashes.group(2)), "SHA-256 " + name);
            check(hashedPaths.add(name), "no duplicate hash path");
            checkedHashes++;
        }
        check(hashedPaths.containsAll(List.of("data/raw/documents.csv", "data/raw/document_texts.csv",
            "data/raw/matters.csv", "data/raw/reviewers.csv", "data/samples/documents_12.csv")), "all main datasets hashed");
        List<Map<String, String>> edge = read(root.resolve("data/samples/csv_edge_cases.csv"));
        check(edge.size() == 5, "edge-case fixture size");
        check(edge.get(1).get("note").contains(";"), "quoted semicolon fixture");
        check(edge.get(2).get("note").contains("\""), "escaped quotation fixture");
        check(edge.get(3).get("note").contains("\n"), "multiline fixture");
        check(edge.get(4).get("value").isEmpty(), "trailing empty fixture value");

        System.out.println("PASS: fixture schemas, row/text relationships, duplicates, anomaly ledger and edge cases.");
        System.out.println("PASS: " + checkedMetrics + " independently derived mentor metrics; " + checkedHashes + " file hashes.");
        if (args.length == 0 || Files.exists(root.resolve("data/llm"))) {
            verifyLlmFixtures(root, docs, texts, matterIds);
        } else {
            System.out.println("SKIP: optional LLM fixtures absent from explicitly selected fixture-only root.");
        }
        System.out.println("Intentional data-quality problems remain. This check is not a learner-assignment grade.");
    }

    /** Checks fixture integrity, not the performance of any language model. */
    static void verifyLlmFixtures(Path root, List<Map<String, String>> docs,
                                  List<Map<String, String>> texts, Set<String> matterIds) throws IOException {
        List<Map<String, String>> questions = read(root.resolve("data/llm/questions.csv"));
        List<Map<String, String>> answers = read(root.resolve("data/llm/mock_answers.csv"));
        List<Map<String, String>> key = read(root.resolve("mentor/llm_review_key.csv"));
        List<Map<String, String>> context = read(root.resolve("data/llm/untrusted_context.csv"));
        Set<String> expectedIds = Set.of("Q01", "Q02", "Q03", "Q04", "Q05", "Q06", "Q07", "Q08");
        check(questions.size() == 8 && uniqueIds(questions, "question_id").equals(expectedIds), "eight unique question IDs");
        check(answers.size() == 8 && uniqueIds(answers, "question_id").equals(expectedIds), "mock-answer completeness");
        check(key.size() == 8 && uniqueIds(key, "question_id").equals(expectedIds), "mentor-key completeness");
        check(new ArrayList<>(questions.get(0).keySet()).equals(List.of("question_id", "split", "matter_id",
            "question", "expected_behavior", "reference_document_id", "reference_excerpt")), "question schema");
        check(new ArrayList<>(answers.get(0).keySet()).equals(List.of("question_id", "answer", "claimed_document_ids")), "mock-answer schema");
        check(new ArrayList<>(key.get(0).keySet()).equals(List.of("question_id", "supported", "citation_resolves",
            "citation_supports_claim", "appropriate_abstention", "scenario", "reason")), "mentor-key schema");
        check(context.size() == 1 && new ArrayList<>(context.get(0).keySet()).equals(
            List.of("question_id", "source_document_id", "untrusted_text")), "untrusted-context schema");

        Map<String, String> corpus = new LinkedHashMap<>();
        for (Map<String, String> row : texts) corpus.put(row.get("document_id"), row.get("text"));
        Map<String, String> documentMatters = new LinkedHashMap<>();
        for (Map<String, String> row : docs) documentMatters.putIfAbsent(row.get("document_id").trim(), row.get("matter_id").trim());
        Map<String, Map<String, String>> questionsById = index(questions, "question_id");
        Map<String, Map<String, String>> answersById = index(answers, "question_id");
        Map<String, Map<String, String>> keyById = index(key, "question_id");
        Set<String> developmentMatters = new HashSet<>();
        Set<String> evaluationMatters = new HashSet<>();
        int developmentCount = 0, evaluationCount = 0;
        for (Map<String, String> question : questions) {
            String id = question.get("question_id");
            String split = question.get("split");
            String matter = question.get("matter_id");
            String reference = question.get("reference_document_id");
            check(!question.get("question").isBlank(), "nonempty question " + id);
            check(Set.of("answer", "abstain").contains(question.get("expected_behavior")), "expected behavior " + id);
            check(matterIds.contains(matter) && matter.equals(documentMatters.get(reference)), "question/document/matter join " + id);
            check(corpus.containsKey(reference) && !question.get("reference_excerpt").isBlank()
                && corpus.get(reference).contains(question.get("reference_excerpt")), "exact evidence excerpt " + id);
            if (split.equals("development")) { developmentMatters.add(matter); developmentCount++; }
            else if (split.equals("evaluation")) { evaluationMatters.add(matter); evaluationCount++; }
            else throw new IllegalStateException("Unknown LLM fixture split: " + split);

            Map<String, String> answer = answersById.get(id);
            Map<String, String> review = keyById.get(id);
            check(!answer.get("answer").isBlank(), "nonempty mock answer " + id);
            for (String flag : List.of("supported", "citation_resolves", "citation_supports_claim")) {
                check(Set.of("yes", "no").contains(review.get(flag)), "review flag " + flag + " " + id);
            }
            String citations = answer.get("claimed_document_ids");
            boolean citationResolves = !citations.isBlank();
            for (String citation : citations.split(",", -1)) {
                if (!corpus.containsKey(citation.trim())) citationResolves = false;
            }
            check(review.get("citation_resolves").equals(citationResolves ? "yes" : "no"), "actual citation existence " + id);
            if (review.get("citation_supports_claim").equals("yes")) {
                check(citationResolves && review.get("supported").equals("yes"), "support requires resolving evidence " + id);
            }
            check(!review.get("reason").isBlank() && !review.get("scenario").isBlank(), "mentor explanation " + id);
            if (question.get("expected_behavior").equals("answer")) {
                check(review.get("appropriate_abstention").equals("not_applicable"), "abstention not applicable " + id);
            } else {
                check(Set.of("yes", "no").contains(review.get("appropriate_abstention")), "abstention decision " + id);
            }
        }
        check(developmentCount == 4 && evaluationCount == 4, "four development and four evaluation cases");
        check(developmentMatters.equals(Set.of("MAT-001", "MAT-002"))
            && evaluationMatters.equals(Set.of("MAT-009", "MAT-010")), "fixed matter-group split");
        Set<String> overlap = new HashSet<>(developmentMatters);
        overlap.retainAll(evaluationMatters);
        check(overlap.isEmpty(), "no matter-group overlap across development and evaluation");

        check(answersById.get("Q03").get("claimed_document_ids").isBlank(), "intentional missing citation Q03");
        check(questionsById.get("Q06").get("reference_document_id").equals("DOC-0085")
            && answersById.get("Q06").get("claimed_document_ids").equals("DOC-0086")
            && corpus.containsKey("DOC-0086") && !corpus.get("DOC-0086").contains("3.995,00")
            && keyById.get("Q06").get("scenario").equals("wrong_existing_citation"), "intentional wrong existing citation Q06");
        check(answersById.get("Q07").get("claimed_document_ids").equals("DOC-9999")
            && !corpus.containsKey("DOC-9999") && !documentMatters.containsKey("DOC-9999")
            && answersById.get("Q07").get("answer").contains("anfrage95@kontakt.example")
            && keyById.get("Q07").get("scenario").equals("nonexistent_citation"), "intentional nonexistent citation Q07");
        Map<String, String> injected = context.get(0);
        check(injected.get("question_id").equals("Q08") && injected.get("source_document_id").equals("DOC-0096")
            && corpus.containsKey(injected.get("source_document_id")) && injected.get("untrusted_text").contains("Ignoriere")
            && !corpus.get("DOC-0096").contains(injected.get("untrusted_text"))
            && keyById.get("Q08").get("scenario").equals("followed_untrusted_instruction"), "separate untrusted context Q08");
        System.out.println("PASS: 8 hand-written LLM-evaluation cases, exact excerpts, matter splits and review-key consistency.");
        System.out.println("No live model was called or measured. Wrong/missing citations are intentional review cases.");
    }

    static Map<String, Map<String, String>> index(List<Map<String, String>> rows, String column) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        for (Map<String, String> row : rows) result.put(row.get(column), row);
        return result;
    }

    static void metric(Map<String, String> values, String name, int value) { values.put(name, Integer.toString(value)); }
    static void check(boolean valid, String label) {
        if (!valid) throw new IllegalStateException("Fixture verification failed: " + label);
    }
    static LocalDate strictDate(String text) {
        String value = text.trim();
        String pattern = value.contains(".") ? "dd.MM.uuuu" : "uuuu-MM-dd";
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT));
    }
    static Set<String> uniqueIds(List<Map<String, String>> rows, String column) {
        Set<String> ids = new HashSet<>();
        for (Map<String, String> row : rows) check(ids.add(row.get(column)), "unique " + column);
        return ids;
    }
    static List<Map<String, String>> read(Path file) throws IOException {
        List<List<String>> table = parse(Files.readString(file, StandardCharsets.UTF_8));
        check(!table.isEmpty(), "nonempty " + file);
        List<String> header = table.get(0);
        check(new HashSet<>(header).size() == header.size(), "unique column names " + file);
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < table.size(); i++) {
            check(table.get(i).size() == header.size(), "row width " + file + " record " + (i + 1));
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < header.size(); j++) row.put(header.get(j), table.get(i).get(j));
            rows.add(row);
        }
        return rows;
    }
    static List<List<String>> parse(String source) {
        String text = source.startsWith("\uFEFF") ? source.substring(1) : source;
        List<List<String>> table = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false, closed = false, started = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') { value.append('"'); i++; }
                    else { quoted = false; closed = true; }
                } else value.append(c);
            } else if (c == ';') {
                row.add(value.toString()); value.setLength(0); closed = false; started = true;
            } else if (c == '\n' || c == '\r') {
                row.add(value.toString()); table.add(row); row = new ArrayList<>();
                value.setLength(0); closed = false; started = false;
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
            } else if (c == '"' && value.length() == 0 && !closed) { quoted = true; started = true; }
            else {
                check(!closed && c != '"', "CSV quote syntax");
                value.append(c); started = true;
            }
        }
        check(!quoted, "closed CSV quotes");
        if (started || !row.isEmpty() || value.length() > 0 || closed) { row.add(value.toString()); table.add(row); }
        return table;
    }
}
