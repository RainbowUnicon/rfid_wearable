import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.thingmagic.TagReadData;

public class DataContainer {
	public final String fileName;
	private final HashMap<String, HashMap<Integer, ArrayList<Integer>>> table;
	public DataContainer(String fileName){
		this.fileName = fileName;
		this.table = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
	}
	
	public void addTable(String name){
		if(table.containsKey(name)){
			throw new IllegalArgumentException(name + "  already exists!");
		}
		table.put(name, new HashMap<Integer, ArrayList<Integer>>());
	}
	
	public void addEntry(String name, int f, int i){
		if(table.containsKey(name) == false){
			throw new IllegalArgumentException(name + " already exists!");
		}
		HashMap<Integer, ArrayList<Integer>> _table = table.get(name);
		if(_table.containsKey(f) == false) 
			_table.put(f, new ArrayList<Integer>());
		_table.get(f).add(i);
	}
	public void write(){
		try(PrintWriter writer = new PrintWriter("data/_" + fileName +".py", "UTF-8")){
			String tableNames[] = table.keySet().toArray(new String[table.keySet().size()]);
			Arrays.sort(tableNames);
			for(String tableName: tableNames){
				writer.println(tableName + " = {}");
			}
			for(String tableName : tableNames){
				Integer freqs[] = table.get(tableName).keySet().toArray(new Integer[table.get(tableName).keySet().size()]);
				Arrays.sort(freqs);
				for(int freq : freqs){
					writer.println(tableName + "[" + freq + "] = " + table.get(tableName).get(freq));
				}
			}
			for(String tableName : tableNames){
				writer.println("a"+tableName+"[" +fileName + "] = " + tableName);
			}
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
