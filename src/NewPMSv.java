import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import socnet1.PM;
import socnet1.RMIClient;

public class NewPMSv extends HttpServlet {

	// public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
	// 	request.getSession().invalidate();
	// 	response.sendRedirect("index.jsp");
	// }

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RMIClient c    = (RMIClient) request.getSession().getAttribute("rmi");
		String text    = RMIClient.filter( request.getParameter("text") );
		String dest    = request.getParameter("dest");

		c.rmiServer.addAndNotifyPM_RMI( new PM(c.username, dest, text) );

		response.sendRedirect("pms.jsp");
	}	
}
