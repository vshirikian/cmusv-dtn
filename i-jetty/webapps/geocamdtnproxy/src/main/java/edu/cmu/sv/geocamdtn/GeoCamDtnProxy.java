package edu.cmu.sv.geocamdtn;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.util.Log;

// import org.mortbay.servlet.MultiPartRequest;



/* ------------------------------------------------------------ */
/** GeoCamDtnProxy Servlet 
 * 
 */
public class GeoCamDtnProxy extends HttpServlet 
{
    // DTNServiceConnector serviceConnector = null;
    
    /* ------------------------------------------------------------ */
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config);
    	//to demonstrate it is possible
        Object o = config.getServletContext().getAttribute("org.mortbay.ijetty.contentResolver");
        android.content.ContentResolver resolver = (android.content.ContentResolver)o;
        android.content.Context androidContext = (android.content.Context)config.getServletContext().getAttribute("org.mortbay.ijetty.context");
        
        // serviceConnector = new DTNServiceConnector(context);
    }

    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
	// Just some debug for the params 
	Map params = request.getParameterMap();
	Iterator i = params.keySet().iterator();
	while ( i.hasNext() )
	    {
		String key = (String) i.next();
		String value = ((String[]) params.get( key ))[ 0 ];
		Log.d("GeoCamDtnProxy", key + " " + value);
	    }

	String uuidKey = "uuid";
	String uuid = request.getParameter(uuidKey);
	String fileNameKey = "photo";
	String fileName = request.getParameter(fileNameKey);
	Log.d("GeoCamDtnProxy", "Filename is " + fileName);
	if (null != fileName)
	    {
		sendResponse(response, fileName);
	    }
	else 
	    {
		sendResponse(response, "");
	    }
    	// Send bundle
    	// serviceConnector.bindDTNService();
    	//serviceConnector.send(mPRequest.);
    	// serviceConnector.unbindDTNService();
    }
    
    private void sendResponse(HttpServletResponse response, String uuid) throws IOException
    {
        response.setContentType("text/html");        
	String filePosted = "file posted <!--\nGEOCAM_SHARE_POSTED ";
	String endFilePosted = "\n-->";
	StringBuffer buf = new StringBuffer();
	buf.append(filePosted);
	buf.append(uuid);
	buf.append(endFilePosted);
	PrintWriter out = response.getWriter();
	out.println("<html>");
	out.println(buf);
        out.println("</html>");
        out.flush();
	out.close();
    }

    
}
