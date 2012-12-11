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

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.io.IOException;

public class ChatWebSocketServlet extends WebSocketServlet {

	private final ConcurrentHashMap<String, ArrayList<ChatMessageInbound>> chatrooms = new ConcurrentHashMap<String, ArrayList<ChatMessageInbound>>();

	protected StreamInbound createWebSocketInbound(String subProtocol,
			HttpServletRequest request) {
		return new ChatMessageInbound( (String) request.getParameter("name"), (String) request.getParameter("username") );
	}

	private final class ChatMessageInbound extends MessageInbound {

		private final String chat_name;
		private final String username;

		private ChatMessageInbound(String name, String username) {
			this.chat_name =name;
			this.username = username;
		}

		protected void onOpen(WsOutbound outbound) {
			if( chatrooms.get(this.chat_name)==null )	// create chatroom
				chatrooms.put(this.chat_name, new ArrayList<ChatMessageInbound>());

			chatrooms.get(this.chat_name).add(this);
			broadcast("&lt;" + username + "&gt; entered");
		}

		protected void onClose(int status) {
			chatrooms.get(this.chat_name).remove(this);
			broadcast("&lt;" + username + "&gt; left");
		}

		protected void onTextMessage(CharBuffer message) throws IOException {
			// never trust the client
			String filteredMessage = filter(message.toString());
			broadcast("&lt;" + username + "&gt; " + filteredMessage);
		}

		private void broadcast(String message) { // send message to all
			for (ChatMessageInbound connection : chatrooms.get(this.chat_name)) {
				try {
					CharBuffer buffer = CharBuffer.wrap(message);
					connection.getWsOutbound().writeTextMessage(buffer);
				} catch (IOException ignore) {}
			}
		}

		public String filter(String message) {
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

		protected void onBinaryMessage(ByteBuffer message) throws IOException {
			throw new UnsupportedOperationException("Binary messages not supported.");
		}
	}
}