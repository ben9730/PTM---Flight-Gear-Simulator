package model.algorithems;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import model.data.*;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

    private ArrayList<CorrelatedFeatures> mCorrelatedFeatures = new ArrayList<>();
    private TimeSeries ts = null;

    @Override
    //run on all the column for each column use the pearson fun in statlib and check it, two variable maxPearson and his index
    // Crate arraylist of correlatedFeatures  add the maxperson and index, need to implant the Correlatedfeatures use in statlib and calculate the threshold
    public void learnNormal(TimeSeries ts) {
        this.ts = ts;
        //4
        int num_of_features = ts.getNumberOfFeatures();
        //100
        int featue_Size = ts.getSizeOfAllFeatures();
        for (int i = 0; i < num_of_features; i++) {
            for (int j = 0; j < num_of_features; j++) {
                if (i != j) {
                    float[] fi = ts.getArrayListFromIndex(i);
                    float[] fj = ts.getArrayListFromIndex(j);
                    float corleation = StatLib.pearson(fi, fj);
                    //0.99
                    if (corleation != Float.MIN_VALUE) {
                        String featureI = ts.getFeatureName(i);
                        String featureJ = ts.getFeatureName(j);
                        if (features_already_in(featureI, featureJ)) {
                            continue;
                        }
                        mCorrelatedFeatures.add(new CorrelatedFeatures(featureI, featureJ, corleation, null, 0));
                    }
                }
            }


        }
        ArrayList<ArrayList<Point>> arrayListsOfPoints = new ArrayList<ArrayList<Point>>();

        int NUM_OF_CORRELATED_FEATURES = mCorrelatedFeatures.size();
        for (int i = 0; i < NUM_OF_CORRELATED_FEATURES; i++) {
            ArrayList<Point> points = new ArrayList<>();
            float[] f1 = ts.getArrayFromString(mCorrelatedFeatures.get(i).feature1);
            float[] f2 = ts.getArrayFromString(mCorrelatedFeatures.get(i).feature2);
            for (int j = 0; j < featue_Size; j++) {
                float x = f1[j];
                float y = f2[j];
                points.add(j, new Point(x, y));
            }
            arrayListsOfPoints.add(points);
        }

        for (int i = 0; i < NUM_OF_CORRELATED_FEATURES; i++) {
            Point[] pointsConverted = new Point[arrayListsOfPoints.get(i).size()];
            int r = 0;

            for (Point point : arrayListsOfPoints.get(i)) {
                pointsConverted[r++] = (point != null ? point : new Point(0, 0)); // Or whatever default you want.
            }

            CorrelatedFeatures cr = mCorrelatedFeatures.get(i);
            mCorrelatedFeatures.set(i, new CorrelatedFeatures(cr.feature1, cr.feature2, cr.corrlation, StatLib.linear_reg(pointsConverted), 0.0f));
        }
        for (int i = 0; i < NUM_OF_CORRELATED_FEATURES; i++) {
            float max_deviation = 0.0f;
            for (int j = 0; j < featue_Size; j++) {
                float deviation;
                Point p = arrayListsOfPoints.get(i).get(j);
                Line line = mCorrelatedFeatures.get(i).lin_reg;
                deviation = StatLib.dev(p, line);
                if (max_deviation < deviation) {
                    max_deviation = deviation;
                }
                CorrelatedFeatures cr = mCorrelatedFeatures.get(i);
                mCorrelatedFeatures.set(i, new CorrelatedFeatures(cr.feature1, cr.feature2, cr.corrlation, cr.lin_reg, max_deviation * 1.1f));

            }

        }


    }


    @Override
    public List<AnomalyReport> detect(TimeSeries ts) {
        ArrayList<AnomalyReport> anomalyReports = new ArrayList<>();
        int num_of_features = ts.getNumberOfFeatures();
        //100
        int size = ts.getSizeOfAllFeatures();
        for (int i = 0; i < mCorrelatedFeatures.size(); i++) {
            if (mCorrelatedFeatures.get(i).corrlation >= 0.9) {
                for (int j = 0; j < size; j++) {
                    float[] row = ts.getLineOfFloat(j);
                    int featureOneIndex = ts.getIndexOfFeature(mCorrelatedFeatures.get(i).feature1);
                    int featureTwoIndex = ts.getIndexOfFeature(mCorrelatedFeatures.get(i).feature2);
                    float x = row[featureOneIndex];
                    float y = row[featureTwoIndex];
                    Point point = new Point(x, y);
                    float deviation = StatLib.dev(point, mCorrelatedFeatures.get(i).lin_reg);
                    if (deviation > mCorrelatedFeatures.get(i).threshold) {
                        String description = String.format("%s-%s", mCorrelatedFeatures.get(i).feature1, mCorrelatedFeatures.get(i).feature2);
                        anomalyReports.add(new AnomalyReport(description, j + 1));
                    }

                }
            }
        }

        return anomalyReports;
    }

    @Override
    public Runnable draw(Canvas canvas, CorrelatedFeatures correlatedFeatures, int timeStamp) {
        return () -> {
            List<Point> points = new ArrayList<>();
            float arr[] = ts.getArrayFromString(correlatedFeatures.feature1);
            float arr2[] = ts.getArrayFromString(correlatedFeatures.feature2);
            int until = Math.max(timeStamp - 100, 0);
            for (int i = timeStamp - 1; i > until; i--) {
                points.add(new Point(arr[i], arr2[i]));
            }
            Line l = correlatedFeatures.lin_reg;
            canvas.getGraphicsContext2D().setStroke(Color.BLACK);
            canvas.getGraphicsContext2D().strokeLine(1 % 200, l.f(1) % 200, timeStamp % 200, l.f(timeStamp) % 200);
            canvas.getGraphicsContext2D().setStroke(Color.BLUE);
            canvas.getGraphicsContext2D().setLineWidth(2);
            for (Point p : points) {
                canvas.getGraphicsContext2D().strokeLine((50 * p.x) % 200, (50 * p.y) % 200, (50 * p.x % 200), (50 * p.y % 200));
            }
        };
    }

    public List<CorrelatedFeatures> getNormalModel() {
        return mCorrelatedFeatures;
    }

    public boolean features_already_in(String feature1, String feature2) {
        for (CorrelatedFeatures cf : mCorrelatedFeatures) {
            if (cf.feature1.equals(feature2) && cf.feature2.equals(feature1)) {
                return true;
            }
        }
        return false;
    }


}
