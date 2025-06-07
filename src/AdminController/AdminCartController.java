package AdminController;

import Main.DatabaseHandler;
import Class.AdminCartItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class AdminCartController {

    @FXML
    private TableView<AdminCartItem> mytable;

    @FXML
    private TableColumn<AdminCartItem, Integer> cartIdCol;

    @FXML
    private TableColumn<AdminCartItem, String> productIdCol;

    @FXML
    private TableColumn<AdminCartItem, String> studentNumberCol;

    @FXML
    private TableColumn<AdminCartItem, String> imageUrlCol;

    @FXML
    private TableColumn<AdminCartItem, Integer> quantityCol;

    @FXML
    private TableColumn<AdminCartItem, Double> amountCol;

    @FXML
    private TextField searchField;

    private final ObservableList<AdminCartItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up columns
        cartIdCol.setCellValueFactory(new PropertyValueFactory<>("cartId"));
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        studentNumberCol.setCellValueFactory(new PropertyValueFactory<>("studentNumber"));

        // For image, we want to display actual ImageView, not just URL text
        imageUrlCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        imageUrlCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isBlank()) {
                    setGraphic(null);
                } else {
                    imageView.setImage(new Image(url, 60, 60, true, true));
                    setGraphic(imageView);
                }
            }
        });

        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // Load all cart data from DB (all users)
        loadAllCartItemsFromDB();

        // Setup search filter
        FilteredList<AdminCartItem> filteredData = new FilteredList<>(cartItems, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(cartItem -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Filter by productId or studentNumber
                if (cartItem.getProductId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (cartItem.getStudentNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(cartItem.getCartId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(cartItem.getQuantity()).contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(cartItem.getAmount()).contains(lowerCaseFilter)) {
                    return true;
                }

                return false; // no match
            });
        });

        mytable.setItems(filteredData);
    }

    private void loadAllCartItemsFromDB() {
        cartItems.clear();

        String query = "SELECT cart_id, product_id, student_number, image_url, quantity, amount FROM cart";

        try (Connection conn = DatabaseHandler.getDBConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String cartId = rs.getString("cart_id"); // String instead of int
                String productId = rs.getString("product_id");
                String studentNumber = rs.getString("student_number");
                String imageUrl = rs.getString("image_url");
                int quantity = rs.getInt("quantity");
                double amount = rs.getDouble("amount");

                AdminCartItem item = new AdminCartItem(cartId, productId, studentNumber, imageUrl, quantity, amount);
                cartItems.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load cart data.");
            alert.showAndWait();
        }
    }
}