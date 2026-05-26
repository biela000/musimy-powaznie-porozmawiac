package com.jtjmpm.desktop;

import com.jtjmpm.desktop.service.ApiSocketClient;
import com.jtjmpm.desktop.service.GameSocketService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private GameSocketService server;

    @Override
    public void start(Stage stage) throws IOException {
        Parameters params = getParameters();
        int port = 8080;
        if(!params.getRaw().isEmpty()){
            try {
                port = Integer.parseInt(params.getRaw().get(0));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        server = new GameSocketService(port);
        server.start();

        // Currently just using localhost but should be changed
        // to an env variable later :)
        //"ws://127.0.0.1:3000" local host
        ApiSocketClient.getInstance().connect("ws://127.0.0.1:3000", () -> {
            System.out.println("API connection established");
        });

        FXMLLoader fxmlLoader = new FXMLLoader(
                Application.class.getResource("lobby-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Game");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> {
            try {
                server.stop();
                ApiSocketClient.getInstance().close();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }
}
