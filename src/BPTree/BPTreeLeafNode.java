package BPTree;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import Apes.Functions;
import Apes.Page;
import Apes.SQLTerm;
import Apes.Tuple;

public class BPTreeLeafNode<T extends Comparable<T>> extends BPTreeNode<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Ref[] records;
	private BPTreeLeafNode<T> next;

	@SuppressWarnings("unchecked")
	public BPTreeLeafNode(int n , String indexColumn) 
	{
		super(n,indexColumn);
		keys = new Comparable[n];
		records = new Ref[n];

	}

	/**
	 * @return the next leaf node
	 */
	public BPTreeLeafNode<T> getNext()
	{
		return this.next;
	}

	/**
	 * sets the next leaf node
	 * @param node the next leaf node
	 */
	public void setNext(BPTreeLeafNode<T> node)
	{
		this.next = node;
	}

	/**
	 * @param index the index to find its record
	 * @return the reference of the queried index
	 */
	public Ref getRecord(int index) 
	{
		return records[index];
	}

	/**
	 * sets the record at the given index with the passed reference
	 * @param index the index to set the value at
	 * @param recordReference the reference to the record
	 */
	public void setRecord(int index, Ref recordReference) 
	{
		records[index] = recordReference;
	}

	/**
	 * @return the reference of the last record
	 */
	public Ref getFirstRecord()
	{
		return records[0];
	}

	/**
	 * @return the reference of the last record
	 */
	public Ref getLastRecord()
	{
		return records[numberOfKeys-1];
	}

	/**
	 * finds the minimum number of keys the current node must hold
	 */
	public int minKeys()
	{
		if(this.isRoot())
			return 1;
		return (order + 1) / 2;
	}

	/**
	 * insert the specified key associated with a given record refernce in the B+ tree
	 */
	public PushUp<T> insert(T tuple,TupleRef recordReference, BPTreeInnerNode<T> parent, int ptr)	{
		
		if(this.isFull() && !contains(((Tuple)tuple).get(getIndexColumn())))
		{	
			BPTreeNode<T> newNode = this.split(tuple, recordReference);
			Comparable<T> newKey = newNode.getFirstKey();
			return new PushUp<T>(newNode, newKey);
		}
		else
		{
			int index = 0;
			while (index < numberOfKeys && Functions.compareObjects(getKey(index), ((Tuple)tuple).get(getIndexColumn())) < 0) 
				++index;

			int compare = Functions.compareObjects(getKey(index), ((Tuple)tuple).get(getIndexColumn()));
			if (compare == 0 && records[index] != null) {
				records[index].insert(recordReference);
				return null;
			}

			this.insertAt(index, tuple, recordReference);
			return null;

		}
	}

	/**
	 * inserts the passed key associated with its record reference in the specified index
	 * @param index the index at which the key will be inserted
	 * @param key the key to be inserted
	 * @param recordReference the pointer to the record associated with the key
	 */
	private void insertAt(int index, Comparable<T> key, TupleRef recordReference) 
	{
		for (int i = numberOfKeys - 1; i >= index; --i) 
		{
			this.setKey(i + 1, getKey(i));
			this.setRecord(i + 1, getRecord(i));
		}

		if (key instanceof Tuple)
			this.setKey(index, ((Tuple)key).get(getIndexColumn()));
		else
			this.setKey(index, key);
		
		
		Ref ref = new Ref(((Tuple)key).get(getIndexColumn()));
		this.setRecord(index, ref);
		ref.insert(recordReference);
		++numberOfKeys;
	}

	private void insertAt(int index, Comparable<T> key, Ref recordReference) 
	{
		for (int i = numberOfKeys - 1; i >= index; --i) 
		{
			this.setKey(i + 1, getKey(i));
			this.setRecord(i + 1, getRecord(i));
		}

		if (key instanceof Tuple)
			this.setKey(index, ((Tuple)key).get(getIndexColumn()));
		else
			this.setKey(index, key);
		this.setRecord(index, recordReference);
		++numberOfKeys;
	}

	/**
	 * splits the current node
	 * @param key the new key that caused the split
	 * @param recordReference the reference of the new key
	 * @return the new node that results from the split
	 */
	public BPTreeNode<T> split(T key, TupleRef recordReference) 
	{
		int keyIndex = this.findIndex(key);
		int midIndex = numberOfKeys / 2;
		if((numberOfKeys & 1) == 1 && keyIndex > midIndex)	//split nodes evenly
			++midIndex;		


		int totalKeys = numberOfKeys + 1;
		//move keys to a new node
		BPTreeLeafNode<T> newNode = new BPTreeLeafNode<T>(order,this.getIndexColumn());
		for (int i = midIndex; i < totalKeys - 1; ++i) 
		{
			newNode.insertAt(i - midIndex, this.getKey(i), this.getRecord(i));
			numberOfKeys--;
		}

		//insert the new key
		if(keyIndex < totalKeys / 2)
			this.insertAt(keyIndex, key, recordReference);
		else
			newNode.insertAt(keyIndex - midIndex, key, recordReference);

		//set next pointers
		newNode.setNext(this.getNext());
		this.setNext(newNode);

		return newNode;
	}

	/**
	 * finds the index at which the passed key must be located 
	 * @param key the key to be checked for its location
	 * @return the expected index of the key
	 */
	public int findIndex(T key) 
	{
		for (int i = 0; i < numberOfKeys; ++i) 
		{
			int cmp = Functions.compareObjects(getKey(i), ((Tuple)key).get(getIndexColumn()));
			if (cmp > 0) {
				return i;
			}
		}
		return numberOfKeys;
	}

	/**
	 * returns the record reference with the passed key and null if does not exist
	 */
	@Override
	public Ref search(T key) 
	{
		for(int i = 0; i < numberOfKeys; ++i)
			if(Functions.compareObjects(getKey(i), ((Tuple)key).get(getIndexColumn())) == 0)
				return this.getRecord(i);
		return null;
	}

	/**
	 * delete the passed key from the B+ tree
	 */
	public boolean delete(T key, BPTreeInnerNode<T> parent, int ptr) 
	{
		for(int i = 0; i < numberOfKeys; ++i)
			if(Functions.compareObjects(keys[i], ((Tuple)key).get(getIndexColumn())) == 0)
			{
				this.deleteAt(i,key);
				if(i == 0 && ptr > 0)
				{
					//update key at parent
					parent.setKey(ptr - 1, this.getFirstKey());
				}
				//check that node has enough keys
				if(!this.isRoot() && numberOfKeys < this.minKeys())
				{
					//1.try to borrow
					if(borrow(parent, ptr))
						return true;
					//2.merge
					merge(parent, ptr);
				}
				return true;
			}
		return false;
	}

	/**
	 * delete a key at the specified index of the node
	 * @param index the index of the key to be deleted
	 */

	public void deleteAt(int index)
	{
		for(int i = index; i < numberOfKeys - 1; ++i)
		{
			keys[i] = keys[i+1];
			records[i] = records[i+1];
		}
		numberOfKeys--;
	}

	public void deleteAt(int index , T deletedTuple)
	{
		Ref ref = records[index];
		for (int i = 0 ; i< ref.size() ; i++) {
			TupleRef tupleRef = ref.get(i);
			Page page = (Page) Functions.deserialize("data/" + tupleRef.getPage() +".class");
			Tuple tuple = page.get(tupleRef.getIndexInPage());
			if(tuple.equals(deletedTuple)) {
				ref.remove(tupleRef);
			}

		}
		if (ref.isEmpty()) {
			for(int i = index; i < numberOfKeys - 1; ++i)
			{
				keys[i] = keys[i+1];
				records[i] = records[i+1];
			}
			numberOfKeys--;
		}
		
//		else {
//			if (deletedTuple.equals(keys[index])) {
//				TupleRef tupleRef = ref.getFirst();
//				Page page = (Page) Functions.deserialize("data/" + tupleRef.getPage() + ".class");
//				Tuple tuple = page.get(tupleRef.getIndexInPage());
//				keys[index] = (T) tuple;
//			}
//		}
	}

	/**
	 * tries to borrow a key from the left or right sibling
	 * @param parent the parent of the current node
	 * @param ptr the index of the parent pointer that points to this node 
	 * @return true if borrow is done successfully and false otherwise
	 */
	public boolean borrow(BPTreeInnerNode<T> parent, int ptr)
	{
		//check left sibling
		if(ptr > 0)
		{
			BPTreeLeafNode<T> leftSibling = (BPTreeLeafNode<T>) parent.getChild(ptr-1);
			if(leftSibling.numberOfKeys > leftSibling.minKeys())
			{
				this.insertAt(0, leftSibling.getLastKey(), leftSibling.getLastRecord());		
				leftSibling.deleteAt(leftSibling.numberOfKeys - 1);
				parent.setKey(ptr - 1, keys[0]);
				return true;
			}
		}

		//check right sibling
		if(ptr < parent.numberOfKeys)
		{
			BPTreeLeafNode<T> rightSibling = (BPTreeLeafNode<T>) parent.getChild(ptr+1);
			if(rightSibling.numberOfKeys > rightSibling.minKeys())
			{
				this.insertAt(numberOfKeys, rightSibling.getFirstKey(), rightSibling.getFirstRecord());
				rightSibling.deleteAt(0);
				parent.setKey(ptr, rightSibling.getFirstKey());
				return true;
			}
		}
		return false;
	}

	/**
	 * merges the current node with its left or right sibling
	 * @param parent the parent of the current node
	 * @param ptr the index of the parent pointer that points to this node 
	 */
	public void merge(BPTreeInnerNode<T> parent, int ptr)
	{
		if(ptr > 0)
		{
			//merge with left
			BPTreeLeafNode<T> leftSibling = (BPTreeLeafNode<T>) parent.getChild(ptr-1);
			leftSibling.merge(this);
			parent.deleteAt(ptr-1);			
		}
		else
		{
			//merge with right
			BPTreeLeafNode<T> rightSibling = (BPTreeLeafNode<T>) parent.getChild(ptr+1);
			this.merge(rightSibling);
			parent.deleteAt(ptr);
		}
	}

	/**
	 * merge the current node with the specified node. The foreign node will be deleted
	 * @param foreignNode the node to be merged with the current node
	 */
	public void merge(BPTreeLeafNode<T> foreignNode)
	{
		for(int i = 0; i < foreignNode.numberOfKeys; ++i)
			this.insertAt(numberOfKeys, foreignNode.getKey(i), foreignNode.getRecord(i));

		this.setNext(foreignNode.getNext());
	}

	@Override
	public LinkedList<Tuple> smallerequalSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators) {
		LinkedList<Tuple> tuples = new LinkedList<>();
		for (Ref ref : records) {
			if (ref!=null && ((indexedTerm.get_strOperator().equals("<") && Functions.compareObjects(ref.getValue(), indexedTerm.get_objValue()) < 0) ||
					(indexedTerm.get_strOperator().equals("<=") && Functions.compareObjects(ref.getValue(),indexedTerm.get_objValue()) <= 0))) 
				for (TupleRef tupleref : ref) {
					Page page = (Page) Functions.deserialize("data/" + tupleref.getPage() + ".class");
					Tuple tuple = page.get(tupleref.getIndexInPage());
					if (Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
						tuples.add(tuple);
				}
			else
				return tuples;
		}
		if(next !=null)
			tuples.addAll(next.smallerequalSearch(indexedTerm, otherSQLTerms, strarrOperators));
		return tuples;
	}


	@Override
	public LinkedList<Tuple> greaterequalSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators) {
		LinkedList<Tuple> tuples = new LinkedList<>();
		for (Ref ref : records) {
			if (ref!=null && ((indexedTerm.get_strOperator().equals(">") && Functions.compareObjects(ref.getValue(), indexedTerm.get_objValue()) > 0) ||
					(indexedTerm.get_strOperator().equals(">=") && Functions.compareObjects(ref.getValue(),indexedTerm.get_objValue()) >= 0)))
				for (TupleRef tupleref : ref) {
					Page page = (Page) Functions.deserialize("data/" + tupleref.getPage() + ".class");
					Tuple tuple = page.get(tupleref.getIndexInPage());
					if (Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
						tuples.add(tuple);
				}
		}
		if(next !=null)
			tuples.addAll(next.greaterequalSearch(indexedTerm, otherSQLTerms, strarrOperators));
		return tuples;
	}


	@Override
	public LinkedList<Tuple> equalSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators) {
		LinkedList<Tuple> tuples = new LinkedList<>();
		for (Ref ref : records) {
			if ((indexedTerm.get_strOperator().equals("=") && Functions.compareObjects(ref.getValue(), indexedTerm.get_objValue()) == 0))
				for (TupleRef tupleref : ref) {
					Page page = (Page) Functions.deserialize("data/" + tupleref.getPage() + ".class");
					Tuple tuple = page.get(tupleref.getIndexInPage());
					if (Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
						tuples.add(tuple);
				}
		}
		return tuples;
	}


	@Override
	public LinkedList<Tuple> notEqualSearch(SQLTerm indexedTerm, SQLTerm[] otherSQLTerms, String[] strarrOperators) {
		LinkedList<Tuple> tuples = new LinkedList<>();
		for (Ref ref : records) {
			if ( ref !=null && (indexedTerm.get_strOperator().equals("!=") && Functions.compareObjects(ref.getValue(), indexedTerm.get_objValue()) != 0)) 
				for (TupleRef tupleref : ref) {
					Page page = (Page) Functions.deserialize("data/" + tupleref.getPage() + ".class");
					Tuple tuple = page.get(tupleref.getIndexInPage());
					if (Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
						tuples.add(tuple);
				}
		}
		if(next !=null)
			tuples.addAll(next.notEqualSearch(indexedTerm, otherSQLTerms, strarrOperators));
		return tuples;
	}

	@Override
	public TupleRef insertLocation(T tuple) {
		TupleRef tupleRef = null;
		this.printLeafNodes();
		for(int i = 0; i < numberOfKeys; i++) {
			if(Functions.compareObjects(((Tuple)tuple).get(getIndexColumn()), records[i].getValue()) < 0) {
				Ref ref = getRecord(i);
				tupleRef = ref.getFirst();
				return tupleRef;
			}
		}
		return tupleRef;
	}

	@Override
	public LinkedList<TupleRef> searchToDelete(Hashtable<String, Object> deleteColumnValues) {

		LinkedList<TupleRef> tuplesRefs = new LinkedList<TupleRef>();
		for (int i = 0 ; i < records.length ; i++) {
			Ref ref = records[i];
			if (ref == null)
				return tuplesRefs;
			if (Functions.compareObjects(ref.getValue(), deleteColumnValues.get(getIndexColumn())) == 0) {
				for (TupleRef tupleref : ref) {
					Page page = (Page) Functions.deserialize("data/" + tupleref.getPage() + ".class");
					Tuple tuple = page.get(tupleref.getIndexInPage());
					if (tuple.compareToDelete(deleteColumnValues))
						tuplesRefs.add(tupleref);
				}
				break;
			}		
		}
		return tuplesRefs;
	}

	@Override
	public void updateTupleRef(T tuple, TupleRef tupleRef) {
		for (int i = 0 ; i < records.length ; i++) {
			Ref ref = records[i];
			if (ref == null)
				return;
			if (Functions.compareObjects(ref.getValue(), ((Tuple)tuple).get(getIndexColumn())) == 0) 
				for (TupleRef tupleref : ref) {
					Page page = (Page) Functions.deserialize("data/" + tupleref.getPage() + ".class");
					int tupleIndex =  tupleref.getIndexInPage();
					if (tupleIndex < 0 || tupleIndex == page.size())
						continue;
					Tuple toUpdatetuple = page.get(tupleIndex);
					if (tuple.equals(toUpdatetuple)) {
						tupleref.setTupleRef(tupleRef);
						break;
					}
				}		
		}
	}
	
	@Override
	public void printLeafNodes() {
		for (int i = 0 ; i < numberOfKeys ; i++) {
			Ref ref = records[i];
			System.out.println(keys[i] + " : " + ref);
		}
		if (next != null)
			next.printLeafNodes();
	}
}












