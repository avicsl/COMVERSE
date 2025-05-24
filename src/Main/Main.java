package Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/Main/start.fxml"));
        primaryStage.setTitle("Welcome to Comverse!");
        primaryStage.setScene(new Scene(root, 906, 610));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 