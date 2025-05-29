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
import java.util.stream.Collectors;
 
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
 
        // Prepare SQL statements
        String cartQuery = "SELECT cart_id FROM cart WHERE product_id = ? AND student_number = ?";
        String orderInsert = "INSERT INTO orders (transaction_id, product_id, cart_id, student_number, image_url, order_date, quantity, total_amount) VALUES (?, ?, ?, ?, ?, CURDATE(), ?, ?)";
        String getNextTransactionId = "SELECT IFNULL(MAX(CAST(SUBSTRING(transaction_id, 5) AS UNSIGNED)), 0) + 1 AS next_id FROM orders";
 
        try (
            PreparedStatement cartStmt = conn.prepareStatement(cartQuery);
            PreparedStatement orderStmt = conn.prepareStatement(orderInsert);
            PreparedStatement transactionIdStmt = conn.prepareStatement(getNextTransactionId)
        ) {
            // Generate a single transaction_id for this checkout
            ResultSet transactionIdRs = transactionIdStmt.executeQuery();
            String transactionId = "";
            if (transactionIdRs.next()) {
                int nextTransactionNum = transactionIdRs.getInt("next_id");
                transactionId = "TRN-" + String.format("%03d", nextTransactionNum);
            }
            transactionIdRs.close();
 
            // Calculate total amount for all selected items
            BigDecimal totalAmountSum = BigDecimal.ZERO;
 
            // First, get all cart_ids and calculate total amounts
            List<String> cartIds = new ArrayList<>();
            for (CartItem item : selectedItems) {
                cartStmt.setString(1, item.getProductID());
                cartStmt.setString(2, studentNumber);
                ResultSet rs = cartStmt.executeQuery();
 
                if (rs.next()) {
                    cartIds.add(rs.getString("cart_id"));
                    BigDecimal itemTotal = BigDecimal.valueOf(item.getQuantity() * item.getPrice());
                    totalAmountSum = totalAmountSum.add(itemTotal);
                } else {
                    showAlert("Cart ID not found for product: " + item.getProductID(), Alert.AlertType.ERROR);
                    conn.rollback();
                    return;
                }
                rs.close();
            }
 
            // Add ₱40 transaction fee once per batch
            totalAmountSum = totalAmountSum.add(BigDecimal.valueOf(40));
 
            // Insert all selected items with the SAME transaction_id and SAME total_amount
            // Each row will get a unique order_id from the trigger
            for (int i = 0; i < selectedItems.size(); i++) {
                CartItem item = selectedItems.get(i);
                orderStmt.setString(1, transactionId); // Same transaction_id for all items
                orderStmt.setString(2, item.getProductID());
                orderStmt.setString(3, cartIds.get(i));
                orderStmt.setString(4, studentNumber);
                orderStmt.setString(5, item.getImageUrl());
                orderStmt.setInt(6, item.getQuantity());
                orderStmt.setBigDecimal(7, totalAmountSum); // Same total amount (including ₱40 fee) for all rows
 
                orderStmt.addBatch();
            }
 
            int[] insertResults = orderStmt.executeBatch();
 
            // Commit once all inserts succeed
            conn.commit();
 
            // Remove checked out items from local cart UI list only (do NOT delete from DB)
            cartItems.removeAll(selectedItems);
            showAlert("Checkout successful! Total Amount: ₱" + totalAmountSum, Alert.AlertType.INFORMATION);
 
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

 
 
    private void showAlert(String message, Alert.AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setContentText(message);
    alert.showAndWait();
}
 
 
}
