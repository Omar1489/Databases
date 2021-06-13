package RTree;


import java.util.Scanner;

import RTree.RTree.Entry;

public class TestRTree {	
	public static void main(String[] args) 
	{		
		RTree<Integer> tree = new RTree<Integer>(null);
		Scanner sc = new Scanner(System.in);
		System.out.println("insert");
		while(true) 
		{
			float i = sc.nextFloat();
			float j = sc.nextFloat();
			float k = sc.nextFloat();
			float l = sc.nextFloat();
			int x = sc.nextInt();
			
			if(x == -1)
				break;
			float[] coords = new float[2];
			float[] dimensions = new float[2];
			coords[0] = i;
			coords[1] = j;
			dimensions[0] = k;
			dimensions[1] = l;
			tree.insert(coords,dimensions,x);
			System.out.println(tree.toString());
		}
		
		System.out.println("update");
		while(true) {
		float i2 = sc.nextFloat();
		float j2 = sc.nextFloat();
		float k2 = sc.nextFloat();
		float l2 = sc.nextFloat();
		int x = sc.nextInt();
		if(x == -1)
			break;
		float[] coords2 = new float[2];
		float[] dimensions2 = new float[2];
		coords2[0] = i2;
		coords2[1] = j2;
		dimensions2[0] = k2;
		dimensions2[1] = l2;
		if(tree.getEntryValue(coords2, dimensions2)==null) {
		System.out.println("null1");
		}
		else {
			int e = tree.getEntryValue(coords2, dimensions2);
			System.out.println(e);
		}
		tree.update(coords2, dimensions2, x);
		if(tree.getEntryValue(coords2, dimensions2)==null) {
		System.out.println("null2");
		}
		else {
			int e = tree.getEntryValue(coords2, dimensions2);
			System.out.println(tree.toString());
		}
	}	
		System.out.println("delete");
		while(true) 
		{
			float i = sc.nextFloat();
			float j = sc.nextFloat();
			float k = sc.nextFloat();
			float l = sc.nextFloat();
			int x = sc.nextInt();
			if(x == -1)
				break;
			float[] coords = new float[2];
			float[] dimensions = new float[2];
			coords[0] = i;
			coords[1] = j;
			dimensions[0] = k;
			dimensions[1] = l;
			tree.delete(coords,dimensions,x);
			System.out.println(tree.toString());
		}
		sc.close();
		
				}
}
