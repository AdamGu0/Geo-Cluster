package main;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Description: define a cluster class and it's property. A cluster contains
 * points which belong to it and its center point
 *
 * @author Rocky Chen
 */
public class Cluster implements Serializable {

    public ArrayList<Point> pointsList; // points list containing all the points
    public Point centroid;       // centroid 
    private Point farthestPoint; // farthest point from centroid in cluster
    private int vectorSize;
    public Rectangle2D gridBounds;
    public boolean isGPS;

    // constructor and inilization
    public Cluster(boolean isGPS, int vectorSize) {
        this.isGPS = isGPS;
        this.pointsList = new ArrayList();
        this.centroid = new Point(isGPS);
        this.farthestPoint = new Point(isGPS);
        this.vectorSize = vectorSize;
    }

    // calculate the centroid by averaging the points
    public void calcCentroid(Point point) {
        // no data points associated to this cluster, use the initial centroid
        if (pointsList.isEmpty()) {
            centroid = point.copyPoint();
            return;
        }
        int i, j;
        int len = pointsList.size();
        double sumValue = 0.0;
        double[] sumVectors = new double[vectorSize];

        for (i = 0; i < vectorSize; i++) {
            sumValue = 0.0;
            for (j = 0; j < len; j++) {
                sumValue += pointsList.get(j).vectors[i];
            }
            sumVectors[i] = sumValue / len;
        }

        centroid = new Point(sumVectors, isGPS);

    }

    // calculate sum-of-squared distances of cluster 
    public double calcSsd() {
        double ssd = 0.0;
        // no data points associated to this cluster
        if (pointsList.isEmpty()) {
            return ssd;
        }

        for (Point p : pointsList) {
            ssd += Math.pow(p.getDistance(centroid), 2);
        }
        return ssd;
    }

    // calculate MSE of cluster
    public double calcMSE() {
        double ssd, mse = 0.0;
        ssd = calcSsd();
        if (pointsList.isEmpty()) {
            return mse;
        }

        mse = ssd / pointsList.size();

        return mse;
    }
    
    // find the farthest point from the centroid and get its maximum distance
    public double getMaxDistance() {
        int i;
        int len = 0;
        int index = 0;
        double md = 0.0; // maximum distance
        double tempd = 0.0; // distance between any point to centriod

        len = pointsList.size();

        for (i = 0; i < len; i++) {
            tempd = pointsList.get(i).getDistance(centroid);
            if (tempd > md) {
                md = tempd;
                index = i;
            }
        }
        farthestPoint = pointsList.get(index).copyPoint(); // get the farthest point

        return md;
    }

    public Point getFstPoint() {
        double len = getMaxDistance();

        return farthestPoint;

    }

    //get one point from the pointlist
    public Point getPoint(int index) {
        int len = pointsList.size();

        if (len == 0) {
            return null;
        }
        return (Point) pointsList.get(index);
    }

    public Object deepCopy() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);

        return ois.readObject();
    }

    public int getVectorSize() {
        return vectorSize;
    }

    public void setVectorSize(int vectorSize) {
        this.vectorSize = vectorSize;
    }

    public void printCentroid() {
        if (this.centroid == null) {
            System.out.println("no centroid!");
            Point.gPointString = "";
            return;
        }
        
        this.centroid.printPoint();
        
    }
}
