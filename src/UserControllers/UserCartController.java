package UserControllers;

import Class.CartItem;
import Class.Student;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import Main.DatabaseHandler;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserCartController {

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> productnamecol;

    @FXML
    private TableColumn<CartItem, String> productimagecol;

    @FXML
    private TableColumn<CartItem, Double> pricecol;

    @FXML
    private TableColumn<CartItem, Integer> quantitycol;

    @FXML
    private TableColumn<CartItem, CheckBox> selectcol;

    @FXML
    private TextField subtotalTextfield;

    
    public static Student loggedInUser;


    private static UserCartController instance;

    // Shared observable list for cart items
    private static final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    public static UserCartController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        // Setup table columns
        productnamecol.setCellValueFactory(data -> data.getValue().productNameProperty());

        productimagecol.setCellValueFactory(data -> data.getValue().imageUrlProperty());
        productimagecol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(new Image(item, 120, 120, true, true));
                    setGraphic(imageView);
                }
            }
        });

        pricecol.setCellValueFactory(data -> data.getValue().priceProperty().asObject());

        quantitycol.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());

        selectcol.setCellValueFactory(data -> {
            CheckBox checkBox = data.getValue().getSelect();

            // Remove any old listener before adding new one to avoid duplicates
            checkBox.selectedProperty().removeListener((obs, oldVal, newVal) -> updateSubtotal());

            // Add listener to update subtotal when checkbox toggled
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateSubtotal());

            return new SimpleObjectProperty<>(checkBox);
        });

        selectcol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : item);
            }
        });

        // Bind the cartItems list to the table
        cartTable.setItems(cartItems);

        // Load saved cart data from DB for the logged-in user
        loadCartItemsFromDB();

        // Initialize subtotal field
        updateSubtotal();
    }

    private void updateSubtotal() {
        double subtotal = 0.0;

        for (CartItem item : cartItems) {
            if (item.getSelect().isSelected()) {
                subtotal += item.getPrice() * item.getQuantity();
            }
        }

        subtotalTextfield.setText(String.format("%.2f", subtotal));
    }

    /**
     * Load the cart items from the database for the currently logged in user.
     */
    public void loadCartItemsFromDB() {
        cartItems.clear();

        String studentNumber = UserLoginController.loggedInUser.getStudentNumber();
        String query = "SELECT product_id, image_url, quantity, amount FROM cart WHERE student_number = ?";

        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String productId = rs.getString("product_id");
                String imageUrl = rs.getString("image_url");
                int quantity = rs.getInt("quantity");
                double amount = rs.getDouble("amount") / quantity; // price per item

                String productName = fetchProductNameById(productId);

                CartItem item = new CartItem(productId, productName, imageUrl, amount);
                item.setQuantity(quantity);

                cartItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateSubtotal();
    }

    /**
     * Helper method to get product name from product table by ID.
     */
    private String fetchProductNameById(String productId) {
        String name = "";
        String query = "SELECT product_name FROM product WHERE product_id = ?";

        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("product_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static void addToCart(CartItem newItem) {
        for (CartItem existingItem : cartItems) {
            if (existingItem.getProductID().equals(newItem.getProductID())) {
                // Update quantity
                int updatedQuantity = existingItem.getQuantity() + newItem.getQuantity();
                existingItem.setQuantity(updatedQuantity);

                // The price per unit stays the same, so no need to update price property
                if (instance != null) {
                    instance.updateSubtotal();
                }
                return; // Exit after updating
            }
        }
        // If not found, add as new item
        cartItems.add(newItem);
        if (instance != null) {
            instance.updateSubtotal();
        }
    }

    @FXML
    private void handleDeleteSelected() {
    ObservableList<CartItem> toRemove = FXCollections.observableArrayList();

    // Gather selected items
    for (CartItem item : cartItems) {
        if (item.getSelect().isSelected()) {
            toRemove.add(item);
        }
    }

    // No items selected
    if (toRemove.isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Selection");
        alert.setHeaderText(null);
        alert.setContentText("Please select items to delete.");
        alert.showAndWait();
        return;
    }

    // Confirm deletion
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Confirm Deletion");
    confirmAlert.setHeaderText("Are you sure you want to delete the selected item(s)?");
    confirmAlert.setContentText("This action cannot be undone.");

    // Wait for user response
    if (confirmAlert.showAndWait().get() == ButtonType.OK) {
        try (Connection conn = DatabaseHandler.getDBConnection()) {
            String deleteQuery = "DELETE FROM cart WHERE student_number = ? AND product_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteQuery);

            for (CartItem item : toRemove) {
                pstmt.setString(1, UserLoginController.loggedInUser.getStudentNumber());
                pstmt.setString(2, item.getProductID());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to delete items");
            errorAlert.setContentText("An error occurred while deleting items from the database.");
            errorAlert.showAndWait();
        }

        cartItems.removeAll(toRemove);
        updateSubtotal();
    }
}

private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public void handleCheckout() {
    if (cartItems.isEmpty()) {
        showAlert("No items in cart.", Alert.AlertType.WARNING);
        return;
    }

    List<CartItem> selectedItems = new ArrayList<>();
    for (CartItem item : cartItems) {
        if (item.getSelect().isSelected()) {
            selectedItems.add(item);
        }
    }

    if (selectedItems.isEmpty()) {
        showAlert("No items selected for checkout.", Alert.AlertType.WARNING);
        return;
    }

    String studentNumber = UserLoginController.loggedInUser.getStudentNumber();

    try (Connection conn = DatabaseHandler.getDBConnection()) {
        conn.setAutoCommit(false); // Begin single SQL transaction

        String cartQuery = "SELECT cart_id FROM cart WHERE product_id = ? AND student_number = ?";
        String orderInsert = "INSERT INTO orders (product_id, cart_id, student_number, image_url, quantity, total_amount) VALUES (?, ?, ?, ?, ?, ?)";
        String deleteCart = "DELETE FROM cart WHERE cart_id = ?";

        try (
            PreparedStatement cartStmt = conn.prepareStatement(cartQuery);
            PreparedStatement orderStmt = conn.prepareStatement(orderInsert);
            PreparedStatement deleteStmt = conn.prepareStatement(deleteCart)
        ) {
            BigDecimal totalTransactionAmount = BigDecimal.ZERO;
            List<String> cartIdsToDelete = new ArrayList<>();

            // Compute total item price first
            for (CartItem item : selectedItems) {
                BigDecimal itemTotal = BigDecimal.valueOf(item.getQuantity() * item.getPrice());
                totalTransactionAmount = totalTransactionAmount.add(itemTotal);
            }

            // Add ₱40 delivery fee per checkout (one transaction)
            totalTransactionAmount = totalTransactionAmount.add(BigDecimal.valueOf(40));

            BigDecimal accumulated = BigDecimal.ZERO;

            // Insert items into order table
            for (int i = 0; i < selectedItems.size(); i++) {
                CartItem item = selectedItems.get(i);

                cartStmt.setString(1, item.getProductID());
                cartStmt.setString(2, studentNumber);
                ResultSet rs = cartStmt.executeQuery();

                if (rs.next()) {
                    String cartId = rs.getString("cart_id");
                    cartIdsToDelete.add(cartId);

                    BigDecimal itemTotal = BigDecimal.valueOf(item.getQuantity() * item.getPrice());

                    if (i == selectedItems.size() - 1) {
                        // Add ₱40 fee to the last item
                        itemTotal = totalTransactionAmount.subtract(accumulated);
                    } else {
                        accumulated = accumulated.add(itemTotal);
                    }

                    orderStmt.setString(1, item.getProductID());
                    orderStmt.setString(2, cartId);
                    orderStmt.setString(3, studentNumber);
                    orderStmt.setString(4, item.getImageUrl());
                    orderStmt.setInt(5, item.getQuantity());
                    orderStmt.setBigDecimal(6, itemTotal);
                    orderStmt.addBatch();
                } else {
                    System.err.println("No cart_id found for product: " + item.getProductID());
                    conn.rollback();
                    return;
                }
            }

            // Insert orders
            orderStmt.executeBatch();

            // Delete from cart
            for (String cartId : cartIdsToDelete) {
                deleteStmt.setString(1, cartId);
                deleteStmt.addBatch();
            }
            deleteStmt.executeBatch();

            conn.commit(); // Commit full transaction (single point)
            cartItems.removeAll(selectedItems); // Remove from UI list
            showAlert("Checkout successful!", Alert.AlertType.INFORMATION);

        } catch (SQLException ex) {
            conn.rollback();
            ex.printStackTrace();
            showAlert("Checkout failed. Please try again.", Alert.AlertType.ERROR);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Database error.", Alert.AlertType.ERROR);
    }
}

}