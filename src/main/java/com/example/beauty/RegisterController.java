
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

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    // Реєстрація нового користувача через HTTP-сервер
    @FXML
    private void onRegister(ActionEvent event) {

        try {

            String username = usernameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Перевірка заповнення полів
            if (username.isEmpty() || phone.isEmpty()
                    || email.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                throw new IllegalArgumentException(
                        "Заповніть усі поля!"
                );
            }

            // Перевірка відповідності паролів
            if (!password.equals(confirmPassword)) {

                throw new IllegalArgumentException(
                        "Паролі не збігаються!"
                );
            }

            // Реєстрація користувача через сервер
            ApiClient.register(
                    username,
                    phone,
                    email,
                    password
            );

            errorLabel.setStyle("-fx-text-fill: #2e7d32;");

            errorLabel.setText(
                    "Реєстрація успішна! Натисніть «Назад», щоб увійти."
            );

            usernameField.clear();
            phoneField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

        } catch (IllegalArgumentException | IllegalStateException e) {

            errorLabel.setStyle("-fx-text-fill: #c0392b;");
            errorLabel.setText(e.getMessage());
        }
    }

    // Повернення до авторизації
    @FXML
    private void onBackToLogin(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("login-view.fxml")
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
            stage.setTitle("Авторизація");
            stage.sizeToScene();
            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            errorLabel.setStyle("-fx-text-fill: #c0392b;");
            errorLabel.setText(
                    "Помилка переходу: " + e.getMessage()
            );
        }
    }
}
