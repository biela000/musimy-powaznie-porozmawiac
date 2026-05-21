package com.jtjmpm.mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StartActivity extends AppCompatActivity {
    private final static String IP_ADDRESS_REGEX = "^(((?!25?[6-9])[12]\\d|[1-9])?\\d\\.?\\b){4}$";
    private EditText IPAddressInput;
    private Button connectButton;

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
    }

    private void setListeners() {
        connectButton.setOnClickListener(this::handleConnectButtonClick);
    }

    private void handleConnectButtonClick(View view) {
        GameHandler gameHandler = GameHandler.getInstance();
        String socketServerUrl = IPAddressInput.getText().toString();

        if (!isIPAddressValid(socketServerUrl)) {
            IPAddressInput.setError("Must be a valid IPv4 address");
            return;
        }

        gameHandler.connect(socketServerUrl);
    }

    private boolean isIPAddressValid(String url) {
        return url.matches(IP_ADDRESS_REGEX);
    }
}