package AdminController;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import Main.DatabaseHandler;

import java.sql.*;

public class AdminShippingController {

    @FXML
    private Button deletehistorybtn;

    @FXML
    private TableView<ShippingData> mytable;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<ShippingData, String> shippingaddresscol;

    @FXML
    private TableColumn<ShippingData, String> shippingamountcol;

    @FXML
    private TableColumn<ShippingData, String> shippingidcol;

    @FXML
    private TableColumn<ShippingData, String> shippingmethodcol;

    @FXML
    private TableColumn<ShippingData, String> studentnumbercol;

    @FXML
    private TableColumn<ShippingData, String> transactionidcol;

    @FXML
    private Button updatehistoryBTN;

    private ObservableList<ShippingData> shippingList = FXCollections.observableArrayList();
    private ObservableList<ShippingData> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns
        setupTableColumns();
        
        // Load shipping data
        loadShippingData();
        
        // Setup search functionality
        setupSearch();
        
        // Setup button actions
        setupButtonActions();
        
        
    }

    private void setupTableColumns() {
        shippingidcol.setCellValueFactory(new PropertyValueFactory<>("shippingId"));
        transactionidcol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        studentnumbercol.setCellValueFactory(new PropertyValueFactory<>("studentNumber"));
        shippingaddresscol.setCellValueFactory(new PropertyValueFactory<>("shippingAddress"));
        shippingmethodcol.setCellValueFactory(new PropertyValueFactory<>("shippingMethod"));
        shippingamountcol.setCellValueFactory(new PropertyValueFactory<>("shippingAmount"));
        
        // Set column widths
        shippingidcol.setPrefWidth(120);
        transactionidcol.setPrefWidth(150);
        studentnumbercol.setPrefWidth(120);
        shippingaddresscol.setPrefWidth(200);
        shippingmethodcol.setPrefWidth(120);
        shippingamountcol.setPrefWidth(100);
    }

    private void loadShippingData() {
        shippingList.clear();
        
        String query = "SELECT shipping_id, transaction_id, student_number, shipping_address, " +
                      "shipping_date, shipping_amount FROM shipping ORDER BY shipping_date DESC";
        
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                ShippingData shipping = new ShippingData(
                    rs.getString("shipping_id"),
                    rs.getString("transaction_id"),  
                    rs.getString("student_number"),
                    rs.getString("shipping_address"),
                    rs.getDate("shipping_date"),
                    rs.getBigDecimal("shipping_amount")
                );
                shippingList.add(shipping);
            }
            
            filteredList.setAll(shippingList);
            mytable.setItems(filteredList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load shipping data: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterShipping(newValue);
        });
    }

    private void filterShipping(String searchText) {
        filteredList.clear();
        
        for (ShippingData shipping : shippingList) {
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                shipping.getShippingId().toLowerCase().contains(searchText.toLowerCase()) ||
                shipping.getTransactionId().toLowerCase().contains(searchText.toLowerCase()) ||
                shipping.getStudentNumber().toLowerCase().contains(searchText.toLowerCase()) ||
                shipping.getShippingAddress().toLowerCase().contains(searchText.toLowerCase());
            
            if (matchesSearch) {
                filteredList.add(shipping);
            }
        }
        
        mytable.setItems(filteredList);
    }

    private void setupButtonActions() {
        deletehistorybtn.setOnAction(e -> handleDeleteShipping());
    }

   

    private void handleDeleteShipping() {
        ShippingData selectedShipping = mytable.getSelectionModel().getSelectedItem();
        
        if (selectedShipping == null) {
            showAlert("No Selection", "Please select a shipping record to delete.");
            return;
        }
        
        // Confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Shipping Record");
        confirmDialog.setContentText("Are you sure you want to delete Shipping ID: " + selectedShipping.getShippingId() + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteShipping(selectedShipping.getShippingId());
            }
        });
    }

    private void deleteShipping(String shippingId) {
        String query = "DELETE FROM shipping WHERE shipping_id = ?";
        
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, shippingId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showAlert("Success", "Shipping record deleted successfully!");
                loadShippingData(); // Refresh the table
            } else {
                showAlert("Error", "No shipping record found to delete.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not delete shipping record: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class to represent shipping data
    public static class ShippingData {
        private final SimpleStringProperty shippingId;
        private final SimpleStringProperty transactionId;
        private final SimpleStringProperty studentNumber;
        private final SimpleStringProperty shippingAddress;
        private final SimpleStringProperty shippingMethod;
        private final SimpleStringProperty shippingAmount;
        private final SimpleStringProperty shippingDate;

        public ShippingData(String shippingId, String transactionId, String studentNumber, 
                           String shippingAddress, java.sql.Date shippingDate, java.math.BigDecimal shippingAmount) {
            this.shippingId = new SimpleStringProperty(shippingId);
            this.transactionId = new SimpleStringProperty(transactionId);
            this.studentNumber = new SimpleStringProperty(studentNumber);
            this.shippingAddress = new SimpleStringProperty(shippingAddress);
            this.shippingMethod = new SimpleStringProperty("Flash Express"); // Fixed shipping method
            this.shippingAmount = new SimpleStringProperty(String.format("₱%.2f", shippingAmount.doubleValue()));
            this.shippingDate = new SimpleStringProperty(shippingDate.toString());
        }

        // Getters
        public String getShippingId() { return shippingId.get(); }
        public String getTransactionId() { return transactionId.get(); }
        public String getStudentNumber() { return studentNumber.get(); }
        public String getShippingAddress() { return shippingAddress.get(); }
        public String getShippingMethod() { return shippingMethod.get(); }
        public String getShippingAmount() { return shippingAmount.get(); }
        public String getShippingDate() { return shippingDate.get(); }

        // Property getters for TableColumn
        public SimpleStringProperty shippingIdProperty() { return shippingId; }
        public SimpleStringProperty transactionIdProperty() { return transactionId; }
        public SimpleStringProperty studentNumberProperty() { return studentNumber; }
        public SimpleStringProperty shippingAddressProperty() { return shippingAddress; }
        public SimpleStringProperty shippingMethodProperty() { return shippingMethod; }
        public SimpleStringProperty shippingAmountProperty() { return shippingAmount; }
        public SimpleStringProperty shippingDateProperty() { return shippingDate; }
    }
}