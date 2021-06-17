package viewmodel;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ModelContainer;
import model.algorithems.HelperUtility;
import model.algorithems.TimeSeriesAnomalyDetector;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;
import model.settings.FlightGearSettingsReader;
import model.settings.Settings;
import view.*;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static model.ModelContainer.*;

public class Controller implements Initializable, ChangeDataListener, ChangeTimeUIListener, ChangeChartListener, UpdateDetectorUIListener, UpdateCanvas {

    private Stage mMainStage;
    private final ModelContainer mModel = new ModelContainer();

    @FXML
    private ListView mCsvColums;

    //Joystick
    @FXML
    private Slider mRudder, mThrottle;
    @FXML
    private Circle mSmallCircle;

    //Dashboard
    @FXML
    private Label mSpeed, mAltimeter, mRoll, mPitch, mYaw;


    //Charts
    @FXML
    private LineChart mColumnSelectedChart, mCorleatedChart;
    private XYChart.Series mSeriesSelected, mSerisesCorleated;

    //Detectors
    @FXML
    private ChoiceBox mDetectorList;
    @FXML
    private Button mAddDetectorButton;

    //Player
    @FXML
    private ImageView mPlay, mPause, mStop;
    @FXML
    private Slider mPlayerSlider;
    @FXML
    private Label mTime;

    //Canvas
    @FXML
    private Canvas mCanvas;

    private void configureSliders() {
        mRudder.setMin(mModel.getProperty(Rudder).getMinRange());
        mRudder.setMax(mModel.getProperty(Rudder).getMaxRange());
        mThrottle.setMin(mModel.getProperty(Throttle).getMinRange());
        mThrottle.setMax(mModel.getProperty(Throttle).getMaxRange());


    }


    private void configurePlayer() {
        mPlay.setOnMouseClicked(mouseEvent -> {
            mModel.play();
            mPlayerSlider.setDisable(true);
        });
        mPause.setOnMouseClicked(mouseEvent -> {
            mModel.pause();
            mPlayerSlider.setDisable(false);
        });
        mStop.setOnMouseClicked(mouseEvent -> {
            mModel.stop();
            mSerisesCorleated.getData().clear();
            mSeriesSelected.getData().clear();
            mPlayerSlider.setDisable(false);
        });

        mPlayerSlider.valueChangingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                mModel.changeTimeStamp((int) mPlayerSlider.getValue());
            }
        });
        mPlayerSlider.setMin(0);
        mPlayerSlider.setMax(mModel.getLength());
        mPlayerSlider.setDisable(false);
    }

    private void loadExternalDetector() {
        if (!mModel.isSettingSet()) {
            showAlertMessage(Alert.AlertType.ERROR, "Empty Settings");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("External detector");
        fileChooser.setInitialDirectory(new File("./resources"));
        File selectedFile = fileChooser.showOpenDialog(mMainStage);
        if (selectedFile != null) {
            try {
                String dir = selectedFile.getParent();
                String nameOfClass = selectedFile.getName().replace(".class", "");
                TimeSeriesAnomalyDetector result = HelperUtility.loadPlugin(dir, nameOfClass);
                if (result != null) {
                    showAlertMessage(Alert.AlertType.ERROR, "External plugin failed");
                } else {
                    mModel.addDetector(result);
                }

                configurePlayer();
            } catch (Exception e) {

            }
        }

    }

    public void setMainStage(Stage primaryStage) {
        mMainStage = primaryStage;
    }

    private void showAlertMessage(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mModel.addChangeDataListener(this);
        mModel.addTimeDataListener(this);
        mPlayerSlider.setDisable(true);
        mColumnSelectedChart.setCreateSymbols(false);
        mCorleatedChart.setCreateSymbols(false);
        mModel.setChangeChartListener(this);
        mModel.setCanvasListener(this);
        mModel.setUpdateDetectorUiListener(this);
        mCsvColums.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> mModel.onSelectedColumnFromList((String) observableValue.getValue()));
        mAddDetectorButton.setOnMouseClicked(mouseEvent -> {
            loadExternalDetector();
        });
        mDetectorList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                String selectedAlgo = (String) mDetectorList.getItems().get(t1.intValue());
                mModel.setSelectedAlgo(selectedAlgo);
            }
        });


    }

    @Override
    public void onChangedData(int timeStamp, float[] values) {
        Platform.runLater(() -> {
            //DashBoard
            mSpeed.setText(formatDashBoardNumber(values[mModel.getPropertyColumNumber(AirSpeed)]));
            mAltimeter.setText(formatDashBoardNumber(values[mModel.getPropertyColumNumber(Altimeter)]));
            mAltimeter.setText(formatDashBoardNumber(values[mModel.getPropertyColumNumber(Altimeter)]));
            mRoll.setText(formatDashBoardNumber(values[mModel.getPropertyColumNumber(Roll)]));
            mPitch.setText(formatDashBoardNumber(values[mModel.getPropertyColumNumber(Pitch)]));
            mYaw.setText(formatDashBoardNumber(values[mModel.getPropertyColumNumber(Yaw)]));

            //Joystic
            mThrottle.setValue(values[mModel.getPropertyColumNumber(Throttle)]);
            mRudder.setValue(values[mModel.getPropertyColumNumber(Rudder)]);
            mSmallCircle.setCenterX(values[mModel.getPropertyColumNumber(Aileron)] * 50);
            mSmallCircle.setCenterY(values[mModel.getPropertyColumNumber(Elevator)] * 50);

            //Player
            mPlayerSlider.setValue(timeStamp);
            if (timeStamp % 50 == 0) {
                if (mSeriesSelected != null) {
                    mSeriesSelected.getData().add(new XYChart.Data(String.valueOf(timeStamp), values[mModel.getColumNumberFromSelectedColumn((String) mCsvColums.getSelectionModel().getSelectedItem())]));
                }
                CorrelatedFeatures correlatedFeatures = mModel.getCorletedFeature();
                if (correlatedFeatures != null) {
                    mSerisesCorleated.getData().add(new XYChart.Data(String.valueOf(timeStamp), values[mModel.getColumNumberFromSelectedColumn(correlatedFeatures.feature2)]));
                }
            }
        });

    }

    private String formatDashBoardNumber(float value) {
        return String.format("%.2f", value);
    }

    @Override
    public void onChangedTime(long seconds) {
        Platform.runLater(() -> {
            mTime.setText(String.format("%02d:%02d", (seconds % 3600) / 60, seconds % 60));
        });
    }

    public void onOpenCsvClicked() {
        if (!mModel.isSettingSet()) {
            showAlertMessage(Alert.AlertType.ERROR, "Empty Settings");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CsvData");
        fileChooser.setInitialDirectory(new File("./resources"));
        File selectedFile = fileChooser.showOpenDialog(mMainStage);
        if (selectedFile != null) {
            try {
                TimeSeries timeSeris = new TimeSeries(selectedFile.getAbsolutePath());
                mModel.setTimeSeris(timeSeris);
                mCsvColums.getItems().removeAll();
                mCsvColums.getItems().addAll(timeSeris.getAllFeathresNames());
                configureSliders();
                configurePlayer();

            } catch (Exception e) {

            }
        }
    }

    public void onOpenSettingsClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Settings");
        fileChooser.setInitialDirectory(new File("./resources"));
        File selectedFile = fileChooser.showOpenDialog(mMainStage);
        Settings settings;
        try {
            settings = FlightGearSettingsReader.getUserSettings(selectedFile.getAbsolutePath());
        } catch (Exception e) {
            settings = FlightGearSettingsReader.getCachedUserSettings();
        }
        if (settings != null) {
            mModel.setSettings(settings);
        }
        showAlertMessage(settings == null ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION, settings == null ? "Settings load failed\n please try again!" : "Settings loaded successful");
    }


    @Override
    public void onChangedChartDisplay(float[] selectedChart, float[] corrleatedChart) {
        mSeriesSelected = new XYChart.Series();
        mSerisesCorleated = new XYChart.Series();
        for (int i = 0; i < selectedChart.length; i += 50) {
            mSeriesSelected.getData().add(new XYChart.Data(String.valueOf(i), selectedChart[i]));
        }
        for (int i = 0; i < corrleatedChart.length; i += 50) {
            mSerisesCorleated.getData().add(new XYChart.Data(String.valueOf(i), corrleatedChart[i]));
        }

        mColumnSelectedChart.getData().clear();
        mColumnSelectedChart.getData().addAll(mSeriesSelected);

        mCorleatedChart.getData().clear();
        mCorleatedChart.getData().addAll(mSerisesCorleated);

    }

    @Override
    public void onUpdateDetector(String name) {
        mDetectorList.getItems().add(name);
    }

    @Override
    public Canvas getCanvasToDrawOn() {
        return mCanvas;
    }
}
