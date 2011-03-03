package edu.cmu.sv.geocamdtn;

import java.io.PrintWriter;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.mortbay.servlet.MultiPartRequest;



/* ------------------------------------------------------------ */
/** GeoCamDtnProxy Servlet 
 * 
 */
public class GeoCamDtnProxy extends HttpServlet 
{
    String proofOfLife = null;
    
    /* ------------------------------------------------------------ */
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config);
    	//to demonstrate it is possible
        Object o = config.getServletContext().getAttribute("org.mortbay.ijetty.contentResolver");
        android.content.ContentResolver resolver = (android.content.ContentResolver)o;
        android.content.Context androidContext = (android.content.Context)config.getServletContext().getAttribute("org.mortbay.ijetty.context");
        proofOfLife = androidContext.getApplicationInfo().packageName;
    }

    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
	// Lets the uuid to construct the receipt	
	// MultiPartRequest mPRequest = new MultiPartRequest(request);
	// String uuid_key = "uuid";
	// String uuid;
	// if (mPRequest.contains(uuid_key))
	//     {
	// 	uuid = mPRequest.getString(uuid_key);
	//     }
	sendResponse(response, "stuff");
    }
    
    private void sendResponse(HttpServletResponse response, String uuid) throws IOException
    {
        response.setContentType("text/html");
	PrintWriter out = response.getWriter();
        //ServletOutputStream out = response.getOutputStream();
        //out.println("<html>");
        // out.println("file posted <!--\nGEOCAM_SHARE_POSTED %s\n-->");
        // out.println("</html>");
        // out.flush();
    }

    
}
