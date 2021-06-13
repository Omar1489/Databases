package Apes;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import BPTree.BPTree;
import BPTree.Ref;
import BPTree.TupleRef;

public class DBAppTest {
	public static void main(String []args) {
		DBApp dbApp = new DBApp( );
		dbApp.init();
		String strTableName = "Student";
//		// Create table
//		Hashtable<String, String> htblColNameType = new Hashtable<String, String>( );
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		try {
//			dbApp.createTable(strTableName, "id", htblColNameType );
//		} catch (DBAppException e1) {
//		// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//	//insert into table
//		String [] names = {"semsem" ,"nana" , "katy" , "jimmy" , "roro" , "saaeed" , "mickey"};
//		double [] gpa = {0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1.00, 1.05, 1.10, 1.20, 1.30, 1,40 ,1.60, 1.80, 2.10, 2.40, 2.50, 2.60, 2.70, 2.80, 2.90,
//				3.0,3.1,3.2,3.3,3.4,3.5,3.6,3.9,4.1,4.2,4.3,4.3,0.71,0.74,1.55};
//		for(int i= 0 ; i< 10 ; i++) {
//			Hashtable<String,Object> input = new Hashtable<String, Object>();
//			Random r = new Random();
//			int id = r.nextInt(10);
//			if (id == 9) {
//				i--;
//				continue;
//			}
//			int name_index = r.nextInt(names.length);
//			int gpa_index = r.nextInt(gpa.length);
//			input.put("id", new Integer(id));
//			input.put("name",new String (names[name_index]));
//			input.put("gpa", new Double(gpa[gpa_index]));
//			try {
//				dbApp.insertIntoTable("Student",input);
//			} catch (DBAppException e) {
//				e.printStackTrace();
//			}
//
//		}
//
//		//prints table
//		dbApp.printTable(strTableName);

		 
		/*	SQLTerm sqlTerm1 = new SQLTerm();
		sqlTerm1._strTableName = strTableName;
		sqlTerm1._strOperator = "=";
		sqlTerm1._objValue = "roro";
		sqlTerm1._strColumnName = "name";
		SQLTerm sqlTerm2 = new SQLTerm();
		sqlTerm2._strTableName = strTableName;
		sqlTerm2._strOperator = "=";
		sqlTerm2._objValue = "mickey";
		sqlTerm2._strColumnName = "name";
		SQLTerm sqlTerm3 = new SQLTerm();
		sqlTerm3._strTableName = strTableName;
		sqlTerm3._strOperator = "=";
		sqlTerm3._objValue = 6;
		sqlTerm3._strColumnName = "id";
		SQLTerm sqlTerm4 = new SQLTerm();
		sqlTerm4._strTableName = strTableName;
		sqlTerm4._strOperator = "=";
		sqlTerm4._objValue = 1;
		sqlTerm4._strColumnName = "id";
		SQLTerm [] arrSQLTerms = new SQLTerm[4];
		arrSQLTerms[0] = sqlTerm1;
		arrSQLTerms[1] = sqlTerm2;
		arrSQLTerms[2] = sqlTerm3;
		arrSQLTerms[3] = sqlTerm4;
		String[]strarrOperators = new String[3];
		strarrOperators[0] = "OR";
		strarrOperators[1] = "AND";
		strarrOperators[2] = "OR";
		try {
			Iterator<Tuple> out = dbApp.selectFromTable(arrSQLTerms,strarrOperators );
			while (out.hasNext())
				System.out.println(out.next());
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		 */		//prints table
	//	dbApp.printTable(strTableName);


		/*try {
			dbApp.createBTreeIndex(strTableName, "gpa");
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
//		
//		try {
//			dbApp.createBTreeIndex(strTableName, "id");
//		} catch (DBAppException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//		BPTree bptree = (BPTree) Functions.deserialize("data/" + strTableName +"id" +".class");
//		System.out.println(bptree);
		
		
	/*	BPTree bptree = (BPTree) Functions.deserialize("data/" + strTableName +"id" +".class");
		//System.out.println(bptree);
		SQLTerm sqlTerm1 = new SQLTerm();
		sqlTerm1._strTableName = strTableName;
		sqlTerm1._strOperator = "=";
		sqlTerm1._objValue = 82;
		sqlTerm1._strColumnName = "id";
		LinkedList<Tuple> tuples = bptree.equalSearch(sqlTerm1, new SQLTerm[0], new String [0]);
		for(Tuple tuple1 : tuples) {
			System.out.println(tuple1);
		}
		Hashtable<String, Object> h = new Hashtable<>();
		h.put("id", 1);
		ArrayList<TupleRef> list = bptree.searchToDelete(h);
		System.out.println(list);*/
		
		Hashtable<String, Object> toDelete = new Hashtable<String, Object>();
//		//toDelete.put("gpa", 0.75);
		toDelete.put("id", 3);
		try {
			dbApp.deleteFromTable(strTableName, toDelete);
			dbApp.printTable(strTableName);
//			SQLTerm sqlTerm1 = new SQLTerm();
//			sqlTerm1._strTableName = strTableName;
//			sqlTerm1._strOperator = "=";
//			sqlTerm1._objValue = 1;
//			sqlTerm1._strColumnName = "id";
//			SQLTerm [] arrSQLTerms = new SQLTerm[1];
//			arrSQLTerms[0] = sqlTerm1;
//			Iterator<Tuple> out = dbApp.selectFromTable(arrSQLTerms,new String [0]);
//			while (out.hasNext())
//				System.out.println(out.next());
//			
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BPTree bpTree =(BPTree) Functions.deserialize("data/" +strTableName + "id" + ".class");
//		TupleRef tupleRef = bpTree.searchToDelete(toDelete);
//		Page page = (Page) Functions.deserialize("data/" + tupleRef.getPage() + ".class");
//		Tuple tuple = page.get(tupleRef.getIndexInPage());
//		page.remove(tuple);
//		Functions.serialize(page, "data/" + tupleRef.getPage() + ".class");
//		bpTree.delete(tuple);
//		Functions.serialize(bpTree, "data/" +strTableName + "id" + ".class");
//		for (int i = 0 ; i< page.size() ; i++) {
//			TupleRef tupleref = new TupleRef(tupleRef.getPage(), i);
//			bpTree.updateTupleRef(page.get(i), tupleref);
//			Functions.serialize(bpTree, "data/" +strTableName + "id" + ".class");
//		}
//		System.out.println(bptree);
//		dbApp.printTable(strTableName);
//		
		
//		BPTree bpTree =(BPTree) Functions.deserialize("data/" +strTableName + "id" + ".class");
//		TupleRef tupleRef = bpTree.searchToDelete(toDelete);
//		while (tupleRef!=null) {
//			Page page = (Page) Functions.deserialize("data/" + tupleRef.getPage() + ".class");
//			Tuple tuple = page.get(tupleRef.getIndexInPage());
//			bpTree.delete(tuple);
//			Functions.serialize(bpTree, "data/" +strTableName + "id" + ".class");
//			tupleRef = bpTree.searchToDelete(toDelete);
//		}
		System.out.println(bpTree);
//		dbApp.printTable(strTableName);
//		
	}
	
	
}
