import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import socnet1.Post;
import socnet1.RMIClient;

public class DeletePostSv extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int id = Integer.parseInt(request.getParameter("id"));

		RMIClient c = (RMIClient) request.getSession().getAttribute("rmi");

		if(c.username.equals( c.rmiServer.getPostSource(id) )  ){
			c.rmiServer.deletePost( id );
			response.sendRedirect("timeline.jsp");
		}
		else{
			response.sendRedirect("timeline.jsp?msg=Boa tentativa, professor Alcides! Tente no grupo do Ruben");
		}
	}	
}
