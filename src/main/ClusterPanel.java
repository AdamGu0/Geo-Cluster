/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;

/**
 *
 * @author AdamGu0
 */
public class ClusterPanel extends JPanel {

    private ReadData rd;
    public Cluster[] clusters;
    private Graphics gs;
    
    public ClusterPanel(ReadData _rd) {
        super();
        rd = _rd;
    }
    
    @Override
    public void paint(Graphics g) {        
        gs = g;
        gs.setColor(Color.WHITE);
        gs.clearRect(0, 0, this.getWidth(), this.getHeight());
        if ( clusters != null) draw();
    }
    
    public void showClusters(Cluster[] _clusters) {
        clusters = _clusters;
        this.repaint();
    }
    
    private void draw() {
        int len = clusters.length;
        int i;
        for (i = 0; i < len; i++) {
            drawCluster(clusters[i], selectSymbol(i), selectColor(i));
        }
    }
        
    private void drawCluster(Cluster cluster, String str, Color color) {
        ArrayList<Point> points = cluster.pointsList;
        Point centroid = cluster.centroid.copyPoint();

        // draw all the points of cluster
        if (points != null) {
            drawPoints(points, str, color);
        }

        // draw centroid of cluster
        drawPoint(centroid, Color.RED);
    }
    
    public void drawPoint(Point point, Color color) {
        //System.out.println(point.toString());
        Point newPoint = normalizePoint(point);
        int x, y;
        if (newPoint.vectors != null) {
            x = (int) newPoint.vectors[0];
            y = (int) newPoint.vectors[1];
            gs.setColor(color);
            gs.drawOval(x - 5, y - 5, 10, 10);
            gs.fillOval(x - 5, y - 5, 10, 10);
        }
    }

    public void drawPoints(ArrayList<Point> points, String str, Color color) {
        Point[] newPoints = normalizePoints(points);
        Point[] hullPoints; // convex hull points
        int i;
        int len = newPoints.length;
        int x, y;
        gs.setFont(new Font("Arial", Font.BOLD, 12));
        gs.setColor(color);

        for (i = 0; i < len; i++) {
            x = (int) newPoints[i].vectors[0];
            y = (int) newPoints[i].vectors[1];
            // set x, y axis to x-3, y+5 for matching convex hull
            gs.drawString(str, x - 3, y + 5);
        }
        // draw Convex hull for the points containing more than two points
        if (len > 2) {
            ConvexHull convexhull = new ConvexHull(newPoints);
            hullPoints = convexhull.getHullPoints();
            drawConvexHull(hullPoints);
        }
    }
    
    public void drawConvexHull(Point[] points) {
        Color color = new Color(110, 110, 110);
        int i;
        int len;
        Point start, end;
        len = points.length;
        if (len < 2) {
            return;
        }

        for (i = 0; i < len - 1; i++) {
            start = points[i].copyPoint();
            end = points[i + 1].copyPoint();
            drawPolyLine(start, end, color);
        }
        start = points[len - 1];
        end = points[0];
        drawPolyLine(start, end, color);

    }
    
    public void drawPolyLine(Point p1, Point p2, Color color) {
        //gs.setColor(color);
        //gs.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);

        BasicStroke stroke = new BasicStroke(2.0f);
        Line2D line = new Line2D.Double(p1.vectors[0], p1.vectors[1],
                p2.vectors[0], p2.vectors[1]);
        Graphics2D g2d = (Graphics2D)gs;
        g2d.setColor(color);
        g2d.setStroke(stroke);
        g2d.draw(line);

    }
    
        // convert the coordinates of ONE point to the screen canvas system
    private Point normalizePoint(Point point) {
        int width = this.getWidth() - 20;   // leave some margin  ******************change
        int height = this.getHeight() - 20; // leave some margin
        double newX, newY;                     // coordinates in the screen
        double x, y;
        double minX = rd.getMinX();
        double minY = rd.getMinY();
        double maxX = rd.getMaxX();
        double maxY = rd.getMaxY();
        System.out.println("the minX is: " + minX);
        System.out.println("the minY is: " + minY);
        System.out.println("the maxX is: " + maxX);
        System.out.println("the maxY is: " + maxY);
        
        if (point.vectors != null) {
            double[] vectors;
            
            x = point.vectors[0];
            y = point.vectors[1];
            newX = (x - minX) / (maxX - minX) * width + 10;// leave some margin 10 pixle
            newY = (maxY - y) / (maxY - minY) * height + 10;
            vectors = new double[2];
            vectors[0] = newX;
            vectors[1] = newY;
            Point newPoint = new Point(vectors, false);

            return newPoint;
        } else {
            return point;
        }
    }

    // convert the coordinates of points to the screen canvas system
    private Point[] normalizePoints(ArrayList<Point> points) {
        int len = points.size();
        int i;
        int width = this.getWidth() - 20;   // leave some margin
        int height = this.getHeight() - 20; // leave some margin
        double newX, newY;  // coordinates in the screen
        double x, y;
        double minX = rd.getMinX();
        double minY = rd.getMinY();
        double maxX = rd.getMaxX();
        double maxY = rd.getMaxY();
        double[] vectors;
        Point newPoint;
        Point[] newPoints = new Point[len];

        for (i = 0; i < len; i++) {
            x = points.get(i).vectors[0];
            y = points.get(i).vectors[1];
            newX = (x - minX) / (maxX - minX) * width + 10;    // leave some margin 10 pixel
            newY = (maxY - y) / (maxY - minY) * height + 10;
            vectors = new double[2];
            vectors[0] = newX;
            vectors[1] = newY;
            newPoint = new Point(vectors, false);
            newPoints[i] = newPoint;
        }
        return newPoints;
    }
    
    // select the symbol for drawing different clusters with different colors
    private String selectSymbol(int k) {
        String str = "*+#123456789abcdefghijklmnopqrstuvwxyz"; // candidate strings
        String s = "";
        int i = k % 38;
        s = str.substring(i, i + 1);

        return s;
    }

    // select color for drawing each cluster
    private Color selectColor(int i) {
        //Random random = new Random();
        Color color;
        int r, g, b;
        r = i * 50 % 255;
        g = i * 40 % 255;
        b = i * 30 % 255;
        color = new Color(r, g, b);
        return color;
    }
}
