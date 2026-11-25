/**
 * Studentenverwaltung – Frontend.
 *
 * Reines JavaScript, kein Framework, kein Build-Schritt.
 * Der Server liefert bei jeder Antwort dasselbe Format:
 *
 *     { "success": true,  "data": ..., "errorMsg": null }
 *     { "success": false, "data": null, "errorMsg": "..." }
 *
 * Deshalb gibt es genau eine Stelle (anfrage()), die damit umgeht.
 */

const API = '/student';

/* ----------------------------------------------------------------
   Zustand
   ---------------------------------------------------------------- */

/** Alle Studenten vom Server, ungefiltert. */
let studenten = [];

/** ID des Studenten, der gerade bearbeitet wird – null heisst "anlegen". */
let bearbeiteteId = null;

/* ----------------------------------------------------------------
   Elemente
   ---------------------------------------------------------------- */

const el = {
    meldung: document.getElementById('meldung'),
    formular: document.getElementById('formular'),
    formularTitel: document.getElementById('formular-titel'),
    feldId: document.getElementById('feld-id'),
    feldName: document.getElementById('feld-name'),
    feldEmail: document.getElementById('feld-email'),
    feldAlter: document.getElementById('feld-alter'),
    feldSuche: document.getElementById('feld-suche'),
    btnSpeichern: document.getElementById('btn-speichern'),
    btnAbbrechen: document.getElementById('btn-abbrechen'),
    btnNeuLaden: document.getElementById('btn-neu-laden'),
    tabellenKoerper: document.getElementById('tabellen-koerper'),
    anzahl: document.getElementById('anzahl'),
    leer: document.getElementById('leer'),
};

/* ----------------------------------------------------------------
   Meldungen
   ---------------------------------------------------------------- */

let meldungTimer = null;

/**
 * Zeigt eine Meldung an. Bei Erfolg verschwindet sie nach 4 Sekunden,
 * bei einem Fehler bleibt sie stehen, bis der naechste Aufruf kommt.
 */
function zeigeMeldung(text, art) {
    clearTimeout(meldungTimer);

    el.meldung.textContent = text;
    el.meldung.className = 'meldung ' + art;
    el.meldung.hidden = false;

    if (art === 'erfolg') {
        meldungTimer = setTimeout(() => {
            el.meldung.hidden = true;
        }, 4000);
    }
}

function meldungAusblenden() {
    clearTimeout(meldungTimer);
    el.meldung.hidden = true;
}

/* ----------------------------------------------------------------
   HTTP
   ---------------------------------------------------------------- */

/**
 * Einzige Stelle, die fetch aufruft.
 *
 * Der Server antwortet auch bei 404 und 409 mit einem JSON-Koerper
 * im Response-Format. Deshalb wird der Body immer gelesen, egal
 * welcher Statuscode kommt – und der Fehlertext kommt von dort.
 *
 * @throws {Error} mit der Meldung des Servers als message
 */
async function anfrage(pfad, optionen = {}) {
    let antwort;

    try {
        antwort = await fetch(API + pfad, {
            headers: {'Content-Type': 'application/json'},
            ...optionen,
        });
    } catch (netzwerkFehler) {
        // Server nicht erreichbar, Verbindung weg, DNS-Fehler
        throw new Error('Der Server ist nicht erreichbar. Läuft die Anwendung?');
    }

    let koerper = null;
    const text = await antwort.text();
    if (text) {
        try {
            koerper = JSON.parse(text);
        } catch (parseFehler) {
            throw new Error(`Unerwartete Antwort vom Server (HTTP ${antwort.status}).`);
        }
    }

    if (!antwort.ok || koerper === null || koerper.success !== true) {
        const meldung = koerper && koerper.errorMsg
            ? koerper.errorMsg
            : `Fehler ${antwort.status}: ${antwort.statusText}`;
        throw new Error(meldung);
    }

    return koerper.data;
}


/* ----------------------------------------------------------------
   Daten laden
   ---------------------------------------------------------------- */

async function ladeAlle() {
    try {
        studenten = await anfrage('');
        zeichneTabelle();
    } catch (fehler) {
        zeigeMeldung(fehler.message, 'fehler');
    }
}

/* ----------------------------------------------------------------
   Tabelle zeichnen
   ---------------------------------------------------------------- */

/**
 * Baut die Tabelle aus `studenten` neu auf.
 *
 * Die Werte werden mit textContent eingesetzt, nicht mit innerHTML:
 * ein Name wie "<script>" bleibt damit Text und wird nicht ausgefuehrt.
 */
function zeichneTabelle() {
    const filter = el.feldSuche.value.trim().toLowerCase();

    const sichtbar = studenten.filter(s =>
        filter === ''
        || s.name.toLowerCase().includes(filter)
        || s.email.toLowerCase().includes(filter)
    );

    el.tabellenKoerper.replaceChildren();

    for (const student of sichtbar) {
        el.tabellenKoerper.appendChild(baueZeile(student));
    }

    // Zaehler: bei aktivem Filter beide Zahlen zeigen
    if (filter === '') {
        el.anzahl.textContent = studenten.length === 1
            ? '1 Eintrag'
            : `${studenten.length} Einträge`;
    } else {
        el.anzahl.textContent = `${sichtbar.length} von ${studenten.length}`;
    }

    // Leerhinweis: Text unterscheidet sich, je nachdem ob gefiltert wird
    if (sichtbar.length === 0) {
        el.leer.textContent = studenten.length === 0
            ? 'Noch keine Studenten gespeichert.'
            : 'Kein Eintrag passt zum Filter.';
        el.leer.hidden = false;
    } else {
        el.leer.hidden = true;
    }
}

/** Erzeugt eine <tr> fuer einen Studenten. */
function baueZeile(student) {
    const zeile = document.createElement('tr');
    if (student.id === bearbeiteteId) {
        zeile.classList.add('aktiv');
    }

    zeile.appendChild(zelle(student.id, 'sp-id', 'ID'));
    zeile.appendChild(zelle(student.name, 'sp-name', 'Name'));
    zeile.appendChild(zelle(student.email, 'sp-email', 'E-Mail'));
    zeile.appendChild(zelle(student.age ?? '–', 'sp-alter', 'Alter'));

    // Aktionsspalte
    const aktionen = document.createElement('td');
    aktionen.className = 'sp-aktion';

    const btnBearbeiten = document.createElement('button');
    btnBearbeiten.type = 'button';
    btnBearbeiten.className = 'btn-klein';
    btnBearbeiten.textContent = 'Bearbeiten';
    btnBearbeiten.addEventListener('click', () => starteBearbeiten(student));

    const btnLoeschen = document.createElement('button');
    btnLoeschen.type = 'button';
    btnLoeschen.className = 'btn-klein loeschen';
    btnLoeschen.textContent = 'Löschen';
    btnLoeschen.addEventListener('click', () => loesche(student));

    aktionen.append(btnBearbeiten, btnLoeschen);
    zeile.appendChild(aktionen);

    return zeile;
}

/** Eine <td>; data-label wird nur in der mobilen Ansicht gebraucht. */
function zelle(wert, klasse, beschriftung) {
    const td = document.createElement('td');
    td.className = klasse;
    td.dataset.label = beschriftung;
    td.textContent = wert;
    return td;
}


/* ----------------------------------------------------------------
   Formular: Anlegen und Bearbeiten
   ---------------------------------------------------------------- */

/** Liest die Felder aus. Age wird zu null, wenn nichts eingegeben wurde. */
function leseFormular() {
    const alterText = el.feldAlter.value.trim();

    return {
        name: el.feldName.value.trim(),
        email: el.feldEmail.value.trim(),
        age: alterText === '' ? null : Number(alterText),
    };
}

/** Setzt das Formular in den Anlege-Zustand zurueck. */
function setzeFormularZurueck() {
    bearbeiteteId = null;
    el.formular.reset();
    el.feldId.value = '';

    el.formularTitel.textContent = 'Neuen Studenten anlegen';
    el.btnSpeichern.textContent = 'Anlegen';
    el.btnAbbrechen.hidden = true;

    feldfehlerLoeschen();
    zeichneTabelle();
}

/** Fuellt das Formular mit einem bestehenden Studenten. */
function starteBearbeiten(student) {
    bearbeiteteId = student.id;

    el.feldId.value = String(student.id);
    el.feldName.value = student.name;
    el.feldEmail.value = student.email;
    el.feldAlter.value = student.age ?? '';

    el.formularTitel.textContent = `Student #${student.id} bearbeiten`;
    el.btnSpeichern.textContent = 'Speichern';
    el.btnAbbrechen.hidden = false;

    feldfehlerLoeschen();
    zeichneTabelle();

    el.feldName.focus();
    el.feldName.setSelectionRange(el.feldName.value.length, el.feldName.value.length);
}

async function speichere(event) {
    event.preventDefault();
    meldungAusblenden();
    feldfehlerLoeschen();

    const daten = leseFormular();

    // Vorpruefung im Browser – der Server prueft trotzdem noch einmal
    const fehler = pruefeEingabe(daten);
    if (Object.keys(fehler).length > 0) {
        zeigeFeldfehler(fehler);
        zeigeMeldung('Bitte die markierten Felder korrigieren.', 'fehler');
        return;
    }

    const bearbeitetGerade = bearbeiteteId !== null;
    el.btnSpeichern.disabled = true;
    el.btnSpeichern.textContent = 'Speichern …';

    try {
        if (bearbeitetGerade) {
            await anfrage(`/${bearbeiteteId}`, {
                method: 'PUT',
                body: JSON.stringify(daten),
            });
            zeigeMeldung('Änderungen gespeichert.', 'erfolg');
        } else {
            const neueId = await anfrage('', {
                method: 'POST',
                body: JSON.stringify(daten),
            });
            zeigeMeldung(`Student angelegt (ID ${neueId}).`, 'erfolg');
        }

        setzeFormularZurueck();
        await ladeAlle();
    } catch (problem) {
        zeigeMeldung(problem.message, 'fehler');
    } finally {
        el.btnSpeichern.disabled = false;
        el.btnSpeichern.textContent = bearbeiteteId !== null ? 'Speichern' : 'Anlegen';
    }
}


/* ----------------------------------------------------------------
   Loeschen
   ---------------------------------------------------------------- */

async function loesche(student) {
    const sicher = confirm(
        `Student "${student.name}" (ID ${student.id}) wirklich löschen?`
    );
    if (!sicher) {
        return;
    }

    meldungAusblenden();

    try {
        await anfrage(`/${student.id}`, {method: 'DELETE'});
        zeigeMeldung(`Student "${student.name}" gelöscht.`, 'erfolg');

        // Wurde gerade dieser Eintrag bearbeitet, Formular zuruecksetzen
        if (bearbeiteteId === student.id) {
            setzeFormularZurueck();
        }
        await ladeAlle();
    } catch (problem) {
        zeigeMeldung(problem.message, 'fehler');
    }
}

/* ----------------------------------------------------------------
   Vorpruefung im Browser
   ----------------------------------------------------------------
   Der Server validiert dieselben Regeln ueber Bean Validation.
   Diese Pruefung ist nur fuer die schnelle Rueckmeldung da – sie
   ersetzt die Serverpruefung nicht.
   ---------------------------------------------------------------- */

function pruefeEingabe(daten) {
    const fehler = {};

    if (daten.name === '') {
        fehler['feld-name'] = 'Bitte einen Namen eingeben.';
    } else if (daten.name.length > 100) {
        fehler['feld-name'] = 'Höchstens 100 Zeichen.';
    }

    if (daten.email === '') {
        fehler['feld-email'] = 'Bitte eine E-Mail-Adresse eingeben.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(daten.email)) {
        fehler['feld-email'] = 'Das sieht nicht wie eine E-Mail-Adresse aus.';
    }

    if (daten.age !== null && (!Number.isInteger(daten.age) || daten.age <= 0)) {
        fehler['feld-alter'] = 'Alter muss eine ganze Zahl größer als 0 sein.';
    }

    return fehler;
}

function zeigeFeldfehler(fehler) {
    for (const [feldId, text] of Object.entries(fehler)) {
        const feld = document.getElementById(feldId);
        const hinweis = document.querySelector(`.hinweis[data-fuer="${feldId}"]`);

        if (feld) {
            feld.closest('.feld').classList.add('ungueltig');
        }
        if (hinweis) {
            hinweis.textContent = text;
        }
    }
}

function feldfehlerLoeschen() {
    document.querySelectorAll('.feld.ungueltig').forEach(feld => {
        feld.classList.remove('ungueltig');
    });
    document.querySelectorAll('.hinweis').forEach(hinweis => {
        hinweis.textContent = '';
    });
}

/* ----------------------------------------------------------------
   Start
   ---------------------------------------------------------------- */

el.formular.addEventListener('submit', speichere);

el.btnAbbrechen.addEventListener('click', () => {
    setzeFormularZurueck();
    meldungAusblenden();
});

el.btnNeuLaden.addEventListener('click', () => {
    meldungAusblenden();
    ladeAlle();
});

el.feldSuche.addEventListener('input', zeichneTabelle);

// Feldfehler verschwinden, sobald der Nutzer nachbessert
[el.feldName, el.feldEmail, el.feldAlter].forEach(feld => {
    feld.addEventListener('input', () => {
        const wrapper = feld.closest('.feld');
        wrapper.classList.remove('ungueltig');
        const hinweis = wrapper.querySelector('.hinweis');
        if (hinweis) {
            hinweis.textContent = '';
        }
    });
});

ladeAlle();

