package UserControllers;
 
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
 
import Class.Student;
import Main.DatabaseHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
 
public class UserSignupController {
 
    @FXML
    private TextField emailField;
 
    @FXML
    private PasswordField passwordField;
 
    @FXML
    private TextField firstNameField;
 
    @FXML
    private TextField lastNameField;
 
    @FXML
    private Button signUpButton;
 
    @FXML
    private ComboBox<String> coursecombobox;
 
    @FXML
    private ComboBox<String> deptcombobox;
 
    private final ObservableList<String> departments = FXCollections.observableArrayList(
        "CAH - College of Allied Health",
        "COA - College of Architecture",
        "CBA - College of Business and Accountancy",
        "CCIT - College of Computing and Information Technologies",
        "CEAS - College of Education, Arts and Sciences",
        "COE - College of Engineering",
        "CHTM - College of Hospitality and Tourism Management"
    );
 
    private final Map<String, ObservableList<String>> coursesMap = new HashMap<>();
 
    @FXML
    public void initialize() {
        deptcombobox.setItems(departments);
 
        // Set listener to update courses when department is selected
        deptcombobox.setOnAction(event -> {
            String selectedDept = deptcombobox.getValue();
            ObservableList<String> courses = coursesMap.getOrDefault(selectedDept, FXCollections.observableArrayList());
            coursecombobox.setItems(courses);
        });
 
        // Map courses to departments
        coursesMap.put("CAH - College of Allied Health", FXCollections.observableArrayList(
            "BS Nursing",
            "BS Pharmacy",
            "BS Medical Technology / Medical Laboratory Science"
        ));
        coursesMap.put("COA - College of Architecture", FXCollections.observableArrayList(
            "BS Architecture",
            "BS Environmental Planning"
        ));
        coursesMap.put("CBA - College of Business and Accountancy", FXCollections.observableArrayList(
            "BS Accountancy",
            "BS Accounting Information System",
            "BS Management Accounting",
            "BS Real Estate Management",
            "BSBA Financial Management",
            "BSBA Marketing Management"
        ));
        coursesMap.put("CCIT - College of Computing and Information Technologies", FXCollections.observableArrayList(
            "BS Computer Science",
            "BS Information Technology",
            "Associate in Computer Technology",
            "Master of Science in Computer Science",
            "Master in Information Technology",
            "Doctor of Philosophy in Computer Science"
        ));
        coursesMap.put("CEAS - College of Education, Arts and Sciences", FXCollections.observableArrayList(
            "AB English Language Studies",
            "BA Communication",
            "BS Psychology",
            "Bachelor of Elementary Education",
            "Bachelor of Secondary Education (major in English)",
            "Bachelor of Physical Education",
            "Master of Arts in Education (major in English, Filipino, Educational Management, Special Education)",
            "Doctor of Education (Educational Management)"
        ));
        coursesMap.put("COE - College of Engineering", FXCollections.observableArrayList(
            "BS Civil Engineering",
            "BS Computer Engineering",
            "BS Electrical Engineering",
            "BS Electronics Engineering",
            "BS Mechanical Engineering",
            "BS Environmental and Sanitary Engineering",
            "Master of Science in Sanitary Engineering"
        ));
        coursesMap.put("CHTM - College of Hospitality and Tourism Management", FXCollections.observableArrayList(
            "BS Hospitality Management",
            "BS Tourism Management"
        ));
    }
 
    @FXML
    private void handleSignUp() {
        if (!validateInputs()) {
            return;
        }
 
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String department = deptcombobox.getValue();
        String course = coursecombobox.getValue();
 
        DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 
        if (dbHandler.isEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", null, "Email already exists. Please use a different email.");
            return;
        }
 
        Student newUser = new Student(
            null, // studentNumber is auto-generated
            email,
            password,
            firstName,
            lastName,
            department,
            course
        );
 
        boolean success = dbHandler.addUser(newUser);
 
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", null, "User registered successfully!");
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failure", null, "Failed to register user. Please try again.");
        }
    }
 
    private boolean validateInputs() {
        if (emailField.getText().isEmpty()
                || passwordField.getText().isEmpty()
                || firstNameField.getText().isEmpty()
                || lastNameField.getText().isEmpty()
                || deptcombobox.getValue() == null
                || coursecombobox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", null, "Please fill in all fields.");
            return false;
        }
 
        if (!emailField.getText().endsWith("@gmail.com")) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", null, "Email must end with @gmail.com!");
            return false;
        }
 
        return true;
    }
 
    @FXML
    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/UserUI/userlogin.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
 
    private void clearFields() {
        emailField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        deptcombobox.getSelectionModel().clearSelection();
        coursecombobox.getSelectionModel().clearSelection();
    }
 
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
 
