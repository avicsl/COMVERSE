package AdminController;

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

public class AdminController implements Initializable {

    @FXML
    private Label homelabel;
    
    @FXML
    private Button users;

    @FXML
    private BorderPane borderpane;

    @FXML
    private AnchorPane admin;

    @FXML
    private TextField usernameTextfield; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/adminuser.fxml"));
            borderpane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @FXML
    private void users(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/adminuser.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void categories(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/admincategory.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void products(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/adminproduct.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void orderhistory(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/adminorder.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void paymenthistory(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/adminpayment.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void shippinghistory(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/adminshipping.fxml"));
        borderpane.setCenter(view);
    }
 
    @FXML
    private void cart(ActionEvent event) throws IOException{
        AnchorPane view = FXMLLoader.load(getClass().getResource("/AdminUI/admincart.fxml"));
        borderpane.setCenter(view);
    }

    @FXML
    public void back(ActionEvent event) throws IOException {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Log Out");
    alert.setHeaderText(null);
    alert.setContentText("Do you want to log out?");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
        Parent root = FXMLLoader.load(getClass().getResource("/AdminUI/adminlogin.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

 
}
 
