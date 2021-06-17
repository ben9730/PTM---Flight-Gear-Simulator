package model.data;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TimeSeries {
    //A,B,C,D
    // 0 -> "A"
    // 1 -> "B"...
    // key,Value
    private HashMap<Integer, String> mFeaturesName = new HashMap<>();
    private List<String> mFeaturesNameList = new ArrayList<>();

    //"A" -> 1.0,5.0,6.0
    //"B" ->
    // key,Value
    private HashMap<String, ArrayList<Float>> mData = new HashMap<>();

    private ArrayList<ArrayList<Float>> mRows = new ArrayList<ArrayList<Float>>();

    private int mSize;

    public TimeSeries(String csvFileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFileName));
            String line;
            if ((line = br.readLine()) != null) //read the first line of the title name
            {
                int i = 0;
                String[] values = line.split(",");
                for (String v : values) {
                    if (mFeaturesName.containsValue(v)) {
                        String newName = v + "1";
                        mFeaturesName.put(i++, newName); // put the data where key index i, the value v
                        mFeaturesNameList.add(newName);
                    } else {
                        mFeaturesName.put(i++, v); // put the data where key index i, the value v
                        mFeaturesNameList.add(v);
                    }

                }
            }

            while ((line = br.readLine()) != null) // read the rest of the file
            {
                int i = 0;
                String[] values = line.split(",");
                ArrayList<Float> floats = new ArrayList<>();
                for (String v : values) {
                    String featureName = mFeaturesName.get(i);
                    if (mData.get(featureName) == null) {
                        mData.put(featureName, new ArrayList<Float>());
                    }
                    mData.get(featureName).add(Float.parseFloat(v));
                    i++;
                    floats.add(Float.parseFloat(v));

                }
                mRows.add(floats);
                mSize++;
            }
        } catch (Exception ignore) {

        }
    }

    // "A
    public String getFeatureName(Integer index) {
        return mFeaturesName.get(index);
    }

    public int getIndexOfFeature(String feature) {
        Iterator it = mFeaturesName.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue().equals(feature)) {
                return (int) pair.getKey();
            }
        }
        return 0;
    }

    //Nunmber of all features ( A,b,c,d - 4)
    public int getNumberOfFeatures() {
        return mFeaturesName.size();
    }

    //Number of all rows (100)
    public int getSizeOfAllFeatures() {
        return mSize;
    }

    //Enter name and get all the colum
    public float[] getArrayFromString(String name) {
        List<Float> floatList = mData.get(name);
        //From here it's convert the list to rtegular arry
        float[] floatArray = new float[floatList.size()];
        int i = 0;

        for (Float f : floatList) {
            floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        return floatArray;
    }

    public float[] getArrayFromStringUntilTimeStamp(String name, int timeStamp) {
        List<Float> floatList = mData.get(name);
        if (floatList == null) {
            return new float[0];
        }
        //From here it's convert the list to rtegular arry
        float[] floatArray = new float[timeStamp];
        int i = 0;
        if (floatList != null) {
            for (Float f : floatList) {
                if (i == timeStamp) {
                    break;
                }
                floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
                if (i == timeStamp) {
                    break;
                }
            }
        }
        return floatArray;
    }

    //Enter index and get the colum
    public float[] getArrayListFromIndex(Integer index) {
        String nameOfFeature = mFeaturesName.get(index);
        return getArrayFromString(nameOfFeature);
    }

    public float[] getLineOfFloat(Integer line) {
        ArrayList<Float> currentLine = mRows.get(line);
        float[] floatArray = new float[currentLine.size()];
        int i = 0;

        for (Float f : currentLine) {
            floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        return floatArray;
    }

    public ArrayList<Float> getArrayListFromString(String s) {
        return mData.get(s);
    }

    public List<String> getAllFeathresNames() {
        return mFeaturesNameList;
    }
}
