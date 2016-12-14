package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import managers.ServerManager;
import org.controlsfx.control.Notifications;
import proto.ServerInitMessageOuterClass.ServerInitMessage;
import proto.TestStrategyOuterClass.TestStrategy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {
    public static final String SERVER_MANAGER_HOST = "127.0.0.1";
    private static final String CLIENT_MANAGER_HOST = "127.0.0.1";
    private static final int CLIENT_MANAGER_PORT = 50789;
    @FXML
    private Button startButton;
    @FXML
    private TextField fromRange;
    @FXML
    private TextField toRange;
    @FXML
    private TextField stepRange;
    @FXML
    private TextField parameterX;
    @FXML
    private ChoiceBox parameterChanged;
    @FXML
    private ToggleGroup archGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setupScene(Stage stage, BorderPane root) {
        startButton.setOnAction(e -> {
            initializeServer();
        });
    }

    private boolean initializeServer() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName(SERVER_MANAGER_HOST), ServerManager.SERVER_MANAGER_PORT), 5000);
            try (
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dis = new DataInputStream(socket.getInputStream())
            ) {
                TestStrategy targetStrategy = TestStrategy.forNumber(Integer.valueOf(archGroup.getSelectedToggle().getUserData().toString()));

                ServerInitMessage serverInit = ServerInitMessage.newBuilder()
                        .setStategy(targetStrategy)
                        .build();
                serverInit.writeTo(dos);

            }
        } catch (IOException exc) {
            Notifications.create()
                    .title("Perfomance Architectire")
                    .text("Server manager initialize failed: " + exc.getMessage())
                    .showError();
            return false;
        }
        return true;
    }

}
