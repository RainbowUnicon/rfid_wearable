import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import com.thingmagic.*;
public class Main {
	final static float SPEED_OF_LIGHT =299792458;
	public Main(){
		try{
			String readerURI ="tmr:///dev/ttyACM0"; //fill it
			final Reader r = Reader.create(readerURI);
			r.connect();
			int[] antennaList = {1};
			if (true)
			{

				Reader.Region[] supportedRegions = (Reader.Region[])r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
				if (supportedRegions.length < 1)
				{
					throw new Exception("Reader doesn't support any regions");
				}
				else
				{
					r.paramSet("/reader/region/id", supportedRegions[0]);
				}
				SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
				r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
				ReadExceptionListener exceptionListener = new TagReadExceptionReceiver();
				r.addReadExceptionListener(exceptionListener);
				PrintListener rl = new PrintListener();
				r.addReadListener(rl);
				for(int i =0; i < 10; i++){
					r.startReading();   
					Thread.sleep(10000);
					r.stopReading();
					Thread.sleep(10000);
					System.out.print(".");
				}
				System.out.println(".");
				rl.cal();
				r.removeReadListener(rl);
				r.removeReadExceptionListener(exceptionListener);

				r.destroy();
			}

		}
		catch (ReaderException re)
		{
			System.out.println("ReaderException: " + re.getMessage());
		}
		catch (Exception re)
		{
			System.out.println("Exception: " + re.getMessage());
		}

	}
	final static int magicNumber = 7;
	class PrintListener implements ReadListener
	{
		HashMap<Integer,ArrayList<Integer>> table;
		public PrintListener(){
			table = new HashMap<Integer,ArrayList<Integer>>();
		}
		int count = 0; 
		int freq = 0;
		int phase = 0;
		public void tagRead(Reader r, TagReadData tr)
		{
			int frequency = tr.getFrequency();
			int phas = tr.getPhase();

			if(!table.containsKey(frequency)){
				table.put(frequency, new ArrayList<Integer>());
			}
			table.get(frequency).add(phas);


			//	    	float x = SPEED_OF_LIGHT / (frequency * 4 * 180) * phase;
			//	    	float something =  (float)tr.getFrequency() * 1000 / (SPEED_OF_LIGHT) * (float)tr.getPhase()/180f;
			//	    	System.out.print(x + "\t");
			//	      System.out.println("Background read: " + tr.toString());
		}

		public void cal(){
			Integer[] keys = table.keySet().toArray(new Integer[table.keySet().size()]);
			Arrays.sort(keys);
			for(int i: keys){
				StringBuilder s = new StringBuilder();
				for(int x : table.get(i)){
					s.append(x +  "\t");
				}
				System.out.println(i + "\t\t" + s);
			}
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
