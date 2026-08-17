const CACHE = 'deadart-v17';
const SCAMBIO = 'deadart-condiviso';
const FILES = ['./', './index.html', './privacy.html', './manifest.json',
  './icons/icon-192.png', './icons/icon-512.png', './icons/icon-512-maskable.png'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(caches.keys()
    .then(k => Promise.all(k.filter(x => x !== CACHE && x !== SCAMBIO).map(x => caches.delete(x))))
    .then(() => self.clients.claim()));
});

// Quando l'utente condivide qualcosa verso DeadArt, Android manda qui una POST.
// Il contenuto si mette da parte in una cache, poi si apre l'app che lo raccoglie.
async function raccogliCondivisione(request) {
  const dati = await request.formData();
  const cache = await caches.open(SCAMBIO);
  const testo = {
    titolo: dati.get('titolo') || '',
    testo: dati.get('testo') || '',
    indirizzo: dati.get('indirizzo') || ''
  };
  await cache.put('/__condiviso.json', new Response(JSON.stringify(testo)));
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
  e.respondWith(
    caches.match(e.request).then(hit => hit || fetch(e.request).then(res => {
      const copy = res.clone();
      caches.open(CACHE).then(c => c.put(e.request, copy)).catch(() => {});
      return res;
    }).catch(() => caches.match('./index.html')))
  );
});
