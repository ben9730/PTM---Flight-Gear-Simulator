package model.algorithems;


import model.data.CorrelatedFeatures;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class HelperUtility {


    public static CorrelatedFeatures getMaxColumCorrelatedFeature(String value, List<CorrelatedFeatures> correlatedFeatures) {
        CorrelatedFeatures result = null;
        if (correlatedFeatures != null) {
            for (CorrelatedFeatures ptr : correlatedFeatures) {
                if (ptr.feature1.equalsIgnoreCase(value)) {
                    if (result == null) {
                        result = ptr;
                    } else {
                        if (ptr.corrlation > result.corrlation) {
                            result = ptr;
                        }
                    }
                }
            }
        }
        return result;
    }

    public static TimeSeriesAnomalyDetector loadPlugin(String directory, String className) {
        // load class directory
        try {
            URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("file://" + directory)});
            Class<?> c = urlClassLoader.loadClass("model.algorithems." + className);
            return (TimeSeriesAnomalyDetector) c.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
