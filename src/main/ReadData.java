package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Description: operate file class
 *
 * @author
 */
public class ReadData {
    // bounding box of the data set including minimum and maximum x and y value

    private double minX, minY, maxX, maxY;
    private Point[] points;
    private int vectorSize;
    public boolean isGPSData;

    public ReadData() {
        this.points = null;
        this.vectorSize = 0;
    }
    
    /**
     * read file and extract data point return false if data format
     * is not valid
     */
    public boolean readData(File filedata, boolean isGPS) {
        ArrayList pointList = new ArrayList();
        Point point;

        int vectorIndex = 0;
        int lineNo = 0; // line number
        double[] vectors;
        String seperator = " ";

        try {
            FileReader fr = new FileReader(filedata);
            BufferedReader br = new BufferedReader(fr);
            String line;
            boolean firstFlag = true; // used for initilizing the bounding box value

            readline:
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.length() < 3) {
                    continue;
                }
                if (firstFlag) {
                    seperator = checkSeperator(line);
                    vectorSize = checkVectorSize(line, seperator);
                }


                String[] row = line.split(seperator);
                if (row.length != vectorSize) {
                    continue;
                }
                vectors = new double[vectorSize];
                vectorIndex = 0;
                for (String row1 : row) {
                    if (!checkData(row1)) {
                        System.out.println("Data formate error at line number: " + lineNo);
                        continue readline;
                    }
                    vectors[vectorIndex] = Double.parseDouble(row1);
                    vectorIndex++;
                }

                point = new Point(vectors, isGPS);
                pointList.add(point);

                if (firstFlag) {
                    firstFlag = false;
                    // initilize the min and max x,y value with the first data
                    //if (vectorSize == 2) // only the vectorSize is 2
                    //{
                    minX = maxX = vectors[0];
                    minY = maxY = vectors[1];
                    continue;
                    //}
                }
                //if (vectorSize == 2) // only the vectorSize is 2
                //{
                // update the min and max x,y value
                if (vectors[0] > maxX) {
                    maxX = vectors[0];
                } else if (vectors[0] < minX) {
                    minX = vectors[0];
                }

                if (vectors[1] > maxY) {
                    maxY = vectors[1];
                } else if (vectors[1] < minY) {
                    minY = vectors[1];
                }
                //}

            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

        if (pointList.isEmpty()) {
            return false;
        }
        points = convertPoints(pointList);
        isGPSData = isGPS;
        return true;
    }

    /**
     * find the seperator string from the file
     *
     * @param line
     * @return
     */
    private String checkSeperator(String line) { 
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < line.length(); i++) {
            String toBeChecked = line.substring(i, i + 1);
            if (toBeChecked.equals(" ") || toBeChecked.equals(",")) {
                if (startIndex == -1) {
                    startIndex = i;
                    endIndex = i;
                } else if (i == (startIndex + 1)) {
                    endIndex = i;
                } else {
                    break;
                }
            }
        }
        return line.substring(startIndex, endIndex + 1);
    }

    /**
     *
     * @param line
     * @param seperator
     * @return
     */
    private int checkVectorSize(String line, String seperator) {
        int size = 0;
        String[] row = line.split(seperator);
        size = row.length;

        return size;

    }
    
    // check the validity of data
    private boolean checkData(String s) {
        try {
            double d = Double.parseDouble(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    // convert arrayList to points array
    private Point[] convertPoints(ArrayList arrayList) {
        int i;
        int len = arrayList.size();
        Point[] tempPoints = new Point[len];

        for (i = 0; i < len; i++) {
            tempPoints[i] = (Point) arrayList.get(i);
        }

        return tempPoints;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public Point[] getPoints() {
        return points;
    }

    public int getVectorSize() {
        return vectorSize;
    }

}
