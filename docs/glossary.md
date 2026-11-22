# German document-management glossary

These are working meanings for the fictional Aktenblick project, not legal definitions. Keep English Java names such as `document_id` in code; use the German business label when discussing the dataset.

| German term | English help |
| --- | --- |
| die Akte | A matter or case file that groups related documents |
| das Aktenzeichen | A reference identifying a matter; this dataset uses `matter_id` |
| das Dokument | One document identified by `document_id` |
| der Datensatz | One data record; in the metadata export, one ingestion row |
| die Metadaten | Information about a document, such as type, date, and owner |
| der Vertrag | A contract |
| der Nachtrag | An amendment or addendum to an existing document |
| die Kündigung | A termination or cancellation notice; not a legal-validity decision |
| die Rechnung | An invoice |
| die Datenschutzanfrage | A request concerning personal data; no statutory timing is inferred |
| der Schriftverkehr | Correspondence |
| die Abteilung | A department |
| der Einkauf | Procurement or purchasing |
| das Personal | Human resources in this dataset's department labels |
| der Vertrieb | Sales |
| der Datenschutz | Data protection as a department label |
| Neu | Newly received, not yet processed |
| In Prüfung | Under review |
| Freigegeben | Approved in the fictional operational workflow |
| Rückfrage | A follow-up question is outstanding |
| die Bearbeitung | Processing or handling a record |
| der Bearbeiter / die Bearbeiterin | The assigned staff member; the dataset uses `reviewer_id` |
| das Eingangsdatum | Date received |
| das Dokumentdatum | Date written on the document; it is not necessarily the receipt date |
| der interne Prüftermin | Internal review target; `review_due_date` is not a statutory deadline |
| die Dublette | A duplicate; the exact meaning must be defined before removing rows |
| der Widerspruch | A conflict between supplied values in this exercise |
| der fehlende Wert | A missing value |
| der ungültige Wert | An invalid value, distinct from a missing value |
| die Vertraulichkeit | Confidentiality category; fictional metadata, not an access-control system |
| die Quelle | A source, such as a scan or email |
| der Beleg / die Textstelle | Evidence or a supporting passage |
| nachvollziehbar | Traceable or understandable from the evidence |
| die Auswertung | Analysis or reporting |
| der Arbeitsvorrat | The workload or backlog waiting to be processed |

## Useful meeting sentences

**"Mir ist aufgefallen, dass ..."** - I noticed that ...

**"Auf welcher Grundlage sollen wir diese Werte zusammenfassen?"** - What rule should we use to combine these values?

**"Der Wert fehlt im Export. Ich möchte ihn nicht raten."** - The value is missing from the export. I do not want to guess it.

**"Diese Zahl bezieht sich auf eindeutige Dokumente, nicht auf Importzeilen."** - This number refers to unique documents rather than ingestion rows.

**"Die Quelle enthält dazu keine Angabe."** - The source does not provide that information.

**"Ich habe eine kleine Version umgesetzt und an zwölf Datensätzen geprüft."** - I implemented a small version and checked it on twelve records.

## Technical vocabulary in plain English

**CSV:** A text file with records and fields. The separator in this pack is a semicolon.

**UTF-8:** The character encoding used by the files, including German characters.

**Schema:** The agreed field names and meanings.

**Normalization:** Turning known equivalent representations into a documented common format.

**Validation:** Checking whether a value or relationship follows an agreed rule.

**Join:** Matching records from different tables using identifiers.

**Denominator:** The population used under a fraction or percentage.

**Reconciliation:** An explanation of how counts before and after processing fit together.

**Idempotent:** Applying a normalization again does not change the already normalized value.

**OCR:** Text recognition from an image or scan. A confidence value is a supplied signal, not proof that the extracted words are correct.

**LLM:** A model that generates language. In this pack, its outputs are simulated for an evidence-checking exercise.
