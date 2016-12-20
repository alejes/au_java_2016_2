package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class GraphicController implements Initializable {
    @FXML
    HBox mainBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setupScene(BorderPane root,
                           List<Number> parameter,
                           List<Number> clientProcessingTime,
                           List<Number> queryProcessingTime,
                           List<Number> averageClientTime) {
        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        XYChart.Series<Number, Number> series3 = new XYChart.Series<>();
        series1.setName("ClientProcessingTime");
        series2.setName("QueryProcessingTime");
        series3.setName("AverageClientTime");

        ObservableList<XYChart.Data<Number, Number>> data1 = FXCollections.observableArrayList();
        ObservableList<XYChart.Data<Number, Number>> data2 = FXCollections.observableArrayList();
        ObservableList<XYChart.Data<Number, Number>> data3 = FXCollections.observableArrayList();
        for (int i = 0; i < parameter.size(); i++) {
            data1.add(new XYChart.Data<>(parameter.get(i), clientProcessingTime.get(i)));
            data2.add(new XYChart.Data<>(parameter.get(i), queryProcessingTime.get(i)));
            data3.add(new XYChart.Data<>(parameter.get(i), averageClientTime.get(i)));
        }
        series1.setData(data1);
        series2.setData(data2);
        series3.setData(data3);
        ObservableList<XYChart.Series<Number, Number>> observableList1 = FXCollections.observableArrayList();
        ObservableList<XYChart.Series<Number, Number>> observableList2 = FXCollections.observableArrayList();
        ObservableList<XYChart.Series<Number, Number>> observableList3 = FXCollections.observableArrayList();
        observableList1.add(series1);
        observableList2.add(series2);
        observableList3.add(series3);

        LineChart<Number, Number> numberLineChart1 = new LineChart<>(new NumberAxis(), new NumberAxis(), observableList1);
        LineChart<Number, Number> numberLineChart2 = new LineChart<>(new NumberAxis(), new NumberAxis(), observableList2);
        LineChart<Number, Number> numberLineChart3 = new LineChart<>(new NumberAxis(), new NumberAxis(), observableList3);
        numberLineChart1.getXAxis().setLabel("parameter");
        numberLineChart2.getXAxis().setLabel("parameter");
        numberLineChart3.getXAxis().setLabel("parameter");
        numberLineChart1.getYAxis().setLabel("ClientProcessingTime");
        numberLineChart2.getYAxis().setLabel("QueryProcessingTime");
        numberLineChart3.getYAxis().setLabel("AverageClientTime");


        mainBox.getChildren().add(numberLineChart1);
        mainBox.getChildren().add(numberLineChart2);
        mainBox.getChildren().add(numberLineChart3);
    }
}
