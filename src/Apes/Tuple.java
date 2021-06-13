package Apes;
import java.awt.Polygon;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

public class Tuple extends Hashtable<String,Object> implements Comparable<Tuple> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String ClusteringKey;

	public Tuple(String clusteringKey) {
		ClusteringKey = clusteringKey;
	}

	@Override
	public int compareTo(Tuple tuple) {
	// Compares Tuples using their ClusteringKey's Value.
		return Functions.compareObjects(this.get(ClusteringKey), tuple.get(ClusteringKey));
	}
	
	public boolean compareToDelete(Hashtable<String,Object> tuple) {
		//compares if this tuple has identical values to those values in the given tuple.
		Set<String> keys = tuple.keySet();
		for(String key : keys) {
			if(Functions.compareObjects(this.get(key),tuple.get(key)) != 0)
				return false;
			if (this.get(key) instanceof Polygon) {
				MyPolygon toDeletePolygon = new MyPolygon((Polygon) tuple.get(key));
				MyPolygon thisPolygon = new MyPolygon((Polygon) this.get(key));
				if (!thisPolygon.isEqual(toDeletePolygon))
					return false;
			}
		}
		return true;
	}
	
	public void updateTuple(Hashtable<String,Object> htblColNameValue) {
		// updates the values of a Tuple based on Values in the given HashTable
		Date date = new Date();
 		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		htblColNameValue.put("TouchDate",dateFormat.format(date));
		Enumeration<String> e = htblColNameValue.keys();
		while(e.hasMoreElements()) {
			String columnName = (String) e.nextElement();
			Object newValue  = htblColNameValue.get(columnName);
			this.replace(columnName, newValue);
		}
	}
	
	@Override
	public String toString() {
		return this.get(ClusteringKey) +"";
	}
	
}
