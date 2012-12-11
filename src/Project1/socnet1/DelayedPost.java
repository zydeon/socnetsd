package socnet1;

import java.util.ArrayList;
import java.util.Date;

public class DelayedPost extends Post{

	//private static final long serialVersionU = 1L;

	// private int parent;
	// private int replyLevel; 
	private ArrayList<Integer> replies;
	private Date readDate;


	public DelayedPost(String src, Date readDate){
		super(src);
		this.replies = new ArrayList<Integer>();
		this.readDate = readDate;
	}

	public int getReplyLevel(){
		return 0;
	}

	public Date getReadDate(){
		return readDate;
	}

	public ArrayList<Integer> getReplies(){
		return replies;
	}

	public void addReply(int replyID){
		replies.add(replyID);
	}
	
	public int getParent(){
		return 0;
	}	

	public String toString(){
		return  replyGap()+ID+": DELAYED POST on "+ sentDate.toString() +" from '"+source+
				"'\n"+replyGap()+">"+text+"\n";
	}	

	private String replyGap(){
		int i; String gap = "";
		for( i = 0; i < replyLevel; ++i )
			gap += "\t";		
		return gap;
	}	
}
