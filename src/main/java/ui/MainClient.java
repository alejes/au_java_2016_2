package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainClient extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        BorderPane root = loader.load();
        primaryStage.setTitle("Performance Measure");
        primaryStage.setScene(new Scene(root, 600, 225));
        MainController controller = loader.getController();
        controller.setupScene(root);
        primaryStage.show();
    }
}
