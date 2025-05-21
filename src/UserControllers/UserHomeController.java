package UserControllers;
 
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
 
public class UserHomeController implements Initializable {

    private UserController mainController; // reference to parent controller

    public void setMainController(UserController controller) {
        this.mainController = controller;
    }

    @FXML
    private Label timelabel;

    @FXML
    private Label datelabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalDateTime now = LocalDateTime.now();

            String time = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            String date = now.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));

            timelabel.setText(time);
            datelabel.setText(date);
        }));

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    private void shirt(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/usershirt.fxml"));
        UserController.instance.setCenterContent(view);
    }

    @FXML
    private void jacket(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/userjacket.fxml"));
        UserController.instance.setCenterContent(view);
    }

    @FXML
    private void cap(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/usercap.fxml"));
        UserController.instance.setCenterContent(view);
    }

    @FXML
    private void bag(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/userbag.fxml"));
        UserController.instance.setCenterContent(view);
    }

    @FXML
    private void lace(ActionEvent event) throws IOException {
        AnchorPane view = FXMLLoader.load(getClass().getResource("/UserUI/userlace.fxml"));
        UserController.instance.setCenterContent(view);
    }

}
