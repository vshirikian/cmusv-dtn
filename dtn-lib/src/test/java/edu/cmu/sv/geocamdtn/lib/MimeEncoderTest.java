/**
 * 
 */
package edu.cmu.sv.geocamdtn.lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePartDataSource;

import junit.framework.TestCase;

/**
 * @author hbarnor
 *
 */
public class MimeEncoderTest extends TestCase {
	
	private Map<String, String[]> params = new HashMap<String,String[]>();

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		String[] value = {"2011-03-03 03:07:48"};
		params.put("cameraTime", value);
		value[0] = "37.3969559449682";
		params.put("latitude", value);
 		value[0] = "-122.09753102009303";
 		params.put("longitude", value);
 		value[0] = "-0.7124386429786682";
        params.put("roll", value);
        value[0] = "-10.63682746887207";
        params.put("pitch", value);
        value[0] = "375.8007173538208";
        params.put("yaw", value);
        value[0] = "";
        params.put("notes", value);
        value[0] = "default";  
        params.put("tags", value);
        value[0] = "0e9d12c4-46c2-4b88-a5c5-39d1ac7621ff";
        params.put("uuid", value);
        value[0] = "T";
        params.put("yawRef", value);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link edu.cmu.sv.geocamdtn.lib.MimeEncoder#toMime(java.util.Map, java.io.File)}.
	 */
	public void testToMime() {
		byte[] result = MimeEncoder.toMime(params, null);
		int expectedNumParts = params.size() - 1;
		// lets decode the result 
		ByteArrayInputStream resultStream = new ByteArrayInputStream(result);
		MimeBodyPart bodyPart = null;
		MimeMultipart multiPart = null;
		MimePartDataSource ds = null;
		try {
			bodyPart = new MimeBodyPart(resultStream);
			ds = new MimePartDataSource(bodyPart);
			multiPart = new MimeMultipart(ds);
			assertEquals(multiPart.getCount(), expectedNumParts);
			// assertEquals(multiPart.getBodyPart("roll").getContent().toString(), "-0.7124386429786682");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}
