import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import socnet1.RMIClient;

import java.rmi.registry.*;

public class LoginSv extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//PrintWriter out  = response.getWriter();

		String user = request.getParameter("user");
		String pass = request.getParameter("password");

		// ADICIONAR ISTO
		// escape(user);
		// escape(pass);

		RMIClient c = new RMIClient("localhost", 4444, true);
		HttpSession session = request.getSession();
		session.setAttribute("rmi", c);

		if( c.loginToRMI(user, pass)){
			System.out.println("DEUUUU!");
			response.sendRedirect("inputOAuthCode.jsp");
		}
		else{
			System.out.println("NAO DEU");
			session.invalidate();
			response.sendRedirect("login.jsp?msg=Username or password wrong");
		}
	}
}