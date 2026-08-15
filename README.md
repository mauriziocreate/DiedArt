# DeadArt 1.0

L'archivio delle opere che non ci sono più.

Di **Maurizio D'Andrea**. App libera, distribuita senza manutenzione né assistenza.

---

## Cos'è

DeadArt raccoglie le tele distrutte, le prove, le fasi coperte da altre fasi, i disegni buttati, i titoli respinti, le registrazioni fatte mentre lavoravi. Tutto quello che un portfolio non mostra e senza cui l'opera non sarebbe nata.

Ogni scheda nasce muta. Per sette giorni non puoi dire perché l'hai scartata, perché nel momento in cui la scarti non lo sai. Al settimo giorno l'app chiede una cosa sola: *cosa è rimasto?* Rispondi con una parola e con cinque secondi di voce. Quella parola diventa il nome con cui la scheda vive nell'archivio. Se non rispondi entro un giorno resta scritto *dimenticata*, ed è un dato anche quello.

Tutto resta sul telefono: foto, note, registrazioni. Nessun account, nessun server, nessuna pubblicità, nessun acquisto.

## Come funziona dentro

- **Salvataggio**: IndexedDB sul dispositivo (database `deadart`, store `items`, `media`, `conf`). Nessuna rete.
- **Immagini**: ricompresse in JPEG restringendo lato lungo e qualità finché il file non scende sotto 1 MB. Miniatura separata a 420 px per la griglia.
- **Voce**: MediaRecorder, salvata come data URL webm dentro lo stesso store.
- **Copia di sicurezza**: dalla schermata Info, esporta tutto (schede + immagini + audio) in un unico `.json`.

## File

```
index.html      l'app intera, un file solo
manifest.json   nome, icone, colori per l'installazione
sw.js           service worker, funziona offline
icons/          spirale logaritmica, 192 / 512 / 512 maskable
```

## Metterla online (GitHub Pages)

1. Crea un repository, per esempio `deadart`, e carica questi file nella radice.
2. Settings → Pages → Source: `main`, cartella `/ (root)`. Salva.
3. Dopo un minuto risponde su `https://<utente>.github.io/deadart/`.
4. Aprila da Android in Chrome: il menù propone «Installa app». Da lì funziona già offline.

Se la metti in una sottocartella, i percorsi relativi (`./index.html`, `sw.js`) funzionano lo stesso: non c'è niente da cambiare.

## Portarla sul Play Store (Bubblewrap)

Serve Node 18+ e un JDK.

```bash
npm i -g @bubblewrap/cli
bubblewrap init --manifest https://<utente>.github.io/deadart/manifest.json
bubblewrap build
```

Alla prima esecuzione crea la chiave di firma: **conservala**, senza quella non puoi più aggiornare l'app.

Poi:

1. Copia il `sha256` della chiave (`bubblewrap fingerprint list`).
2. Crea il file `.well-known/assetlinks.json` nella radice del sito, con quel fingerprint e il nome pacchetto, per togliere la barra del browser dall'app.
3. Su Google Play Console: crea l'app, carica l'`.aab`, compila la scheda e il modulo *Data safety*.

Nel modulo Data safety la risposta è sempre la stessa: l'app **non raccoglie e non condivide alcun dato**, tutto resta sul dispositivo. Serve comunque un'informativa privacy raggiungibile da un URL pubblico: una pagina di tre righe che dica esattamente questo è sufficiente.

## Nota sui caratteri

I font (Fraunces, IBM Plex Mono) arrivano da Google Fonts. Senza rete l'app usa i caratteri di sistema e resta perfettamente leggibile. Se vuoi che l'aspetto sia identico anche offline, scarica i `.woff2` in una cartella `fonts/`, sostituisci il `<link>` con un `@font-face`, e aggiungi i file all'elenco `FILES` dentro `sw.js`.

## Licenza

Uso libero. Nessuna garanzia, nessuna assistenza.

2026
