package Apes;

import java.io.Serializable;

public class PageData implements Serializable , Comparable<PageData>{
	
	private String pageName;
	private	Tuple max;
	private Tuple min;
	private boolean isFull;
	
	
	public PageData(String pageName, Tuple min, Tuple max, boolean isFull) {
		this.pageName = pageName;
		this.max = max;
		this.min = min;
		this.isFull = isFull;
	}

	public String getPageName() {
		return pageName;
	}

	public Tuple getMax() {
		return max;
	}

	public void setMax(Tuple max) {
		this.max = max;
	}

	public Tuple getMin() {
		return min;
	}

	public void setMin(Tuple min) {
		this.min = min;
	}

	public boolean isFull() {
		return isFull;
	}

	public void setFull(boolean isFull) {
		this.isFull = isFull;
	}

	@Override
	public int compareTo(PageData pageData) {
		if((this.max).compareTo(pageData.getMin())<=0) {
			return -1;
		}
		return 1;
	}
	
	@Override
	public String toString() {
		return "[" + min + " , " + max + "]";
	}
	
}
