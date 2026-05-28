package com.jtjmpm.api.game.game_logic;

import com.jtjmpm.ControllerRotation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RotationVectorParser {

    private List<Point2D.Double> currentPath;
    private boolean isRecording;

    private float startYaw = Float.NaN;
    private float startPitch = Float.NaN;

    private float yawOffset = 0f;
    private float lastRawYaw = Float.NaN;

    public RotationVectorParser() {
        this.currentPath = new ArrayList<>();
        this.isRecording = false;
    }

    private void startRecording() {
        currentPath.clear();
        startYaw = Float.NaN;
        startPitch = Float.NaN;
        lastRawYaw = Float.NaN;
        yawOffset = 0f;
        isRecording = true;
    }

    private void processRawSensorValues(float[] sensorValues) {
        if (!isRecording || sensorValues == null || sensorValues.length < 4) return;
        float x = sensorValues[0];
        float y = sensorValues[1];
        float z = sensorValues[2];
        float w = sensorValues[3];

        float r01 = 2.0f * (x * y - w * z);
        float r11 = 1.0f - 2.0f * (x * x + z * z);
        float r21 = 2.0f * (y * z + w * x);

        float rawYaw = (float) Math.atan2(r01, r11);
        float rawPitch = (float) Math.asin(-r21);

        if (!Float.isNaN(lastRawYaw)) {
            float deltaRawYaw = rawYaw - lastRawYaw;
            if (deltaRawYaw < -Math.PI) {
                yawOffset += (float) (2 * Math.PI);
            } else if (deltaRawYaw > Math.PI) {
                yawOffset -= (float) (2 * Math.PI);
            }
        }
        lastRawYaw = rawYaw;
        float continuousYaw = rawYaw + yawOffset;

        if (Float.isNaN(startYaw)) {
            startYaw = continuousYaw;
            startPitch = rawPitch;
        }

        double finalX = continuousYaw - startYaw;
        double finalY = -(rawPitch - startPitch);

        currentPath.add(new Point2D.Double(finalX, finalY));
    }

    private List<Point2D.Double> stopRecording() {
        isRecording = false;
        return new ArrayList<>(currentPath);
    }

    public List<Point2D.Double> processBatch(List<ControllerRotation> rawDataList) {
        startRecording();
        if (rawDataList != null) {
            for (ControllerRotation sensorValues : rawDataList) {
                float[] point = new float[4];
                point[0] = sensorValues.x;
                point[1] = sensorValues.y;
                point[2] = sensorValues.z;
                point[3] = sensorValues.scalar;
                processRawSensorValues(point);
            }
        }

        return stopRecording();
    }
}
