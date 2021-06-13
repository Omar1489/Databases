package Apes;
import java.awt.Polygon;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import BPTree.BPTree;
import BPTree.TupleRef;

public class Page extends Vector<Tuple> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int size;  
	private int nodeSize;
	private String strClusteringKeyColumn;
	private String pageName;

	public Page(int size, int nodeSize, String strClusteringKeyColumn, String pageName) {
		this.size = size;
		this.nodeSize = nodeSize;
		this.strClusteringKeyColumn = strClusteringKeyColumn;
		this.pageName= pageName;
	}

	public boolean isFull() {
		return (this.size() == size);
	}

	public int getSize() {
		return size;

	}

	public int getNodeSize() {
		return nodeSize;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void updateToRight(int index , Object clusterKeyValue ,Hashtable<String,Object> htblColNameValue, ArrayList<String> toBeUpdatedTrees,
			ArrayList<String> bpTrees, ArrayList<String> rTrees) {		
		int i = index;
		do {
			Tuple tuple = get(i);
			
			if (clusterKeyValue instanceof Polygon) {
				MyPolygon p1 = new MyPolygon((Polygon) clusterKeyValue);
				MyPolygon p2 = new MyPolygon((Polygon) get(i).get(strClusteringKeyColumn));
				if (!p1.isEqual(p2))
					continue;
			}
			for (String treeName : toBeUpdatedTrees) {
				if (bpTrees.contains(treeName)) {
					BPTree bpTree = (BPTree) Functions.deserialize("data/" + treeName + ".class");
					bpTree.delete(tuple);
					Functions.serialize(bpTree, "data/" + treeName + ".class");
				}
				else {
					BPTree rTree = (BPTree) Functions.deserialize("data/" + treeName + ".class");
					rTree.delete(tuple);
					Functions.serialize(rTree, "data/" + treeName + ".class");
				}
			}
			tuple.updateTuple(htblColNameValue);
			for (String treeName : toBeUpdatedTrees) {
				if (bpTrees.contains(treeName)) {
					BPTree bpTree = (BPTree) Functions.deserialize("data/" + treeName + ".class");
					bpTree.insert(tuple, new TupleRef(pageName, i));
					Functions.serialize(bpTree, "data/" + treeName + ".class");
				}
				else {
					BPTree rTree = (BPTree) Functions.deserialize("data/" + treeName + ".class");
					rTree.insert(tuple, new TupleRef(pageName, i));
					Functions.serialize(rTree, "data/" + treeName + ".class");
				}
			}
			i++;
		}
		while(i< size() && Functions.compareObjects(get(i).get(strClusteringKeyColumn), clusterKeyValue) == 0); 
		
	}

	public int firstTupleIndex(int low, int high, Object keyValue) {
		/*
		 *  returns the first occurrence of a Tuple which clustering Key is equal to the give keyValye.
		 *	Returns -1 , if keyValue does Not exist.
		 */
		if(high >= low) { 
			int mid = low + (high - low)/2; 
			if((mid == 0 || Functions.compareObjects(keyValue, get(mid-1).get(strClusteringKeyColumn)) > 0) 
					&& Functions.compareObjects(keyValue, get(mid).get(strClusteringKeyColumn)) == 0) 
				return mid; 
			else if(Functions.compareObjects(keyValue, get(mid).get(strClusteringKeyColumn)) > 0) 
				return firstTupleIndex((mid + 1), high, keyValue); 
			else
				return firstTupleIndex(low, (mid -1), keyValue); 
		} 
		return -1; 	
	}

	public int LastTupleIndex(int low , int high , Object keyValue) {
		/*
		 *  returns the Last occurrence of a Tuple which clustering Key is equal to the give keyValye.
		 *	Returns -1 , if keyValue does Not exist.
		 */
		if (high >= low) 
		{ 
			int mid = low + (high - low)/2; 
			if (( mid == size()-1 || Functions.compareObjects(keyValue, get(mid+1).get(strClusteringKeyColumn)) < 0) 
					&& Functions.compareObjects(keyValue, get(mid).get(strClusteringKeyColumn)) == 0) 
				return mid; 
			else if (Functions.compareObjects(keyValue, get(mid).get(strClusteringKeyColumn)) < 0) 
				return LastTupleIndex(low, (mid -1), keyValue); 
			else
				return LastTupleIndex((mid + 1), high,keyValue); 
		} 
		return -1; 
	}
	
	public int rangeBinarySearch (int low , int high , Tuple tuple ) {
		if(high >= low) { 
			int mid = low + (high - low)/2; 
			if((mid == this.size() - 1 || tuple.compareTo(this.get(mid+1)) < 0) && tuple.compareTo(this.get(mid)) >= 0) 
				return mid + 1; 
			else if(tuple.compareTo(this.get(mid)) < 0) 
				return rangeBinarySearch(low, (mid - 1), tuple); 
			else
				return rangeBinarySearch((mid + 1), high, tuple); 
		} 
		return 0; 
	}
	
}
