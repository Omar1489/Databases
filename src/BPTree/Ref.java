package BPTree;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Properties;

public class Ref extends LinkedList<TupleRef> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object value;
	private int maxCapacity;

	public Ref(Object value) {
		this.value = value;
		maxCapacity = 0;
		FileReader reader;
		try {
			reader = new FileReader("config/DBApp.properties");
			Properties p=new Properties();
			p.load(reader);
			maxCapacity = Integer.parseInt(p.getProperty("NodeSize"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isFull() {
		return (maxCapacity==this.size());
	}

	public boolean isEmpty() {
		return (this.size()==0);
	}

	public Object getValue() {
		return value;
	}

	public void insert(TupleRef tupleRef) {
			this.add(tupleRef);
	}
	
	@Override 
	public String toString() {
		String s ="";
		for (TupleRef tupleRef : this)
			s+= tupleRef + " ";
		return s;
	}

}