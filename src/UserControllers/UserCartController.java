package UserControllers;
 
import Class.CartItem;
import Class.Student;
import Main.DatabaseHandler;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
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
 
    public void handleCheckout() {
    if (cartItems.isEmpty()) {
        showAlert("No items in cart.", Alert.AlertType.WARNING);
        return;
    }

    // Collect selected items
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
        conn.setAutoCommit(false); // Begin transaction

        String getNextTransactionId = "SELECT IFNULL(MAX(CAST(SUBSTRING(transaction_id, 5) AS UNSIGNED)), 0) + 1 AS next_id FROM orders";
        String orderInsert = "INSERT INTO orders (transaction_id, product_id, student_number, image_url, order_date, quantity, total_amount) VALUES (?, ?, ?, ?, CURDATE(), ?, ?)";
        String getCartId = "SELECT cart_id FROM cart WHERE product_id = ? AND student_number = ?";
        String deleteCart = "DELETE FROM cart WHERE cart_id = ?";
        String updateProductQuantity = "UPDATE product SET quantity = quantity - ? WHERE product_id = ?";

        try (
            PreparedStatement transactionIdStmt = conn.prepareStatement(getNextTransactionId);
            PreparedStatement orderStmt = conn.prepareStatement(orderInsert);
            PreparedStatement getCartIdStmt = conn.prepareStatement(getCartId);
            PreparedStatement deleteCartStmt = conn.prepareStatement(deleteCart);
            PreparedStatement updateProductStmt = conn.prepareStatement(updateProductQuantity)
        ) {
            // Generate transaction ID
            ResultSet rs = transactionIdStmt.executeQuery();
            String transactionId = "";
            if (rs.next()) {
                transactionId = "TRN-" + String.format("%03d", rs.getInt("next_id"));
            }
            rs.close();

            // Calculate total amount + ₱40 fee
            BigDecimal totalAmountSum = BigDecimal.ZERO;
            List<String> cartIdsToDelete = new ArrayList<>();

            for (CartItem item : selectedItems) {
                BigDecimal itemTotal = BigDecimal.valueOf(item.getQuantity() * item.getPrice());
                totalAmountSum = totalAmountSum.add(itemTotal);

                // Prepare update quantity
                updateProductStmt.setInt(1, item.getQuantity());
                updateProductStmt.setString(2, item.getProductID());
                updateProductStmt.addBatch();

                // Get cart_id for deletion
                getCartIdStmt.setString(1, item.getProductID());
                getCartIdStmt.setString(2, studentNumber);
                ResultSet cartRs = getCartIdStmt.executeQuery();
                if (cartRs.next()) {
                    cartIdsToDelete.add(cartRs.getString("cart_id"));
                } else {
                    showAlert("Cart ID not found for product: " + item.getProductID(), Alert.AlertType.ERROR);
                    conn.rollback();
                    return;
                }
                cartRs.close();
            }

            totalAmountSum = totalAmountSum.add(BigDecimal.valueOf(40));

            // Insert orders
            for (CartItem item : selectedItems) {
                orderStmt.setString(1, transactionId);
                orderStmt.setString(2, item.getProductID());
                orderStmt.setString(3, studentNumber);
                orderStmt.setString(4, item.getImageUrl());
                orderStmt.setInt(5, item.getQuantity());
                orderStmt.setBigDecimal(6, totalAmountSum);
                orderStmt.addBatch();
            }

            // Execute batches
            updateProductStmt.executeBatch();
            orderStmt.executeBatch();

            // Delete cart entries
            for (String cartId : cartIdsToDelete) {
                deleteCartStmt.setString(1, cartId);
                deleteCartStmt.addBatch();
            }
            deleteCartStmt.executeBatch();

            conn.commit(); // Commit all changes
            cartItems.removeAll(selectedItems);
            
            // Show success message
            showAlert("Total Amount: ₱" + totalAmountSum, Alert.AlertType.INFORMATION);
            
            // Navigate to usercheckout.fxml after successful checkout and pass the total amount
            navigateToCheckout(totalAmountSum);

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

private void navigateToCheckout(BigDecimal totalAmount) {
    try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/UserUI/usercheckout.fxml"));
        javafx.scene.layout.AnchorPane view = loader.load();
        
        // Get the controller and pass the total amount
        UserCheckoutController checkoutController = loader.getController();
        checkoutController.setTotalAmount(totalAmount);
        
        UserController.instance.setCenterContent(view);
        
    } catch (java.io.IOException e) {
        e.printStackTrace();
        showAlert("Failed to load checkout page.", Alert.AlertType.ERROR);
    }
}
 
    private void showAlert(String message, Alert.AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setContentText(message);
    alert.showAndWait();
}
}