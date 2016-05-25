package main;

import java.io.Serializable;

/**
 * Description: A point with multi-dimension vectors
 *
 * @author Rocky Chen
 * @date 01.02.2010
 */
public class Point implements Serializable {

    public double[] vectors; // conation multi-dimension value
    public static String gPointString = "";
    public boolean isGPS;
    public int index = -1;
    
    // construct point object
    public Point(double[] _vectors, boolean isGPS) {
        this.isGPS = isGPS;
        this.vectors = _vectors;
    }
    // construct point object without parameters

    public Point(boolean isGPS) {
        this.isGPS = isGPS;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    //not used
    public double getDistance_(Point point) { // d in meter
        double radLat1 = rad(this.vectors[1]);
        double radLat2 = rad(point.vectors[1]);
        double a = radLat1 - radLat2;
        double b = rad(this.vectors[0]) - rad(point.vectors[0]);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6371; // Radius of the Earth in km
        s = Math.round(s * 1000);
        return s;
    }
    
    private double getGeoDistance(Point p) { // d in kilo
        double R = 6371; // Radius of the Earth in km
        double dLat = (this.vectors[1] - p.vectors[1]) * Math.PI / 180;
        double dLon = (this.vectors[0] - p.vectors[0]) * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(this.vectors[1] * Math.PI / 180) * Math.cos(p.vectors[1] * Math.PI / 180)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // calculate the euclidean distance with another point
    public double getDistance(Point point) {
        if (isGPS) return getGeoDistance(point);
        
        double distance = 0.0;
        for (int i = 0; i < vectors.length; i++) {
            distance += (vectors[i] - point.vectors[i]) * (vectors[i]
                    - point.vectors[i]);
        }
        return Math.sqrt(distance);
    }

    @Override
    public String toString() {
        if (isGPS) return toGeoString();
        String str = "";
        for (int i = 0; i < vectors.length; i++) {
            str += ", " + vectors[i];
        }
        return "(" + str.substring(2) + ")";
    }
    
    private String toGeoString() {
        return "Lon:" + vectors[0] + " Lat:" + vectors[1];
    }
    
    // copy the point itself
    public Point copyPoint() {
        Point point;
        if (vectors == null) {
            point = new Point(this.isGPS);
        } else {
            point = new Point(vectors, this.isGPS);
        }

        point.index = index;

        return point;
    }

    public void printPoint() {
        if (this.vectors == null) {
            return;
        }
        String pointString = "X:" + String.valueOf(vectors[0]);
        for (int i = 1; i < vectors.length; i++) {
            pointString += "\n   Y:" + String.valueOf(vectors[i]) + "\n";
        }
        gPointString = pointString;
        System.out.println(pointString);
        this.toString();
    }
    


}
