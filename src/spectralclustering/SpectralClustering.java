package spectralclustering;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import org.ejml.simple.SimpleMatrix;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import main.Point;

public class SpectralClustering {

    private boolean isGPS;
    private int K;  // K个类
    private int data_number;  // the number of data 点的个数
    private Point[] data;
    private SimpleMatrix adjacentMatrix;  // 邻接矩阵
    private SimpleMatrix similarityMatrix;  // 相似矩阵
    private SimpleMatrix diagonalMatrix;  // 相似矩阵对应的对角矩阵
    private SimpleMatrix laplacianMatrix; // 拉普拉斯矩阵
    private Point[] result;
    
    public SpectralClustering(int k, Point[] data, boolean isGPS) {
        this.isGPS = isGPS;
        this.K = k;
        this.data = data;
        this.data_number = data.length;

        // matrix
        this.adjacentMatrix = new SimpleMatrix();
        this.similarityMatrix = new SimpleMatrix();
        this.diagonalMatrix = new SimpleMatrix();
        this.laplacianMatrix = new SimpleMatrix();

        calculateAdjacentMatrix(1);
        /* 计算邻接矩阵  */
        calculateSimilarityMatrix();
        /* 计算相似矩阵  */
        calculateDiagonalMatrix();
        /* 计算对角矩阵  */
        calculateLaplacianMatrix();
        /* 计算拉普拉斯矩阵  */

 /*		SimpleMatrix W = similarityMatrix;
		SimpleMatrix D = diagonalMatrix;
		SimpleMatrix L = laplacianMatrix;*/

        // export("L1", laplacianMatrix);
        doClustering();

        System.out.println("SpectralClustering end...");

    }

    /**
     * Calculates the Adjacent matrix
     *
     * @param type 距离类型 type = 1 计算欧式距离 type = 2 计算经纬度距离
     * @return
     */
    private void calculateAdjacentMatrix(int type) {
        System.out.println("Computing Adjacent matrix start...");
        adjacentMatrix = new SimpleMatrix(data_number, data_number);

        if (type == 1) {
            for (int row = 0; row < data_number; ++row) {
                Point p1 = data[row];
                for (int col = 0; col <= row; ++col) {
                    Point p2 = data[col];
                    // 利用公式计算欧式距离
                    double distance = p1.getDistance(p2);
                    adjacentMatrix.set(row, col, distance);
                    adjacentMatrix.set(col, row, distance);
                }
            }
        } else if (type == 2) {
            for (int row = 0; row < data_number; ++row) {
                Point p1 = data[row];
                for (int col = 0; col <= row; ++col) {
                    Point p2 = data[col];
                    // 利用公式计算两队经度、纬度的距离
                    double distance = p1.getDistance_(p2);
                    adjacentMatrix.set(row, col, distance);
                    adjacentMatrix.set(col, row, distance);
                }
            }
        }

        // export("adjacentMatrix", adjacentMatrix);
        System.out.println("Computing Adjacent matrix end...");

    }

    /**
     * Calculates the Similarity matrix
     *
     * @return
     */
    private void calculateSimilarityMatrix() {
        System.out.println("Computing Similarity matrix...");

        similarityMatrix = new SimpleMatrix(data_number, data_number);
        for (int row = 0; row < data_number; ++row) {
            // DataInstance a1 = clusterData.get(i);
            for (int col = 0; col <= row; ++col) {

                double distance = adjacentMatrix.get(row, col); // get
                double new_distance;
                if (distance == 0) {  // 相同的点距离为0，相似度设置为0， 防止1/0出现无穷大
                    new_distance = 0;
                } else {
                    new_distance = 1 / distance;
                }

                similarityMatrix.set(row, col, new_distance);
                similarityMatrix.set(col, row, new_distance);
            }
        }
    }

    /**
     * Calculates Diagonal matrix from Similarity matrix
     */
    private void calculateDiagonalMatrix() {
        System.out.println("Computing Diagonal values from Similarity matrix...");

        /* initialize matrix */
        diagonalMatrix = new SimpleMatrix(data_number, data_number);
        for (int row = 0; row < data_number; ++row) {
            SimpleMatrix rowMatrix = similarityMatrix.extractVector(true, row);  // 提取每一行
            double sum = 0;

            for (int col = 0; col < data_number; col++) {  // 计算该行之和
                sum += rowMatrix.get(col);
            }
            diagonalMatrix.set(row, row, sum);
        }
    }

    /**
     * Calculates the Laplacian matrix
     */
    private void calculateLaplacianMatrix() {
        System.out.println("Computing Laplacian matrix from Diagonal matrix...");
        /* initialize matrix*/
        SimpleMatrix W = similarityMatrix;
        SimpleMatrix D = diagonalInverseSquareRoot(diagonalMatrix);
        laplacianMatrix = D.mult(W).mult(W);

        System.out.println("Computing Laplacian matrix from Diagonal matrix end...");
    }

    /**
     * Takes the D^-1/2 for a diagonal matrix...
     *
     * @param matrix
     * @return
     */
    private SimpleMatrix diagonalInverseSquareRoot(SimpleMatrix matrix) {
        for (int i = 0; i < matrix.numRows(); i++) {
            double d = matrix.get(i, i);
            d = 1.0 / Math.sqrt(d);
            matrix.set(i, i, d);
        }
        return matrix;
    }

    /**
     * Calculates the eigenvalue and eigenvector, and then cluster
     */
    public void doClustering() {
        // int rowDim = laplacianMatrix.numRows();

        /* eigven vecs matrix */
        SimpleMatrix X = new SimpleMatrix(data_number, K);

        /*		int x = X.numRows();
		int y = X.numCols();*/
 /* Eigen decomposition */
        System.out.println("Performing Eigen Decomposition on Laplacian matrix...");

        double[][] A = new double[data_number][data_number];

        for (int i = 0; i < data_number; ++i) {
            for (int j = 0; j < data_number; ++j) {
                A[i][j] = laplacianMatrix.get(i, j);
            }
        }

        Matrix m = new Matrix(A);

        // public EigenvalueDecomposition(Matrix Arg)
        EigenvalueDecomposition eig = new EigenvalueDecomposition(m);
        Matrix v = eig.getV();
        Matrix d = eig.getD();

        /*
	    EigenvalueDecomposition ans = m.eig();
	    Matrix d = ans.getD();  // 1863 * 1863  特征值
	    Matrix v = ans.getV();  // 1863 * 1863 特征向量
         */
        for (int col = 0; col < K; ++col) {
            for (int row = 0; row < data_number; ++row) {
                X.set(row, col, v.get(row, col));
            }
        }

        /* Normalize the rows of X */
        SimpleMatrix Y = new SimpleMatrix(X.numRows(), X.numCols());
        for (int i = 0; i < X.numRows(); i++) {
            double xDenom = 0;
            for (int j = 0; j < X.numCols(); j++) {
                xDenom += Math.pow(X.get(i, j), 2);
            }
            xDenom = (xDenom > 0) ? 1.0 / Math.sqrt(xDenom) : 0;
            for (int j = 0; j < X.numCols(); j++) {
                double val = X.get(i, j) * xDenom;
                Y.set(i, j, val);
            }
        }

        result = new Point[data_number];

        for (int i = 0; i < data_number; ++i) {
            double[] vector = new double[K];
            for (int j = 0; j < K; j++) {
                vector[j] = Y.get(i, j);  // v
            }
            Point p = new Point(vector, isGPS);
            p.index = i;
            result[i] = p;
            
            // pointList.add(point)
        }

        /*		
	     * SVD优化，比eig还慢一点
		SingularValueDecomposition svd =  m.svd();
		svd.getU();
		svd.getS();
		svd.getV();*/
 /*		SimpleEVD<SimpleMatrix> eig = laplacianMatrix.eig();
		// Now get the K largest column vectors 
		for (int col = 0; col < K; col++) {
			SimpleMatrix vec = eig.getEigenVector(col);
			for (int row = 0; row < data_number; row++) {
				X.set(row, col, vec.get(row));
			}
		}*/
 /* Normalize the rows of X */
 /*		SimpleMatrix Y = new SimpleMatrix(X.numRows(), X.numCols());
		for (int i = 0; i < X.numRows(); i++) {
			double xDenom = 0;
			for (int j = 0; j < X.numCols(); j++) {
				xDenom += Math.pow(X.get(i, j), 2);
			}
			xDenom = (xDenom > 0) ? 1.0 / Math.sqrt(xDenom) : 0;
			for (int j = 0; j < X.numCols(); j++) {
				double val = X.get(i, j) * xDenom;
				Y.set(i, j, val);
			}
		}*/
        // SimpleMatrix Ycopy = Y;
        System.out.println("end................");
        System.out.println("end doClustering");
    }

    /**
     * get the martix of N * K， N is the number of data, K is K个类
     *
     * @return
     */
    public Point[] getResltMartix() {
        if (result != null) {
            return result;
        } else {
            return null;
        }

    }

    private String printMatrix(SimpleMatrix matrix) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat f = new DecimalFormat("###.###");
        for (int i = 0; i < matrix.numRows(); i++) {
            String[] arr = new String[matrix.numCols()];
            for (int j = 0; j < matrix.numCols(); j++) {
                arr[j] = f.format(matrix.get(i, j));
            }
            sb.append(String.join("\t", arr) + "\n");
        }
        return sb.toString();
    }

    /**
     * export...
     *
     * @param matrix
     * @return
     */
    private void export(String file_name, SimpleMatrix matrix) {
        System.out.println("export start ...");
        try {
            if (!file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File("./data_test_tian/" + file_name + ".txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                /*            	int row1 = matrix.numRows();
            	int col1 = matrix.numCols();
            	int data1 = data_number;*/
                for (int row = 0; row < data_number; ++row) {
                    for (int col = 0; col < data_number; ++col) {
                        double d = matrix.get(row, col);  // get

                        System.out.print(d);
                        System.out.print(" ");

                        // objectOts.writeDouble(d);
                        // objectOts.writeChars(String.valueOf(d));
                        String dstr = String.valueOf(d);
                        objectOts.writeBytes(String.valueOf(d));

                        objectOts.writeBytes(" ");

                    }
                    System.out.println();
                    objectOts.writeBytes("\n");
                }

                objectOts.close();
                System.out.println("export end ...");
                System.out.println(file_name + " ok");

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void export_row(String file_name, SimpleMatrix matrix) {
        System.out.println("export start ...");
        try {
            if (!file_name.equals("")) {
                FileOutputStream fileOts = new FileOutputStream(new File("./data_test_tian/" + file_name + ".txt"));
                DataOutputStream objectOts = new DataOutputStream(fileOts);

                for (int row = 0; row < matrix.numCols(); ++row) {

                    double d = matrix.get(row);  // get	

                    if (d == 0.0) {
                        System.out.println("value = 0, index is: " + row);
                    }

                    objectOts.writeChars(String.valueOf(d));
                    objectOts.writeChars(" ");

                }
                objectOts.writeChars("\n");
                objectOts.close();
                System.out.println("export end ...");
                System.out.println(file_name + " ok");

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
