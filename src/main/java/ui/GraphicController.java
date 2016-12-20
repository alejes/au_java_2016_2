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
import java.util.DoubleSummaryStatistics;
import java.util.ResourceBundle;


public class GraphicController implements Initializable {
    @FXML
    LineChart clientProcessingTimeChart;
    @FXML
    LineChart queryProcessingTimeChart;
    @FXML
    LineChart averageClientTimeChart;
    @FXML
    HBox mainBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setupScene(BorderPane root) {
        //numberLineChart.setTitle("Series");
        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();

        series2.setName("cos(x)");
        series1.setName("sin(x)");

        ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
        for (int i = 0; i < 20; i++) {
            data.add(new XYChart.Data<Number, Number>(i,Math.sin(i)));
        }
        series1.setData(data);
        NumberAxis x = new NumberAxis();
        NumberAxis y = new NumberAxis();
        ObservableList<XYChart.Series<Number, Number>> observableList = FXCollections.observableArrayList();
        observableList.add(series1);

        LineChart<Number, Number> numberLineChart = new LineChart<Number, Number>(x, y, observableList);
        numberLineChart.getXAxis().setLabel("parameter");
        numberLineChart.getYAxis().setLabel("ClientProcessingTime");

        mainBox.getChildren().add(numberLineChart);

    }
}
