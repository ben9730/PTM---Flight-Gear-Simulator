package model.player;

import model.data.TimeSeries;
import model.settings.Settings;
import view.ChangeDataListener;
import view.ChangeTimeUIListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Player {

    private int ratio;
    private int mLength;
    private TimeSeries mTimeSeries;
    private Settings mSettings;
    private final static int MILISECONDS = 1000;

    private int mCurrentTimeStamp = 0;
    private int mSeconds = 0;

    private final List<ChangeDataListener> mChangeDataListeners = new ArrayList<>();
    private final List<ChangeTimeUIListener> mChangeTimeUiListeners = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> scheduledFuture = null;


    public void setTimeSeris(TimeSeries timeSeris) {
        mTimeSeries = timeSeris;
        mLength = timeSeris.getSizeOfAllFeatures() / ratio;
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
        ratio = MILISECONDS / settings.getDataSamplingRate();
    }

    public void addChangeDataListener(ChangeDataListener changeDataListener) {
        mChangeDataListeners.add(changeDataListener);
    }

    public void addTimeChangeListener(ChangeTimeUIListener changeTimeUIListener) {
        mChangeTimeUiListeners.add(changeTimeUIListener);
    }

    public void play() {
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Playing time stamp - " + mCurrentTimeStamp);
            setTimeStamp(mCurrentTimeStamp + 1);
            if (mCurrentTimeStamp % ratio == 0) {
                setClock(mSeconds + 1);
            }

        }, 0, mSettings.getDataSamplingRate(), TimeUnit.MILLISECONDS);


    }

    public void pause() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }

    public void setTimeStamp(int timeStamp) {
        mCurrentTimeStamp = timeStamp;
        float[] data = mTimeSeries.getLineOfFloat(mCurrentTimeStamp);
        for (ChangeDataListener changeDataListener : mChangeDataListeners) {
            changeDataListener.onChangedData(mCurrentTimeStamp, data);
        }
    }

    public void setClock(int seconds) {
        mSeconds = seconds;
        for (ChangeTimeUIListener changeTimeUIListener : mChangeTimeUiListeners) {
            changeTimeUIListener.onChangedTime(seconds);
        }
    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        setClock(0);
        setTimeStamp(0);
    }

    public int getLength() {
        return mTimeSeries.getSizeOfAllFeatures();
    }

    public void injectTimeStamp(int value) {
        setTimeStamp(value);
        setClock(value / ratio);
    }

    public int getCurrentTimeStamp() {
        return mCurrentTimeStamp;
    }
}
