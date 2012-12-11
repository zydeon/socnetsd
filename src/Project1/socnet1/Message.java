package socnet1;

import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.Scanner;

public class Message implements Serializable, Cloneable{

	//private static final long serialVersionU = 1L;

	protected int ID;		// Client does not lead with hashmaps but with ordered structures and needs the ID to choose messages
	protected String source;
	protected String text;
	protected Date sentDate;	
	protected String fileName;
	protected String idFacebook;	// post id on facebook
	protected String wallID;		// user wall id where posts get written on facebook

	public Message(String src){
		inputMessage();
		this.source = src;
		this.idFacebook = "";
		this.wallID = "";
	}

	public Message(String src, String text){
		this.text = text;
		this.source = src;
		this.idFacebook = "";
		this.wallID = "";
	}

	public Message(String src, String text, String fileName){
		this.text = text;
		this.source = src;
		this.fileName = fileName;
		this.idFacebook = "";
		this.wallID = "";
	}		

	public Message(String src, String text, Date sentDate, String idFace, String wallID){
		this.source = src;
		this.text = text;
		this.sentDate = sentDate;
		this.idFacebook = idFace;
		this.wallID = wallID;
	}

	private void inputMessage(){
		Scanner sc = new Scanner(System.in);
		this.text = Protection.inputStr(">> ", sc);
	}


	public String getSource(){
		return source;
	}

	public Date getSentDate(){
		return sentDate;
	}

	public int getID(){
		return ID;
	}

	public void setID(int id){
		ID = id;
	}

	public String getText(){
		return text;
	}

	public void setText(String text){
		this.text = text;
	}

	public void setSentDate(Date date){
		sentDate = date;
	}

	public String getFileName(){
		return this.fileName;
	}

	public String getIdFacebook(){
		return this.idFacebook;
	}

	public void setIdFacebook(String id){
		this.idFacebook = id;
	}	

	public Object clone(){
		try{
			return super.clone();
		}catch(java.lang.CloneNotSupportedException e){System.out.println("Erro no clone: "+e.getMessage()); return null;}
	}
}
