# Guida Ufficiale: Compilazione e Installazione Gratuita su iPhone (iOS)

Questa guida ti mostra passo dopo passo come creare il file installabile (`.ipa`) dell'app **EcoMap Precision per iPhone** senza possedere un Mac e come installarlo direttamente sul tuo iPhone tramite cavetto USB a **costo 0€**.

---

## 1. Come caricare il progetto su GitHub (dal tuo PC Windows)

1. Vai su [github.com](https://github.com) e accedi con il tuo account (se non ne hai uno, crealo gratuitamente).
2. In alto a destra clicca su **+** -> **New repository**.
3. Dai un nome al repository (ad esempio `ecomap-precision-ios`).
4. Scegli **Public** (i repository pubblici su GitHub hanno minuti di compilazione Mac illimitati e gratuiti).
5. Clicca **Create repository**.
6. Apri il terminale (o PowerShell) sul tuo PC nella cartella del progetto ed esegui questi 4 comandi (sostituendo l'URL del tuo repository):

```bash
git init
git add .
git commit -m "Aggiunto progetto EcoMap Precision per iPhone"
git branch -M main
git remote add origin https://github.com/TUO-USERNAME/TUO-REPOSITORY.git
git push -u origin main
```

---

## 2. Compilazione Automatica nel Cloud (GitHub Actions)

Appena fai il `push` del codice:
1. Vai sulla pagina del tuo repository su GitHub e clicca sulla scheda in alto **"Actions"**.
2. Vedrai comparire in tempo reale il flusso **"Build iOS IPA (EcoMap Precision)"**.
3. Un server Mac di GitHub (macOS con Xcode) prenderà il codice, lo compilerà e genererà il pacchetto per iPhone.
4. Al termine (circa 2-3 minuti), vedrai una spunta verde ✅.
5. Clicca sul nome del lavoro completato: in basso nella sezione **Artifacts** troverai il file:
   👉 **`EcoMapPrecision-iOS-IPA`**
6. Cliccaci sopra per scaricare lo zip contenente il file `EcoMapPrecision.ipa` sul tuo computer Windows.

---

## 3. Installazione su iPhone tramite Cavetto (con Sideloadly)

**Requisiti:**
- Il tuo iPhone collegato con il cavo USB al computer Windows.
- Il programma gratuito **Sideloadly** installato sul PC ([sideloadly.io](https://sideloadly.io)).
- **iTunes** (o iCloud) installato su Windows (serve a Windows per riconoscere l'iPhone).

### Istruzioni passo-passo:
1. Collega il tuo iPhone al PC con il cavo USB (se l'iPhone chiede *"Autorizza questo computer"*, inserisci il codice di sblocco e premi *"Autorizza"*).
2. Avvia **Sideloadly** su Windows.
3. Nel campo **"Device"** vedrai comparire il nome del tuo iPhone.
4. Nel campo **"Apple ID"** inserisci la tua normale email del tuo ID Apple (quella gratuita con cui scarichi le app).
5. Trascina il file `EcoMapPrecision.ipa` (che hai scaricato da GitHub) dentro la grande icona di Sideloadly.
6. Clicca sul pulsante **"Start"**.
7. Se richiesta, inserisci la password del tuo ID Apple (serve solo ai server Apple per firmare l'app gratuitamente per il tuo dispositivo).
8. Attendi circa 30 secondi: vedrai comparire la scritta **"Done"** e l'icona di **EcoMap Precision** apparirà sulla schermata principale del tuo iPhone!

---

## 4. Primo avvio sull'iPhone (Autorizzazione Sviluppatore)

La prima volta che tocchi l'icona dell'app su iPhone, iOS mostrerà l'avviso *"Sviluppatore interno non autorizzato"*. È la normale procedura di sicurezza di Apple:

1. Apri le **Impostazioni** dell'iPhone.
2. Vai su **Generali** -> **Gestione VPN e dispositivo** (oppure *Gestione profili*).
3. Sotto la voce *"App sviluppatore"* tocca la tua email ID Apple.
4. Tocca **"Autorizza [tua email]"** e conferma.
5. Torna alla home e apri **EcoMap Precision**: l'app si aprirà subito al 100% nativa con mappa, GPS, storico e creazione report PDF!

---

## 5. Rinnovo ogni 7 giorni (Account Gratuito Apple)

Poiché l'ID Apple gratuito rilascia licenze di prova della durata di 7 giorni:
* I tuoi dati, segnalazioni e postazioni rimangono **sempre salvati** sul telefono.
* Se dopo una settimana l'app non si apre, basta ricollegare l'iPhone al PC, aprire Sideloadly e premere di nuovo **Start**.
* *(Opzionale)*: Se in Sideloadly abiliti l'opzione **Wi-Fi**, il rinnovo avverrà in automatico in background quando il PC e l'iPhone sono connessi allo stesso modem Wi-Fi di casa/ufficio.
