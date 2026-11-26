
package com.example.beauty;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ComboBox<String> accountTypeComboBox;

    @FXML
    private ComboBox<String> masterComboBox;

    @FXML
    private TextField detailsField;

    @FXML
    private TextField amountField;

    @FXML
    private Button payButton;

    @FXML
    private Button minimizeButton;

    // Ціни на послуги різних майстрів
    private final Map<String, Double> masterPrices = new HashMap<>();

    @FXML
    public void initialize() {

        // Виведення імені користувача
        String user = UserStore.getCurrentUser();

        if (welcomeLabel != null) {
            welcomeLabel.setText(
                    user != null ? "Вітаємо, " + user + "!" : "Головне вікно"
            );
        }

        // Заповнення списку послуг
        if (accountTypeComboBox != null) {

            accountTypeComboBox.getItems().addAll(
                    "Манікюр",
                    "Стрижка",
                    "Масаж обличчя",
                    "Фарбування волосся",
                    "SPA-процедура"
            );

            // Оновлення майстрів залежно від послуги
            accountTypeComboBox.valueProperty().addListener(
                    (observable, oldValue, newValue) -> {

                        masterComboBox.setValue(null);
                        masterComboBox.getItems().clear();
                        masterPrices.clear();
                        amountField.clear();

                        if (newValue == null) {
                            return;
                        }

                        switch (newValue) {

                            case "Манікюр":
                                masterComboBox.getItems().addAll(
                                        "Олена", "Ірина"
                                );
                                masterPrices.put("Олена", 500.0);
                                masterPrices.put("Ірина", 600.0);
                                break;

                            case "Стрижка":
                                masterComboBox.getItems().addAll(
                                        "Андрій", "Максим"
                                );
                                masterPrices.put("Андрій", 700.0);
                                masterPrices.put("Максим", 900.0);
                                break;

                            case "Масаж обличчя":
                                masterComboBox.getItems().add("Катерина");
                                masterPrices.put("Катерина", 800.0);
                                break;

                            case "Фарбування волосся":
                                masterComboBox.getItems().addAll(
                                        "Світлана", "Андрій"
                                );
                                masterPrices.put("Світлана", 1200.0);
                                masterPrices.put("Андрій", 1400.0);
                                break;

                            case "SPA-процедура":
                                masterComboBox.getItems().addAll(
                                        "Олена", "Катерина"
                                );
                                masterPrices.put("Олена", 1500.0);
                                masterPrices.put("Катерина", 1800.0);
                                break;
                        }
                    }
            );
        }

        // Автоматичне визначення вартості послуги
        if (masterComboBox != null) {

            masterComboBox.valueProperty().addListener(
                    (observable, oldValue, newValue) -> {

                        if (newValue != null &&
                                masterPrices.containsKey(newValue)) {

                            Double price = masterPrices.get(newValue);

                            amountField.setText(
                                    String.valueOf(price)
                            );

                        } else {
                            amountField.clear();
                        }
                    }
            );
        }

        // Data Binding кнопки оплати
        if (payButton != null &&
                accountTypeComboBox != null &&
                masterComboBox != null &&
                amountField != null) {

            payButton.disableProperty().bind(
                    accountTypeComboBox.valueProperty().isNull()
                            .or(masterComboBox.valueProperty().isNull())
                            .or(amountField.textProperty().isEmpty())
            );
        }
    }

    // Оплата послуги
    @FXML
    private void onPay(ActionEvent event) {

        try {

            String selectedService =
                    accountTypeComboBox.getValue();

            String selectedMaster =
                    masterComboBox.getValue();

            String additionalDetails =
                    detailsField.getText().trim();

            double amount = Double.parseDouble(
                    amountField.getText().trim()
            );

            // Перевірка правильності суми
            if (!Double.isFinite(amount) || amount <= 0) {
                throw new IllegalArgumentException(
                        "Сума повинна бути більшою за 0!"
                );
            }

            // Формування опису платежу
            String fullDetails =
                    "Майстер: " + selectedMaster;

            if (!additionalDetails.isEmpty()) {
                fullDetails += " | " + additionalDetails;
            }

            // Створення платежу
            Payment payment = new Payment(
                    selectedService,
                    fullDetails,
                    amount
            );

            // Збереження платежу в історії
            UserStore.addPayment(payment);

            // Повідомлення про успішну оплату
            Alert alert = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alert.setTitle("Payment");
            alert.setHeaderText("Оплата успішна!");

            alert.setContentText(
                    "Послуга: " + selectedService +
                            "\nМайстер: " + selectedMaster +
                            "\nСума: " + amount + " грн"
            );

            alert.showAndWait();

            // Очищення полів
            accountTypeComboBox.setValue(null);
            masterComboBox.setValue(null);
            masterComboBox.getItems().clear();
            detailsField.clear();
            amountField.clear();

        } catch (NumberFormatException e) {

            showError("Введіть правильну суму!");

        } catch (IllegalArgumentException e) {

            showError(e.getMessage());

        } catch (Exception e) {

            showError(
                    "Помилка при оплаті: " + e.getMessage()
            );
        }
    }

    // Відображення історії платежів
    @FXML
    private void onShowHistory(ActionEvent event) {

        try {

            switchScene(
                    event,
                    "history-view.fxml",
                    "Payment History"
            );

        } catch (IOException e) {

            showError(
                    "Не вдалося відкрити історію: " +
                            e.getMessage()
            );
        }
    }

    // Вихід з облікового запису
    @FXML
    private void onLogout(ActionEvent event) {

        try {

            UserStore.logout();

            switchScene(
                    event,
                    "login-view.fxml",
                    "Sign In"
            );

        } catch (IOException e) {

            showError(
                    "Помилка виходу: " + e.getMessage()
            );
        }
    }

    // Перехід між вікнами програми
    private void switchScene(
            ActionEvent event,
            String fxml,
            String title
    ) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource(fxml)
        );

        Parent root = loader.load();

        Stage stage = (Stage) (
                (Node) event.getSource()
        ).getScene().getWindow();

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                App.class.getResource(
                        "styles.css"
                ).toExternalForm()
        );

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    // Відображення повідомлення про помилку
    private void showError(String message) {

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Error");
        alert.setHeaderText("Помилка");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
