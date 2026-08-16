package info.dandreart.deadart;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * DeadArt — salvataggio e apertura file con le finestre di sistema di Android.
 * Il browser dentro l'app non puo' scaricare da solo: qui si usa
 * ACTION_CREATE_DOCUMENT ("Salva con nome") e ACTION_OPEN_DOCUMENT ("Apri").
 */
@CapacitorPlugin(name = "DeadArtFile")
public class DeadArtFilePlugin extends Plugin {

    /** Salva dati (base64) chiedendo all'utente dove metterli. */
    @PluginMethod
    public void salva(PluginCall call) {
        String base64 = call.getString("base64");
        String fileName = call.getString("fileName", "deadart");
        String mime = call.getString("mime", "application/octet-stream");
        if (base64 == null || base64.isEmpty()) { call.reject("Nessun dato da salvare"); return; }

        call.setKeepAlive(true);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, safeName(fileName));
        startActivityForResult(call, intent, "salvaRisultato");
    }

    @ActivityCallback
    private void salvaRisultato(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK
                || result.getData() == null || result.getData().getData() == null) {
            JSObject ret = new JSObject(); ret.put("annullato", true); call.resolve(ret); return;
        }
        Uri destinazione = result.getData().getData();
        try (OutputStream out = getContext().getContentResolver().openOutputStream(destinazione, "w")) {
            if (out == null) throw new Exception("Impossibile aprire la destinazione");
            String base64 = call.getString("base64", "");
            int virgola = base64.indexOf(',');
            if (base64.startsWith("data:") && virgola > 0) base64 = base64.substring(virgola + 1);
            out.write(Base64.decode(base64, Base64.DEFAULT));
            out.flush();
            JSObject ret = new JSObject();
            ret.put("annullato", false);
            ret.put("uri", destinazione.toString());
            ret.put("nome", queryName(destinazione));
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Salvataggio non riuscito: " + e.getMessage(), e);
        }
    }

    /** Apre un file di testo (la copia .json) e ne restituisce il contenuto. */
    @PluginMethod
    public void apri(PluginCall call) {
        call.setKeepAlive(true);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"application/json", "text/plain", "application/octet-stream"});
        startActivityForResult(call, intent, "apriRisultato");
    }

    @ActivityCallback
    private void apriRisultato(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK
                || result.getData() == null || result.getData().getData() == null) {
            JSObject ret = new JSObject(); ret.put("annullato", true); call.resolve(ret); return;
        }
        Uri sorgente = result.getData().getData();
        try (InputStream in = getContext().getContentResolver().openInputStream(sorgente);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) throw new Exception("Impossibile leggere il file");
            byte[] buffer = new byte[64 * 1024];
            int letti;
            while ((letti = in.read(buffer)) != -1) out.write(buffer, 0, letti);
            JSObject ret = new JSObject();
            ret.put("annullato", false);
            ret.put("nome", queryName(sorgente));
            ret.put("testo", out.toString(StandardCharsets.UTF_8.name()));
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Apertura non riuscita: " + e.getMessage(), e);
        }
    }


    // ---- salvataggio a blocchi: per i file grossi (PDF, copie) ----
    private OutputStream flussoAperto = null;
    private Uri uriAperto = null;

    @PluginMethod
    public void salvaInizio(PluginCall call) {
        chiudiFlusso();
        String fileName = call.getString("fileName", "deadart");
        String mime = call.getString("mime", "application/octet-stream");
        call.setKeepAlive(true);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, safeName(fileName));
        startActivityForResult(call, intent, "salvaInizioRisultato");
    }

    @ActivityCallback
    private void salvaInizioRisultato(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK
                || result.getData() == null || result.getData().getData() == null) {
            JSObject ret = new JSObject(); ret.put("annullato", true); call.resolve(ret); return;
        }
        try {
            uriAperto = result.getData().getData();
            flussoAperto = getContext().getContentResolver().openOutputStream(uriAperto, "w");
            if (flussoAperto == null) throw new Exception("destinazione non apribile");
            JSObject ret = new JSObject();
            ret.put("annullato", false);
            ret.put("nome", queryName(uriAperto));
            call.resolve(ret);
        } catch (Exception e) {
            chiudiFlusso();
            call.reject("Non riesco ad aprire il file: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void salvaPezzo(PluginCall call) {
        if (flussoAperto == null) { call.reject("Nessun file aperto"); return; }
        try {
            String b64 = call.getString("base64", "");
            int virgola = b64.indexOf(',');
            if (b64.startsWith("data:") && virgola > 0) b64 = b64.substring(virgola + 1);
            flussoAperto.write(Base64.decode(b64, Base64.DEFAULT));
            call.resolve();
        } catch (Exception e) {
            chiudiFlusso();
            call.reject("Scrittura non riuscita: " + e.getMessage(), e);
        }
    }

    @PluginMethod
    public void salvaFine(PluginCall call) {
        try {
            if (flussoAperto != null) { flussoAperto.flush(); }
            JSObject ret = new JSObject();
            ret.put("nome", uriAperto != null ? queryName(uriAperto) : "file");
            chiudiFlusso();
            call.resolve(ret);
        } catch (Exception e) {
            chiudiFlusso();
            call.reject("Chiusura non riuscita: " + e.getMessage(), e);
        }
    }

    private void chiudiFlusso() {
        try { if (flussoAperto != null) flussoAperto.close(); } catch (Exception ignored) {}
        flussoAperto = null; uriAperto = null;
    }

    /** Chiude l'app per davvero, non la manda solo in secondo piano. */
    @PluginMethod
    public void chiudi(PluginCall call) {
        call.resolve();
        getActivity().runOnUiThread(() -> {
            try { getActivity().finishAffinity(); }
            catch (Exception e) { getActivity().finish(); }
        });
    }

    private String queryName(Uri uri) {
        try (Cursor c = getContext().getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) return c.getString(i);
            }
        } catch (Exception ignored) {}
        return "file";
    }

    private String safeName(String v) {
        return v == null ? "deadart" : v.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
