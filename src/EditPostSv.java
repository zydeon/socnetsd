import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.MultipartConfig;

import socnet1.Post;
import socnet1.RMIClient;

public class EditPostSv extends HttpServlet {

	// public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
	// 	request.getSession().invalidate();
	// 	response.sendRedirect("index.jsp");
	// }

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RMIClient c = (RMIClient) request.getSession().getAttribute("rmi");
		String text = RMIClient.filter( request.getParameter("text") );
		int postID = Integer.parseInt(request.getParameter("postID"));

		if(c.username.equals( c.rmiServer.getPostSource(postID) )  ){
			c.rmiServer.updatePost(text, postID);
			response.sendRedirect("timeline.jsp");
		}
		else{
			response.sendRedirect("timeline.jsp?msg=Boa tentativa, professor Alcides! Tente no grupo do Ruben");
		}	
	}	
}
