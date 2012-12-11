package socnet1;

import java.rmi.*;
import java.util.ArrayList;
import java.util.Stack;
import webSocket.UsersOnWebSocketServlet;

public interface RMIClientInterface extends Remote{

	public void notifyPM() throws RemoteException ;
	public void notifyPost() throws RemoteException;
	public void setBackupServer(String backupIP, int backupPort) throws RemoteException;	
	public void notifyUserOn(String username) throws RemoteException;
	public void notifyUserOff(String username) throws RemoteException;
}
