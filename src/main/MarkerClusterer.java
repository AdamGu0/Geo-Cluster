/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;

/**
 *
 * @author AdamGu0
 */
public class MarkerClusterer {

    private final MapKit mapKit;
    private Point[] data;
    public int gridSize;
    private ArrayList<Cluster> clusters;
    public Cluster[] clustersArray;
    public long duration;
    public boolean startCluster = false;

    public MarkerClusterer(MapKit mapKit) {
        this.mapKit = mapKit;
    }

    public int startMarkerCluster(Point[] data, int gridSize) {
        if (data == null) {
            return 1;
        } else if (data.length == 0) {
            return 2;
        }
        this.startCluster = true;
        this.gridSize = gridSize;
        this.data = data.clone();
        doMarkerCluster();
        return 0;
    }

    public boolean doMarkerCluster() {
        if (!startCluster) return startCluster;
        
        long start = System.currentTimeMillis();
        this.clusters = new ArrayList<Cluster>();
        Rectangle2D bounds = getExtentedGeoBounds(mapKit.getMainMap().getViewportBounds());
        for (Point p : data) {
            if (containsGeoPoint(bounds, p)) {
                addToClosestCluster(p);
            }
        }
        
        clustersArray = new Cluster[this.clusters.size()];
        for (int i = 0; i < clusters.size(); i++) {
            clustersArray[i] = clusters.get(i);
        }
        duration = System.currentTimeMillis() - start;

        mapKit.setWaypoints(clustersArray);
        return startCluster;
    }

    private void addToClosestCluster(Point p) {
        
        double distance = Double.MAX_VALUE;
        Cluster clusterToAdd = null;
        for (Cluster c : clusters) {
            Point center = c.centroid;
            double d = center.getGeoDistance(p);
            if (d < distance) {
                distance = d;
                clusterToAdd = c;
            }
        }

        if ((clusterToAdd != null) && containsGeoPoint(clusterToAdd.gridBasedBounds, p)) {
            addPoint(clusterToAdd, p);
        } else {
            Cluster cluster = new Cluster(true, 2);
            addPoint(cluster, p);
            clusters.add(cluster);
        }
    }
    
    private void addPoint(Cluster cluster, Point p) {
        cluster.pointsList.add(p);
        cluster.calcCentroid(p);
        
        Point center = cluster.centroid;
        GeoPosition centerPosition = new GeoPosition(center.vectors[1], center.vectors[0]);
        Point2D p1 = mapKit.getMainMap().getTileFactory().geoToPixel(centerPosition, mapKit.getMainMap().getZoom());
        cluster.gridBasedBounds = getExtentedGeoBounds(new Rectangle((int)(p1.getX()), (int)(p1.getY()), 0, 0));
    }

    private Rectangle2D getExtentedGeoBounds(Rectangle bounds) {
        Point2D upperLeft = new Point2D.Double(bounds.x - gridSize, bounds.y - gridSize);
        Point2D lowerRight = new Point2D.Double(bounds.x + bounds.width + gridSize, bounds.y + bounds.height + gridSize);
        TileFactory tf = mapKit.getMainMap().getTileFactory();
        int zoom = mapKit.getMainMap().getZoom();
        GeoPosition ul = tf.pixelToGeo(upperLeft, zoom);
        GeoPosition lr = tf.pixelToGeo(lowerRight, zoom);
        return new Rectangle2D.Double(ul.getLongitude(), ul.getLatitude(), lr.getLongitude() - ul.getLongitude(), lr.getLatitude() - ul.getLatitude());
    }

    private boolean containsGeoPoint(Rectangle2D bounds, Point p) {
        double x0 = bounds.getX();
        double y0 = bounds.getY();
        double x = p.vectors[0];
        double y = p.vectors[1];
        return (x >= x0 && y <= y0 && x < x0 + bounds.getWidth() && y > y0 + bounds.getHeight());
    }
}
