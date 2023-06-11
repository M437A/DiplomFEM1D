package org.example.GUI;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.ElasticDeformationMES;

public class App extends Application {

    private LineChart linechart;

    @Override
    public void start(Stage stage) {
        ElasticDeformationMES solver = new ElasticDeformationMES();
        solver.calculate(4);

        NumberAxis xAxis = new NumberAxis("x", -0.1, 2.1, 0.1);
        NumberAxis yAxis = new NumberAxis("u(x)", -2, 40, 1);

        linechart = new LineChart(xAxis, yAxis);

        XYChart.Series series = new XYChart.Series();
        series.setName("Введите колличество элементов(максимальное 40)");

        for (int i = 0; i <solver.yi.length; i++) {
            if (solver.yi[i]>0 || solver.xi[i]>0) {
                series.getData().add(new XYChart.Data<>(solver.xi[i], solver.yi[i]));
                System.out.printf("%.2f - %.4f\n",solver.xi[i], solver.yi[i]);
            }

        }

        linechart.setMinWidth(900);
        linechart.setMaxWidth(900);
        linechart.setMinHeight(600);
        linechart.setMaxHeight(600);
        linechart.setCreateSymbols(false);
        linechart.getData().add(series);

        TextField movesInput = new TextField();
        Button startButton = new Button("Расчитать");
        startButton.setOnAction(action -> {
            int new_n = Integer.parseInt(movesInput.getText());
            if (new_n > 40) {
                throw new IllegalArgumentException("to many elements");
            }
            linechart.getData().clear(); //Очищает сатрые данные
            solver.calculate(new_n);    //Считает задачу
            XYChart.Series seriesB = new XYChart.Series();
            System.out.println("n = "+new_n);
            for (int i = 0; i < solver.yi.length; i++) {
                if(solver.xi[i]>0 || solver.yi[i]>0){
                    seriesB.getData().add(new XYChart.Data<>(solver.xi[i], solver.yi[i]));
                    System.out.printf("%.2f - %.4f\n",solver.xi[i], solver.yi[i]);
                }
            }
            System.out.println("------------------");
            linechart.getData().add(seriesB);
        });
        HBox hBox = new HBox();
        hBox.getChildren().addAll(movesInput, startButton);
        hBox.setAlignment(Pos.CENTER);
        hBox.setStyle("-fx-font-size: 20px");


        VBox vBox = new VBox();
        vBox.getChildren().addAll(linechart, hBox);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox, 900, 650);
        stage.setTitle("Диаграмма");
        stage.setScene(scene);
        stage.show();
    }


}
