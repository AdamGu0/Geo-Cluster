package hierarchicalclustering;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;

import main.Cluster;
//import main.OutputData;
import main.Point;

public class HierarchicalClustering {

    boolean isGPS;
    private int K;  // K个类

    private int data_number;  // the number of data 点的个数

    private Point[] data;

    // 保存每个类别的中心，  contains centroids of clusters
    private static ArrayList<double[]> centroids;

    // 保存没个类别中点的个数
    private static ArrayList<Integer> mNumPointsInClusters;

    private static ArrayList<Integer> mMinIndex;

    public ArrayList<HCluster> hclusters;

    public HierarchicalClustering(int k, Point[] data, boolean isGPS) {
        this.isGPS = isGPS;
        this.K = k;
        this.data = data;
        this.data_number = data.length;
    }

    public void doCluster() {
        HClusterData hd = new HClusterData(data, data_number, K, isGPS);
        hd.doCluster();
        this.hclusters = hd.getHclusters();

        // export(filename, K, hclusters);
        // export1(filename, K, hclusters, data_number);
        // export2(filename, K, hclusters);
        // export3(filename, totalMSE);
    }

    public Cluster[] getCluster() {
        Cluster[] clusters = new Cluster[this.K];
        for (int i = 0; i < this.K; i++) {
            clusters[i] = new Cluster(isGPS, data[0].vectors.length);
        }

        for (int i = 0; i < this.hclusters.size(); i++) { // K类
            HCluster hcluster = this.hclusters.get(i);

            int size = hcluster.pointsIndexList.size();
            for (int j = 0; j < size; j++) {
                int index = hcluster.pointsIndexList.get(j);
                clusters[i].pointsList.add(this.data[index]);
            }
            clusters[i].calcCentroid(new Point(isGPS));
        }

        return clusters;
    }
    
        /**
     * calculate the MSE of clusters 计算总MSE
     *
     * @return MSE of clusters
     */
    public double calcTotalMSE() {
        double total_mse = 0.0;

        for (int i = 0; i < this.K; i++) {
            total_mse += hclusters.get(i).calcMSE(this.data);
        }

        return total_mse;
    }

    /**
     * export result
     *
     * @param file_name 输出文件名
     * @param k K个类
     * @param hclusters 聚类
     * @return
     */
    private void export(String file_name, int k, ArrayList<HCluster> hclusters) {
        System.out.println("export start ...");
        try {
            if (!file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File("./data_test/" + file_name + "_test.txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                for (int i = 0; i < k; i++) {
                    objectOts.writeBytes("The " + (i + 1) + " cluster");
                    objectOts.writeBytes("\r\n");

                    int cluster_size = hclusters.get(i).pointsIndexList.size();
                    objectOts.writeBytes("cluster size: " + cluster_size);
                    objectOts.writeBytes("\r\n");

                    for (int j = 0; j < cluster_size; j++) {
                        int indexOfNumber = hclusters.get(i).pointsIndexList.get(j); // 表示index
                        objectOts.writeBytes(indexOfNumber + 1 + ": " + data[indexOfNumber].vectors[0] + " " + data[indexOfNumber].vectors[1]);
                        objectOts.writeBytes("\r\n");
                    }
                    objectOts.writeBytes("\r\n");
                }
                objectOts.close();
                System.out.println("export end ...");
                /*        		String dstr = String.valueOf(d);
        		objectOts.writeBytes(String.valueOf(d));	
        		objectOts.writeBytes(" ");
        		objectOts.writeBytes("\n");*/
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
