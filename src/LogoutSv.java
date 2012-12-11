import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import socnet1.RMIClient;

public class LogoutSv extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		RMIClient c = (RMIClient) request.getSession().getAttribute("rmi");
		c.logout();
		request.getSession().invalidate();
		response.sendRedirect("index.jsp");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);	
	}	
}
