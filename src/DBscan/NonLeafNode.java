package DBscan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NonLeafNode extends TreeNode {
	
	public List<TreeNode> children;
	
	public NonLeafNode()
	{
		children = new ArrayList<TreeNode>();
	}
	
	
	@Override
	public void insert(Tuple tuple)
	{
		int insertChildIndex = this.fetchchosenChildIndex(tuple);
		children.get(insertChildIndex).insert(tuple);
	}
	
	//return the index of the chosen child
	public int fetchchosenChildIndex(Tuple tuple)
	{
		int childIndex = 0;
		double minAddedArea = Double.MAX_VALUE;
		
		for (int i=0; i<children.size(); i++)
		{
			double tempArea = tuple.areaAddedIfInsertion(children.get(i).range);
			if (tempArea < minAddedArea)
			{
				childIndex = i;
				minAddedArea = tempArea;
			}
		}
		return childIndex;
	}

	@Override
	public void split() {
		
		NonLeafNode newBrother = new NonLeafNode();
		
		int fromIndex = this.children.size() / 2;
		for (int i=fromIndex; i<this.children.size(); i++)
		{
			newBrother.children.add(this.children.get(i));
		}
		//newBrother.children = this.children.subList(fromIndex, this.children.size());
		for (int i=0; i<newBrother.children.size(); i++)
		{
			newBrother.children.get(i).parent = newBrother;
		}
		newBrother.refreshRange();
		
		//remove the tuples that moved to the new tuple
		int newBrotherChildrenSize = newBrother.children.size();
		for (int i=0; i<newBrotherChildrenSize; i++)
			this.children.remove(fromIndex);
		this.refreshRange();
		
		if (this.parent != null)
		{
			this.parent.children.add(newBrother);
			this.parent.refreshRange();
			newBrother.parent = this.parent;
			
			if (this.parent.children.size() > RTree.M)
				this.parent.split();
		}else   //it is the root
		{
			NonLeafNode newParent = new NonLeafNode();
			newParent.children.add(this);
			newParent.children.add(newBrother);
			
			newParent.refreshRange();
			
			this.parent = newParent;
			newBrother.parent = newParent;
			
		}

	}

	@Override
	public List<Tuple> search(I searchRange) {
		List<Tuple> searchResult = new ArrayList<Tuple>();
//		for (int i=0; i<children.size(); i++)
//		{
//			if ( children.get(i).range.overlappedWith(searchRange) )
//				searchResult.addAll(children.get(i).search(searchRange));
//		}
		
		if(this.range.overlappedWith(searchRange))
		{
			Iterator<TreeNode> it =children.iterator();
			while(it.hasNext())
			{
				TreeNode child = it.next();
				if(child.range.overlappedWith(searchRange))
					searchResult.addAll(child.search(searchRange));
			}
		}
		return searchResult;
	}

	@Override
	public void refreshRange() {
		
		double newXMin = Double.MAX_VALUE;
		double newXMax = Double.MIN_VALUE;
		double newYMin = Double.MAX_VALUE;
		double newYMax = Double.MIN_VALUE;
		
		for (int i=0; i<children.size(); i++)
		{
			double childXMin = children.get(i).range.xMin;
			double childXMax = children.get(i).range.xMax;
			double childYMin = children.get(i).range.yMin;
			double childYMax = children.get(i).range.yMax;
			
			newXMin = (childXMin < newXMin)? childXMin : newXMin;
			newXMax = (childXMax > newXMax)? childXMax : newXMax;
			newYMin = (childYMin < newYMin)? childYMin : newYMin;
			newYMax = (childYMax > newYMax)? childYMax : newYMax;
		}
		
		this.setRange(newXMin, newXMax, newYMin, newYMax);
		
		if (this.parent != null)
			this.parent.refreshRange();
		
	}

}
