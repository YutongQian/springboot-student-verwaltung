# Weboberflaeche fuer die Studentenverwaltung

Stand: 25.11.2026
Ziel: Anlegen, Anzeigen, Bearbeiten und Loeschen direkt im Browser,
ohne separates Frontend-Projekt und ohne Build-Schritt.

## Starten

```bash
cd ~/IdeaProjects/springbootProjekt
./mvnw spring-boot:run
```

Dann im Browser oeffnen:

    http://localhost:8080/

Spring Boot liefert `index.html` automatisch aus, weil sie in
`src/main/resources/static/` liegt. Es gibt keinen zweiten Server.

![Übersicht](../docs/screenshot-uebersicht.png)

## Die drei Dateien

| Datei | Zweck |
|---|---|
| `static/index.html` | Aufbau der Seite (Formular, Tabelle, Meldungen) |
| `static/style.css` | Aussehen, inkl. Mobilansicht unter 720 px |
| `static/app.js` | Logik: laden, anlegen, bearbeiten, loeschen, filtern |

Kein Framework, kein Bundler, keine npm-Abhaengigkeiten. Reines
HTML, CSS und JavaScript (ES2017).

## Was die Seite kann

| Funktion | Bedienung |
|---|---|
| Liste anzeigen | automatisch beim Laden |
| Anlegen | Formular ausfuellen, "Anlegen" |
| Bearbeiten | "Bearbeiten" in der Zeile, Felder aendern, "Speichern" |
| Loeschen | "Loeschen" in der Zeile, Rueckfrage bestaetigen |
| Filtern | Feld "Filtern" – sucht in Name und E-Mail |
| Neu laden | "Neu laden" |

Nach jeder Aenderung wird die Liste frisch vom Server geholt. Die
Seite haelt keinen eigenen Datenstand, der auseinanderlaufen koennte.

## Backend-Aenderungen, die dafuer noetig waren

Zwei Dinge fehlten und mussten ergaenzt werden – ohne sie waere
"Anzeigen" und "Bearbeiten" nicht vollstaendig moeglich gewesen.

### 1 · Neuer Endpunkt `GET /student`

Es gab nur `GET /student/{id}`. Die Oberflaeche kann aber nicht
wissen, welche IDs existieren. Neu:

```
GET /student   ->  { "data": [ {...}, {...} ], "success": true }
```

### 2 · `PUT` aktualisiert jetzt auch `age`

Vorher nahm die Methode nur `name` und `email` entgegen. Ein
geaendertes Alter wurde stillschweigend verworfen – im Formular
haette man 35 eingetragen und 31 zurueckbekommen.

Aus der Signatur

    updateStudentById(long id, String name, String email)

wurde

    updateStudentById(long id, StudentDTO studentDTO)

Damit sind alle Felder des DTO abgedeckt und weitere Felder kommen
spaeter ohne Signaturaenderung dazu. Fehlende Werte im DTO
(`null`) lassen das jeweilige Feld unveraendert.

### 3 · 404 blieb nicht 404

Beim Testen aufgefallen: Ein Aufruf von `/gibtsnicht` antwortete mit
**HTTP 500** und dem Text "Interner Fehler".

Ursache: `GlobalExceptionHandler` hat eine allgemeine Regel
`@ExceptionHandler(Exception.class)`. Die fing auch Springs
`NoResourceFoundException` ab, die eigentlich fuer "Datei nicht
gefunden" gedacht ist.

Behoben durch einen eigenen Handler:

```java
@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<Response<Void>> handleNoResource(NoResourceFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Response.newFail("Nicht gefunden: " + ex.getResourcePath()));
}
```

Das betrifft auch das REST-Verhalten: Ein falscher Pfad liefert jetzt
404 statt eines vorgetaeuschten Serverfehlers.

## Tests

Die Testzahl ist von 16 auf 21 gestiegen:

```
StudentControllerTest     9 Tests  (2 neu: Liste voll / Liste leer)
StudentServiceImplTest   11 Tests  (3 neu: age, Liste voll, Liste leer)
ApplicationTests          1 Test
--------------------------------------------------------------
                         21 Tests, alle gruen
```

Neu abgedeckt:

- `getAllStudents` liefert alle Eintraege als DTO-Liste
- `getAllStudents` liefert eine leere Liste, wenn nichts gespeichert ist
- `updateStudentById` aendert auch das Alter
- `GET /student` liefert ein JSON-Array
- `GET /student` liefert ein leeres Array

## Was bewusst NICHT verwendet wurde

Kein React/Vue, kein Thymeleaf, kein Bootstrap. Begruendung:

- Ein zweites Projekt (npm, Build, eigener Dev-Server) waere fuer
  fuenf Formularfelder unverhaeltnismaessig.
- Ohne Framework sieht man im Code direkt, was passiert. Bei einer
  Bewerbung ist das ein Vorteil: der Leser sieht `fetch`, `PUT`,
  `DELETE` – und nicht einen Aufruf, dessen Verhalten in einer
  Bibliothek steckt.
- Es gibt nichts zu installieren. `git clone`, `./mvnw spring-boot:run`,
  fertig.

## Sicherheitshinweis

`app.js` setzt alle Werte mit `textContent` ein, nicht mit
`innerHTML`. Ein Name wie `<script>alert(1)</script>` wird damit als
Text angezeigt und nicht ausgefuehrt. Wer darauf spaeter `innerHTML`
verwendet, oeffnet eine XSS-Luecke.

## Bekannte Grenzen

- **Keine Paginierung.** Bei vielen Datensaetzen wird die Tabelle
  lang. `findAll()` ohne `Pageable` ist fuer eine Uebungsanwendung
  in Ordnung, fuer einen echten Bestand nicht.
- **Kein Login.** Jeder mit Zugriff auf den Port kann alles aendern.
- **Filter laeuft im Browser**, nicht in der Datenbank. Bei wenigen
  hundert Zeilen unproblematisch.
- **`spring.jpa.hibernate.ddl-auto=update`** steht weiterhin in
  `application.properties`. Fuer Entwicklung gedacht, nicht fuer
  Produktion (siehe Kommentar in der Datei).
