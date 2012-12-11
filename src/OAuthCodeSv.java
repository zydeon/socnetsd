import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import socnet1.PM;
import socnet1.RMIClient;

public class OAuthCodeSv extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RMIClient c    = (RMIClient) request.getSession().getAttribute("rmi");
		String code    = request.getParameter("code");

		HttpSession session = request.getSession();
		session.setAttribute("authCode", code);

		c.initOAuth(code);
		response.sendRedirect("");
	}	
}
