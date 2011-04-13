package edu.cmu.sv.geocamdtn;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import android.content.Intent;
import android.util.Log;
import edu.cmu.sv.geocamdtn.lib.Constants;

/** 
 * GeoCamDTNProxy Servlet 
 * Receives the MIME encoded data from GeoCamLens Android application, 
 * Write MIME data to disk, and sends Intent with MIME file location to DTNMediatorService for DTN bundling
 * Also returns a successful receipt to the GeoCamLens Android application.
 */
public class GeoCamDTNProxy extends HttpServlet {
 
	private static final long serialVersionUID = 1L;
	private static final String TAG = "edu.cmu.sv.geocamdtn.ServletProxy";
	
	// static MIME variables
	private static final String BOUNDARY = "--------multipart_formdata_boundary$--------";
    private static final String CRLF = "\r\n";
		
	// unused
    private android.content.Context androidContext;

	public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	// to demonstrate it is possible
        // Object o = config.getServletContext().getAttribute("org.mortbay.ijetty.contentResolver");
        // android.content.ContentResolver resolver = (android.content.ContentResolver)o;
        androidContext = (android.content.Context)config.getServletContext().getAttribute("org.mortbay.ijetty.context");
    }

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// since we will call getParameter, we cannot use the inputstream or the buffered reader
    	// lets reecreate the MIME encoded data and write it to file
		File mime = writeMimeToDisk(request);	
		
		// then send it to the DTNMediatorService for bundling
    	sendToDTN(mime);

    	// unused
    	// String uuid = request.getParameter(UUID_KEY);
    	String fileName = request.getParameter(Constants.FILE_KEY);
		Log.d(TAG, "Filename is " + fileName);
		if (null != fileName) {
			sendResponse(response, fileName);
		} else {
			sendResponse(response, "");
		}
    }

    /**
     * Decode MIME from HTTP POST request and write to File.
     * @param request
     */
	private File writeMimeToDisk(HttpServletRequest request) {
		// can potentially use the multipart writer from jetty servlet
		// create unique file with MIME data on sdcard using uuid
		File mimeFile;
		FileOutputStream f;
		DataOutputStream out;
		try {
			mimeFile = File.createTempFile(request.getParameter("uuid"), null);
			f = new FileOutputStream(mimeFile);
			out = new DataOutputStream(f);
			// will delete temp files when phone java VM shuts down
			mimeFile.deleteOnExit();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "ERROR ---- FileNotFoundException while creating file to write MIME data in:" + e);
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			Log.e(TAG, "ERROR ---- IOException while creating file to write MIME data in:" + e);
			e.printStackTrace();
			return null;
		}
		
		// write standard MIME header info to file
		try {
			out.writeBytes("Content-Type: multipart/form-data; boundary=" + BOUNDARY + CRLF);
			out.writeBytes(CRLF);
		} catch (IOException e) {
			Log.e(TAG, "IOException whiles generating MIME data:" + e);
			e.printStackTrace();
		}
		
		
		// iterate over request parameters which contain metadata
    	@SuppressWarnings("unchecked")
		Iterator<String> iter = request.getParameterMap().keySet().iterator();
		String key;
		String value;
		String fileName = null;
		
		// encode MIME of metadata
		while (iter.hasNext()) {
			key = iter.next();
			value = request.getParameter(key);
			Log.i(TAG, "Adding " + key + " -> " + value + " to MIME file");
			if (!key.equalsIgnoreCase(Constants.FILE_KEY)) {
				try {
					out.writeBytes("--" + BOUNDARY + CRLF);
					out.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + CRLF);
					out.writeBytes(CRLF);
					out.writeBytes(value + CRLF);
				} catch (IOException e) {
					Log.e(TAG, "IOException whiles generating MIME data");
					e.printStackTrace();
				}
			} else {
				fileName = value;
			}
	    }

		// encode MIME of photo
		if (fileName != null) {
			// location of photo on disk
			File photo = (File)request.getAttribute(Constants.FILE_KEY);
			try {
				Log.i(TAG, "Adding PHOTO " + photo.getName() + "to MIME file");
				out.writeBytes("--" + BOUNDARY + CRLF);
				// now lets write out the file object 
				out.writeBytes("Content-Disposition: form-data; name=\"" + Constants.FILE_KEY
						+ "\"; filename=\"" + fileName + "\"" + CRLF);
				out.writeBytes("Content-Type: application/octet-stream" + CRLF);
				out.writeBytes(CRLF);
				FileInputStream fin = new FileInputStream(photo);
				
				/*
				 * Create byte array large enough to hold the content of the file.
				 * Use File.length to determine size of the file in bytes.
				 */
				byte fileContent[] = new byte[(int)photo.length()];
				// read the content into the array
				fin.read(fileContent);
				// now add the array to mime output
				out.write(fileContent);
			    out.writeBytes(CRLF);
		        out.writeBytes("--" + BOUNDARY + "--" + CRLF);
		        out.writeBytes(CRLF);
		        
		        fin.close();
		        fileContent = null;
			} catch (FileNotFoundException e) {
				Log.e(TAG, "File not found whiles reading " + photo.getName()
						+ " to encode into mime data");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "IOException whiles generating file part of mime data");
				e.printStackTrace();
			}
		}
		
		// clean up
		try {
	        out.flush();
	        out.close();
	        out = null;
	        f.flush();
	        f.close();
	        f = null;
		} catch (IOException e) {
			Log.e(TAG, "IOException whiles generating file part of mime data");
			e.printStackTrace();
		}
		
		return mimeFile;
	}
    
    /**
     * Send File with MIME to DTNMediatorService for DTN enqueing.
     * @param file
     */
    private void sendToDTN(File file) {    	
		Intent geoCamDTNIntent = new Intent(Constants.ACTION_SEND_DTN_BUNDLE);
		geoCamDTNIntent.putExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD, file);
		androidContext.startService(geoCamDTNIntent);	
    }

    /**
     * Send success response to GeoCamLens client
     * @param response
     * @param uuid
     * @throws IOException
     */
	private void sendResponse(HttpServletResponse response, String uuid) throws IOException {
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
