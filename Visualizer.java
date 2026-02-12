package org.example.bb84;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Visualizer extends Application {

    private static List<double[]> noiseData = new ArrayList<>();
    private static List<double[]> distanceData = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        // Load data from CSV files
        loadData("qber_vs_noise.csv", noiseData);
        loadData("qber_vs_distance.csv", distanceData);

        // Create charts
        LineChart<Number, Number> noiseChart = createChart("QBER and Secure Key Rate vs Noise Probability", noiseData, "Noise Probability");
        LineChart<Number, Number> distanceChart = createChart("QBER and Secure Key Rate vs Distance", distanceData, "Distance (km)");

        // Layout
        HBox root = new HBox(noiseChart, distanceChart);
        Scene scene = new Scene(root, 1200, 600);
        primaryStage.setTitle("BB84 QKD Simulation Results");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private LineChart<Number, Number> createChart(String title, List<double[]> data, String xLabel) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("QBER / Key Rate");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);

        XYChart.Series<Number, Number> qberSeries = new XYChart.Series<>();
        qberSeries.setName("QBER");
        XYChart.Series<Number, Number> keyRateSeries = new XYChart.Series<>();
        keyRateSeries.setName("Secure Key Rate");

        for (double[] point : data) {
            qberSeries.getData().add(new XYChart.Data<>(point[0], point[1]));
            keyRateSeries.getData().add(new XYChart.Data<>(point[0], point[2]));
        }

        lineChart.getData().add(qberSeries);
        lineChart.getData().add(keyRateSeries);

        return lineChart;
    }

    private void loadData(String filename, List<double[]> data) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                double[] point = new double[3];
                for (int i = 0; i < 3; i++) {
                    point[i] = Double.parseDouble(parts[i]);
                }
                data.add(point);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
