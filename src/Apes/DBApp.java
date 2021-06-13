package Apes;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;

public class DBApp {

	private static final String FILE_HEADER = "Table Name,Column Name,Column Type,ClusteringKey,Indexed";

	public void init( ) {
		Properties p=new Properties();  
		p.setProperty("MaximumRowsCountinPage","5");  
		p.setProperty("NodeSize","15");  

		try {
			p.store(new FileWriter("config/DBApp.properties"),"DataBase Properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createTable(String strTableName, String strClusteringKeyColumn, Hashtable<String,String> htblColNameType )	
			throws DBAppException {

		File file = new File("data/metadata.csv");

		//checks if the metadata exists. If it does not exist, then create a new metadata csv file.
		if(!file.exists())
			createCSV(file);

		//throws an exception if table already exists.
		if(tableExists(strTableName)) {
			throw new DBAppException("Table already exists");
		}

		//creates a new table and adds it to the metadata.
		Table table = new Table(strTableName, strClusteringKeyColumn);
		addToCSV(table , strClusteringKeyColumn, htblColNameType);

		//serialize the table.
		try {
			FileOutputStream fileOut = new FileOutputStream("data/" + strTableName + ".class");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(table);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	//creates the Metadata with a file_Header --> case : when metadata does not exist.
	private void createCSV(File file) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file,true);	          
			fileWriter.append(FILE_HEADER.toString());
			fileWriter.append("\n");
		}catch (Exception e) {
			System.out.println("Error in CsvFileWriter");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter");
				e.printStackTrace();
			}
		}
	}

	//Adds a Table to the metadata.CSV.
	private void addToCSV(Table table , String strClusteringKey,Hashtable<String,String> htHashtable) {
		String name = table.getName();
		Enumeration<String> columns = htHashtable.keys();
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter("data/metadata.csv",true);
			while (columns.hasMoreElements()) {
				fileWriter.append(name);
				fileWriter.append(",");
				String columnName = (String) columns.nextElement();
				fileWriter.append(columnName);
				fileWriter.append(",");
				fileWriter.append(htHashtable.get(columnName));
				fileWriter.append(",");

				if(columnName.equals(strClusteringKey))
					fileWriter.append("True");
				else
					fileWriter.append("False");

				fileWriter.append(",");
				fileWriter.append("False");
				fileWriter.append("\n");	
			}

		}catch (Exception e) {
			System.out.println("Error in CsvFileWriter");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter");
				e.printStackTrace();
			}
		}
	}

	//Checks if the table already exists in the Metadata.
	private boolean tableExists(String tableName) {
		BufferedReader fileReader = null;
		Boolean out = false; 
		try {
			String line = "";
			fileReader = new BufferedReader(new FileReader("data/metadata.csv"));
			fileReader.readLine();
			while ((line = fileReader.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length > 0 && tokens[0].equals(tableName)) {
					out = true;
				}
			}
		} 
		catch (Exception e) {
			System.out.println("Error in CsvFileReader!");
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				System.out.println("Error while closing fileReader");
				e.printStackTrace();
			}
		}
		return out;
	}
	
	public void createBTreeIndex(String strTableName,String strColName) throws DBAppException{
		Table table = (Table) Functions.deserialize("data/" + strTableName + ".class");
		table.creatingBPTree(strColName);
		updateCSV(strTableName, strColName);
	}
	
	private void updateCSV(String tableName, String columnName) {
		File currentFile = new File("data/metadata.csv");
		File newFile = new File("data/test.csv");
		Scanner x = null;
		try {
			FileWriter fw = new FileWriter(newFile,true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			x = new Scanner(currentFile);
			x.useDelimiter("[,\n]");
			while (x.hasNext()) {
				 String tablename = x.next();
				 String columnname = x.next();
				 String columnType = x.next();
				 String columnClustered = x.next();
				 String columnIndexed = x.next();
				 if (tableName.equals(tablename) && columnName.equals(columnname)) {
					pw.println(tablename + "," + columnname + "," + columnType + "," + columnClustered + "," + "TRUE"); 
				 }
				 else {
					 pw.println(tablename + "," + columnname + "," + columnType + "," + columnClustered + "," + columnIndexed);
				 }
			}
			x.close();
			pw.flush();
			pw.close();
			currentFile.delete();
			newFile.renameTo(currentFile);
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void createRTreeIndex(String strTableName,String strColName) throws DBAppException{
		Table table = (Table) Functions.deserialize("data/" + strTableName + ".class");
		table.creatingRTree(strColName);
		updateCSV(strTableName, strColName);
	}
	

	public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException{
		Table table = (Table) Functions.deserialize("data/" +strTableName + ".class");
		table.insert(htblColNameValue); 
	}

	public void updateTable(String strTableName, String strClusteringKey, Hashtable<String,Object> htblColNameValue )
			throws DBAppException{
		Table table = (Table) Functions.deserialize("data/" +strTableName + ".class");
		table.update(htblColNameValue,strClusteringKey);
	}

	public void deleteFromTable(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException {
		Table table = (Table) Functions.deserialize("data/" +strTableName + ".class");
		table.delete(htblColNameValue);
	}

	public void printTable(String tableName) {
		Table table = (Table) Functions.deserialize("data/" + tableName + ".class");
		table.toString();
	}

	public Iterator<Tuple> selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException {

		if(arrSQLTerms.length != strarrOperators.length +1)
			throw new DBAppException("please enter valid selection Terms");

		Table table = (Table) Functions.deserialize("data/" + arrSQLTerms[0]._strTableName + ".class");

		if (toDoAllLinear(table, arrSQLTerms, strarrOperators) == true) {
			return (table.LinearSearch(arrSQLTerms, strarrOperators)).iterator();
		
		}

		int andIndex = groupedAndIndex(table, arrSQLTerms, strarrOperators);
		if(andIndex != -1) {
			return groupedIndexedBSSelection(table, arrSQLTerms, strarrOperators, andIndex).iterator();
		}
		LinkedList<Tuple> selections = oneByOneSelection(table, arrSQLTerms, strarrOperators);
		return selections.iterator();
	}

	private LinkedList<Tuple> oneByOneSelection(Table table,SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException{
		LinkedList<LinkedList<Tuple>> selections = new LinkedList<>();

		for (SQLTerm sqlterm : arrSQLTerms) {
			LinkedList<Tuple> tuples = table.singleSelect(sqlterm._strColumnName, sqlterm._strOperator, sqlterm._objValue); 
			selections.add(tuples);
		}

		for (String operation : strarrOperators) {
			if(operation.equals("AND"))
				selections.addFirst(sortedIntersectOperation(selections.removeFirst(), selections.removeFirst()));
			else if (operation.equals("OR"))
				selections.addFirst(sortedUnionOperation(selections.removeFirst(), selections.removeFirst()));
			else
				selections.addFirst(sortedXorOperation(selections.removeFirst(), selections.removeFirst()));
		}
		return selections.getFirst();
	}

	private LinkedList<Tuple> groupedIndexedBSSelection(Table table,SQLTerm[] arrSQLTerms, String[] strarrOperators, int andIndex) throws DBAppException{
		LinkedList<Tuple> selections = new LinkedList<>();
		int lastAND = andIndex;
		for (int i = lastAND ; i < strarrOperators.length ; i++) {
			if (!strarrOperators[i].equals("AND"))
				lastAND = i -1;
		}
		String[] groupedOperators = new String[lastAND];
		SQLTerm[] groupedSQLTerms = new SQLTerm[lastAND+1];

		for (int i = 0; i<= lastAND; i++) {
			if (i == andIndex)
				continue;
			groupedOperators[i] = strarrOperators[i];
		}

		for (int i = 0; i<=lastAND + 1;i++) {
			if (i == (andIndex+1))
				continue;
			groupedSQLTerms[i] = arrSQLTerms[i];
		}

		LinkedList<Tuple> groupedSelection = new LinkedList<>();

		if (table.hasAnIndex(arrSQLTerms[andIndex+1]._strColumnName)) {
			// TODO : multiple And search based on an indexed column.
			groupedSelection = table.indexedMultipleSelection(arrSQLTerms[andIndex+1], groupedSQLTerms, groupedOperators);
		}
		else
			groupedSelection = table.multipleSelectionBS(arrSQLTerms[andIndex+1], groupedSQLTerms, groupedOperators);

		if (lastAND == strarrOperators.length-1)
			return groupedSelection;

		String[] oneByOneOperators = Arrays.copyOfRange(strarrOperators, (lastAND +2), strarrOperators.length);
		SQLTerm[] oneByOneSQLTerms = Arrays.copyOfRange(arrSQLTerms, lastAND + 2, arrSQLTerms.length);

		String intermediateOperator = strarrOperators[lastAND+1];

		LinkedList<Tuple> oneByOneSelection = oneByOneSelection(table, oneByOneSQLTerms, oneByOneOperators);

		if (intermediateOperator.equals("AND"))
			selections = sortedIntersectOperation(groupedSelection, oneByOneSelection);
		else if (intermediateOperator.equals("OR"))
			selections = sortedUnionOperation(groupedSelection, oneByOneSelection);
		else
			selections = sortedXorOperation(groupedSelection, oneByOneSelection);

		return selections;
	}

	public boolean toDoAllLinear(Table table ,SQLTerm[] arrSQLTerms, String[] strarrOperators) {
		if (strarrOperators.length == 0)
			return false;
		String clusteringKey = table.getStrClusteringKeyColumn();
		boolean out = true;
		for (int i = 0 ; i< strarrOperators.length ; i++) {
			String selectColumn = arrSQLTerms[i+1]._strColumnName;
			if (i==0) {
				if (strarrOperators[i].equals("AND")) {
					if (!selectColumn.equals(clusteringKey) && !table.hasAnIndex(selectColumn) 
							&&!arrSQLTerms[i]._strColumnName.equals(clusteringKey) && !table.hasAnIndex(arrSQLTerms[i]._strColumnName))
						out = true;
					else
						out = false;
				}
				else {
					if ((!selectColumn.equals(clusteringKey) && !table.hasAnIndex(selectColumn)) 
							|| (!arrSQLTerms[i]._strColumnName.equals(clusteringKey) && !table.hasAnIndex(arrSQLTerms[i]._strColumnName)))
						out = true;
					else 
						out = false;
				}
			}
			else {
				if (strarrOperators[i].equals("AND")) {
					if (!selectColumn.equals(clusteringKey) && !table.hasAnIndex(selectColumn))
						out = out & true;
					else
						out = out & false;
				}
				else {
					if (!selectColumn.equals(clusteringKey) && !table.hasAnIndex(selectColumn))
						out = out || true;
					else
						out = out || false;
				}
			}
		}
		return out;
	}

	private LinkedList<Tuple> sortedIntersectOperation(LinkedList<Tuple> ArrayOne , LinkedList<Tuple> ArrayTwo){

		LinkedList<Tuple> tuples = new  LinkedList<>();
		while(!ArrayOne.isEmpty() && !ArrayTwo.isEmpty()) {

			if (ArrayOne.getFirst().compareTo(ArrayTwo.getFirst()) < 0 && ArrayTwo.contains(ArrayOne.getFirst())) {
				tuples.add(ArrayOne.getFirst());
				ArrayOne.removeFirst();
			}

			else if (ArrayOne.getFirst().compareTo(ArrayTwo.getFirst()) > 0 && ArrayOne.contains(ArrayTwo.getFirst())) {
				tuples.add(ArrayTwo.getFirst());
				ArrayTwo.removeFirst();
			}

			else {
				if (ArrayOne.contains(ArrayTwo.getFirst()))
					tuples.add(ArrayTwo.getFirst());
				ArrayOne.removeFirst();
				if (ArrayTwo.contains(ArrayOne.getFirst()))
					tuples.add(ArrayOne.getFirst());
				ArrayTwo.removeFirst();
			}
		}
		return tuples;
	}

	private LinkedList<Tuple> sortedUnionOperation(LinkedList<Tuple> ArrayOne , LinkedList<Tuple> ArrayTwo){
		LinkedList<Tuple> tuples = new LinkedList<>();
		while(!ArrayOne.isEmpty() || !ArrayTwo.isEmpty()) {
			if(ArrayOne.isEmpty()) {
				tuples.addAll(ArrayTwo);
				break;
			}

			else if(ArrayTwo.isEmpty()) {
				tuples.addAll(ArrayOne);
				break;
			}

			else {
				if (ArrayOne.getFirst().compareTo(ArrayTwo.getFirst()) < 0 && !ArrayTwo.contains(ArrayOne.getFirst())) {
					tuples.add(ArrayOne.getFirst());
					ArrayOne.removeFirst();
				}

				else if (ArrayOne.getFirst().compareTo(ArrayTwo.getFirst()) > 0 && !ArrayOne.contains(ArrayTwo.getFirst())) {
					tuples.add(ArrayTwo.getFirst());
					ArrayTwo.removeFirst();
				}

				else {
					if (!ArrayTwo.contains(ArrayOne.getFirst()))
						tuples.add(ArrayOne.getFirst());
					ArrayOne.removeFirst();
					if (!ArrayOne.contains(ArrayTwo.getFirst()))
						tuples.add(ArrayTwo.getFirst());
					ArrayTwo.removeFirst();
				}
			}
		}
		return tuples;
	}

	private  LinkedList<Tuple> sortedXorOperation( LinkedList<Tuple> ArrayOne ,  LinkedList<Tuple> ArrayTwo){
		LinkedList<Tuple> tuples = new  LinkedList<>();
		HashSet<Tuple> commonTuples = new HashSet<>();
		while(!ArrayOne.isEmpty() || !ArrayTwo.isEmpty()) {

			if(ArrayOne.isEmpty()) {
				tuples.addAll(ArrayTwo);
				break;
			}

			else if(ArrayTwo.isEmpty()) {
				tuples.addAll(ArrayOne);
				break;
			}

			else {
				if (ArrayOne.getFirst().compareTo(ArrayTwo.getFirst()) < 0) {  
					if (!ArrayTwo.contains(ArrayOne.getFirst()) && !commonTuples.contains(ArrayOne.getFirst())) {
						tuples.add(ArrayOne.getFirst());
					}

					commonTuples.add(ArrayOne.removeFirst());
				}

				else if (ArrayOne.getFirst().compareTo(ArrayTwo.getFirst()) > 0) {
					if( !ArrayOne.contains(ArrayTwo.getFirst()) && !commonTuples.contains(ArrayTwo.getFirst())) {
						tuples.add(ArrayTwo.getFirst());
					}
					commonTuples.add(ArrayTwo.removeFirst());
				}
				else {
					if (!ArrayOne.contains(ArrayTwo.getFirst()) && !commonTuples.contains(ArrayTwo.getFirst()))
						tuples.add(ArrayTwo.getFirst());
					commonTuples.add(ArrayOne.removeFirst());
					if( !ArrayTwo.contains(ArrayOne.getFirst()) && !commonTuples.contains(ArrayOne.getFirst()))
						tuples.add(ArrayOne.getFirst());
					commonTuples.add(ArrayTwo.removeFirst());
				}
			}
		}
		return tuples;

	}

	private int groupedAndIndex(Table table, SQLTerm[] arrSQLTerms, String[] strarrOperators) {
		int index = -1;
		for (int i = strarrOperators.length -1 ; i >=0 ; i--) {
			if (index != -1 && !strarrOperators[i].equals("AND"))
				break;
			if(strarrOperators[i].equals("AND")) {
				String columnName = arrSQLTerms[i+1]._strColumnName;
				if (table.hasAnIndex(columnName))
					return i;
				else if (columnName.equals(table.getStrClusteringKeyColumn()) && index == -1)
					index = i;
			}
		}
		return index;
	}

}
