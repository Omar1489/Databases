package Apes;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import BPTree.BPTree;
import BPTree.TupleRef;

public class Main {
	public static void main(String[]args) {

		DBApp dbApp = new DBApp( );
		dbApp.init();
		String strTableName = "Student";
		createtable(dbApp, strTableName);
//		createBPlusTree(dbApp, strTableName, "gpa");
//		createBPlusTree(dbApp, strTableName, "name");
//		createBPlusTree(dbApp, strTableName, "id");
//		updateTable(dbApp, strTableName);
//		deleteFunction(dbApp, strTableName,"gpa");
//		deletefromtree(strTableName);
//		locationofInsertion(dbApp, strTableName);
//		inexedInsert(strTableName, dbApp);
//		print(strTableName);
//		printLeafNodes(strTableName);
		
	}
	
	public static void inexedInsert(String strTableName, DBApp dbApp) {
		Hashtable<String, Object> tuple = new Hashtable<String, Object>();
		tuple.put("id", 4);
		tuple.put("name", "sam");
		tuple.put("gpa", 0.7);
		System.out.println(tuple);
//		System.out.println(Functions.deserialize("data/" + strTableName + ".class"));
		try {
			dbApp.insertIntoTable(strTableName, tuple);
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(Functions.deserialize("data/" + strTableName + ".class"));
//		System.out.println(Functions.deserialize("data/" + strTableName + "id" + ".class"));
	}
	
	public static void locationofInsertion(DBApp dbApp,String strTableName) {
		dbApp.printTable(strTableName);
		BPTree tree = (BPTree)Functions.deserialize("data/" + strTableName + "id" + ".class");
		Tuple tuple = new Tuple("id");
		tuple.put("id", -1);
		System.out.println(tree.insertLocation(tuple));
	}
	
	public static void createtable(DBApp dbApp , String strTableName) {
		// Create table
//		Hashtable<String, String> htblColNameType = new Hashtable<String, String>( );
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		try {
//			dbApp.createTable(strTableName, "id", htblColNameType );
//		} catch (DBAppException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		//insert into table
		String [] names = {"semsem" ,"nana" , "katy" , "jimmy" , "roro" , "saaeed" , "mickey","ronzo","monto"};
		double [] gpa = {0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.00, 1.05, 1.10, 1.20, 1.30, 1,40 ,1.60, 1.80, 2.10, 2.40, 2.50, 2.60, 2.70, 2.80, 2.90,
				3.0,3.1,3.2,3.3,3.4,3.5,3.6,3.9,4.1,4.2,4.3,4.3,0.71,0.74,1.55};
		for(int i= 0 ; i< 10 ; i++) {
			Hashtable<String,Object> input = new Hashtable<String, Object>();
			Random r = new Random();
			int id = r.nextInt(25);
			int name_index = r.nextInt(names.length);
			int gpa_index = r.nextInt(gpa.length);
			input.put("id", new Integer(id));
			input.put("name",new String (names[name_index]));
			input.put("gpa", new Double(gpa[gpa_index]));
			try {
				dbApp.insertIntoTable("Student",input);
			} catch (DBAppException e) {
				e.printStackTrace();
			}

		}

		//prints table
		dbApp.printTable(strTableName);
	}

	@SuppressWarnings("rawtypes")
	public static void createBPlusTree(DBApp dbApp , String strTableName, String columnName) {
		try {
			dbApp.createBTreeIndex(strTableName, columnName);
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BPTree bptree = (BPTree) Functions.deserialize("data/" + strTableName + columnName +".class");
		System.out.println(bptree);
	}

	public static void deleteFunction(DBApp dbApp , String strTableName ,String columnName) {
		Hashtable<String, Object> toDelete = new Hashtable<String, Object>();
		toDelete.put("id", 4);
		try {
			dbApp.deleteFromTable(strTableName, toDelete);
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateTable(DBApp dbApp ,String strTableName) {
		Hashtable<String, Object> updates = new Hashtable<String, Object>();
		updates.put("gpa", 0.7);
		updates.put("name", "sam");
		//updates.put("gpa" , 1.3);
		try {
			dbApp.updateTable(strTableName, "22", updates);
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		dbApp.printTable(strTableName);
//		System.out.println(Functions.deserialize("data/" + strTableName + "gpa" + ".class"));
//		System.out.println(Functions.deserialize("data/" + strTableName + "id" + ".class"));
//		System.out.println(Functions.deserialize("data/" + strTableName + "name" + ".class"));
	}
	
	public static void print(String strTableName) {
		System.out.println(Functions.deserialize("data/" + strTableName + ".class"));
//		System.out.println(Functions.deserialize("data/" + strTableName + "gpa" + ".class"));
		System.out.println( Functions.deserialize("data/" + strTableName + "id" + ".class"));
//		System.out.println(Functions.deserialize("data/" + strTableName + "name" + ".class"));
	}
	
	public static void printLeafNodes(String strTableName) {
		BPTree idBPTree = (BPTree) Functions.deserialize("data/" + strTableName + "id" + ".class");
		idBPTree.printLeafNodes();
	}
}
