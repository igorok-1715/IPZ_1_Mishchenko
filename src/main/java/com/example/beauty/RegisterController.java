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
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void onRegister(ActionEvent event) {
        try {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                throw new IllegalArgumentException("Паролі не співпадають.");
            }
            UserStore.register(usernameField.getText(), passwordField.getText());
            errorLabel.setStyle("-fx-text-fill: #2e7d32;");
            errorLabel.setText("Реєстрація успішна! Натисніть «Back», щоб увійти.");
        } catch (IllegalArgumentException e) {
            errorLabel.setStyle("-fx-text-fill: #c0392b;");
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Sign In");
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Помилка переходу: " + e.getMessage());
        }
    }
}