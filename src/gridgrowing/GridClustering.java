package gridgrowing;

/**
 *     R R R R           A           MMM         MMM    B B B B        O O O O      TTTTTTTTTTTTTTTT  
 *     R     R          A A          M  M       M  M    B      B      O       O            TT
 *     R     R         A   A         M   M     M   M    B      B     O         O           TT 
 *     R R  R         A     A        M    M   M    M    B B B B      O         O           TT
 *     R    R        A A A A A       M     MMM     M    B      B     O         O           TT
 *     R     R      A         A      M             M    B      B      O       O            TT
 *     R      R    A           A     M             M    B B B B        O O O O             TT  
 */

public class GridClustering {
	
	//four parameters 
	public static Point[] data;
	public static int m;
	public static int nx;
	public static int ny;
	
	//media data 
	public int[][] I;
	public int[][] seed;
	public int[][] J;
	
	public int clustersNumber = 0;  //the number of the clusters
	
	public GridClustering(Point[] data, int m, int nx, int ny)
	{
		this.data = data;
		this.m    = m;
		this.nx   = nx;
		this.ny   = ny;
	}
	
	//compute the grid each point belongs to and get the I
	//according to the given 'data','nx','ny'
	public boolean dataGridImage()
	{
		if (data == null || m == 0 || nx <= 0 || ny<= 0)
		{
			System.err.println("Please initial the parameters first!");
			return false;
		}
		
		double x_min = 0, x_max = 0;
		double y_min = 0, y_max = 0;
		double point_x, point_y;
		
		x_min = data[0].xValue;
		x_max = data[0].xValue;
		y_min = data[0].yValue;
		y_max = data[0].yValue;
		for (int i=0; i<data.length; i++)
		{
			point_x = data[i].xValue;
			point_y = data[i].yValue;
			if (point_x <= x_min)
				x_min = point_x;
			if (point_x >= x_max)
				x_max = point_x;
			if (point_y <= y_min)
				y_min = point_y;
			if (point_y >= y_max)
				y_max = point_y;
		}
		
		double width  = x_max - x_min;
		double height = y_max - y_min;
		if (width <= 0 || height <= 0)
		{
			System.err.println("The width and height are wrong!");
			return false;
		}
		int temp_t, temp_s;
		for (int i=0; i<data.length; i++)
		{
			temp_t = (int)Math.ceil( (x_max - data[i].xValue) / width * nx);
			temp_s = (int)Math.ceil( (y_max - data[i].yValue) / height * ny);
			if (temp_t == 0)
				temp_t = 1;
			if (temp_s == 0)
				temp_s = 1;
			
			data[i].t = temp_t;
			data[i].s = temp_s;
		}
		int xyBig = (nx>ny)? nx : ny;
		I = new int[xyBig+1][xyBig+1];
		for(int i=0; i< data.length; i++)
		{
			I[data[i].t][data[i].s] += 1;
		}
		
		return true;
	}
	
	//0 for random, 1 for maximal values, 2 for aliquots
	public void findSeeds(int option)
	{
		if (option<0 || option>2 || m<1)
		{
			System.err.println("No this option for finding seeds!");
			return;
		}
		
		seed = new int[m][2];
		if (option == 0)
		{
			//choose m values that range from 1 to nx*ny which represents 
			//the grid chosen to be one seed
			int[] seedGridNumbers = Brain.randomChoose(1, nx*ny, m);
			int temp_t, temp_s;
			for (int i=0; i<m; i++)
			{
				temp_t = (seedGridNumbers[i] - 1) / nx + 1;
				temp_s = seedGridNumbers[i] - (temp_t - 1) * nx;
				
				seed[i][0] = temp_t;
				seed[i][1] = temp_s;
			}
			
		}else if(option == 1)     
		{
			seed = Brain.getSeedByMaxValues(m, I);
		}else           // option == 2
		{
			int[] seedGridNumbers = Brain.aliquotsChoose(nx*ny, m);
			int temp_t, temp_s;
			for (int i=0; i<m; i++)
			{
				temp_t = (seedGridNumbers[i] - 1) / nx + 1;
				temp_s = seedGridNumbers[i] - (temp_t - 1) * nx;
				
				seed[i][0] = temp_t;
				seed[i][1] = temp_s;
			}

		}
		
	}
	
	public void seedGrowing()
	{
		if (seed == null)
		{
			System.out.println("The seed is empty, please use method 'findSeeds' first!");
			return;
		}
		int xyBig = (nx>ny)? nx : ny;
		J = new int[xyBig+1][xyBig+1];  
		
		int[] x_direction = {-1, 0, 1, 0,  1, -1, 1, -1};
		int[] y_direction = {0, 1, 0, -1, 1, 1, -1, -1};
		int label = 1;            
		int[] seedx = new int[(nx + 1) * (ny + 1)];
		int[] seedy = new int[(nx + 1) * (ny + 1)];
		
		for(int i=0; i<seed.length; i++)
		{
			int nStart = 1;
			int nEnd   = 1;
			seedx[1] = seed[i][0];
			seedy[1] = seed[i][1];
			boolean flag = false;
			
			if (J[seed[i][0]] [seed[i][1]] == 0)
			{
				J[seed[i][0]] [seed[i][1]] = label;
				while(nStart <= nEnd)
				{
					int current_x = seedx[nStart];
					int current_y = seedy[nStart];
					for (int k=0; k<8; k++)
					{
						int current_xx = current_x + x_direction[k];
						int current_yy = current_y + y_direction[k];
						if(current_xx > 0 && current_xx < (xyBig+1) 
						&& current_yy > 0 && current_yy < (xyBig+1))
						{
							if (I[current_xx][current_yy]>0 && J[current_xx][current_yy]==0)
							{
								flag = true;
								J[current_xx][current_yy] = label;
								nEnd ++;
								seedx[nEnd] = current_xx;
								seedy[nEnd] = current_yy;
							}
						}
					}
					nStart++;
				}
//				if(flag)    //only if label is assigned 
//				{
//					label++;
//					flag = false;
//				}
				//if(flag)
				label++;
				
			}
		}
		
		System.out.println("Label is : "+ label);
		if (label > 1)
		{
			this.clustersNumber = label - 1;
		}
		System.out.println("There are "+ this.clustersNumber + " clusters.");
	}
	
	public void assignClusterIDtoPoints()
	{
		for (int i=0; i<data.length; i++)
		{
			int gridT = data[i].t;
			int gridS = data[i].s;
			data[i].clusterID = J[gridT][gridS];
		}
	}
	
	public void printDataCluster()
	{
		for (int i=0; i<data.length; i++)
		{
			System.out.println(data[i].xValue + "," + data[i].yValue + " clusterID: "+ data[i].clusterID );
		}
	}
	
	public void printJ()
	{
		if(J != null && J.length>1 && J[0].length>1)
		{
			for(int i=1; i<J.length; i++)
			{
				for(int j=1; j<J[0].length; j++)
				{
					System.out.println("J["+i+"]["+j+"] : "+J[i][j]);
				}
			}
		}
	}
	
}
