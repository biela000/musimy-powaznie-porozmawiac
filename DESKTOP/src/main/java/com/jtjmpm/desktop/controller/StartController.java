package com.jtjmpm.desktop.controller;

import com.jtjmpm.desktop.service.ApiSocketClient;
import com.jtjmpm.desktop.service.GameSocketService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class StartController {
    private final static String LOBBY_VIEW = "/com/jtjmpm/desktop/lobby-view.fxml";
    private final static String API_URL = "ws://127.0.0.1:3000";

    @FXML
    private Button startButton;
    @FXML private Label phoneStatusLabel;
    @FXML private Label apiStatusLabel;
    @FXML private ImageView qrCodeImageView;

    private boolean isPhoneConnected = false;
    private boolean isApiConnected = false;

    public void initialize() {
        startButton.setDisable(true);

        GameSocketService.getInstance().setOnClientConnected(() -> {
            isPhoneConnected = true;
            Platform.runLater(this::updateUI);
        });

        GameSocketService.getInstance().setOnClientDisconnected(() -> {
            isPhoneConnected = false;
            Platform.runLater(this::updateUI);
        });

        ApiSocketClient.getInstance().setServerUrl(API_URL);
        ApiSocketClient.getInstance().setOnConnected(() -> {
            isApiConnected = true;
            Platform.runLater(this::updateUI);
        });
        ApiSocketClient.getInstance().connect();

        setupQRCode();
        updateUI();
    }


    private void updateUI() {
        phoneStatusLabel.setText(isPhoneConnected ? "Phone: Connected" : "Phone: Waiting...");
        apiStatusLabel.setText(isApiConnected ? "API: Connected" : "API: Waiting...");

        // Only enable if both are successfully connected
        startButton.setDisable(!(isPhoneConnected && isApiConnected));
    }

    @FXML
    private void onStartClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOBBY_VIEW));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) startButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupQRCode() {
        String ipAddress = getValidLocalIp();
        int port = GameSocketService.getInstance().getPort();

        String qrData = ipAddress + ":" + port;
        generateQRCode(qrData);
    }

    private void generateQRCode(String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int width = 200;
        int height = 200;
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            WritableImage image = new WritableImage(width, height);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.getPixelWriter().setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            qrCodeImageView.setImage(image);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private String getValidLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr.getHostAddress().contains(":")) {
                        continue;
                    }

                    return addr.getHostAddress();
                }
            }
        } catch (SocketException e) {
            System.err.println("Failed to retrieve network interfaces.");
            e.printStackTrace();
        }

        return "127.0.0.1";
    }
}
