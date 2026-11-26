package com.example.beauty;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        try {
            Database.initialize();

            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("login-view.fxml")
            );

            Parent root = loader.load();

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    App.class.getResource("styles.css")
                            .toExternalForm()
            );

            stage.setTitle("Авторизація");
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();

        } catch (IOException e) {

            System.err.println(
                    "Не вдалося запустити застосунок:"
            );

            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}