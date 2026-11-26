
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class MainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ComboBox<String> accountTypeComboBox;

    @FXML
    private ComboBox<String> masterComboBox;

    @FXML
    private DatePicker visitDatePicker;

    @FXML
    private ComboBox<String> visitTimeComboBox;

    @FXML
    private TextField detailsField;

    @FXML
    private TextField amountField;

    @FXML
    private Button payButton;

    @FXML
    private Button minimizeButton;

    // Ціни на послуги різних майстрів
    private final Map<String, Double> masterPrices =
            new HashMap<>();

    @FXML
    public void initialize() {

        // Виведення імені користувача
        String user = UserStore.getCurrentUser();

        if (welcomeLabel != null) {
            welcomeLabel.setText(
                    user != null
                            ? "Вітаємо, " + user + "!"
                            : "Головне вікно"
            );
        }

        // Заповнення списку послуг
        accountTypeComboBox.getItems().addAll(
                "Манікюр",
                "Стрижка",
                "Масаж обличчя",
                "Фарбування волосся",
                "SPA-процедура"
        );

        // Заборона вибору минулих дат
        visitDatePicker.setDayCellFactory(
                picker -> new DateCell() {

                    @Override
                    public void updateItem(
                            LocalDate date, boolean empty) {

                        super.updateItem(date, empty);

                        setDisable(
                                empty || date == null ||
                                        date.isBefore(LocalDate.now())
                        );
                    }
                }
        );

        // Заборона ручного редагування ціни
        amountField.setEditable(false);

        // Оновлення списку майстрів
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
                            masterComboBox.getItems().add(
                                    "Катерина"
                            );
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

        // Автоматичне визначення вартості
        masterComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue != null &&
                            masterPrices.containsKey(newValue)) {

                        amountField.setText(
                                String.valueOf(
                                        masterPrices.get(newValue)
                                )
                        );

                    } else {
                        amountField.clear();
                    }

                    // Оновлення вільного часу майстра
                    updateAvailableTimes();
                }
        );

        // Оновлення часу після зміни дати
        visitDatePicker.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateAvailableTimes();
                }
        );

        // Data Binding кнопки запису
        payButton.disableProperty().bind(
                accountTypeComboBox.valueProperty().isNull()
                        .or(masterComboBox.valueProperty().isNull())
                        .or(visitDatePicker.valueProperty().isNull())
                        .or(visitTimeComboBox.valueProperty().isNull())
                        .or(amountField.textProperty().isEmpty())
        );

        updateAvailableTimes();
    }

    // Формування списку вільного часу
    private void updateAvailableTimes() {

        visitTimeComboBox.setValue(null);
        visitTimeComboBox.getItems().clear();

        String master = masterComboBox.getValue();
        LocalDate date = visitDatePicker.getValue();

        // Спочатку потрібно вибрати майстра та дату
        if (master == null || date == null) {
            visitTimeComboBox.setDisable(true);
            return;
        }

        // Перевірка часу з 09:00 до 18:00
        for (int hour = 9; hour <= 18; hour++) {

            LocalTime time = LocalTime.of(hour, 0);

            LocalDateTime visitDateTime =
                    LocalDateTime.of(date, time);

            // Додаємо лише майбутній та вільний час
            if (visitDateTime.isAfter(LocalDateTime.now())
                    && !UserStore.isMasterBusy(
                    master, visitDateTime)) {

                visitTimeComboBox.getItems().add(
                        time.toString()
                );
            }
        }

        // Якщо вільного часу немає
        if (visitTimeComboBox.getItems().isEmpty()) {

            visitTimeComboBox.setPromptText(
                    "Вільного часу немає"
            );

            visitTimeComboBox.setDisable(true);

        } else {

            visitTimeComboBox.setPromptText(
                    "Оберіть час візиту"
            );

            visitTimeComboBox.setDisable(false);
        }
    }

    // Запис клієнта на процедуру
    @FXML
    private void onPay(ActionEvent event) {

        try {

            String selectedService =
                    accountTypeComboBox.getValue();

            String selectedMaster =
                    masterComboBox.getValue();

            String additionalDetails =
                    detailsField.getText().trim();

            LocalDate selectedDate =
                    visitDatePicker.getValue();

            String selectedTime =
                    visitTimeComboBox.getValue();

            // Перевірка заповнення полів
            if (selectedService == null ||
                    selectedMaster == null ||
                    selectedDate == null ||
                    selectedTime == null) {

                showError("Заповніть усі необхідні поля!");
                return;
            }

            double amount = Double.parseDouble(
                    amountField.getText().trim()
            );

            // Перевірка вартості
            if (!Double.isFinite(amount) || amount <= 0) {

                showError("Некоректна вартість послуги!");
                return;
            }

            LocalTime time =
                    LocalTime.parse(selectedTime);

            LocalDateTime visitDateTime =
                    LocalDateTime.of(selectedDate, time);

            // Перевірка дати та часу
            if (!visitDateTime.isAfter(LocalDateTime.now())) {

                showError(
                        "Не можна записатися на минулу дату або час!"
                );

                updateAvailableTimes();
                return;
            }

            // Перевірка зайнятості майстра
            if (UserStore.isMasterBusy(
                    selectedMaster, visitDateTime)) {

                showError(
                        "Майстер зайнятий! Оберіть інший час."
                );

                updateAvailableTimes();
                return;
            }

            // Створення запису на процедуру
            Payment payment = new Payment(
                    selectedService,
                    selectedMaster,
                    additionalDetails,
                    amount,
                    visitDateTime
            );

            // Збереження запису в історії
            UserStore.addPayment(payment);

            // Повідомлення про успішний запис
            Alert alert = new Alert(
                    Alert.AlertType.INFORMATION
            );

            alert.setTitle("Запис на процедуру");
            alert.setHeaderText("Ви успішно записалися!");

            alert.setContentText(
                    "Послуга: " + selectedService +
                            "\nМайстер: " + selectedMaster +
                            "\nДата і час: " +
                            payment.getFormattedDateTime() +
                            "\nВартість: " + amount + " грн"
            );

            alert.showAndWait();

            // Очищення полів після запису
            accountTypeComboBox.setValue(null);
            masterComboBox.setValue(null);
            masterComboBox.getItems().clear();

            visitDatePicker.setValue(null);
            visitTimeComboBox.setValue(null);

            detailsField.clear();
            amountField.clear();

            updateAvailableTimes();

        } catch (NumberFormatException e) {

            showError("Некоректна сума!");

        } catch (Exception e) {

            showError(
                    "Помилка при записі: " + e.getMessage()
            );
        }
    }

    // Відображення історії візитів
    @FXML
    private void onShowHistory(ActionEvent event) {

        try {

            switchScene(
                    event,
                    "history-view.fxml",
                    "Історія візитів"
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
        stage.sizeToScene();
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
