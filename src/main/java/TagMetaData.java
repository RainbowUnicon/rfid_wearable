import com.thingmagic.TagReadData;

public class TagMetaData {
	public final TagReadData tr;
	public String description;
	public long time;
	public int temperature;
	public int power;
	
	public TagMetaData(TagReadData tr){
		this.tr = tr;
	}
}
