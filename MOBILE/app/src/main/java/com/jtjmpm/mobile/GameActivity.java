package com.jtjmpm.mobile;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.jtjmpm.ControllerRotation;
import com.jtjmpm.PlayerMoveMessage;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    private Button drawButton;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private final List<ControllerRotation> playerMove = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        addListeners();
    }

    private void init() {
        drawButton = findViewById(R.id.drawButton);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }

    private void addListeners() {
        drawButton.setOnTouchListener(this::handleDrawButtonTouch);
    }

    private boolean handleDrawButtonTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                playerMove.clear();
                startSensor();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopSensor();
                sendMove();
                break;
        }

        return false;
    }

    private void startSensor() {
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void stopSensor() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            float[] eventValues = event.values.clone();
            playerMove.add(new ControllerRotation(
                    eventValues[0],
                    eventValues[1],
                    eventValues[2]
            ));
        }
    }

    private void sendMove() {
        if (playerMove.isEmpty()) return;
        PlayerMoveMessage message = new PlayerMoveMessage(playerMove);
        String json = new Gson().toJson(message);
        GameHandler.getInstance().send(json);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        stopSensor();
    }
}