package com.jtjmpm.mobile;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.jtjmpm.ControllerRotation;
import com.jtjmpm.messages.PlayerMoveMessage;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    private final List<ControllerRotation> playerMove = new ArrayList<>();
    private int activeSpellIndex = -1;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Vibrator vibrator;

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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        int[] buttonIds = {R.id.spellButton1, R.id.spellButton2, R.id.spellButton3, R.id.spellButton4};
        for (int i = 0; i < buttonIds.length; i++) {
            final int spellIndex = i;
            findViewById(buttonIds[i]).setOnTouchListener((view, event) -> handleSpellButtonTouch(view, event, spellIndex));
        }
    }

    private boolean handleSpellButtonTouch(View view, MotionEvent event, int spellIndex) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                playerMove.clear();
                activeSpellIndex = spellIndex;
                startSensor();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopSensor();
                sendMove();
                vibrateOnCast();
                break;
        }
        return false;
    }

    private void startSensor() {
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
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
                    eventValues[2],
                    eventValues[3]
            ));
        }
    }

    private void sendMove() {
        if (playerMove.isEmpty()) return;
        if (activeSpellIndex < 0) return;
        PlayerMoveMessage message = new PlayerMoveMessage(playerMove, activeSpellIndex);
        String json = new Gson().toJson(message);
        GameHandler.getInstance().send(json);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /** Short buzz at the moment the gesture is released — confirms the spell was thrown. */
    private void vibrateOnCast() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(120);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensor();
    }
}
