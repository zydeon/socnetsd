import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import socnet1.Post;
import socnet1.RMIClient;

public class NewReplySv extends HttpServlet {

	// public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
	// 	request.getSession().invalidate();
	// 	response.sendRedirect("index.jsp");
	// }

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RMIClient c    = (RMIClient) request.getSession().getAttribute("rmi");
		String text    = RMIClient.filter( request.getParameter("text") );
		int parent     = Integer.parseInt( request.getParameter("parent") );
		int replyLevel = Integer.parseInt( request.getParameter("replyLvl") );
		String src     = c.username;
		Post p         = new Post(src, text, parent, replyLevel);

		c.rmiServer.replyPostRMI( p );
		//c.addComment(  )

		response.sendRedirect("timeline.jsp");
	}	
}
