import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Date;
import socnet1.RMIClient;

public class RegisterSv extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String user = request.getParameter("user");
		String pass = request.getParameter("password");

		RMIClient c = new RMIClient("localhost", 4444, true);
		HttpSession session = request.getSession();	
		session.setAttribute("rmi", c);

		if(c.registerToRMI(user, pass)){
			c.setUsername(user);
			c.setPassword(pass);
			System.out.println("DEU!");
			response.sendRedirect("inputOAuthCode.jsp");
		}
		else{
			session.invalidate();
			System.out.println("NAO DEU!");
			response.sendRedirect("register.jsp?msg=User already exists");
		}	
	}

}
