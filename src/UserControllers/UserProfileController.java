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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable {

    @FXML private TextField IDTextfield;
    @FXML private ComboBox<String> coursecombobox;
    @FXML private ComboBox<String> deptcombobox;
    @FXML private Button editbtn;
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

        editbtn.setText("Edit");
    }

    private void loadUserOrders(String studentNumber) {
        ObservableList<Order> orderList = FXCollections.observableArrayList();

        try {
            String query = "SELECT order_id, transaction_id, product_id, student_number, image_url, order_date, quantity, total_amount " +
                    "FROM orders WHERE student_number = ?";
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
}
