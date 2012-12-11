package socnet1;

import java.net.Socket;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import asciiart.ASCIIConverter;



class ConnectionThread extends Thread {

	// info exchange
	public static final int TRANSFER_MESSAGE = 1;   // client -> server
	public static final int CHECK_PM	     = 2;   // client -> server
	public static final int CHECK_POSTS	     = 3;   // client -> server
	public static final int EDIT_POST	     = 4;   // client -> server
	public static final int DELETE_POST	     = 5;   // client -> server
	public static final int REPLY_POST	     = 6;   // client -> server
	public static final int CHECK_USERS	     = 7;   // client -> server
	public static final int LOGOUT 		     = 8;   // client -> server
	public static final int TRANSFER_PICTURE = 9;   // client -> server
	// notifications
	public static final int NEW_PM	   		 = 1;   // server -> client
	public static final int NEW_POST	     = 2;   // server -> client

	private Server server;
	private String username;

	Socket[] sockets;

	// info exchange
	private ObjectOutputStream out;
	private ObjectInputStream in;
	// notifications
	private ObjectOutputStream out_n;
	
	
	public ConnectionThread(ObjectInputStream in, ObjectOutputStream out, Server sv , String username){
		this.sockets  = new Socket[2];
		this.server   = sv;
		this.username = username;

		this.in = in;
		this.out = out;
	}

	public void run(){
		int cmd;
		try{
			while(true){
				cmd = (Integer)in.readObject();



				switch(cmd){
					case TRANSFER_MESSAGE: parseMsg();   System.out.println("TRANSFER_MESSAGE"); break;
					case TRANSFER_PICTURE: receiveImg(); System.out.println("TRANSFER_PICTURE"); break;
					case CHECK_PM:	       checkPM();    System.out.println("CHECK_PM");	     break;
					case CHECK_POSTS:      checkPosts(); System.out.println("CHECK_POSTS");	     break;
					case EDIT_POST:	       editPost();   System.out.println("EDIT_POST");	     break;
					case DELETE_POST:      deletePost(); System.out.println("DELETE_POST");	     break;
					case REPLY_POST:       replyPost();  System.out.println("REPLY_POST");	     break;
					case CHECK_USERS:      checkUsers(); System.out.println("CHECK_USERS");  	 break;
					case LOGOUT:		   logout();  	 System.out.println("LOGOUT");	return;
				}
			}
		}
		catch(EOFException e){
			System.out.println("EOF: " + e + "-> '"+this.username+"' logged out");
		}
		catch(IOException e){
			System.out.println("IO:" + e);
		}
		catch(java.lang.ClassNotFoundException e){
		System.out.println("IO:" + e.getMessage());}
	}

	public String getUsername(){
		return this.username;
	}

	public void setNotificationsSocket( ObjectInputStream in, ObjectOutputStream out ){
		out_n = out;
		this.start();
	}

	private void logout(){
		server.logoutClientTCP(username);
	}

	private void receiveImg(){
		try{
			DataInputStream is = new DataInputStream(in);
			byte[] chunk = new byte[1024];

			Post p = (Post) receiveMsg();
			String path = server.PATH_TO_IMAGES+p.getFileName();
			FileOutputStream fos = new FileOutputStream(path);

			int num_chunks = (Integer) in.readObject();
			while( num_chunks-- > 0 ){
				is.read(chunk);
				fos.write(chunk);
			}

			fos.close();
			// is.close();
			ASCIIConverter conv = new ASCIIConverter();
			p.setImage( conv.convertAndResize(path) );
			this.server.putPost( p );	
			notifyAll( p.getID() );		
		}
		catch(java.io.FileNotFoundException e){
			System.out.println(e);
		}
		catch(IOException e){
			System.out.println(e);
		}
    	catch(ClassNotFoundException e){
    		System.out.println(e);
    	}
	}

	private Message receiveMsg(){
		try{
			Message msg = (Message) in.readObject();
			return msg;
		}
		catch(java.lang.ClassNotFoundException e){
			System.out.println("IO:" + e.getMessage());
		}
		catch(java.io.IOException e){
			System.out.println("IO:" + e.getMessage());
		}


		return null;
	}

	private void parseMsg(){
		Message msg = receiveMsg();

		if (msg instanceof DelayedPost){		// DELAYED POST
			try{
				server.putDelayedPost( (DelayedPost) msg );
			}
			catch (java.rmi.RemoteException re) {
				System.out.println("RemoteException " + re);
			}
			// DelayedPost dp = (DelayedPost) msg;
			// new Timer().schedule(new DelayTask(dp, this) , dp.getReadDate());
		}

		else if( msg instanceof Post ){
			Post post = (Post) msg;
			try{
				this.server.putPost( post );
			}
		catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }
			notifyAll( post.getID() );
		}
		else if(msg instanceof PM){
			PM pm = (PM) msg;
			try{
				this.server.putPM(pm);
			}
		catch (java.rmi.RemoteException re) { System.out.println("RemoteException " + re); }

			// TCP
			ConnectionThread c = server.getConnections().get( pm.getDest() );
			User u = server.getUser( pm.getDest() );
			if(u != null){
				u.pushPmToBeRead( pm.getID() );

				if(c!=null){
					notifyPMUser( );
				}
				else{
					System.out.println("USER NAO EXISTENTE POR TCP");
				}
				//RMI
				server.notifyPMUserRMI(pm.getDest());
			}
			else
			System.out.println("USER NAO EXISTE");
		}
	}

	private void checkPM(){
		Stack<PM> pms = new Stack<PM>();
		ConcurrentHashMap<Integer, PM> allPms = server.getPMs();

		Stack<Integer> pmsToBeRead = server.getUser(this.username).getPmsToBeRead();

		while( !pmsToBeRead.empty() )
		pms.push( allPms.get( pmsToBeRead.pop() ) );


		try{
			out.writeObject(pms);
			out.flush();
		}catch(IOException e){
			System.out.println("IO:" + e.getMessage());
		}
	}

	private void checkPosts(){
		System.out.println(server.getPosts());
		ArrayList<Post> posts = getAllOrderedPosts();
		try{
			int i;
			int id = server.getUser(this.username).popPostToBeRead();

			//System.out.println(server.getUser(this.username).getPostsToBeRead());

			for( i = 0; i < posts.size(); ++i ){
				posts.get(i).read = true;
			}

			while( id != -1 ){
				//posts.get(  posts.indexOf(id)  ).read = false;

				for( i = 0; i < posts.size(); ++i ){
					if( posts.get(i).getID() == id )
					posts.get(i).read = false;
				}
				id = server.getUser(this.username).popPostToBeRead() ;
			}

			out.writeObject(posts);
			out.flush();



		}catch(IOException e){
			System.out.println("IO:" + e.getMessage());
		}
	}

	private void checkUsers(){
		ArrayList<String> onUsers = new ArrayList<String>();
		Iterator<ConnectionThread> it;
		ConnectionThread c;

		//TCP
		it = server.getConnections().values().iterator();
		while(it.hasNext()){
			c = it.next();
			onUsers.add(c.getUsername());
			System.out.println("ui");
		}

		Iterator<String> it2;
		String user;
		it2 = server.getRmiUsersOnline().keySet().iterator();
		while( it2.hasNext() ){
			onUsers.add(it2.next());
			System.out.println("ui");
		}

		try{
			out.writeObject(onUsers);
			out.flush();
		}
		catch(IOException e){
			System.out.println("IO:" + e.getMessage());
		}

	}

	private void editPost(){
		ArrayList<Post> posts = getUserWrittenPosts();
		try{
			out.writeObject(posts);
			out.flush();

			if( posts.size()>0 ){
				Integer msgID = (Integer) in.readObject();
				String newText = (String) in.readObject();
				try{
					server.updatePost(newText, msgID);
				}
				catch (java.rmi.RemoteException re) {
					System.out.println("RemoteException " + re);
				}
			}


		}catch(IOException e){
			System.out.println("IO:" + e.getMessage());
		}
		catch(java.lang.ClassNotFoundException e){
			System.out.println("ClassNotFoundException:" + e.getMessage());
		}
	}

	private void deletePost(){
		ArrayList<Post> posts = getUserWrittenPosts();
		try{
			out.writeObject(posts);
			out.flush();

			deleteAllReplies( (Integer) in.readObject() );

		}catch(IOException e){
			System.out.println("IO:" + e.getMessage());
		}
		catch(java.lang.ClassNotFoundException e){
			System.out.println("ClassNotFoundException:" + e.getMessage());
		}
	}

	private void replyPost(){
		ArrayList<Post> posts = getAllOrderedPosts();

		if(posts == null){
			System.out.println("NAO HA POSTS");
		}
		else{
			try{
				out.writeObject( posts );
				out.flush();

				if( posts.size()>0 ){

					Post reply = (Post) receiveMsg();

					try{
						this.server.putPost(reply);
					}
					catch (java.rmi.RemoteException re) {
						System.out.println("RemoteException " + re);
					}

					server.getPost(reply.getParent()).addReply(reply.getID());	// add reply to parent
					notifyAll( reply.getID() );

					Backup.writePosts( server.getPosts() ) ;
				}

			}catch(IOException e){
				System.out.println("IO:" + e.getMessage());
			}
		}
	}

	private void deleteAllReplies(int msgID){
		// delete post and its replies
		ArrayList<Integer> replies = server.getPost(msgID).getReplies();

		if( replies.size() == 0 ){
			try{
				server.deletePost(msgID);
			}
			catch (java.rmi.RemoteException re) {
				System.out.println("RemoteException " + re);
			}
		}
		else{
			int i;
			for( i = 0; i < replies.size(); ++i ){
				deleteAllReplies(replies.get(i));
				replies.remove( replies.get(i) );
			}

			try{
				server.deletePost(msgID);
			}
			catch (java.rmi.RemoteException re) {
				System.out.println("RemoteException " + re);
			}
		}
	}

	public void notifyAll( int msgID ){
		Iterator<String> it_;
		String username;
		Iterator<ConnectionThread> it;
		ConnectionThread c;
		RMIClientInterface ru;


		// TCP
		it = server.getConnections().values().iterator();
		while( it.hasNext() ){
			c = it.next();
			if( !c.getUsername().equals( this.username ) ){
				server.getUser( c.getUsername() ).pushPostToBeRead(msgID);
				c.notifyPostUser();

			}
		}

		// RMI
		it_ = server.getRmiUsersOnline().keySet().iterator();
		while( it_.hasNext() ){
			username = it_.next();
			server.notifyPostUserRMI( username );
			server.getUser( username ).pushPostToBeRead(msgID);
		}
	}

	public void notifyPMUser( ){
		synchronized(out_n){
			try{
				out_n.writeObject( NEW_PM );
			}catch(IOException e){
				System.out.println("IO:" + e.getMessage());
			}
		}
	}

	public void notifyPostUser( ){
		synchronized(out_n){
			try{
				out_n.writeObject( NEW_POST );
			}catch(IOException e){
				System.out.println("IO:" + e.getMessage());
			}
		}
	}

	private ArrayList<Post> getUserWrittenPosts(){
		ArrayList<Post> posts = getAllOrderedPosts();
		System.out.println(posts);
		int i;
		// only posts that were sent by this user
		System.out.println(this.username);
		for( i = 0; i < posts.size(); ++i ){
			if( !posts.get(i).getSource().equals( this.username ) ){
				posts.remove(i);
				i-=1;
			}
		}

		System.out.println(posts);
		return posts;
	}


	private ArrayList<Post> getAllOrderedPosts(){
		int i, j;
		ArrayList<Post> posts = new ArrayList<Post> (server.getPosts().values());

		// second by Date
		Collections.sort(posts, new Comparator<Post>(){
			public int compare(Post p1, Post p2) {
				return -p1.getSentDate().compareTo(p2.getSentDate());
			}
		}
		);

		ArrayList<Post> orderedPosts = new ArrayList<Post>();

		for(i=0;i<posts.size();++i)
			if(posts.get(i).getParent()==0)
				getChildren( orderedPosts, posts.get(i) );

		return orderedPosts;
	}

	private void getChildren( ArrayList<Post>orderedPosts, Post post ){
		orderedPosts.add( post );

		int i;
		for(i=post.getReplies().size()-1; i>= 0;--i){
			getChildren(orderedPosts,server.getPost(post.getReplies().get(i)));
		}
	}

	public Server getServer(){
		return this.server;
	}
}

