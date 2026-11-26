package com.example.beauty;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private TableColumn<Payment, String> detailsColumn;
    @FXML
    private TableColumn<Payment, Double> amountColumn;

    @FXML
    public void initialize() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        historyTable.setItems(FXCollections.observableArrayList(UserStore.getHistory()));
    }

    @FXML
    private void onBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("main-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Оплата послуг салону краси");
            stage.show();
        } catch (IOException e) {
            // Показуємо помилку переходу через Alert, щоб не втрачати try/catch для файлових операцій
        }
    }
}