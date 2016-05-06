/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import java.util.ArrayList;

/**
 *     R R R R           A           MMM         MMM    B B B B        O O O O      TTTTTTTTTTTTTTTT  
 *     R     R          A A          M  M       M  M    B      B      O       O            TT
 *     R     R         A   A         M   M     M   M    B      B     O         O           TT 
 *     R R  R         A     A        M    M   M    M    B B B B      O         O           TT
 *     R    R        A A A A A       M     MMM     M    B      B     O         O           TT
 *     R     R      A         A      M             M    B      B      O       O            TT
 *     R      R    A           A     M             M    B B B B        O O O O             TT  
 */

public class ConvexHull {
  private Point[] points;              // given points
  private Point[] hullPoints;          // convex hull points
  private Point maxDistancePoint;      // the point with furthest distance from line
  private ArrayList<Point> hullList;   // containing all the hull points

  // construct a new ConvexHull object
  public ConvexHull(Point[] _points)
  {
    this.points = _points;
    this.hullPoints = null;
    this.hullList = new ArrayList();
  }

  /**
   * Description: return the hull points in clockwise order
   */
  public Point[] getHullPoints()
  {
    Point maxXPoint, minXPoint;
    // Initial start with max and min X value points. 
    // These points are guaranteed to be points of the convex hull
    // Initially the points on both sides of the line are processed.
    Point[] mmPoints = getMinMaxXPoint();
    minXPoint = mmPoints[0];
    maxXPoint = mmPoints[1];
    quickHull(points, minXPoint, maxXPoint);
    quickHull(points, maxXPoint, minXPoint);

    hullPoints = (Point[]) hullList.toArray(new Point[hullList.size()]);

    return hullPoints;
  }
  
  /**
   * Description: calculate the distance betwee the point and the line
   * Here we are using Area value to presents the distance as the height
   * the area could be calculated by cross product of the line vector 
   * (startPoint to endPoint) and the startPoint to point vector.
   * ((y2*x1) - (x2*y1))  http://en.wikipedia.org/wiki/Determinant
   * The Area formula can be written as follows:
   * A = |startPoint->endPoint| * h
   * Since our purpose is to find the maximal distance same with 'h', so it is
   * equivalent to find the maximum Area value, on the other hand, it can 
   * improve the performance without devision
   * The return result could be positive and negative which indicates the point
   * is on the left or right of the line
   * @param startPoint
   * @param endPoint
   * @param point
   * @return
   */
  private double calculateDistance(Point startPoint, Point endPoint, 
                                     Point point)
  {
    double x, y, x1, x2, y1, y2;
    double xVector1, yVector1, xVector2, yVector2;
    double area; // area of the parallelogram
    x = point.vectors[0];
    y = point.vectors[1];
    x1 = endPoint.vectors[0];
    x2 = startPoint.vectors[0];
    y1 = endPoint.vectors[1];
    y2 = startPoint.vectors[1];
    xVector1 = x1 - x2;
    yVector1 = y1 - y2;
    xVector2 = x - x2;
    yVector2 = y - y2;
    
    area = yVector2 * xVector1 - xVector2 * yVector1;
    
    return area;
  }
  /**
   * Description: get the points on the left side of the line
   * @param nPoints
   * @param startPoint
   * @param endPoint
   * @return
   */
  private Point[] getLeftPoints(Point[] nPoints, Point startPoint,
                                  Point endPoint)
  {
    int i;
    int len;
    int maxIndex = 0;
    double distance;
    double maxDistance = 0.0;
    Point point;
    Point[] leftPoints;
    len = nPoints.length;
    ArrayList<Point> pointsList = new ArrayList();

    for(i=0; i < len; i++)
    {
      point = nPoints[i].copyPoint();
      distance = calculateDistance(startPoint, endPoint, point);
      if(distance > 0)
      {
        pointsList.add(nPoints[i]);
        if(distance > maxDistance)
        {
          maxDistance = distance;
          maxIndex = i;
        }
      }
    }
    // no left points found
    if(pointsList.size() == 0)
    {
      maxDistancePoint = null;
      return null;
    }

    maxDistancePoint = nPoints[maxIndex].copyPoint(); // set the maxPoint
    leftPoints = (Point[]) pointsList.toArray(new Point[pointsList.size()]);

    return leftPoints;
  }

  /**
   * Description: execute Quick Hull algorithm on the given data points using
   * start-end line as delimiter and only left side of the line will be analyzed
   * @param nPoints
   * @param startPoint
   * @param endPoint
   * @return
   */
  private void quickHull(Point[] nPoints, Point startPoint, Point endPoint)
  {
    Point[] leftPoints; // points on the left line
    Point maxPoint; //

    leftPoints = getLeftPoints(nPoints, startPoint, endPoint);    

    // no points on the left line, add the end point to the hullList
    if(leftPoints == null)
    {
      hullList.add(endPoint);
      return;
    }

    // The new maximal point creates a triangle together with startPoint and
    // endPoint, Everything inside this trianlge can be ignored. Everything
    // else needs to handled recursively. Because the quickHull invocation
    // only handles points left of the line we can simply call it for the
    // different line segements to process the right kind of points.
    maxPoint = maxDistancePoint.copyPoint();
    quickHull(leftPoints, startPoint, maxPoint);
    quickHull(leftPoints, maxPoint, endPoint);

  }

  /**
   * Description: find and return the point with minimal and maximal X value
   */
  private Point[] getMinMaxXPoint()
  {
    int i;
    int len;
    int minIndex, maxIndex;
    double x, minX, maxX;
    Point[] mmPoints = new Point[2]; // containing minXPoint and maxXPoint

    minIndex = maxIndex = 0;
    len = points.length;
    minX = maxX = points[0].vectors[0];

    for(i=1; i < len; i++)
    {
      x = points[i].vectors[0];
      if(x > maxX)
      {
        maxX = x;
        maxIndex = i;
      }else if(x < minX)
      {
        minX = x;
        minIndex = i;
      }
    }

    mmPoints[0] = points[minIndex].copyPoint(); // minXPoint
    mmPoints[1] = points[maxIndex].copyPoint(); // maxXPoint

    return mmPoints;
  }


}
