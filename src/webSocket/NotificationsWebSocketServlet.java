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

public class NotificationsWebSocketServlet extends WebSocketServlet{
	
	private final Set<NotificationsMessageInbound> connections = new CopyOnWriteArraySet<NotificationsMessageInbound>();

	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
		return new NotificationsMessageInbound( (RMIClient) request.getSession().getAttribute("rmi") );
	}
	
	

	public final class NotificationsMessageInbound extends MessageInbound {
		
		private RMIClient client;
		
		private NotificationsMessageInbound( RMIClient c ) {
			this.client = c;
		}
		
		protected void onOpen(WsOutbound outbound) {
			connections.add(this);
			client.setNotificationsWebsocket(this);
		}
		
		protected void onClose(int status) {
			connections.remove(this);
			client.unsetNotificationsWebsocket();
		}

		protected void onTextMessage(CharBuffer message) throws IOException {
			// // never trust the client
			// String filteredMessage = filter(message.toString());
			// broadcast("&lt;" + nickname + "&gt; " + filteredMessage);
		}

		public void message(String message) { // send message to all
			try {
				CharBuffer buffer = CharBuffer.wrap(message);
				getWsOutbound().writeTextMessage(buffer);
			} catch (IOException ignore) {}
		}

		public void notifyPost() { // send message to all
			String message = "New post on timeline";
			try {
				CharBuffer buffer = CharBuffer.wrap(message);
				getWsOutbound().writeTextMessage(buffer);
			} catch (IOException ignore) {}
		}

		public void notifyPM() { // send message to all
			String message = "New PM!";
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
