package info.dandreart.deadart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.PermissionRequest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BridgeActivity {

    private static final int COLORE_BARRA = Color.rgb(11, 11, 11);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(DeadArtFilePlugin.class);
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(COLORE_BARRA);

        // Microfono e fotocamera: chiesti all'avvio, una volta sola.
        String[] servono = { Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA };
        boolean mancano = false;
        for (String p : servono) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) mancano = true;
        }
        if (mancano) ActivityCompat.requestPermissions(this, servono, 4711);

        // Si estende il client di Capacitor, non lo si sostituisce:
        // altrimenti si perde il selettore file e il ponte con i plugin.
        getBridge().getWebView().setWebChromeClient(new BridgeWebChromeClient(getBridge()) {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    try { request.grant(request.getResources()); }
                    catch (Exception e) { request.deny(); }
                });
            }
        });

        prendiCondivisione(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        prendiCondivisione(intent);
        // la pagina e' gia' aperta: la si avvisa che c'e' qualcosa da ritirare
        if (getBridge() != null && getBridge().getWebView() != null) {
            runOnUiThread(() -> getBridge().getWebView().evaluateJavascript(
                "window.dispatchEvent(new Event('deadart-condiviso'))", null));
        }
    }

    /** Legge quello che un'altra app ha condiviso e lo lascia al plugin. */
    private void prendiCondivisione(Intent intent) {
        if (intent == null) return;
        String azione = intent.getAction();
        if (!Intent.ACTION_SEND.equals(azione) && !Intent.ACTION_SEND_MULTIPLE.equals(azione)) return;

        JSObject dati = new JSObject();
        dati.put("titolo", valore(intent.getStringExtra(Intent.EXTRA_SUBJECT)));
        dati.put("testo",  valore(intent.getStringExtra(Intent.EXTRA_TEXT)));
        dati.put("indirizzo", "");

        List<Uri> immagini = new ArrayList<>();
        if (Intent.ACTION_SEND.equals(azione)) {
            Uri u = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (u != null) immagini.add(u);
        } else {
            ArrayList<Uri> lista = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (lista != null) immagini.addAll(lista);
        }

        JSArray comeDati = new JSArray();
        int quante = 0;
        for (Uri u : immagini) {
            if (quante >= 4) break;
            String dataUrl = inBase64(u);
            if (dataUrl != null) { comeDati.put(dataUrl); quante++; }
        }
        dati.put("immagini", comeDati);

        DeadArtFilePlugin.condivisione = dati;
    }

    private String valore(String s) { return s == null ? "" : s; }

    private String inBase64(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) return null;
            byte[] buffer = new byte[64 * 1024];
            int letti;
            long totale = 0;
            while ((letti = in.read(buffer)) != -1) {
                totale += letti;
                if (totale > 12L * 1024 * 1024) return null; // immagini enormi: si lasciano stare
                out.write(buffer, 0, letti);
            }
            String tipo = getContentResolver().getType(uri);
            if (tipo == null) {
                String est = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                tipo = MimeTypeMap.getSingleton().getMimeTypeFromExtension(est);
            }
            if (tipo == null) tipo = "image/jpeg";
            return "data:" + tipo + ";base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }
}
