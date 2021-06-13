package Apes;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import BPTree.BPTree;
import BPTree.TupleRef;
import RTree.RTree;

public class Table implements java.io.Serializable {

	/**d
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int pageCounter;
	private String name;
	private String strClusteringKeyColumn;
	private Vector<PageData> pages;
	private ArrayList<String> bPTrees = new ArrayList<String>();
	private ArrayList<String> rTrees = new ArrayList<String>();

	public String getStrClusteringKeyColumn() {
		return strClusteringKeyColumn;
	}

	public ArrayList<String> getbPTrees() {
		return bPTrees;
	}

	public ArrayList<String> getrTrees() {
		return rTrees;
	}

	public Table(String name, String strClusteringKeyColumn) {
		this.pageCounter = 0;
		this.name = name;
		this.strClusteringKeyColumn = strClusteringKeyColumn;
		this.pages = new Vector<PageData>();
		this.bPTrees = new ArrayList<String>();
	}

	public void insert(Hashtable<String, Object> htblColNameValue) throws DBAppException {
		if(!checkValidInsertion(htblColNameValue)) {
			throw new DBAppException("the entered tuple is invalid");
		}

		Tuple tuple = new Tuple(strClusteringKeyColumn);
		Enumeration<String> keys = htblColNameValue.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			tuple.put(key,htblColNameValue.get(key));
		}

		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		tuple.put("TouchDate",dateFormat.format(date));

		if(pages.isEmpty()) {
			createNewPage(tuple);
			Functions.serialize(this,"data/" +name+".class");
			return;
		}		

		else if (hasAnIndex(strClusteringKeyColumn)) {
			String type = ColumnKeyType(getStrClusteringKeyColumn());
			if(type.contains("Polygon"))
				indexedInsertR(tuple);
			else
				indexedInsertBP(tuple);
			Functions.serialize(this,"data/" +name+".class");
		}

		else {
			binaryInsert(tuple);
			Functions.serialize(this,"data/" +name+".class");
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void indexedInsertBP(Tuple tuple) throws DBAppException {
		BPTree indexTree = (BPTree) Functions.deserialize("data/" + name + strClusteringKeyColumn + ".class");
		TupleRef refLocation = indexTree.insertLocation(tuple);
		System.out.println(tuple);
		System.out.println(refLocation);
		System.out.println(this);
		if (refLocation != null) {
			String pageName = refLocation.getPage();
			int insertionIndex = refLocation.getIndexInPage();
			PageData pageData = getPageData(pageName);
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");

			if (!pageData.isFull()) {
				insertIntoPage(pageData, page, tuple, insertionIndex);
				Functions.serialize(this, "data/" + name + ".class");
				return;
			}

			else {
				Functions.serialize(indexTree,"data/" + name + strClusteringKeyColumn + ".class");
				Tuple removeTuple = pageData.getMax();
				for (String bPTreeName : bPTrees) {
					BPTree bptree = (BPTree) Functions.deserialize("data/" + bPTreeName + ".class");
					bptree.delete(removeTuple);
					Functions.serialize(bptree, "data/" + bPTreeName + ".class");
				}

				for (String rTreeName : rTrees) {
					BPTree rtree = (BPTree) Functions.deserialize("data/" + rTreeName + ".class");
					rtree.delete(removeTuple);
					Functions.serialize(rtree, "data/" + rTreeName + ".class");
				}

				page.remove(page.size()-1);
			
				insertIntoPage(pageData, page, tuple, insertionIndex);
				insert(removeTuple);
				return;
			}
		}
		else {
			PageData pageData = pages.get(pages.size()-1);
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");
			if(!pageData.isFull()) {
				insertIntoPage(pageData, page, tuple, page.size());
				Functions.serialize(this, "data/" + name + ".class");
				return;
			}
			else {
				createNewPage(tuple);
				Functions.serialize(this, "data/" + name + ".class");
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void indexedInsertR(Tuple tuple) throws DBAppException {
		BPTree indexTree = (BPTree) Functions.deserialize("data/" + name + strClusteringKeyColumn + ".class");
		TupleRef refLocation = indexTree.insertLocation(tuple); 
		if (refLocation != null) {
			String pageName = refLocation.getPage();
			int insertionIndex = refLocation.getIndexInPage();
			PageData pageData = getPageData(pageName);
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");

			if (!pageData.isFull()) {
				insertIntoPage(pageData, page, tuple, insertionIndex);
				Functions.serialize(this, "data/" + name + ".class");
				return;
			}

			else {
				Tuple removeTuple = pageData.getMax();

				for (String bPTreeName : bPTrees) {
					BPTree bptree = (BPTree) Functions.deserialize("data/" + bPTreeName + ".class");
					bptree.delete(removeTuple);
					Functions.serialize(bptree, "data/" + bPTreeName + ".class");
				}

				for (String rTreeName : rTrees) {
					BPTree rtree = (BPTree) Functions.deserialize("data/" + rTreeName + ".class");
					rtree.delete(removeTuple);
					Functions.serialize(rtree, "data/" + rTreeName + ".class");
				}

				page.remove(page.size()-1);
				insertIntoPage(pageData, page, tuple, insertionIndex);
				Functions.serialize(this, "data/" + name + ".class");
				insert(removeTuple);
				return;
			}
		}
		else {
			PageData pageData = pages.get(pages.size()-1);
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");
			if(!pageData.isFull()) {
				insertIntoPage(pageData, page, tuple, page.size());
				Functions.serialize(this, "data/" + name + ".class");
				return;
			}
			else {
				createNewPage(tuple);
				Functions.serialize(this, "data/" + name + ".class");
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void binaryInsert(Tuple tuple) throws DBAppException {
		for(int i= 0 ; i<pages.size() ; i++) {
			PageData pd = pages.get(i);
			Page page = (Page) Functions.deserialize("data/"+pd.getPageName()+".class");
			if(!pd.isFull()) { 
				if( Functions.compareObjects(tuple.get(strClusteringKeyColumn), pd.getMax().get(strClusteringKeyColumn)) <= 0) {
					int insertionIndex = page.rangeBinarySearch(0, page.size() -1, tuple);
					insertIntoPage(pd, page,tuple, insertionIndex);
					Functions.serialize(this,"data/" +name+".class");
					return;
				}
				else if (i == pages.size()-1) {
					insertIntoPage(pd, page,tuple, page.size());
					Functions.serialize(this,"data/" +name+".class");
					return;
				}

			}

			else {
				if(Functions.compareObjects(tuple.get(strClusteringKeyColumn), pd.getMax().get(strClusteringKeyColumn)) <0) {
					int insertionIndex = page.rangeBinarySearch(0, page.size() -1, tuple);
					Tuple oldMax = pd.getMax();

					for (String bptreeName : bPTrees) {
						BPTree bptree = (BPTree) Functions.deserialize("data/" + bptreeName + ".class");
						bptree.delete(oldMax);
						Functions.serialize(bptree, "data/" + bptreeName + ".class");
					}
					for (String rtreeName : rTrees) {
						BPTree rtree = (BPTree) Functions.deserialize("data/" + rtreeName + ".class");
						rtree.delete(oldMax);
						Functions.serialize(rtree, "data/" + rtreeName + ".class");
					}

					page.remove(oldMax);
					insertIntoPage(pd, page, tuple, insertionIndex);
					insert(oldMax);
					Functions.serialize(this,"data/" +name+".class");
					return;
				}
				else if (pages.lastElement().equals(pages.get(i)) /*&& pages.size()==(i+1)*/) {
					createNewPage(tuple);
					Functions.serialize(this,"data/" +name+".class");
					return;
				}
			}
		}
		Functions.serialize(this,"data/" +name+".class");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void insertIntoPage(PageData pageData, Page page,Tuple tuple, int index){

		for (String bptreeName : bPTrees) {
			BPTree bptree = (BPTree) Functions.deserialize("data/" + bptreeName + ".class");
			for (int i = page.size()-1 ; i >= index ; i--) {
				bptree.updateTupleRef(page.get(i), new TupleRef(pageData.getPageName(), (i+1) ));
			}
			bptree.insert(tuple, new TupleRef(pageData.getPageName(), index));
			Functions.serialize(bptree, "data/" + bptreeName + ".class");
		}

		for (String rtreeName : rTrees) {
			BPTree rtree = (BPTree) Functions.deserialize("data/" + rtreeName + ".class");
			for (int i = page.size()-1 ; i >= index ; i--) {
				rtree.updateTupleRef(page.get(i), new TupleRef(pageData.getPageName(), (i+1) ));
			}
			rtree.insert(tuple, new TupleRef(pageData.getPageName(), index));
			Functions.serialize(rtree, "data/" + rtreeName + ".class");
		}

		page.add(index,tuple);
		pageData.setFull(page.isFull());
		pageData.setMax(page.lastElement());
		pageData.setMin(page.firstElement());
		Functions.serialize(page,"data/"+pageData.getPageName()+".class");		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createNewPage(Tuple tuple) {

		FileReader reader;
		try {
			reader = new FileReader("config/DBApp.properties");
			Properties p=new Properties();  
			p.load(reader);    
			Page page = new Page(Integer.parseInt(p.getProperty("MaximumRowsCountinPage")),Integer.parseInt(p.getProperty("NodeSize")),strClusteringKeyColumn,name+"_page"+ pageCounter);
			page.add(tuple);
			PageData pagedata = new PageData(name+"_page"+ pageCounter, tuple, tuple, page.isFull());
			pageCounter++;
			pages.add(pagedata);
			String path = "data/"+pagedata.getPageName()+".class";
			Functions.serialize(page, path);

			for (String bptreeName : bPTrees) {
				BPTree bptree = (BPTree) Functions.deserialize("data/" + bptreeName + ".class");
				System.out.println(bptree);
				bptree.insert(tuple, new TupleRef(pagedata.getPageName(), 0));
				System.out.println(bptree);
				Functions.serialize(bptree, "data/" + bptreeName + ".class");
			}

			for (String rTreeName : rTrees) {
				BPTree rTree = (BPTree) Functions.deserialize("data/" + rTreeName +".class");
				rTree.insert(tuple, new TupleRef(pagedata.getPageName(), 0));
				Functions.serialize(rTree, "data/" + rTreeName +".class");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}


	public void delete(Hashtable<String,Object> htblColNameValue) throws DBAppException {

		if(!checkValidDeletionUpdate(htblColNameValue)) {
			throw new DBAppException("Deletion : Invalid input");
		}

		htblColNameValue.remove("TouchDate");

		Enumeration<String> keys= htblColNameValue.keys();
		while (keys.hasMoreElements()) {
			String key  = keys.nextElement();
			if (hasAnIndex(key)) {
				String type = ColumnKeyType(key);
				if (type.contains("Polygon"))
					indexedDeleteR(htblColNameValue, key);
				else
					indexedDeleteBP(htblColNameValue, key);
				return;
			}
		}

		Set<String> columnNames = htblColNameValue.keySet();
		if(columnNames.contains(strClusteringKeyColumn)) {
			binaryDelete(htblColNameValue, htblColNameValue.get(strClusteringKeyColumn));
			return;
		}
		else
			linearDelete(htblColNameValue);
	}


	private void linearDelete(Hashtable<String,Object> htblColNameValue) {
		for(int i =0 ; i < pages.size() ;i++) {
			PageData pageData = pages.get(i);
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");
			for(int j = 0 ; j<page.size() ; j++) {
				Tuple tuple = page.get(j);
				if (tuple.compareToDelete(htblColNameValue)) {
					updateTreesDELETE(pageData.getPageName(), j, tuple);
					page.remove(j);
					j--;
					pageData.setFull(page.isFull());
					Functions.serialize(page,"data/" + pageData.getPageName() + ".class");
				}
			}
			if (page.isEmpty()) {
				File file = new File("data/" + pageData.getPageName() + ".class");
				file.delete();
				pages.remove(pageData);
				i--;
				Functions.serialize(this,"data/" + name + ".class");
			}
			else {
				pageData.setMax(page.lastElement());
				pageData.setMin(page.firstElement());
				pageData.setFull(page.isFull());
				Functions.serialize(this,"data/" + name + ".class");
			}
		}
		Functions.serialize(this,"data/" + name + ".class");
	}

	
	private void binaryDelete(Hashtable<String,Object> htblColNameValue , Object clusteringKeyValue) {
		int pageIndex = firstPageIndex(0, pages.size() -1, clusteringKeyValue);
		boolean done = false;

		if (pageIndex == pages.size())
			return;

		for (int i =pageIndex ;done == false && i < pages.size() ; i++) {
			PageData pageData = pages.get(i);
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");
			int tupleIndex;
			if (i== pageIndex) {
				tupleIndex = page.firstTupleIndex(0, page.size(), clusteringKeyValue);
				if (tupleIndex== -1)
					continue;
				for (int j =tupleIndex ; j < page.size() ; j++) {
					Tuple tuple = page.get(j);
					if (tuple.compareToDelete(htblColNameValue)) {
						updateTreesDELETE(pageData.getPageName(),j,tuple);
						page.remove(tuple);
						pageData.setFull(page.isFull());
						j--;
						Functions.serialize(page,"data/" + pageData.getPageName() + ".class");
					}
					else if(Functions.compareObjects(clusteringKeyValue,tuple.get(strClusteringKeyColumn))!=0) {
						done = true;
						break;
					}
				}
			}
			else {
				for (int j =0 ; j < page.size() ; j++) {
					Tuple tuple = page.get(j);
					if (tuple.compareToDelete(htblColNameValue)) {
						updateTreesDELETE(pageData.getPageName(),j,tuple);
						page.remove(tuple);
						pageData.setFull(page.isFull());
						j--;
						Functions.serialize(page,"data/" + pageData.getPageName() + ".class");
					}
					else if(Functions.compareObjects(clusteringKeyValue,tuple.get(strClusteringKeyColumn))!=0){
						done = true;
						break;
					}
				}
			}

			if (page.isEmpty()) {
				File file = new File("data/" + pageData.getPageName() + ".class");
				file.delete();
				pages.remove(pageData);
				i--;
			}
			else {
				pageData.setMax(page.lastElement());
				pageData.setMin(page.firstElement());
				pageData.setFull(page.isFull());
			}
			Functions.serialize(this,"data/" + name + ".class");
		}
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void indexedDeleteBP(Hashtable<String,Object> htblColNameValue , String indexedColumn) {
		BPTree bpTree = (BPTree) Functions.deserialize("data/" +name + indexedColumn + ".class");
		LinkedList<TupleRef> toDeleteRefs = bpTree.searchToDelete(htblColNameValue);
		
		while (!toDeleteRefs.isEmpty()) { 
			
			TupleRef toDeleteRef = toDeleteRefs.removeFirst();
			String pageName = toDeleteRef.getPage();
			int tupleIndex = toDeleteRef.getIndexInPage();
			
			for (TupleRef tupleRef : toDeleteRefs) {
				if (tupleRef.getPage().equals(pageName) && tupleRef.getIndexInPage() > tupleIndex)
					tupleRef.setTupleRef(new TupleRef(pageName, tupleRef.getIndexInPage()-1));
			}
			Page page = (Page) Functions.deserialize("data/" + pageName + ".class");
			PageData pageData = getPageData(pageName);
			Tuple tuple = page.get(tupleIndex);
			updateTreesDELETE(pageName, tupleIndex, tuple);
			page.remove(tupleIndex);
			Functions.serialize(page, "data/" + pageData.getPageName() + ".class");

			if (page.isEmpty()) {
				File file = new File("data/" + pageData.getPageName() + ".class");
				file.delete();
				pages.remove(pageData);
				Functions.serialize(this,"data/" + name + ".class");
			}
			else {
				pageData.setMax(page.lastElement());
				pageData.setMin(page.firstElement());
				pageData.setFull(page.isFull());
				Functions.serialize(this,"data/" + name + ".class");
			}
		}
		Functions.serialize(this,"data/" + name + ".class");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void indexedDeleteR(Hashtable<String,Object> htblColNameValue , String indexedColumn) {
		BPTree rTree = (BPTree) Functions.deserialize("data/" +name + indexedColumn + ".class");
		LinkedList<TupleRef> toDeleteRefs = rTree.searchToDelete(htblColNameValue);
		while (!toDeleteRefs.isEmpty()) { 
			TupleRef toDeleteRef = toDeleteRefs.removeFirst();
			String pageName = toDeleteRef.getPage();
			int tupleIndex = toDeleteRef.getIndexInPage();
			for (TupleRef tupleRef : toDeleteRefs) {
				if (tupleRef.getPage().equals(pageName) && tupleRef.getIndexInPage() > tupleIndex)
					tupleRef.setTupleRef(new TupleRef(pageName, tupleRef.getIndexInPage()-1));
			}
			Page page = (Page) Functions.deserialize("data/" + pageName + ".class");
			PageData pageData = getPageData(pageName);
			Tuple tuple = page.get(tupleIndex);
			updateTreesDELETE(pageName, tupleIndex, tuple);
			page.remove(tupleIndex);
			Functions.serialize(page, "data/" + pageData.getPageName() + ".class");

			if (page.isEmpty()) {
				File file = new File("data/" + pageData.getPageName() + ".class");
				file.delete();
				pages.remove(pageData);
				Functions.serialize(this,"data/" + name + ".class");
			}
			else {
				pageData.setMax(page.lastElement());
				pageData.setMin(page.firstElement());
				pageData.setFull(page.isFull());
				Functions.serialize(this,"data/" + name + ".class");
			}
		}
		Functions.serialize(this,"data/" + name + ".class");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateTreesDELETE(String pageName, int startIndex, Tuple deletedTuple) {
		Page page = (Page) Functions.deserialize("data/" + pageName + ".class");
		for (String bptreeName : bPTrees) {
			BPTree bptree = (BPTree) Functions.deserialize("data/" + bptreeName + ".class");
			bptree.delete(deletedTuple);
			Functions.serialize(bptree, "data/" + bptreeName + ".class");
			for (int i = startIndex +1  ; i < page.size() ; i++) {
				TupleRef tupleRef = new TupleRef(pageName, i-1);
				bptree.updateTupleRef(page.get(i), tupleRef);
			}
			Functions.serialize(bptree, "data/" + bptreeName + ".class");
		}

		for (String rtreeName : rTrees) {
			BPTree rtree = (BPTree) Functions.deserialize("data/" + rtreeName + ".class");
			rtree.delete(deletedTuple);
			Functions.serialize(rtree, "data/" + rtreeName + ".class");
			for (int i = startIndex +1  ; i < page.size() ; i++) {
				TupleRef tupleRef = new TupleRef(pageName, i-1);
				rtree.updateTupleRef(page.get(i), new TupleRef(tupleRef.getPage(), tupleRef.getIndexInPage()));
			}
			Functions.serialize(rtree, "data/" + rtreeName + ".class");
		}
	}

	private boolean checkValidInsertion(Hashtable<String,Object> htblColNameValue) {
		BufferedReader fileReader = null;
		Set<String> keys = htblColNameValue.keySet();
		keys.remove("TouchDate");
		try {
			String line = "";
			fileReader = new BufferedReader(new FileReader("data/metadata.csv"));
			fileReader.readLine();
			ArrayList<String []> all_columns = new ArrayList<>();

			while ((line = fileReader.readLine()) != null) {
				String[] column_CSV = line.split(",");
				if(column_CSV.length > 0 && column_CSV[0].equals(name))
					all_columns.add(column_CSV);
			}

			if(keys.size()!=all_columns.size()) {
				return false;
			}

			for(int i = 0; i<all_columns.size() ; i++) {
				String[] column_CSV = (String[]) all_columns.get(i);
				String columnName = column_CSV[1];
				String columnType = column_CSV[2];

				if (!keys.contains(columnName)) {
					return false;
				}

				Object columnValue = htblColNameValue.get(columnName);
				if(!valid_type(columnType, columnValue))
					return false;
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

		return true;
	}

	private boolean checkValidDeletionUpdate(Hashtable<String,Object> htblColNameValue) {
		BufferedReader fileReader = null;
		Set<String> keys = htblColNameValue.keySet();
		keys.remove("TouchDate");
		try {
			String line = "";
			fileReader = new BufferedReader(new FileReader("data/metadata.csv"));
			fileReader.readLine();
			ArrayList<String []> all_columns = new ArrayList<>();

			while ((line = fileReader.readLine()) != null) {
				String[] column_CSV = line.split(",");
				if(column_CSV.length > 0 && column_CSV[0].equals(name) && keys.contains(column_CSV[1]))
					all_columns.add(column_CSV);
			}

			if(keys.size()!=all_columns.size()) {
				return false;	
			}            

			for(int i = 0; i<all_columns.size() ; i++) {
				String[] column_CSV = (String[]) all_columns.get(i);
				String columnName = column_CSV[1];
				String columnType = column_CSV[2];
				Object columnValue = htblColNameValue.get(columnName);
				if(!valid_type(columnType, columnValue))
					return false;
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
		return true;
	}

	private boolean valid_type(String columnType,Object columnValue ) {

		if (columnType.contains("String"))
			return (columnValue instanceof String);
		if (columnType.contains("Integer"))
			return (columnValue instanceof Integer);
		if (columnType.contains("Double"))
			return (columnValue instanceof Double);
		if (columnType.contains("Boolean"))
			return (columnValue instanceof Boolean);
		if (columnType.contains("Polygon"))
			return (columnValue instanceof Polygon);
		if (columnType.contains("Date"))
			return (columnValue instanceof Date);
		return false;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		for(int i = 0 ;i<pages.size() ; i++) {
			Page p = (Page) Functions.deserialize("data/" + pages.get(i).getPageName() + ".class");
			System.out.print(p);
			System.out.println();
		}
		return "";
	}

	public void update(Hashtable<String,Object> htblColNameValue, String strClusteringKey) throws DBAppException {

		if(!checkValidDeletionUpdate(htblColNameValue)) {
			throw new DBAppException("the entered tuple is invalid");
		}

		Set<String> updateColumns = htblColNameValue.keySet();
		if (updateColumns.contains(strClusteringKeyColumn)) {
			throw new DBAppException("you are not allowed to update the value of the clustering Key");
		}

		ArrayList<String> toBeUpdatedTrees = new ArrayList<String>();
		for (String columnName : updateColumns) {
			for (String bPString : bPTrees) 
				if (bPString.contains(columnName)) {
					toBeUpdatedTrees.add(bPString);
					break;
				}
			for (String RString : updateColumns)
				if (RString.contains(columnName)) {
					toBeUpdatedTrees.add(RString);
					break;
				}
		}


		if  (hasAnIndex(strClusteringKeyColumn)) {
			indexedupdate(htblColNameValue, strClusteringKey, toBeUpdatedTrees);
		}

		binaryupdate(htblColNameValue, strClusteringKey, toBeUpdatedTrees);


	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void indexedupdate(Hashtable<String,Object> htblColNameValue, String strClusteringKey, ArrayList<String> toBeUpdatedTrees) throws DBAppException {
		BPTree indexedTree = (BPTree) Functions.deserialize("data/" + name + strClusteringKeyColumn + ".class");
		Object clusterKeyValue = ClusterKey(ColumnKeyType(strClusteringKeyColumn), strClusteringKey);
		Hashtable<String, Object> keyHashtable = new Hashtable<String, Object>();
		keyHashtable.put(strClusteringKeyColumn, clusterKeyValue);
		LinkedList<TupleRef> tupleRefs =  indexedTree.searchToDelete(keyHashtable);
		for (TupleRef tupleRef : tupleRefs) {
			Page page = (Page) Functions.deserialize("data/" + tupleRef.getPage() + ".class");
			Tuple tuple = page.get(tupleRef.getIndexInPage());
			for (String bPTreeName : toBeUpdatedTrees) {
				if (bPTrees.contains(bPTreeName)) {
					BPTree bpTree = (BPTree) Functions.deserialize("data/" + bPTreeName + ".class");
					bpTree.delete(tuple);
					Functions.serialize(bpTree, "data/" + bPTreeName + ".class");
				}
				else {
					BPTree rTree = (BPTree) Functions.deserialize("data/" + bPTreeName + ".class");
					rTree.delete(tuple);
					Functions.serialize(rTree, "data/" + bPTreeName + ".class");
				}
			}
			tuple.updateTuple(htblColNameValue);
			Functions.serialize(page, "data/" + tupleRef.getPage() + ".class");
			for (String bPTreeName : toBeUpdatedTrees) {
				if (bPTrees.contains(bPTreeName)) {
					BPTree bpTree = (BPTree) Functions.deserialize("data/" + bPTreeName + ".class");
					bpTree.insert(tuple, tupleRef);
					Functions.serialize(bpTree, "data/" + bPTreeName + ".class");
				}
				else {
					BPTree rTree = (BPTree) Functions.deserialize("data/" + bPTreeName + ".class");
					rTree.insert(tuple, new TupleRef(tupleRef.getPage(), tupleRef.getIndexInPage()));
					Functions.serialize(rTree, "data/" + bPTreeName + ".class");
				}
			}
		}
		Functions.serialize(this,"data/" + name + ".class");
	}

	private void binaryupdate(Hashtable<String,Object> htblColNameValue, String strClusteringKey, ArrayList<String> toBeUpdatedTrees) throws DBAppException {
		Object clusterKeyValue = ClusterKey(ColumnKeyType(strClusteringKeyColumn), strClusteringKey);
		int firstPageIndex = firstPageIndex(0, pages.size() -1, clusterKeyValue);

		for (int i = firstPageIndex ; i <pages.size() ; i++) {
			PageData pageData = pages.get(i);
			if (Functions.compareObjects(clusterKeyValue , pageData.getMax().get(strClusteringKeyColumn)) <= 0 ) {
				Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");
				int index = page.firstTupleIndex(0, page.size()-1, clusterKeyValue);
				if (index == -1)
					continue;
				page.updateToRight(index, clusterKeyValue, htblColNameValue, toBeUpdatedTrees, bPTrees, rTrees);
				Functions.serialize(page,"data/" + pageData.getPageName() + ".class");
			}
		}
		Functions.serialize(this,"data/" + name + ".class");

	}

	private String ColumnKeyType(String columnName) throws DBAppException {
		BufferedReader fileReader = null;
		try {
			String line = "";
			fileReader = new BufferedReader(new FileReader("data/metadata.csv"));
			fileReader.readLine();

			while ((line = fileReader.readLine()) != null) {
				String[] column_CSV = line.split(",");
				if (column_CSV[0].equals(name) && columnName.equals(column_CSV[1])) {
					return column_CSV[2];
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

		throw new DBAppException("Column : " + columnName + " does not exist in Table : " + name );
	}

	private Object ClusterKey(String type, String key) {
		if (type.contains("Integer"))
			return Integer.parseInt(key);
		if (type.contains("Double"))
			return Double.parseDouble(key);
		if (type.contains("Date"))
			try {
				return new SimpleDateFormat("yyyy/MM/dd").parse(key);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		if (type.contains("Polygon")) {
			MyPolygon myPolygon= new MyPolygon(key); 
			return myPolygon.getPolygon();
		}
		return key;					
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	LinkedList<Tuple> indexedMultipleSelection(SQLTerm indexedTerm  ,SQLTerm[] otherSQLTerms, String[] strarrOperators){
		String condtition = indexedTerm._strOperator;
		Object tree = Functions.deserialize("data/" + name + indexedTerm._strColumnName + ".class");
		if (condtition.equals("=")) {
			if (indexedTerm._objValue instanceof Polygon)
				return ((BPTree)tree).equalSearch(indexedTerm, otherSQLTerms, strarrOperators);
			else
				return ((BPTree)tree).equalSearch(indexedTerm, otherSQLTerms, strarrOperators);
		}
		else if (condtition.equals("!=")){
			if (indexedTerm._objValue instanceof Polygon)
				return ((BPTree)tree).notEqualSearch(indexedTerm, otherSQLTerms, strarrOperators);
			else
				return ((BPTree)tree).notEqualSearch(indexedTerm, otherSQLTerms, strarrOperators);
		}
		else if (condtition.equals("<") || condtition.equals("<=")){
			if (indexedTerm._objValue instanceof Polygon)
				return ((BPTree)tree).smallerthanequalSearch(indexedTerm, otherSQLTerms, strarrOperators);
			else
				return ((BPTree)tree).smallerthanequalSearch(indexedTerm, otherSQLTerms, strarrOperators);
		}
		else {
			if (indexedTerm._objValue instanceof Polygon)
				return ((BPTree)tree).greaterthanequalSearch(indexedTerm, otherSQLTerms, strarrOperators);
			else
				return ((BPTree)tree).greaterthanequalSearch(indexedTerm, otherSQLTerms, strarrOperators);
		}
	}

	LinkedList<Tuple> multipleSelectionBS(SQLTerm clusteredTerm  ,SQLTerm[] otherSQLTerms, String[] strarrOperators) {
		String condtition = clusteredTerm._strOperator;
		Object keyValue = clusteredTerm._objValue;
		if (condtition.equals("="))
			return equalOperationBS(keyValue, otherSQLTerms, strarrOperators);
		else if (condtition.equals("!="))
			return NotequalOperationBS(keyValue, otherSQLTerms, strarrOperators);
		else if (condtition.equals("<"))
			return smallerThanOperationsBS(keyValue, condtition, otherSQLTerms, strarrOperators);
		else if (condtition.equals("<="))
			return smallerThanOperationsBS(keyValue, condtition, otherSQLTerms, strarrOperators);
		else if (condtition.equals(">"))
			return greaterThanOperationsBS(keyValue, condtition, otherSQLTerms, strarrOperators);
		else
			return greaterThanOperationsBS(keyValue, condtition, otherSQLTerms, strarrOperators);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LinkedList<Tuple> singleSelect(String _strColumnName , String _strOperator, Object keyValue) throws DBAppException {
		if(!valid_type(ColumnKeyType(_strColumnName), keyValue))
			throw new DBAppException("invalid type entry");
		if(_strOperator.equals("=")) {
			if (hasAnIndex(_strColumnName)) {
				BPTree bptree = (BPTree) Functions.deserialize("data/" + name + _strColumnName + ".class");
				SQLTerm sqlterm = new SQLTerm();
				sqlterm._strColumnName = _strColumnName;
				sqlterm._strOperator = _strOperator;
				sqlterm._strTableName = name;
				sqlterm._objValue = keyValue;
				return bptree.equalSearch(sqlterm, new SQLTerm[0], new String[0]);
			}
			else if (_strColumnName.equals(strClusteringKeyColumn)) 
				return equalOperationBS(keyValue, new SQLTerm [0],new String [0]);
			else
				return singleLinearSearch(_strColumnName, _strOperator, keyValue);
		}

		else if (_strOperator.equals("!=")) {
			if (hasAnIndex(_strColumnName)) {
				BPTree bptree = (BPTree) Functions.deserialize("data/" + name + _strColumnName + ".class");
				SQLTerm sqlterm = new SQLTerm();
				sqlterm._strColumnName = _strColumnName;
				sqlterm._strOperator = _strOperator;
				sqlterm._strTableName = name;
				sqlterm._objValue = keyValue;
				return bptree.notEqualSearch(sqlterm, new SQLTerm[0], new String[0]);
			}
			else if (_strColumnName.equals(strClusteringKeyColumn)) 
				return NotequalOperationBS(keyValue,new SQLTerm [0], new String [0]);
			else
				return singleLinearSearch(_strColumnName, _strOperator, keyValue);
		}

		else if (_strOperator.equals(">")) {
			if(hasAnIndex(_strColumnName)) {
				BPTree bptree = (BPTree) Functions.deserialize("data/" + name + _strColumnName + ".class");
				SQLTerm sqlterm = new SQLTerm();
				sqlterm._strColumnName = _strColumnName;
				sqlterm._strOperator = _strOperator;
				sqlterm._strTableName = name;
				sqlterm._objValue = keyValue;
				return bptree.greaterthanequalSearch(sqlterm, new SQLTerm[0], new String[0]);
			}
			else if (_strColumnName.equals(strClusteringKeyColumn)) 
				return greaterThanOperationsBS(keyValue, "<",new SQLTerm [0], new String [0]);
			else
				return singleLinearSearch(_strColumnName, _strOperator, keyValue);
		}

		else if (_strOperator.equals(">=")) {
			if(hasAnIndex(_strColumnName)) {
				BPTree bptree = (BPTree) Functions.deserialize("data/" + name + _strColumnName + ".class");
				SQLTerm sqlterm = new SQLTerm();
				sqlterm._strColumnName = _strColumnName;
				sqlterm._strOperator = _strOperator;
				sqlterm._strTableName = name;
				sqlterm._objValue = keyValue;
				return bptree.greaterthanequalSearch(sqlterm, new SQLTerm[0], new String[0]);
			}
			else if (_strColumnName.equals(strClusteringKeyColumn)) 
				return greaterThanOperationsBS(keyValue, "<", new SQLTerm [0], new String [0]);
			else
				return singleLinearSearch(_strColumnName, _strOperator, keyValue);
		}

		else if (_strOperator.equals("<")) {
			if(hasAnIndex(_strColumnName)) {
				BPTree bptree = (BPTree) Functions.deserialize("data/" + name + _strColumnName + ".class");
				SQLTerm sqlterm = new SQLTerm();
				sqlterm._strColumnName = _strColumnName;
				sqlterm._strOperator = _strOperator;
				sqlterm._strTableName = name;
				sqlterm._objValue = keyValue;
				return bptree.smallerthanequalSearch(sqlterm, new SQLTerm[0], new String[0]);
			}
			else if (_strColumnName.equals(strClusteringKeyColumn)) 
				return smallerThanOperationsBS(keyValue, "<", new SQLTerm [0],new String [0]);
			else
				return singleLinearSearch(_strColumnName, _strOperator, keyValue);
		}
		else {
			if(hasAnIndex(_strColumnName)) {
				BPTree bptree = (BPTree) Functions.deserialize("data/" + name + _strColumnName + ".class");
				SQLTerm sqlterm = new SQLTerm();
				sqlterm._strColumnName = _strColumnName;
				sqlterm._strOperator = _strOperator;
				sqlterm._strTableName = name;
				sqlterm._objValue = keyValue;
				return bptree.smallerthanequalSearch(sqlterm, new SQLTerm[0], new String[0]);
			}
			else if (_strColumnName.equals(strClusteringKeyColumn)) 
				return smallerThanOperationsBS(keyValue, "<=", new SQLTerm [0],new String [0]);
			else
				return singleLinearSearch(_strColumnName, _strOperator, keyValue);
		}
	}

	LinkedList<Tuple> LinearSearch(SQLTerm[] arrSQLTerms, String[] strarrOperators){
		// linear Selection Operations.
		LinkedList<Tuple> tuples = new LinkedList<>();
		for (PageData pagedata : pages) {
			Page page = (Page) Functions.deserialize("data/" + pagedata.getPageName() + ".class");
			for (Tuple tuple : page) 
				if (Functions.satisfiesTerms(tuple, arrSQLTerms, strarrOperators) == true)
					tuples.add(tuple);
		}
		return tuples;
	}

	private LinkedList<Tuple> singleLinearSearch(String strColumnName, String strOperator, Object keyValue) {
		// linear Selection Operations.
		LinkedList<Tuple> tuples = new LinkedList<>();
		for(PageData pageData : pages) {
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");
			for(Tuple tuple : page) {
				if(strOperator.equals("=") && Functions.compareObjects(tuple.get(strColumnName), keyValue) == 0)
					tuples.add(tuple);
				else if(strOperator.equals("!=") && Functions.compareObjects(tuple.get(strColumnName), keyValue) != 0)
					tuples.add(tuple);
				else if(strOperator.equals(">") && Functions.compareObjects(tuple.get(strColumnName), keyValue) > 0)
					tuples.add(tuple);
				else if(strOperator.equals(">=") && Functions.compareObjects(tuple.get(strColumnName), keyValue) >= 0)
					tuples.add(tuple);
				else if(strOperator.equals("<") && Functions.compareObjects(tuple.get(strColumnName), keyValue) < 0)
					tuples.add(tuple);
				else if(strOperator.equals("<=") && Functions.compareObjects(tuple.get(strColumnName), keyValue) <= 0)
					tuples.add(tuple);	
			}
		}
		return tuples;
	}

	private LinkedList<Tuple> equalOperationBS(Object keyValue, SQLTerm[] otherSQLTerms, String[] strarrOperators){
		LinkedList<Tuple> tuples = new LinkedList<>();
		int pageIndex = firstPageIndex(0, pages.size()-1, keyValue);
		if (pageIndex != -1) {
			while(pageIndex < pages.size()) {
				Page page = (Page) Functions.deserialize("data/" + pages.get(pageIndex).getPageName() + ".class");
				int firstTupleIndex = page.firstTupleIndex(0, page.size() - 1, keyValue);
				int lastTupleIndex = page.LastTupleIndex(0,page.size() - 1,keyValue);
				if (firstTupleIndex == -1)
					break;
				for (int i = firstTupleIndex ; i <= lastTupleIndex ; i++) {
					Tuple tuple = page.get(i);
					if(Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
						tuples.add(tuple);
				}
				pageIndex++;
			}
		}
		return tuples;

	}

	private LinkedList<Tuple> NotequalOperationBS(Object keyValue, SQLTerm[] otherSQLTerms, String[] strarrOperators) {
		LinkedList<Tuple> outputTuples = new LinkedList<>();
		for(PageData pageData : pages) {
			Page page = (Page) Functions.deserialize("data/" + pageData.getPageName() + ".class");
			for (Tuple tuple : page) {
				if(Functions.compareObjects(keyValue, tuple.get(strClusteringKeyColumn)) !=0 
						&& Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
					outputTuples.add(tuple);
			}
		}
		return outputTuples;
	}

	private LinkedList<Tuple> smallerThanOperationsBS(Object keyValue, String strOperator, SQLTerm[] otherSQLTerms, String[] strarrOperators){
		LinkedList<Tuple> tuples = new LinkedList<>();
		int pageIndex;
		if (strOperator.equals("<")) 
			pageIndex = firstPageIndex(0, pages.size()-1, keyValue);
		else
			pageIndex = lastPageIndex(0, pages.size()-1, keyValue);

		for(int i = 0 ; i < pageIndex ; i++) {
			Page page =(Page) Functions.deserialize("data/" + pages.get(i).getPageName() + ".class");
			for (Tuple tuple : page) {
				if(Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
					tuples.add(tuple);
			}
		}
		if (pageIndex != pages.size()) {
			Page page =(Page) Functions.deserialize("data/" + pages.get(pageIndex).getPageName() + ".class");
			for(Tuple tuple : page) {
				if((strOperator.equals("<") && Functions.compareObjects(tuple.get(strClusteringKeyColumn), keyValue) >= 0)
						|| strOperator.equals("<=") && Functions.compareObjects(tuple.get(strClusteringKeyColumn), keyValue) > 0)
					break;
				if(Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
					tuples.add(tuple);
			}
		}
		return tuples;
	}

	private LinkedList<Tuple> greaterThanOperationsBS(Object keyValue, String strOperator, SQLTerm[] otherSQLTerms, String[] strarrOperators){
		LinkedList<Tuple> tuples = new LinkedList<>();
		int pageIndex;
		if (strOperator.equals(">=")) 
			pageIndex = firstPageIndex(0, pages.size()-1, keyValue);
		else
			pageIndex = lastPageIndex(0, pages.size()-1, keyValue);

		for(int i = pages.size() -1 ; i >= pageIndex ; i--) {
			Page page =(Page) Functions.deserialize("data/" + pages.get(i).getPageName() + ".class");
			if(i == pageIndex) {
				for (int j = page.size() -1 ; j>=0 ; j--) {
					Tuple tuple = page.get(j);
					if((strOperator.equals(">=") && Functions.compareObjects(tuple.get(strClusteringKeyColumn),keyValue) < 0)
							|| strOperator.equals(">") && Functions.compareObjects(tuple.get(strClusteringKeyColumn),keyValue) <= 0)
						break;
					if(Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
						tuples.addFirst(tuple);
				}
				break;
			}
			for (int j = page.size() -1 ; j>=0 ; j--) {
				Tuple tuple = page.get(j);
				if(Functions.satisfiesTerms(tuple, otherSQLTerms, strarrOperators))
					tuples.addFirst(tuple);
			}
		}
		return tuples;
	}

	int firstPageIndex(int low, int high, Object keyValue) {
		/*
		 *  returns the first occurrence of a page which may contain the Tuple according to the given Key .
		 *	Returns Pages.szie() , if the keyValue is greater than all clustering keys.
		 */
		if(high >= low) { 
			int mid = low + (high - low)/2; 
			if((mid == 0 || Functions.compareObjects(keyValue, pages.get(mid-1).getMax().get(strClusteringKeyColumn)) > 0) 
					&& Functions.compareObjects(keyValue, pages.get(mid).getMax().get(strClusteringKeyColumn)) <= 0) 
				return mid; 
			else if(Functions.compareObjects(keyValue, pages.get(mid).getMax().get(strClusteringKeyColumn)) > 0) 
				return firstPageIndex((mid + 1), high, keyValue); 
			else
				return firstPageIndex(low, (mid -1), keyValue); 
		} 
		return pages.size(); 	
	}

	int lastPageIndex(int low, int high, Object keyValue) {
		/*
		 *  returns the first occurrence of a page which may contain the Tuple according to the given Key .
		 *	Returns -1 , if keyValue does Not exist.
		 */
		if(high >= low) { 
			int mid = low + (high - low)/2; 
			if((mid == (pages.size() - 1) || Functions.compareObjects(keyValue, pages.get(mid+1).getMin().get(strClusteringKeyColumn)) < 0) 
					&& Functions.compareObjects(keyValue, pages.get(mid).getMin().get(strClusteringKeyColumn)) >= 0) 
				return mid; 
			else if(Functions.compareObjects(keyValue, pages.get(mid).getMax().get(strClusteringKeyColumn)) < 0) 
				return lastPageIndex(low, (mid - 1), keyValue); 
			else
				return lastPageIndex((mid + 1), high, keyValue); 
		} 
		return pages.size(); 	
	}

	boolean hasAnIndex(String _strColumnName) {
		//Checks if the entered Column has B/R tree indexing.
		BufferedReader fileReader = null;
		try {
			String line = "";
			fileReader = new BufferedReader(new FileReader("data/metadata.csv"));
			fileReader.readLine();

			while ((line = fileReader.readLine()) != null) {
				String[] column_CSV = line.split(",");
				if (column_CSV[0].equals(name) && _strColumnName.equals(column_CSV[1])) {
					if(column_CSV[4].equals("TRUE"))
						return true;
					else
						return false;
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
		return false;
	}

	public void creatingBPTree(String columnName) {
		BPTree<Tuple> samasemo = new BPTree<Tuple>(columnName);
		for(PageData pageData:pages ) {
			Page page = (Page)Functions.deserialize("data/" +pageData.getPageName() + ".class");
			for(int i = 0;i<page.size();i++) {
				TupleRef seksh = new TupleRef(pageData.getPageName(), i);
				samasemo.insert(page.get(i), seksh);
			}
		}
		Functions.serialize(samasemo,"data/" + name + columnName + ".class");
		bPTrees.add(name + columnName);
		Functions.serialize(this, "data/" + name + ".class");
	}

//	public void creatingRTree(String columnName) {
//		BPTree<Tuple> samasemo = new BPTree<Tuple>(columnName);
//		for(PageData pageData:pages ) {
//			Page page = (Page)Functions.deserialize("data/" +pageData.getPageName() + ".class");
//			for(int i = 0;i<page.size();i++) {
//				TupleRef seksh = new TupleRef(pageData.getPageName(), i);
//				samasemo.insert(page.get(i), seksh);
//			}
//		}
//		Functions.serialize(samasemo,"data/" + name + columnName + ".class");
//		rTrees.add(name + columnName);
//		Functions.serialize(this, "data/" + name + ".class");
//	}
	
	
	public void creatingRTree(String columnName) {
		BufferedReader fileReader = null;
		try {
			String line = "";
			fileReader = new BufferedReader(new FileReader("data/metadata.csv"));
			fileReader.readLine();
			ArrayList<String []> all_columns = new ArrayList<>();

			while ((line = fileReader.readLine()) != null) {
				String[] column_CSV = line.split(",");
				if(column_CSV.length > 0 && column_CSV[0].equals(name))
					all_columns.add(column_CSV);
			}



			for(int i = 0; i<all_columns.size() ; i++) {
				String[] column_CSV = (String[]) all_columns.get(i);
				String colName = column_CSV[1];
				String columnType = column_CSV[2];
				if(colName.equals(columnName)) {
					if(!columnType.contains("Polygon")) {
						System.out.println("Wrong Column Type");
						return;
					}
				}
			}
		} 
		catch (Exception e) {
			System.out.println("Error in CsvFileReader!");
			e.printStackTrace();
		}
		finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				System.out.println("Error while closing fileReader");
				e.printStackTrace();
			}
		}
		RTree<Tuple> tree= new RTree<Tuple>(columnName);
		for(PageData pageData:pages ) {
			Page page = (Page)Functions.deserialize("data/" +pageData.getPageName() + ".class");
			for(int i = 0;i<page.size();i++) {
				Polygon poly = (Polygon) page.get(i).get(columnName);
				MyPolygon MyPoly = new MyPolygon(poly);
				float[] dimensions  = new float[2];
				dimensions[0] = (float) poly.getBounds().getSize().getHeight();
				dimensions[1] = (float) poly.getBounds().getSize().getWidth();
				float[] coords = new float[2];
				coords[0] = (float) poly.getBounds().getX();
				coords[1] = (float) poly.getBounds().getY();
				tree.insert(coords, dimensions,page.get(i));	
			}
		}
	}

	public PageData getPageData(String pageName) {
		for (int i =0 ; i< pages.size() ; i++) {
			if(pages.get(i).getPageName().equals(pageName))
				return pages.get(i);
		}
		return null;
	}
	
	
}
