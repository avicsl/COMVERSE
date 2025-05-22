package UserControllers;
 
 
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
 
public class UserController implements Initializable {
 
    @FXML
    private Label homelabel;
   
    @FXML
    private Button home;
 
    @FXML
    private BorderPane borderpane;
 
    @FXML
    private AnchorPane admin;
 
    @FXML
    private TextField usernameTextfield;

    public static UserController instance;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/userhome.fxml"));
            borderpane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCenterContent(AnchorPane content) {
        borderpane.setCenter(content);
    }


 
    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            AnchorPane view = loader.load();
            UserController controller = loader.getController();
            if (controller != null) {
                controller.displayName(username);
            }
            borderpane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCenterPane(String fxmlPath) {
    try {
        AnchorPane view = FXMLLoader.load(getClass().getResource(fxmlPath));
        borderpane.setCenter(view);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

 
    @FXML
    public void back(ActionEvent event) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to log out?");
 
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Parent root = FXMLLoader.load(getClass().getResource("/UserUI/userlogin.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }
 
    @FXML
    private void home(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/userhome.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void profile(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/userprofile.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void cart(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/usercart.fxml"));
        borderpane.setCenter(view);
    }
}
   
