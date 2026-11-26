package com.example.beauty;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    // Авторизація користувача через HTTP-сервер
    @FXML
    private void onSignIn(ActionEvent event) {

        try {

            ApiClient.login(
                    usernameField.getText(),
                    passwordField.getText()
            );

            errorLabel.setText("");

            switchScene(
                    event,
                    "main-view.fxml",
                    "Запис до салону краси"
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            errorLabel.setText(e.getMessage());

        } catch (IOException e) {

            e.printStackTrace();

            try {
                ApiClient.logout();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }

            errorLabel.setText(
                    "Не вдалося відкрити головне вікно!"
            );
        }
    }

    // Перехід до вікна реєстрації
    @FXML
    private void onGoToRegister(ActionEvent event) {

        try {

            switchScene(
                    event,
                    "register-view.fxml",
                    "Реєстрація"
            );

        } catch (IOException e) {

            e.printStackTrace();

            errorLabel.setText(
                    "Не вдалося відкрити вікно реєстрації!"
            );
        }
    }

    // Перехід між вікнами
    private void switchScene(
            ActionEvent event,
            String fxml,
            String title
    ) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource(fxml)
        );

        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                App.class.getResource("styles.css")
                        .toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle(title);
        stage.sizeToScene();
        stage.show();
    }
}