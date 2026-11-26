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

    @FXML
    private void onSignIn(ActionEvent event) {
        try {
            UserStore.login(usernameField.getText(), passwordField.getText());
            errorLabel.setText("");
            switchScene(event, "main-view.fxml", "Оплата послуг салону краси");
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (IOException e) {
            errorLabel.setText("Не вдалося відкрити головне вікно: " + e.getMessage());
        }
    }

    @FXML
    private void onGoToRegister(ActionEvent event) {
        try {
            switchScene(event, "register-view.fxml", "Register");
        } catch (IOException e) {
            errorLabel.setText("Не вдалося відкрити вікно реєстрації: " + e.getMessage());
        }
    }

    private void switchScene(ActionEvent event, String fxml, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }
}