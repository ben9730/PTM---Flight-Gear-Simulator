package model.algorithems;

import javafx.scene.canvas.Canvas;
import model.data.AnomalyReport;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;

import java.awt.*;
import java.util.List;

public interface TimeSeriesAnomalyDetector {
    void learnNormal(TimeSeries ts);

    List<AnomalyReport> detect(TimeSeries ts);

    Runnable draw(Canvas canvas, CorrelatedFeatures correlatedFeatures, int timeStamp);
}
