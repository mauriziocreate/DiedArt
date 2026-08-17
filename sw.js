/* DeadArt — service worker
   I file dell'app si prendono sempre dalla rete quando c'è: così un aggiornamento
   arriva subito e non resti mai con la versione vecchia. Le immagini e i caratteri
   restano in cache. Senza rete, funziona tutto lo stesso.                        */
const VERSIONE = '1.1.0';
const CACHE = 'deadart-' + VERSIONE;
const SCAMBIO = 'deadart-condiviso';
const FILES = ['./', './index.html', './privacy.html', './manifest.json',
  './icons/icon-192.png', './icons/icon-512.png', './icons/icon-512-maskable.png'];

// i file che devono essere sempre freschi
const SEMPRE_FRESCHI = ['/index.html', '/manifest.json', '/privacy.html', '/'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(caches.keys()
    .then(k => Promise.all(k.filter(x => x !== CACHE && x !== SCAMBIO).map(x => caches.delete(x))))
    .then(() => self.clients.claim()));
});

// L'app può chiedere di passare subito alla versione nuova
self.addEventListener('message', e => {
  if (e.data === 'aggiorna-adesso') self.skipWaiting();
});

// Quando si condivide qualcosa verso DeadArt, Android manda qui una POST:
// il contenuto si mette da parte, poi si apre l'app che lo raccoglie.
async function raccogliCondivisione(request) {
  const dati = await request.formData();
  const cache = await caches.open(SCAMBIO);
  await cache.put('/__condiviso.json', new Response(JSON.stringify({
    titolo: dati.get('titolo') || '',
    testo: dati.get('testo') || '',
    indirizzo: dati.get('indirizzo') || ''
  })));
  const immagini = dati.getAll('immagini').filter(f => f && f.size);
  await cache.put('/__condiviso-quante', new Response(String(immagini.length)));
  for (let n = 0; n < immagini.length && n < 4; n++) {
    await cache.put('/__condiviso-img-' + n, new Response(immagini[n]));
  }
  return Response.redirect('./index.html?condiviso=1', 303);
}

self.addEventListener('fetch', e => {
  const url = new URL(e.request.url);

  if (e.request.method === 'POST' && url.pathname.endsWith('/condividi')) {
    e.respondWith(raccogliCondivisione(e.request));
    return;
  }
  if (e.request.method !== 'GET') return;

  const fresco = SEMPRE_FRESCHI.some(p => url.pathname.endsWith(p)) || url.pathname.endsWith('/sw.js');

  if (fresco) {
    // prima la rete, la cache solo se non c'è linea
    e.respondWith(
      fetch(e.request).then(res => {
        const copia = res.clone();
        caches.open(CACHE).then(c => c.put(e.request, copia)).catch(() => {});
        return res;
      }).catch(() => caches.match(e.request).then(hit => hit || caches.match('./index.html')))
    );
    return;
  }

  // tutto il resto: prima la cache, è roba che non cambia
  e.respondWith(
    caches.match(e.request).then(hit => hit || fetch(e.request).then(res => {
      const copia = res.clone();
      caches.open(CACHE).then(c => c.put(e.request, copia)).catch(() => {});
      return res;
    }).catch(() => caches.match('./index.html')))
  );
});
