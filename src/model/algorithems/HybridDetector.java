package model.algorithems;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import model.algorithems.models.*;
import model.data.AnomalyReport;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;

import java.util.ArrayList;
import java.util.List;

public class HybridDetector implements TimeSeriesAnomalyDetector {

    public ArrayList<Circle> circles = new ArrayList<>();
    public ArrayList<AnomalyReport> detections = new ArrayList<>();

    public List<AnomalyReport> HybridAlgorithm(TimeSeries timeSeries1, TimeSeries timeSeries2) {
        return detections;

    }

    @Override
    public void learnNormal(TimeSeries timeSeries) {
    }

    @Override
    public List<AnomalyReport> detect(TimeSeries timeSeries) {
        List<AnomalyReport> anomalyReports = new ArrayList<AnomalyReport>();
        return anomalyReports;
    }

    @Override
    public Runnable draw(Canvas canvas, CorrelatedFeatures correlatedFeatures, int timeStamp) {
        return null;
    }

}