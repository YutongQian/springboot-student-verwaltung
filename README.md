# springbootProjekt — Studentenverwaltung

Eine kleine REST-Anwendung mit Spring Boot, die einen CRUD-Zyklus für
Studenten abbildet. Das Projekt ist als Lern- und Nachweisprojekt
entstanden: Es zeigt Schichtentrennung, Bean Validation, zentrale
Fehlerbehandlung und automatisierte Tests.

---

## Technische Angaben

| | |
|---|---|
| Sprache | Java 17 |
| Framework | Spring Boot 4.2.0 |
| Persistenz | Spring Data JPA / Hibernate |
| Datenbank | MySQL (Produktion), H2 im Test |
| Build | Maven |
| Tests | JUnit 5, Mockito, MockMvc, AssertJ |

---

## Architektur

Die Anwendung ist klassisch in Schichten getrennt. Jede Schicht kennt nur
die direkt darunterliegende:

```
controller     HTTP-Schicht, nimmt Requests an, liefert Response
   |
   v
service        Fachlogik, Transaktionsgrenze, Regeln
   |
   v
dao            Entity + Repository (Spring Data JPA)
   
dto            Datentransferobjekt für die API (entkoppelt von der Entity)
converter      Wandlung Entity <-> DTO
exception      Eigene Ausnahmen + zentrale Fehlerbehandlung
```

**Warum DTO und Entity getrennt sind:** Die Entity bildet die
Datenbank ab, das DTO die API. Beide haben unterschiedliche Regeln.
Würde die Entity direkt ausgeliefert, könnte der Client Felder setzen,
die er nicht setzen darf, und jede Schemaänderung wäre automatisch
eine API-Änderung.

---

## Endpunkte

| Methode | Pfad | Beschreibung |
|---|---|---|
| GET | `/student/{id}` | Einen Studenten lesen |
| POST | `/student` | Neuen Studenten anlegen |
| PUT | `/student/{id}` | Studenten ändern (JSON-Body) |
| DELETE | `/student/{id}` | Studenten löschen |
| GET | `/hello` | Smoke-Test |

Alle Antworten haben dasselbe Format:

```json
{
  "success": true,
  "data": { "id": 1, "name": "Yutong Qian", "email": "y@example.com", "age": 30 },
  "errorMsg": null
}
```

Bei Fehlern:

```json
{
  "success": false,
  "data": null,
  "errorMsg": "Student mit der ID 99 wurde nicht gefunden"
}
```

---

## Fehlerbehandlung und Statuscodes

| Fall | HTTP | Ausnahme |
|---|---|---|
| Student existiert nicht | 404 | `StudentNotFoundException` |
| E-Mail bereits vergeben | 409 | `EmailAlreadyUsedException` |
| Body ungültig (leer, E-Mail fehlt) | 400 | `MethodArgumentNotValidException` |
| Unerwarteter Fehler | 500 | `Exception` |

Zentrale Stelle: `exception/GlobalExceptionHandler.java`.

**Warum das nötig ist:** Ohne diese Klasse liefert Spring für jede
fachliche Ausnahme einen nackten HTTP 500. Der Client erfährt dann nicht,
ob er die ID falsch war oder ob der Server kaputt ist.

---

## Voraussetzungen zum Starten

- JDK 17 oder neuer
- Maven (oder den beiliegenden `mvnw`)
- Eine laufende MySQL-Instanz mit leerer Datenbank `test`

Zugangsdaten werden über Umgebungsvariablen gesetzt; die Standardwerte
sind für eine lokale Entwicklungsumgebung gedacht:

```bash
export DB_USERNAME=root
export DB_PASSWORD=
```

---

## Starten

```bash
./mvnw spring-boot:run
```

Die Anwendung läuft dann auf `http://localhost:8080`.

Das Schema wird beim Start automatisch angelegt bzw. aktualisiert
(`spring.jpa.hibernate.ddl-auto=update`). Das ist für die Entwicklung
praktisch, für den Produktivbetrieb nicht geeignet.

---

## Tests

```bash
./mvnw test
```

15 Tests, aufgeteilt auf zwei Klassen:

| Klasse | Art | Was geprüft wird |
|---|---|---|
| `StudentServiceImplTest` | Unit-Test mit Mockito | Fachlogik: findet, wirft, speichert oder speichert bewusst nicht |
| `StudentControllerTest` | `@WebMvcTest` mit MockMvc | HTTP-Statuscodes, Antwortformat, Validierung des Bodys |

Die Unit-Tests laufen ohne Spring und ohne Datenbank, dadurch in
Millisekunden.

---

## Bekannte Grenzen

- **Kein Unique-Constraint in der Datenbank.** Die Prüfung auf doppelte
  E-Mail-Adressen findet im Service statt (`findByEmail`). Bei parallelen
  Requests ist das eine Race Condition. Sauber wäre ein
  `UNIQUE KEY` auf `email` plus Abfangen der
  `DataIntegrityViolationException`.
- **Kein Paging.** `GET /student` fehlt bewusst noch.
- **Keine Authentifizierung.** Alle Endpunkte sind offen.
- **Kein Flyway/Liquibase.** Das Schema entsteht über Hibernate.

Diese Punkte sind bekannt und nicht übersehen.
