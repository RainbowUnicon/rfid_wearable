import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

import com.thingmagic.ReadExceptionListener;
import com.thingmagic.ReadListener;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TMConstants;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;

public class Main {
	final static String URI = "tmr:///dev/ttyACM0";
	final static float SPEED_OF_LIGHT =299792458;
	public Main() {
		String distance = null;
		
		Scanner sc = new Scanner(System.in);

		while(true){
			System.out.println("distance: ");
			String line = sc.nextLine();
			System.out.println("Is this correct? " + line);
			String reply = sc.nextLine();
			if(reply.equalsIgnoreCase("yes") || reply.equalsIgnoreCase("y")){
				distance = line;
				break;
			}
			if(reply.equalsIgnoreCase("no") || reply.equalsIgnoreCase("n")){
				sc.close();
				System.exit(-1);
			}
		}
		sc.close();
		
		try(PrintWriter writer = new PrintWriter("data/_" + distance +"mm.py", "UTF-8")){
			String readerURI = URI;
			final Reader r = Reader.create(readerURI);
			r.connect();
			int[] antennaList = {1};

			Reader.Region[] supportedRegions = (Reader.Region[])r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
			r.paramSet("/reader/region/id", supportedRegions[0]);
			
			SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
			r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
			
			ReadExceptionListener exceptionListener = new TagReadExceptionReceiver();
			PrintListener rl = new PrintListener();
			r.addReadExceptionListener(exceptionListener);
			r.addReadListener(rl);
			
			for(int i =0; i < 1; i++){
				r.startReading();   
				Thread.sleep(1000);
				r.stopReading();
				Thread.sleep(1000);
				System.out.print(".");
			}
			System.out.println(".");
			
			Integer keys[] = rl.fcTable.keySet().toArray(new Integer[rl.fcTable.keySet().size()]);
			Arrays.sort(keys);
			
			writer.println("fpTable = {}");
			writer.println("fcTable = {}");
			writer.println("frTable = {}");
			
			for(int freq : keys){
				writer.println("fpTable[" + freq + "] = " + rl.fpTable.get(freq));
				writer.println("frTable[" + freq + "] = " + rl.frTable.get(freq));
				writer.println("fcTable[" + freq + "] = " +rl.fcTable.get(freq));
			}
			writer.println("dfpTable[" +distance + "] = fpTable");
			writer.println("dfrTable[" +distance + "] = frTable");
			writer.println("dfcTable[" +distance + "] = fcTable");
			
			r.removeReadListener(rl);
			r.removeReadExceptionListener(exceptionListener);
			r.destroy();


		}
		catch (ReaderException re)
		{
			System.out.println("ReaderException: " + re.getMessage());
		}
		catch (IOException ie){
			ie.printStackTrace();
		}
		catch (Exception re)
		{
			System.out.println("Exception: " + re.getMessage());
		}

	}
	
	
	class PrintListener implements ReadListener
	{
		final HashMap<Integer,ArrayList<Integer>> fpTable;
		final HashMap<Integer, ArrayList<Integer>> frTable;
		final HashMap<Integer, ArrayList<Integer>> fcTable;
		
		public PrintListener(){
			fpTable = new HashMap<Integer,ArrayList<Integer>>();
			frTable = new HashMap<Integer, ArrayList<Integer>>();
			fcTable = new HashMap<Integer, ArrayList<Integer>>();
		}
		
		public void tagRead(Reader r, TagReadData tr)
		{
			int frequency = tr.getFrequency();
			int phase = tr.getPhase();
			int rssi = tr.getRssi();
			int count = tr.getReadCount();

			if(!fpTable.containsKey(frequency)) 
				fpTable.put(frequency, new ArrayList<Integer>());
			fpTable.get(frequency).add(phase);
			
			if(!frTable.containsKey(frequency)) 
				frTable.put(frequency, new ArrayList<Integer>());
			frTable.get(frequency).add(rssi);
			
			if(!fcTable.containsKey(frequency)) 
				fcTable.put(frequency, new ArrayList<Integer>());
			fcTable.get(frequency).add(count);
		}
	}

	static class TagReadExceptionReceiver implements ReadExceptionListener
	{
		String strDateFormat = "M/d/yyyy h:m:s a";
		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		public void tagReadException(com.thingmagic.Reader r, ReaderException re)
		{
			String format = sdf.format(Calendar.getInstance().getTime());
			System.out.println("Reader Exception: " + re.getMessage() + " Occured on :" + format);
			if(re.getMessage().equals("Connection Lost"))
			{
				System.exit(1);
			}
		}
	}
	/*
	 * Main method
	 */
	public static void main(String args[]){
		new Main();
	}
}
