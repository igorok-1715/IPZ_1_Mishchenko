
package com.example.beauty;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class HistoryController {

    @FXML
    private TableView<Payment> historyTable;

    @FXML
    private TableColumn<Payment, String> typeColumn;

    @FXML
    private TableColumn<Payment, String> masterColumn;

    @FXML
    private TableColumn<Payment, String> detailsColumn;

    @FXML
    private TableColumn<Payment, Double> amountColumn;

    @FXML
    private TableColumn<Payment, String> dateTimeColumn;

    @FXML
    public void initialize() {

        // Назва послуги
        typeColumn.setCellValueFactory(
                new PropertyValueFactory<>("type")
        );

        // Ім'я майстра
        masterColumn.setCellValueFactory(
                new PropertyValueFactory<>("master")
        );

        // Додаткові побажання клієнта
        detailsColumn.setCellValueFactory(
                new PropertyValueFactory<>("details")
        );

        // Вартість послуги
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );

        // Запланована дата та час візиту
        dateTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("formattedDateTime")
        );

        // Завантаження історії поточного користувача
        historyTable.setItems(
                FXCollections.observableArrayList(
                        UserStore.getHistory()
                )
        );

        // Повідомлення, якщо історія порожня
        historyTable.setPlaceholder(
                new Label("Історія візитів відсутня")
        );

        historyTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );
    }

    // Повернення до головного вікна
    @FXML
    private void onBack(ActionEvent event) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("main-view.fxml")
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
            stage.setTitle("Запис до салону краси");
            stage.sizeToScene();
            stage.show();

        } catch (IOException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Помилка");
            alert.setHeaderText("Не вдалося відкрити головне вікно");
            alert.setContentText(e.getMessage());

            alert.showAndWait();
        }
    }
}
