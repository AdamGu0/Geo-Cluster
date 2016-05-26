package hierarchicalclustering;

import java.util.ArrayList;

import main.Point;

public class HCluster {
    private boolean isGPS;
    public ArrayList<Integer> pointsIndexList; // points list containing all the index of points

    public double[] centroid;

    public HCluster(int index, double[] center, boolean isGPS) {
        this.isGPS = isGPS;
        pointsIndexList = new ArrayList<Integer>();
        pointsIndexList.add(index);

        int dimension = center.length;
        centroid = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            centroid[i] = center[i];
        }
    }

    /**
     * calculate the MSE of cluster 计算簇中的每个点到该簇的中心点的距离之和
     *
     * @return MSE of one cluster
     */
    public double calcMSE(Point[] data) {
        double ssd = 0;
        double mse = 0.0;

        for (Integer i : pointsIndexList) {
            ssd += HClusterData.distanceBetween(this.centroid, data[i].vectors, isGPS);
        }

        mse = ssd / this.pointsIndexList.size();

        /*
    	System.out.println("calcMSE");
    	System.out.println(pointsIndexList.size());
    	System.out.println(this.mNumPointsInClusters);
    	System.out.println("mse " + mse);
         */
        return mse;
    }
}
