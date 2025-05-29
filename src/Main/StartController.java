package Main;

import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class StartController {

    @FXML
    private Button userButton;

    @FXML
    private Button adminButton;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public void userButtonHandler(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/UserUI/userlogin.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void adminButtonHandler(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/AdminUI/adminlogin.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
