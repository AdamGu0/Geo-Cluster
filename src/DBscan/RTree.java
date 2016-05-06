package DBscan;

import java.util.ArrayList;
import java.util.List;

public class RTree {
	
	public static int M = 5;
	
	public TreeNode root;
	
	public RTree ()
	{}
	
	public void insert(Tuple tuple)
	{
		//if the tree is empty
		if (root == null)
			root = new LeafNode();
		root.insert(tuple);
		
		//after insertion, there could generate a new root
		if (root.parent != null)
			root = root.parent;
	}
	
	public List<Tuple> searchFromRoot(I searchRange)
	{
		if (root==null)
			return null;
		else
			return root.search(searchRange);
	}
	
	public List<Tuple> search(I searchRange, TreeNode fromNode)
	{
		return fromNode.search(searchRange);
	}
	
	public boolean isEmpty()
	{
		return (this.root == null);
	}
}
