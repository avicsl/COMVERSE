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
    @FXML private TableColumn<Order, String> statusCol;
    @FXML private TableColumn<Order, String> receivedDateCol;

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
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        receivedDateCol.setCellValueFactory(new PropertyValueFactory<>("receivedDate"));

        // Set up image column
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

        // Set up status column with color coding
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("Received".equals(status)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Set up received date column
        receivedDateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String receivedDate, boolean empty) {
                super.updateItem(receivedDate, empty);
                if (empty || receivedDate == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(receivedDate);
                    if ("Not Received".equals(receivedDate)) {
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: blue;");
                    }
                }
            }
        });

        loadOrdersFromDatabase(); // Load initial data

        // SEARCH FUNCTIONALITY (UPDATED TO INCLUDE STATUS AND RECEIVED DATE)
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
                } else if (order.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getReceivedDate().toLowerCase().contains(lowerCaseFilter)) {
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
                confirmAlert.setContentText("Are you sure you want to delete Order ID: " + selectedOrder.getOrderId() + "?\n" +
                        "Status: " + selectedOrder.getStatus() + "\n" +
                        "Student: " + selectedOrder.getStudentNumber());

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
        // Updated query to include user_marked_received and received_date columns
        String query = "SELECT order_id, transaction_id, product_id, student_number, order_date, quantity, total_amount, image_url, " +
                      "COALESCE(user_marked_received, 0) as user_marked_received, " +
                      "DATE_FORMAT(received_date, '%Y-%m-%d %H:%i:%s') as received_date " +
                      "FROM orders ORDER BY order_date DESC, user_marked_received ASC";

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
                boolean userMarkedReceived = rs.getBoolean("user_marked_received");
                String receivedDate = rs.getString("received_date");

                Order order = new Order(orderId, transactionId, productId, studentNumber, orderDate, quantity, totalAmount, imageUrl, userMarkedReceived, receivedDate);
                orderList.add(order);
            }

            mytable.setItems(orderList);
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Error", "Failed to load orders from database: " + e.getMessage());
        }
    }

    private void deleteOrder(Order order) {
        // Show additional confirmation for received orders
        if (order.isUserMarkedReceived()) {
            Alert extraConfirm = new Alert(Alert.AlertType.WARNING);
            extraConfirm.setTitle("Delete Received Order");
            extraConfirm.setHeaderText("This order has been marked as received!");
            extraConfirm.setContentText("Order ID: " + order.getOrderId() + "\n" +
                    "Received Date: " + order.getReceivedDate() + "\n" +
                    "Student: " + order.getStudentNumber() + "\n\n" +
                    "Are you sure you want to permanently delete this received order?");
            
            extraConfirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            extraConfirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    performOrderDeletion(order);
                }
            });
        } else {
            performOrderDeletion(order);
        }
    }

    private void performOrderDeletion(Order order) {
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
                infoAlert.setContentText("Order ID " + order.getOrderId() + " deleted successfully.\n" +
                        "Status was: " + order.getStatus());
                infoAlert.showAndWait();
            } else {
                showErrorAlert("Deletion Failed", "Failed to delete order with ID: " + order.getOrderId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("SQL Error", "An error occurred while deleting the order: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}