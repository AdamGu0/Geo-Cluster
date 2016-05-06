package DBscan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *     R R R R           A           MMM         MMM    B B B B        O O O O      TTTTTTTTTTTTTTTT  
 *     R     R          A A          M  M       M  M    B      B      O       O            TT
 *     R     R         A   A         M   M     M   M    B      B     O         O           TT 
 *     R R  R         A     A        M    M   M    M    B B B B      O         O           TT
 *     R    R        A A A A A       M     MMM     M    B      B     O         O           TT
 *     R     R      A         A      M             M    B      B      O       O            TT
 *     R      R    A           A     M             M    B B B B        O O O O             TT  
 */

public class DBScan {

    public static double Eps;
    public static int MinPts = 1;

    public List<Tuple> dataObjects;   //all the data

    private RTree rtree;

    public DBScan() {
        rtree = new RTree();
        dataObjects = new ArrayList<Tuple>();
    }

    //step one
    public void setDataObjectsAndBuildRTree(List<Tuple> data) {
        if (data == null || data.size() == 0) {
            System.err.println("The list of the data shouldn't be empty!");
            return;
        }

        this.dataObjects = data;
        long startBuildTree = System.currentTimeMillis();
        for (int i = 0; i < dataObjects.size(); i++) {
            rtree.insert(dataObjects.get(i));
        }
        long finishBuildTree = System.currentTimeMillis();
        System.out.println("Time spent build RTree: " + (finishBuildTree - startBuildTree) + " ms.");
    }

    //step two
    //mark every point if it is core point
    public void checkCorePoints() {
        System.out.println();
        long startCheck = System.currentTimeMillis();
        if (dataObjects == null || dataObjects.size() == 0) {
            System.err.println("dataOjects is empty!");
            return;
        }

        Iterator<Tuple> iterator = dataObjects.iterator();
        while (iterator.hasNext()) {
            Tuple tuple = iterator.next();
            int neighborNumber = fetchNeighbors(tuple).size();
            if (neighborNumber > DBScan.MinPts) {
                tuple.isCorePoint = true;
            }
        }
        long finishCheck = System.currentTimeMillis();
        System.out.println("Checing every point if it is core point spent: " + (finishCheck - startCheck) + " ms.");
    }

    //step three (main step)
    //return the number of the clusters(clusterID from 1 to n)
    //-1 represents wrong
    public int dbscan() {
        if (dataObjects == null || dataObjects.size() == 0) {
            System.err.println("dataOjects is empty!");
            return -1;
        }

        int clusterID = 1;
        Iterator<Tuple> iterator = dataObjects.iterator();

        while (iterator.hasNext()) {
            Tuple tuple = iterator.next();
            if (tuple.isVisited) {
                continue;
            }
            tuple.isVisited = true;
            if (tuple.isCorePoint) {
                tuple.clusterID = clusterID;
                includeNeighbors(tuple, clusterID);
                clusterID++;
            }
        }

//		while(iterator.hasNext())
//		{
//			Tuple tuple = iterator.next();
//			if(tuple.isVisited)
//				continue;
//			tuple.isVisited = true;
//			
//			if (fetchNeighbors(tuple).size() > DBScan.MinPts)
//			{
//				tuple.clusterID = clusterID;
//				includeNeighbors(tuple, clusterID);
//				clusterID++;
//			}
//				
//		}
        return clusterID - 1;
    }

    //set the neighbors of the tuple to be of cluster: clusterID
    public void includeNeighbors(Tuple tuple, int clusterID) {
        List<Tuple> myNeighbors = fetchNeighbors(tuple);
        Iterator<Tuple> iterator = myNeighbors.iterator();
        while (iterator.hasNext()) {
            Tuple neighbor = iterator.next();
            if (neighbor.clusterID == 0) {
                neighbor.isVisited = true;
                neighbor.clusterID = clusterID;
                //TODO: i think only the core-point neighbor is needed to expand it's neighbors

                if (neighbor.isCorePoint) {
                    includeNeighbors(neighbor, clusterID);
                }

//				if(fetchNeighbors(neighbor).size() > DBScan.MinPts)
//					includeNeighbors(neighbor, clusterID);
            }
        }
    }

    //get the neighbors of one tuple
    //self not included
    public List<Tuple> fetchNeighbors(Tuple tuple) {
        if (this.rtree.isEmpty()) {
            return null;
        }

        List<Tuple> neighbors = new ArrayList<Tuple>();

        I rectangleRange = new I();
        rectangleRange.setRange(tuple.xValue - DBScan.Eps, tuple.xValue + DBScan.Eps,
                tuple.yValue - DBScan.Eps, tuple.yValue + DBScan.Eps);
        List<Tuple> candidateNeighbors = rtree.searchFromRoot(rectangleRange);
        Iterator<Tuple> iterator = candidateNeighbors.iterator();
        while (iterator.hasNext()) {
            Tuple candidateTuple = iterator.next();
            if (tuple.distanceTo(candidateTuple) <= DBScan.Eps) {
                neighbors.add(candidateTuple);
            }
        }

        neighbors.remove(tuple);

        return neighbors;
    }

}
