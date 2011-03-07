/**
 * 
 */
package edu.cmu.sv.geocamdtn.lib;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import android.util.Log;


/**
 * Takes a map and generates a DTN MimeEncoded byterray.
 * 
 * @author hbarnor
 *
 */
public class MimeEncoder {
	
	private static final String BOUNDARY = "--------multipart_formdata_boundary$--------";
    private static final String CRLF = "\r\n";
    private static final String TAG = "GeoCamDTNLib::MimeEncoder";


	public static byte[] toMime(Map<String,String[]> params, File file)
	{
		// can potentially use the multipart writer from jetty servlet
		ByteArrayOutputStream bufferedStream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bufferedStream);
		String [] values;
		String fileName = null;
		for (String key : params.keySet()) 
			{
				// Don't create a filename part yet 
				if (!key.equalsIgnoreCase("filename"))
				{
					try 
					{
						out.writeBytes("--" + BOUNDARY + CRLF);
						out.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + CRLF);
						out.writeBytes(CRLF);
						values = params.get(key);
						for (String value : values)
						{
							out.writeBytes(value + CRLF);
						}
					} 
					catch (IOException e) 
					{
						Log.d(TAG, "IOException whiles generating Mime data");
						e.printStackTrace();
					}
				}
				else
				{
					fileName = params.get(key)[0];
				}
	        }
		
		if (null != fileName)
		{

			try 
			{
				out.writeBytes("--" + BOUNDARY + CRLF);
				// Now lets write out the file object 
				out.writeBytes("Content-Disposition: form-data; name=\"" + Constants.FILE_KEY
						+ "\"; filename=\"" + fileName + "\"" + CRLF);
				out.writeBytes("Content-Type: application/octet-stream" + CRLF);
				out.writeBytes(CRLF);
				FileInputStream fin = new FileInputStream(file);
				/*
				 * Create byte array large enough to hold the content of the file.
				 * Use File.length to determine size of the file in bytes.
				 */
				byte fileContent[] = new byte[(int)file.length()];
				// read the content into the array
				fin.read(fileContent);
				// now add the array to mime output
				out.write(fileContent);
			    out.writeBytes(CRLF);
		        out.writeBytes("--" + BOUNDARY + "--" + CRLF);
		        out.writeBytes(CRLF);
			} 
			catch(FileNotFoundException e)
		    {
				Log.d(TAG, "File not found whiles reading " + file.getName() + " to encode into mime data");
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				Log.d(TAG, "IOException whiles generating file part of mime data");
				e.printStackTrace();
			}
		}
		return bufferedStream.toByteArray();
	}
	
	
}
