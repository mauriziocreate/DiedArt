# DeadArt

Il cantiere delle opere in corso e l'archivio delle opere che non ci sono più.

Di **Maurizio D'Andrea**. App libera, distribuita senza manutenzione né assistenza.

---

## Cos'è

**Il cantiere** tiene le opere mentre nascono. Apri una scheda quando cominci e fotografi il lavoro ogni volta che cambia: ogni scatto diventa una fase, con la sua data, il suo nome e la sua nota. Alla fine hai la storia del quadro, non solo il quadro.

**L'archivio** tiene quelle che non ce l'hanno fatta: le tele distrutte, le prove, le fasi coperte da altre fasi, i disegni buttati, i titoli respinti. Un'opera del cantiere che muore passa di là con tutte le sue fasi dietro.

Ogni scheda dell'archivio nasce muta. Per sette giorni non puoi dire perché l'hai scartata, perché nel momento in cui la scarti non lo sai. Al settimo giorno l'app chiede una cosa sola: *cosa è rimasto?* Rispondi con una parola e con cinque secondi di voce. Quella parola diventa il nome con cui la scheda vive nell'archivio. Se non rispondi entro un giorno resta scritto *dimenticata*, ed è un dato anche quello.

Tutto resta sul telefono: nessun account, nessun server, nessun cookie, nessuna pubblicità.

## Come funziona dentro

- **Salvataggio**: IndexedDB sul dispositivo (database `deadart`, store `items`, `media`, `conf`).
- **Immagini**: ricompresse in JPEG restringendo lato lungo e qualità finché il file non scende sotto 1 MB. Miniatura separata a 420 px per la griglia.
- **Scarico**: ogni foto ha un collegamento «scarica», che la salva nei Download con un nome parlante (`titolo-fase-01.jpg`).
- **Voce**: MediaRecorder, salvata dentro lo stesso archivio locale.
- **Copia di sicurezza**: dalla schermata Info, tutto in un unico `.json`.

## File

```
index.html                       l'app intera, un file solo
privacy.html                     informativa, serve al Play Store
manifest.json                    nome, icone, colori
sw.js                            service worker, funziona offline
icons/                           spirale logaritmica, 192 / 512 / 512 maskable
package.json                     dipendenze Capacitor (servono solo alla compilazione)
capacitor.config.json            id pacchetto: info.dandreart.deadart
.github/workflows/build-apk.yml  compila l'APK su GitHub
```

## Metterla online (GitHub Pages)

1. Carica tutti i file nella radice del repository, cartelle comprese.
2. Settings → Pages → Source: `main`, cartella `/ (root)`. Salva.
3. Dopo un minuto risponde su `https://<utente>.github.io/<repo>/`.
4. Aprila da Android in Chrome: il menù propone «Installa app». Da lì funziona già offline.

## Compilare l'APK su GitHub

Il workflow `build-apk.yml` fa tutto da solo a ogni modifica. Usa gli stessi segreti di MAIR GO!, quindi se li hai già impostati in quel repository ti basta rimetterli qui.

**Settings → Secrets and variables → Actions → New repository secret**, quattro voci:

| Nome | Contenuto |
|---|---|
| `KEYSTORE_BASE64` | il file `.keystore` convertito in base64 |
| `MAIR_KEYSTORE_PASSWORD` | password del keystore |
| `MAIR_KEY_ALIAS` | alias della chiave |
| `MAIR_KEY_PASSWORD` | password della chiave |

Se non hai ancora un keystore, se ne crea uno così (una volta sola, sul tuo computer):

```bash
keytool -genkey -v -keystore deadart-release.keystore \
  -alias deadart -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 deadart-release.keystore > keystore.txt
```

Il contenuto di `keystore.txt` va in `KEYSTORE_BASE64`. **Conserva il file `.keystore`**: senza quello non potrai più aggiornare l'app.

Poi: **Actions → Compila APK DeadArt → Run workflow**. Al termine, in fondo alla pagina della esecuzione, trovi `DeadArt-APK` da scaricare e installare sul telefono.

## Portarla sul Play Store

L'APK del workflow serve per provare e per l'installazione diretta. Per il Play Store serve il formato `.aab`: il modo più rapido è **pwabuilder.com** — incolli l'indirizzo di GitHub Pages, poi *Package for stores* → Android → *Generate package*, con package ID `info.dandreart.deadart`.

Nella Play Console, nel modulo *Data safety*, la risposta è sempre la stessa: **nessun dato raccolto, nessun dato condiviso**. Come URL dell'informativa privacy indica `https://<utente>.github.io/<repo>/privacy.html`.

Nota: gli account sviluppatore personali aperti dopo novembre 2023 devono prima completare un test chiuso con almeno 12 tester per 14 giorni consecutivi.

## Prima di pubblicare

Apri `privacy.html` e sostituisci `INSERISCI-QUI-LA-TUA-EMAIL` con il tuo indirizzo: Google richiede un contatto raggiungibile.

## Licenza

Uso libero. Nessuna garanzia, nessuna assistenza.

2026
