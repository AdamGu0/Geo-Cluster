package DBscan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LeafNode extends TreeNode{

	public List<Tuple> tuples;
	
	public LeafNode()
	{
		tuples = new ArrayList<Tuple>();
	}
	
	//TODO: adjust tree
	@Override
	public void insert(Tuple tuple)
	{
		tuples.add(tuple);
		
		if (tuples.size() <= RTree.M)
			this.refreshRange();
		else
			this.split();
	}
	
	//refresh the range of the node after insertion(or deletion)
	@Override
	public void refreshRange() {
		double newXMin = Double.MAX_VALUE;
		double newXMax = Double.MIN_VALUE;
		double newYMin = Double.MAX_VALUE;
		double newYMax = Double.MIN_VALUE;
		
		for(int i=0; i<tuples.size(); i++)
		{
			double xValue = tuples.get(i).xValue;
			double yValue = tuples.get(i).yValue;
			newXMin = (xValue < newXMin) ? xValue : newXMin;
			newXMax = (xValue > newXMax) ? xValue : newXMax;
			newYMin = (yValue < newYMin) ? yValue : newYMin;
			newYMax = (yValue > newYMax) ? yValue : newYMax;
		}
		
		this.setRange(newXMin, newXMax, newYMin, newYMax);
		
		if (this.parent != null)
			this.parent.refreshRange();
	}

	
	
	
	@Override
	public void split() {
		
		LeafNode newLeafNode = new LeafNode();
		
		
		int fromIndex = this.tuples.size() / 2;
		
		for (int i=fromIndex; i<this.tuples.size(); i++)
		{
			newLeafNode.tuples.add(this.tuples.get(i));
		}
//		newLeafNode.tuples = this.tuples.subList(fromIndex, this.tuples.size());
		newLeafNode.refreshRange();
		
		//remove the tuples that moved to the new tuple
		int newLeafTupleSize = newLeafNode.tuples.size();
		for (int i=0; i<newLeafTupleSize; i++)
			this.tuples.remove(fromIndex);
		this.refreshRange();
		
		
		if (this.parent != null)
		{
			this.parent.children.add(newLeafNode);
			newLeafNode.parent = this.parent;
			this.parent.refreshRange();
			
			
			if (this.parent.children.size() > RTree.M)
				this.parent.split();
		}else
		{
			NonLeafNode newParent = new NonLeafNode();
			newParent.children.add(this);
			newParent.children.add(newLeafNode);
			
//			double parentXMin = (this.range.xMin < newLeafNode.range.xMin)? this.range.xMin : newLeafNode.range.xMin;
//			double parentXMax = (this.range.xMax > newLeafNode.range.xMax)? this.range.xMax : newLeafNode.range.xMax;
//			double parentYMin = (this.range.yMin < newLeafNode.range.yMin)? this.range.yMin : newLeafNode.range.yMin;
//			double parentYMax = (this.range.yMax > newLeafNode.range.yMax)? this.range.yMax : newLeafNode.range.yMax;
//			newParent.setRange(parentXMin, parentXMax, parentYMin, parentYMax);
			
			newParent.refreshRange();
			
			this.parent = newParent;
			newLeafNode.parent = newParent;
		}
		
	}

//	@Override
//	public List<Tuple> search(I range) {
//		
//		List<Tuple> searchResult  = new ArrayList<Tuple>();
//		for (int i=0; i<tuples.size(); i++)
//		{
//			if (tuples.get(i).isInRange(range))
//				searchResult.add(tuples.get(i));
//		}
//		return searchResult;
//	}


	@Override
	public List<Tuple> search(I range)
	{
		List<Tuple> searchResult = new ArrayList<Tuple>();
		
		Iterator<Tuple> iterator = tuples.iterator();
		while(iterator.hasNext())
		{
			Tuple t = iterator.next();
			if(t.isInRange(range))
				searchResult.add(t);
		}
		return searchResult;
	}


	

}
