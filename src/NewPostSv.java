import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.MultipartConfig;

import socnet1.Post;
import socnet1.RMIClient;
import asciiart.ASCIIConverter;


@MultipartConfig()
public class NewPostSv extends HttpServlet {

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RMIClient c = (RMIClient) request.getSession().getAttribute("rmi");
		String text = RMIClient.filter( request.getParameter("text") );
		//String text = getValue(request.getPart("text"));
		String src  = c.username;
		Part file = request.getPart("pic");
		Post p;

		if( file.getSize() > 0 ){
			String fileName = getFilename(file);
			String currentPath = request.getSession().getServletContext().getRealPath("/");
			file.write( currentPath + "images/"+fileName );
			p = new Post(src, text, fileName);
			ASCIIConverter conv = new ASCIIConverter();
			p.setImage( conv.convertAndResize(currentPath + "images/"+fileName) );
		}
		else{
			p = new Post(src, text);
		}

		c.rmiServer.addAndNotifyPostRMI( p );
		response.sendRedirect("timeline.jsp");
	}	


	private static String getFilename(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}
}
