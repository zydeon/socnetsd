package socnet1;

public class PM extends Message{
	//private static final long serialVersionU = 1L;

	private String dest;

	public PM(String src, String dest){
		super(src);
		this.dest = dest;	
	}

	public PM(String src, String dest, String text){
		super(src, text);
		this.dest = dest;
	}

	public String getDest(){
		return dest;
	}

	public String toString(){
		return  ID+": PM on "+ sentDate.toString() +" from '"+source+"' to '"+dest+
				"'\n"+text+"\n";
	}	
}