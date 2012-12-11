package socnet1;

import java.util.ArrayList;
import java.util.Date;

public class Post extends Message{

	//private static final long serialVersionU = 1L;

	protected int parent;
	protected int replyLevel; 
	private ArrayList<Integer> replies;
	public boolean read;
	private String image;


	public Post(String src ){
		super(src);
		this.parent = 0;
		this.replyLevel = 0;
		this.replies = new ArrayList<Integer>();
	}	
	public Post(String src, int parent, int replyLevel ){
		super(src);
		this.parent = parent;
		this.replyLevel = replyLevel;
		this.replies = new ArrayList<Integer>();
	}

	public Post(String src, String text){
		super(src, text);
		this.parent     = 0;
		this.replyLevel = 0;
		this.replies = new ArrayList<Integer>();
	}	

	public Post(String src, String text, String fileName){
		super(src, text, fileName);
		this.parent     = 0;
		this.replyLevel = 0;
		this.replies = new ArrayList<Integer>();
	}

	public Post(String src, String text, int parent, int replyLevel){
		super(src, text);
		this.parent     = parent;
		this.replyLevel = replyLevel;
		this.replies = new ArrayList<Integer>();
	}

	public Post( String src, String text, Date date, String faceID, String wallID ){
		super(src, text, date, faceID, wallID);
		parent = 0;
		this.replyLevel = 0;
		this.replies = new ArrayList<Integer>();
	}

	public int getReplyLevel(){
		return replyLevel;
	}

	public ArrayList<Integer> getReplies(){
		return replies;
	}

	public void addReply(int replyID){
		replies.add(replyID);
	}

	public void removeReply(int replyID){
		if(replies.remove((Integer)replyID));		// distinguish between remove(index) and remove(object)
	}
	
	public int getParent(){
		return parent;
	}	

	public boolean unread(){
		return !read;
	}

	public String toString(){
		return  replyGap()+ID+": POST on "+ sentDate.toString() +" from '"+source+
				"'\n"+replyGap()+">"+text+"read="+read+"\n"+image;
	}	

	private String replyGap(){
		int i; String gap = "";
		for( i = 0; i < replyLevel; ++i )
			gap += "\t";
		
		return gap;
	}	

	public void setImage(String image){
		this.image = image;
	}

	public Boolean isFacebookPost(){
		return idFacebook.equals("");
	}
}
