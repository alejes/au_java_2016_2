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
import proto.ClientInitMessageOuterClass.ClientInitMessage;
import proto.ClientResponseStatMessageOuterClass.ClientResponseStatMessage;
import proto.ServerDataOuterClass.ServerData;
import proto.ServerInitMessageOuterClass.ServerInitMessage;
import proto.ServerRequestStatMessageOuterClass.ServerRequestStatMessage;
import proto.ServerResponseStatMessageOuterClass.ServerResponseStatMessage;
import proto.TestStrategyOuterClass.TestStrategy;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import static managers.ClientManager.CLIENT_MANAGER_HOST;
import static managers.ClientManager.CLIENT_MANAGER_PORT;
import static managers.ServerManager.SERVER_MANAGER_HOST;


public class MainController implements Initializable {
    @FXML
    private Button startButton;
    @FXML
    private TextField fromRangeField;
    @FXML
    private TextField toRangeField;
    @FXML
    private TextField stepRangeField;
    @FXML
    private TextField parameterX;
    @FXML
    private TextField parameterN;
    @FXML
    private TextField parameterM;
    @FXML
    private TextField parameterDelta;
    @FXML
    private ChoiceBox parameterChanged;
    @FXML
    private ToggleGroup archGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setupScene(Stage stage, BorderPane root) {
        startButton.setOnAction(e -> {
            final int fromRange = Integer.valueOf(fromRangeField.getText());
            final int toRange = Integer.valueOf(toRangeField.getText());
            final int stepRange = Integer.valueOf(stepRangeField.getText());
            final int N = Integer.valueOf(parameterN.getText());
            final int X = Integer.valueOf(parameterX.getText());
            final int M = Integer.valueOf(parameterM.getText());
            final int Delta = Integer.valueOf(parameterDelta.getText());
            final TestStrategy targetStrategy = TestStrategy.forNumber(Integer.valueOf(archGroup.getSelectedToggle().getUserData().toString()));
            if (targetStrategy == null) {
                Notifications.create()
                        .title("Perfomance Architectire")
                        .text("Unknown strategy")
                        .showError();
                return;
            }

            try (PrintWriter out = new PrintWriter(new Date().getTime() + ".log.txt")) {
                out.println("Strategy: " + targetStrategy.name());
                out.println("X = " + parameterX.getText());
                out.println("N = " + N);
                out.println("M = " + M);
                out.println("∆ = " + Delta);
                out.println(parameterChanged.getValue().toString() + " from " + fromRange + " to " + toRange + " by " + stepRange);

                for (int changedValue = fromRange; changedValue <= toRange; changedValue += stepRange) {
                    out.print("parameter = " + changedValue + " ");
                    switch (parameterChanged.getValue().toString()) {
                        case "M":
                            initializeServerAndClient(targetStrategy, N, changedValue, Delta, out);
                            break;
                        case "N":
                            initializeServerAndClient(targetStrategy, changedValue, M, Delta, out);
                            break;
                        case "∆":
                            initializeServerAndClient(targetStrategy, N, M, changedValue, out);
                            break;
                        default:
                            Notifications.create()
                                    .title("Perfomance Architectire")
                                    .text("Unknown parameter type")
                                    .showError();
                    }
                }
            } catch (IOException e1) {
                Notifications.create()
                        .title("Perfomance Architectire")
                        .text("Cannot write to file: " + e1.getMessage())
                        .showError();
            }
        });
    }

    private boolean initializeServerAndClient(TestStrategy targetStrategy, int N, int M, int Delta, PrintWriter out) {
        try (Socket clientSocket = new Socket();
             Socket serverSocket = new Socket()) {
            serverSocket.connect(new InetSocketAddress(InetAddress.getByName(SERVER_MANAGER_HOST), ServerManager.SERVER_MANAGER_PORT), 5000);
            clientSocket.connect(new InetSocketAddress(InetAddress.getByName(CLIENT_MANAGER_HOST), CLIENT_MANAGER_PORT), 5000);
            try (
                    DataOutputStream serverDos = new DataOutputStream(serverSocket.getOutputStream());
                    DataOutputStream clientDos = new DataOutputStream(clientSocket.getOutputStream());
                    DataInputStream serverDis = new DataInputStream(serverSocket.getInputStream());
                    DataInputStream clientDis = new DataInputStream(clientSocket.getInputStream())
            ) {
                ServerInitMessage.newBuilder()
                        .setStrategy(targetStrategy)
                        .build()
                        .writeDelimitedTo(serverDos);
                serverDos.flush();
                ServerData serverData = ServerData.parseDelimitedFrom(serverDis);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ClientInitMessage.newBuilder()
                        .setStrategy(targetStrategy)
                        .setX(Integer.valueOf(parameterX.getText()))
                        .setN(N)
                        .setM(M)
                        .setDelta(Delta)
                        .setServer(serverData)
                        .build()
                        .writeDelimitedTo(clientDos);

                clientDos.flush();

                ClientResponseStatMessage clientResponseStatMessage = ClientResponseStatMessage.parseDelimitedFrom(clientDis);

                ServerRequestStatMessage.newBuilder()
                        .build()
                        .writeDelimitedTo(serverDos);
                serverDos.flush();

                ServerResponseStatMessage serverResponseStatMessage = ServerResponseStatMessage.parseDelimitedFrom(serverDis);

                out.println("; ClientProcessingTime = " + serverResponseStatMessage.getClientProcessingTime() +
                        "; QueryProcessingTime= " + serverResponseStatMessage.getQueryProcessingTime() +
                        "; AverageClientTime = " + clientResponseStatMessage.getAverageClientTime());
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
