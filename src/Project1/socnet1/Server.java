package socnet1;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Stack;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.Enumeration;
import asciiart.ASCIIConverter;

import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;
import org.json.simple.*;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.scribe.exceptions.OAuthException;

import webSocket.UsersOnWebSocketServlet;
import rest.FacebookRest;


public class Server extends UnicastRemoteObject implements ServerInterface {

	private ConcurrentHashMap<String, ConnectionThread> connections;
	private ConcurrentHashMap<Integer, PM> pms;
	private ConcurrentHashMap<Integer, Post> posts;
	private ConcurrentHashMap<Integer, DelayedPost> delayedPosts;
	private ConcurrentHashMap<String, User> users;
	private int listenPort;
	private int type;
	public String otherIP;
	public int otherPort;
	private int otherRMI;
	private int rmiPort;
	public int udpPort;
	public ServerSocket listenSocket;
	private ConcurrentHashMap<String, RMIClientInterface> rmiUsersOnline;
	private ConcurrentHashMap<String, FacebookRest> rests;
	private String[] chatrooms = {"Um", "Dois"};

	public static int NEXT_MSG_ID = 1;
	// init sockets

	public static final int INIT_NOTIFICATION_SOCKET = 1;
	public static final int INIT_LOGIN               = 2;
	public static final int INIT_REGISTER            = 3;

	public static final int SLEEP_BETWEEN_PINGS      = 500;
	public static final int UDP_TIMEOUT              = (SLEEP_BETWEEN_PINGS*30);
	//public static final int N_TO_TAKEOVER            = 5;

	public static final int PRIMARY 				 = 1;

	public static final String PATH_TO_IMAGES = "/Users/Pedro/Documents/FCTUC/3Ano/1semestre/SD/Pratica/Projecto2/socnet/images/";

	public static void main(String args[]){
		if(args.length != 4){
			System.out.println("Usage: java Server <server function> <port_number> <other_server_ip> <other_server_port>");
			System.exit(0);
		}
		
		int type       = Integer.parseInt(args[0]);
		int port       = Integer.parseInt(args[1]);
		int rmiport    = port+1;
		String otherIP = args[2];
		int otherPort  = Integer.parseInt(args[3]);
		int otherRMI   = otherPort+1;


		try{
			Server s = new Server(port,type,otherIP,rmiport,otherPort,otherRMI);
		}
		catch (RemoteException re) {
			System.out.println("Exception in Server.main: " + re);
		}
	}

	public Server(int port, int type,String otherIP, int rmiPort, int otherPort, int otherRMI) throws RemoteException{

		this.connections    = new ConcurrentHashMap<String, ConnectionThread>();
		this.pms            = new ConcurrentHashMap<Integer, PM>();
		this.posts          = new ConcurrentHashMap<Integer, Post>();
		this.delayedPosts   = new ConcurrentHashMap<Integer, DelayedPost>();
		this.users          = new ConcurrentHashMap<String, User>();
		this.rmiUsersOnline = new ConcurrentHashMap<String, RMIClientInterface>();
		this.rests          = new ConcurrentHashMap<String, FacebookRest>();
		this.otherIP        = otherIP;
		this.otherPort      = otherPort;
		this.otherRMI       = otherRMI;
		this.udpPort        = 4444;
		this.rmiPort        = rmiPort;
		this.listenPort     = port;

		if(type == PRIMARY)
		run();
		else{
			runAsBackup();
		}

	}


	public void init(){
		// System.getProperties().put("java.security.policy", "policy.all") ;
		// System.setSecurityManager(new RMISecurityManager());
		try{
			LocateRegistry.createRegistry(this.rmiPort);
			Naming.rebind("rmi://localhost:"+this.rmiPort+"/ZE_CARLOS", this);

			this.listenSocket = new ServerSocket(listenPort);
			System.out.println("A Escuta no Porto "+listenPort);
		}
		catch (IOException e ){
			System.out.println("Listen: " + e.getMessage());
		}
		
	}

	public void run( ){
		init();
		readBackup();

		System.out.println("Main server starting on port "+listenPort);
		try{
			ConfirmThread confirmState = new ConfirmThread(this);

			while( true ) {
				handleConnection( listenSocket.accept() );
			}
		}
	catch(IOException e){ System.out.println("Listen:" + e.getMessage());}

		runAsBackup();
	}

	private void handleConnection( Socket s ){
		int i, cmd;
		String username, pass;

		try{

			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in   = new ObjectInputStream(s.getInputStream());

			cmd      = (Integer) in.readObject();
			username = (String) in.readObject();

			switch(cmd){
				case INIT_NOTIFICATION_SOCKET:
				// TER ATENCAO QUE LIGACAO PODE TER CAIDO DEPOIS DE CRIAR A PRIMEIRA LIGACAO, VERIFICAR SE USER AINDA SE ENCONTRA LIGADO
				connections.get( username ).setNotificationsSocket(in, out);
				break;

				case INIT_LOGIN:
				System.out.println(username + " logging in...");
				pass = (String) in.readObject();
				if(verifyLogin(username, pass)){
					connections.put(username,new ConnectionThread(in,out,this,username));
					out.writeObject(Client.SUCCESS);
					out.writeObject(this.otherIP);
					out.writeObject(this.otherPort);
					out.flush();
					notifyRMIUserOn(username);
				}
				else{
					System.out.println("PASS ERRADA");
					out.writeObject(Client.FAIL);
					out.flush();
				}
				break;

				case INIT_REGISTER:
				System.out.println(username + " registering...");
				pass = (String) in.readObject();
				if(registerUser(username, pass)){
					connections.put(username,new ConnectionThread(in,out,this,username));
					out.writeObject(Client.SUCCESS);
					out.writeObject(this.otherIP);
					out.writeObject(this.otherPort);
					out.flush();
					notifyRMIUserOn(username);
				}
				else{
					System.out.println("USER JA EXISTE");
					out.writeObject(Client.FAIL);
					out.flush();
				}
				break;
			}


		}catch(IOException e){
			System.out.println("IO:" + e.getMessage());
		}
		catch(java.lang.ClassNotFoundException e){
			System.out.println("IO:" + e.getMessage());
		}
	}

	public void logout(String username) throws RemoteException{
		rmiUsersOnline.remove(username);
		notifyRMIUserOff(username);
	}

	public synchronized boolean verifyLoginRMI(RMIClientInterface c, String username, String pass) throws RemoteException{
		if(users.containsKey(username)){
			User u = users.get(username);
			String password = ((User) users.get(username)).getPassword();

			if( password.equals(pass) ){				
				// save current rmi users online
				rmiUsersOnline.put(username, c );
				c.setBackupServer( this.otherIP, this.otherRMI );
				notifyRMIUserOn(username);

				// rest
				if(rests.get(username)==null){
					FacebookRest r = new FacebookRest();
					rests.put(username, r );
				}
	
				return true;
			}
		}
		return false;
	}

	public synchronized void addRMIOnline(RMIClientInterface c, String username) throws RemoteException{
		rmiUsersOnline.put(username, c );		
	}

	public synchronized String getAuthUrl(String username) throws RemoteException{
		return rests.get(username).getAuthUrl();
	}

	public synchronized boolean registerUserRMI(RMIClientInterface c, String username, String pass) throws RemoteException{
		if( users.containsKey(username) ){
			return false;
		}
		users.put(username,  new User(username, pass) );
		// save current rmi users online
		rmiUsersOnline.put(username, c );
		c.setBackupServer( this.otherIP, this.otherRMI );
		notifyRMIUserOn(username);	

		// rest
		if(rests.get(username)==null){
			FacebookRest r = new FacebookRest();
			rests.put(username, r );
		}

		Backup.writeUsers(this.users);
		System.out.println(username + " registered");
		return true;
	}

	public boolean verifyLogin(String username, String pass){
		if(users.containsKey(username)){
			String password = users.get(username).getPassword();
			return password.equals(pass);
		}
		return false;
	}

	public boolean registerUser(String username, String pass){
		if( users.containsKey(username) ){
			return false;
		}
		users.put(username,  new User(username, pass) );

		Backup.writeUsers(this.users);

		System.out.println(username + " registered");
		return true;
	}

	public void logoutClientTCP(String username){
		this.connections.remove(username);
		try{
			notifyRMIUserOff(username);
		}
		catch(java.rmi.RemoteException e){
			System.out.println(e);
		}

	}

	////////////////////////////////////////////////////////////////////////////////
	//// PUTTERS
	// CRIAR ESTAS FGUNCOES APARTE EM CONNECTIONTHREAD
	// meter ids e data mesmo antes de fazer a insercao e com synchronized para garantir coerencia
	public synchronized void putPM(PM p)  throws RemoteException{
		p.setSentDate( new Date() );
		p.setID( NEXT_MSG_ID++ );
		this.pms.put(p.getID(), p);

		Backup.writePms(this.pms);
	}

	public synchronized void putPost(Post p)  throws RemoteException{
		p.setSentDate( new Date() );
		p.setID( NEXT_MSG_ID++ );
		this.posts.put(p.getID() , p);
		Backup.writePosts(this.posts);
	}

	public synchronized void putFBPost(Post p)  throws RemoteException{
		// does not set date
		p.setID( NEXT_MSG_ID++ );
		this.posts.put(p.getID() , p);
		Backup.writePosts(this.posts);
	}	

	public synchronized void putDelayedPost(DelayedPost dp)  throws RemoteException{
		dp.setID( NEXT_MSG_ID++ );
		this.delayedPosts.put(dp.getID() , dp);
		new Timer().schedule(new DelayTask(dp.getID(), this), dp.getReadDate());

		Backup.writeDelayedPosts(this.delayedPosts);
	}

	public synchronized void updatePost(String newText, int msgID) throws RemoteException{
		Post newMsg = (Post) posts.get(msgID).clone();
		newMsg.setText( newText );
		posts.replace(msgID, newMsg);

		Backup.writePosts(this.posts);
		notifyAll(newMsg);
	}

	public void deletePost(int msgID)  throws RemoteException{
		// remove from facebook
		
		String username = posts.get(msgID).getSource();
		String idFace = posts.get(msgID).getIdFacebook();

		rests.get(username).removePost( idFace ); // check if its a reply or not

		posts.remove(msgID);
		Iterator<Post> it = posts.values().iterator();
		// remove replies also
		while(it.hasNext())
			it.next().removeReply(msgID);

		Backup.writePosts(this.posts);
	}

	public synchronized ArrayList<String> checkOnlineUsers() throws RemoteException{
		ArrayList<String> users = new ArrayList<String>();
		Iterator<String> it;
		// RMI
		it = rmiUsersOnline.keySet().iterator();
		while(it.hasNext())
			users.add( it.next() );

		// TCP
		Iterator<ConnectionThread> it2;
		it2 = connections.values().iterator();
		while( it2.hasNext() )
			users.add( it2.next().getUsername() );


		System.out.println("USERS ONLINE: "+users);
		return users;
	}

	public synchronized void addAndNotifyPostRMI( String username, String text, String filename, byte[] file ) throws RemoteException{
		Post p = new Post(username, text, filename);
		//p.setIdFacebook(idF)
		String path = PATH_TO_IMAGES+filename;
		try{
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(file);
			fos.close();

			ASCIIConverter conv = new ASCIIConverter();
			p.setImage( conv.convertAndResize(path) );

			FacebookRest r = rests.get( p.getSource() );
			if( r!=null ){
				String id = r.addPost( p.getText() );
				p.setIdFacebook(id);
			}

			putPost( p );
			if(r!=null)
				r.setStartTime(p.getSentDate().getTime());	
			notifyAll(p);

		}
		catch(java.io.FileNotFoundException e){
			System.out.println(e);
		}
		catch(java.io.IOException e){
			System.out.println(e);
		}
	}


	public synchronized void addAndNotifyPostRMI(Post p) throws RemoteException{
		FacebookRest r = rests.get( p.getSource() );
		if( r!=null ){
			String id = r.addPost( p.getText() );
			p.setIdFacebook(id);
		}
		putPost(p);
		if(r!=null)
			r.setStartTime(p.getSentDate().getTime());
		notifyAll(p);
	}

	public synchronized void addAndNotifyFBPostRMI(Post p) throws RemoteException{
		putFBPost(p);
		notifyAll(p);
	}	

	public void notifyAll(Post p){
		Iterator<String> it1;
		Iterator<ConnectionThread> it2;
		String username;
		ConnectionThread c;

		// RMI
		it1 = rmiUsersOnline.keySet().iterator();
		while( it1.hasNext() ){
			username = it1.next();
			if( !p.getSource().equals(username) ){
				users.get(username).pushPostToBeRead( p.getID() );
				notifyPostUserRMI(username);
			}
		}

		// TCP
		it2 = connections.values().iterator();
		while( it2.hasNext() ){
			c = it2.next();
			if( !c.getUsername().equals( p.getSource() ) ){
				users.get( c.getUsername() ).pushPostToBeRead( p.getID() );
				c.notifyPostUser( );
			}
		}
	}

	public void notifyRMIUserOn(String username) throws RemoteException{
		Iterator<RMIClientInterface> it1;
		it1 = rmiUsersOnline.values().iterator();
		while( it1.hasNext() ){			
			it1.next().notifyUserOn(username);
		}		
	}

	public void notifyRMIUserOff(String username) throws RemoteException{
		try{
			Iterator<RMIClientInterface> it1;
			it1 = rmiUsersOnline.values().iterator();
			while( it1.hasNext() ){			
				it1.next().notifyUserOff(username);
			}	
		}	
		catch(RemoteException e){
			System.out.println(e);
		}
	}	

	public synchronized void initOAuth(String username, String authCode) throws RemoteException{
		FacebookRest r = rests.get(username);
		r.initService(authCode);

		// start rest poll thread
		RestPoll t = new RestPoll(this, r);
		t.start();
	}

	public synchronized void addAndNotifyPM_RMI(PM p) throws RemoteException{
		Iterator<ConnectionThread> it;
		String username = p.getDest();
		ConnectionThread c;
		User u = users.get( username );

		if(u!=null){
			putPM(p);
			u.pushPmToBeRead( p.getID() );
			notifyPMUserRMI( username );

			// TCP
			it = connections.values().iterator();
			while( it.hasNext() ){
				c = it.next();
				if( c.getUsername().equals(username) )
					c.notifyPMUser(  );
			}
		}
		else
			System.out.println("USER NAO EXISTE");
	}

	public synchronized void addDelayedPostRMI(DelayedPost dp) throws RemoteException{
		putDelayedPost(dp);
	}

	public synchronized void notifyPMUserRMI( String username ){
		RMIClientInterface ru;

		// RMI
		ru = rmiUsersOnline.get( username );
		if(ru!=null)
		try{
			ru.notifyPM();
		}
		catch (java.rmi.RemoteException re) {
			System.out.println("RemoteException " + re);
		}
		else
		System.out.println("USER NAO ESTA ONLINE POR RMI");
	}


	public synchronized void notifyPostUserRMI( String username ){
		RMIClientInterface ru;

		// RMI
		ru = rmiUsersOnline.get( username );
		if(ru!=null){
			try{
				ru.notifyPost();
			}
			catch (java.rmi.RemoteException re) {
				System.out.println("RemoteException " + re);
			}
		}
		else
		System.out.println("USER NAO ESTA ONLINE POR RMI");
	}

	//// GETTERS
	public ConcurrentHashMap<Integer, PM> getPMs() {
		return pms;
	}

	public int getType(){
		return this.type;
	}

	public ConcurrentHashMap<Integer, Post> getPosts(){
		return this.posts;
	}

	public Post getPost(int msgID){
		return this.posts.get(msgID);
	}
	

	public User getUser(String username){
		return this.users.get(username);
	}

	public synchronized ArrayList<String> getUserNames() throws RemoteException{
		ArrayList<String> usernames = new ArrayList<String>();
		Enumeration<String> keys = users.keys();
		while( keys.hasMoreElements() )
			usernames.add( (String) keys.nextElement());
		return usernames;
		// return usernames.toArray(new String[0]);
	}


	public ConcurrentHashMap<String, RMIClientInterface> getRmiUsersOnline(){
		return rmiUsersOnline;
	}

	public ConcurrentHashMap<String, ConnectionThread> getConnections(){
		return connections;
	}

	public ConcurrentHashMap<String, User> getUsers(){
		return users;
	}


	public ConcurrentHashMap<Integer, DelayedPost> getDelayedPosts(){
		return delayedPosts;
	}


	private void readBackup(){
		this.pms            = new ConcurrentHashMap<Integer, PM>();
		this.posts          = new ConcurrentHashMap<Integer, Post>();
		this.delayedPosts   = new ConcurrentHashMap<Integer, DelayedPost>();
		this.users          = new ConcurrentHashMap<String, User>();



		ConcurrentHashMap<String, User> u = Backup.readUsers();
		if( u != null )
		this.users.putAll(u);

		ConcurrentHashMap<Integer, PM> p = Backup.readPMs();
		if( p != null )
		this.pms.putAll(p);

		int maxID = NEXT_MSG_ID;
		Iterator<PM> it1;
		PM pm;
		it1 = pms.values().iterator();
		while( it1.hasNext() ){
			pm = it1.next();
			if( maxID < pm.getID() )
			maxID = pm.getID();
		}

		ConcurrentHashMap<Integer, Post> p_ = Backup.readPosts();
		if( p_ != null )
		this.posts.putAll(p_);

		Iterator<Post> it2;
		Post post;
		it2 = posts.values().iterator();
		while( it2.hasNext() ){
			post = it2.next();
			if( maxID < post.getID() )
			maxID = post.getID();
		}

		ConcurrentHashMap<Integer, DelayedPost> dp = Backup.readDelayedPosts();
		if( dp != null )
		this.delayedPosts.putAll(dp);


		Iterator<DelayedPost> it3;
		DelayedPost dp_;
		it3 = delayedPosts.values().iterator();
		while( it3.hasNext() ){
			dp_ = it3.next();
			if( maxID < dp_.getID() )
			maxID = dp_.getID();

			// criar timers
			new Timer().schedule(new DelayTask(dp_.getID(), this), dp_.getReadDate());
		}

		NEXT_MSG_ID = maxID+1;
	}

	public synchronized void replyPostRMI(Post reply) throws RemoteException{
		String id = "";
		if(reply.getReplyLevel()==1){
			// get facebook id of parent post
			String idFace = posts.get( reply.getParent() ).getIdFacebook();
			id = rests.get( reply.getSource() ).addComment( idFace, reply.getText() );
		}

		reply.setIdFacebook(id);
		// try{
			putPost(reply);
		// }
		// catch (java.rmi.RemoteException re) {
		// 	System.out.println("RemoteException " + re);
		// }

		getPost(reply.getParent()).addReply(reply.getID());  // add reply to parent
		Backup.writePosts(posts);
		notifyAll(reply);

	}
	public synchronized ArrayList<Post> getUserWrittenPostsRMI(String username) throws RemoteException{
		try{
			ArrayList<Post> posts_ = getAllOrderedPostsRMI(username);
			int i;
			// only posts that were sent by this user
			for( i = 0; i < posts_.size(); ++i ){
				if( !posts_.get(i).getSource().equals(username) ){
					posts_.remove(i);
					i-=1;
				}
			}
			return posts_;
		}
		catch (java.rmi.RemoteException re) {
			System.out.println("RemoteException " + re);
		}

		return new ArrayList<Post>(); // nao retornar null
	}

	public synchronized String getPostSource(int postID) throws RemoteException{
		Post p = posts.get(postID);
		if(p!=null){
			return p.getSource();
		}
		return null;
	}

	public synchronized ArrayList<Post> getAllOrderedPostsRMI(String username) throws RemoteException{
		int i, j;
		ArrayList<Post> posts_ = new ArrayList<Post> (this.posts.values());
		
		// second by Date
		//if(posts_.size()>1)
		Collections.sort(posts_, new Comparator<Post>(){
			public int compare(Post p1, Post p2) {
				return -p1.getSentDate().compareTo(p2.getSentDate());
			}
		});
		

		ArrayList<Post> orderedPosts = new ArrayList<Post>();

		for( i = 0; i < posts_.size(); ++i ){
			if(posts_.get(i).getParent()==0){
				getChildren( orderedPosts, posts_.get(i) );
			}
		}


		// actualizar posts que nao foram lidos
		int id = users.get( username ).popPostToBeRead();
		for( i = 0; i < orderedPosts.size(); ++i ){
			orderedPosts.get(i).read = true;
		}
		while( id != -1 ){
			//posts.get(  posts.indexOf(id)  ).read = false;
			for( i = 0; i < orderedPosts.size(); ++i ){
				if( orderedPosts.get(i).getID() == id )
				orderedPosts.get(i).read = false;
			}
			id = users.get(username).popPostToBeRead() ;
		}

		return orderedPosts;
	}
	public synchronized Stack<PM> getNewPMs(String username) throws RemoteException{
		Stack<PM> pms_ = new Stack<PM>();

		Stack<Integer> pmsToBeRead = users.get(username).getPmsToBeRead();

		System.out.println(pmsToBeRead);

		while( !pmsToBeRead.empty() )
		pms_.push( pms.get( pmsToBeRead.pop() ) );
		
		return pms_;
	}

	public synchronized PM[] getAllPMs(String username) throws RemoteException{
		ArrayList<PM> pms_ = new ArrayList<PM>();
		PM pm;

		Iterator<PM> it = pms.values().iterator();
		while(it.hasNext()){
			pm = it.next();
			if(pm.getDest().equals(username))
				pms_.add(pm);
		}

		// sort pms by date
		Collections.sort(pms_, new Comparator<PM>(){
					public int compare(PM p1, PM p2) {
						return p1.getSentDate().compareTo(p2.getSentDate());
					}
				});		

		return pms_.toArray( new PM[0] );
	}	

	private synchronized void getChildren( ArrayList<Post>orderedPosts, Post post ){
		orderedPosts.add( post );

		int i;
		for( i = post.getReplies().size()-1; i >= 0; --i ){		// ordem de data (mais atual em cima)
			getChildren( orderedPosts, this.posts.get(post.getReplies().get(i)));
		}
	}

	public synchronized String[] getChatrooms() throws RemoteException{
		return this.chatrooms;
	}

	//// USED BY BACKUP ONLY //////////////////////////////////////////////////


	public void runAsBackup( ){
		System.out.println("Backup server starting");
		int n=0;


		DatagramSocket socket = null;
		try{
			listenSocket = new ServerSocket(listenPort);
			new redirectThread(this);

			socket = new DatagramSocket( udpPort );
			socket.setSoTimeout(UDP_TIMEOUT);
			System.out.print("Receiving ");
			while(true){
				byte[] buf = new byte[256];
				DatagramPacket ping   = new DatagramPacket(buf,buf.length);
				socket.receive(ping);

				System.out.print(".");
			}

		}
		catch(SocketTimeoutException e){
			System.out.println("Lost connection");
			/*System.out.println("Lost Connection ("+ (N_TO_TAKEOVER-(n+1)) +" to take over)");*/
		}
	catch(SocketException e){System.out.println("Socket1:" + e.getMessage());}
	catch(IOException e){System.out.println("IOException:" + e.getMessage());}

		if(socket!=null)
		socket.close();

		try{
			listenSocket.close();
		}
	catch(IOException e){System.out.println("IOException:" + e.getMessage());}
		run( );
	}
}

class redirectThread extends Thread{
	private Server server;
	private ServerSocket socket;

	public redirectThread(Server sv){
		this.server = sv;
		this.socket = server.listenSocket;
		this.start();
	}
	public void run(){
		String username, pass;
		int cmd;

		try{
			while( true ){

				Socket s = socket.accept();

				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream in   = new ObjectInputStream(s.getInputStream());

				cmd      = (Integer) in.readObject();
				username = (String) in.readObject();
				pass = (String) in.readObject();

				System.out.println(username + " trying to log in...");

				out.writeObject(Client.REDIRECT);
				out.writeObject(server.otherIP);
				out.writeObject(server.otherPort);
				out.flush();
			}

		}catch(IOException e){
			System.out.println("Terminating redirectThread...: " + e.getMessage());
		}
		catch(java.lang.ClassNotFoundException e){
			System.out.println("IO:" + e.getMessage());
		}
	}
}

class ConfirmThread extends Thread {
	private Server server;

	public ConfirmThread( Server sv ){
		this.server = sv;
		this.start();
	}
	public void run(){
		DatagramSocket socket = null;
		try{
			socket = new DatagramSocket( );

			while(true){
				String texto="ping";
				byte[] m =texto.getBytes();
				// DatagramPacket request = new DatagramPacket(m,m.length);
				InetAddress address = InetAddress.getByName(server.otherIP);

				// socket.receive(request);

				DatagramPacket ping   = new DatagramPacket(m,m.length, address , server.udpPort );
				socket.send(ping);
				Thread.sleep(server.SLEEP_BETWEEN_PINGS);
			}
		}catch(SocketTimeoutException e){
		}catch (SocketException e){System.out.println("Socket2: " + e.getMessage());
		}catch (IOException e){System.out.println("IO: " + e.getMessage());
		}catch (InterruptedException e){}

	}
}

class DelayTask extends TimerTask{
	// delayed postst
	private int dpID;
	private Server server;
	public DelayTask(int dpID, Server sv){
		this.dpID = dpID;
		this.server = sv;
	}
	public void run(){
		DelayedPost dp = server.getDelayedPosts().get(dpID);
		server.getDelayedPosts().remove(dpID);
		dp.setSentDate( new Date() );
		server.getPosts().put(dpID, dp);
		server.notifyAll(dp);

		Backup.writeDelayedPosts( server.getDelayedPosts() );
		Backup.writePosts( server.getPosts() );
	}
}

class RestPoll extends Thread{
	private Server server;
	private FacebookRest rest;
	private static final int SLEEP_TIME = 1 * 10 * 1000;

	public RestPoll(Server sv, FacebookRest r){
		this.server = sv;
		this.rest = r;
	}
	public void run(){
		while(true){
			try{
				ArrayList<Post> posts = this.rest.getPosts();
				System.out.println(posts);
				for(Post p : posts)
					server.addAndNotifyFBPostRMI( p );



				// if( posts != null && posts.size()>0 ){
				// 	int i = 0;
				// 	while( i<posts.size() && posts.get(i).getSentDate().compareTo(start_time)>0 ){
				// 		server.addAndNotifyFBPostRMI( posts.get(i++) );
				// 		System.out.println("A adicionar:"+posts.get(i-1));
				// 	}
				// 	this.start_time = posts.get(0).getSentDate();
				// 	System.out.println("start_time depois = "+start_time);			
				// }

				Thread.sleep(RestPoll.SLEEP_TIME);
			}
			catch(RemoteException e){
				System.out.println( e);
			}
			catch(InterruptedException e){
				System.out.println(e);
			}
		}
	}
}

