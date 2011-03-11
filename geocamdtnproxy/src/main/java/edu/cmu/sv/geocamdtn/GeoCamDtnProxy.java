package edu.cmu.sv.geocamdtn;

import java.io.File;
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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import edu.cmu.sv.geocamdtn.lib.Constants;



/* ------------------------------------------------------------ */
/** 
 * GeoCamDtnProxy Servlet 
 * Receives the mime encoded data from geocam and sends it to the
 * geocam dtn service for dtn transport.
 * Returns a transmitted receipt to the geocamlens application. 
 *
 */
public class GeoCamDtnProxy extends HttpServlet 
{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TAG = "edu.cmu.sv.geocamdtn.ServletProxy";
    private android.content.Context androidContext;

    /* ------------------------------------------------------------ */
    public void init(ServletConfig config) throws ServletException
    {
    	super.init(config);
    	//to demonstrate it is possible
        // Object o = config.getServletContext().getAttribute("org.mortbay.ijetty.contentResolver");
        // android.content.ContentResolver resolver = (android.content.ContentResolver)o;
        androidContext = (android.content.Context)config.getServletContext().getAttribute("org.mortbay.ijetty.context");
    }

    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	// Just some debug for the params 
    	Map<String, String[]> params = request.getParameterMap();
    	File file = (File) request.getAttribute( Constants.FILE_KEY );
    	// since we will call getParameter, we cannot use the inputstream or the buffered reader
    	// so lets reecreate the mime encoded data and then send it to dtn
    	sendToDTN(params, file);

    	Iterator i = params.keySet().iterator();
    	while ( i.hasNext() )
    	{
    		String key = (String) i.next();
    		String value = ((String[]) params.get( key ))[ 0 ];
    		Log.d(TAG, key + " " + value);
    	}

    	String uuidKey = "uuid";
    	String uuid = request.getParameter(uuidKey);
    	String fileNameKey = "photo";
    	String fileName = request.getParameter(fileNameKey);
    	Log.d(TAG, "Filename is " + fileName);
    	if (null != fileName)
	    {
    		sendResponse(response, fileName);
	    }
    	else 
	    {
    		sendResponse(response, "");
	    }
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

    /**
     * Add the parameters to an intent and send it to the
     * GeoCamDTN service for dtn enqueing.
     *
     */
    private void sendToDTN(Map params, File file)
    {
		// lets create a bundle with all that we need
		Bundle data = new Bundle();
		Iterator i = params.keySet().iterator();
		String key;
		String[] values;

		while ( i.hasNext() )
		    {
			key = (String) i.next();
			values = ((String[]) params.get( key ));
			data.putStringArray(key, values);
		    }
		data.putSerializable(Constants.FILE_KEY, file);
		Intent geoCamDTNIntent = new Intent(Constants.ACTION_CREATE_DTN_BUNDLE);
		geoCamDTNIntent.putExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD, data);
		androidContext.startService(geoCamDTNIntent);	
    }
    
}
