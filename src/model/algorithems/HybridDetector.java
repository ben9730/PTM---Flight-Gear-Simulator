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
    private SimpleAnomalyDetector simpleAnomalyDetector;
    private ZscoreDetector zscoreDetector;
    private TimeSeries ts;
    private static final float highCorelation = (float) 0.98;
    private static final float lowCorelation = (float) 0.5;

    public HybridDetector(TimeSeries timeSeris, ZscoreDetector zscoreDetector, SimpleAnomalyDetector simpleAnomalyDetector) {
        this.ts = timeSeris;
        this.zscoreDetector = zscoreDetector;
        this.simpleAnomalyDetector = simpleAnomalyDetector;

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
        if (correlatedFeatures.corrlation > highCorelation) {
            return simpleAnomalyDetector.draw(canvas, correlatedFeatures, timeStamp);
        } else if (correlatedFeatures.corrlation < lowCorelation) {
            return simpleAnomalyDetector.draw(canvas, correlatedFeatures, timeStamp);
        }
        return () -> {

        };
    }

}