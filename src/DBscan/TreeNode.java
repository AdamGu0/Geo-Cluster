package DBscan;

import java.util.List;

public abstract class TreeNode {
	
	public NonLeafNode parent;
	
	//the parameter of I (two-dimensional)
	public I range;
	
	public TreeNode()
	{
		range = new I();
	}
	
	public abstract void split();
	public abstract void insert(Tuple tuple);
	public abstract List<Tuple> search(I range);
	
	public abstract void refreshRange();   //after insertion (or deletion), the range of the tree should be refreshed

	public void setRange(double newXMin, double newXMax, double newYMin, double newYMax)
	{
		this.range.xMin = newXMin;
		this.range.xMax = newXMax;
		this.range.yMin = newYMin;
		this.range.yMax = newYMax;
	}
}
