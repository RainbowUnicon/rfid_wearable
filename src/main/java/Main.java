import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Scanner;

import com.thingmagic.ReadExceptionListener;
import com.thingmagic.ReadListener;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.SerialReader;
import com.thingmagic.SerialReader.ReaderStats;
import com.thingmagic.SerialReader.StatusReport;
import com.thingmagic.SerialReader.TemperatureStatusReport;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.StatsListener;
import com.thingmagic.StatusListener;
import com.thingmagic.TMConstants;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;
//[918250, 923250, 913250, 905250, 923750, 912750, 918750, 926250, 921250, 905750, 915250, 904750, 911250, 916750, 926750, 921750, 913750, 925250, 910750, 916250, 922750, 904250, 917250, 909750, 903750, 911750, 906250, 919750, 927250, 922250, 907250, 920750, 909250, 925750, 920250, 914750, 908750, 924750, 915750, 910250, 903250, 908250, 919250, 924250, 914250, 902750, 907750, 917750, 906750, 912250]
//375
public class Main {
	final static String URI = "tmr:///dev/ttyACM0";
	final static float SPEED_OF_LIGHT =299792458;

	public Main() {
		final String readerURI = URI;
		final int[] antennaList = {1,2};
		java.awt.Toolkit.getDefaultToolkit().beep();
		final String fileName = getFileName();
		String desc;
		PrintListener rl = new PrintListener();
		
		while(!(desc = getDesc()).equalsIgnoreCase("exit")){
			try{

				final Reader r = Reader.create(readerURI);
				r.connect();
				r.paramSet("/reader/status/temperatureenable" , true);
			
				rl.desc = desc;
				rl.power = (int)r.paramGet("/reader/radio/readPower");
				
				Reader.Region[] supportedRegions = (Reader.Region[])r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
				r.paramSet("/reader/region/id", supportedRegions[0]);

				r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000));

				ReadExceptionListener exceptionListener = new TagReadExceptionReceiver();
				StatusListener statusListener = new StatusListener(){
					@Override
					public void statusMessage(Reader arg0, StatusReport[] arg1) {
						for(StatusReport s : arg1){
							if(s instanceof TemperatureStatusReport){
								rl.temperature = ((TemperatureStatusReport)s).getTemperature();
								//System.out.println(rl.temperature);
							}
						}
						
					}
				};
				r.addReadExceptionListener(exceptionListener);
				r.addReadListener(rl);
				r.addStatusListener(statusListener);

				for(int i =0; i < 1; i++){
					//System.out.println( r.paramGet("/reader/radio/temperature"));
					rl.startTimer();
					r.startReading();
					Thread.sleep(1000);
					r.stopReading();
					rl.endTimer();
					Thread.sleep(1000);
					System.out.println(i + "th cycle");
				}

				r.removeStatusListener(statusListener);
				r.removeReadListener(rl);
				r.removeReadExceptionListener(exceptionListener);
				r.destroy();
				java.awt.Toolkit.getDefaultToolkit().beep();
				TagDataWriter.write(fileName,rl.getQueue());
				rl.getQueue().clear();
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
		
	}


	class PrintListener implements ReadListener
	{
		private LinkedList<TagMetaData> queue;
		private String desc;
		private int temperature;
		private int power;
		private long lastBufferTime = 0;
		private long currBufferTime;
		public PrintListener(){
			queue = new LinkedList<TagMetaData>();
		}
		public void startTimer(){
			currBufferTime = System.currentTimeMillis();
		}
		public void endTimer(){
			lastBufferTime += System.currentTimeMillis() - currBufferTime;
		}

		public void tagRead(Reader ingoreme, TagReadData tr)
		{
			TagMetaData tm = new TagMetaData(tr);
			tm.description = desc;
			try {
				tm.temperature =temperature;
				tm.power = power;
				tm.time = (System.currentTimeMillis() - currBufferTime) + lastBufferTime;
				queue.add(tm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public LinkedList<TagMetaData> getQueue(){
			return queue;
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

	private final String getFileName(){
		String fileName = null;
		Scanner sc = new Scanner(System.in);
		while(true){
			System.out.println("File name: ");
			String line = sc.nextLine();
			System.out.println("Is this correct? " + line);
			String reply = sc.nextLine();
			if(reply.equalsIgnoreCase("yes") || reply.equalsIgnoreCase("y")){
				File file = new File("data/_" + line + ".py");
				if(file.exists()){
					System.out.println("File already exists!");
					continue;
				}
				fileName = line;
				break;
			}
			if(reply.equalsIgnoreCase("no") || reply.equalsIgnoreCase("n")){
				System.out.println("Okay, let's try again");
			}
		}

		return fileName;
	}
	
	private final String getDesc(){
		String desc = null;
		Scanner sc = new Scanner(System.in);

		while(true){
			System.out.println("Description: ");
			String line = sc.nextLine();
			System.out.println("Is this correct? " + line);
			String reply = sc.nextLine();
			if(reply.equalsIgnoreCase("yes") || reply.equalsIgnoreCase("y")){

				desc = line;
				break;
			}
			else if(reply.equalsIgnoreCase("no") || reply.equalsIgnoreCase("n")){
				System.out.println("Okay, let's try again");		
			}
			else{
				System.out.println("Sorry, I don't understand what you just said");
			}
		}
		return desc;
	}
	/*
	 * Main method
	 */
	public static void main(String args[]){
		new Main();
	}
}
