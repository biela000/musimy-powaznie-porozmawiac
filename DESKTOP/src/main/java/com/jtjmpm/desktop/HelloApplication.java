package com.jtjmpm.desktop;

import com.jtjmpm.desktop.service.GameSocketService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private GameSocketService server;
    @Override
    public void start(Stage stage) throws IOException {
        server = new GameSocketService(8080);
        server.start();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> {
            try {
                server.stop();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
}
