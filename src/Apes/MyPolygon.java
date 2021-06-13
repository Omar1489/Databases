package Apes;
import java.awt.Dimension;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;

public class MyPolygon implements Comparable<MyPolygon>{
	
	Polygon polygon;
	
	public MyPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	
	public MyPolygon(String input) {
		polygon = new Polygon();
		ArrayList<Integer> a  = new ArrayList<>();
		String num = "";
		for (int i =0 ; i< input.length() ; i++) {
			Character c = input.charAt(i);
			String s = c.toString();
			if (!Character.isDigit(c) && !num.isEmpty()) {
				a.add(Integer.parseInt(num));
				num = "";
			}
			else if (!Character.isDigit(c) && num.isEmpty()){
				continue;
			}
			else
				num +=s;
		}
		int l = a.size()/2;
		int [] x = new int [l];
		int [] y = new int [l];
		
		for (int i = 0 ; i<a.size() ; i++) {
			int index = i /2;
			if (i%2 == 0)
				x[index] = a.get(i);
			else
				y[index] = a.get(i);
		}
		for (int i = 0 ; i < l ; i++) {
			polygon.addPoint(x[i], y[i]);
		}
	}
	
	@Override
	public int compareTo(MyPolygon o) {
		Polygon polygon1 = o.getPolygon();
		Dimension d = polygon.getBounds().getSize();
		Dimension d1 = polygon1.getBounds().getSize();
		Double area = d.getHeight() * d.getWidth();
		Double area1 = d1.getHeight() * d1.getWidth();
		if (area < area1)
			return -1;
		else if (area > area1)
			return 1;
		return 0;
	}
	
	public boolean isEqual(MyPolygon myPolygon) {
		return (Arrays.equals(this.polygon.xpoints, myPolygon.polygon.xpoints) && Arrays.equals(this.polygon.ypoints, myPolygon.polygon.ypoints));
	}
	
	@Override
	public String toString() {
		Dimension d = polygon.getBounds().getSize();
		System.out.println(d.getHeight() * d.getWidth());
		return ""+ d.getHeight() * d.getWidth();
	}

	public Polygon getPolygon() {
		return polygon;
	}
	
	
}
