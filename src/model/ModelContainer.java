package model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import model.algorithems.*;
import model.connection.flightgear.SocketSender;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;
import model.player.Player;
import model.settings.Property;
import model.settings.Settings;
import view.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ModelContainer {
    public static final String Rudder = "Rudder";
    public static final String Throttle = "Throttle";
    public static final String AirSpeed = "AirSpeed";
    public static final String Altimeter = "Altimeter";
    public static final String Aileron = "Aileron";
    public static final String Elevator = "Elevator";
    public static final String Roll = "Roll";
    public static final String Pitch = "Pitch";
    public static final String Yaw = "Yaw";


    private Settings mSettings = null;
    private TimeSeries mTimeSeris = null;
    private Player mPlayer = new Player();
    private SocketSender mFlightGearConnection = null;
    private ChangeChartListener mChangeChartListener = null;
    private UpdateDetectorUIListener mUpdateDetectorUiListener = null;
    private CorrelatedFeatures mCorleatedToTheSelected = null;
    private UpdateCanvas mCanvasListener = null;

    private final ArrayList<TimeSeriesAnomalyDetector> mDetectors = new ArrayList<>();
    private final List<CorrelatedFeatures> mCorleatedFeatureResult = new ArrayList();

    private TimeSeriesAnomalyDetector mSelectedAlgo = null;
    Future longRunningTaskFuture = null;
    ExecutorService executorService = Executors.newSingleThreadExecutor();


    public void setSettings(Settings settings) {
        this.mSettings = settings;
        mPlayer.setSettings(settings);
        mFlightGearConnection = new SocketSender(settings.getFlightGearPort());
        addChangeDataListener(mFlightGearConnection);
    }

    public void setChangeChartListener(ChangeChartListener listener) {
        mChangeChartListener = listener;
    }

    public void setCanvasListener(UpdateCanvas canvasListener) {
        mCanvasListener = canvasListener;
    }

    public void setTimeSeris(TimeSeries timeSeris) {
        this.mTimeSeris = timeSeris;
        mPlayer.setTimeSeris(timeSeris);

        new Thread(() -> {
            SimpleAnomalyDetector simpleAnomalyDetector = new SimpleAnomalyDetector();
            simpleAnomalyDetector.learnNormal(timeSeris);
            mCorleatedFeatureResult.addAll(simpleAnomalyDetector.getNormalModel());
            addDetector(simpleAnomalyDetector);
            ZscoreDetector zscoreDetector = new ZscoreDetector();
            zscoreDetector.detect(timeSeris);
            zscoreDetector.learnNormal(timeSeris);
            addDetector(zscoreDetector);
            HybridDetector hybridDetector = new HybridDetector(timeSeris,zscoreDetector,simpleAnomalyDetector);
            addDetector(hybridDetector);
        }).start();

    }

    public void addDetector(TimeSeriesAnomalyDetector timeSeriesAnomalyDetector) {
        mDetectors.add(timeSeriesAnomalyDetector);
        if (mUpdateDetectorUiListener != null) {
            mUpdateDetectorUiListener.onUpdateDetector(timeSeriesAnomalyDetector.getClass().getSimpleName());
        }
    }

    public boolean isSettingSet() {
        return mSettings != null;
    }

    public void play() {
        mPlayer.play();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void stop() {
        mPlayer.stop();
    }

    public Property getProperty(String name) {
        for (Property property : mSettings.getPropertyList()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    public int getPropertyColumNumber(String name) {
        for (Property property : mSettings.getPropertyList()) {
            if (property.getName().equals(name)) {
                return property.getColumNumber();
            }
        }
        return 0;
    }

    public void addChangeDataListener(ChangeDataListener changeDataListener) {
        mPlayer.addChangeDataListener(changeDataListener);
    }

    public void addTimeDataListener(ChangeTimeUIListener changeTimeUIListener) {
        mPlayer.addTimeChangeListener(changeTimeUIListener);
    }

    public double getLength() {
        return mPlayer.getLength();
    }

    public void changeTimeStamp(int value) {
        mPlayer.injectTimeStamp(value);
    }

    public void onSelectedColumnFromList(String value) {
        if (mChangeChartListener != null) {
            float[] selectedData = mTimeSeris.getArrayFromStringUntilTimeStamp(value, mPlayer.getCurrentTimeStamp());
            mCorleatedToTheSelected = HelperUtility.getMaxColumCorrelatedFeature(value, mCorleatedFeatureResult);
            float[] corelteadData = mTimeSeris.getArrayFromStringUntilTimeStamp(mCorleatedToTheSelected != null ? mCorleatedToTheSelected.feature2 : "", mPlayer.getCurrentTimeStamp());
            mChangeChartListener.onChangedChartDisplay(selectedData, corelteadData);
            updateCanvas();
        }
    }

    public int getColumNumberFromSelectedColumn(String selectedItem) {
        return mTimeSeris.getIndexOfFeature(selectedItem);
    }

    public void setUpdateDetectorUiListener(UpdateDetectorUIListener mUpdateDetectorUiListener) {
        this.mUpdateDetectorUiListener = mUpdateDetectorUiListener;
    }

    public CorrelatedFeatures getCorletedFeature() {
        return mCorleatedToTheSelected;
    }

    public void setSelectedAlgo(String selectedAlgo) {
        for (TimeSeriesAnomalyDetector detector : mDetectors) {
            if (detector.getClass().getSimpleName().equals(selectedAlgo)) {
                mSelectedAlgo = detector;
                updateCanvas();
                break;
            }
        }
    }

    private void updateCanvas() {
        Canvas canvas = mCanvasListener.getCanvasToDrawOn();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (mCorleatedToTheSelected != null && mSelectedAlgo != null) {
            if (longRunningTaskFuture != null) {
                longRunningTaskFuture.cancel(true);
                executorService = Executors.newSingleThreadExecutor();
            }
            longRunningTaskFuture = executorService.submit(mSelectedAlgo.draw(canvas, mCorleatedToTheSelected, mPlayer.getCurrentTimeStamp()));
        }

    }


}