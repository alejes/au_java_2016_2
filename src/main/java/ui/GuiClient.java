package ui;

import client.TorrentClientImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import models.torrent.TorrentClient;

import java.io.IOException;

public class GuiClient extends Application {
    private static final String serverHost = "127.0.0.1";
    private static TorrentClient tc;


    public static void main(String[] args) throws IOException {
        int clientId = 0;
        if (args.length > 0) {
            clientId = Integer.valueOf(args[0]);
        }
        boolean cleanState = false;
        if (args.length > 1) {
            cleanState = args[1].equals("cleanState");
        }
        tc = new TorrentClientImpl(serverHost, clientId, cleanState);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
        BorderPane root = loader.load();
        primaryStage.setTitle("TorrentClientUI");
        primaryStage.setScene(new Scene(root, 600, 400));
        ClientController controller = loader.getController();
        controller.setupScene(primaryStage, tc, root);
        primaryStage.show();
    }
}
