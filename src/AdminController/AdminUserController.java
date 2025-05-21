package AdminController;
 
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
 
import Class.Student;
import Main.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
 
public class AdminUserController implements Initializable {
 
    private final ObservableList<Student> userList = FXCollections.observableArrayList();
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    private final Map<String, ObservableList<String>> coursesMap = new HashMap<>();
 
    @FXML private TableView<Student> mytable;
    @FXML private TableColumn<Student, String> studnumcol;
    @FXML private TableColumn<Student, String> fnamecol;
    @FXML private TableColumn<Student, String> lnamecol;
    @FXML private TableColumn<Student, String> emailcol;
    @FXML private TableColumn<Student, String> passwordcol;
    @FXML private TableColumn<Student, String> coursecol;
    @FXML private TableColumn<Student, String> departmentcol;
 
    @FXML private TextField fnameTextField;
    @FXML private TextField lnameTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField pTextField;
    @FXML private ComboBox<String> coursecombobox;
    @FXML private ComboBox<String> deptcombobox;
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studnumcol.setCellValueFactory(new PropertyValueFactory<>("studentNumber"));
        fnamecol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lnamecol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailcol.setCellValueFactory(new PropertyValueFactory<>("email"));
        passwordcol.setCellValueFactory(new PropertyValueFactory<>("password"));
        coursecol.setCellValueFactory(new PropertyValueFactory<>("course"));
        departmentcol.setCellValueFactory(new PropertyValueFactory<>("department"));
 
        mytable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
 
        ObservableList<String> departments = FXCollections.observableArrayList(
            "College of Allied Health",
            "College of Architecture",
            "College of Business and Accountancy",
            "College of Computing and Information Technologies",
            "College of Education, Arts and Sciences",
            "College of Engineering",
            "College of Hospitality and Tourism Management"
        );
        deptcombobox.setItems(departments);
 
        coursesMap.put("College of Allied Health", FXCollections.observableArrayList(
            "BS Nursing",
            "BS Pharmacy",
            "BS Medical Technology / Medical Laboratory Science"
        ));
        coursesMap.put("College of Architecture", FXCollections.observableArrayList(
            "BS Architecture",
            "BS Environmental Planning"
        ));
        coursesMap.put("College of Business and Accountancy", FXCollections.observableArrayList(
            "BS Accountancy",
            "BS Accounting Information System",
            "BS Management Accounting",
            "BS Real Estate Management",
            "BSBA Financial Management",
            "BSBA Marketing Management"
        ));
        coursesMap.put("College of Computing and Information Technologies", FXCollections.observableArrayList(
            "BS Computer Science",
            "BS Information Technology",
            "Associate in Computer Technology",
            "Master of Science in Computer Science",
            "Master in Information Technology",
            "Doctor of Philosophy in Computer Science"
        ));
        coursesMap.put("College of Education, Arts and Sciences", FXCollections.observableArrayList(
            "AB English Language Studies",
            "BA Communication",
            "BS Psychology",
            "Bachelor of Elementary Education",
            "Bachelor of Secondary Education (major in English)",
            "Bachelor of Physical Education",
            "Master of Arts in Education (major in English, Filipino, Educational Management, Special Education)",
            "Doctor of Education (Educational Management)"
        ));
        coursesMap.put("College of Engineering", FXCollections.observableArrayList(
            "BS Civil Engineering",
            "BS Computer Engineering",
            "BS Electrical Engineering",
            "BS Electronics Engineering",
            "BS Mechanical Engineering",
            "BS Environmental and Sanitary Engineering",
            "Master of Science in Sanitary Engineering"
        ));
        coursesMap.put("College of Hospitality and Tourism Management", FXCollections.observableArrayList(
            "BS Hospitality Management",
            "BS Tourism Management"
        ));
 
        deptcombobox.setOnAction(e -> {
            String selectedDept = deptcombobox.getSelectionModel().getSelectedItem();
            if (selectedDept != null) {
                coursecombobox.setItems(coursesMap.getOrDefault(selectedDept, FXCollections.observableArrayList()));
                coursecombobox.getSelectionModel().clearSelection();
            }
        });
 
        loadUserData();
    }
 
    private void loadUserData() {
        userList.clear();
        userList.addAll(dbHandler.getAllUsers());
        mytable.setItems(userList);
    }
 
    private void populateFields(Student student) {
        fnameTextField.setText(student.getFirstName());
        lnameTextField.setText(student.getLastName());
        emailTextField.setText(student.getEmail());
        pTextField.setText(student.getPassword());
        deptcombobox.setValue(student.getDepartment());
        coursecombobox.setItems(coursesMap.getOrDefault(student.getDepartment(), FXCollections.observableArrayList()));
        coursecombobox.setValue(student.getCourse());
    }
 
    @FXML
    private void handleAddUser() {
        if (!validateInputs()) return;
 
        Student newUser = new Student(
            null,
            emailTextField.getText().trim(),
            pTextField.getText(),
            fnameTextField.getText().trim(),
            lnameTextField.getText().trim(),
            deptcombobox.getValue(),
            coursecombobox.getValue()
        );
 
        boolean success = dbHandler.addUser(newUser);
 
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", null, "User registered successfully!");
            loadUserData();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failure", null, "Failed to register user. Please try again.");
        }
    }
 
    @FXML
    private void handleUpdateUser() {
    Student selected = mytable.getSelectionModel().getSelectedItem();
    if (selected == null) {
        showAlert(Alert.AlertType.WARNING, "No Selection", null, "Please select a user to update.");
        return;
    }

    if (!validateInputsForUpdate(selected)) return;

    // Check if any changes were made
    boolean noChanges = 
        selected.getFirstName().equals(fnameTextField.getText()) &&
        selected.getLastName().equals(lnameTextField.getText()) &&
        selected.getEmail().equals(emailTextField.getText()) &&
        selected.getPassword().equals(pTextField.getText()) &&
        selected.getCourse().equals(coursecombobox.getValue()) &&
        selected.getDepartment().equals(deptcombobox.getValue());

    if (noChanges) {
        showAlert(Alert.AlertType.INFORMATION, "No Changes Detected", null, "No changes were made to update.");
        return;
    }

    // Proceed with update
    selected.setFirstName(fnameTextField.getText());
    selected.setLastName(lnameTextField.getText());
    selected.setEmail(emailTextField.getText());
    selected.setPassword(pTextField.getText());
    selected.setCourse(coursecombobox.getValue());
    selected.setDepartment(deptcombobox.getValue());

    if (dbHandler.updateUser(selected)) {
        showAlert(Alert.AlertType.INFORMATION, "Success", null, "User updated successfully!");
        loadUserData();
        clearFields();
    } else {
        showAlert(Alert.AlertType.ERROR, "Error", null, "Failed to update user.");
    }
}

 
    @FXML
    private void handleDeleteUser() {
        Student selected = mytable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }
 
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this user?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
 
        if (dbHandler.deleteUser(selected.getStudentNumber())) {
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "User deleted successfully.");
            loadUserData();
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
        }
    }
 
    private void clearFields() {
        fnameTextField.clear();
        lnameTextField.clear();
        emailTextField.clear();
        pTextField.clear();
        coursecombobox.getSelectionModel().clearSelection();
        deptcombobox.getSelectionModel().clearSelection();
    }
 
    private boolean validateInputs() {
        if (fnameTextField.getText().isEmpty() ||
            lnameTextField.getText().isEmpty() ||
            emailTextField.getText().isEmpty() ||
            pTextField.getText().isEmpty() ||
            coursecombobox.getValue() == null ||
            deptcombobox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Input", "Please fill in all fields.");
            return false;
        }
 
        String email = emailTextField.getText();
        if (!email.endsWith("@gmail.com")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email must end with @gmail.com!");
            return false;
        }
 
        if (dbHandler.isEmailExists(email)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Email already exists!");
            return false;
        }
 
        return true;
    }
 
    private boolean validateInputsForUpdate(Student current) {
    if (fnameTextField.getText().isEmpty() ||
        lnameTextField.getText().isEmpty() ||
        emailTextField.getText().isEmpty() ||
        pTextField.getText().isEmpty() ||
        coursecombobox.getValue() == null ||
        deptcombobox.getValue() == null) {
        showAlert(Alert.AlertType.WARNING, "Missing Input", "Please fill in all fields.");
        return false;
    }

    String email = emailTextField.getText();
    if (!email.endsWith("@gmail.com")) {
        showAlert(Alert.AlertType.WARNING, "Validation Error", "Email must end with @gmail.com!");
        return false;
    }

    // Only check if the new email is already taken by another user
    if (!email.equals(current.getEmail()) && dbHandler.isEmailExists(email)) {
        showAlert(Alert.AlertType.WARNING, "Validation Error", "Email already exists!");
        return false;
    }

    return true;
}

    private void showAlert(Alert.AlertType type, String title, String content) {
        showAlert(type, title, null, content);
    }
 
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
 
 