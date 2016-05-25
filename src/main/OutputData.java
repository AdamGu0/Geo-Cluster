/**
 *
 */
package main;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import hierarchicalclustering.HCluster;

/**
 * @author
 *
 */
public class OutputData {

    /*
	 * common 
     */
    // file name
    private String file_name;
    // K
    private int K;
    // data number, that is the number of point
    private int data_number;

    /*
	 *  hierarchical cluster Result
     */
    private ArrayList<HCluster> hclusters;

    /*
	 *  Spectral cluster Result
     */
    private Cluster[] clusters;

    private Point[] data;

    public Point[] centralPoint;     // 保存谱聚类中点 

    /**
     * hierarchical clustering constructor and initialization
     *
     * @param file_name 输出文件名
     * @param k K个类
     * @param hclusters 聚类结果
     * @param data_number 点的个数
     * @return
     */
    public OutputData(String file_name, int K, ArrayList<HCluster> hclusters, int data_number) {
        this.file_name = file_name + "_HierarchicalCluster";
        this.K = K;
        this.hclusters = hclusters;
        this.data_number = data_number;
    }

    /**
     * Spectral clustering constructor and initialization
     *
     * @param file_name 输出文件名
     * @param k K个类
     * @param clusters 聚类结果
     * @param data_number 点的个数
     * @return
     */
    public OutputData(String file_name, int K, Cluster[] clusters, int data_number, Point[] data) {
        this.file_name = file_name + "_SpectralCluster";
        this.K = K;
        this.clusters = clusters;
        this.data_number = data_number;
        this.data = data;
        this.centralPoint = new Point[K];

        System.out.println("ok");
    }

    /*
	 *  hierarchical clustering
     */
    public boolean outHierarchicalData(double totalMSE) {
        String path = "./data_result/HierarchicalCluster/";

        if (!OutputHierarchicalClusterResult(path)) {
            return false;
        }
        if (!OutputHierarchicalCentralPoint(path)) {
            return false;
        }
        if (!OutputMSE(path, totalMSE)) // Output hierarchical MSE
        {
            return false;
        }

        return true;

    }

    /**
     * Output hierarchical cluster result
     *
     * @param path the path to be write
     * @return
     */
    private boolean OutputHierarchicalClusterResult(String path) {
        System.out.println("Output ClusterResult start ...");
        try {
            if (!this.file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File(path + this.file_name + "_ClusterResult.txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                int[] cluster_result = new int[this.data_number]; // int[i] = k表示第i个数(index从0开始)的类别为k

                for (int i = 0; i < this.K; i++) {
                    int cluster_size = this.hclusters.get(i).pointsIndexList.size();

                    for (int j = 0; j < cluster_size; j++) {
                        int indexOfNumber = this.hclusters.get(i).pointsIndexList.get(j); // 表示index
                        cluster_result[indexOfNumber] = i + 1; // 赋值类别
                    }
                }

                int index = 0;
                for (; index < this.data_number - 1; index++) {
                    // objectOts.writeBytes(index + 1 + " " + cluster_result[index]);
                    objectOts.writeBytes("" + cluster_result[index]);
                    objectOts.writeBytes("\r\n");
                }
                objectOts.writeBytes("" + cluster_result[index]);

                objectOts.close();
                System.out.println("Output ClusterResult end ...");

                return true;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Output hierarchical centralPoint
     *
     * @param path the path to be write
     * @return
     */
    private boolean OutputHierarchicalCentralPoint(String path) {
        System.out.println("Output CentralPoint start ...");
        try {
            if (!this.file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File(path + this.file_name + "_CentralPoint.txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                for (int i = 0; i < this.K; i++) {
                    double[] centroid = this.hclusters.get(i).centroid;
                    objectOts.writeBytes(i + 1 + "");
                    for (int j = 0; j < centroid.length; j++) {
                        double d = centroid[j];
                        objectOts.writeBytes(" " + d);
                    }
                    if (i != this.K - 1) {
                        objectOts.writeBytes("\r\n");
                    }
                }

                objectOts.close();
                System.out.println("Output CentralPoint end ...");

                return true;

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Output MSE
     *
     * @param path the path to be write
     * @param totalMSE the total MSE
     * @return
     */
    public boolean OutputMSE(String path, double totalMSE) {
        System.out.println("Output MSE start ...");
        try {
            if (!this.file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File(path + this.file_name + "_MSE.txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                objectOts.writeBytes(totalMSE + "");
                // objectOts.writeBytes("\r\n");            
                objectOts.close();
                System.out.println("Output MSE end ...");

                return true;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    /*
	 *  Spectral clustering
     */
    public boolean outSpectralData() {
        String path = "./data_result/SpectralCluster/";

        if (!OutputSpectralClusterResult(path)) {
            return false;
        }

        if (!OutputSpectralCentralPoint(path)) {
            return false;
        }
        /*
		 if (!OutputMSE(totalMSE))   // Output hierarchical MSE
			 return false;*/

        return true;
    }

    /**
     * Output Spectral cluster result
     *
     * @return
     */
    private boolean OutputSpectralClusterResult(String path) // String file_name, Cluster[] clusters, int k
    {
        System.out.println("Output ClusterResult start ...");
        try {
            if (!this.file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File(path + this.file_name + "_ClusterResult.txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                int[] cluster_result = new int[this.data_number];

                for (int i = 0; i < this.K; i++) {

                    int cluster_size = this.clusters[i].pointsList.size();
                    // objectOts.writeBytes("cluster size: " + cluster_size);
                    // objectOts.writeBytes("\r\n");

                    for (int j = 0; j < cluster_size; j++) {
                        int indexOfNumber = (int) this.clusters[i].pointsList.get(j).index; // 表示index
                        cluster_result[indexOfNumber] = i + 1; // 赋值类别
                    }
                }

                int index = 0;
                for (; index < this.data_number - 1; index++) {
                    // objectOts.writeBytes(index + 1 + " " + cluster_result[index]);
                    objectOts.writeBytes("" + cluster_result[index]);
                    objectOts.writeBytes("\r\n");
                }
                objectOts.writeBytes("" + cluster_result[index]);

                objectOts.close();
                System.out.println("Output ClusterResult end ...");

                return true;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Output Spectral centralPoint
     *
     * @return
     */
    private boolean OutputSpectralCentralPoint(String path) {
        System.out.println("Output CentralPoint start ...");
        try {
            if (!this.file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File(path + this.file_name + "_CentralPoint.txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                int dimensionality = this.data[0].vectors.length;  // 维度
                for (int i = 0; i < this.K; i++) {
                    int cluster_size = this.clusters[i].pointsList.size();
                    double[] centroid = new double[dimensionality];

                    objectOts.writeBytes(i + 1 + "");

                    for (int dim = 0; dim < dimensionality; dim++) {
                        for (int count = 0; count < cluster_size; count++) {
                            int indexOfNumber = (int) this.clusters[i].pointsList.get(count).index;
                            centroid[dim] += this.data[indexOfNumber].vectors[dim];
                        }
                        // objectOts.writeBytes(" " + d);
                    }

                    for (int dim = 0; dim < dimensionality; dim++) {
                        if (cluster_size > 0) {
                            centroid[dim] /= cluster_size;
                            objectOts.writeBytes(" " + centroid[dim]);
                        } else {
                            objectOts.writeBytes(" " + "0");
                        }
                    }

                    Point point = new Point(centroid, data[0].isGPS);
                    this.centralPoint[i] = (Point) point;

                    if (i != this.K - 1) {
                        objectOts.writeBytes("\r\n");
                    }
                }

                objectOts.close();
                System.out.println("Output CentralPoint end ...");

                return true;

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * calculate the MSE of clusters 计算总MSE
     *
     * @return MSE of Spectral clusters
     */
    public double calcTotalMSE() {
        double total_mse = 0.0;

        for (int i = 0; i < this.K; i++) {

            int cluster_size = this.clusters[i].pointsList.size();
            for (int count = 0; count < cluster_size; count++) {
                int indexOfNumber = (int) this.clusters[i].pointsList.get(count).index;
                total_mse += this.centralPoint[i].getDistance(this.data[indexOfNumber]);
            }
            total_mse /= cluster_size;

        }

        return total_mse;
    }

}
