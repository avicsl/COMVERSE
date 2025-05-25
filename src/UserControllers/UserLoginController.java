package UserControllers;
 
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
import Class.Student;
import Main.DatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
 
public class UserLoginController {
 
    @FXML
    private TextField usernameField;
 
    @FXML
    private PasswordField passwordField;
 
    @FXML
    private Button loginButton;
 
    @FXML
    private Button signup;
 
    private DatabaseHandler databaseHandler;
 
    public static Student loggedInUser; // Store the logged-in user for access
 
    public void initialize() {
        databaseHandler = DatabaseHandler.getInstance();
    }
 
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();
 
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Missing Fields", "Please enter both email and password.", Alert.AlertType.WARNING);
            return;
        }
 
        Student student = authenticateUser(email, password);
        if (student != null) {
            loggedInUser = student; // Save the logged-in student
 
            
 
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserUI/user.fxml"));
                Parent root = loader.load();
 
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Navigation Error", "Failed to load the homepage.", e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Login Failed", null, "Invalid email or password.", Alert.AlertType.ERROR);
        }
    }
 
    private Student authenticateUser(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseHandler.getDBConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
 
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
 
            if (rs.next()) {
                return new Student(
                    rs.getString("student_number"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("department"),
                    rs.getString("course")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
 
    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
 
    @FXML
    public void signup(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/UserUI/usersignup.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
 
    @FXML
    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Main/start.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
 