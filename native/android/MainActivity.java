package info.dandreart.deadart;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private static final int COLORE_BARRA = Color.rgb(11, 11, 11);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(DeadArtFilePlugin.class);
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(COLORE_BARRA);

        // Chiede subito microfono e fotocamera: senza, la WebView non registra e non scatta.
        String[] servono = { Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA };
        boolean mancano = false;
        for (String p : servono) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) mancano = true;
        }
        if (mancano) ActivityCompat.requestPermissions(this, servono, 4711);

        // La WebView, di suo, nega ogni richiesta di microfono fatta da getUserMedia.
        // Qui la si autorizza, una volta che Android ha dato il permesso all'app.
        getBridge().getWebView().setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    try { request.grant(request.getResources()); }
                    catch (Exception ignored) { request.deny(); }
                });
            }
        });
    }
}
