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
            FXMLLoader loader = new FXMLLoader(App.class.getResource("login-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 420, 340);
            scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
            stage.setTitle("Sign In");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Не вдалося запустити застосунок: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}