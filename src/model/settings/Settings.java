package model.settings;

import org.w3c.dom.Document;

import java.util.List;

public class Settings {
    private static final String DATA_SAMPLING_RATE_TAG = "DataSamplingRate";
    private static final String FLIGHT_GEAR_PORT = "FlightGearPort";

    private List<Property> mPropertyList;
    private int mDataSamplingRate;
    private int mFlightGearPort;

    public static Settings fromDoc(Document doc) {
        Settings settings = new Settings();
        settings.setPropertyList(PropertyList.fromDoc(doc));
        settings.setDataSamplingRate(Integer.parseInt(doc.getElementsByTagName(DATA_SAMPLING_RATE_TAG).item(0).getTextContent().trim()));
        settings.setFlightGearPort(Integer.parseInt(doc.getElementsByTagName(FLIGHT_GEAR_PORT).item(0).getTextContent().trim()));
        return settings;
    }


    public List<Property> getPropertyList() {
        return mPropertyList;
    }

    private void setPropertyList(List<Property> list) {
        mPropertyList = list;
    }

    public int getDataSamplingRate() {
        return mDataSamplingRate;
    }

    public void setDataSamplingRate(int mDataSamplingRate) {
        this.mDataSamplingRate = mDataSamplingRate;
    }


    public int getFlightGearPort() {
        return mFlightGearPort;
    }

    public void setFlightGearPort(int port) {
        mFlightGearPort = port;
    }
}
