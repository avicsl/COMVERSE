package AdminController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import Main.DatabaseHandler;

import java.sql.*;

public class AdminPaymentController {

    @FXML
    private Button deletpaymentbtn;

    @FXML
    private TableView<PaymentData> mytable;

    @FXML
    private TableColumn<PaymentData, String> orderidcol;

    @FXML
    private TableColumn<PaymentData, String> paymentidcol;

    @FXML
    private TableColumn<PaymentData, String> paymentmethodcol;


    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<PaymentData, String> studentnumbercol;

    @FXML
    private Button updatepaymentbtn;

    private ObservableList<PaymentData> paymentList = FXCollections.observableArrayList();
    private ObservableList<PaymentData> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns
        setupTableColumns();
        
       
        
        // Load payment data
        loadPaymentData();
        
        // Setup search functionality
        setupSearch();
        
        // Setup button actions
        setupButtonActions();
    }

    private void setupTableColumns() {
        paymentidcol.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        orderidcol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        studentnumbercol.setCellValueFactory(new PropertyValueFactory<>("studentNumber"));
        paymentmethodcol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        
        // Set column widths
        paymentidcol.setPrefWidth(120);
        orderidcol.setPrefWidth(150);
        studentnumbercol.setPrefWidth(120);
        paymentmethodcol.setPrefWidth(150);
    }

    
    private void loadPaymentData() {
        paymentList.clear();
        
        String query = "SELECT payment_id, transaction_id, student_number, payment_method, payment_amount, payment_date " +
                      "FROM payment ORDER BY payment_date DESC";
        
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                PaymentData payment = new PaymentData(
                    rs.getString("payment_id"),
                    rs.getString("transaction_id"),  
                    rs.getString("student_number"),
                    rs.getString("payment_method"),
                    rs.getBigDecimal("payment_amount"),
                    rs.getDate("payment_date")
                );
                paymentList.add(payment);
            }
            
            filteredList.setAll(paymentList);
            mytable.setItems(filteredList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load payment data: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPayments(newValue);
        });
    }

    private void filterPayments(String searchText) {
        filteredList.clear();
        
        for (PaymentData payment : paymentList) {
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                payment.getPaymentId().toLowerCase().contains(searchText.toLowerCase()) ||
                payment.getTransactionId().toLowerCase().contains(searchText.toLowerCase()) ||
                payment.getStudentNumber().toLowerCase().contains(searchText.toLowerCase()) ||
                payment.getPaymentMethod().toLowerCase().contains(searchText.toLowerCase());
            
            if (matchesSearch) {
                filteredList.add(payment);
            }
        }
        
        mytable.setItems(filteredList);
    }

    private void setupButtonActions() {
        deletpaymentbtn.setOnAction(e -> handleDeletePayment());
    }

    
    private void handleDeletePayment() {
        PaymentData selectedPayment = mytable.getSelectionModel().getSelectedItem();
        
        if (selectedPayment == null) {
            showAlert("No Selection", "Please select a payment record to delete.");
            return;
        }
        
        // Confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Payment Record");
        confirmDialog.setContentText("Are you sure you want to delete Payment ID: " + selectedPayment.getPaymentId() + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deletePayment(selectedPayment.getPaymentId());
            }
        });
    }

    private void deletePayment(String paymentId) {
        String query = "DELETE FROM payment WHERE payment_id = ?";
        
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, paymentId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Success", "Payment record deleted successfully!");
                loadPaymentData(); // Refresh the table
            } else {
                showAlert("Error", "No payment record found to delete.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not delete payment record: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class to represent payment data
    public static class PaymentData {
        private final SimpleStringProperty paymentId;
        private final SimpleStringProperty transactionId;
        private final SimpleStringProperty studentNumber;
        private final SimpleStringProperty paymentMethod;
        private final SimpleStringProperty paymentAmount;
        private final SimpleStringProperty paymentDate;

        public PaymentData(String paymentId, String transactionId, String studentNumber, 
                          String paymentMethod, java.math.BigDecimal paymentAmount, java.sql.Date paymentDate) {
            this.paymentId = new SimpleStringProperty(paymentId);
            this.transactionId = new SimpleStringProperty(transactionId);
            this.studentNumber = new SimpleStringProperty(studentNumber);
            this.paymentMethod = new SimpleStringProperty(paymentMethod);
            this.paymentAmount = new SimpleStringProperty(String.format("₱%.2f", paymentAmount.doubleValue()));
            this.paymentDate = new SimpleStringProperty(paymentDate.toString());
        }

        // Getters
        public String getPaymentId() { return paymentId.get(); }
        public String getTransactionId() { return transactionId.get(); }
        public String getStudentNumber() { return studentNumber.get(); }
        public String getPaymentMethod() { return paymentMethod.get(); }
        public String getPaymentAmount() { return paymentAmount.get(); }
        public String getPaymentDate() { return paymentDate.get(); }

        // Property getters for TableColumn
        public SimpleStringProperty paymentIdProperty() { return paymentId; }
        public SimpleStringProperty transactionIdProperty() { return transactionId; }
        public SimpleStringProperty studentNumberProperty() { return studentNumber; }
        public SimpleStringProperty paymentMethodProperty() { return paymentMethod; }
        public SimpleStringProperty paymentAmountProperty() { return paymentAmount; }
        public SimpleStringProperty paymentDateProperty() { return paymentDate; }
    }
}