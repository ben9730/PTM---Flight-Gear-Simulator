package model.algorithems;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import model.data.AnomalyReport;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZscoreDetector implements TimeSeriesAnomalyDetector {

    HashMap<String, Float> mTresholdsMap = new HashMap<>();
    HashMap<String, ArrayList<Float>> mzScores = new HashMap<>();

    private List<AnomalyReport> reports = new ArrayList<>();

    public void learnNormal(TimeSeries ts) {
        List<String> features = ts.getAllFeathresNames();
        float maxZscore, currentZscore;
        for (int i = 0; i < features.size(); i++) {
            maxZscore = Float.MIN_VALUE;
            for (int j = 0; j < ts.getSizeOfAllFeatures(); j++) {
                currentZscore = calculateZScore(ts.getArrayListFromString(features.get(i)), j);
                if (mzScores.containsKey(features.get(i))) {
                    mzScores.get(features.get(i)).add(currentZscore);
                } else {
                    mzScores.put(features.get(i), new ArrayList<>());
                    mzScores.get(features.get(i)).add(currentZscore);
                }
                if (currentZscore > maxZscore) {
                    maxZscore = currentZscore;
                }
            }
            mTresholdsMap.put(features.get(i), maxZscore);
        }
    }

    public List<AnomalyReport> detect(TimeSeries ts) {
        List<String> features = ts.getAllFeathresNames();
        List<AnomalyReport> liveReports = new ArrayList<AnomalyReport>();
        float maxZScore, currentZScore = 0;
        for (int i = 0; i < features.size(); i++) {
            maxZScore = Float.MIN_VALUE;
            for (int j = 0; j < ts.getSizeOfAllFeatures(); j++) {
                currentZScore = calculateZScore(ts.getArrayListFromString(features.get(i)), j);
                if (currentZScore > maxZScore) {
                    maxZScore = currentZScore;
                }
            }
            if (mTresholdsMap.containsKey(features.get(i))) {
                if (mTresholdsMap.get(features.get(i)) > maxZScore) {
                    AnomalyReport report = new AnomalyReport(features.get(i), i + 1); //add a new detected object
                    liveReports.add(report);
                }
            }
        }
        reports.clear();
        reports.addAll(liveReports);
        return liveReports;
    }

    @Override
    public Runnable draw(Canvas canvas, CorrelatedFeatures correlatedFeatures, int timeStamp) {
        return () -> {
            ArrayList<Float> points = new ArrayList<>();
            ArrayList<Float> list = mzScores.get(correlatedFeatures.feature1);
            for (int i = 0; i < timeStamp; i++)
                points.add(list.get(i));

            canvas.getGraphicsContext2D().setStroke(Color.BLACK);
            for (int i = 0; i < points.size() - 1; i++) {
                canvas.getGraphicsContext2D().strokeLine(points.get(i) % 200, i % 200, points.get(i + 1) % 200, (i + 1) % 200);
            }
        };
    }

    private float calculateZScore(ArrayList<Float> parameters, int index) {
        float mean = calculateMean(parameters, index);
        float sd = calculateSD(parameters, mean, index);
        float ZScore = Math.abs(parameters.get(index) - mean) / sd;
        return ZScore;
    }

    private float calculateSD(ArrayList<Float> parameters, float mean, int index) {
        float standardDeviation = 0;

        for (int i = 0; i < index; i++) {
            standardDeviation += Math.pow(parameters.get(i) - mean, 2);
        }
        return standardDeviation;
    }

    private float calculateMean(ArrayList<Float> parameters, int index) {
        float sum = 0, mean;
        for (int i = 0; i < index; i++) {
            sum += parameters.get(i);
        }
        mean = sum / index + 1;
        return mean;
    }
}
