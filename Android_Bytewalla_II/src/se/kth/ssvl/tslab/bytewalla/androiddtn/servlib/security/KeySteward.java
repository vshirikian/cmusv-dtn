/*
 *	  This file is part of the Master Thesis "Security in Bytewalla" by Sebastian Domancich.
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/1011248/".
 *    
 *    Copyright 2010 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */

package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import java.util.Enumeration;

import ext.org.bouncycastle.cms.CMSEnvelopedData;
import ext.org.bouncycastle.cms.CMSEnvelopedDataGenerator1;
import ext.org.bouncycastle.cms.CMSProcessable;
import ext.org.bouncycastle.cms.CMSProcessableByteArray;
import ext.org.bouncycastle.cms.RecipientId;
import ext.org.bouncycastle.cms.RecipientInformation;
import ext.org.bouncycastle.cms.RecipientInformationStore;


import android.content.res.Resources;
import android.util.Log;
import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNApps;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.SDNV;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.naming.EndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.IByteBuffer;

/**
 * This class is in charge of the key stewardship; i.e., it provides support for public key cryptography. 
 * The ciphersuites make use of symmetric key cryptography, that is fast for encryption, but distributing symmetric 
 * keys is difficult. public key are convenient for key distribution, but slow at encryption. As a consequence, 
 * the best solution is to use public key crytpo as a  secure means of distributing the keys for symmetric encryption
 * So far this class is a placeholder, as key transport is the next task to be implemented.
 * 
 * @author Sebastian Domancich (sdo@kth.se)
 */   
public class KeySteward 
{
	private static String TAG = "KeySteward";

	//	encrypts the key using a certificate
	/**
	 * This method encrypts the key using a certificate. we obtain a encrypted symmetric key, as a length-value pair (type is added in "generate"). 
	 * @param b (): not used.
	 * @param kpi (): not used.
	 * @param link (IN): not used.
	 * @param security_dest (): not used.
	 * @param data (IN): contains the key to be encrypted.
	 * @param data_len (IN): length of the key in the buffer buf. 
	 * @param db (OUT): encrypted symmetric key, as a length-value pair (type is added in "generate"). 
	 * @return  number of bytes successfully consumed, In case of error return -1
	 */    
	public static int encrypt(final  Bundle     b,
			KeyParameterInfo kpi,
			final Link    link,
			String       security_dest,
			byte[]           data,
			int            data_len,
			IByteBuffer       db) throws Exception
	{
		Log.d(TAG, String.format("encrypt().")); 

		//File file = new File(DTNManager.me.getResources().getString(R.string.keyStoreFilePath));
		
		Resources myCntx = DTNManager.me.getResources();
		//InputStream ins = new FileInputStream(file);
		InputStream ins = myCntx.openRawResource(R.raw.keystore);

		//int size = ins.available();

		// Read the entire resource into a local byte buffer.
		//byte[] buffer = new byte[size];
		//ins.read(buffer);
		//ins.close();

		//Log.d(TAG, String.format("*^*******************************************************************************")); 

		//Log.d(TAG, String.format("KeySteward.encrypt. len1: "+len)); 
		//String dest_str="dtn://tattoo.bytewalla.org";	

		KeyStore store1 = KeyStore.getInstance("BKS");
		char[]   password = ".".toCharArray();
		//store1.load(new FileInputStream("E:\\Downloads\\portecle-1.5\\portecle-1.5\\keystore_bytewalla_nexus.jks"), password);
		store1.load(ins, password);

		Enumeration en = store1.aliases();
		X509Certificate  cert=null;
		while (en.hasMoreElements())
		{

			String alias = (String)en.nextElement();
			//Log.d(TAG, String.format("found alias: " + alias+ ", isCertificate?: " + store1.isCertificateEntry(alias)));
			if (alias.equals(security_dest))
			{
				//Log.d(TAG, String.format("got it."));
				cert=(X509Certificate) store1.getCertificate(alias);
			}
		}

		//Certificate[]    chain = credentials.getCertificateChain(Utils.END_ENTITY_ALIAS_1);
		//cert = (X509Certificate)chain[0];

		// DERSet pepe= new DERSet();
		// set up the generator
		CMSEnvelopedDataGenerator1 gen = new ext.org.bouncycastle.cms.CMSEnvelopedDataGenerator1();

		//sd. we add the recipient's public key to the cms message. //it'll use that key to encrypt the sym key.
		//it also fills the rid field in KeyTransRecipientInfo.
		gen.addKeyTransRecipient(cert);

		// create the simmetric key.
		//CMSProcessable data = new CMSProcessableByteArray("Hello World!".getBytes());

		byte[]          key1 = new byte[16];//128 bits
		//key1[0]=1; key1[1]=2; key1[2]=1;  key1[3]=2; key1[4]=1;  key1[5]=2; key1[6]=1;  key1[7]=2; key1[8]=1;  key1[9]=2; key1[10]=1;  key1[11]=2; key1[12]=1;  key1[13]=2; key1[14]=1;  key1[15]=2;
		key1=data;

		//Keysteward.encrypt

		CMSProcessable data1 = new CMSProcessableByteArray(key1);

		//sd. we create a CMSEnveloped data, by encrypting the symmetric key with the public key from the certificate 
		//"cert".

		//Log.d(TAG, String.format("plain key before encryption: "));
		//System.out.println("length: "+enveloped.getEncoded().length);

		//for (int i=0; i<key1.length; i++)
		//{
		//	Log.d(TAG, String.format(String.format( "0x%x. |", key1[i])));
		//}
		//System.out.println();
		CMSEnvelopedData enveloped = gen.generate(data1, CMSEnvelopedDataGenerator1.AES128_CBC, "BC");

		byte[]          cMS_structure = new byte[enveloped.getEncoded().length];

		cMS_structure=enveloped.getEncoded();
		//Log.d(TAG, String.format("encoded key after encryption: "));
		//System.out.println("length: "+enveloped.getEncoded().length);

		//for (int i=0; i<enveloped.getEncoded().length; i++)
		//{
		//	Log.d(TAG, String.format( "0x%x. |", cMS_structure[i]));
		//}
		//System.out.println();

		Log.d(TAG, String.format("Encoding size of encrypted key. Size: "+enveloped.getEncoded().length));
		SDNV.encode(enveloped.getEncoded().length, db);

		//save encrypted symetric key in "db"!!!

		db.put(cMS_structure);

		// buf.put( encrypted data);
		//buf.rewind();

		db.rewind();

		//------------------------------------------------------------------------

		//Log.d(TAG, String.format("KeySteward.encrypt")); 
		//IByteBuffer   buf;
		//int len;
		//int    size;

		//len = data_len;
		//Log.d(TAG, String.format("KeySteward.encrypt. len1: "+len)); 
		//size = Math.max( (data_len + 4), 512 );
		//Log.d(TAG, String.format("KeySteward.encrypt. len2: "+len)); 
		//buf = db;
		//byte temp_key_array[] = {0,0x10}; //16 bytes
		//buf.put(temp_key_array);
		//buf.put(data);
		//buf.rewind();
		return 0;    
	}

	/**
	 * This method ...
	 * @param b (): 
	 * @param security_src (): 
	 * @param enc_data (IN): is the buffer buf, and now points to the encrypted symmetric key 
	 * @param enc_data_len (IN): length of the key in the buffer buf. 
	 * @param db (OUT): decrypted symmetric key. 
	 * @return  number of bytes successfully consumed, In case of error return -1
	 * @throws Exception 
	 */    
	public static int decrypt(final Bundle b,
			String   security_src,
			IByteBuffer       enc_data,
			long        enc_data_len,
			IByteBuffer   db) throws Exception
	{
		
		
		int init_pos = enc_data.position();
	

		if (enc_data_len < 2)
			return -1;

		

		
		long[] len = new long[1];
		//len[0]=enc_data_len;
		SDNV.decode(enc_data, len);
		Log.e(TAG, String.format("len of CMS encoded key field: "+len[0]));
		
		Resources myCntx = DTNManager.me.getResources();
		InputStream ins = myCntx.openRawResource(R.raw.keystore);

		//File file = new File(DTNManager.me.getResources().getString(R.string.keyStoreFilePath));
		//InputStream ins = new FileInputStream(file);
		
		char[]   password = ".".toCharArray();

		EndpointID        local_eid = BundleDaemon.getInstance().local_eid();
		//String my_alias="dtn://tattoo.bytewalla.org";
		String my_alias=local_eid.toString();
		//Log.e(TAG, String.format( "local_eid: "+my_alias+"****************************"));
		KeyStore store2 = KeyStore.getInstance("BKS");
		char[]   password2 = "keyPassword".toCharArray();
		// store2.load(new FileInputStream("E:\\Downloads\\portecle-1.5\\portecle-1.5\\keystore_bytewalla_tattoo.bks"), password);
		store2.load(ins, password);
		//Enumeration en1 = store2.aliases();
		PrivateKey       key =null;
		//while (en1.hasMoreElements())
		//{

		// String alias = (String)en1.nextElement();
		// System.out.println("found alias: " + alias+ ", isCertificate?: " + store1.isCertificateEntry(alias));
		// if (alias.equals(my_alias))
		//{
		// 	System.out.println("got it.");
		key=(PrivateKey) store2.getKey(my_alias, password2);

		CMSEnvelopedData enveloped;

		byte[]          cMS_structure = new byte[(int) len[0]];
		enc_data.get(cMS_structure);

		// re-create the CMSEnvelopedData object from its encoded form. we create the binary data from the object.
		//IMPORTANT: This will normally be encoded using the BER rules rather than the DER ones
		enveloped = new CMSEnvelopedData(cMS_structure);

		//Log.e(TAG, String.format( "first 10 bytes of encoded key before decryption: "));
		//Log.e(TAG, String.format( "Decrypt(). Encrypted key before decryption: "));
		//System.out.println("length: "+enveloped.getEncoded().length);

		//for (int i=0; i<enveloped.getEncoded().length; i++)
		//for (int i=0; i<10; i++)
		//{
		//	Log.e(TAG, String.format( "0x%x. |", cMS_structure[i]));
		//}

		//System.out.println();
		//PrivateKey       key = (PrivateKey)credentials.getKey(Utils.END_ENTITY_ALIAS_1, Utils.KEY_PASSWD);

		Certificate[]    chain = store2.getCertificateChain(my_alias);
		X509Certificate  cert1 = (X509Certificate)chain[0];


		// look for our recipient identifier
		RecipientId     recId = new RecipientId();

		recId.setSerialNumber(cert1.getSerialNumber());
		recId.setIssuer(cert1.getIssuerX500Principal().getEncoded());

		RecipientInformationStore   recipients = enveloped.getRecipientInfos();
		RecipientInformation        recipient = recipients.get(recId);
		byte[] recData=null;

		//byte[]          key1 = new byte[16];//128 bits
		//key1[0]=1; key1[1]=2; key1[2]=1;  key1[3]=2; key1[4]=1;  key1[5]=2; key1[6]=1;  key1[7]=2; key1[8]=1;  key1[9]=2; key1[10]=1;  key1[11]=2; key1[12]=1;  key1[13]=2; key1[14]=1;  key1[15]=2;

		if (recipient != null)
		{
			// decrypt the data

			//sd. takes the private key and decrypts the encrypted symmetric key, placing the symetric key in recData.
			recData = recipient.getContent(key, "BC");

		}
		else
		{
			Log.e(TAG, String.format("could not find a matching recipient"));
		}

		
		
		String key_str=""; 
		for (int i=0; i<recData.length;i++)
			key_str=new String(key_str+ String.format("%2.2h ", Ciphersuite_C3.unsignedByteToInt(recData[i])));

		Log.d(TAG, String.format(  "Decrypt(). symmetric key after decryption: 0x "+key_str));
		
		
		
		
		
		
		
		

		//we copy the decrypted key
		db.put(recData);

		//BufferHelper.copy_data(db, 0, enc_data, enc_data.position(), (int) len[0]);
		enc_data.position(init_pos);  

		return 0;    
	}

	static int sign(final Bundle    b,
			KeyParameterInfo kpi,
			final Link   link,
			String          data,
			int           data_len,
			IByteBuffer      db) //databuffer
	{
		return 0;
	}

	static int signature_length(final Bundle     b,
			KeyParameterInfo kpi,
			final Link    link,
			int            data_len,
			int           out_len)
	{
		return 0;
	}

	static int verify(final Bundle b,
			String       enc_data,
			int        enc_data_len,
			String       data,
			int        data_len)
	{
		return 0;
	}
}