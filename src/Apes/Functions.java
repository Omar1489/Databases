package Apes;

import java.awt.Polygon;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

public class Functions {

	private Functions() {

	}

	public static int compareObjects(Object o1 , Object o2) {
		//Compares two Objects , Assume that both objects have same type.
		// polygons --> Insert , > , < , >= , <= ,!=
		if(o1 instanceof Integer) {
			if((int)o1 < (int)o2)
				return -1;
			else if((int)o1 > (int)o2)  
				return 1;
		}
		else if(o1 instanceof Double) {
			if((Double)o1 < (Double)o2)
				return -1;
			else if((Double)o1 > (Double)o2)
				return 1;
		}
		else if(o1 instanceof Boolean) {
			if((boolean)o1 == true && (boolean)o2 == false)
				return -1;
			else if((boolean)o1 == false && (boolean)o2 == true)
				return 1;
		}
		else if(o1 instanceof String) {
			return ((String)o1).compareTo((String)o2);
		}
		else if(o1 instanceof Date) {
			return ((Date)o1).compareTo((Date)o2);
		}

		else if(o1 instanceof Polygon) {
			MyPolygon polygonOne = new MyPolygon((Polygon)o1);
			MyPolygon polygonTwo = new MyPolygon((Polygon)o2);
			return (polygonOne.compareTo(polygonTwo));
		}	
		return 0;

	}

	public static void serialize(Object object, String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static Object deserialize(String path) {
		Object o = null;
		try {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			o = in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Table class not found");
			c.printStackTrace();
		}
		return o;
	}

	public static boolean satisfiesTerms(Tuple tuple ,SQLTerm[] arrSQLTerms, String[] strarrOperators) {
		if (arrSQLTerms.length == 0)
			return true;
		if (strarrOperators.length == 0)
			return Functions.satisfiesCondition(tuple, arrSQLTerms[0]);

		LinkedList<Boolean> checkList = new LinkedList<>();
		for (SQLTerm term : arrSQLTerms) {
			checkList.add(satisfiesCondition(tuple, term));
		}
		for (String operator : strarrOperators) {
			boolean firstCheck = checkList.removeFirst();
			boolean secondCheck = checkList.removeFirst();
			if (operator.equals("AND")) 
				checkList.addFirst(firstCheck & secondCheck);

			else if (operator.equals("OR")) 
				checkList.addFirst(firstCheck || secondCheck);

			else
				checkList.addFirst(firstCheck != secondCheck);
		}
		if (checkList.getFirst() == true)
			return true;
		return false;

	}

	public static boolean satisfiesCondition(Tuple tuple , SQLTerm term) {
		if (term._strOperator.equals("=")) {
			if (tuple.get(term._strColumnName) instanceof Polygon) {
				MyPolygon tuplePolygon = new MyPolygon((Polygon) tuple.get(term._strColumnName));
				MyPolygon termPolygon = new MyPolygon((Polygon) term._objValue);
				return tuplePolygon.isEqual(termPolygon);
			}
			return (Functions.compareObjects(tuple.get(term._strColumnName), term._objValue) == 0);
		}
		else if (term._strOperator.equals("!=")) {
			if (tuple.get(term._strColumnName) instanceof Polygon) {
				MyPolygon tuplePolygon = new MyPolygon((Polygon) tuple.get(term._strColumnName));
				MyPolygon termPolygon = new MyPolygon((Polygon) term._objValue);
				return !tuplePolygon.isEqual(termPolygon);
			}
			return (Functions.compareObjects(tuple.get(term._strColumnName), term._objValue) != 0);
		}
		else if (term._strOperator.equals(">"))
			return (Functions.compareObjects(tuple.get(term._strColumnName), term._objValue) > 0);
		else if (term._strOperator.equals(">="))
			return (Functions.compareObjects(tuple.get(term._strColumnName), term._objValue) >= 0);
		else if (term._strOperator.equals("<"))
			return (Functions.compareObjects(tuple.get(term._strColumnName), term._objValue) < 0);
		else
			return (Functions.compareObjects(tuple.get(term._strColumnName), term._objValue) <= 0);
	}

	public static boolean equalPolygon(Polygon polygon1, Polygon polygon2) {
		return (Arrays.equals(polygon1.xpoints, polygon2.xpoints) && Arrays.equals(polygon1.ypoints, polygon2.ypoints));
	}
	
}
