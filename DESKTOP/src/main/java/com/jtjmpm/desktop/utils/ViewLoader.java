package com.jtjmpm.desktop.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

import java.io.IOException;
import java.net.URL;

public class ViewLoader {

    public static final double TARGET_WIDTH = 1366.0;
    public static final double TARGET_HEIGHT = 768.0;

    public static Scene loadScaledScene(FXMLLoader loader) throws IOException {
        Parent root = loader.load();

        if (root instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region region = (javafx.scene.layout.Region) root;
            region.setPrefSize(TARGET_WIDTH, TARGET_HEIGHT);
            region.setMinSize(TARGET_WIDTH, TARGET_HEIGHT);
            region.setMaxSize(TARGET_WIDTH, TARGET_HEIGHT);
        }

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(TARGET_WIDTH, TARGET_HEIGHT);
        root.setClip(clip);

        javafx.scene.layout.Pane wrapperPane = new javafx.scene.layout.Pane(root);
        wrapperPane.setStyle("-fx-background-color: #f4f4f4;");

        Scene scene = new Scene(wrapperPane, TARGET_WIDTH, TARGET_HEIGHT);

        wrapperPane.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double scaleX = newBounds.getWidth() / TARGET_WIDTH;
            double scaleY = newBounds.getHeight() / TARGET_HEIGHT;

            Scale scale = new Scale(scaleX, scaleY);
            scale.setPivotX(0);
            scale.setPivotY(0);

            root.getTransforms().setAll(scale);
        });

        return scene;
    }
}
