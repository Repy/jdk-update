package info.repy.adoptopenjdkupdate;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Controller.fxml"));
        Scene scene = new Scene(root, 300, 275);
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));
        stage.setTitle("openjdk downloader");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
