package model.data;

public class StatLib {

    // simple average
    public static float avg(float[] x) {
        float sum = 0;
        for (float var : x) {
            sum += var;
        }

        return sum / (x.length);
    }

    // returns the variance of X and Y
    public static float var(float[] x) {
        float sum = 0;
        float avgPow2;
        float average;
        for (float var : x) {
            sum += var * var;
        }
        avgPow2 = sum / x.length;
        average = avg(x);
        return avgPow2 - (average * average);
    }

    // returns the covariance of X and Y
    public static float cov(float[] x, float[] y) {
        float sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += (x[i] - avg(x)) * (y[i] - avg(y));
        }
        return sum / x.length;
    }

    // returns the Pearson correlation coefficient of X and Y
    public static float pearson(float[] x, float[] y) {
        float standardDeviationX;
        float standardDeviationY;
        standardDeviationX = (float) Math.sqrt(var(x));
        standardDeviationY = (float) Math.sqrt(var(y));
        if (standardDeviationX == 0 || standardDeviationY == 0) {
            return Float.MIN_VALUE;
        }
        return cov(x, y) / (standardDeviationX * standardDeviationY);
    }

    // performs a linear regression and returns the line equation
    public static Line linear_reg(Point[] points) {
        float covXY = 0;
        float varX = 0;
        float a;
        float b;
        float avgX = 0;
        float avgY = 0;
        float[] arrx = new float[points.length];
        float[] arry = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            arrx[i] = points[i].x;
            arry[i] = points[i].y;
        }
        covXY = cov(arrx, arry);
        varX += var(arrx);

        a = covXY / varX;
        for (Point p : points) {
            avgX += p.x;
            avgY += p.y;
        }
        avgX /= points.length;
        avgY /= points.length;
        b = avgY - (avgX * a);
        return new Line(a, b);
    }

    // returns the deviation between point p and the line equation of the points
    public static float dev(Point p, Point[] points) {
        Line l = linear_reg(points);
        float y = 0;
        y = (p.x * l.a) + l.b;
        return Math.abs(y - p.y);
    }


    // returns the deviation between point p and the line
    public static float dev(Point p, Line l) {
        float y = 0;
        y = (p.x * l.a) + l.b;
        return Math.abs(y - p.y);
    }
}
