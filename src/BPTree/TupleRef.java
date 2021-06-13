package BPTree;

import java.io.Serializable;

public class TupleRef implements Serializable , Comparable<TupleRef>{
	/**
	 * This class represents a pointer to the record. It is used at the leaves of the B+ tree 
	 */
	private static final long serialVersionUID = 1L;
	private int indexInPage;
	private String pageName;
	
	public TupleRef(String pageName, int indexInPage)
	{
		this.pageName = pageName;
		this.indexInPage = indexInPage;
	}
	
	/**
	 * @return the page at which the record is saved on the hard disk
	 */
	public String getPage()
	{
		return pageName;
	}
	
	/**
	 * @return the index at which the record is saved in the page
	 */
	public int getIndexInPage()
	{
		return indexInPage;
	}

	@Override
	public int compareTo(TupleRef tupleRef) {
		if (pageName.compareTo(tupleRef.getPage()) < 0)
			return -1;
		else if (pageName.compareTo(tupleRef.getPage()) > 0)
			return 1;
		else {
			if (indexInPage < tupleRef.getIndexInPage())
				return -1;
			else if (indexInPage > tupleRef.getIndexInPage())
				return 1;
			else 
				return 0;
		}
		
	}
	
	public String toString() {
		return pageName + " " + indexInPage;
	}
	
	
	
	public void setIndexInPage(int indexInPage) {
		this.indexInPage = indexInPage;
	}

	public void setTupleRef(TupleRef tupleRef) {
		this.pageName = tupleRef.getPage();
		this.indexInPage = tupleRef.getIndexInPage();
	}
	
}
