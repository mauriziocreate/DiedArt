package info.dandreart.deadart;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.PermissionRequest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;

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

        // IMPORTANTE: si estende il client di Capacitor, non lo si sostituisce.
        // Sostituendolo si perde il selettore file (galleria e scatto) e il ponte con i plugin.
        // Qui si aggiunge soltanto il consenso alle richieste di microfono fatte da getUserMedia.
        getBridge().getWebView().setWebChromeClient(new BridgeWebChromeClient(getBridge()) {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    try { request.grant(request.getResources()); }
                    catch (Exception e) { request.deny(); }
                });
            }
        });
    }
}
