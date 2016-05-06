package gridgrowing;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *     R R R R           A           MMM         MMM    B B B B        O O O O      TTTTTTTTTTTTTTTT  
 *     R     R          A A          M  M       M  M    B      B      O       O            TT
 *     R     R         A   A         M   M     M   M    B      B     O         O           TT 
 *     R R  R         A     A        M    M   M    M    B B B B      O         O           TT
 *     R    R        A A A A A       M     MMM     M    B      B     O         O           TT
 *     R     R      A         A      M             M    B      B      O       O            TT
 *     R      R    A           A     M             M    B B B B        O O O O             TT  
 */

public class Point {

	public double xValue;
	public double yValue;

	//t and s represents the grid this point belongs to
	public int t;
	public int s;
	
	//the clusterID it belongs to
	public int clusterID = 0;
	
	public Point(double x, double y)
	{
		this.xValue = x;
		this.yValue = y;
	}
	
}
