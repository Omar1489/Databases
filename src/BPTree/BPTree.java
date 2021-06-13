package BPTree;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import Apes.SQLTerm;
import Apes.Tuple;

public class BPTree<T extends Comparable<T>> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int order;
	private BPTreeNode<T> root;
	private String indexColumn;
	
	/**
	 * Creates an empty B+ tree
	 * @param order the maximum number of keys in the nodes of the tree
	 */
	public BPTree(String column) 
	{
		FileReader reader;
        try {
            reader = new FileReader("config/DBApp.properties");
            Properties p=new Properties();
            p.load(reader);
            order = Integer.parseInt(p.getProperty("NodeSize"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.indexColumn = column;
        root = new BPTreeLeafNode<T>(this.order,this.indexColumn);
        root.setRoot(true);
	}
	
	/**
	 * Inserts the specified key associated with the given record in the B+ tree
	 * @param key the key to be inserted
	 * @param recordReference the reference of the record associated with the key
	 */
	public void insert(T key, TupleRef recordReference)
	{
		PushUp<T> pushUp = root.insert(key, recordReference, null, -1);
		if(pushUp != null)
		{
			BPTreeInnerNode<T> newRoot = new BPTreeInnerNode<T>(order,this.indexColumn);
			newRoot.insertLeftAt(0, pushUp.key, root);
			newRoot.setChild(1, pushUp.newNode);
			root.setRoot(false);
			root = newRoot;
			root.setRoot(true);
		}
	}
	
	
	/**
	 * Looks up for the record that is associated with the specified key
	 * @param key the key to find its record
	 * @return the reference of the record associated with this key 
	 */
	public Ref search(T key)
	{
		return root.search(key);
	}
	
	public LinkedList<Tuple> smallerthanequalSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators){
		return root.smallerequalSearch(indexedTerm, otherSQLTerms, strarrOperators);
	}
	
	public LinkedList<Tuple> greaterthanequalSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators){
		return root.greaterequalSearch(indexedTerm, otherSQLTerms, strarrOperators);
	}
	
	public LinkedList<Tuple> equalSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators){
		return root.equalSearch(indexedTerm, otherSQLTerms, strarrOperators);
	}
	
	public LinkedList<Tuple> notEqualSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators){
		return root.notEqualSearch(indexedTerm, otherSQLTerms, strarrOperators);
	}
	
	public TupleRef insertLocation(T tuple) {
		return root.insertLocation(tuple);
	}
	
	public LinkedList<TupleRef> searchToDelete(Hashtable<String, Object> deleteColumnsValue) {
		return root.searchToDelete(deleteColumnsValue);
	}
	
	public void updateTupleRef(T tuple,TupleRef tupleRef) {
		root.updateTupleRef(tuple, tupleRef);
	}
	
	/**
	 * Delete a key and its associated record from the tree.
	 * @param key the key to be deleted
	 * @return a boolean to indicate whether the key is successfully deleted or it was not in the tree
	 */
	public boolean delete(T key)
	{
		boolean done = root.delete(key, null, -1);
		//go down and find the new root in case the old root is deleted
		while(root instanceof BPTreeInnerNode && !root.isRoot())
			root = ((BPTreeInnerNode<T>) root).getFirstChild();
		return done;
	}
	
	/**
	 * Returns a string representation of the B+ tree.
	 */
	public String toString()
	{	
		
		//	<For Testing>
		// node :  (id)[k1|k2|k3|k4]{P1,P2,P3,}
		String s = "";
		Queue<BPTreeNode<T>> cur = new LinkedList<BPTreeNode<T>>(), next;
		cur.add(root);
		while(!cur.isEmpty())
		{
			next = new LinkedList<BPTreeNode<T>>();
			while(!cur.isEmpty())
			{
				BPTreeNode<T> curNode = cur.remove();
				System.out.print(curNode);
				if(curNode instanceof BPTreeLeafNode)
					System.out.print("->");
				else
				{
					System.out.print("{");
					BPTreeInnerNode<T> parent = (BPTreeInnerNode<T>) curNode;
					for(int i = 0; i <= parent.numberOfKeys; ++i)
					{
						System.out.print(parent.getChild(i).index+",");
						next.add(parent.getChild(i));
					}
					System.out.print("} ");
				}
				
			}
			System.out.println();
			cur = next;
		}	
		//	</For Testing>
		return s;
	}
	
	public void printLeafNodes() {
		root.printLeafNodes();
	}
	
}