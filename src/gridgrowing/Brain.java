package gridgrowing;

import java.util.Comparator;
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

public class Brain {
	
	//return n random values range that: min <= X <= max
	public static int[] randomChoose(int min, int max, int n)
	{
	    if (n > (max - min + 1) || max < min) 
	       return null;   
	    
	    int[] result = new int[n];
	    int count = 0;
	    while(count < n) 
	    {
	        int num = (int) (Math.random() * (max - min)) + min;  
	        boolean flag = true;  
	        for (int j = 0; j < n; j++) 
	        {  
	            if(num == result[j])
	            {  
	                flag = false;
	                break;
	            }  
	        }
	        if(flag)
	        {  
	            result[count] = num;  
	            count++;  
	        }  
	    }  
	    return result;  
	}  
	
	//return m aliquots values range from 1 to topNumber
	public static int[] aliquotsChoose(int topNumber, int m)
	{
		int[] result = new int[m];
		if(m > (topNumber / 2))
		{
			for(int i=0; i<m; i++)
				result[i] = i;
		}else
		{
			int groupSize   = (int) topNumber / m;
			int startNumber = (int)(Math.random()*groupSize) + 1;
			for(int i=0;i<m;i++)
				result[i] = startNumber + groupSize * i;
		}
		return result;
	}
	
	//m: number of the seed
	public static int[][] getSeedByMaxValues(int m, int[][] I)
	{
		int[][] result = new int[m][2];
		
		TreeSet<GridInfo> ssa = new TreeSet<GridInfo>(new SortGridByPointNumber());
		
		int ny = I.length - 1;
		int nx = I[0].length - 1;
		if (nx<1 || ny<1)
		{
			System.err.println("Error with input parameter I!");
			return null;
		}
		for (int i=1; i<=ny; i++)
		{
			for (int j=1; j<=nx; j++)
			{
				if (ssa.size()< m)
				{
					// 
					ssa.add(new GridInfo(i,j,I[i][j]));
				}else
				{
					//
					if (I[i][j] > ssa.first().numberOfPoints)
					{
						ssa.pollFirst(); //remove the minimal element of the set
						ssa.add(new GridInfo(i,j,I[i][j]));
					}
				}
			}
		}
		
		if (ssa.size() != m)
		{
			System.out.println("The number of seeds is not m!");
			return null;
		}
		
		for (int i=0; i<m; i++)
		{
			GridInfo tempgi = ssa.pollFirst();
			result[i][0] = tempgi.t;
			result[i][1] = tempgi.s;
		}
		
		return result;
	}
	
	
	public static class GridInfo
	{
		public int t;
		public int s;
		public int numberOfPoints;
		
		public GridInfo(int t, int s, int numberOfPoints)
		{
			this.t = t;
			this.s = s;
			this.numberOfPoints = numberOfPoints;
		}
	}
	
	//comparator of two grid (by piont number)
	public static class SortGridByPointNumber implements Comparator<Object>
	{
		@Override
		public int compare(Object o1, Object o2) 
		{
			GridInfo gi1 = (GridInfo)o1;
			GridInfo gi2 = (GridInfo)o2;
			if (gi1.numberOfPoints <= gi2.numberOfPoints)
				return -1;
			else
				return 1;
		}
	}
}
