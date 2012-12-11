package socnet1;

import java.rmi.*;
import java.util.ArrayList;
import java.util.Stack;
import rest.FacebookRest;

public interface ServerInterface extends Remote{

	public boolean verifyLoginRMI(RMIClientInterface c, String username, String pass)  throws RemoteException;
	public boolean registerUserRMI(RMIClientInterface c, String username, String pass) throws RemoteException;
	public void putPM(PM m) throws RemoteException;
	public void putPost(Post m) throws RemoteException;
	public void updatePost(String newText, int msgID) throws RemoteException;
	public void deletePost(int msgID) throws RemoteException;
	public void addAndNotifyPM_RMI(PM p) throws RemoteException;
	public void addAndNotifyPostRMI(Post p) throws RemoteException;
	public void addAndNotifyPostRMI( String username, String text, String filename, byte[] file ) throws RemoteException;
	public void addDelayedPostRMI(DelayedPost dp) throws RemoteException;
	public ArrayList<Post> getUserWrittenPostsRMI(String username) throws RemoteException;
	public ArrayList<Post> getAllOrderedPostsRMI(String username) throws RemoteException;
	public void replyPostRMI(Post reply) throws RemoteException;
	public Stack<PM> getNewPMs(String username) throws RemoteException;
	public ArrayList<String> checkOnlineUsers() throws RemoteException;
	public ArrayList<String> getUserNames() throws RemoteException;
	public void logout(String username) throws RemoteException;
	public PM[] getAllPMs(String username) throws RemoteException;
	public String[] getChatrooms() throws RemoteException;
	public void notifyRMIUserOn(String username) throws RemoteException;
	public void notifyRMIUserOff(String username) throws RemoteException;
	public String getAuthUrl(String username) throws RemoteException;
	public void initOAuth(String username, String authCode) throws RemoteException;
	public void addRMIOnline(RMIClientInterface c, String username) throws RemoteException;
	public String getPostSource(int postID) throws RemoteException;
}
