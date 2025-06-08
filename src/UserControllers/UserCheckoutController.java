package UserControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import Main.DatabaseHandler;
import java.math.BigDecimal;
import java.sql.*;

public class UserCheckoutController {

    @FXML
    private TextField shippingaddress;

    @FXML
    private TextField smethodfield;

    @FXML
    private TextField samountfield;

    @FXML
    private TextField totalamountfield; 

    @FXML
    private ComboBox<String> paymentmethod;

    @FXML
    private Button addtocartbtn;

    @FXML
    private Button addtocartbtn1;

    // Store the current total amount and transaction ID for database operations
    private BigDecimal currentTotalAmount;
    private String currentTransactionId;
    private String currentStudentNumber;
    
    // Track if shipping information has been saved
    private boolean shippingInfoSaved = false;

    @FXML
    public void initialize() {
        // Set non-editable fields
        smethodfield.setText("Flash Express");
        smethodfield.setEditable(false);

        samountfield.setText("40.00");
        samountfield.setEditable(false);

        // Make totalamountfield non-editable as well
        totalamountfield.setEditable(false);

        // Set payment method options
        paymentmethod.getItems().addAll(
            "Cash on Delivery",
            "American Express",
            "GCash",
            "Maya",
            "PayPal",
            "Visa",
            "JCB"
        );

        // Handle button clicks
        addtocartbtn.setOnAction(e -> handleSavePayment());
        addtocartbtn1.setOnAction(e -> handleSaveShipping());

        // Get current student number
        currentStudentNumber = UserLoginController.loggedInUser.getStudentNumber();
    }

    // Method to set the total amount and transaction ID from cart checkout
    public void setCheckoutDetails(BigDecimal totalAmount, String transactionId) {
        if (totalAmount != null && transactionId != null) {
            // Don't add shipping cost here since it's already included in totalAmount from orders
            this.currentTotalAmount = totalAmount;
            this.currentTransactionId = transactionId;
            totalamountfield.setText(String.format("%.2f", this.currentTotalAmount.doubleValue()));
            
            // Reset shipping status when new checkout details are set
            this.shippingInfoSaved = false;
        }
    }

    // Alternative method if you only have total amount (will fetch latest transaction)
    public void setTotalAmount(BigDecimal totalAmount) {
        if (totalAmount != null) {
            // Don't add shipping cost here since it's already included in totalAmount from orders
            this.currentTotalAmount = totalAmount;
            totalamountfield.setText(String.format("%.2f", this.currentTotalAmount.doubleValue()));
            
            // Load the latest transaction ID for the current user
            loadLatestTransactionId();
            
            // Reset shipping status when new total amount is set
            this.shippingInfoSaved = false;
        }
    }

    // Load the latest transaction ID for the current user
    private void loadLatestTransactionId() {
        String query = "SELECT transaction_id FROM orders WHERE student_number = ? ORDER BY order_date DESC LIMIT 1";

        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentStudentNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                currentTransactionId = rs.getString("transaction_id");
                // Check if shipping info already exists for this transaction
                checkExistingShippingInfo();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load transaction information.");
        }
    }

    // Check if shipping information already exists for the current transaction
    private void checkExistingShippingInfo() {
        if (currentTransactionId != null && !currentTransactionId.isEmpty()) {
            shippingInfoSaved = shippingExists();
        }
    }

    // Handle saving shipping information (addtocartbtn1)
    private void handleSaveShipping() {
        String address = shippingaddress.getText().trim();

        if (address.isEmpty()) {
            showAlert("Invalid Address", "Please enter a valid shipping address.");
            return;
        }

        if (currentTransactionId == null || currentTransactionId.isEmpty()) {
            showAlert("Error", "No transaction found to add shipping information.");
            return;
        }

        try (Connection conn = DatabaseHandler.getDBConnection()) {
            // Use INSERT ... ON DUPLICATE KEY UPDATE or check and update if exists
            if (shippingExists()) {
                // Update existing shipping information
                String updateShipping = "UPDATE shipping SET shipping_address = ?, shipping_date = CURDATE() WHERE transaction_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateShipping)) {
                    pstmt.setString(1, address);
                    pstmt.setString(2, currentTransactionId);
                    pstmt.executeUpdate();
                    shippingInfoSaved = true; // Mark as saved
                    showAlert("Success", "Shipping information updated successfully!");
                }
            } else {
                // Insert new shipping information
                String insertShipping = "INSERT INTO shipping (transaction_id, student_number, shipping_address, shipping_date, shipping_amount) VALUES (?, ?, ?, CURDATE(), 40.00)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertShipping)) {
                    pstmt.setString(1, currentTransactionId);
                    pstmt.setString(2, currentStudentNumber);
                    pstmt.setString(3, address);
                    pstmt.executeUpdate();
                    shippingInfoSaved = true; // Mark as saved
                    showAlert("Success", "Shipping information saved successfully!");
                }
            }
                
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to save shipping information: " + ex.getMessage());
        }
    }

    // Handle saving payment information (addtocartbtn)
    private void handleSavePayment() {
        // Check if shipping information has been saved first
        if (!shippingInfoSaved) {
            showAlert("Message", "Please read the note and click 'I understand' first.");
            return;
        }

        String address = shippingaddress.getText().trim();
        String payment = paymentmethod.getValue();

        if (address.isEmpty()) {
            showAlert("Invalid Address", "Please enter a valid shipping address.");
            return;
        }

        if (payment == null || payment.isEmpty()) {
            showAlert("Payment Method Required", "Please select a payment method.");
            return;
        }

        if (currentTransactionId == null || currentTransactionId.isEmpty()) {
            showAlert("Error", "No transaction found to add payment information.");
            return;
        }

        if (currentTotalAmount == null) {
            showAlert("Error", "Total amount not available.");
            return;
        }

        try (Connection conn = DatabaseHandler.getDBConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // Handle shipping information - update if exists, insert if not
                if (shippingExists()) {
                    // Update existing shipping information
                    String updateShipping = "UPDATE shipping SET shipping_address = ?, shipping_date = CURDATE() WHERE transaction_id = ?";
                    try (PreparedStatement shippingStmt = conn.prepareStatement(updateShipping)) {
                        shippingStmt.setString(1, address);
                        shippingStmt.setString(2, currentTransactionId);
                        shippingStmt.executeUpdate();
                    }
                } else {
                    // Insert new shipping information
                    String insertShipping = "INSERT INTO shipping (transaction_id, student_number, shipping_address, shipping_date, shipping_amount) VALUES (?, ?, ?, CURDATE(), 40.00)";
                    try (PreparedStatement shippingStmt = conn.prepareStatement(insertShipping)) {
                        shippingStmt.setString(1, currentTransactionId);
                        shippingStmt.setString(2, currentStudentNumber);
                        shippingStmt.setString(3, address);
                        shippingStmt.executeUpdate();
                    }
                }
                
                // Save payment information - allow multiple payments
                String insertPayment = "INSERT INTO payment (transaction_id, student_number, payment_amount, payment_method, payment_date) VALUES (?, ?, ?, ?, CURDATE())";
                try (PreparedStatement paymentStmt = conn.prepareStatement(insertPayment)) {
                    paymentStmt.setString(1, currentTransactionId);
                    paymentStmt.setString(2, currentStudentNumber);
                    paymentStmt.setBigDecimal(3, currentTotalAmount); // This should match the total_amount from orders table
                    paymentStmt.setString(4, payment);
                    paymentStmt.executeUpdate();
                }
                
                conn.commit(); // Commit transaction
                showAlert("Success", "You've checkout successfully!\nOrder has been placed!");
                
                // Clear the form after successful payment
                clearForm();
                
            } catch (SQLException ex) {
                conn.rollback(); // Rollback on error
                ex.printStackTrace();
                showAlert("Error", "Failed to save payment information: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not connect to database.");
        }
    }

    // Check if shipping already exists for the current transaction
    private boolean shippingExists() {
        String query = "SELECT COUNT(*) FROM shipping WHERE transaction_id = ?";
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, currentTransactionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Clear the form after successful operations
    private void clearForm() {
        shippingaddress.clear();
        paymentmethod.setValue(null);
        totalamountfield.clear();
        currentTransactionId = null;
        currentTotalAmount = null;
        shippingInfoSaved = false; // Reset shipping status
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
