package main;

import java.util.ArrayList;
import java.util.Random;

/**
 * Description: implement clustering methods
 *
 * @author Rocky Chen
 * @date 01.02.2010
 */
public class ClusterData {

    public Cluster[] clusters; // result of clustering data set
    final private Point[] data;  // data sets
    private int k; // number of clusters
    private int[] GLAIndex; // centroid index for each point
    private double[] NearestArray;
    public String strOut;
    private boolean isGPS;
    /*
   * add new Cluster
     */
    public Cluster[] newCluster;

    public ClusterData(Point[] _data, boolean isGPS) {
        data = _data;
        this.isGPS = isGPS;
        int len = data.length;
        GLAIndex = new int[len];
        NearestArray = new double[len];
    }

    public static double calcSSB(Cluster[] clusters, boolean isGPS) {
        double SSB = 0.0;
        Point allCentroid;
        int vectorSize = clusters[0].getVectorSize();
        double sumValue = 0.0;
        double[] sumVectors = new double[vectorSize];
        for (int i = 0; i < vectorSize; i++) {
            sumValue = 0;
            for (int j = 0; j < clusters.length; j++) {
                if (clusters[j].centroid == null) {
                    System.err.println("no centroid");
                }
                if (clusters[j].centroid.vectors != null) {
                    sumValue += clusters[j].centroid.vectors[i];
                }
            }

            sumVectors[i] = sumValue / clusters.length;
        }
        allCentroid = new Point(sumVectors, isGPS);

        for (int i = 0; i < clusters.length; i++) {
            int clusterSize = clusters[i].pointsList.size();
            double distance = clusters[i].centroid.getDistance(allCentroid);
            SSB += clusterSize * Math.pow(distance, 2);
        }

        return SSB;
    }

    public Cluster[] kmeans(int _k, int iterationTimes) {
        k = _k;
        initClusters();
        lloyd(iterationTimes);
        return clusters;

    }

    /**
     * Function: smartSwap Description: implement the swap clustering method by
     * swapping the farthest point in a cluster and one of the two nearest
     * clusters centroids Main steps: 1. k initial "means" are randomly selected
     * from the data set 2. k clusters are created by associating every point
     * with the nearest mean 3. Update the centroids with new means of the k
     * clusters. 4. find the nearest two clusters and randomly select a point
     * from data set if this data set does not belong these two nearest
     * clusters, then swap them, 5. Repeat Steps 2, 3 and 4 for k times
     *
     * @author Rocky
     */
    public Cluster[] smartSwap(int _k) {
        k = _k;
        initClusters();
        assignDataSwap();
        updateCentroidsSwap();
        swapIterations();
        return clusters;
    }

    /**
     * get the index of point which is the largest distance
     *
     * @return
     */
    private void getLargestDistortion(int _k) {
        int index = 0;
        int K = _k;
        int[] indexs = new int[K];
        int i, j;
        double[] newVectors = new double[2];
        double tempDist = 0.0, innerDist = 0.0, bestDist = 0.0;
        bestDist = clusters[0].centroid.getDistance(clusters[0].getPoint(0).copyPoint());
        newVectors = clusters[0].getPoint(0).copyPoint().vectors;

        for (i = 0; i < K; i++) {
            for (j = 0; j < clusters[i].pointsList.size(); j++) {
                tempDist = clusters[i].centroid.getDistance(clusters[i].getPoint(j).copyPoint());
                if (tempDist > bestDist) {
                    double temps = bestDist;
                    bestDist = tempDist;
                    tempDist = temps;
                    newVectors = clusters[i].getPoint(j).copyPoint().vectors;
                }
            }
        }
        Cluster newclusters[] = new Cluster[K + 1];
        for (int ii = 0; ii < K; ii++) {
            newclusters[ii].centroid = clusters[ii].centroid;
        }
        newclusters[K].centroid = new Point(newVectors, isGPS);
    }

    // initilize the clusters
    private Point[] initClusters() {
        int i;
        int len = data.length;
        int r; // random value
        Random random = new Random();
        Point[] initC = new Point[k];
        clusters = new Cluster[k];
        System.out.println("k: " + k);

        // initilize the centroid for each cluster
        for (i = 0; i < k; i++) {
            clusters[i] = new Cluster(isGPS, data[0].vectors.length);
            r = random.nextInt(len);
            clusters[i].centroid = data[r];
            initC[i] = clusters[i].centroid.copyPoint();
        }

        return initC;
    }

    // assign data by using activity classification in Lloyd iteration
    private void fastIteration(Point[] previousC, Point[] currentC) {
        int i;
        int index;
        int[] activity;
        int len = data.length;
        double dist;
        double preDist;
        activity = checkDiff(previousC, currentC);

        for (i = 0; i < len; i++) {
            index = GLAIndex[i];
            if (checkExist(index, activity)) // in activity clusters
            {
                dist = data[i].getDistance(currentC[index]);
                preDist = data[i].getDistance(previousC[index]);

                if (dist > preDist) {
                    fullAssign(i, currentC);
                } else {
                    activityAssgin(i, currentC, activity);
                }
            } else // in static clusters
            {
                activityAssgin(i, currentC, activity);
            }
        }

        // update the clusters
        assignClusters();

    }

    /**
     * Description: assign the data with cluster indicate index to clusters
     */
    private void assignClusters() {
        int i;
        int index;
        int len = data.length;
        // initilize the points in clusters before reaasign data points
        for (i = 0; i < k; i++) {
            clusters[i].pointsList.clear();
        }

        for (i = 0; i < len; i++) {
            index = GLAIndex[i];
            clusters[index].pointsList.add(data[i]);
        }
    }

    /**
     * Description: assign the point to the best cluster between current cluster
     * and activity clusters
     *
     * @param index
     * @param currentC
     * @param activity
     */
    private void activityAssgin(int index, Point[] currentC, int[] activity) {
        int i, temp;
        int bestId; // best cluster for a point
        int len = activity.length;
        double dist, bestDist;
        Point point = data[index];
        temp = GLAIndex[index];
        bestDist = point.getDistance(currentC[temp]);
        bestId = temp;

        for (i = 0; i < len; i++) {
            temp = activity[i];
            dist = point.getDistance(currentC[temp]);

            if (dist < bestDist) {
                bestDist = dist;
                bestId = temp;
            }
        }

        GLAIndex[index] = bestId;

    }

    /**
     * Description: full search for finding the best cluster for the point
     *
     * @param index
     * @param currentC
     */
    private void fullAssign(int index, Point[] currentC) {
        int i;
        int bestId; // best cluster for a point
        double dist, bestDist;
        Point point = data[index];

        // initilize the point to the first cluster
        bestDist = point.getDistance(currentC[0]);


        bestId = 0;

        for (i = 1; i < k; i++) {
            dist = point.getDistance(currentC[i]);

            if (dist < bestDist) {
                bestDist = dist;
                bestId = i;
            }
        }

        GLAIndex[index] = bestId;

    }

    /**
     * Description: check the value exists in a array or not
     *
     * @param value
     * @param valueArray
     * @return
     */
    private boolean checkExist(int value, int[] valueArray) {
        int len = valueArray.length;

        for (int i = 0; i < len; i++) {
            if (value == valueArray[i]) {
                return true;
            }
        }

        return false;
    }

    /**
     * Description: check the different value between two array
     *
     * @param previousC
     * @param currentC
     * @return the different indices
     */
    private int[] checkDiff(Point[] previousC, Point[] currentC) {
        int i, len;
        int[] diff;

        ArrayList<Integer> diffList = new ArrayList();

        for (i = 0; i < k; i++) {
            if (previousC[i].getDistance(currentC[i]) != 0) {
                diffList.add(i);
            }
        }

        len = diffList.size();
        diff = new int[len];

        for (i = 0; i < len; i++) {
            diff[i] = Integer.parseInt(diffList.get(i).toString());
        }

        return diff;
    }
    // associate the data point to the nearest centroid from k clusters

    private void assignData() {
        int i, j;
        int len = data.length;
        double dist, bestDist;
        int bestId; // best cluster for a point
        // initilize the points in clusters before reaasign data points
        for (i = 0; i < k; i++) {
            clusters[i].pointsList.clear();
        }

        // assign point to the nearest centroid
        for (i = 0; i < len; i++) {
            // initilize the point to the first cluster
            bestDist = data[i].getDistance(clusters[0].centroid);
            bestId = 0;

            for (j = 1; j < k; j++) {
                dist = data[i].getDistance(clusters[j].centroid);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestId = j;
                }
            }
            // associate the data point to the nearest centroid
            clusters[bestId].pointsList.add(data[i]);
            GLAIndex[i] = bestId;
        }
    }
    // FOR SWAP METHOD

    private void assignDataSwap() {
        int i, j;
        int len = data.length;
        double dist, bestDist;
        int bestId; // best cluster for a point
        // initilize the points in clusters before reaasign data points
        for (i = 0; i < k; i++) {
            clusters[i].pointsList.clear();
        }

        // assign point to the nearest centroid
        for (i = 0; i < len; i++) {
            // initilize the point to the first cluster
            bestDist = data[i].getDistance(clusters[0].centroid);

            bestId = 0;

            for (j = 1; j < k; j++) {
                dist = data[i].getDistance(clusters[j].centroid);

                if (dist < bestDist) {
                    bestDist = dist;
                    bestId = j;
                }
            }
            // associate the data point to the nearest centroid
            clusters[bestId].pointsList.add(data[i]);
            GLAIndex[i] = bestId;
        }
    }

    // update centroid after each association iteration and get new updated centroids
    private Point[] updateCentroids() {
        int i;
        Point[] currentC = new Point[k];  // array containing all the centroids

        for (i = 0; i < k; i++) {
            clusters[i].calcCentroid(clusters[i].centroid);
            currentC[i] = clusters[i].centroid.copyPoint();
        }

        return currentC;
    }

    // for swap
    private Point[] updateCentroidsSwap() {
        int i;
        Point[] currentC = new Point[k];  // array containing all the centroids

        for (i = 0; i < k; i++) {
            clusters[i].calcCentroid(clusters[i].centroid);
            currentC[i] = clusters[i].centroid.copyPoint();
        }

        return currentC;
    }

    // get current centroids of all clusters
    public Point[] getCentroids() {
        int i;
        Point[] currentC = new Point[k];  // array containing all the centroids

        for (i = 0; i < k; i++) {
            currentC[i] = clusters[i].centroid.copyPoint();
        }

        return currentC;
    }

    // assign new centroids to the clusters
    private void assignCentroids(Point[] points) {
        int i;
        for (i = 0; i < k; i++) {
            clusters[i].centroid = points[i].copyPoint();
        }

    }

    // check the current centroids and previous centroids are same or not
    private boolean checkCentroids(Point[] currentC, Point[] previousC) {
        boolean flag = true;
        double dist;  // distance between two centroids
        int i;

        for (i = 0; i < k; i++) {
            dist = currentC[i].getDistance(previousC[i]);

            if (dist != 0) {
                flag = false;
                break;
            }
        }

        return flag;
    }

    /**
     * Description: Perform swap between the cluster (nearest pair) and a point
     * in the candidate cluster (largest distortion)
     *
     * @param nearIndex
     * @param point
     * @return true if it's a good swap
     */
    private boolean doSwapping(int nearIndex, Point point) {
        boolean flag = true; // swap success flag

        double oldMSE = 0, newMSE;
        int len = GLAIndex.length;
        int[] GLAIndexBackup = new int[len];
        System.arraycopy(GLAIndex, 0, GLAIndexBackup, 0, len);
        oldMSE = getMSE(clusters);

        Point[] previousC = getCentroids();
        Point[] originalC = getCentroids();
        clusters[nearIndex].centroid = point.copyPoint();

        Point[] currentC;
        currentC = getCentroids();
        fastIteration(previousC, currentC);
        previousC = currentC;

        // 2 steps fine-tuning kmeans for evaluate new centroids
        for (int j = 0; j < 2; j++) {
            currentC = updateCentroidsSwap();
            fastIteration(previousC, currentC);
            previousC = currentC;
        }
        
        newMSE = getMSE(clusters);
        if (newMSE >= oldMSE) // results not improved, resume the previous centroids
        {
            GLAIndex = GLAIndexBackup;
            assignClusters();
            assignCentroids(originalC);
            currentC = updateCentroidsSwap();
            fastIteration(originalC, currentC);
            flag = false;

        }

        return flag;

    }

    // get the mse order for all clusters
    private int[] getMseIndices() {
        int i, j;
        int tempIndex;
        int[] mseIndices = new int[k];
        double tempmse;
        double[] mseValue = new double[k];

        for (i = 0; i < k; i++) {
            mseValue[i] = clusters[i].calcSsd();
            mseIndices[i] = i;
        }

        // sort the mse value in descending order
        for (i = 0; i < k; i++) {
            for (j = i + 1; j < k; j++) {
                if (mseValue[i] < mseValue[j]) {
                    tempmse = mseValue[i];
                    mseValue[i] = mseValue[j];
                    mseValue[j] = tempmse;

                    tempIndex = mseIndices[i];
                    mseIndices[i] = mseIndices[j];
                    mseIndices[j] = tempIndex;
                }
            }
        }

        return mseIndices;
    }

    private double log2(double value) {
        return Math.log(value) / Math.log(2.0);
    }

    // select a nearest centroid and a point in a cluster with biggest MSE
    private void swapIterations() {
        int nearIndex;
        int centroidIndex = 0;
        int mseIndex = 0;
        int mseOrder = 0;
        int validStep = 0;

        int maxStep = (int) log2(k); // stop condition
        //int maxStep = 1; // stop condition, for fast swap
        int[] nearestPair = {0, 0}; // nearest centroid pair
        int[] mseIndices;  // indices of MSE of clusters in descending order
        Point swapPoint;  // swapping point

        while (mseOrder < maxStep) {
            nearestPair = getNearestClusters();
            nearIndex = (k / 2 == 0) ? 0 : 1;
            centroidIndex = nearestPair[nearIndex];
            mseIndices = getMseIndices();
            mseIndex = mseIndices[mseOrder];
            // select the first point in the pointsList, any point is OK
            swapPoint = (Point) clusters[mseIndex].pointsList.get(0);

            if (doSwapping(centroidIndex, swapPoint)) {
                mseOrder = 0; // GOOD swap, select the biggest mse
                validStep++;
            } else {
                // BAD swap, select the next bigger mse in descending order
                // in order to avoid getting stuck
                mseOrder++;
            }

        }
        double xOut = clusters[nearestPair[0]].centroid.getDistance(clusters[nearestPair[1]].centroid);
        strOut = "k: " + k + ", nearestPair distance: " + xOut;
        System.out.println("k: " + k + ", nearestPair distance: " + xOut);
    }

    /**
     * Description: Lloyd's algorithm for grouping data points into a given
     * number of categories
     *
     * @param iterationTimes
     */
    private void lloyd(int iterationTimes) {
        Point[] currentC, previousC;
        boolean kStopFlag = false; // kmeans stop flag
        previousC = getCentroids();
        int iteration = 0;

        while (!kStopFlag && iteration < iterationTimes) //while (iteration < iterationTimes)
        {
            assignData();
            currentC = updateCentroids();
            kStopFlag = checkCentroids(currentC, previousC);

            //centroidsDist(currentC, previousC);
            previousC = currentC; // set to previous centroids array
            iteration++;
        }

    }

    /**
     * Descirption: find the two nearest clusters Input: Cluster[] Output: Int[]
     * containing two nearest indices
     */
    private int[] getNearestClusters() {
        int i, j;
        int nearIndex1 = 0; // one of the two nearest clusters index
        int nearIndex2 = 1; // another index of two nearest clusters
        double tempDist = 0.0;
        double nearDist = 0.0; // distance between two nearest clusters
        int[] indices = new int[2];
        // find the nearest two clusters, initilazation with first two clusters
        nearDist = clusters[0].centroid.getDistance(clusters[1].centroid);

        for (i = 0; i < k; i++) {
            for (j = i + 1; j < k; j++) {
                tempDist = clusters[i].centroid.getDistance(clusters[j].centroid);

                if (tempDist < nearDist) {
                    nearDist = tempDist;
                    nearIndex1 = i;
                    nearIndex2 = j;
                }
            }
        }
        indices[0] = nearIndex1;
        indices[1] = nearIndex2;
        return indices;

    }
    // calculate the MSE (mean square error)

    public double getMSE(Cluster[] tempClusters) {
        int i;
        int n = data.length;
        int len = tempClusters.length; // number of clusters
        double sum = 0.0; // sum of square distance for all data set
        double mse = 0.0;

        for (i = 0; i < len; i++) {
            sum += tempClusters[i].calcSsd();
        }
        mse = sum / n;

        return mse;

    }

    // get the MSE for current clusters
    public double getCurrentMSE() {
        double mse = getMSE(clusters);

        return mse;
    }

}
