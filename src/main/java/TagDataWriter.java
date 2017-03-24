import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import com.thingmagic.TagReadData;

public class TagDataWriter {
	static boolean first = true;
	public static void write(String fileName, LinkedList<TagMetaData> queue){
		//System.out.println("Wrtiting data...");
		try(PrintWriter writer = new PrintWriter(new FileOutputStream(
			    new File("data/_" + fileName +".py"), 
			    true /* append = true */))){
			if(first == true){
				writer.println("_line = []");
				first = false;
			}
			for(TagMetaData data : queue){
				TagReadData tr = data.tr;
				writer.println("_line.append(TagData(\"" +data.description + "\",\"" +tr.epcString() + "\","+tr.getAntenna() +","+ tr.getFrequency()+"," +tr.getPhase() +"," +tr.getReadCount() +","+ tr.getRssi() + ","+tr.getTime() + "," + data.time + "," +data.power + "))");
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Fin.");
	}
}
