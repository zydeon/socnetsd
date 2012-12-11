package webSocket;

import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WsOutbound;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.IOException;
import java.util.*;
import java.util.Iterator;

import socnet1.RMIClient;

public class UsersOnWebSocketServlet extends WebSocketServlet{
	
	private final Set<UsersOnMessageInbound> connections = new CopyOnWriteArraySet<UsersOnMessageInbound>();

	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
		return new UsersOnMessageInbound( (RMIClient) request.getSession().getAttribute("rmi") );
	}
	
	

	public final class UsersOnMessageInbound extends MessageInbound {
		
		private RMIClient client;
		
		private UsersOnMessageInbound( RMIClient c ) {
			this.client = c;
		}
		
		protected void onOpen(WsOutbound outbound) {
			connections.add(this);
			client.setUsersOnWebsocket(this);
			try{
				client.rmiServer.addRMIOnline(client, client.username);
				client.rmiServer.notifyRMIUserOn(client.username);
			}
			catch(java.rmi.RemoteException e){
				System.out.println(e);
			}
		}
		
		protected void onClose(int status) {
			connections.remove(this);
			client.unsetUsersOnWebsocket();
			try{
				client.rmiServer.logout(client.username);
			}
			catch(java.rmi.RemoteException e){
				System.out.println(e);
			}
		}

		protected void onTextMessage(CharBuffer message) throws IOException {
		}

		public void message(String message) { // send message to all
			try {
				CharBuffer buffer = CharBuffer.wrap(message);
				getWsOutbound().writeTextMessage(buffer);
			} catch (IOException ignore) {}
		}

		protected void onBinaryMessage(ByteBuffer message) throws IOException {
			throw new UnsupportedOperationException("Binary messages not supported.");
		}
	}
}
