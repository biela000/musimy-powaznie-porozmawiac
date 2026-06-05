package com.jtjmpm.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class StartActivity extends AppCompatActivity {
    private final static String IP_ADDRESS_REGEX = "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):\\d{1,5}\\b";
    private EditText IPAddressInput;
    private Button connectButton;
    private Button scanButton;

    private final ActivityResultLauncher<ScanOptions> qrScanner = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    IPAddressInput.setText(result.getContents());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setListeners();
    }

    private void init() {
        IPAddressInput = findViewById(R.id.IPAddressInput);
        connectButton = findViewById(R.id.connectButton);
        scanButton = findViewById(R.id.scanButton);

        IPAddressInput.setInputType(InputType.TYPE_CLASS_PHONE);
        IPAddressInput.setFilters(new InputFilter[]{new IpAddressFilter(), new InputFilter.LengthFilter(21)});
    }

    private void setListeners() {
        connectButton.setOnClickListener(this::handleConnectButtonClick);
        scanButton.setOnClickListener(this::handleScanButtonClick);
    }

    private void handleConnectButtonClick(View view) {
        GameHandler gameHandler = GameHandler.getInstance();
        String socketServerUrl = IPAddressInput.getText().toString();

        if (!isIPAddressValid(socketServerUrl)) {
            IPAddressInput.setError("Must be a valid IPv4 address");
            return;
        }

        gameHandler.connect(socketServerUrl, this::goToGameActivity, this::handleConnectionError);
    }

    private void handleScanButtonClick(View view) {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan server IP QR code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        qrScanner.launch(options);
    }

    private boolean isIPAddressValid(String url) {
        return url.matches(IP_ADDRESS_REGEX);
    }

    private void goToGameActivity() {
        runOnUiThread(() -> {
            Intent intent = new Intent(StartActivity.this, GameActivity.class);
            startActivity(intent);
        });
    }

    private void handleConnectionError() {
        runOnUiThread(() -> IPAddressInput.setError("There was problem connecting to this IP"));
    }

    private static class IpAddressFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (!Character.isDigit(c) && c != '.' && c != ':') {
                    return "";
                }
            }
            return null;
        }
    }
}
