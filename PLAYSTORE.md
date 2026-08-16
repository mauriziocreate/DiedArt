# DeadArt — pubblicazione sul Play Store

Tutto quello che serve, in ordine. I testi sono pronti da copiare.

---

## 1. La chiave di firma

È la cosa più delicata di tutte: **se la perdi, non potrai mai più aggiornare l'app**. Nemmeno Google può rimediare. Va creata una volta e conservata in due posti diversi.

Sul tuo computer, in un terminale:

```bash
keytool -genkey -v -keystore deadart.keystore \
  -alias deadart -keyalg RSA -keysize 2048 -validity 10000
```

Ti chiede una password: inventala e **scrivila subito da qualche parte**. Poi nome, organizzazione, città, provincia, sigla del paese (IT): rispondi quello che vuoi, non compare da nessuna parte per gli utenti.

Poi converti il file in testo:

**Linux o Mac**
```bash
base64 -w0 deadart.keystore > keystore.txt
```

**Windows, in PowerShell**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("deadart.keystore")) | Out-File -Encoding ascii keystore.txt
```

Conserva `deadart.keystore` fuori da GitHub: su una chiavetta e su Drive, non solo sul computer.

## 2. I quattro segreti su GitHub

Nel repository: **Settings → Secrets and variables → Actions → New repository secret**.

| Nome del segreto | Cosa incollarci |
|---|---|
| `KEYSTORE_BASE64` | tutto il contenuto di `keystore.txt`, su una riga sola |
| `MAIR_KEYSTORE_PASSWORD` | la password che hai inventato |
| `MAIR_KEY_ALIAS` | `deadart` |
| `MAIR_KEY_PASSWORD` | la stessa password (a meno che tu ne abbia messa un'altra per la chiave) |

I nomi sono quelli di MAIR GO! apposta: se hai già quei segreti, puoi riusare lo stesso keystore. Va benissimo — due app diverse possono condividere la chiave, perché a distinguerle è l'identificativo del pacchetto.

Attenzione a `KEYSTORE_BASE64`: deve essere una riga unica, senza a capo e senza spazi. Se il file di testo va a capo, apri e togli le interruzioni.

## 3. Le versioni

Nel repository c'è il file **`VERSIONE`**, che contiene una riga sola: `1.0.0`.

- Il **nome della versione** è quello che leggi lì dentro, ed è quello che vede l'utente sul Play Store. Cambialo tu quando vuoi: `1.0.1` per una correzione, `1.1.0` per una funzione nuova, `2.0.0` per un cambiamento grosso.
- Il **numero di build** cresce da solo a ogni compilazione (1001, 1002, 1003…). Google pretende che sia sempre più alto del precedente, ed è il motivo per cui non devi pensarci.

Quindi: per pubblicare un aggiornamento ti basta modificare `VERSIONE` e fare commit. Il resto è automatico.

## 4. Compilare

**Actions → Compila DeadArt → Run workflow.** Al termine, in fondo alla pagina dell'esecuzione, sotto *Artifacts*, trovi un pacchetto con dentro tre file:

- `DeadArt-1.0.0-1001.aab` — **questo è quello da caricare sul Play Store**
- `DeadArt-1.0.0-1001.apk` — versione firmata, per installarla a mano o mandarla a qualcuno
- `DeadArt-prova-1.0.0-1001.apk` — versione di prova

Se il file `.aab` non c'è, significa che i segreti non sono impostati bene: guarda il passaggio *Ricrea il keystore* nel registro, te lo dice.

## 5. La scheda del Play Store

### Nome dell'app (max 30 caratteri)
```
DeadArt
```

### Descrizione breve (max 80 caratteri)
```
Il cantiere delle opere in corso e l'archivio di quelle che non ci sono più.
```

### Descrizione completa (max 4000 caratteri)
```
DeadArt è un taccuino da studio per chi dipinge, disegna, scolpisce o costruisce cose con le mani.

IL CANTIERE
Apri una scheda quando cominci un lavoro e fotografalo ogni volta che cambia. Ogni scatto diventa una fase, con la sua data, il suo nome, i minuti che ci hai messo, cosa è cambiato e le tue note. Puoi aggiungere una registrazione vocale a ogni fase: due parole dette davanti alla tela valgono più di dieci scritte dopo. Alla fine non hai solo il quadro: hai la sua storia, e il conto esatto delle ore che è costato.

L'ARCHIVIO
Le tele distrutte, le prove, le fasi coperte da altre fasi, i disegni buttati, i titoli respinti. Tutto quello che un portfolio non mostra e senza cui l'opera non sarebbe nata. Ogni scheda registra che fine ha fatto il lavoro, quanto è sopravvissuto, dove si trovava, e sotto quale altra opera è finito. Un'opera del cantiere che muore passa nell'archivio portandosi dietro tutte le sue fasi.

LA DECANTAZIONE
Quando archivi un lavoro scartato, l'app non ti chiede subito perché l'hai fatto: in quel momento sei ancora dentro la rabbia o la fretta, e diresti una scusa. Passano sette giorni. Poi ti fa una domanda sola: cosa è rimasto? Rispondi con una parola e con cinque secondi di voce. Quella parola diventa il nome della scheda, e l'archivio si legge come un elenco di parole. Se non rispondi entro il giorno dopo, resta scritto "dimenticata": anche non ricordare è un'informazione su di te.

ESPORTAZIONE IN PDF
Dal cantiere, dall'archivio o dalla singola opera, con le immagini, le date e tutti i dati. Un documento che si apre su qualsiasi computer, si stampa e si manda a una galleria o a un critico. Il PDF di una singola opera è la sua monografia: copertina e una pagina per ogni fase.

TUTTO RESTA SUL TUO TELEFONO
Nessun account, nessun server, nessun cookie, nessuna pubblicità, nessun acquisto, nessun dato che esce dal dispositivo. L'app funziona anche senza connessione.

DeadArt è un'app libera realizzata da Maurizio D'Andrea, artista internazionale, e distribuita senza assistenza né manutenzione.
```

### Categoria
**Arte e design**. (In alternativa *Produttività*, ma la prima è più giusta.)

### Tag
arte, pittura, studio, archivio, catalogo, artista, atelier

### Contatto
Email: `dandreart.info@gmail.com`
Sito: `https://dandreart.info`
Informativa privacy: `https://TUONOME.github.io/TUOREPO/privacy.html`

---

## 6. Il modulo Data safety

Google lo chiede e va compilato con attenzione. Nel tuo caso è semplice, perché è tutto no.

- **L'app raccoglie o condivide dati utente richiesti?** → **No**
- **I dati sono criptati in transito?** → non applicabile, non c'è transito
- **Gli utenti possono chiedere la cancellazione dei dati?** → **Sì**, dalla schermata Info, pulsante «cancella tutto», o disinstallando l'app

Se ti chiede conferma sui permessi: fotocamera, microfono e file servono al funzionamento sul dispositivo e **non** producono raccolta di dati, perché niente viene trasmesso.

## 7. Classificazione dei contenuti

Questionario rapido. Le risposte sono tutte **no**: niente violenza, sesso, linguaggio volgare, sostanze, gioco d'azzardo, contenuti generati dagli utenti condivisi pubblicamente, condivisione di posizione, acquisti. Ne esce PEGI 3 / Tutti.

## 8. Le immagini della scheda

Nella cartella `playstore/` trovi già pronti:

- `icona-512x512.png` — icona dell'app
- `grafica-in-evidenza-1024x500.png` — la banda in alto nella scheda

**Le schermate le devi fare tu**, e sono obbligatorie: minimo 2, meglio 4-6, formato telefono. Suggerimento su cosa fotografare, in quest'ordine:

1. Il cantiere con tre o quattro opere in corso
2. Un'opera aperta con la fila delle fasi e le date rosse
3. Il modulo di una nuova fase, con i minuti di lavoro
4. La schermata della decantazione con la domanda
5. L'archivio con le parole grandi
6. Una pagina del PDF esportato

Riempi l'app di contenuti veri prima di fotografarla: le schermate vuote fanno sembrare l'app vuota.

## 9. Il test chiuso obbligatorio

Gli account sviluppatore **personali** aperti dopo il 13 novembre 2023 devono completare un test chiuso prima di poter pubblicare: almeno **12 tester** iscritti in modo continuativo per **14 giorni**. Gli account **organizzazione** sono esenti.

In pratica: crea una release in *Test chiuso*, aggiungi 12 indirizzi Gmail veri (amici, colleghi, artisti), mandagli il link, e devono accettare l'invito e installare davvero. Dopo due settimane si sblocca la richiesta di accesso alla produzione.

Mettilo in conto: tra test e revisione passano tre settimane buone. Nel frattempo l'app funziona già da GitHub Pages, installabile dal menù di Chrome, e l'APK firmato si può mandare a chiunque.

## 10. Prima di premere Pubblica

- [ ] `deadart.keystore` salvato in due posti diversi
- [ ] I quattro segreti impostati su GitHub
- [ ] Il file `.aab` compare tra gli artifacts
- [ ] `privacy.html` raggiungibile e con l'email giusta dentro
- [ ] L'app provata sul telefono: scatto, galleria, voce, PDF, condivisione
- [ ] Almeno quattro schermate fatte con l'app piena di contenuti
- [ ] Identificativo del pacchetto: `info.dandreart.deadart` — **non cambierà mai più**

---

*DeadArt — Maurizio D'Andrea — app libera, senza assistenza né manutenzione.*
