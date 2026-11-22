import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Maintainer fixture factory, not a learner solution. JDK 21 is recommended and tested. */
public class GenerateDataset {
    static final LocalDate SNAPSHOT = LocalDate.of(2026, 11, 20);
    static final String[] HEADER = {"ingest_id", "document_id", "matter_id", "title", "document_type",
        "department", "received_date", "document_date", "status", "page_count", "ocr_confidence", "source",
        "reviewed_date", "reviewer_id", "confidentiality", "language", "review_due_date"};
    static final String[] TYPES = {"Vertrag", "Nachtrag", "Kündigung", "Rechnung", "Datenschutzanfrage", "Schriftverkehr"};
    static final String[] DEPTS = {"Einkauf", "Personal", "Vertrieb", "Datenschutz"};
    static final String[] SOURCES = {"E-Mail", "Scan", "Upload"};
    static final String[] STATUSES = {"Neu", "In Prüfung", "Freigegeben", "Rückfrage"};
    static final DateTimeFormatter DE_DATE = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT);
    static final List<Map<String, String>> rows = new ArrayList<>();
    static final List<String[]> anomalies = new ArrayList<>();
    static final List<String[]> texts = new ArrayList<>();
    static final List<Path> generated = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java tools/GenerateDataset.java <NEW-EMPTY-OUTPUT-DIRECTORY>");
            System.err.println("Creates fixtures below that directory. Never point this at an existing checkout.");
            System.exit(2);
        }
        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        if (Files.exists(root)) {
            try (var children = Files.list(root)) {
                if (children.findAny().isPresent()) throw new IllegalArgumentException("Output directory must be empty: " + root);
            }
        }
        Files.createDirectories(root);
        buildCanonicalRows();
        injectProblems();
        addDuplicates();
        writeCsv(root, "data/raw/documents.csv", HEADER, rows.stream().map(GenerateDataset::values).toList());
        writeCsv(root, "data/samples/documents_12.csv", HEADER, rows.subList(0, 12).stream().map(GenerateDataset::values).toList());
        writeCsv(root, "data/raw/document_texts.csv", new String[]{"document_id", "text"}, texts);
        writeReferenceTables(root);
        writeCsv(root, "mentor/data/anomalies.csv", new String[]{"ingest_id", "document_id", "field", "before", "after", "issue_kind", "reason"}, anomalies);
        writeMetrics(root);
        for (int i = 0; i < 6; i++) {
            writeText(root, "data/samples/text/" + texts.get(i)[0] + ".txt", texts.get(i)[1] + "\n");
        }
        writeCsv(root, "data/samples/csv_edge_cases.csv", new String[]{"id", "note", "value"}, List.of(
            new String[]{"1", "Umlaut: Prüfung", "0,82"},
            new String[]{"2", "A semicolon; inside one field", "92%"},
            new String[]{"3", "A \"quoted\" word", ""},
            new String[]{"4", "First line\nSecond line", "7"},
            new String[]{"5", "Last field is deliberately empty", ""}));
        writeManifest(root);
        verifyInMemory();
        System.out.println("Created deterministic Aktenblick fixtures in " + root);
        System.out.println("128 metadata rows; 120 document IDs; 120 texts; 12 matters; 6 reviewers.");
        System.out.println("Five exact duplicate pairs and three conflicting pairs; " + anomalies.size() + " documented injections.");
    }

    static void buildCanonicalRows() {
        for (int i = 1; i <= 120; i++) {
            int matter = (i - 1) / 10 + 1;
            String department = DEPTS[(matter - 1) % DEPTS.length];
            String type = TYPES[(i - 1) % TYPES.length];
            LocalDate received = LocalDate.of(2026, 9, 1).plusDays((i * 7L) % 81);
            LocalDate documentDate = received.minusDays(1 + i % 9);
            String source = SOURCES[(i * 7) % SOURCES.length];
            String status = STATUSES[(i * 3 + matter) % STATUSES.length];
            if (i == 19 || i == 31) status = "Freigegeben";
            if (i == 36) status = "In Prüfung";
            if (i == 106) status = "In Prüfung";
            if (i == 4) source = "E-Mail";
            if (i == 30) source = "Upload";
            if (i == 20 || i == 34) source = "Scan";
            String ocr = source.equals("Scan") ? String.format(Locale.ROOT, "%.2f", (70 + i % 29) / 100.0) : "";
            if (i == 6 || i == 7) { source = "Scan"; ocr = i == 6 ? "0.82" : "0.92"; }
            LocalDate review = received.plusDays(1 + i % 7);
            if (review.isAfter(SNAPSHOT)) review = SNAPSHOT;
            String reviewer = status.equals("Neu") ? "" : String.format(Locale.ROOT, "REV-%03d", 1 + (i % 5));
            String title = switch (type) {
                case "Vertrag" -> "Servicevereinbarung Partner " + String.format(Locale.ROOT, "%02d", matter);
                case "Nachtrag" -> "Ergänzung Standort " + (i % 4 + 1);
                case "Kündigung" -> "Beendigung einer Servicevereinbarung";
                case "Rechnung" -> "Leistungsabrechnung " + String.format(Locale.ROOT, "RE-2026-%04d", i);
                case "Datenschutzanfrage" -> "Anfrage zu gespeicherten Kontaktdaten";
                default -> "Rückfrage zur Unterlagenlieferung " + i;
            };
            if (i == 40) title = "Rechnung für \"Standort Süd\"; Zusatzleistung";
            if (i == 41) title = "Abstimmung\nStandort Nord";
            String confidentiality = i % 9 == 0 ? "Streng vertraulich" : i % 3 == 0 ? "Vertraulich" : "Intern";
            String[] vals = {String.format(Locale.ROOT, "ROW-%04d", i), doc(i), String.format(Locale.ROOT, "MAT-%03d", matter),
                title, type, department, received.toString(), documentDate.toString(), status, Integer.toString(1 + i % 18),
                ocr, source, status.equals("Freigegeben") ? review.toString() : "", reviewer, confidentiality, "de", received.plusDays(10).toString()};
            Map<String, String> row = new LinkedHashMap<>();
            for (int c = 0; c < HEADER.length; c++) row.put(HEADER[c], vals[c]);
            rows.add(row);
            texts.add(new String[]{doc(i), makeText(i, matter, type, documentDate)});
        }
    }

    static void injectProblems() {
        inject(2, "department", "  " + rows.get(1).get("department") + " ", "format", "Leading and trailing spaces; trim edges only.");
        inject(3, "status", rows.get(2).get("status").toLowerCase(Locale.GERMAN), "alias", "Lowercase spelling; use the documented status mapping.");
        inject(4, "source", "email", "alias", "Alias for E-Mail.");
        inject(5, "received_date", germanDate(5, "received_date"), "format", "Valid German date notation.");
        inject(6, "ocr_confidence", "0,82", "format", "Decimal comma; normalize to 0.82.");
        inject(7, "ocr_confidence", "92%", "format", "Percentage; normalize to 0.92.");
        inject(8, "title", "", "missing", "Title missing; do not invent one in the cleaning exercise.");
        inject(9, "page_count", "0", "invalid_number", "Zero pages is invalid for an ingested document.");
        inject(10, "received_date", "31.02.2026", "invalid_date", "Impossible calendar date; strict parsing must reject it.");
        inject(11, "reviewer_id", "REV-999", "unknown_reference", "Reviewer absent from reviewer reference export.");
        inject(12, "matter_id", "MAT-999", "unknown_reference", "Matter absent from matter reference export.");
        inject(15, "document_type", "Kuendigung", "alias", "ASCII spelling of Kündigung.");
        inject(16, "status", " " + rows.get(15).get("status") + "  ", "format", "Leading and trailing spaces.");
        inject(17, "received_date", "", "missing", "Receipt date unavailable; do not derive it from another field.");
        inject(18, "document_date", LocalDate.parse(rows.get(17).get("received_date")).plusDays(2).toString(), "chronology", "Document date after receipt requires review, not automatic correction.");
        inject(19, "reviewed_date", LocalDate.parse(rows.get(18).get("received_date")).minusDays(1).toString(), "chronology", "Review recorded before receipt.");
        inject(20, "ocr_confidence", "1.20", "invalid_number", "OCR confidence outside the allowed 0..1 range.");
        inject(21, "page_count", "-3", "invalid_number", "Negative page count.");
        inject(22, "page_count", "fünf", "invalid_number", "Page count supplied as text; no undocumented inference.");
        inject(23, "language", "xx", "unknown_category", "Unsupported language code.");
        inject(24, "confidentiality", "", "missing", "Classification missing; do not default to less restrictive access.");
        inject(25, "document_date", "2026-04-31", "invalid_date", "April has no day 31.");
        inject(26, "review_due_date", LocalDate.parse(rows.get(25).get("received_date")).minusDays(2).toString(), "chronology", "Internal operational review target predates receipt.");
        inject(27, "department", rows.get(26).get("department").toLowerCase(Locale.GERMAN), "alias", "Lowercase department spelling.");
        inject(29, "document_type", "Datenschutz-Anfrage", "alias", "Documented alias for Datenschutzanfrage.");
        inject(30, "source", "UPLOAD", "alias", "Uppercase spelling of Upload.");
        inject(31, "reviewed_date", "", "missing", "Freigegeben status without a review date.");
        inject(32, "received_date", "2026-12-01", "snapshot_date", "Receipt date is after the fixed snapshot.");
        inject(32, "document_date", "2026-11-30", "snapshot_date", "Document date is after the fixed snapshot.");
        inject(32, "review_due_date", "2026-12-11", "paired_fixture_change", "Moved with future receipt to keep due-date ordering consistent; a future due date alone is allowed.");
        inject(33, "department", "Recht", "unknown_category", "Department not present in the agreed controlled vocabulary.");
        inject(34, "ocr_confidence", "", "missing", "A scan has no OCR confidence; blanks for E-Mail/Upload are otherwise allowed.");
        inject(36, "reviewer_id", "", "missing", "In Prüfung without an assigned reviewer.");
        inject(37, "document_id", " " + doc(37) + " ", "format", "Trim before joining IDs; do not alter the original file.");
        inject(38, "matter_id", " " + rows.get(37).get("matter_id") + " ", "format", "Trim the foreign key before joining.");
        inject(39, "received_date", " " + rows.get(38).get("received_date") + " ", "format", "Trim dates before parsing.");
        inject(42, "reviewer_id", "REV-006", "inactive_reference", "Existing reviewer is inactive; flag for reassignment, not an orphan.");
    }

    static String germanDate(int number, String field) {
        return LocalDate.parse(rows.get(number - 1).get(field)).format(DE_DATE);
    }

    static void inject(int number, String field, String value, String kind, String reason) {
        Map<String, String> row = rows.get(number - 1);
        String before = row.put(field, value);
        anomalies.add(new String[]{row.get("ingest_id"), doc(number), field, before, value, kind, reason});
    }

    static void addDuplicates() {
        int[] ids = {51, 62, 73, 84, 95, 106, 107, 108};
        for (int j = 0; j < ids.length; j++) {
            Map<String, String> original = rows.get(ids[j] - 1);
            Map<String, String> copy = new LinkedHashMap<>(original);
            copy.put("ingest_id", String.format(Locale.ROOT, "ROW-%04d", 121 + j));
            if (j < 5) {
                anomalies.add(new String[]{copy.get("ingest_id"), doc(ids[j]), "*", original.get("ingest_id"), copy.get("ingest_id"),
                    "exact_duplicate", "All business fields identical; ingest_id differs because the row was imported again."});
            } else {
                String field = j == 5 ? "status" : j == 6 ? "page_count" : "confidentiality";
                String before = copy.get(field);
                String after = j == 5 ? "Rückfrage" : j == 6 ? Integer.toString(Integer.parseInt(before) + 1) : "Streng vertraulich";
                if (after.equals(before)) after = "Intern";
                copy.put(field, after);
                anomalies.add(new String[]{copy.get("ingest_id"), doc(ids[j]), field, before, after,
                    "conflicting_duplicate", "Two rows share a document ID but disagree. Withhold the whole group for review; do not choose the later row."});
            }
            rows.add(copy);
        }
    }

    static void writeReferenceTables(Path root) throws IOException {
        List<String[]> matters = new ArrayList<>();
        String[] purposes = {"Standortservice", "Unterlagen Personal", "Vertriebspartner", "Kontaktanfragen"};
        for (int m = 1; m <= 12; m++) {
            matters.add(new String[]{String.format(Locale.ROOT, "MAT-%03d", m), purposes[(m - 1) % 4] + " / Partner " + String.format(Locale.ROOT, "%02d", m),
                DEPTS[(m - 1) % 4], LocalDate.of(2026, 8, 1).plusDays(m).toString(), m % 5 == 0 ? "Abgeschlossen" : "Offen"});
        }
        writeCsv(root, "data/raw/matters.csv", new String[]{"matter_id", "matter_name", "department", "opened_date", "matter_status"}, matters);
        writeCsv(root, "data/raw/reviewers.csv", new String[]{"reviewer_id", "display_name", "department", "active"}, List.of(
            new String[]{"REV-001", "Lena Beispiel", "Einkauf", "true"},
            new String[]{"REV-002", "Jonas Muster", "Personal", "true"},
            new String[]{"REV-003", "Mira Test", "Vertrieb", "true"},
            new String[]{"REV-004", "Tobias Demo", "Datenschutz", "true"},
            new String[]{"REV-005", "Sofia Beispiel", "Einkauf", "true"},
            new String[]{"REV-006", "Emil Muster", "Personal", "false"}));
    }

    static void writeMetrics(Path root) throws IOException {
        List<String[]> metrics = new ArrayList<>();
        metric(metrics, "snapshot_date", SNAPSHOT.toString(), "Fixed reporting cutoff; never replace with the current date during this exercise.");
        metric(metrics, "raw_document_rows", rows.size(), "CSV data records, excluding header; physical line count is not record count.");
        metric(metrics, "distinct_document_ids_after_trim", rows.stream().map(r -> r.get("document_id").trim()).distinct().count(), "Business IDs; trim outer spaces first.");
        metric(metrics, "sample_document_rows", 12, "First twelve raw metadata records, byte-equivalent field values.");
        metric(metrics, "matter_rows", 12, "Reference export; all unique MAT IDs.");
        metric(metrics, "reviewer_rows", 6, "Five active reviewers and one inactive reviewer.");
        metric(metrics, "document_text_rows", texts.size(), "One synthetic excerpt for each logical document, not one per ingestion row.");
        metric(metrics, "duplicate_business_ids", 8, "Business IDs appearing twice after trimming.");
        metric(metrics, "exact_duplicate_groups", 5, "Pairs identical on all fields except ingest_id.");
        metric(metrics, "exact_duplicate_excess_rows", 5, "One redundant row per exact pair.");
        metric(metrics, "conflicting_duplicate_groups", 3, "Pairs disagreeing on status, page_count or confidentiality.");
        metric(metrics, "conflicting_duplicate_rows", 6, "All rows belonging to those three conflict groups.");
        metric(metrics, "rows_after_exact_dedup_only", 123, "128 minus 5; conflicting groups are still present at this stage.");
        metric(metrics, "nonconflicting_unique_candidate_documents", 117, "128 minus 5 redundant rows minus 6 conflict rows. Other quality flags remain; this does not mean 117 fully valid rows.");
        metric(metrics, "document_type_count_each_before_aliases", 20, "Each of six canonical types has 20 distinct document IDs in the source fixture; not a real-world distribution.");
        metric(metrics, "missing_title_rows", countBlank("title"), "Raw metadata rows with an empty title.");
        metric(metrics, "missing_received_date_rows", countBlank("received_date"), "Raw metadata rows with an empty receipt date.");
        metric(metrics, "invalid_page_count_rows", 3, "DOC-0009 zero; DOC-0021 negative; DOC-0022 nonnumeric.");
        metric(metrics, "impossible_calendar_date_cells", 2, "ROW-0010 received_date and ROW-0025 document_date; not a count of all chronology issues.");
        metric(metrics, "unknown_matter_reference_rows", rows.stream().filter(r -> r.get("matter_id").trim().equals("MAT-999")).count(), "MAT-999; trim outer whitespace before checking the reference table.");
        metric(metrics, "unknown_reviewer_reference_rows", rows.stream().filter(r -> r.get("reviewer_id").equals("REV-999")).count(), "REV-999; blank assignment is a separate condition.");
        metric(metrics, "inactive_reviewer_assignment_rows", rows.stream().filter(r -> r.get("reviewer_id").equals("REV-006")).count(), "Existing but inactive reviewer; distinct from an unknown ID.");
        metric(metrics, "unsupported_language_rows", rows.stream().filter(r -> r.get("language").equals("xx")).count(), "Only de is supported by this training export contract.");
        metric(metrics, "future_received_date_rows", 1, "ROW-0032 occurs after the fixed 2026-11-20 snapshot.");
        metric(metrics, "injection_log_records", anomalies.size(), "One log record per deliberate cell change or appended duplicate; not an error total.");
        writeCsv(root, "mentor/data/expected_metrics.csv", new String[]{"metric", "value", "definition"}, metrics);
        List<String[]> duplicates = new ArrayList<>();
        int[] ids = {51, 62, 73, 84, 95, 106, 107, 108};
        for (int i = 0; i < ids.length; i++) duplicates.add(new String[]{doc(ids[i]), String.format(Locale.ROOT, "ROW-%04d", ids[i]),
            String.format(Locale.ROOT, "ROW-%04d", 121 + i), i < 5 ? "exact" : "conflicting", i < 5 ? String.format(Locale.ROOT, "ROW-%04d", ids[i]) : "WITHHOLD_BOTH"});
        writeCsv(root, "mentor/data/duplicate_groups.csv", new String[]{"document_id", "first_ingest_id", "second_ingest_id", "duplicate_kind", "action"}, duplicates);
    }

    static long countBlank(String field) { return rows.stream().filter(r -> r.get(field).isBlank()).count(); }
    static void metric(List<String[]> list, String name, Object value, String definition) { list.add(new String[]{name, value.toString(), definition}); }

    static String makeText(int i, int matter, String type, LocalDate documentDate) {
        String partner = "Partner " + String.format(Locale.ROOT, "%02d", matter) + " GmbH";
        String amount = String.format(Locale.GERMANY, "%,.2f", (850 + i * 37 % 4100) * 1.0);
        String serviceMonth = documentDate.minusMonths(1).format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.GERMAN));
        String site = new String[]{"Nord", "Süd", "West", "Ost"}[i % 4];
        String lead = "SYNTHETISCHER ÜBUNGSTEXT - kein echtes Rechtsdokument\n"
            + "Dokument-ID: " + doc(i) + "\nAktenzeichen: " + String.format(Locale.ROOT, "MAT-%03d", matter)
            + "\nDokumentdatum: " + documentDate.format(DE_DATE) + "\nBeteiligte: Rheinblick Service GmbH und " + partner + " (beide erfunden)\n\n";
        String body = switch (type) {
            case "Vertrag" -> "Die Rheinblick Service GmbH und " + partner + " vereinbaren die regelmäßige Betreuung des Standorts " + site + ". "
                + "Zum beschriebenen Umfang gehören die Sichtprüfung der Geräte, die Dokumentation der ausgeführten Arbeiten und eine monatliche Abstimmung mit der Standortkoordination. "
                + "Für das vereinbarte Leistungspaket ist ein monatlicher Betrag von " + amount + " EUR netto vorgesehen. Der geplante Beginn ist der 01.12.2026. "
                + "Eine Erweiterung des Leistungsumfangs wird gesondert dokumentiert. Die Anlage \"Geräteliste; Stand 1\" nennt die betroffenen Geräte und internen Kennungen.\n\n"
                + "Die operative Kontaktadresse lautet service@partner" + matter + ".example. Die Projektablage soll diese Fassung zusammen mit der Anlage führen. "
                + "Ein Unterschriftenblatt ist in diesem Auszug nicht enthalten. Vor der internen Freigabe soll die zuständige Person kontrollieren, ob alle Anlagen vorliegen und ob die Beträge mit dem Bestellvorgang übereinstimmen. "
                + "Dieser Auszug enthält keine Aussage darüber, ob eine rechtliche Prüfung bereits abgeschlossen ist.";
            case "Nachtrag" -> "Die Beteiligten möchten den bisherigen Leistungsumfang am Standort " + site + " ergänzen. Ab dem 01.12.2026 soll ein zusätzlicher monatlicher Kontrolltermin stattfinden. "
                + "Außerdem soll das Serviceteam eine kurze Zusammenfassung der offenen Punkte an die Standortkoordination übermitteln. Für die Zusatzleistung ist ein monatlicher Betrag von " + amount + " EUR netto vorgeschlagen. "
                + "Die bisherige Gerätezuordnung soll unverändert bleiben. Eine aktualisierte Anlage wird als \"Terminplan; Ergänzung 2\" bezeichnet.\n\n"
                + "Die Kontaktperson bittet darum, die ursprüngliche Vereinbarung und diese Ergänzung gemeinsam abzulegen. Im Begleitschreiben wird eine mündliche Abstimmung erwähnt; eine schriftliche Bestätigung ist diesem Auszug nicht beigefügt. "
                + "Das Projektteam soll prüfen, ob die genannte Ausgangsfassung im Bestand vorhanden ist. Die Anmerkung \"Entwurf zur Abstimmung\" steht weiterhin am Ende des Dokuments. "
                + "Eine Entscheidung über die Wirksamkeit oder eine verbindliche Auslegung lässt sich aus dem Auszug allein nicht ableiten.";
            case "Kündigung" -> partner + " teilt mit, dass die Servicevereinbarung für den Standort " + site + " aus Sicht der Absenderin zum 31.01.2027 beendet werden soll. "
                + "Als interner Grund wird eine Neuorganisation der Betreuung genannt. Das Schreiben bittet um eine Eingangsbestätigung an verwaltung@partner" + matter + ".example. "
                + "Außerdem wird eine Abstimmung über noch ausstehende Arbeitsergebnisse und die Rückgabe der überlassenen Unterlagen gewünscht. Eine konkrete Rückgabeliste liegt dem Auszug nicht bei.\n\n"
                + "Die Absenderin verwendet die Formulierung \"zum genannten Termin\" und verweist auf die ursprüngliche Vereinbarung. Deren genaue Versionsnummer wird nicht genannt. "
                + "Die interne Bearbeitung soll deshalb zunächst die zugehörige Akte und die passende Vertragsfassung ermitteln. Ob der angegebene Beendigungstermin zutrifft, wurde hier nicht beurteilt. "
                + "Eine Unterschrift ist im vorliegenden Textauszug nicht sichtbar. Das Team soll die Originaldatei prüfen und eine fachliche Rückfrage dokumentieren, falls Unterlagen fehlen.";
            case "Rechnung" -> "Die Abrechnung " + String.format(Locale.ROOT, "RE-2026-%04d", i) + " betrifft dokumentierte Serviceleistungen am Standort " + site + ". "
                + "Als Leistungszeitraum wird der " + serviceMonth + " angegeben. Der ausgewiesene Nettobetrag beträgt " + amount + " EUR. Die Positionen umfassen einen Kontrolltermin, die Zusammenstellung des Arbeitsberichts und eine zusätzliche Abstimmung. "
                + "Die interne Bestellreferenz lautet " + String.format(Locale.ROOT, "BEST-%03d", matter) + ". Der Text enthält keine Bankverbindung und keine echte Zahlungsanweisung.\n\n"
                + "In der Begleitnotiz wird darum gebeten, die Abrechnung mit der freigegebenen Leistungsbeschreibung zu vergleichen. Eine Anlage mit Einzelstunden wird erwähnt, ist in diesem Auszug jedoch nicht enthalten. "
                + "Die Buchhaltung hat noch keine sachliche Prüfung bestätigt. Falls Leistungsumfang, Betrag oder Referenz voneinander abweichen, soll die verantwortliche Person eine Rückfrage stellen. "
                + "Die Zuordnung zur Projektakte dient hier nur der Dokumentation. Aus dieser Übungsunterlage darf keine Zahlung ausgelöst werden.";
            case "Datenschutzanfrage" -> "Eine synthetische Kontaktperson mit der Kennung KONTAKT-" + String.format(Locale.ROOT, "%03d", i) + " fragt nach den zu ihrer Kontaktaufnahme gespeicherten Angaben. "
                + "Die Anfrage bezieht sich auf eine frühere Terminabstimmung mit dem Standort " + site + ". Gewünscht wird eine nachvollziehbare Übersicht der im Vorgang verwendeten Kontaktdaten und der internen Empfänger. "
                + "Als Rückkanal ist anfrage" + i + "@kontakt.example angegeben. Die Person bittet außerdem um Korrektur einer veralteten Kontaktbezeichnung.\n\n"
                + "Die interne Notiz schlägt vor, zuerst den passenden Vorgang zu ermitteln und die Identität mit dem dafür vorgesehenen Prozess zu prüfen. Der Auszug enthält weder eine abgeschlossene Prüfung noch eine bestätigte Antwort. "
                + "Das Bearbeitungsteam soll keine zusätzlichen personenbezogenen Angaben aus anderen Quellen ergänzen. Eine fachlich zuständige Person soll über weitere Schritte entscheiden. "
                + "Ein gesetzlicher Termin wird in dieser Übungsunterlage nicht berechnet. Die im Export enthaltene review_due_date ist ausschließlich ein erfundenes internes Bearbeitungsziel.";
            default -> "Im bisherigen Austausch zur Akte " + String.format(Locale.ROOT, "MAT-%03d", matter) + " fehlt nach Angaben der Standortkoordination noch die überarbeitete Anlagenliste für den Standort " + site + ". "
                + partner + " fragt, welche Fassung für die nächste interne Abstimmung verwendet werden soll. Im Betreff steht \"Unterlagen; bitte Fassung klären\". "
                + "Zwei Dateinamen werden genannt: Anlagenliste_Entwurf und Anlagenliste_final. Dem Text lässt sich nicht entnehmen, ob die zweite Datei tatsächlich freigegeben wurde.\n\n"
                + "Das Team bittet um eine eindeutige Referenz und eine kurze Beschreibung der Änderungen. Eine Antwort soll zunächst als Entwurf gespeichert werden. "
                + "Die Notiz nennt keinen Betrag und trifft keine Entscheidung über die zugrunde liegende Vereinbarung. Für die weitere Bearbeitung sollen Eingang, Zuständigkeit und fehlende Unterlagen dokumentiert werden. "
                + "Die Kontaktadresse lautet koordination@partner" + matter + ".example. Nach Eingang der fehlenden Anlage soll der Vorgang erneut geprüft und der Bearbeitungsstand aktualisiert werden.";
        };
        return lead + body;
    }

    static void writeCsv(Path root, String relative, String[] header, List<String[]> records) throws IOException {
        StringBuilder out = new StringBuilder();
        appendRow(out, header);
        for (String[] record : records) {
            if (record.length != header.length) throw new IllegalStateException("Bad width: " + relative);
            appendRow(out, record);
        }
        writeText(root, relative, out.toString());
    }
    static void appendRow(StringBuilder out, String[] fields) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) out.append(';');
            String field = fields[i];
            if (field.indexOf(';') >= 0 || field.indexOf('"') >= 0 || field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0) {
                out.append('"').append(field.replace("\"", "\"\"")).append('"');
            } else out.append(field);
        }
        out.append('\n');
    }
    static void writeText(Path root, String relative, String content) throws IOException {
        Path file = root.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
        generated.add(file);
    }
    static void writeManifest(Path root) throws Exception {
        StringBuilder json = new StringBuilder("{\n  \"dataset\": \"Aktenblick synthetic training export\",\n  \"version\": \"1.0\",\n  \"snapshot_date\": \"2026-11-20\",\n  \"synthetic\": true,\n  \"encoding\": \"UTF-8 without BOM\",\n  \"csv_delimiter\": \";\",\n  \"metadata_rows\": 128,\n  \"distinct_documents\": 120,\n  \"matter_rows\": 12,\n  \"reviewer_rows\": 6,\n  \"text_rows\": 120,\n  \"text_template_families\": 6,\n  \"generator\": \"tools/GenerateDataset.java\",\n  \"hash_scope\": \"Files generated by the Java fixture factory; excludes handbooks and optional PDF facsimiles.\",\n  \"sha256\": {\n");
        List<Path> ordered = generated.stream().sorted().toList();
        for (int i = 0; i < ordered.size(); i++) {
            Path file = ordered.get(i);
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
            json.append("    \"").append(root.relativize(file).toString().replace('\\', '/')).append("\": \"").append(hash).append("\"");
            if (i + 1 < ordered.size()) json.append(',');
            json.append('\n');
        }
        json.append("  }\n}\n");
        Files.writeString(root.resolve("data/manifest.json"), json.toString(), StandardCharsets.UTF_8);
    }
    static void verifyInMemory() {
        if (rows.size() != 128 || texts.size() != 120) throw new AssertionError("Wrong dataset size");
        Map<String, List<Map<String, String>>> groups = rows.stream().collect(Collectors.groupingBy(r -> r.get("document_id").trim()));
        long exact = groups.values().stream().filter(g -> g.size() == 2 && sameBusinessFields(g.get(0), g.get(1))).count();
        long conflicts = groups.values().stream().filter(g -> g.size() == 2 && !sameBusinessFields(g.get(0), g.get(1))).count();
        if (groups.size() != 120 || exact != 5 || conflicts != 3) throw new AssertionError("Duplicate fixture incorrect");
        if (rows.stream().map(r -> r.get("ingest_id")).distinct().count() != 128) throw new AssertionError("Repeated ingestion ID");
        if (!groups.keySet().equals(texts.stream().map(t -> t[0]).collect(Collectors.toSet()))) throw new AssertionError("Text join mismatch");
    }
    static boolean sameBusinessFields(Map<String, String> a, Map<String, String> b) {
        return Arrays.stream(HEADER).filter(h -> !h.equals("ingest_id")).allMatch(h -> a.get(h).equals(b.get(h)));
    }
    static String[] values(Map<String, String> row) { return Arrays.stream(HEADER).map(row::get).toArray(String[]::new); }
    static String doc(int i) { return String.format(Locale.ROOT, "DOC-%04d", i); }
}
