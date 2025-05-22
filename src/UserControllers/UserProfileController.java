package UserControllers;
 
import Class.Student;
import Main.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
 
import java.net.URL;
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
 
    private final DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    private Student currentUser;
 
    private final Map<String, ObservableList<String>> coursesMap = new HashMap<>();
 
    private boolean isEditing = false;
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDepartmentsAndCourses();
        setFieldsEditable(false);
 
        deptcombobox.setOnAction(e -> {
            String selectedDept = deptcombobox.getValue();
            if (selectedDept != null) {
                coursecombobox.setItems(coursesMap.getOrDefault(selectedDept, FXCollections.observableArrayList()));
                coursecombobox.getSelectionModel().clearSelection();
            }
        });
 
        currentUser = UserLoginController.loggedInUser;
        if (currentUser != null) {
            populateUserProfile(currentUser);
        }
 
        editbtn.setText("Edit");
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
 
 