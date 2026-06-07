package com.jtjmpm.desktop;

import com.jtjmpm.desktop.service.ApiSocketClient;
import com.jtjmpm.desktop.service.GameSocketService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private final static String WINDOW_TITLE = "MPP";
    private final static String START_VIEW = "start-view.fxml";

    private int getPortFromParams() {
        Parameters params = getParameters();

        int port = 8080;
        if (!params.getRaw().isEmpty()) {
            try {
                port = Integer.parseInt(params.getRaw().getFirst());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return port;
    }

    @Override
    public void start(Stage stage) throws IOException {
        int port = getPortFromParams();
        GameSocketService.setPort(port);

        GameSocketService server = GameSocketService.getInstance();
        server.start();

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource(START_VIEW));
        Scene scene = com.jtjmpm.desktop.utils.ViewLoader.loadScaledScene(fxmlLoader);
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        stage.setOnCloseRequest(_ -> {
            try {
                server.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
