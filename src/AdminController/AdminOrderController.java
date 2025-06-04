package AdminController;

import Class.Order;
import Main.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminOrderController {

    @FXML
    private TableView<Order> mytable;

    @FXML private TableColumn<Order, String> orderIdCol;
    @FXML private TableColumn<Order, String> transactionIdCol;
    @FXML private TableColumn<Order, String> productIdCol;
    @FXML private TableColumn<Order, String> studentNumberCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, String> quantityCol;
    @FXML private TableColumn<Order, Double> totalAmountCol;
    @FXML private TableColumn<Order, String> imageCol;

    @FXML private Button deletecustomerbtn;  
    @FXML private TextField searchField;

    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        studentNumberCol.setCellValueFactory(new PropertyValueFactory<>("studentNumber"));
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        imageCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imageCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(imageUrl, 100, 100, true, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        loadOrdersFromDatabase(); // Load initial data

        // SEARCH FUNCTIONALITY (ADDED)
        FilteredList<Order> filteredData = new FilteredList<>(orderList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (order.getOrderId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getTransactionId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getProductId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getStudentNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getOrderDate().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getQuantity().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(order.getTotalAmount()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        mytable.setItems(filteredData);
        // END OF SEARCH FUNCTIONALITY

        // DELETE BUTTON
        deletecustomerbtn.setOnAction(event -> {
            Order selectedOrder = mytable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Deletion");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("Are you sure you want to delete Order ID: " + selectedOrder.getOrderId() + "?");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        deleteOrder(selectedOrder);
                    }
                });
            } else {
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle("No Selection");
                warningAlert.setHeaderText(null);
                warningAlert.setContentText("Please select an order to delete.");
                warningAlert.showAndWait();
            }
        });
    }

    private void loadOrdersFromDatabase() {
        String query = "SELECT order_id, transaction_id, product_id, student_number, order_date, quantity, total_amount, image_url FROM orders";

        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            orderList.clear();

            while (rs.next()) {
                String orderId = rs.getString("order_id");
                String transactionId = rs.getString("transaction_id");
                String productId = rs.getString("product_id");
                String studentNumber = rs.getString("student_number");
                String orderDate = rs.getString("order_date");
                String quantity = rs.getString("quantity");
                double totalAmount = rs.getDouble("total_amount");
                String imageUrl = rs.getString("image_url");

                Order order = new Order(orderId, transactionId, productId, studentNumber, orderDate, quantity, totalAmount, imageUrl);
                orderList.add(order);
            }

            mytable.setItems(orderList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteOrder(Order order) {
        String deleteQuery = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setString(1, order.getOrderId());

            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                orderList.remove(order);
                mytable.refresh();

                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Deleted");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("Order ID " + order.getOrderId() + " deleted successfully.");
                infoAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Deletion Failed");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Failed to delete order with ID: " + order.getOrderId());
                errorAlert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("SQL Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("An error occurred while deleting the order.");
            errorAlert.showAndWait();
        }
    }
}