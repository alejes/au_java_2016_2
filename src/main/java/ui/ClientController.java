package ui;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.TorrentFile;
import models.torrent.TorrentClient;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private final ObservableList<HBox> filesShowList = FXCollections.observableArrayList();
    @FXML
    private Button download;
    @FXML
    private Button upload;
    @FXML
    private Button update;
    @FXML
    private TextField download_id;

    public void setupScene(Stage stage, TorrentClient tc, BorderPane root) {

        final FileChooser fileChooser = new FileChooser();

        upload.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        tc.registerFile(file);
                        if (!tc.forceUpdate()) {
                            Notifications.create()
                                    .title("Torrent Client")
                                    .text("Update failed")
                                    .showWarning();
                        }
                    }
                });


        ListView<HBox> listView = new ListView<>(filesShowList);
        listView.setPrefSize(200, 250);
        listView.setItems(filesShowList);
        root.setCenter(listView);

        update.setOnAction(
                e -> {
                    if (!tc.forceUpdate()) {
                        Notifications.create()
                                .title("Torrent Client")
                                .text("Update failed")
                                .showWarning();
                    }
                    Collection<TorrentFile> localList = tc.distributedFiles();
                    tc.listFiles().forEach(x -> {
                        Optional<TorrentFile> local = localList.stream().filter(y -> y.getFileId() == x.getFileId()).findAny();
                        TorrentFile targetFile = local.orElse(x);

                        boolean find = false;

                        // No filesShowList.filtered(), JDK Bag with NPE in ObservableList

                        for (HBox it : filesShowList) {
                            if ((it != null) && (it.getId().equals(String.valueOf(targetFile.getFileId())))) {
                                StatusBar statusBar = (StatusBar) it.getChildren().get(0);
                                statusBar.setProgress(1.0 * targetFile.getCountPieces() / targetFile.getTotalPieces());
                                statusBar.setText(x.toString(/*inLocalList*/local.isPresent(),/*showProgress*/ false));
                                find = true;
                            }
                        }
                        if (!find) {
                            HBox hbox = new HBox(200);
                            StatusBar statusBar = new StatusBar();
                            statusBar.setText(x.toString(/*inLocalList*/local.isPresent(),/*showProgress*/ false));
                            hbox.setId(String.valueOf(targetFile.getFileId()));
                            statusBar.setProgress(1.0 * targetFile.getCountPieces() / targetFile.getTotalPieces());
                            HBox.setHgrow(statusBar, Priority.ALWAYS);

                            hbox.setAlignment(Pos.BASELINE_LEFT);
                            hbox.getChildren().addAll(
                                    statusBar
                            );
                            hbox.setPadding(new Insets(10));
                            filesShowList.add(hbox);
                        }

                    });
                });

        download.setOnAction(e -> {
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                if (!tc.addGetTask(Integer.parseInt(download_id.getText()), file.getAbsolutePath())) {
                    Notifications.create()
                            .title("Torrent Client")
                            .text("Add file failed")
                            .showWarning();
                }
            } else {
                Notifications.create()
                        .title("Torrent Client")
                        .text("File select failed")
                        .showWarning();
            }
        });

        stage.setOnCloseRequest(we -> {
            try {
                tc.close();
            } catch (IOException e) {
                Notifications.create()
                        .title("Torrent Client")
                        .text("Close failed: " + e.getMessage())
                        .showError();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
