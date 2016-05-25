package hierarchicalclustering;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import main.Cluster;
import main.Point;

public class HClusterData {
    private boolean isGPS;
    private int K;

    private Point[] data;
    // the number of data 点的个数
    private int data_number;

    // similarityMatrix: contains distances between each pair of clusters. 
    private static ArrayList<ArrayList<Double>> similarityMatrix;

    // private HCluster[] hcluster;
    private static ArrayList<HCluster> hclusters;

    // 保存每个类别的中心，  contains centroids of clusters
    // private static ArrayList<double[]> centroids;
    // mNumPointsInClusters: contains the number of points in each clusters.
    //private static ArrayList<Integer> mNumPointsInClusters;
    // mMinDist: contains "index" of the minimum-distanced point for each points. Requires O(n) space.
    private static ArrayList<Integer> mMinIndex;

    public HClusterData(Point[] data, int data_number, int k, boolean isGPS) {

        System.out.println("HClusterData initialize");
        this.isGPS = isGPS;
        this.data = data;
        this.data_number = data_number;
        this.K = k;

        mMinIndex = new ArrayList<Integer>();

        similarityMatrix = new ArrayList<ArrayList<Double>>();
        calculateSimilarityMatrix();

        hclusters = new ArrayList<HCluster>();
        for (int i = 0; i < data_number; i++) {
            hclusters.add(new HCluster(i, data[i].vectors, isGPS));
        }

    }

    /**
     * Calculates the Similarity matrix
     *
     * @return
     */
    private void calculateSimilarityMatrix() {
        System.out.println("Computing Similarity matrix start...");

        for (int row = 0; row < data_number; ++row) {
            similarityMatrix.add(new ArrayList<Double>());
            Point p1 = data[row];
            for (int col = 0; col < data_number; ++col) {
                Point p2 = data[col];
                double distance = p1.getDistance(p2);  // Euclidean distance.
                similarityMatrix.get(row).add(distance); // 计算每一行的Euclidean distance.	
            }

            int newMinDistIndex = getMinDistIndex(similarityMatrix.get(row), row);
            if (newMinDistIndex < 0) {
                System.out.println("ERROR: no newMinDistIndex");
            }
            mMinIndex.add(newMinDistIndex);
        }

        System.out.println("Computing Similarity matrix end...");
    }

    /**
     * Returns the index that contains minimum value in the list, except the
     * case that index == indexNotInterested.
     *
     * @param list
     * @param indexNotInterested
     * @return index of minimum
     */
    private static int getMinDistIndex(ArrayList<Double> list, int indexNotInterested) {
        double newDistMin = Double.MAX_VALUE;
        int newMinDistIndex = -1;

        for (int i = 0; i < list.size(); i++) {
            if (i != indexNotInterested && newDistMin > list.get(i)) {
                newDistMin = list.get(i);
                newMinDistIndex = i;
            }
        }

        return newMinDistIndex;
    }

    /**
     * Returns arithmetic mean of two points. For arbitrary-dimensional data.
     *
     * @param from
     * @param to
     * @return average between two points
     */
    private static double[] averageBetween(double[] from, double[] to, int wFrom, int wTo) {
        if (from.length != to.length) {
            return null;
        }

        // Average = arithmetic mean
        double[] average = new double[from.length];
        for (int i = 0; i < from.length; i++) {
            average[i] = (from[i] * wFrom + to[i] * wTo) / (wFrom + wTo);
        }
        return average;
    }

    /**
     * Returns Euclidean distance between two points. For arbitrary-dimensional
     * data.
     *
     * @param from
     * @param to
     * @return distance between two points
     */
    public static double distanceBetween(double[] from, double[] to, boolean isGPS) {
        if (from.length != to.length) {
            return (double) -1;
        }
        
        if (isGPS) {
            double R = 6371; // Radius of the Earth in km
            double dLat = (from[1] - to[1]) * Math.PI / 180;
            double dLon = (from[0] - to[0]) * Math.PI / 180;
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(from[1] * Math.PI / 180) * Math.cos(to[1] * Math.PI / 180)
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        }

        // Euclidean distance.
        double dist = 0;
        for (int i = 0; i < from.length; i++) {
            dist += (from[i] - to[i]) * (from[i] - to[i]);
        }
        return Math.sqrt(dist);
    }

    public void doCluster() {

        while (hclusters.size() != K) {

            System.out.println(hclusters.size());

            // Get the pair
            int ith = -1;
            int jth = -1;
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < mMinIndex.size(); i++) {
                int minForIthPoint = mMinIndex.get(i);
                double distIth = similarityMatrix.get(i).get(minForIthPoint);
                if (minDist > distIth) {
                    ith = i;
                    jth = minForIthPoint;
                    minDist = distIth;
                }
            }

            // Swap if necessary. Keep jth > ith to avoid index update problems.
            if (ith > jth) {
                int temp = ith;
                ith = jth;
                jth = temp;
            }

            // Update mClusters.
            double[] average = averageBetween(hclusters.get(ith).centroid, hclusters.get(jth).centroid,
                    hclusters.get(ith).mNumPointsInClusters, hclusters.get(jth).mNumPointsInClusters);

            for (int i = 0; i < 2; i++) {
                hclusters.get(ith).centroid[i] = average[i];
            }
            int count = hclusters.get(ith).mNumPointsInClusters + hclusters.get(jth).mNumPointsInClusters;
            hclusters.get(ith).mNumPointsInClusters = count;

            // 将第j类的元素全部加入到i类中
            for (int i = 0; i < hclusters.get(jth).pointsIndexList.size(); i++) {
                hclusters.get(ith).pointsIndexList.add(hclusters.get(jth).pointsIndexList.get(i));
            }
            hclusters.get(jth).pointsIndexList.clear();
            hclusters.remove(jth);

            // Update mMatrix first, and then mMinIndex.
            similarityMatrix.remove(jth);
            mMinIndex.remove(jth);

            for (int ii = 0; ii < similarityMatrix.size(); ii++) {
                // mMatrix update
                similarityMatrix.get(ii).remove(jth);
                double distNow = distanceBetween(hclusters.get(ii).centroid, hclusters.get(ith).centroid, isGPS);
                similarityMatrix.get(ii).set(ith, distNow);

                // mMinIndex update. worst case is when mMinIndex[ii] == ith or jth, and distNow > distIth or distJth
                int newMinDistIndex = getMinDistIndex(similarityMatrix.get(ii), ii);
                mMinIndex.set(ii, newMinDistIndex);
            }

        } // while	

        // Print output
/*        for(int i = 0; i < hclusters.size(); i++) {
    		System.out.print(i + 1);
    		for(int j = 0; j < 2; j++)
    			System.out.print(" " + hclusters.get(i).centroid[j]);
    		System.out.print("\n");
    		for (int j = 0; j < hclusters.get(i).pointsIndexList.size(); j++) {
    			System.out.print(hclusters.get(i).pointsIndexList.get(j) + " ");
    		}
    		System.out.print("\n\n");
        }*/
    }

    /**
     * Returns the result
     *
     * @return 聚类结果
     */
    public ArrayList<HCluster> getHclusters() {
        return hclusters;
    }
}
