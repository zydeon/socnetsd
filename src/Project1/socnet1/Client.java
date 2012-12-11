package socnet1;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.util.Stack;
import java.util.ArrayList;
import java.lang.Runtime;
import java.util.ArrayDeque;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import java.lang.Math;

// Create 2 threads, one for reading and another for writing

public class Client {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String serverIP;
	private int port;
	private String backupIP;
	private int backupPort;
	
	private Scanner sc;

	private int state;
	private String username;
	private String password;

	private int newPMs;
	private int newPosts;
	private ArrayDeque<Message> msgsToSend;

	private static final int OFFLINE        = 0;
	private static final int OFFLINE_LOGGED = 1;
	private static final int ONLINE         = 2;

	public static final int REDIRECT = 2;
	public static final int SUCCESS = 1;
	public static final int FAIL = 0;

	public static final int CONNECTION_ATTEMPTS = 3;

	private static final String WARNING_CONNECTION_DOWN = "Network unavailable..";
	
	public static void main(String args[]) {

		if (args.length != 2) {
			System.out.println("java Client <hostname> <port>");
			System.exit(0);
		}

		int port = Integer.parseInt(args[1]);
		String serverIP = args[0];

		Client c = new Client(serverIP, port);

		c.menuOffline();
		c.menu();

	}

	public Client(String serverIP, int port){
		this.port     = port;
		this.serverIP = serverIP;
		newPMs        = 0;
		newPosts      = 0;
		state         = OFFLINE;
		sc            = new Scanner(System.in);
		msgsToSend   = new ArrayDeque<Message>();
	}

	private void menuOffline(){
		int op;
		while( state == OFFLINE){

			System.out.printf("%c[1;46;48m### soc.net ###%c[0;50;48m\n", 0x1B, 0x1B);
			//System.out.println("\n\n### soc.net ###");

			System.out.println("0: Exit");
			System.out.println("1: Login");
			System.out.println("2: Register");

			op = Protection.inputInt("Option: ", sc);
			switch(op){
				case 1:login();break;
				case 2:register();break;
				case 0:System.exit(0);break;
			}
		}
	}
	
	private void menu(){
		int op;

		while (true){
			System.out.printf("\n\n%c[1;46;48m### soc.net ###%c[0;50;48m\n", 0x1B, 0x1B);
			System.out.print( menuOptions() );
			while(!sc.hasNextInt()){
				System.out.printf("\n\n%c[1;46;48m### soc.net ###%c[0;50;48m\n", 0x1B, 0x1B);
				System.out.print( menuOptions() );
				sc.next(); //limpa buffer
			}
			op = sc.nextInt();
			sc.nextLine();  // limpar buffer para proxima leitura
			
			switch(op){
				case 0:logout();break;
				case 1:createPost();break;
				case 2:createPM();break;
				case 3:createDelayedPost();break;
				case 4:editPost();break;
				case 5:deletePost();break;
				case 6:replyPost();break;
				case 7:createPicture();break;
				case 8:checkPMs(); break;
				case 9:checkPosts();break;
				case 10:viewUsers();break;
			}
		}
	}
	
	private String menuOptions(){
		return  //"\n\n### soc.net ###\n" +
		"0: Logout\n"+
		"1: Create post\n"+
		"2: Create PM\n"+
		"3: Create delayed post\n"+
		"4: Edit post\n"+
		"5: Delete post\n"+
		"6: Reply post\n"+
		"7: Create picture\n"+
		"8: View PMs ("+newPMs+")\n"+
		"9: View Posts ("+newPosts+")\n"+
		"10: View Online Users\n"+
		"Option: ";
	}

	private void logout() {
		try{
			out.writeObject( ConnectionThread.LOGOUT );
			out.flush();		
			System.exit(0);
		}catch(IOException e){System.out.println("IO:" + e.getMessage());}
	}

	private void login(){
		username = Protection.inputStr("Insert Username: ",sc);
		password = Protection.inputStr("Insert Password: ",sc);
		if (establishConnection(Server.INIT_LOGIN) )
		state = ONLINE;
	}
	
	private void register(){
		username = Protection.inputStr("Choose Username: ",sc);
		password = Protection.inputStr("Choose Password: ",sc);
		if( establishConnection(Server.INIT_REGISTER) )
		state = ONLINE;
	}

	private void createPost(){
		Post msg = new Post(username);
		if( state == ONLINE ){
			sendMessage(msg);
		}
		else
			msgsToSend.add(msg);

	}

	private void createPM(){
		String to = Protection.inputStr("To: ",sc);
		PM msg = new PM(username, to);

		if( state == ONLINE ){
			sendMessage(msg);
		}
		else
		msgsToSend.add(msg);
	}
	
	@SuppressWarnings("deprecation")
	private void createDelayedPost(){
		int d = Protection.inputInt("Day> ", sc);
		int m = 9; //= Protection.inputInt("Month> ", sc);
		int y = 2012; //Protection.inputInt("Year> ", sc);
		int h = Protection.inputInt("Hour> ", sc);
		int i = Protection.inputInt("Minute> ", sc);
		int s = Protection.inputInt("Second> ", sc);

		Date date = new Date( y-1900,m,d,h,i,s );
		//System.out.println("Delayed post sent on: " +date);

		Post msg = new DelayedPost(username, date  );
		if( state == ONLINE ){
			sendMessage(msg);
		}
		else
		msgsToSend.add(msg);
	}

	@SuppressWarnings("unchecked")	
	private void checkPosts(){
		if( state == ONLINE ){
			ArrayList<Post> posts;
			int i;

			try{
				out.writeObject( ConnectionThread.CHECK_POSTS );
				out.flush();

				posts = (ArrayList<Post>) in.readObject();
				if(posts!=null){
					System.out.println("New posts: ");

					for( i = 0; i < posts.size(); ++i ){

						if(posts.get(i).unread())
						System.out.printf("%c[1;31;48m", 0x1B);
						
						//else
						System.out.println(posts.get(i));

						System.out.printf("%c[0;38;48m", 0x1B);

					}


					//System.out.println(posts);

					newPosts = 0;
				}

			}catch(IOException e){System.out.println("IO:" + e.getMessage());}
		catch(java.lang.ClassNotFoundException e){System.out.println("ClassNotFoundException:" + e.getMessage());}
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	@SuppressWarnings("unchecked")	
	private void viewUsers(){
		if(state == ONLINE){
			ArrayList<String> users;
			int i;

			try{
				out.writeObject( ConnectionThread.CHECK_USERS );
				out.flush();

				users = (ArrayList<String>) in.readObject();
				if(users!=null){
					System.out.println("Online Users: ");
					System.out.println(users);
				}

			}catch(IOException e){System.out.println("IO:" + e.getMessage());}
		catch(java.lang.ClassNotFoundException e){System.out.println("ClassNotFoundException:" + e.getMessage());}
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}
	
	@SuppressWarnings("unchecked")	
	private void checkPMs(){
		Stack<PM> pms;

		if( state == ONLINE ){
			try{
				out.writeObject( ConnectionThread.CHECK_PM );
				out.flush();

				pms = (Stack<PM>) in.readObject();
				if(pms!=null){
					System.out.println("New pms: ");
					System.out.println(pms);
					newPMs = 0;
				}
			}catch(IOException e){System.out.println("IO:" + e.getMessage());}
		catch(java.lang.ClassNotFoundException e){System.out.println("ClassNotFoundException:" + e.getMessage());}
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void replyPost(){
		if( state == ONLINE ){
			Post post = choosePost(ConnectionThread.REPLY_POST);

			if(post != null){
				try{
					Post reply = new Post( username, post.getID(), post.getReplyLevel()+1);

					out.writeObject( reply );
					out.flush();

				}catch(IOException e){System.out.println("IO:" + e.getMessage());}

			}
			else{
				System.out.println("Posts were not created!");
			}
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void editPost(){

		if(state == ONLINE){
			Post post = choosePost(ConnectionThread.EDIT_POST);

			if(post != null){
				try{
					out.writeObject(post.getID());
					out.writeObject(Protection.inputStr("New text>> ", sc) );
					out.flush();

				}catch(IOException e){System.out.println("IO:" + e.getMessage());}
			}
			else
			System.out.println("Posts were not created!");
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void deletePost(){
		if(state == ONLINE){
			Post post = choosePost(ConnectionThread.DELETE_POST);

			if(post != null){
				try{
					out.writeObject(post.getID());
					out.flush();
				}catch(IOException e){System.out.println("IO:" + e.getMessage());}
			}
			else
			System.out.println("Posts were not created!");
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void createPicture(){
		if(state == ONLINE){
			    String path = Protection.inputStr("Path> ", sc);
			    //ASCIIConverter conv = new ASCIIConverter();
			    String[] tokens = path.split("/");
			    String filename = tokens[tokens.length-1];

			    try{
					//String img = conv.convertAndResize(path);
					Post post = new Post( username, "Post com imagem", filename );
					out.writeObject( ConnectionThread.TRANSFER_PICTURE );
					out.writeObject(post);
					out.flush();
					sendPicture(path);					
			    }
		    	catch(IOException e){System.out.println("IO:" + e.getMessage());}
		}
		else
		    System.out.println( WARNING_CONNECTION_DOWN );
	}

	public void sendPicture(String path){
		byte[] chunk = new byte[1024];

		try{
			FileInputStream fis = new FileInputStream( path );
			DataOutputStream os = new DataOutputStream(out);
			// get file size .available() is an estimate
			long total_bytes = (new File(path)).length();		
			int num_chunks = (int) Math.ceil((float)total_bytes/chunk.length);
			out.writeObject(num_chunks);
			int len;
			while((len=fis.read(chunk))>=0){
				os.write(chunk, 0, len);
			}
			os.flush();
			// os.close();
			fis.close();
		}
		catch(FileNotFoundException e){
			System.out.println(e);
		}
		catch(IOException e){
			System.out.println(e);
		}
	}
	
	@SuppressWarnings("unchecked")	
	private Post choosePost( int operationType ){

		ArrayList<Post> posts;
		ArrayList<Integer> ids = new ArrayList<Integer>();
		int i, chosen;

		try{
			out.writeObject( operationType );
			out.flush();

			posts = (ArrayList<Post>) in.readObject();

			for( i = 0; i < posts.size(); ++i )
			ids.add( posts.get(i).getID() );


			if(posts.size()>0){
				System.out.println("\nChoose Post: ");

				System.out.println(posts);

				do{
					chosen = Protection.inputInt("Posts ID: ", sc);
				}while( !ids.contains(chosen) );

				return posts.get( ids.indexOf(chosen) );

			}
			else
			return null;

		}catch(IOException e){System.out.println("IO:" + e.getMessage());}
	catch(java.lang.ClassNotFoundException e){System.out.println("ClassNotFoundException:" + e.getMessage());}
		
		return null;
	}

	private boolean establishConnection(int type){
		try{
			
			Socket s1 = new Socket(serverIP, port);
			Socket s2 = new Socket();               // notify socket
			if(s1.isConnected()){
				
				in  = new ObjectInputStream(s1.getInputStream());
				out = new ObjectOutputStream(s1.getOutputStream());
				
				// identify himself
				out.writeObject(type);
				out.writeObject(username);
				out.writeObject(password);
				out.flush();

				int conn_state = (Integer) in.readObject();

				if( conn_state==SUCCESS ){
					s2.connect(s1.getRemoteSocketAddress());
					
					if( s2.isConnected() ){
						NotificationsThread n = new NotificationsThread(s2, this);
						this.backupIP   = (String)in.readObject();
						this.backupPort = (Integer)in.readObject();
						return true;
					}
				}
				else if( conn_state == REDIRECT ){

					this.backupIP   = (String)in.readObject();
					this.backupPort = (Integer)in.readObject();


					swapServer();
					s1.close();

					//establishConnection(type);

					reconnect( false );
					//return true;
				}
				else{
					s1.close();
				}
			}
		}
		catch(IOException e){
			//System.out.println("IO:" + e.getMessage());
		}
	catch(java.lang.ClassNotFoundException e){System.out.println("ClassNotFoundException:" + e.getMessage());}

		return false;
	}

	public void reconnect( boolean popups ){
		int i;
		state = OFFLINE_LOGGED;
		Process p = null;

		if(popups)
		try{
			p = Runtime.getRuntime().exec("/usr/bin/java -cp bin socnet1.PopUp Network_down,_trying to reconnect... "+JOptionPane.WARNING_MESSAGE);
		}catch(IOException e){System.out.println("IO:" + e.getMessage());}

		for(i=0;i<CONNECTION_ATTEMPTS;i++){
			System.out.println("A tentar ligar a "+serverIP+":"+port);

			if(establishConnection( Server.INIT_LOGIN )){
				state = ONLINE;
				if(popups){
					//p.destroy();
					try{
						p = Runtime.getRuntime().exec("/usr/bin/java -cp bin socnet1.PopUp Network_up! "+JOptionPane.INFORMATION_MESSAGE);
					}catch(IOException e){System.out.println("IO:" + e.getMessage());}
				}
				// resend buffered posts
				while( !msgsToSend.isEmpty() )
				sendMessage( msgsToSend.poll() );

				return;
			}
			try{
				Thread.sleep(3000);
			}
		catch(java.lang.InterruptedException e){}
		}

		swapServer();

		reconnect(popups);
	}

	private void sendMessage( Message msg ){
		try{
			out.writeObject( ConnectionThread.TRANSFER_MESSAGE );
			out.writeObject(msg);
			out.flush();
		}catch(IOException e){System.out.println("IO:" + e.getMessage());}
	}

	public void swapServer(){
		// swap servers
		String tmp_i  = this.serverIP;
		this.serverIP = this.backupIP;
		this.backupIP = tmp_i;

		int tmp_p = this.port;
		this.port = this.backupPort;
		this.backupPort = tmp_p;
	}


	public String getUsername(){
		return username;
	}

	public void notifyPM(){
		newPMs+=1;
		try{
			Process p = Runtime.getRuntime().exec("/usr/bin/java -cp bin socnet1.PopUp New_PM "+JOptionPane.INFORMATION_MESSAGE);
		}catch(IOException e){System.out.println("IO:" + e.getMessage());}
	}

	public void notifyPost(){
		newPosts+=1;
		
		try{
			Process p = Runtime.getRuntime().exec("/usr/bin/java  -cp bin socnet1.PopUp New_Post "+JOptionPane.INFORMATION_MESSAGE);
		}catch(IOException e){System.out.println("IO:" + e.getMessage());}
	}

}

class NotificationsThread extends Thread{
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Client client;
	
	public NotificationsThread(Socket s,Client c){
		this.client = c;
		try{
			in = new ObjectInputStream(s.getInputStream());
			out = new ObjectOutputStream(s.getOutputStream());

			// identify himself
			out.writeObject(Server.INIT_NOTIFICATION_SOCKET);
			out.writeObject(client.getUsername());
			out.flush();

			this.start();
		}
	catch(IOException e){System.out.println("IO:" + e.getMessage());}
	}
	
	public void run(){
		try{
			while( true ){
				if( (Integer) in.readObject() == ConnectionThread.NEW_POST )
				client.notifyPost();
				else
				client.notifyPM();
			}

		}
		catch(EOFException e){
			// PORQUE e.getMessage() é NULL ??
			// System.out.println("EOF:" + e.getMessage());
			client.reconnect(true);
			// System.out.println("acabou !!!");
		}
		catch(IOException e){
			System.out.println("IO_:" + e.getMessage());
			client.reconnect(true);
			// System.out.println("acabou !!!");
		}
	catch(java.lang.ClassNotFoundException e){System.out.println("ClassNotFoundException:" + e.getMessage());}
	}
}
