package socnet1;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.util.Stack;
import java.util.ArrayList;
import java.lang.Runtime;
import java.util.ArrayDeque;
import java.rmi.*;
import java.util.concurrent.Callable;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.JOptionPane;
import java.util.Date;
import java.rmi.registry.*;

import webSocket.UsersOnWebSocketServlet;
import webSocket.NotificationsWebSocketServlet;

public class RMIClient extends UnicastRemoteObject implements RMIClientInterface{

	public String serverIP;
	public int rmiPort;
	private String backupIP;
	private int backupPort;

	private Scanner sc;

	private int state;
	public String username;
	public String password;

	private int newPMs;
	private int newPosts;
	private ArrayDeque<Message> msgsToSend;

	public ServerInterface rmiServer;
	public UsersOnWebSocketServlet.UsersOnMessageInbound usersOnWebsocket;
	public NotificationsWebSocketServlet.NotificationsMessageInbound notificationsWebsocket;

	public static final int OFFLINE        = 0;
	public static final int OFFLINE_LOGGED = 1;
	public static final int ONLINE         = 2;
	
	private static final String WARNING_CONNECTION_DOWN = "Network unavailable..";

	public static final int CONNECTION_ATTEMPTS = 3;

	private Boolean webBrowser;
	private String authUrl;


	public static void main(String args[]) {

		if (args.length != 2) {
			System.out.println("java Client <hostname> <rmiport>");
			System.exit(0);
		}

		int rmiport = Integer.parseInt(args[1]);
		String serverIP = args[0];

		try{
			RMIClient c = new RMIClient(serverIP, rmiport, false);
			c.menuOffline();
			c.menu();
		}
		catch (RemoteException re) {
			System.out.println("Exception in Server.main: " + re);
		}


	}

	public RMIClient(String serverIP, int rmiport, Boolean webBrowser) throws RemoteException{
		this.rmiPort  = rmiport + 1;
		this.serverIP = serverIP;
		newPMs        = 0;
		newPosts      = 0;
		state         = OFFLINE;
		sc            = new Scanner(System.in);
		msgsToSend    = new ArrayDeque<Message>();
		this.webBrowser = webBrowser;

		// System.getProperties().put("java.security.policy", "policy.all");
		// System.setSecurityManager(new RMISecurityManager());

		if( establishConnection() ){
			System.out.println("Cliente ligado por RMI!!!");
		}

		else
			System.out.println("Erro ao ligar a servidor "+serverIP+" na porta rmi "+rmiPort);
		
	}	

	public boolean establishConnection(){
		try{
			System.out.println("A ligar a rmi://"+serverIP+":"+rmiPort+"/ZE_CARLOS");

			rmiServer = (ServerInterface) LocateRegistry.getRegistry(serverIP, rmiPort).lookup("ZE_CARLOS");

			System.out.println("já está");
			return true;
		}
	catch(java.rmi.NotBoundException e){System.out.println("NotBoundException: "+e.getMessage());}
	//catch(java.net.MalformedURLException e){System.out.println("MalformedURLException: "+e.getMessage());}
		catch(java.rmi.RemoteException e){
			//System.out.println("RemoteException: "+e.getMessage());
		}

		return false;
	}

	private void menuOffline(){
		int op;
		while( state == OFFLINE ){

			System.out.printf("%c[1;46;48m### soc.net ###%c[0;50;48m\n", 0x1B, 0x1B);
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

	public Boolean isOnline(){
		return state != OFFLINE;
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
			
			try{
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
			catch (RemoteException re){
				Process p = null;
				try{
					p = Runtime.getRuntime().exec("/usr/bin/java -cp bin socnet1.PopUp Network_down,_trying_to_reconnect... "+JOptionPane.INFORMATION_MESSAGE);
				}catch(IOException e){System.out.println("COISO");}

				new ReconnectThread(this);
				// if(p!=null)
				// 	p.destroy();
			}
		}
	}


	
	private String menuOptions(){
		return // "\n\n### soc.net ###\n" +
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

	private void login(){
		String user = Protection.inputStr("Insert Username: ",sc);
		String pass = Protection.inputStr("Insert Password: ",sc);

		if(loginToRMI(user, pass)){
			System.out.println("Login com sucesso\n");

			// Get OAuth communication
			System.out.println(this.authUrl);
			System.out.println("Code:");
			String code = Protection.inputStr("> ", sc);

			try{
				initOAuth(code);
			}
			catch(RemoteException e){
				System.out.println(e);
			}

		}
		else
			System.out.println("Login sem sucesso");

	}


	public Boolean loginToRMI(String username, String pass){
		try{
			if( rmiServer.verifyLoginRMI(this, username, pass) ){
				this.username = username;
				this.password = pass;

				this.authUrl = rmiServer.getAuthUrl(username);

				state = ONLINE;
				return true;
			}
		}
	catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }
		return false;
	}
	
	private void register(){
		String user = Protection.inputStr("Insert Username: ",sc);
		String pass = Protection.inputStr("Insert Password: ",sc);

		if(registerToRMI(user, pass)){
			System.out.println("Register com sucesso");

			// Get OAuth communication
			System.out.println(this.authUrl);
			System.out.println("Code:");
			String code = Protection.inputStr("> ", sc);

			try{
				initOAuth(code);			
			}
			catch(RemoteException e){
				System.out.println(e);
			}
		}
		else
		System.out.println("Register sem sucesso");
	}

	public Boolean registerToRMI(String username, String pass){
		try{
			if(rmiServer.registerUserRMI(this, username, pass)){
				this.username = username;
				this.password = password;

				this.authUrl = rmiServer.getAuthUrl(username);

				state = ONLINE;
				return true;
			}
		}
	catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }

		return false;
	}

	public void logout() throws RemoteException{
		rmiServer.logout(username);
		if(!webBrowser)
			System.exit(0);
	}

	private void createPost() throws RemoteException{
		Post p = new Post(username);
		if(state == ONLINE)
			rmiServer.addAndNotifyPostRMI( p );
		else
			msgsToSend.add(p);
	}

	// public void addPost(Post p){
	// 	String faceID = rest.addPost(p.getText());
	// 	p.setIdFacebook(faceID);
	// 	if(state == ONLINE)
	// 		rmiServer.addAndNotifyPostRMI( p );
	// 	else
	// 		msgsToSend.add(p);		
	// }

	private void createPM() throws RemoteException{
		String to = Protection.inputStr("To: ",sc);
		PM p = new PM(username, to);
		if(state == ONLINE)
		rmiServer.addAndNotifyPM_RMI( p );
		else
		msgsToSend.add(p);
	}

	@SuppressWarnings("deprecation")
	private void createDelayedPost() throws RemoteException{
		int d = Protection.inputInt("Day> ", sc);
		int m = 9; //= Protection.inputInt("Month> ", sc);
		int y = 2012; //Protection.inputInt("Year> ", sc);
		int h = Protection.inputInt("Hour> ", sc);
		int i = Protection.inputInt("Minute> ", sc);
		int s = Protection.inputInt("Second> ", sc);

		Date date = new Date( y-1900,m,d,h,i,s );
		//System.out.println("Delayed post sent on: " +date);

		DelayedPost msg = new DelayedPost(username, date);
		if( state == ONLINE ){
			rmiServer.addDelayedPostRMI( msg );
		}
		else
		msgsToSend.add(msg);
	}	

	private void checkPosts() throws RemoteException{
		int i;
		if(state==ONLINE){
			ArrayList<Post> posts = rmiServer.getAllOrderedPostsRMI(username);

			System.out.println("New posts:");

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
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void checkPMs() throws RemoteException{
		if( state==ONLINE ){
			Stack<PM> pms = rmiServer.getNewPMs(username);
			System.out.println("New pms:");
			System.out.println(pms);
			newPMs = 0;
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void replyPost() throws RemoteException{
		if(state==ONLINE){
			// http://stackoverflow.com/questions/4685563/how-to-pass-a-function-as-a-parameter-in-java
			Post post = choosePosts(new Callable<ArrayList<Post>>(){
				public ArrayList<Post> call(){
					ArrayList<Post> posts =null;
					try{
						posts = rmiServer.getAllOrderedPostsRMI(username);
					}
				catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }
					return posts;
				}
			}
			);

			if(post != null){
				Post reply = new Post( username, post.getID(), post.getReplyLevel()+1);
				rmiServer.replyPostRMI(reply);
			}
			else{
				System.out.println("Posts were not created!");
			}
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	public void addComment(){

	}

	private void editPost() throws RemoteException{
		if(state == ONLINE){
			// http://stackoverflow.com/questions/4685563/how-to-pass-a-function-as-a-parameter-in-java
			Post post = choosePosts(new Callable<ArrayList<Post>>(){
				public ArrayList<Post> call(){
					ArrayList<Post> posts =null;
					try{
						posts = rmiServer.getUserWrittenPostsRMI(username);
					}
				catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }
					return posts;
				}
			}
			);

			if(post != null){
				rmiServer.updatePost( Protection.inputStr("> ", sc), post.getID() );
			}
			else{
				System.out.println("Posts were not created!");
			}
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}
	private void deletePost() throws RemoteException{
		if(state==ONLINE){
			// http://stackoverflow.com/questions/4685563/how-to-pass-a-function-as-a-parameter-in-java
			Post post = choosePosts(new Callable<ArrayList<Post>>(){
				public ArrayList<Post> call(){
					ArrayList<Post> posts =null;
					try{
						posts = rmiServer.getUserWrittenPostsRMI(username);
					}
				catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }
					return posts;
				}
			}
			);

			if(post != null){
				rmiServer.deletePost( post.getID() );
			}
			else{
				System.out.println("Posts were not created!");
			}
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void createPicture() throws RemoteException{
		if(state == ONLINE){
			String path = Protection.inputStr("Path> ", sc);
			String text = Protection.inputStr("Text>", sc);
			String[] tokens = path.split("/");
			String filename = tokens[tokens.length-1];
			
			byte[] image = new byte[5*1024*1024];
			try{
				FileInputStream fis = new FileInputStream(path);
				if( fis.read(image)!=-1 ){
					rmiServer.addAndNotifyPostRMI( username, text, filename, image );
				}
				fis.close();
			}
			catch(FileNotFoundException e){
				System.out.println(e);
			}
			catch(IOException e){System.out.println("IOException "+e);}
			
		
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	private void viewUsers() throws RemoteException{
		if(state == ONLINE){
			ArrayList<String> users = rmiServer.checkOnlineUsers();
			System.out.println("Online Users:");
			System.out.println(users);
		}
		else
		System.out.println( WARNING_CONNECTION_DOWN );
	}

	
	private Post choosePosts( Callable<ArrayList<Post>> getPostsFunction ) throws RemoteException{
		ArrayList<Post> posts;
		ArrayList<Integer> ids = new ArrayList<Integer>();
		int i, chosen;

		try{
			posts = (ArrayList<Post>) getPostsFunction.call();	// only written posts or all posts
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
			
		}
	catch (java.lang.Exception e) { System.out.println("Exception " + e); }
		
		return null;
	}



	public String getUsername(){
		return username;
	}

	public void notifyPM() throws RemoteException {
		newPMs+=1;
		if(notificationsWebsocket == null){
			// NOTIFY LOCALLY
			try{
				Process p = Runtime.getRuntime().exec("/usr/bin/java -cp bin socnet1.PopUp New_PM "+JOptionPane.INFORMATION_MESSAGE);
			}catch(IOException e){System.out.println("COISO");}
		}
		else{
			// NOTIFY VIA WEB
			notificationsWebsocket.notifyPM();
		}
	}

	public void notifyPost() throws RemoteException{
		newPosts+=1;

		if( notificationsWebsocket == null ){
			// NOTIFY LOCALLY
			try{
				Process p = Runtime.getRuntime().exec("/usr/bin/java  -cp bin socnet1.PopUp New_Post "+JOptionPane.INFORMATION_MESSAGE);
			}catch(IOException e){System.out.println("COISO");}
		}
		else{
			// NOTIFY VIA WEB
			notificationsWebsocket.notifyPost();
		}
		
	}

	public void notifyUserOn(String username) throws RemoteException{
		if( usersOnWebsocket != null ){
			usersOnWebsocket.message(">"+username);
		}
	}

	public void notifyUserOff(String username) throws RemoteException{
		if( usersOnWebsocket != null ){
			usersOnWebsocket.message("<"+username);
		}
	}

	public void setBackupServer(String backupIP, int backupPort) throws RemoteException{
		this.backupIP   = backupIP;
		this.backupPort = backupPort;
	}

	public void setState(int s){
		state = s;
	}

	public void swapServers(){
		String tmp_i  = this.serverIP;
		this.serverIP = this.backupIP;
		this.backupIP = tmp_i;

		int tmp_p = this.rmiPort;
		this.rmiPort = this.backupPort;
		this.backupPort = tmp_p;
	}

	public void sendBufferedPosts(){
		Message msg;
		try{
			// resend buffered posts


			while( !msgsToSend.isEmpty() ){
				msg = msgsToSend.poll();

				System.out.println("A enviar "+msg.getText());
				if( msg instanceof DelayedPost)
				rmiServer.addDelayedPostRMI( (DelayedPost) msg );
				else if( msg instanceof Post )
				rmiServer.addAndNotifyPostRMI( (Post) msg );
				else
				rmiServer.addAndNotifyPM_RMI( (PM) msg );
			}
		}
	catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }
	}

	public void setUsername(String username){
		this.username = username;
	}
	public void setPassword(String pass){
		this.password = pass;
	}
	public String getPassword(){
		return this.password;
	}

	public void setUsersOnWebsocket(UsersOnWebSocketServlet.UsersOnMessageInbound inbound){
		this.usersOnWebsocket = inbound;
	}
	public void unsetUsersOnWebsocket(){
		this.usersOnWebsocket = null;
	}
	public void setNotificationsWebsocket(NotificationsWebSocketServlet.NotificationsMessageInbound inbound){
		this.notificationsWebsocket = inbound;
	}
	public void unsetNotificationsWebsocket(){
		this.notificationsWebsocket = null;
	}	

	public String getAuthUrl() throws RemoteException{
		return rmiServer.getAuthUrl(username);
	}

	public void initOAuth(String authCode) throws RemoteException{
		rmiServer.initOAuth(username, authCode);
	}

	public static String filter(String message) {
		if (message == null)
			return (null);
		// filter characters that are sensitive in HTML
		char content[] = new char[message.length()];
		message.getChars(0, message.length(), content, 0);
		StringBuilder result = new StringBuilder(content.length + 50);
		for (int i = 0; i < content.length; i++) {
			switch (content[i]) {
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '"':
				result.append("&quot;");
				break;
			default:
				result.append(content[i]);
			}
		}
		return (result.toString());
	}	

}

class ReconnectThread extends Thread{
	private RMIClient client;

	public ReconnectThread(RMIClient c){
		client = c;
		client.setState( RMIClient.OFFLINE_LOGGED );

		this.start();

	}

	public void run(){
		reconnect();
	}

	private void reconnect(){
		int i;
		Process p = null;


		for(i=0;i<RMIClient.CONNECTION_ATTEMPTS;i++){
			System.out.println("A tentar ligar a "+client.serverIP+":"+client.rmiPort);
			// TODO: mudar type
			try{
				if( client.establishConnection( ) &&
				client.rmiServer.verifyLoginRMI(client, client.username, client.password)
				){
					client.setState(RMIClient.ONLINE);

					p = Runtime.getRuntime().exec("/usr/bin/java -cp bin socnet1.PopUp Network_up! "+JOptionPane.INFORMATION_MESSAGE);

					System.out.println("A enviar posts por enviar  !!");

					client.sendBufferedPosts();

					
					return;
				}

				Thread.sleep(3000);
			}catch(IOException e){System.out.println("COISO");}
		//catch(RemoteException e){System.out.println("COISO");}
		catch(java.lang.InterruptedException e){}
			
		}
		client.swapServers();
		reconnect();
	}
}
