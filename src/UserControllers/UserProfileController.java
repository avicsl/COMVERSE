package UserControllers;

import Class.Student;
import Class.Order;

import Main.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable {

    @FXML private TextField IDTextfield;
    @FXML private ComboBox<String> coursecombobox;
    @FXML private ComboBox<String> deptcombobox;
    @FXML private Button editbtn;
    @FXML private Button deleteBtn;
    @FXML private Button cancelBtn;
    @FXML private TextField emailTextField;
    @FXML private TextField fnameTextfield;
    @FXML private TextField lnameTextfield;
    @FXML private TextField passwordTextfield;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> transactionIdCol;
    @FXML private TableColumn<Order, String> productIdCol;
    @FXML private TableColumn<Order, String> imageUrlCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, String> quantityCol;
    @FXML private TableColumn<Order, String> totalAmountCol;


    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    private Student currentUser;

    private final Map<String, ObservableList<String>> coursesMap = new HashMap<>();

    private boolean isEditing = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = UserLoginController.loggedInUser;  // Ensure currentUser is initialized early

        loadDepartmentsAndCourses();
        setFieldsEditable(false);

        if (currentUser != null) {
            populateUserProfile(currentUser);
            loadUserOrders(currentUser.getStudentNumber());
        }

        deptcombobox.setOnAction(e -> {
            String selectedDept = deptcombobox.getValue();
            if (selectedDept != null) {
                coursecombobox.setItems(coursesMap.getOrDefault(selectedDept, FXCollections.observableArrayList()));
                coursecombobox.getSelectionModel().clearSelection();
            }
        });

        cancelBtn.setDisable(true);
        cancelBtn.setText("Cancel Order");

        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                boolean canDelete = canDeleteOrder(newSelection);
                deleteBtn.setDisable(!canDelete);
                cancelBtn.setDisable(false); // Enable cancel button for any selected order
            } else {
                deleteBtn.setDisable(true);
                cancelBtn.setDisable(true);
            }
        });

        // Initialize delete button
        deleteBtn.setText("Mark as Received");
        deleteBtn.setDisable(true);

        // Enable/disable delete button based on table selection
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                boolean canDelete = canDeleteOrder(newSelection);
                deleteBtn.setDisable(!canDelete);
            } else {
                deleteBtn.setDisable(true);
            }
        });

        editbtn.setText("Edit");
    }


    private void loadUserOrders(String studentNumber) {
        ObservableList<Order> orderList = FXCollections.observableArrayList();

        try {
            // Modified query to only show orders that are not marked as received by the user
            String query = "SELECT order_id, transaction_id, product_id, student_number, image_url, order_date, quantity, total_amount " +
                    "FROM orders WHERE student_number = ? AND (user_marked_received IS NULL OR user_marked_received = 0)";
            PreparedStatement statement = DatabaseHandler.getDBConnection().prepareStatement(query);
            statement.setString(1, studentNumber);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                orderList.add(new Order(
                        rs.getString("order_id"),              // orderId
                        rs.getString("transaction_id"),        // transactionId
                        rs.getString("product_id"),            // productId
                        rs.getString("student_number"),        // studentNumber
                        rs.getDate("order_date").toString(),   // orderDate
                        rs.getString("quantity"),               // quantity
                        rs.getDouble("total_amount"),           // totalAmount
                        rs.getString("image_url")               // imageUrl
                ));
            }

            // Setup table columns - ensure PropertyValueFactory keys match Order's getter names (without 'get')
            transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
            productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
            imageUrlCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
            orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
            quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

            // Set custom cell factory to display images in imageUrlCol
            imageUrlCol.setCellFactory(new Callback<TableColumn<Order, String>, TableCell<Order, String>>() {
                @Override
                public TableCell<Order, String> call(TableColumn<Order, String> param) {
                    return new TableCell<Order, String>() {
                        private final ImageView imageView = new ImageView();

                        {
                            imageView.setFitWidth(100);   // Width of the image in the cell
                            imageView.setFitHeight(70);   // Height of the image in the cell
                            imageView.setPreserveRatio(true);
                        }

                        @Override
                        protected void updateItem(String imageUrl, boolean empty) {
                            super.updateItem(imageUrl, empty);
                            if (empty || imageUrl == null || imageUrl.isEmpty()) {
                                setGraphic(null);
                            } else {
                                try {
                                    Image image = new Image(imageUrl, true);  // Load asynchronously
                                    imageView.setImage(image);
                                    setGraphic(imageView);
                                } catch (Exception e) {
                                    setGraphic(null);
                                }
                            }
                        }
                    };
                }
            });

            ordersTable.setItems(orderList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean canDeleteOrder(Order order) {
        try {
            LocalDate orderDate = LocalDate.parse(order.getOrderDate());
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(orderDate, currentDate);
            return daysBetween >= 3;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleDeleteButton() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to mark as received.");
            return;
        }

        if (!canDeleteOrder(selectedOrder)) {
            LocalDate orderDate = LocalDate.parse(selectedOrder.getOrderDate());
            LocalDate eligibleDate = orderDate.plusDays(3);
            showAlert(Alert.AlertType.WARNING, "Cannot Mark as Received", 
                    "Orders can only be marked as received after 3 days from the order date.\n" +
                    "This order can be marked as received on: " + eligibleDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Action");
        confirmAlert.setHeaderText("Mark Order as Received");
        confirmAlert.setContentText("Are you sure you want to mark this order as received?\n" +
                "Transaction ID: " + selectedOrder.getTransactionId() + "\n" +
                "Product ID: " + selectedOrder.getProductId() + "\n" +
                "Order Date: " + selectedOrder.getOrderDate() + "\n\n" +
                "Note: This will hide the order from your view but keep it in the system records.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean marked = markOrderAsReceived(selectedOrder);
            if (marked) {
                // Remove from table view only
                ordersTable.getItems().remove(selectedOrder);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order has been marked as received and removed from your orders list.\nThe order record is preserved in the system for administrative purposes.");
                deleteBtn.setDisable(true);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to mark order as received. Please try again.");
            }
        }
    }

    // Modified method - now marks as received instead of deleting
    private boolean markOrderAsReceived(Order order) {
        try {
            // Add user_marked_received column if it doesn't exist (you may need to add this to your database schema)
            String query = "UPDATE orders SET user_marked_received = 1, received_date = CURRENT_TIMESTAMP WHERE order_id = ? AND student_number = ?";
            PreparedStatement statement = DatabaseHandler.getDBConnection().prepareStatement(query);
            statement.setString(1, order.getOrderId());
            statement.setString(2, currentUser.getStudentNumber());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Keep the old method name for compatibility but rename the implementation
    private boolean deleteOrderFromDatabase(Order order) {
        return markOrderAsReceived(order);
    }

    private void populateUserProfile(Student student) {
        IDTextfield.setText(student.getStudentNumber());
        fnameTextfield.setText(student.getFirstName());
        lnameTextfield.setText(student.getLastName());
        emailTextField.setText(student.getEmail());
        passwordTextfield.setText(student.getPassword());

        deptcombobox.setValue(student.getDepartment());
        coursecombobox.setItems(coursesMap.getOrDefault(student.getDepartment(), FXCollections.observableArrayList()));
        coursecombobox.setValue(student.getCourse());
    }

    private void setFieldsEditable(boolean editable) {
        IDTextfield.setEditable(false);
        fnameTextfield.setEditable(editable);
        lnameTextfield.setEditable(editable);
        emailTextField.setEditable(editable);
        passwordTextfield.setEditable(editable);

        deptcombobox.setEditable(false);
        deptcombobox.setMouseTransparent(!editable);
        deptcombobox.setFocusTraversable(editable);
        deptcombobox.setVisible(true);

        coursecombobox.setEditable(false);
        coursecombobox.setMouseTransparent(!editable);
        coursecombobox.setFocusTraversable(editable);
        coursecombobox.setVisible(true);
    }

    @FXML
    private void handleEditButton() {
        if (!isEditing) {
            setFieldsEditable(true);
            editbtn.setText("Save");
            isEditing = true;
        } else {
            if (!validateInputs()) return;

            currentUser.setFirstName(fnameTextfield.getText());
            currentUser.setLastName(lnameTextfield.getText());
            currentUser.setEmail(emailTextField.getText());
            currentUser.setPassword(passwordTextfield.getText());
            currentUser.setDepartment(deptcombobox.getValue());
            currentUser.setCourse(coursecombobox.getValue());

            boolean updated = dbHandler.updateUser(currentUser);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                setFieldsEditable(false);
                editbtn.setText("Edit");
                isEditing = false;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile.");
            }
        }
    }

    private boolean validateInputs() {
        String email = emailTextField.getText().trim();

        if (fnameTextfield.getText().isEmpty() ||
                lnameTextfield.getText().isEmpty() ||
                email.isEmpty() ||
                passwordTextfield.getText().isEmpty() ||
                deptcombobox.getValue() == null ||
                coursecombobox.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please fill in all fields.");
            return false;
        }

        if (!email.endsWith("@gmail.com")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email must end with @gmail.com!");
            return false;
        }

        // Check for duplicate email
        Student existing = dbHandler.getUserByEmail(email);
        if (existing != null && !existing.getStudentNumber().equals(currentUser.getStudentNumber())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email already exists. Please use a different email.");
            return false;
        }

        return true;
    }

    private void loadDepartmentsAndCourses() {
        deptcombobox.setItems(FXCollections.observableArrayList(
                "College of Allied Health",
                "College of Architecture",
                "College of Business and Accountancy",
                "College of Computing and Information Technologies",
                "College of Education, Arts and Sciences",
                "College of Engineering",
                "College of Hospitality and Tourism Management"
        ));

        coursesMap.put("College of Allied Health", FXCollections.observableArrayList(
                "BS Nursing", "BS Pharmacy", "BS Medical Technology / Medical Laboratory Science"));
        coursesMap.put("College of Architecture", FXCollections.observableArrayList(
                "BS Architecture", "BS Environmental Planning"));
        coursesMap.put("College of Business and Accountancy", FXCollections.observableArrayList(
                "BS Accountancy", "BS Accounting Information System", "BS Management Accounting",
                "BS Real Estate Management", "BSBA Financial Management", "BSBA Marketing Management"));
        coursesMap.put("College of Computing and Information Technologies", FXCollections.observableArrayList(
                "BS Computer Science", "BS Information Technology", "Associate in Computer Technology",
                "Master of Science in Computer Science", "Master in Information Technology", "Doctor of Philosophy in Computer Science"));
        coursesMap.put("College of Education, Arts and Sciences", FXCollections.observableArrayList(
                "AB English Language Studies", "BA Communication", "BS Psychology", "Bachelor of Elementary Education",
                "Bachelor of Secondary Education (major in English)", "Bachelor of Physical Education",
                "Master of Arts in Education (major in English, Filipino, Educational Management, Special Education)",
                "Doctor of Education (Educational Management)"));
        coursesMap.put("College of Engineering", FXCollections.observableArrayList(
                "BS Civil Engineering", "BS Computer Engineering", "BS Electrical Engineering",
                "BS Electronics Engineering", "BS Mechanical Engineering", "BS Environmental and Sanitary Engineering",
                "Master of Science in Sanitary Engineering"));
        coursesMap.put("College of Hospitality and Tourism Management", FXCollections.observableArrayList(
                "BS Hospitality Management", "BS Tourism Management"));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCancelOrderButton() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to cancel.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Order");
        confirmAlert.setContentText("Are you sure you want to cancel this order?\n" +
                "Transaction ID: " + selectedOrder.getTransactionId() + "\n" +
                "Product ID: " + selectedOrder.getProductId());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // First restore the quantity to the product
            boolean quantityRestored = restoreProductQuantity(selectedOrder);
            
            if (quantityRestored) {
                // Then delete the order and associated data
                boolean deleted = deleteOrderAndAssociatedData(selectedOrder);
                if (deleted) {
                    ordersTable.getItems().remove(selectedOrder);
                    showAlert(Alert.AlertType.INFORMATION, "Order Cancelled", "The order has been successfully cancelled and product quantity has been restored.");
                    cancelBtn.setDisable(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Cancellation Failed", "Failed to cancel the order. Please try again.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Cancellation Failed", "Failed to restore product quantity. Order cancellation aborted.");
            }
        }
    }

    private boolean restoreProductQuantity(Order order) {
        try {
            // Get the current product quantity - Fixed table name from 'products' to 'product'
            String getProductQuery = "SELECT quantity FROM product WHERE product_id = ?";
            PreparedStatement getStmt = DatabaseHandler.getDBConnection().prepareStatement(getProductQuery);
            getStmt.setString(1, order.getProductId());
            ResultSet rs = getStmt.executeQuery();
            
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                int orderQuantity = Integer.parseInt(order.getQuantity());
                int newQuantity = currentQuantity + orderQuantity;
                
                // Update the product quantity - Fixed table name from 'products' to 'product'
                String updateQuery = "UPDATE product SET quantity = ? WHERE product_id = ?";
                PreparedStatement updateStmt = DatabaseHandler.getDBConnection().prepareStatement(updateQuery);
                updateStmt.setInt(1, newQuantity);
                updateStmt.setString(2, order.getProductId());
                
                int rowsAffected = updateStmt.executeUpdate();
                return rowsAffected > 0;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteOrderAndAssociatedData(Order order) {
        try {
            String deletePayment = "DELETE FROM payment WHERE transaction_id = ?";
            String deleteShipping = "DELETE FROM shipping WHERE transaction_id = ?";
            String deleteOrder = "DELETE FROM orders WHERE order_id = ? AND transaction_id = ?";

            var conn = DatabaseHandler.getDBConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (
                PreparedStatement stmt1 = conn.prepareStatement(deletePayment);
                PreparedStatement stmt2 = conn.prepareStatement(deleteShipping);
                PreparedStatement stmt3 = conn.prepareStatement(deleteOrder)
            ) {
                stmt1.setString(1, order.getTransactionId());
                stmt1.executeUpdate();

                stmt2.setString(1, order.getTransactionId());
                stmt2.executeUpdate();

                stmt3.setString(1, order.getOrderId());
                stmt3.setString(2, order.getTransactionId());
                int rows = stmt3.executeUpdate();

                conn.commit(); // Commit transaction if all deletes successful
                return rows > 0;
            } catch (Exception e) {
                conn.rollback(); // Rollback on failure
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}